package org.profmaad.LedgerAndroid;

import java.io.IOException;
import java.io.BufferedReader;

import android.util.Log;

import org.apache.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class WebserviceResponse
{
	private static final String TAG = "LedgerAndroid";

	private boolean error;
	private String message;

	public WebserviceResponse() {};

	public boolean isError() { return error; }
	public String getMessage() { return message; }

	public static WebserviceResponse createFromHttpResponse(HttpResponse response) throws IOException
	{
		BufferedReader reader = HttpResponseBodyHelper.getBodyReader(response);
		if(reader == null) { return null; }
		
		Gson gson = new Gson();

		WebserviceResponse result = null;
		try
		{
			result = gson.fromJson(reader, WebserviceResponse.class);
		}
		catch(JsonParseException e)
		{
			Log.e(TAG, "Failed to parse WebserviceReponse from HttpResponse: "+e.toString());
			return null;
		}

		return result;
	}
}
