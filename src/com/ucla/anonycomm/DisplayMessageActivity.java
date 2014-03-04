package com.ucla.anonycomm;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		Intent intent = getIntent();
		m_message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		TextView t_msg = (TextView) findViewById(R.id.sent_message);
		t_msg.setTextSize(20);
		t_msg.setText(m_message);

		// Create the image view
		ImageView imageView = (ImageView) findViewById(R.id.sent_image);
		m_imagePath = intent.getStringExtra(MainActivity.EXTRA_PATH);
		Bitmap image = BitmapFactory.decodeFile(m_imagePath);
		imageView.setImageBitmap(image);

		new SendMsg().execute();
	}
	
	// update m_ip and m_port inside onResume
	// as they need to be updated every time settings_activity is called
	@Override
	protected void onResume() {
		
		// without super.onResume, it will crash
		super.onResume();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		m_ip = settings.getString("setIP", "108.168.239.90");
		m_port = settings.getString("setPort", "8080");
		m_encry = settings.getBoolean("setEncry", false);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	 private class SendMsg extends AsyncTask<Void, Void, Integer> {
	        @Override
	        protected Integer doInBackground(Void... arg) {
	    		int resp1 = 0, resp2 = 0;
	    		try {
	    			// send photo only if the path is non empty
	    	    	if(!m_imagePath.isEmpty()) {
	    	    		HttpClient httpclient2 = new DefaultHttpClient();
	    	    		HttpPost httppost2 = new HttpPost("http://" + m_ip + ":" + m_port + "/session/send");
	    	    		
	    	    		// Get the bytes of the image
	    	    		File f = new File(m_imagePath);
	    	    		byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(f);
	    	    		// Convert the bytes into hexadecimal string
	    	    		String contenthex = "";
	    	    		for (int i = 0; i < content.length; i++) {
	    	    			String hex = Integer.toHexString(0xFF & content[i]);
	    	    			if(hex.length() == 1)
	    	    				contenthex += "0";
	    	    			contenthex += hex;
	    	    		}
	    	    		
	    	    		// Execute the post request
	    	    		HttpEntity entity2 = new ByteArrayEntity(contenthex.getBytes("UTF-8"));
	    	    		httppost2.setEntity(entity2);
	    	    		
	    	    		HttpResponse hresp1 = httpclient2.execute(httppost2);
	    	    		if(hresp1 == null)
	    	    			resp1 = -1;
	    	    		else
	    	    			resp1 = (hresp1.getStatusLine().getStatusCode() == 200) ? 0 : -1;
	    	    	}
	    		} catch (Exception e) {
	    			
	    		}
	    	    try {
	    	    	// send only when a user needs to send message
	    	    	if (!m_message.isEmpty()) {
	    	        	HttpClient httpclient = new DefaultHttpClient();
	    	    	    HttpPost httppost = new HttpPost("http://" + m_ip + ":" + m_port + "/session/send");
	    	    		
	    	    	    HttpEntity entity = new ByteArrayEntity(m_message.getBytes("UTF-8"));
	    	    	    httppost.setEntity(entity);
		    	        // Execute HTTP Post Request
	    	    	    HttpResponse hresp2 = httpclient.execute(httppost);
	    	    	    if (hresp2 == null)
	    	    	    	resp2 = -1;
	    	    	    else
	    	    	    	resp2 = (hresp2.getStatusLine().getStatusCode() == 200) ? 0 : -1;
	    	    	}
	    	    	
	    	    } catch (Exception e) {
	    	    	
	    	    }
	    	    // if both are zero, should sum to zero
    	    	return resp2 + resp1;
	        }
	        // onPostExecute displays the results of the AsyncTask.
	        @Override
	        protected void onPostExecute(Integer resp) {
    	        if (resp == 0) {
    	        	Toast.makeText(DisplayMessageActivity.this,
        				"Sent Successfully", Toast.LENGTH_LONG).show();
    	        } else {
    	        	Toast.makeText(DisplayMessageActivity.this,
        				"Failed to send"  , Toast.LENGTH_LONG).show();
    	        }
	       }
 	}	   
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	public void resend_msg(View view){
		new SendMsg().execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private String m_ip;  // Dissent server IP, default to "108.168.239.90"
	private String m_port;  // Dissent server port, default to "8080"
	private boolean m_encry;  // if true, encrypt messages with AES (TODO)
	private String m_imagePath;
	private String m_message;
}
