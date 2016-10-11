package com.example.inboxlistproject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

// Main class + Http Get class 
public class MainActivity extends Activity {
	private static final int TYPE_INCOMING_MESSAGE = 1;
	private ListView messageList;
	private MessageListAdapter messageListAdapter;
	private ArrayList<Message> recordsStored;
	private ArrayList<Message> listInboxMessages;
	private ArrayList<String> contactsKnownNumbers = new ArrayList<String>();	
	private ProgressDialog progressDialogInbox;
	private CustomHandler customHandler;

	// try parsing json
	//curl -X POST -d "hello=hello" https://mysterious-island-92840.herokuapp.com/curl_exampleþ

	private static String url = "https://mysterious-island-92840.herokuapp.com/curl_exampleþ";
	ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();
	private static final String TAG = HttpHandler.class.getSimpleName();

	public final static String NUMBER = "number"; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new GetContacts().execute();
		new HttpPostResonse().execute();
		setContentView(R.layout.activity_main);

		initViews();
	}

	@Override
	public void onResume() {

		super.onResume();
		populateMessageList();
	}

	private void initViews() {
		System.out.println("gilad gilad");

		customHandler = new CustomHandler(this);
		progressDialogInbox = new ProgressDialog(this);

		recordsStored = new ArrayList<Message>();

		messageList = (ListView) findViewById(R.id.messageList);
		populateMessageList();
	}

	public void populateMessageList() {
		fetchInboxMessages();

		messageListAdapter = new MessageListAdapter(this,
				R.layout.message_list_item, recordsStored);
		messageList.setAdapter(messageListAdapter);

		messageList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(MainActivity.this, MessageActivity.class);


				Message data=(Message)listInboxMessages.get(arg2);

				i.putExtra(NUMBER, data.messageNumber);
				startActivity(i);

				// TODO Auto-generated method stub
				//String item = "hello";

				//Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
			}
		});

	}

	private void showProgressDialog(String message) {
		progressDialogInbox.setMessage(message);
		progressDialogInbox.setIndeterminate(true);
		progressDialogInbox.setCancelable(true);
		progressDialogInbox.show();
	}

	private void fetchInboxMessages() {
		if (listInboxMessages == null) {
			showProgressDialog("Fetching Inbox Messages...");
			startThread();
		} else {
			// messageType = TYPE_INCOMING_MESSAGE;
			recordsStored = listInboxMessages;
			messageListAdapter.setArrayList(recordsStored);
		}
	}

	public class FetchMessageThread extends Thread {

		public int tag = -1;

		public FetchMessageThread(int tag) {
			this.tag = tag;
		}

		@Override
		public void run() {

			recordsStored = fetchInboxSms(TYPE_INCOMING_MESSAGE);
			listInboxMessages = recordsStored;
			customHandler.sendEmptyMessage(0);

		}

	}

	public ArrayList<Message> fetchInboxSms(int type) {
		populateContactsKnownNumbers();

		ArrayList<Message> smsInbox = new ArrayList<Message>();

		Uri uriSms = Uri.parse("content://sms");

		Cursor cursor = this.getContentResolver()
				.query(uriSms,
						new String[] { "address", "count(address) AS address_count"}, 
						"type=" + type + ") GROUP BY (address", null,
						"address_count" + " COLLATE LOCALIZED ASC");

		if (cursor != null) {
			cursor.moveToLast();
			if (cursor.getCount() > 0) {

				do {

					Message message = new Message();
					message.messageNumber = cursor.getString(cursor
							.getColumnIndex("address"));
					message.messageContent = cursor.getString(cursor
							.getColumnIndex("address_count"));

					if (contactsKnownNumbers.contains(message.messageNumber)) {
						continue;
					}

					smsInbox.add(message);
				} while (cursor.moveToPrevious());
			}
		}

		return smsInbox;
	}

	private void populateContactsKnownNumbers() {
		Cursor contactCursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null, null, null, null);

		for(contactCursor.moveToFirst(); !contactCursor.isAfterLast(); contactCursor.moveToNext()) {
			String number = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			contactsKnownNumbers.add(number);
			contactsKnownNumbers.add("+972" + number.substring(1));
		}

	}

	private FetchMessageThread fetchMessageThread;

	private int currentCount = 0;

	public synchronized void startThread() {

		if (fetchMessageThread == null) {
			fetchMessageThread = new FetchMessageThread(currentCount);
			fetchMessageThread.start();
		}
	}

	public synchronized void stopThread() {
		if (fetchMessageThread != null) {
			Log.i("Cancel thread", "stop thread");
			FetchMessageThread moribund = fetchMessageThread;
			currentCount = fetchMessageThread.tag == 0 ? 1 : 0;
			fetchMessageThread = null;
			moribund.interrupt();
		}
	}

	static class CustomHandler extends Handler {
		private final WeakReference<MainActivity> activityHolder;

		CustomHandler(MainActivity inboxListActivity) {
			activityHolder = new WeakReference<MainActivity>(inboxListActivity);
		}

		@Override
		public void handleMessage(android.os.Message msg) {

			MainActivity inboxListActivity = activityHolder.get();
			if (inboxListActivity.fetchMessageThread != null
					&& inboxListActivity.currentCount == inboxListActivity.fetchMessageThread.tag) {
				Log.i("received result", "received result");
				inboxListActivity.fetchMessageThread = null;

				inboxListActivity.messageListAdapter
				.setArrayList(inboxListActivity.recordsStored);
				inboxListActivity.progressDialogInbox.dismiss();
			}
		}
	}

	private OnCancelListener dialogCancelListener = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			stopThread();
		}

	};

	// The class to handle The HTTP Get/Post Request
	private class HttpPostResonse extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			progressDialogInbox = new ProgressDialog(MainActivity.this);
			progressDialogInbox.setMessage("Hold tight! Initializing App");
			progressDialogInbox.setCancelable(false);
			//progressDialogInbox.show();

		}

		private String getUserUUID() {
			UserID userid = new UserID(getApplicationContext());
			return userid.getUUID();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			HttpHandler sh = new HttpHandler();
			String usersUrl = "https://mysterious-island-92840.herokuapp.com/users";

			HashMap<String, String> params=new HashMap<String, String>();
			String uuid = getUserUUID();
			params.put("uuid", uuid);
			String jsonStr = sh.makePostCall(usersUrl, params);

			Log.e(TAG, "Gilad Gilad 1111: " + uuid);
			Log.e(TAG, "Gilad Gilad Response from url2: " + jsonStr);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (progressDialogInbox.isShowing())
				progressDialogInbox.dismiss();
		}
	}

	// The class to handle The HTTP Get/Post Request for contacts
	private class GetContacts extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			progressDialogInbox = new ProgressDialog(MainActivity.this);
			progressDialogInbox.setMessage("Please wait...");
			progressDialogInbox.setCancelable(false);
			//progressDialogInbox.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// Making a request to url and getting response        	
			HttpHandler sh = new HttpHandler();

			HashMap<String, String> params=new HashMap<String, String>();
			params.put("hello","hello");
			params.put("hello2","hello2");    		
			String jsonStr = sh.makePostCall(url, params);

			Log.e(TAG, "Response from url: " + jsonStr);

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);

					// Getting JSON Array node
					JSONArray contacts = jsonObj.getJSONArray("contacts");

					// looping through All Contacts
					for (int i = 0; i < contacts.length(); i++) {
						JSONObject c = contacts.getJSONObject(i);

						String id = c.getString("id");
						String name = c.getString("name");
						String email = c.getString("email");
						String address = c.getString("address");
						String gender = c.getString("gender");

						// Phone node is JSON Object
						JSONObject phone = c.getJSONObject("phone");
						String mobile = phone.getString("mobile");
						String home = phone.getString("home");
						String office = phone.getString("office");

						// tmp hash map for single contact
						HashMap<String, String> contact = new HashMap<>();

						// adding each child node to HashMap key => value
						contact.put("id", id);
						contact.put("name", name);
						contact.put("email", email);
						contact.put("mobile", mobile);

						System.out.println("gilad333: " + contact);

						// adding contact to contact list
						contactList.add(contact);
					}
				} catch (final JSONException e) {
					Log.e(TAG, "Json parsing error: " + e.getMessage());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									"Json parsing error: " + e.getMessage(),
									Toast.LENGTH_LONG)
									.show();
						}
					});

				}
			} else {
				Log.e(TAG, "Couldn't get json from server.");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(),
								"Couldn't get json from server. Check LogCat for possible errors!",
								Toast.LENGTH_LONG)
								.show();
					}
				});

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (progressDialogInbox.isShowing())
				progressDialogInbox.dismiss();
			/**
			 * Updating parsed JSON data into ListView
			 * */
			//            ListAdapter adapter = new SimpleAdapter(
			//                    MainActivity.this, contactList,
			//                    R.layout.list_item, new String[]{"name", "email",
			//                    "mobile"}, new int[]{R.id.name,
			//                    R.id.email, R.id.mobile});
			// 
			//            lv.setAdapter(adapter);
		}

	}
}
