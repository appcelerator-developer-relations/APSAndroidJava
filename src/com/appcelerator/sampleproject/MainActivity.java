package com.appcelerator.sampleproject;

import java.util.HashMap;
import org.json.JSONArray;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Build;

import com.appcelerator.aps.APSAnalytics;
import com.appcelerator.aps.APSCloudException;
import com.appcelerator.aps.APSPerformance;
import com.appcelerator.aps.APSResponse;
import com.appcelerator.aps.APSResponseHandler;
import com.appcelerator.aps.APSServiceManager;
import com.appcelerator.aps.APSUsers;

public class MainActivity extends ActionBarActivity {
	
	private static Activity currentActivity;
	private static final APSAnalytics analytics = APSAnalytics.getInstance();
	private static final APSPerformance performance = APSPerformance.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentActivity = this;
        APSServiceManager.getInstance().enable(getApplicationContext(), "APS_APP_KEY");
        APSAnalytics.getInstance().sendAppEnrollEvent();
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }
    
    @Override
    public void onPause(){
        super.onPause();
        analytics.sendAppBackgroundEvent();
    }

    @Override
    public void onResume(){
        super.onResume();
        analytics.sendAppForegroundEvent();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	Spinner spinner;
    	EditText textField;
    	
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            Button button = (Button)rootView.findViewById(R.id.button1);
            button.setOnClickListener(new Button.OnClickListener()
            {
                @Override
                public void onClick(View v){
                    doClick();
                }
            });            spinner = (Spinner)rootView.findViewById(R.id.spinner1);
            textField = (EditText)rootView.findViewById(R.id.editText1);
            populateSpinner();
            return rootView;
        }
      
		public void doClick(){
			analytics.sendAppFeatureEvent("sample.feature.login", null);
			
			final String username = spinner.getSelectedItem().toString();
			String password = textField.getText().toString();
		    
			// Use a HashMap to send the method parameters for the REST request
		    HashMap<String, Object> data = new HashMap<String, Object>();
		    data.put("login", username);
		    data.put("password", password);
		    
		    // Need to place Cloud calls in a try-catch block since it may throw an APSClientError
		    try {
		        APSUsers.login(data, new APSResponseHandler() {
		            // This callback will run in a background thread
		            @Override
		            public void onResponse(final APSResponse e) {
		                if (e.getSuccess()) {
		                	Log.i("ACSUsers", "Successfully logged in as " + username);
		                	performance.setUsername(username);
		                }
		                else {
		                    Log.e("ACSUsers", e.getResponseString());
		                }                   
		            }
		            
		            @Override
		            public void onException(APSCloudException e) {
		            	Log.e("ACSUsers", e.toString());
		            }
		        });
		    } catch (APSCloudException e) {
		        Log.e("ACSUsers", e.toString());
		    }
		    
		    try {
		    	throw new Exception("Something happened...");
		    } catch (Exception exception) {
		    	performance.logHandledException(exception);
		    }
		}
        
		public void populateSpinner() {
		    try {
		        APSUsers.query(null, new APSResponseHandler() {
		            @Override
		            public void onResponse(final APSResponse e) {
                        if (e.getSuccess()) {
                            try {
                                JSONArray payload = e.getResponse().getJSONArray("users");
                                String[] items = new String[payload.length()];
                                for (int i = 0; i < payload.length(); i++) {
                                	items[i] = payload.getJSONObject(i).getString("username");
                                }
                                ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(currentActivity,
                                        android.R.layout.simple_spinner_item, items);
                                spinner.setAdapter(spinnerArrayAdapter); 
                            } catch (Exception ex) {
                                Log.e("ACSUsers", "Error parsing JSON object: " + ex.toString());
                            }
		                }		                
		                else {
		                	Toast.makeText(currentActivity, "ERROR: Unable to get users.", Toast.LENGTH_SHORT).show();
		                    Log.e("ACSUsers", e.getResponseString());
		                }                   
		            }
		            
		            @Override
		            public void onException(APSCloudException e) {
		            	Log.e("ACSUsers", e.toString());
		            }
		        });
		    } catch (APSCloudException e) {
		        Log.e("ACSUsers", e.toString());
		    }     
		}
    }

}
