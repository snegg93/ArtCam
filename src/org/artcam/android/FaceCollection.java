package org.artcam.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class FaceCollection extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_collection);		
	}
	
	public void onBackButtonClick(View v) {
		finish();
	}
	
	private File getOutputMediaFile(){

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ArtCam");
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("ArtCam", "failed to create directory");
	            return null;
	        }
	    }	    
	    
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"/*+ timeStamp*/ + ".jpg");

	    return mediaFile;
	}
	
	private Uri fileUri;
	public FaceImageView img;
	
	public void onSomeClick(View v) {		      
	    fileUri = Uri.fromFile(getOutputMediaFile());
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    startActivityForResult(intent, 1);
	}
	
	public void onOtherClick(View v) {
		addFace();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			addFace();
		}
	}
	private float mat[] = {
			0.8f, 0.8f, 0.8f, 0, 0,
			0.8f, 0.8f, 0.8f, 0, 0,
			0.8f, 0.8f, 0.8f, 0, 0,
			0, 0, 0, 1, 0
	};
	public void addFace() {
		img = new FaceImageView(this);
		img.setLayoutParams(new LayoutParams(100, 100));
		LinearLayout l = (LinearLayout)findViewById(R.id.placementLayout);
        img.setScaleType(ScaleType.FIT_START);
		l.addView(img);
        ColorMatrix cm = new ColorMatrix();
		cm.set(mat);
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);			
		img.setColorFilter(cf);
		//DetectFace r = new DetectFace();
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ArtCam");
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("ArtCam", "failed to create directory");
	            return;
	        }
	    }
	    File f = new File(mediaStorageDir.getPath() + File.separator + "IMG_.jpg");
		//r.fileUri = Uri.fromFile(f);
		//r.img = img;
		//ProgressBar b = (ProgressBar)findViewById(R.id.progressBar1);
        //b.setVisibility(View.VISIBLE);  
		//r.execute("");
        img.setImageURI(Uri.fromFile(f));
        img.filePath = Uri.fromFile(f).getPath();
	}
	
	
	public class FaceImageView extends ImageView implements OnTouchListener  {
		public FaceImageView(Context c) {
			super(c);
			this.setOnTouchListener(this);
		}
		
		@Override
		protected void onDraw(Canvas c) {
			super.onDraw(c);			
		}
		
		@Override
	    public boolean onTouch(View view, MotionEvent event) {
			if (view == this) {
				Intent i = new Intent(getContext(), FaceEditActivity.class);
				i.putExtra("BitmapFilePath", filePath);
				startActivity(i);
			}
	        return false;
	    }
		
		public String filePath;
	}
	
	public class DetectFace extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... p) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565; 
			background_image = BitmapFactory.decodeFile(fileUri.getPath(), opt);
			if (background_image == null)
				 return null;
			FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), 1);
			faces = new FaceDetector.Face[1];
	        face_count = face_detector.findFaces(background_image, faces);
	        return null;
		}
		
		@Override
        protected void onPostExecute(Void result) {
			ProgressBar b = (ProgressBar)findViewById(R.id.progressBar1);
		    b.setVisibility(View.GONE);
		    File f = new File(fileUri.getPath());
	        if (f.exists())
	        	f.delete();
			if (face_count < 1 || background_image == null)
	        	return;
	        PointF p = new PointF();
	        faces[0].getMidPoint(p);
	        Bitmap bmp = Bitmap.createBitmap(background_image, Math.round(p.x - faces[0].eyesDistance()), Math.round(p.y - faces[0].eyesDistance()/2), 
					Math.round(faces[0].eyesDistance()*2), Math.round(faces[0].eyesDistance()*2));
	        FileOutputStream fs;
	        try {
	        	f.createNewFile();
				fs = new FileOutputStream(f);
		        bmp.compress(Bitmap.CompressFormat.PNG, 0, fs);
		        fs.flush();
		        fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        img.setImageBitmap(bmp);
        }
		
		public Uri fileUri;
		public int face_count;
		public FaceDetector.Face[] faces;
		public Bitmap background_image;
		public FaceImageView img;
	}
}

