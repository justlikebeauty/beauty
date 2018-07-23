package com.easternbeauty.n;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easternbeauty.fragment.ImagePagerFragment;
import com.easternbeauty.n.RefreshableView.PullToRefreshListener;
import com.easternbeauty.utils.ActivityUtil;
import com.easternbeauty.utils.ImageUrlParser;
import com.easternbeauty.utils.JsonRecord;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImageListActivity extends Activity implements OnItemClickListener {

	private com.easternbeauty.n.RefreshableView refreshableView;
	private GridView gridView;

	private DisplayImageOptions options;

	// Image URL list
	private List<String> urlList;

	// GridView adapter
	private ImageAdapter imageAdapter;

	// by click which button to enter this page
	private int fromBtnId;

	private DefaultHttpClient httpClient;
	private CookieStore cookieStore;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_imagegrid);

		String title = ActivityUtil.getParam(getIntent(),
				Constants.Extra.TITLE_STR);
		setTitle(title);
		this.fromBtnId = Integer.valueOf(ActivityUtil.getParam(getIntent(),
				Constants.Extra.FROM_BUTTON_ID));

		this.options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.beauty_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.beauty_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		this.gridView = (GridView) findViewById(R.id.grid);

		this.refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			JsonRecord retRecord;

			@Override
			public void onRefresh() {
				BeautyCategoryManager mgr = BeautyCategoryManager
						.getInstance(ImageListActivity.this);
				String url = mgr.getNextJsonUrl(fromBtnId);
				// debug("url=" + url);
				retRecord = getJsonResponse(url);
				if (retRecord != null && retRecord.imageUrlList != null) {
					mgr.commitNextPage(ImageListActivity.this, fromBtnId, retRecord);
				}
				refreshableView.finishRefreshing();
			}

			@Override
			public void onFinish() {
				if (retRecord != null && retRecord.imageUrlList != null) {
					updateImageList(retRecord.imageUrlList);
				} else {
					Toast.makeText(ImageListActivity.this,
							"Cannot get image urls.", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}, 0);

		showTip();
	}

	@Override
	public void onResume() {
		super.onResume();

		this.urlList = initImageUrlList();
		this.imageAdapter = new ImageAdapter(urlList);
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(this);
	}

	private void showTip() {
		SharedPreferences sp = this.getSharedPreferences("ui_config", 0);
		int tipCount = sp.getInt("pullDownTips", 0);
		if (tipCount < 5) {
			Toast.makeText(this, "Please pull down to refresh.",
					Toast.LENGTH_LONG).show();
			tipCount++;
			Editor editor = sp.edit();
			editor.putInt("pullDownTips", tipCount);
			editor.commit();
		}
	}

	// Download image url list from a json page
	protected JsonRecord getJsonResponse(String jsonUrl) {

		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			cookieStore = new BasicCookieStore();
			httpClient.setCookieStore(cookieStore);
			httpClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 16000);
			httpClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 60000);
		}

		// // Create local HTTP context
		// HttpContext localContext = new BasicHttpContext();
		// // Bind custom cookie store to the local context
		// localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		try {
			HttpGet get = new HttpGet(jsonUrl);
			HttpResponse response = httpClient.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// List<Cookie> cookies =
				// httpClient.getCookieStore().getCookies();
				// for (int i = 0; i < cookies.size(); i++) {
				// debug("Local cookie: " + cookies.get(i));
				// }

				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				int length = -1;
				while ((length = is.read(buf)) != -1) {
					// debug("read %d bytes", length);
					baos.write(buf, 0, length);
				}
				String str = new String(baos.toByteArray(), "utf-8");
				ImageUrlParser parser = new ImageUrlParser();
				JsonRecord record = parser.parse(str);
				return record;
			}
		} catch (Exception e) {
			debug(e.getMessage());
		}
		return null;
	}

	private List<String> initImageUrlList() {
		return BeautyCategoryManager.getInstance(this).getLastImageUrlList(
				this.fromBtnId);
	}

	private void debug(String format, Object... args) {
		String str = String.format(format, args);
		Log.d("DEBUG", "" + System.currentTimeMillis() + ": " + str);
	}

	@SuppressWarnings("unused")
	private class ImageUrlFinder extends
			AsyncTask<String, Integer, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			//debug("doInBackground(Params... params) called");
			return getJsonResponse(params[0]).imageUrlList;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (result != null) {
				ImageListActivity.this.updateImageList(result);
			} else {
				String err = "Cannot get response from server.";
				Toast.makeText(ImageListActivity.this, err, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<String> imageUrlList;

		public ImageAdapter(List<String> urlList) {
			inflater = LayoutInflater.from(ImageListActivity.this);
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

		public void updateImages(ArrayList<String> newUrlList) {
			this.imageUrlList = newUrlList;
		}
	}

	private static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this, ImageViewActivity.class);
		intent.putExtra(Constants.Extra.FRAGMENT_INDEX,
				ImagePagerFragment.INDEX);
		intent.putExtra(Constants.Extra.IMAGE_POSITION, position);
		intent.putExtra(Constants.Extra.IMAGE_URL_LIST,
				urlList.toArray(new String[urlList.size()]));
		startActivity(intent);
	}

	public void updateImageList(ArrayList<String> newUrlList) {
		this.urlList = newUrlList;
		imageAdapter.updateImages(newUrlList);
		imageAdapter.notifyDataSetChanged();
	}
}
