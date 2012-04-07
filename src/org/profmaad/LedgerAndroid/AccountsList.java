package org.profmaad.LedgerAndroid;

import java.util.ArrayList;

import java.io.IOException;
import java.io.BufferedReader;

import android.util.Log;

import org.apache.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class AccountsList
{
	private static final String TAG = "LedgerAndroid";

	private ArrayList<String> accounts;

	public AccountsList() {};

	public ArrayList<String> getAccounts() { return accounts; }

	public static AccountsList createFromHttpResponse(HttpResponse response) throws IOException
	{
		BufferedReader reader = HttpResponseBodyHelper.getBodyReader(response);
		if(reader == null) { return null; }
		
		Gson gson = new Gson();

		AccountsList result = null;
		try
		{
			result = gson.fromJson(reader, AccountsList.class);
		}
		catch(JsonParseException e)
		{
			Log.e(TAG, "Failed to parse AccountsList from HttpResponse: "+e.toString());
			return null;
		}

		return result;
	}
}
