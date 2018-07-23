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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easternbeauty.n.Constants;
import com.easternbeauty.n.R;
import com.easternbeauty.utils.ImageUrlParser;
import com.easternbeauty.utils.JsonRecord;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridFragment extends AbsListViewBaseFragment {

	public static final int INDEX = 1;

	DisplayImageOptions options;

	// To find out image urls
	ImageUrlFinder mTask;

	private class ImageUrlFinder extends AsyncTask<String, Integer, String> {

		private void debug(String format, Object... args) {
			String str = String.format(format, args);
			Log.d("DEBUG", "" + System.currentTimeMillis() + ": " + str);
		}

		@Override
		protected String doInBackground(String... params) {
			//debug("doInBackground(Params... params) called");
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(params[0]);
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int length = -1;
					while ((length = is.read(buf)) != -1) {
						//debug("read %d bytes", length);
						baos.write(buf, 0, length);
					}
					return new String(baos.toByteArray(), "utf-8");
				}
			} catch (Exception e) {
				debug(e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					ImageUrlParser parser = new ImageUrlParser();
					JsonRecord record = parser.parse(result);
					final List<String> urlList = record.imageUrlList;
					((GridView) listView).setAdapter(new ImageAdapter(urlList));
					listView.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.INVISIBLE);
					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							startImagePagerActivity(urlList, position);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				String err = "Cannot get response from server.";
				Toast.makeText(getActivity().getApplicationContext(), err,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.beauty_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.beauty_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		
		Bundle bundle = getArguments();
		String jsonUrl = bundle.getString(Constants.Extra.BAIDU_JSON_URL);
		
		ImageUrlFinder mTask = new ImageUrlFinder();
		mTask.execute(jsonUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fr_image_grid, container,
				false);
		listView = (GridView) rootView.findViewById(R.id.grid);
		progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
		
		listView.setVisibility(View.INVISIBLE);
		
		return rootView;
	}

	public class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<String> imageUrlList;

		public ImageAdapter(List<String> urlList) {
			inflater = LayoutInflater.from(getActivity());
			this.imageUrlList = urlList;
		}

		@Override
		public int getCount() {
			return imageUrlList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = inflater
						.inflate(R.layout.item_grid_image, parent, false);
				holder = new ViewHolder();
				assert view != null;
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view
						.findViewById(R.id.progress);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			ImageLoader.getInstance().displayImage(imageUrlList.get(position),
					holder.imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							holder.progressBar.setProgress(0);
							holder.progressBar.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							holder.progressBar.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							holder.progressBar.setVisibility(View.GONE);
						}
					}, new ImageLoadingProgressListener() {
						@Override
						public void onProgressUpdate(String imageUri,
								View view, int current, int total) {
							holder.progressBar.setProgress(Math.round(100.0f
									* current / total));
						}
					});

			return view;
		}
	}

	static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}
}