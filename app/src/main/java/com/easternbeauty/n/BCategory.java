package com.easternbeauty.n;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BCategory {

	public String name;

	public int btnId;

	// Description resource id
	public int strResId;

	// Range: 1-totalCount/60
	// lastPage = 0, means just displayed the internal 30 images
	public int lastPage;
	
	// Total image number for the category
	public int totalImages;

	// Tag used in json url
	public String tag;

	public List<String> lastImageUrlList;

	public BCategory(int btnId, int strResId, String name, String tag,
			SharedPreferences sp) {
		this.btnId = btnId;
		this.strResId = strResId;
		this.name = name;
		this.tag = tag;

		this.lastImageUrlList = new ArrayList<String>();
		this.lastPage = sp.getInt(name + "_lastPage", 0);
		this.totalImages = sp.getInt(name +"_images", 0);
		if (totalImages == 0) {
			initImageNumber();
		}
		
		String strUrls = sp.getString(name + "_urls", "");
		if (!strUrls.equals("")) {
			String[] urls = strUrls.split("\n");
			for (String url : urls) {
				lastImageUrlList.add(url);
			}
		} else {
			initUrlListFromConstants();
		}
	}

	private void initImageNumber() {
		switch (this.btnId) {
		case R.id.btn_adorable:
			this.totalImages = 1300;
			break;
		case R.id.btn_classic:
			this.totalImages = 120;
			break;
		case R.id.btn_fashion:
			this.totalImages = 1000;
			break;
		case R.id.btn_longhair:
			this.totalImages = 1280;
			break;
		case R.id.btn_pure:
			this.totalImages = 14000;
			break;
		case R.id.btn_school:
			this.totalImages = 1450;
			break;
		case R.id.btn_temperament:
			this.totalImages = 5900;
			break;
		case R.id.btn_longlegs:
			this.totalImages = 700;
			break;
		case R.id.btn_model:
			this.totalImages = 9800;
		case R.id.btn_sexy:
		default:
			this.totalImages = 4000;
			break;
		}
	}

	private void initUrlListFromConstants() {
		String[] urls;
		switch (this.btnId) {
		case R.id.btn_adorable:
			urls = Constants.ADORABLE_IMAGES;
			break;
		case R.id.btn_classic:
			urls = Constants.CLASSIC_IMAGES;
			break;
		case R.id.btn_fashion:
			urls = Constants.FASHION_IMAGES;
			break;
		case R.id.btn_longhair:
			urls = Constants.LONGHAIR_IMAGES;
			break;
		case R.id.btn_pure:
			urls = Constants.PURE_IMAGES;
			break;
		case R.id.btn_school:
			urls = Constants.SCHOOL_IMAGES;
			break;
		case R.id.btn_temperament:
			urls = Constants.TEMPER_IMAGES;
			break;
		case R.id.btn_longlegs:
			urls = Constants.LONGLEGS_IMAGES;
			break;
		case R.id.btn_model:
			urls = Constants.MODEL_IMAGES;
			break;
		case R.id.btn_sexy:
		default:
			urls = Constants.SEXY_IMAGES;
			break;
		}
		
		for (String url : urls) {
			lastImageUrlList.add(url);
		}
	}

	public void save(SharedPreferences sp) {
		Editor editor = sp.edit();
		if (totalImages > 0) {
			editor.putInt(name +"_images", this.totalImages);
			editor.putInt(name + "_lastPage", this.lastPage);
			editor.putString(name + "_urls", joinUrl());
		}
		editor.commit();
	}
	
	private String joinUrl() {
		if (lastImageUrlList.isEmpty()) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		for (String url : lastImageUrlList) {
			sb.append(url + "\n");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
}
