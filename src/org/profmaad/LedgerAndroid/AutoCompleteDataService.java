package org.profmaad.LedgerAndroid;

import android.app.IntentService;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class AutoCompleteDataService extends IntentService
{
	private static final String EXTRA_PACKAGE = "org.profmaad.LedgerAndroid.";
	private static final String EXTRA_TRANSACTION_JSON = "transaction_json";
	private static final String EXTRA_ACCOUNTS_LIST = "accounts";
	private static final String EXTRA_PAYEES_LIST = "payees";
	private static final String ACCOUNTS_RESOURCE = "/accounts";
	private static final String PAYEES_RESOURCE = "/payees";
	private static final String TAG = "LedgerAndroid";
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String INTENT_AUTOCOMPLETE_DATA = "autocomplete-data-received";

	private String endpointUrl;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		endpointUrl = prefs.getString("wsuri", "http://10.0.2.2:9292");

		return super.onStartCommand(intent, flags, startId);
	}

	public AutoCompleteDataService()
	{
		super("AutoCompleteDataService");
	}

	protected void onHandleIntent(Intent intent)
	{
		HttpClient httpClient = new DefaultHttpClient();

		HttpGet accountsRequest = new HttpGet(endpointUrl+ACCOUNTS_RESOURCE);
		accountsRequest.setHeader("Content-Encoding", DEFAULT_ENCODING);
		HttpGet payeesRequest = new HttpGet(endpointUrl+PAYEES_RESOURCE);
		payeesRequest.setHeader("Content-Encoding", DEFAULT_ENCODING);

		Intent resultIntent = new Intent(INTENT_AUTOCOMPLETE_DATA);

		Gson gson = new Gson();

		try
		{			
			HttpResponse accountsResponse = httpClient.execute(accountsRequest);
			if(accountsResponse.getStatusLine().getStatusCode() != 200)
			{
				WebserviceResponse error = WebserviceResponse.createFromHttpResponse(accountsResponse);
				if(error != null && error.isError())
				{
					Log.e(TAG, "Failed to retrieve autocomplete data: "+error.getMessage());
				}
			}
			else
			{
				AccountsList accounts = AccountsList.createFromHttpResponse(accountsResponse);
				if(accounts != null)
				{
					resultIntent.putStringArrayListExtra(EXTRA_PACKAGE+EXTRA_ACCOUNTS_LIST, accounts.getAccounts());
				}
				else
				{
					Log.e(TAG, "Failed to retrieve autocomplete data: no accounts received");
				}
			}

			HttpResponse payeesResponse = httpClient.execute(payeesRequest);
			if(payeesResponse.getStatusLine().getStatusCode() != 200)
			{
				WebserviceResponse error = WebserviceResponse.createFromHttpResponse(payeesResponse);
				if(error != null && error.isError())
				{
					Log.e(TAG, "Failed to retrieve autocomplete data: "+error.getMessage());
				}
			}
			else
			{
				PayeesList payees = PayeesList.createFromHttpResponse(payeesResponse);
				if(payees != null)
				{
					resultIntent.putStringArrayListExtra(EXTRA_PACKAGE+EXTRA_PAYEES_LIST, payees.getPayees());
				}
				else
				{
					Log.e(TAG, "Failed to retrieve autocomplete data: no payees received");
				}
			}

			LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
		}
		catch(ClientProtocolException e)
		{
			Log.e(TAG, "Failed to retrieve autocomplete data: "+e.toString());
			return;
		}
		catch(IOException e)
		{
			Log.e(TAG, "Failed to retrieve autocomplete data: "+e.toString());
			return;
		}					
	}
}