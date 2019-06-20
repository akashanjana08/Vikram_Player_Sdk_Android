package com.multitv.multitvplayersdk.customeviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.multitv.multitvplayersdk.R;

public class LoadingView extends FrameLayout {

	Context mContext;
	Animation animation;
	ProgressBar progressBar;
	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_loading_two,
				this);
		 progressBar=(ProgressBar)this.findViewById(R.id.loading_spinner);
		progressBar.setIndeterminate(true);
		ImageView animationTarget = (ImageView) this
				.findViewById(R.id.testImage);
		animation = AnimationUtils.loadAnimation(mContext,
				R.anim.rotate_around_center_point);
		animationTarget.startAnimation(animation);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		// TODO Auto-generated method stub
		super.onVisibilityChanged(changedView, visibility);
	}

	public void show() {
		progressBar.setVisibility(View.VISIBLE);
		setVisibility(VISIBLE);
		if (animation != null) {
			animation.start();
		}
		// if (animation != null)
		// animation.cancel();

	}

	public void hide() {
		setVisibility(GONE);
		progressBar.setVisibility(View.GONE);
		if (animation != null) {
			animation.cancel();
		}
		// if (animation != null) {
		// animation.reset();
		// animation.startNow();
		// }
	}

}
