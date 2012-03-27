package org.profmaad.LedgerAndroid;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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

	public LedgerTransaction(Date date, String amount, String payee, String accountTo, String accountFrom)
	{
		this.date = date;
		this.payee = payee;

		this.postings = new ArrayList<LedgerPosting>(2);

		LedgerPosting toPosting = new LedgerPosting(accountTo, amount);
		LedgerPosting fromPosting = new LedgerPosting(accountFrom);

		this.postings.add(toPosting);
		this.postings.add(fromPosting);
	}
}