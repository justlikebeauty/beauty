package com.easternbeauty.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class ImageUrlParser {

	@SuppressWarnings("unused")
	private static void debug(String format, Object... args) {
		String str = String.format(format, args);
		Log.d("DEBUG", "" + System.currentTimeMillis() + ": " + str);
	}


	public JsonRecord parse(String content) throws JSONException {
		//debug("parse started");
		JsonRecord record = new JsonRecord();
		JSONTokener jsonParser = new JSONTokener(content);
		JSONObject obj = (JSONObject) jsonParser.nextValue();
		try {
			record.totalNum = obj.getInt("totalNum");
			record.startIndex = obj.getInt("start_index");
			record.returnNum = obj.getInt("return_number");
		} catch (JSONException e) {
		}
		JSONArray datas = obj.getJSONArray("data");

		int size = datas.length();
		ArrayList<String> urls = new ArrayList<String>(64);
		for (int i = 0; i < size; i++) {
			JSONObject element = datas.getJSONObject(i);

			// Get url, first try download_url, then try image_url
			String url = null;
			try {
				url = element.getString("download_url");
			} catch (JSONException e) {
			}
			if (url == null) {
				try {
					url = element.getString("image_url");
				} catch (JSONException e) {
				}
			}

			if (url != null) {
				urls.add(url);
			}
		}
		//debug("parse ended");
		record.imageUrlList = urls;
		return record;
	}
}
