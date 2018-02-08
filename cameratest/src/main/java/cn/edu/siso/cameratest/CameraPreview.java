package cn.edu.siso.cameratest;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder = null;
    private Camera camera = null;

    public static final String TAG = "CameraPreview";

    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        this.holder = getHolder();
        this.holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder); // 为相机设置绘制的对象
            camera.startPreview(); // 启动相机预览
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "相机预览错误设置：" + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null){
            // 摄像头预览对象不存在
            return;
        }

        // 在摄像头绘制对象变化前停止摄像头预览
        camera.stopPreview();

        try {
            // 重新设置摄像头预览对象
            camera.setPreviewDisplay(holder);
            camera.startPreview(); // 开启预览
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "启动摄像头预览错误：" + e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
