package com.easternbeauty.utils;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

public class ActivityUtil {

	public static void attachParam(Intent intent, String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        intent.putExtras(bundle);
	}
	
	public static void attachParam(Intent intent, String key, ArrayList<String> value) {
		Bundle bundle = new Bundle();
        bundle.putStringArrayList(key, value);
        intent.putExtras(bundle);
	}
	
	public static String getParam(Intent intent, String key) {
		Bundle bundle = intent.getExtras();
        return bundle.getString(key);
	}
	
	public static ArrayList<String> getStringArray(Intent intent, String key) {
		Bundle bundle = intent.getExtras();
        return bundle.getStringArrayList(key);
	}
}
