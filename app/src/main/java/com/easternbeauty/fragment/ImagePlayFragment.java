package com.easternbeauty.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.easternbeauty.anim.FlipAnimation;
import com.easternbeauty.anim.InterpolatedTimeListener;
import com.easternbeauty.anim.SlideAnimation;
import com.easternbeauty.n.Constants;
import com.easternbeauty.n.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class ImagePlayFragment extends BaseFragment implements
		InterpolatedTimeListener {

	public static final int INDEX = 3;

	// Animation
	private Animation animation;
	// Range of animation progress is [0-100]
	private int animationProgress = 0;
	// animation is played from animatedFrom to animatedFrom+1
	private int animatedFrom = 0;

	private View mainView;
	private ImageView images[] = new ImageView[5];
	private View imageContainers[] = new View[5];

	DisplayImageOptions options;

	// Where the auto play started
	private int startPos;
	private int currentPos;

	// All the image urls
	private String[] urls;

	private MyHandler handler;

	private String animationType;

	private static class MyHandler extends Handler {
		private WeakReference<ImagePlayFragment> fragRef;

		MyHandler(ImagePlayFragment frag) {
			this.fragRef = new WeakReference<ImagePlayFragment>(frag);
		}

		@Override
		public void handleMessage(Message msg) {
			ImagePlayFragment frag = fragRef.get();
			if (frag == null) {
				return;
			}

			switch (msg.what) {
			// Play the animation
			case 0:
				if (frag.playAnimation()) {
					this.sendEmptyMessageDelayed(0, 3300);
				} else {
					this.sendEmptyMessageDelayed(2, 800);
				}
				break;
			// To load the future image
			case 1:
				frag.loadNextImages();
				break;
			// Auto play ended
			case 2:
				Activity activity = frag.getActivity();
				Toast.makeText(activity, "play ended.", Toast.LENGTH_SHORT)
						.show();
				activity.finish();
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private void debug(String format, Object... args) {
		String str = String.format(format, args);
		Log.d("DEBUG", "" + System.currentTimeMillis() + ": " + str);
	}

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

		lockScreen();
	}

	protected void lockScreen() {
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	protected void unlockScreen() {
		getActivity().getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private Animation createAnimation() {
		Animation anim = null;
		if ("Flip".equalsIgnoreCase(animationType)) {
			float cX = mainView.getWidth() / 2.0f;
			float cY = mainView.getHeight() / 2.0f;
			FlipAnimation flipAnim = new FlipAnimation(cX, cY,
					FlipAnimation.ROTATE_DECREASE);
			flipAnim.setInterpolatedTimeListener(this);
			flipAnim.setFillAfter(true);
			anim = flipAnim;
		} else {
			SlideAnimation slideAnim = new SlideAnimation(mainView.getWidth(),
					mainView.getHeight());
			slideAnim.setInterpolatedTimeListener(this);
			slideAnim.setFillAfter(true);
			anim = slideAnim;
		}
		return anim;
	}

	// Return true means should continue playing
	public boolean playAnimation() {
		animationProgress = 0;

		if (this.animation == null) {
			this.animation = createAnimation();
		}

		mainView.startAnimation(animation);
		int nextPos = (currentPos + 1) % urls.length;

		return nextPos != startPos;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		this.startPos = bundle.getInt(Constants.Extra.IMAGE_POSITION, 0);
		this.currentPos = startPos;
		this.urls = bundle.getStringArray(Constants.Extra.IMAGE_URL_LIST);
		this.animationType = bundle.getString(Constants.Extra.ANIMATION_TYPE);

		View rootView = inflater.inflate(R.layout.fr_image_play, container,
				false);
		this.mainView = rootView.findViewById(R.id.layout_playmain);
		this.images[0] = (ImageView) rootView.findViewById(R.id.image1);
		this.images[1] = (ImageView) rootView.findViewById(R.id.image2);
		this.images[2] = (ImageView) rootView.findViewById(R.id.image3);
		this.images[3] = (ImageView) rootView.findViewById(R.id.image4);
		this.images[4] = (ImageView) rootView.findViewById(R.id.image5);
		imageContainers[0] = rootView.findViewById(R.id.layout1);
		imageContainers[1] = rootView.findViewById(R.id.layout2);
		imageContainers[2] = rootView.findViewById(R.id.layout3);
		imageContainers[3] = rootView.findViewById(R.id.layout4);
		imageContainers[4] = rootView.findViewById(R.id.layout5);
		for (int i = 0; i < 5; i++) {
			ImageLoader.getInstance().displayImage(
					urls[(startPos + i) % urls.length], images[i], options);
		}

		this.handler = new MyHandler(this);
		handler.sendEmptyMessageDelayed(0, 1000);

		return rootView;
	}

	@Override
	public void interpolatedTime(float interpolatedTime) {
		if (animationProgress < 50 && interpolatedTime >= 0.5f) {
			// debug("animatedFrom=" + animatedFrom + ", currentPos=" +
			// currentPos);
			imageContainers[animatedFrom].setVisibility(View.GONE);
			images[animatedFrom].setVisibility(View.GONE);

			int next = (animatedFrom + 1) % 5;
			ImageView iv = images[next];
			View container = imageContainers[next];

			iv.setVisibility(View.VISIBLE);
			container.setVisibility(View.VISIBLE);
			animationProgress = 50;
		}

		if (animationProgress < 100 && interpolatedTime == 1) {
			// debug("interpolatedTime == 1");
			animatedFrom = (animatedFrom + 1) % 5;
			currentPos = (currentPos + 1) % urls.length;
			handler.sendEmptyMessageDelayed(1, 100);
			animationProgress = 100;
		}
	}

	private void loadNextImages() {
		// debug("loadNextImages, animatedFrom=" + animatedFrom +
		// ", currentPos="
		// + currentPos);
		ImageView iv = images[(animatedFrom + 3) % 5];
		ImageLoader.getInstance().displayImage(
				urls[(currentPos + 3) % urls.length], iv, options);

	}
}
