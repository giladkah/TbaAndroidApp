package com.example.inboxlistproject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

public class MessageActivity extends Activity {
	private static final int TYPE_INCOMING_MESSAGE = 1;
	private Cursor cursor; 
	ListView conversationListView;
	
	String messageNumber = null;
	ArrayList<Message> conversation = new ArrayList<Message>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_activity);
		
		getNumber(savedInstanceState);
		getConversation();
				
		conversationListView = (ListView) findViewById(R.id.msgListView);

		MessageListAdapter messageListAdapter = new MessageListAdapter(this,
				R.layout.chatbubble, conversation);
		conversationListView.setAdapter(messageListAdapter);

//		
//		TextView myAwesomeTextView = (TextView)findViewById(R.id.txt_msgTO);
//		TextView myAwesomeTextView2 = (TextView)findViewById(R.id.txt_messageContent);
//
//		//in your OnCreate() method
//		myAwesomeTextView.setText(messageNumber);
//		if (conversation != null) {
//			myAwesomeTextView2.setText("My Awesome Text2: " + conversation.size() );
//		}
	}


	private void getNumber(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if(extras == null) {
				messageNumber= "moshe";
			} else {
				messageNumber= extras.getString("number");
			}
		} else {
			messageNumber= (String) savedInstanceState.getSerializable("number");
		}
	}


	public void getConversation() {
		Uri uriSms = Uri.parse("content://sms");

		String[] reqCols = new String[] { "_id", "address", "body" };

		// Get Content Resolver object, which will deal with Content
		// Provider
		ContentResolver cr = getContentResolver();
//		 "\"" + messageNumber + "\""
		// Fetch Inbox SMS Message from Built-in Content Provider
		cursor = cr.query(uriSms, reqCols, "address=" + "\"" + messageNumber + "\"" , null, null);
				
		if (cursor != null) {
			cursor.moveToLast();
			if (cursor.getCount() > 0) {

				do {

					Message message = new Message();
					message.messageNumber = cursor.getString(cursor
							.getColumnIndex("address"));
					message.messageContent = cursor.getString(cursor
							.getColumnIndex("body"));
					conversation.add(message);
				} while (cursor.moveToPrevious());
			}
		}
		
	}

}
