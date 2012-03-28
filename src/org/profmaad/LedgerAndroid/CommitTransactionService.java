package org.profmaad.LedgerAndroid;

import android.app.IntentService;

import android.content.Intent;
import android.app.PendingIntent;
import android.content.Context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.app.Notification;
import android.app.NotificationManager;

import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.NameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class CommitTransactionService extends IntentService
{
	private static final String EXTRA_TRANSACTION_JSON = "transaction_json";
	private static final String ENDPOINT_RESOURCE = "/transactions";
	private static final String TAG = "LedgerAndroid";
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final int NOTIFICATION_ERROR = 1;

	private String endpointUrl;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		endpointUrl = prefs.getString("wsuri", "http://10.0.2.2:9292");

		return super.onStartCommand(intent, flags, startId);
	}

	public CommitTransactionService()
	{
		super("CommitTransactionService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		String transactionJson = intent.getStringExtra(EXTRA_TRANSACTION_JSON);

		HttpClient httpClient = new DefaultHttpClient();

		HttpPost postRequest = new HttpPost(endpointUrl+ENDPOINT_RESOURCE);
		postRequest.setHeader("Content-Encoding", DEFAULT_ENCODING);

		try
		{
			List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
			postData.add(new BasicNameValuePair("transaction", transactionJson));
			postRequest.setEntity(new UrlEncodedFormEntity(postData, DEFAULT_ENCODING));

			HttpResponse response = httpClient.execute(postRequest);
			WebserviceResponse webserviceResponse = getWebserviceResponse(response);
			if(webserviceResponse == null) { return; }
			
			if( webserviceResponse.isError())
			{
				Log.e(TAG, "Failed to commit transaction: "+webserviceResponse.getMessage());
				postErrorNotification(webserviceResponse.getMessage(), transactionJson);
			}			
		}
		catch(ClientProtocolException e)
		{
			Log.e(TAG, "Failed to commit transaction: "+e.toString());
			postErrorNotification(e.toString(), transactionJson);
			return;
		}
		catch(IOException e)
		{
			Log.e(TAG, "Failed to commit transaction: "+e.toString());
			postErrorNotification(e.toString(), transactionJson);
			return;
		}
	}
	
	private WebserviceResponse getWebserviceResponse(HttpResponse response) throws IOException
	{
		if(response.getEntity() == null) { return null; }

		String encoding = DEFAULT_ENCODING;
		if(response.getEntity().getContentEncoding() != null) { encoding = response.getEntity().getContentEncoding().getValue(); }

		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));
		
		Gson gson = new Gson();

		WebserviceResponse result = null;
		try
		{
			result = gson.fromJson(reader, WebserviceResponse.class);
		}
		catch(JsonParseException e)
		{
			Log.e(TAG, "Failed to parse response from webservice: "+e.toString());
			return null;
		}

		return result;
	}

	private void postErrorNotification(String reason, String transactionJson)
	{
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, CreateTransaction.class);
		intent.putExtra(EXTRA_TRANSACTION_JSON, transactionJson);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Notification notification = new Notification(android.R.drawable.stat_notify_error, "Error", System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), "Failed to commit transaction", reason, pIntent);

		manager.notify(NOTIFICATION_ERROR, notification);
	}
}