package com.easternbeauty.anim;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SlideAnimation extends Animation {

	private InterpolatedTimeListener listener;
	private int width;
	@SuppressWarnings("unused")
	private int height;
	
	public static final long DURATION = 800l;

	public SlideAnimation(int width, int height) {
		this.width = width;
		this.height = height;
		setDuration(DURATION);
	}

	public void setInterpolatedTimeListener(InterpolatedTimeListener listener) {
		this.listener = listener;
	}

	@Override
	protected void applyTransformation(float interpolatedTime,
			Transformation transformation) {
		if (listener != null) {
			listener.interpolatedTime(interpolatedTime);
		}

		final Matrix matrix = transformation.getMatrix();

		// Left slide the first image
		if (interpolatedTime < 0.5f) {
			float offset = interpolatedTime * 2 * width;
			matrix.setTranslate(-offset, 0);
		} else {
			float offset = (1.0f - interpolatedTime) * 2 * width;
			matrix.setTranslate(offset, 0);
		}
	}
}
