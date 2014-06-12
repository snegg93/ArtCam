package org.artcam.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void onExitClick(View v) {
		finish();
	}
	
	public void onFaceCollectionClick(View v) {
		startActivity(new Intent(this, FaceCollection.class));
	}
}
