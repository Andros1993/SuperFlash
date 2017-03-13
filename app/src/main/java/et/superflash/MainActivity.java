package et.superflash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    private MaterialAnimatedSwitch aSwitch = null;
    private Camera camera = null;
    private Camera.Parameters parameters = null;
    private String[] permissions = {Manifest.permission.CAMERA};
    private Handler handler = null;
    private Timer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    public void initView() {
        aSwitch = (MaterialAnimatedSwitch) findViewById(R.id.mas);
    }

    public void initData() {
//        checkPermission();
        initLight();
    }

    /**
     * check permission
     */
//    private void checkPermission() {
//        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA},
//                    200);
//        }else {
//            initLight();
//        }
//    }

    /**
     * init light
     */
    private void initLight() {
        aSwitch.setOnClickListener(this);
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            camera = Camera.open();
            parameters = camera.getParameters();
            handler = new Handler();
            timer = new Timer();
        } else {
            Toast.makeText(this,"sorry,this device do not support the app.",Toast.LENGTH_LONG).show();
            finish();
        }

//        flicker();
    }

    private void flicker() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                switchFlash();
            }
        },1000,100);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 200){
//            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                initLight();
//            }else{
//                Toast.makeText(MainActivity.this, "请允许权限后使用~", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }

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
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
