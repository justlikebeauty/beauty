/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.easternbeauty.n;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.easternbeauty.fragment.ImageGridFragment;
import com.easternbeauty.fragment.ImagePagerFragment;
import com.easternbeauty.fragment.ImagePlayFragment;

public class ImageViewActivity extends AppCompatActivity {
	//private String animationType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		int frIndex = getIntent()
				.getIntExtra(Constants.Extra.FRAGMENT_INDEX, 0);
		Fragment fr;
		String tag;
		switch (frIndex) {
		default:
		case ImageGridFragment.INDEX:
			tag = ImageGridFragment.class.getSimpleName();
			fr = getFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new ImageGridFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;

		case ImagePagerFragment.INDEX:
			tag = ImagePagerFragment.class.getSimpleName();
			fr = getFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new ImagePagerFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;

		case ImagePlayFragment.INDEX:
			hideActionBar();
			tag = ImagePlayFragment.class.getSimpleName();
			fr = getFragmentManager().findFragmentByTag(tag);
			if (fr == null) {
				fr = new ImagePlayFragment();
				fr.setArguments(getIntent().getExtras());
			}
			break;

		}

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fr, tag).commit();
	}

//	private void createActionbar() {
//		String[] titles = new String[2];
//		titles[0] = "Slide  ";
//		titles[1] = "Flip   ";
//		SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_dropdown_item, titles);
//
//		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
//
//			@Override
//			public boolean onNavigationItemSelected(int position, long itemId) {
//				switch (position) {
//				case 0:
//					animationType = "Slide";
//					break;
//				case 1:
//					animationType = "Flip";
//					break;
//				default:
//					break;
//				}
//				return true;
//			}
//		};
//
//		ActionBar actionBar = getActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
//				mOnNavigationListener);
//	}

	private void hideActionBar() {
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
	}

}