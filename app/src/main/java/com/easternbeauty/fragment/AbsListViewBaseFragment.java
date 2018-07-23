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
package com.easternbeauty.fragment;

import java.util.List;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.easternbeauty.n.Constants;
import com.easternbeauty.n.ImageViewActivity;
import com.easternbeauty.n.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public abstract class AbsListViewBaseFragment extends BaseFragment {

	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";

	protected AbsListView listView;
	protected ProgressBar progressBar;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = false;

	@Override
	public void onResume() {
		super.onResume();
		applyScrollListener();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem pauseOnScrollItem = menu.findItem(R.id.item_pause_on_scroll);
		pauseOnScrollItem.setVisible(true);
		pauseOnScrollItem.setChecked(pauseOnScroll);

		MenuItem pauseOnFlingItem = menu.findItem(R.id.item_pause_on_fling);
		pauseOnFlingItem.setVisible(true);
		pauseOnFlingItem.setChecked(pauseOnFling);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_pause_on_scroll:
			pauseOnScroll = !pauseOnScroll;
			item.setChecked(pauseOnScroll);
			applyScrollListener();
			return true;
		case R.id.item_pause_on_fling:
			pauseOnFling = !pauseOnFling;
			item.setChecked(pauseOnFling);
			applyScrollListener();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void startImagePagerActivity(List<String> urlList, int position) {
		Intent intent = new Intent(getActivity(), ImageViewActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX,
				ImagePagerFragment.INDEX);
		intent.putExtra(Constants.Extra.IMAGE_POSITION, position);
		intent.putExtra(Constants.Extra.IMAGE_URL_LIST,
				urlList.toArray(new String[urlList.size()]));
		startActivity(intent);
	}

	private void applyScrollListener() {
		listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
				.getInstance(), pauseOnScroll, pauseOnFling));
	}
}
