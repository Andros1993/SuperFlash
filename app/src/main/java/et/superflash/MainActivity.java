package et.superflash;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.umeng.analytics.MobclickAgent;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import et.superflash.module.KeyConstant;
import et.superflash.view.ColorPicker;
import et.superflash.view.ColorPickerDialog;

//import android.support.v7.app.AppCompatActivity;


public class MainActivity extends Activity implements OnTouchListener, OnClickListener, DiscreteSeekBar.OnProgressChangeListener, ColorPicker.OnColorSelectListener {

    private Button btnUp, btnDown;
    private LinearLayout viewGroup;
    private final int BTN_FLASHLIGHT_FLAG = 1;
    private final int BTN_SCREENLIGHT_FLAG = 2;
    private final String flashColor = "#ffff00";
    private final String screenColor = "#ffffff";
    private final String defaultColor = "#CCEED0";
    private MaterialAnimatedSwitch aSwitch = null;
    private DiscreteSeekBar seekBar = null;
    private Camera camera = null;
    private Camera.Parameters parameters = null;
    private View flashLightView,screenlightView;
    private int[] xyBuf = {0,0};
    private LayoutParams layoutParams;
    private float screenBuf = 0f;

    private ColorPickerDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        btnDown = (Button) findViewById(R.id.flashlight);
        btnUp = (Button) findViewById(R.id.screenlight);
        viewGroup = (LinearLayout) findViewById(R.id.activity_main);
        seekBar = (DiscreteSeekBar) findViewById(R.id.screenlightbar);
        aSwitch = (MaterialAnimatedSwitch) findViewById(R.id.mas);
        flashLightView = findViewById(R.id.flashlight_view);
        screenlightView = findViewById(R.id.screenlight_view);
    }

    private void initData() {

    }

    public void initListener() {
        btnUp.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);
        seekBar.setOnProgressChangeListener(this);
        screenlightView.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            switch (view.getId()) {
                case R.id.flashlight:

                    revealView(motionEvent.getRawX(), motionEvent.getRawY(), BTN_FLASHLIGHT_FLAG, flashColor);
                    initLight();
                    flashLightView.setVisibility(View.VISIBLE);
                    screenlightView.setVisibility(View.GONE);
                    break;
                case R.id.screenlight:
                    revealView(motionEvent.getRawX(), motionEvent.getRawY(), BTN_SCREENLIGHT_FLAG, screenColor);
                    flashLightView.setVisibility(View.GONE);
                    screenlightView.setVisibility(View.VISIBLE);
                    if(layoutParams == null){
                        layoutParams = getWindow().getAttributes();
                    }
//                    screenBuf = layoutParams.screenBrightness;
                    try {
                        screenBuf =Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    } catch (SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    Log.d("Et","setProgress:" + (int)(screenBuf * 100/ 255) );
                    seekBar.setProgress((int)(screenBuf * 100/ 255) );
                    checkFirstTimeOpen();
                    break;
                default:
                    break;
            }

            btnDown.setVisibility(View.GONE);
            btnUp.setVisibility(View.GONE);
        }


        return false;
    }

    /**
     * check whether the user is the first tiem open,need to tip user can chuang the screen color
     */
    private void checkFirstTimeOpen() {

        SharedPreferences setting = getSharedPreferences(KeyConstant.USER_DATA_SP, 0);
        Boolean user_first = setting.getBoolean(KeyConstant.USER_FIRST_TIME_OPEN,true);
        if(user_first){//first time open
            setting.edit().putBoolean(KeyConstant.USER_FIRST_TIME_OPEN, false).commit();
            Toast.makeText(MainActivity.this, "点击屏幕可以改变颜色哦~", Toast.LENGTH_LONG).show();
        }
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
        return anim;
    }

    private Animator backFromFlashAnimation(final View view){
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
                view.setVisibility(View.GONE);
                btnDown.setVisibility(View.VISIBLE);
                btnUp.setVisibility(View.VISIBLE);
//                switchFlash();
                closeLight();
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

    private void initLight() {
        aSwitch.setOnClickListener(this);
        PackageManager pm = getPackageManager();
        //check the device whether to support camera
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Toast.makeText(this,"sorry,this device do not support the app.",Toast.LENGTH_LONG).show();
            finish();
        } else {
            camera = Camera.open();
            parameters = camera.getParameters();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mas:
                switchFlash();
                break;
            case R.id.screenlight_view:
//                Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show();

                if(mDialog == null){
                    mDialog = new ColorPickerDialog(this);
                }
                mDialog.setPickerOnListener(this);
                mDialog.show();
                break;
            default:
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
        if(parameters == null || camera == null){
            return;
        }
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
        aSwitch.toggle();
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
                    backFromFlashAnimation(flashLightView);
                    return true;
                }else if(screenlightView.getVisibility() == View.VISIBLE){
                    backFromFlashAnimation(screenlightView);
                    layoutParams.screenBrightness = screenBuf;
                    Log.d("Et","set:" + screenBuf);
                    getWindow().setAttributes(layoutParams);
                    return true;
                }

                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        Log.d("Et","setting screenBrightness : " + (value * 255)/100);
        setLight((value * 255)/100);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }

    private void setLight(int brightness) {
        Log.d("Et","setLight : " + Float.valueOf(brightness) * (1f / 255f));
        layoutParams.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onColorSelect(int color) {
        screenlightView.setBackgroundColor(color);
    }
}
