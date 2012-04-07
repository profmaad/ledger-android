package org.profmaad.LedgerAndroid;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.http.HttpResponse;

public class HttpResponseBodyHelper
{
	private static final String DEFAULT_ENCODING = "UTF-8";

	public static BufferedReader getBodyReader(HttpResponse response) throws IOException
	{
		if(response.getEntity() == null) { return null; }

		String encoding = DEFAULT_ENCODING;
		if(response.getEntity().getContentEncoding() != null) { encoding = response.getEntity().getContentEncoding().getValue(); }

		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));

		return reader;
	}
}