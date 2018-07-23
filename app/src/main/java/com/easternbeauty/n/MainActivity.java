package com.easternbeauty.n;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.easternbeauty.utils.ActivityUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private View rootView;
	private int backgroundIndex;

	private int[] bgResourceIds = { R.drawable.bg0, R.drawable.bg1,
			R.drawable.bg2, };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		// Init the background
		this.rootView = this.findViewById(R.id.root_view);
		loadBackgroundIndex();
		rootView.setBackgroundResource(bgResourceIds[backgroundIndex
				% bgResourceIds.length]);

		Button btn = (Button) this.findViewById(R.id.btn_adorable);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_fashion);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_classic);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_longhair);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_pure);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_school);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_sexy);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_temperament);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_longlegs);
		btn.setOnClickListener(this);
		btn = (Button) this.findViewById(R.id.btn_model);
		btn.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		ImageLoader.getInstance().stop();
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_background) {
			new AlertDialog.Builder(this)
					.setTitle("Select a background")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setSingleChoiceItems(
							new String[] { "Background 1", "Background 2",
									"Background 3" }, backgroundIndex,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									backgroundSelected(which);
									dialog.dismiss();
								}
							}).setNegativeButton("Close", null).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void backgroundSelected(int index) {
		if (index < bgResourceIds.length) {
			this.backgroundIndex = index;
			rootView.setBackgroundResource(bgResourceIds[index]);
			saveBackgroundIndex();
		}
	}

	private void loadBackgroundIndex() {
		SharedPreferences sp = this.getSharedPreferences("ui_config", 0);
		this.backgroundIndex = sp.getInt("bgIndex", 0);
	}

	private void saveBackgroundIndex() {
		SharedPreferences sp = this.getSharedPreferences("ui_config", 0);
		Editor editor = sp.edit();
		editor.putInt("bgIndex", backgroundIndex);
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		BCategory bc = BeautyCategoryManager.getInstance(this).findByBtnId(id);
		if (bc != null) {
			Intent intent = new Intent(this, ImageListActivity.class);
			String title = getResources().getString(bc.strResId);
			ActivityUtil.attachParam(intent, Constants.Extra.TITLE_STR, title);
			ActivityUtil.attachParam(intent, Constants.Extra.FROM_BUTTON_ID, ""
					+ id);
			startActivity(intent);
		}
	}
}
