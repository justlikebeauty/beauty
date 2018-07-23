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

import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easternbeauty.n.Constants;
import com.easternbeauty.n.ImageViewActivity;
import com.easternbeauty.n.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImagePagerFragment extends BaseFragment implements
		OnPageChangeListener {

	public static final int INDEX = 2;

	DisplayImageOptions options;

	private int position;

	private String[] urls;

	private ViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.beauty_error)
				.resetViewBeforeLoading(true).cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		this.position = bundle.getInt(Constants.Extra.IMAGE_POSITION, 0);
		this.urls = bundle.getStringArray(Constants.Extra.IMAGE_URL_LIST);
		updateTitle();

		View rootView = inflater.inflate(R.layout.fr_image_pager, container,
				false);
		this.viewPager = (ViewPager) rootView.findViewById(R.id.pager);
		viewPager.setAdapter(new ImageAdapter(urls));
		viewPager.setCurrentItem(position);
		viewPager.setOnPageChangeListener(this);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Add menu items
		MenuItem slideItem = menu.add(0, 0, 0, "Slide Play");
		MenuItem flipItem = menu.add(0, 1, 0, "Flip Play");
		// Bind to ActionBar 
		slideItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		flipItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			startImagePlayActivity("Slide");
			return true;
		case 1:
			startImagePlayActivity("Flip");
			return true;
		default:
			return false;
		}
	}

	private void startImagePlayActivity(String animationType) {
		Activity activity = getActivity();
		Intent intent = new Intent(activity, ImageViewActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImagePlayFragment.INDEX);
		intent.putExtra(Constants.Extra.IMAGE_POSITION,
				viewPager.getCurrentItem());
		intent.putExtra(Constants.Extra.IMAGE_URL_LIST, urls);
		intent.putExtra(Constants.Extra.ANIMATION_TYPE, animationType);
		startActivity(intent);
	}

	private class ImageAdapter extends PagerAdapter {

		private LayoutInflater inflater;
		private String[] urls;

		public ImageAdapter(String[] urls) {
			inflater = LayoutInflater.from(getActivity());
			this.urls = urls;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return urls.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);
			assert imageLayout != null;
			PhotoView imageView = (PhotoView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			ImageLoader.getInstance().displayImage(urls[position], imageView,
					options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}

							Activity activity = getActivity();
							if (activity != null) {
								Toast.makeText(activity, message,
										Toast.LENGTH_SHORT).show();
							}

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private void debug(String format, Object... args) {
		String str = String.format(format, args);
		Log.d("DEBUG", "" + System.currentTimeMillis() + ": " + str);
	}
	
	private void updateTitle() {
		getActivity().setTitle("" + (position + 1) + "/" + urls.length);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// debug("onPageScrollStateChanged(%d)", arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// debug("onPageScrolled(%d, %f, %d)", arg0, arg1, arg2);
	}

	@Override
	public void onPageSelected(int pageIndex) {
		this.position = pageIndex; 
		updateTitle();
	}
}