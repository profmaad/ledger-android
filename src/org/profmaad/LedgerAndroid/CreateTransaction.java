package org.profmaad.LedgerAndroid;

import android.app.Activity;
import android.os.Bundle;

import android.app.NotificationManager;
import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
	private static final String EXTRA_PACKAGE = "org.profmaad.LedgerAndroid.";
	private static final String EXTRA_TRANSACTION_JSON = "transaction_json";
	private static final String EXTRA_ACCOUNTS_LIST = "accounts";
	private static final String EXTRA_PAYEES_LIST = "payees";
	private static final int NOTIFICATION_ERROR = 1;
	private static final String INTENT_AUTOCOMPLETE_DATA = "autocomplete-data-received";

	private BroadcastReceiver autocompleteReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			ArrayList<String> accounts = intent.getStringArrayListExtra(EXTRA_PACKAGE+EXTRA_ACCOUNTS_LIST);
			ArrayList<String> payees = intent.getStringArrayListExtra(EXTRA_PACKAGE+EXTRA_PAYEES_LIST);

			Log.d(TAG, "received accounts:");
			Log.d(TAG, accounts.toString());
			Log.d(TAG, "received payees:");
			Log.d(TAG, payees.toString());
		}
	};

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

		Intent startIntent = getIntent();
		if(startIntent.hasExtra(EXTRA_TRANSACTION_JSON))
		{
			NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(NOTIFICATION_ERROR);
		
			String transactionJson = startIntent.getStringExtra(EXTRA_TRANSACTION_JSON);

			Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();

			try
			{
				LedgerTransaction transaction = gson.fromJson(transactionJson, LedgerTransaction.class);

				if(transaction.getPostings().size() >= 2)
				{
					setDate(transaction.getDate());
					payeeEdit.setText(transaction.getPayee());

					amountEdit.setText(parseAmount(transaction.getPostings().get(0).getAmount()));
					accountToEdit.setText(transaction.getPostings().get(0).getAccount());
					accountFromEdit.setText(transaction.getPostings().get(1).getAccount());
				}
			}
			catch(JsonSyntaxException e)
			{
				Log.w(TAG, "Failed to parse transaction json from startt intent: "+e.toString());
			}
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(autocompleteReceiver, new IntentFilter(INTENT_AUTOCOMPLETE_DATA));
		startService(new Intent(this, AutoCompleteDataService.class));
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
	private void setDate(Date d)
	{
		date = d;
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
	private String parseAmount(String amount)
	{
		String[] parts = amount.split(" ");

		for(String part : parts)
		{
			try
			{
				float result = Float.valueOf(part);
				return part;
			}
			catch(NumberFormatException e)
			{
				continue;
			}
		}

		return "";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.preferences:
			Intent startPreferencesIntent = new Intent(this, LedgerAndroidPreferences.class);
			startActivity(startPreferencesIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
