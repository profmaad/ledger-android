package org.profmaad.LedgerAndroid;

import android.app.Activity;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.Button;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import android.content.Intent;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.util.Log;

public class CreateTransaction extends Activity
{
	private EditText dateDisplay;
	private Button dateButton;

	private EditText amountEdit;
	private EditText payeeEdit;
	private EditText accountToEdit;
	private EditText accountFromEdit;

	private Date date;
	private float amount;
	private String payee;
	private String accountTo;
	private String accountFrom;

	private static final int DIALOG_DATE_PICKER = 0;
	private static final String TAG = "LedgerAndroid";
	private static final String EXTRA_TRANSACTION_JSON = "transaction_json";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_transaction);

		dateDisplay = (EditText)findViewById(R.id.date_display);
		dateButton = (Button)findViewById(R.id.date_button);

		amountEdit = (EditText)findViewById(R.id.amount);
		payeeEdit = (EditText)findViewById(R.id.payee);
		accountToEdit = (EditText)findViewById(R.id.account_to);
		accountFromEdit = (EditText)findViewById(R.id.account_from);

		setDate(Calendar.getInstance());
		
		dateButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showDialog(DIALOG_DATE_PICKER);
			}
		});
    }

	private DatePickerDialog.OnDateSetListener dateSetListener =
		new DatePickerDialog.OnDateSetListener()
		{
			public void onDateSet(DatePicker view, int year, int month, int day)
			{
				Calendar c = Calendar.getInstance();
				c.set(year, month, day);
				setDate(c);
			}
		};

	private void setDate(Calendar c)
	{
		date = c.getTime();
		dateDisplay.setText(SimpleDateFormat.getDateInstance().format(date));
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case DIALOG_DATE_PICKER:
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			return new DatePickerDialog(this, dateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}

		return null;
	}

	public void cancel(View v)
	{
		finish();
	}
	public void save(View v)
	{
		Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").setPrettyPrinting().create();

		LedgerTransaction transaction = new LedgerTransaction(date, formatAmount(amountEdit.getText().toString()), payeeEdit.getText().toString(), accountToEdit.getText().toString(), accountFromEdit.getText().toString());

		Intent commitIntent = new Intent(this, CommitTransactionService.class);
		commitIntent.putExtra(EXTRA_TRANSACTION_JSON, gson.toJson(transaction));

		startService(commitIntent);

		finish();
	}

	private String formatAmount(float amount)
	{
		return formatAmount(Float.toString(amount));
	}
	private String formatAmount(String amount)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String currencySymbol = prefs.getString("default_currency", "€");
		boolean prependSymbol = prefs.getBoolean("default_currency_prepend", false);
		

		if(prependSymbol)
		{
			return currencySymbol+" "+amount;
		}
		else
		{
			return amount+" "+currencySymbol;
		}
	}
}
