package com.easternbeauty.n;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.easternbeauty.utils.JsonRecord;

public class BeautyCategoryManager {

	private List<BCategory> categories;

	// Image count in one page
	public static int imageCount = 60;

	private static BeautyCategoryManager mgr;

	public static BeautyCategoryManager getInstance(Context ctx) {
		if (mgr == null) {
			mgr = new BeautyCategoryManager(ctx);
		}
		return mgr;
	}

	private BeautyCategoryManager(Context ctx) {
		categories = new ArrayList<BCategory>();
		SharedPreferences sp = ctx.getSharedPreferences("beauty_config", 0);
		categories.add(new BCategory(R.id.btn_adorable,
				R.string.adorable_girls, "aborable", "可爱", sp));
		categories.add(new BCategory(R.id.btn_classic, R.string.classic_beauty,
				"classic", "古典", sp));
		categories.add(new BCategory(R.id.btn_fashion, R.string.fashion,
				"fashion", "时尚", sp));
		categories.add(new BCategory(R.id.btn_longhair,
				R.string.long_hair_beauty, "lhair", "长发", sp));
		categories.add(new BCategory(R.id.btn_pure, R.string.pure_beauty,
				"pure", "清纯", sp));
		categories.add(new BCategory(R.id.btn_school, R.string.school_flowers,
				"school", "校花", sp));
		categories.add(new BCategory(R.id.btn_longlegs, R.string.long_legs,
				"longlegs", "长腿", sp));
		categories.add(new BCategory(R.id.btn_model, R.string.model_beauty,
				"model", "模特", sp));
		categories.add(new BCategory(R.id.btn_sexy, R.string.sexy_girls,
				"sexy", "性感", sp));
		categories.add(new BCategory(R.id.btn_temperament,
				R.string.beautiful_temperament, "te", "气质", sp));
	}

	public BCategory findByBtnId(int id) {
		for (BCategory c : categories) {
			if (c.btnId == id) {
				return c;
			}
		}
		return null;
	}

	public BCategory findByName(String name) {
		for (BCategory c : categories) {
			if (c.name.equals(name)) {
				return c;
			}
		}
		return null;
	}

	public List<String> getLastImageUrlList(int btnId) {
		BCategory bc = findByBtnId(btnId);
		return bc.lastImageUrlList;
	}

	// Get the json URL which will return image urls for next page
	public String getNextJsonUrl(int btnId) {
		BCategory bc = findByBtnId(btnId);
		return String
				.format("http://image.baidu.com/channel/listjson?pn=%d&rn=%d&tag1=美女&tag2=%s&ftags=&sorttype=0&ie=utf8",
						bc.lastPage * imageCount, imageCount, bc.tag);
	}

	// When successfully loaded next page, call it to save information
	public void commitNextPage(Context ctx, int btnId, JsonRecord retRecord) {
		BCategory bc = findByBtnId(btnId);
		bc.lastPage += 1;
		// Minus 10 to insure the play fragment has enough images
		if (bc.lastPage >= (bc.totalImages + imageCount - 10) / imageCount) {
			bc.lastPage = 0;
		}
		bc.lastImageUrlList.clear();
		bc.lastImageUrlList.addAll(retRecord.imageUrlList);
		if (retRecord.totalNum > 0) {
			bc.totalImages = retRecord.totalNum;
		}
		
		SharedPreferences sp = ctx.getSharedPreferences("beauty_config", 0);
		bc.save(sp);
	}
}
