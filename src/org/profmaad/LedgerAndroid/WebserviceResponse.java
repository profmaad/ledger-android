package org.profmaad.LedgerAndroid;

public class WebserviceResponse
{
	private boolean error;
	private String message;

	public WebserviceResponse() {};

	public boolean isError() { return error; }
	public String getMessage() { return message; }
}