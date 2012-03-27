package org.profmaad.LedgerAndroid;

public class LedgerTransaction
{
	private Date date;
	private Date effective_date;
	private boolean cleared;
	private boolean pending;
	private String code;
	private String payee;
	
	private List<LedgerPosting> postings;

	public LedgerTransaction() {};
}