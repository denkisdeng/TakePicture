package com.zhi.takepicture;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener{
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private SurfaceView mSurfaceView;
    private RelativeLayout mRlTakePicture;
    private Button mBtnTakePicture;
    private Button mBtnAutoFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setFixedSize(480, 272);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceCallback());
    }

    public final class SurfaceCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                // surfaceView创建时打开摄像头
                camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                Log.e("MainActivity:", parameters.flatten());
                parameters.setPreviewSize(480, 320);
                parameters.setPictureSize(640, 480);
                parameters.setPreviewFrameRate(8);
                parameters.setJpegQuality(80);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceHolder);  // 把相机绑定到SurfaceView上
                camera.startPreview();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // SurfaceView摧毁时释放摄像头
            camera.release();
            camera = null;
        }
    }

    private void initEvents() {
        mBtnTakePicture.setOnClickListener(this);
        mBtnAutoFocus.setOnClickListener(this);

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mRlTakePicture.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
    }

    private void initViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mRlTakePicture = (RelativeLayout) findViewById(R.id.rl_take_picture);
        mBtnTakePicture = (Button) findViewById(R.id.btn_take_picture);
        mBtnAutoFocus = (Button) findViewById(R.id.btn_auto_focus);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_take_picture:
                takePicture();
                break;
            case R.id.btn_auto_focus:
                camera.autoFocus(null);
                break;
        }
    }

    /**
     * camera 拍照API中的  takePicture
     * 第一个参数，获取快门按下的回调
     * 第二个参数，获取原始图片的回调
     * 第三个参数，获取压缩后的图片的回调
     */
    private void takePicture(){
        camera.takePicture(null, null, new MyPictureCallback());
    }

    public final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String filename = System.currentTimeMillis()+".jpg";
            File file = new File(Environment.getExternalStorageDirectory(), filename);
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera.startPreview();
        }
    }
}