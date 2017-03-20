package et.superflash;

import android.Manifest;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.umeng.analytics.MobclickAgent;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnTouchListener, OnClickListener {

    private Button btnUp, btnDown;
    private LinearLayout viewGroup;
    private final int BTN_FLASHLIGHT_FLAG = 1;
    private final int BTN_SCREENLIGHT_FLAG = 2;
    private final String flashColor = "#ffff00";
    private final String screenColor = "#ffffff";
    private final String defaultColor = "#CCEED0";
    private MaterialAnimatedSwitch aSwitch = null;
    private Camera camera = null;
    private Camera.Parameters parameters = null;
    private View flashLightView;
    private int[] xyBuf = {0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        btnDown = (Button) findViewById(R.id.flashlight);
        btnUp = (Button) findViewById(R.id.screenlight);
        viewGroup = (LinearLayout) findViewById(R.id.activity_main);
    }

    private void initData() {
        aSwitch = (MaterialAnimatedSwitch) findViewById(R.id.mas);
        flashLightView = findViewById(R.id.flashlight_view);
    }

    public void initListener() {
        btnUp.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()) {
            case R.id.flashlight:

                revealView(motionEvent.getRawX(), motionEvent.getRawY(), BTN_FLASHLIGHT_FLAG, flashColor);
                initLight();
                break;
            case R.id.screenlight:
                revealView(motionEvent.getRawX(), motionEvent.getRawY(), BTN_SCREENLIGHT_FLAG, screenColor);

                break;
            default:
                break;
        }
        flashLightView.setVisibility(View.VISIBLE);
        btnDown.setVisibility(View.GONE);
        btnUp.setVisibility(View.GONE);


        return false;
    }

    private void revealView(float x, float y, int flag, String color) {
        animateRevealColorFromCoordinates(viewGroup, color, (int) x, (int) y, flag);
    }

    private Animator animateRevealColorFromCoordinates(ViewGroup viewRoot, String color, int x, int y, final int flag) {
        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        xyBuf[0] = x;
        xyBuf[1] = y;
        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(Color.parseColor(color));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
//        anim.addListener(new AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                switch (flag) {
//                    case BTN_FLASHLIGHT_FLAG:
//                        break;
//                    case BTN_SCREENLIGHT_FLAG:
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
        return anim;
    }

    private Animator backFromFlashAnimation(){
        float finalRadius = (float) Math.hypot(viewGroup.getWidth(), viewGroup.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewGroup, xyBuf[0], xyBuf[1], finalRadius, 0);
        viewGroup.setBackgroundColor(Color.parseColor(defaultColor));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                flashLightView.setVisibility(View.GONE);
                btnDown.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
                switchFlash();
                releaseCamera();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        return anim;
    }


//
//    /**
//     * check permission
//     */
////    private void checkPermission() {
////        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
////            ActivityCompat.requestPermissions(this,
////                    new String[]{Manifest.permission.CAMERA},
////                    200);
////        }else {
////            initLight();
////        }
////    }
//
//    /**
//     * init light
//     */
    private void initLight() {
        aSwitch.setOnClickListener(this);
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Toast.makeText(this,"sorry,this device do not support the app.",Toast.LENGTH_LONG).show();
            finish();
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
        }

    }

//
////    @Override
////    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
////        if(requestCode == 200){
////            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
////                initLight();
////            }else{
////                Toast.makeText(MainActivity.this, "请允许权限后使用~", Toast.LENGTH_LONG).show();
////                finish();
////            }
////        }
////    }
//
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mas:
                switchFlash();
                break;
        }
    }

    /**
     * switch flash
     */
    private void switchFlash() {
        if (parameters.getFlashMode() == Camera.Parameters.FLASH_MODE_TORCH) {
            closeLight();
        } else {
            openLight();
        }
    }

    /**
     * open light
     */
    private void openLight() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    /**
     * close light
     */
    private void closeLight() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode){
            case KeyEvent.KEYCODE_BACK :
                if(flashLightView.getVisibility() == View.VISIBLE){
//                    flashLightView.setVisibility(View.GONE);
//                    btnDown.setVisibility(View.VISIBLE);
//                    btnUp.setVisibility(View.VISIBLE);
//                    switchFlash();
//                    releaseCamera();
                    backFromFlashAnimation();
                    return true;
                }

                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
