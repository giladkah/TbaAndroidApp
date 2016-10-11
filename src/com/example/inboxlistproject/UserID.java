package com.example.inboxlistproject;

import java.util.UUID;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserID {
	private final SharedPreferences sharedPrefs;
	private final String MyPreferences = "MyPreferences";
	private final String USER_UUID = "USER_UUID";

	public UserID(Context context) {
		sharedPrefs = context.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
	}

	public String getUUID(){
		String uuid = sharedPrefs.getString(USER_UUID, "");
		if (uuid == "") {
			uuid = createUserID();
			Editor editor = sharedPrefs.edit();
			editor.putString(USER_UUID, uuid);
			editor.commit();
		}
		return uuid;
	}

	private String createUserID() {
		return UUID.randomUUID().toString();
	}

}
