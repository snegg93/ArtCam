package org.artcam.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.common.SignInButton;

public class MainActivity extends Activity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        SignInButton but = new SignInButton(this);
        but.setId(getResources().getInteger(R.integer.signInButtonId));
        but.setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.signInLayout)).addView(but, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Utils.getInstance().setSession(new Utils.Session(this));
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Utils.getInstance().getSession().activityResult(requestCode);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == getResources().getInteger(R.integer.signInButtonId))
        {
            Utils.getInstance().getSession().connect();
        }
    }
	
	public void onExitClick(View v) {
		finish();
	}
	
	public void onFaceCollectionClick(View v) {
		startActivity(new Intent(this, FaceCollection.class));
	}


}
