package com.easternbeauty.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class FlipAnimation extends Animation {

	public static final boolean DEBUG = false;
	public static final boolean ROTATE_DECREASE = true;
	public static final boolean ROTATE_INCREASE = false;
	/** Max depth on Z axis */
	public static final float DEPTH_Z = 800.0f;
	/** Animation duration */
	public static final long DURATION = 1200l;
	private final boolean type;
	private final float centerX;
	private final float centerY;
	private Camera camera;

	public FlipAnimation(float cX, float cY, boolean type) {
		centerX = cX;
		centerY = cY;
		this.type = type;
		setDuration(DURATION);
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		camera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime,
			Transformation transformation) {
		// interpolatedTime: progress, 0~1
		if (listener != null) {
			listener.interpolatedTime(interpolatedTime);
		}

		float from = 0.0f, to = 0.0f;
		if (type == ROTATE_DECREASE) {
			from = 0.0f;
			to = 180.0f;
		} else if (type == ROTATE_INCREASE) {
			from = 360.0f;
			to = 180.0f;
		}

		// Rotate degree
		float degree = from + (to - from) * interpolatedTime;
		boolean overHalf = (interpolatedTime > 0.5f);
		if (overHalf) {
			degree = degree - 180;
		}

		// Rotate depth
		float depth = (0.5f - Math.abs(interpolatedTime - 0.5f)) * DEPTH_Z;

		final Matrix matrix = transformation.getMatrix();
		camera.save();
		// Depth -> distance to the screen
		camera.translate(0.0f, 0.0f, depth);
		// Rotate around x-axis
		camera.rotateX(degree);
		// Rotate around y-axis
		//camera.rotateY(degree);
		camera.getMatrix(matrix);
		camera.restore();

		if (DEBUG) {
			if (overHalf) {
				matrix.preTranslate(-centerX * 2, -centerY);
				matrix.postTranslate(centerX * 2, centerY);
			}
		} else {
			// Make sure the picture is in the center during flip
			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}

	private InterpolatedTimeListener listener;

	public void setInterpolatedTimeListener(InterpolatedTimeListener listener) {
		this.listener = listener;
	}
}