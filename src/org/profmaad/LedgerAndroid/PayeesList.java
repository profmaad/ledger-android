package org.profmaad.LedgerAndroid;

import java.util.ArrayList;

import java.io.IOException;
import java.io.BufferedReader;

import android.util.Log;

import org.apache.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class PayeesList
{
	private static final String TAG = "LedgerAndroid";

	private ArrayList<String> payees;

	public PayeesList() {};

	public ArrayList<String> getPayees() { return payees; }

	public static PayeesList createFromHttpResponse(HttpResponse response) throws IOException
	{
		BufferedReader reader = HttpResponseBodyHelper.getBodyReader(response);
		if(reader == null) { return null; }
		
		Gson gson = new Gson();

		PayeesList result = null;
		try
		{
			result = gson.fromJson(reader, PayeesList.class);
		}
		catch(JsonParseException e)
		{
			Log.e(TAG, "Failed to parse PayeesList from HttpResponse: "+e.toString());
			return null;
		}

		return result;
	}
}
