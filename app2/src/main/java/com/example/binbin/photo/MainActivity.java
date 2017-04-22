package com.example.binbin.photo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.common.Config;
import com.youtu.Youtu;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private SurfaceView surfaceview;
    private Camera camera;
    private Button take;

    private static final float idCardScale = 1.585f;  //85.6/54，身份证长宽比

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE); // 没有标题  必须在设置布局之前找到调用
        setContentView(R.layout.activity_main);

            /*
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // 设置全屏显示
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            */

        take = (Button) findViewById(R.id.take);
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        SurfaceHolder holder = surfaceview.getHolder();
//        holder.setFixedSize(surfaceview.getMeasuredWidth(), surfaceview.getMeasuredHeight());// 设置分辨率
        holder.setKeepScreenOn(true);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // SurfaceView只有当activity显示到了前台，该控件才会被创建     因此需要监听surfaceview的创建
        holder.addCallback(new MySurfaceCallback());

        //拍照按钮
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                takepicture();

            }
        });


    }

    private void checkPermission() {
        String[] permStr = new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        // 权限判断
        if (PermissionUtil.checkPermissions(this, permStr)) {
            openCamera();
        } else {
            PermissionUtil.requestPermission(this, permStr, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取权限成功
                    openCamera();
                } else {
                    // 获取权限失败
                    Toast.makeText(MainActivity.this, "权限获取失败", Toast.LENGTH_SHORT).show();
                    setPermissionApplyDialog();
                }
                break;

        }

    }

    private AlertDialog dialogRequestPerm;
    /**
     * 权限申请
     */
    private void setPermissionApplyDialog() {
        try {
            dialogRequestPerm = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("自动申请权限失败。\\n请点击“设置”-“权限管理”-“相机”手动打开所需权限。").setCancelable(false)
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("设置", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PermissionUtil.startAppSettings(MainActivity.this);
                        }
                    }).create();
            dialogRequestPerm.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //点击事件
    @Override
    public boolean onTouchEvent(MotionEvent event){

        //对焦
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera){
                camera.cancelAutoFocus();
            }
        });

        return super.onTouchEvent(event);
    }

    /**
     * 监听surfaceview的创建
     *
     * @author Administrator
     *         Surfaceview只有当activity显示到前台，该空间才会被创建
     */
    private final class MySurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height){
            // TODO Auto-generated method stub

        }


        @Override
        public void surfaceCreated(SurfaceHolder holder){
            checkPermission();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder){
            if(camera != null){
                camera.release();
                camera = null;
            }
        }

    }

    private void openCamera(){
        try{
            // 当surfaceview创建就去打开相机
            camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            // Log.i("i", params.flatten());
            params.setJpegQuality(100);  // 设置照片的质量
//                params.setPictureSize(1024, 768);
//                params.setPreviewFrameRate(5);  // 预览帧率
//                params.setPreviewFpsRange(5, 8);
            List<Camera.Size> pSize = params.getSupportedPictureSizes();
            for(Camera.Size s : pSize){
                if(s.height < 2000){
                    params.setPictureSize(s.width, s.height);
                    break;
                }
            }
            List<Camera.Size> preSize = params.getSupportedPreviewSizes();
            params.setPreviewSize(preSize.get(0).width, preSize.get(0).height);
            camera.setParameters(params); // 将参数设置给相机
            // 设置预览显示
            camera.setPreviewDisplay(surfaceview.getHolder());
            //右旋90度，将预览调正
            camera.setDisplayOrientation(90);
            // 开启预览
            camera.startPreview();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @param context 上下文
     * @return 屏幕除去通知栏的高度
     */
    public int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    //拍照的函数
    public void takepicture(){
        /*
         * shutter:快门被按下
         * raw:相机所捕获的原始数据
         * jpeg:相机处理的数据
         */
        camera.takePicture(null, null, new MyPictureCallback());
    }

    //byte转Bitmap
    public Bitmap Bytes2Bimap(byte[] b){
        if(b.length != 0){
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }else{
            return null;
        }
    }

    //bitmap转byte
    public byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 将彩色图转换为黑白图
     */


    //照片回调函数，其实是处理照片的
    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            try{
                Bitmap bitmap = Bytes2Bimap(data);
                Matrix m = new Matrix();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                m.setRotate(90);
                //将照片右旋90度
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m,
                        true);

                Log.d("TAG", "width " + width);
                Log.d("TAG", "height " + height);

                if (bitmap == null) {
                    Log.d("zka", "bitmap is nlll");
                    return;
                } else {
                    int height2 = bitmap.getHeight();
                    int width2 = bitmap.getWidth();
//            final Bitmap bitmap1 = Bitmap.createBitmap(bitmap, (width - height) / 2, height / 6, height, height * 2 / 3);
                    int x1 = (int) (width2 * (PreviewBorderView.getIdCardViewLeft() / getScreenWidth(MainActivity.this)));
                    int y1 = (int) (height2 * (PreviewBorderView.getIdCardViewTop() / getScreenHeight(MainActivity.this)));
                    int width1 = (int) (width2 * (PreviewBorderView.getIdCardViewWidth() / getScreenWidth(MainActivity.this)));
                    int height1 = (int) (height2 * (PreviewBorderView.getIdCardViewHeight() / getScreenHeight(MainActivity.this)));
                    //截取中间身份证图片
                    final Bitmap bitmap1 = Bitmap.createBitmap(bitmap, x1, y1, width1, height1);
                    int x, y, w, h;
                    //身份证号
                    x = (int) (bitmap1.getWidth() * 0.340);
                    y = (int) (bitmap1.getHeight() * 0.800);
                    w = (int) (bitmap1.getWidth() * 0.6 + 0.5f);
                    h = (int) (bitmap1.getHeight() * 0.12 + 0.5f);
                    Bitmap bit_hm = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String id = doOcr(bit_hm);
                    //住址
                    x = (int) (bitmap1.getWidth() * 0.166);
                    y = (int) (bitmap1.getHeight() * 0.473);
                    w = (int) (bitmap1.getWidth() * 0.444 + 0.5f);
                    h = (int) (bitmap1.getHeight() * 0.33);
                    Bitmap bit_addr = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String addr = doOcr(bit_addr);
                    //姓名
                    x = (int) (bitmap1.getWidth() * 0.166);
                    y = (int) (bitmap1.getHeight() * 0.005);
                    w = (int) (bitmap1.getWidth() * 0.467);
                    h = (int) (bitmap1.getHeight() * 0.21);
                    Bitmap bit_name = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String name = doOcr(bit_name);
                    //性别
                    x = (int) (bitmap1.getWidth() * 0.166);
                    y = (int) (bitmap1.getHeight() * 0.21);
                    w = (int) (bitmap1.getWidth() * 0.112);
                    h = (int) (bitmap1.getHeight() * 0.13);
                    Bitmap bit_sex = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String sexResult = doOcr(bit_sex);
                    //名族
                    x = (int) (bitmap1.getWidth() * 0.278);
                    y = (int) (bitmap1.getHeight() * 0.21);
                    w = (int) (bitmap1.getWidth() * 0.255);
                    h = (int) (bitmap1.getHeight() * 0.13);
                    Bitmap bit_nation = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String nationResult = doOcr(bit_nation);
                    //出生
                    x = (int) (bitmap1.getWidth() * 0.166);
                    y = (int) (bitmap1.getHeight() * 0.34);
                    w = (int) (bitmap1.getWidth() * 0.467);
                    h = (int) (bitmap1.getHeight() * 0.13);
                    Bitmap bit_birthday = Bitmap.createBitmap(bitmap1, x, y, w, h);
//                    String birthdayResult = doOcr(bit_birthday);


                    try{
                        data = Bitmap2Bytes(bitmap1);
                        File file = new File(Environment.getExternalStorageDirectory(), "身份证" + ".jpg");
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();
                        testImageOcr(bitmap1);

                        data = Bitmap2Bytes(bit_hm);
                        File file3 = new File(Environment.getExternalStorageDirectory(), "身份证号" + ".jpg");
                        FileOutputStream fos3 = new FileOutputStream(file3);
                        fos3.write(data);
                        fos3.close();

                        data = Bitmap2Bytes(bit_addr);
                        File file4 = new File(Environment.getExternalStorageDirectory(), "住址" + ".jpg");
                        FileOutputStream fos4 = new FileOutputStream(file4);
                        fos4.write(data);
                        fos4.close();

                        data = Bitmap2Bytes(bit_name);
                        File file5 = new File(Environment.getExternalStorageDirectory(), "姓名" + ".jpg");
                        FileOutputStream fos5 = new FileOutputStream(file5);
                        fos5.write(data);
                        fos5.close();

                        data = Bitmap2Bytes(bit_sex);
                        File file6 = new File(Environment.getExternalStorageDirectory(), "性别" + ".jpg");
                        FileOutputStream fos6 = new FileOutputStream(file6);
                        fos6.write(data);
                        fos6.close();

                        data = Bitmap2Bytes(bit_nation);
                        File file7 = new File(Environment.getExternalStorageDirectory(), "民族" + ".jpg");
                        FileOutputStream fos7 = new FileOutputStream(file7);
                        fos7.write(data);
                        fos7.close();

                        data = Bitmap2Bytes(bit_birthday);
                        File file8 = new File(Environment.getExternalStorageDirectory(), "出生" + ".jpg");
                        FileOutputStream fos8 = new FileOutputStream(file8);
                        fos8.write(data);
                        fos8.close();
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                }

//                Intent intent = new Intent(MainActivity.this, ResultActivit.class);
//                startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }

    private BitmapFactory.Options opts = null;

    void testImageOcr(final Bitmap selectedImage) {
        opts = new BitmapFactory.Options();
        opts.inDensity = this.getResources().getDisplayMetrics().densityDpi;
        opts.inTargetDensity = this.getResources().getDisplayMetrics().densityDpi;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, API_TENCENTYUN_END_POINT);
                Youtu faceYoutu = new Youtu(Config.APP_ID, Config.SECRET_ID, Config.SECRET_KEY, Youtu.API_YOUTU_END_POINT);
//                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, "http://101.226.76.164:18082/youtu/");

                try {
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.id, opts);
//                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImage, opts);
                    JSONObject respose = faceYoutu.IdcardOcr(selectedImage, 0);
                    Toast.makeText(MainActivity.this, respose.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}


