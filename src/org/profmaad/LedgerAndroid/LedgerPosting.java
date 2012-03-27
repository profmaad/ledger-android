package org.profmaad.LedgerAndroid;

import java.util.Date;

public class LedgerPosting
{
	private String account;
	private String amount;

	private String per_unit_cost;
	private String posting_cost;

	private Date actual_date;
	private Date effective_date;
	
	private boolean virtual;

	private String comment;

	public LedgerPosting() {};

	public LedgerPosting(String account)
	{
		this.account = account;
	}
	public LedgerPosting(String account, String amount)
	{
		this.account = account;
		this.amount = amount;
	}
}