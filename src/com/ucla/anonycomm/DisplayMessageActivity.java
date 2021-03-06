package com.ucla.anonycomm;

import java.io.ByteArrayOutputStream;

import org.abstractj.kalium.keys.KeyPair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
		
		//share preferences from settings
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		m_ip = sharedPref.getString("setIP", "108.168.239.90");
		m_port = sharedPref.getString("setPort", "8080");
		
		
		SharedPreferences keySetting = getSharedPreferences(MainActivity.PREF, 0);
		m_priKey = keySetting.getString("priKey",null);
		m_pubKey = keySetting.getString("pubKey",null);
		
		
		if(m_priKey == null || m_pubKey == null){
        	Toast.makeText(this,
    				"Generating Key Pairs", Toast.LENGTH_LONG).show();
        	
        	//generate key pairs
        	KeyPair keys = new KeyPair();
        	m_priKey = keys.getPrivateKey().toString();
        	m_pubKey= keys.getPublicKey().toString();
        	
        	//Add keys to the preference List
        	SharedPreferences settings = getSharedPreferences(MainActivity.PREF, 0);
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putString("pubKey", keys.getPublicKey().toString());
        	editor.putString("priKey", keys.getPrivateKey().toString());
        	editor.commit();
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
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		m_ip = sharedPref.getString("setIP", "108.168.239.90");
		m_port = sharedPref.getString("setPort", "8080");

		SharedPreferences keySetting = getSharedPreferences(MainActivity.PREF, 0);
		m_priKey = keySetting.getString("priKey",null);
		m_pubKey = keySetting.getString("pubKey",null);
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
		 
		 private ProgressDialog dialog = new ProgressDialog(DisplayMessageActivity.this);
		 
		 @Override
		 protected void onPreExecute() {
		        this.dialog.setMessage("Sending. Be prepared to wait till the battery runs up :(");
		        this.dialog.show();
		 }
		 
	        @Override
	        protected Integer doInBackground(Void... arg) {
	    		int resp1 = 0, resp2 = 0;
	    		try {
	    			// send photo only if the path is non empty
	    	    	    if((m_imagePath!=null)&&!m_imagePath.isEmpty()) {
	    	    		HttpClient httpclient2 = new DefaultHttpClient();
	    	    		HttpPost httppost2 = new HttpPost("http://" + m_ip + ":" + m_port + "/session/send");
	    	    		
	    	    		
	    	    		// Get the bytes of the image
	    	    		//File f = new File(m_imagePath);
	    	    		Bitmap bm = BitmapFactory.decodeFile(m_imagePath);
	    	    		Bitmap resized = Bitmap.createScaledBitmap( bm, 80, 80, true);
	    	    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
				        resized.compress(CompressFormat.JPEG, 40, bos);
	    	    		//byte[] content = org.apache.commons.io.FileUtils.readFileToByteArray(f);
	    	    		byte[] content = bos.toByteArray();
	    	    		System.out.println(content.length);
				        // Convert the bytes into hexadecimal string
	    	    		String contenthex = "";
	    	    		for (int i = 0; i < content.length; i++) {
	    	    			String hex = Integer.toHexString(0xFF & content[i]);
	    	    			if(hex.length() == 1)
	    	    				contenthex += "0";
	    	    			contenthex += hex;
	    	    		}
	    	    		
	    	    		contenthex = "imge_" + contenthex+"_"+m_pubKey;
	    	    		
	    	    		
	    	    		// Execute the post request
	    	    		HttpEntity entity2 = new ByteArrayEntity(contenthex.getBytes("UTF-8"));
	    	    		httppost2.setEntity(entity2);
	    	    		
	    	    		HttpResponse hresp1 = httpclient2.execute(httppost2);
	    	    		if(hresp1 == null){
	    	    			resp1=-1;
	    	    		}
	    	    		else{
	    	    			resp1 = (hresp1.getStatusLine().getStatusCode() == 200) ? 0 : -1;
	    	    		}
	    	    	}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			resp1 = -1;
	    		}
	    	    try {
	    	    	// send only when a user needs to send message
	    	    	if (m_message!=null && !m_message.isEmpty()) {
	    	        	HttpClient httpclient = new DefaultHttpClient();
	    	    	        HttpPost httppost = new HttpPost("http://" + m_ip + ":" + m_port + "/session/send");

	    	    	        
	    	    		HttpEntity entity;
	    	    		String text;
	    	    		
	    	    		text = "text_" + m_message  + "_" + m_pubKey;
	    	    		
						entity = new ByteArrayEntity(text.getBytes("UTF-8"));
	    	    		httppost.setEntity(entity);
		    	        // Execute HTTP Post Request
	    	    	        HttpResponse hresp2 = httpclient.execute(httppost);
	    	    	        if (hresp2 == null || hresp2.getStatusLine().getStatusCode() != 200)
	    	    	    	    resp2 = -1;
	    	            }
	    	        } catch (Exception e) {

	    	        	resp2 = -1;
	    	        }
	    	        // if both are zero, should sum to zero
    	    	        return resp2 + resp1;
	        }
	        // onPostExecute displays the results of the AsyncTask.
	        @Override
	        protected void onPostExecute(Integer resp) {
	            if (dialog.isShowing()) {
	                dialog.dismiss();
	            }
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
	private String m_imagePath;
	private String m_message;
	private String m_priKey;
	private String m_pubKey;
}
