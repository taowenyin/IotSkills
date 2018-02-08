package cn.edu.siso.cameratest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button cameraCaptureBtn = null;

    // 摄像头预览对象
    private CameraPreview cameraPreview = null;
    // 摄像头对象
    private Camera camera = null;
    // 拍照回调对象
    private Camera.PictureCallback pictureCallback = null;

    private int cameraId = 1;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraCaptureBtn = findViewById(R.id.camera_capture_btn);
        FrameLayout previewLayout = findViewById(R.id.camera_preview_layout);

        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputImageFile();
                if (pictureFile == null){
                    Log.d(TAG, "创建图片文件失败，检查存储权限");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        cameraCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用拍照函数
                camera.takePicture(null, null, pictureCallback);
            }
        });

        camera = getCameraInstance(cameraId);
        cameraPreview = new CameraPreview(getApplicationContext(), camera);
//        previewLayout.addView(cameraPreview);
    }

    // 检查是否有摄像头设备
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }

        return false;
    }

    /** 获得并打开一个摄像头对象 */
    public static Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId);
        }
        catch (Exception e){
            Log.i(TAG, "打开相机失败：" + e.getMessage());
        }
        return c;
    }

    /** 创建保存照片的文件对象 */
    private File getOutputImageFile(){
        // 创建SD卡中Picture目录下的子目录
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraTestApp");

        // 如果存放照片的文件夹不存在，则创建该文件夹
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "创建文件夹失败");
                return null;
            }
        }

        // 创建图片文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }
}
