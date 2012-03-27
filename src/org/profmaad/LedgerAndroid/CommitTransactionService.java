package org.profmaad.LedgerAndroid;

import android.app.IntentService;

import android.content.Intent;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.NameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class CommitTransactionService extends IntentService
{
	private static final String EXTRA_TRANSACTION_JSON = "transaction_json";

	private String endpointUrl;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		endpointUrl = prefs.getString("wsuri", "");

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

		HttpPost postRequest = new HttpPost(endpointUrl);

		try
		{
			List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
			postData.add(new BasicNameValuePair("transaction", transactionJson));
			postRequest.setEntity(new UrlEncodedFormEntity(postData));

			HttpResponse response = httpClient.execute(postRequest);
			int responseStatus = response.getStatusLine().getStatusCode();
			
		}
		catch(ClientProtocolException e)
		{
		}
		catch(IOException e)
		{
		}
	}
}