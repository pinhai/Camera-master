package com.example.binbin.photo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ResultActivit extends AppCompatActivity {

    Bitmap photo;
    Bitmap number;
    Bitmap address;
    Bitmap name;
    ImageView imageView;
    EditText nameText;
    EditText addressText;
    EditText numberText, et_sex, et_nation, et_birthday;
    String nameResult, addressResult, numberResult, sexResult, nationResult, birthdayResult;
    private ProgressDialog dialog;
    private AlertDialog dialogRequestPerm;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);

            dialog.cancel();

            addressText.setText(addressResult);
            nameText.setText(nameResult);
            numberText.setText(numberResult);
            et_sex.setText(sexResult);
            et_nation.setText(nationResult);
            et_birthday.setText(birthdayResult);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/身份证" + ".jpg");
        imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(photo);

        addressText = (EditText) findViewById(R.id.address);
        nameText = (EditText) findViewById(R.id.name);
        numberText = (EditText) findViewById(R.id.number);
        et_birthday = (EditText) findViewById(R.id.birthday);
        et_nation = (EditText) findViewById(R.id.nation);
        et_sex = (EditText) findViewById(R.id.sex);

        dialog = new ProgressDialog(this);
        dialog.setMessage("识别中");
        dialog.show();

        checkPermission();

    }

    private boolean copyAssetFile() throws Exception {

        String dir = getSDPath() + "/tessdata";
        String filePath = getSDPath() + "/tessdata/chi_sim.traineddata";
        File f = new File(dir);
        if (f.exists()) {
        } else {
            f.mkdirs();
        }
        File dataFile = new File(filePath);
        if (dataFile.exists()) {
            return true;// 文件存在
        } else {

            InputStream in = this.getAssets().open("chi_sim.traineddata");

            File outFile = new File(filePath);
            if (outFile.exists()) {
                outFile.delete();
            }
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

        return false;
    }

    private void startOcrThread(){
        try{
            copyAssetFile();
        }catch(Exception e){
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run(){

                number = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/身份证号" + ".jpg");
                name = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/姓名" + ".jpg");
                address = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/住址" + ".jpg");
                Bitmap sex = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/性别" + ".jpg");
                Bitmap nation = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/民族" + ".jpg");
                Bitmap birthday = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/出生" + ".jpg");


                String lang = "chi_sim";       //chi_sim
                numberResult = doOcr(number, lang);
                nameResult = doOcr(name, lang);
                addressResult = doOcr(address, lang);
                sexResult = doOcr(sex, lang);
                nationResult = doOcr(nation, lang);
                birthdayResult = doOcr(birthday, lang);


                if(numberResult != null && nameResult != null && addressResult != null){

                    Message msg = new Message();
                    handler.sendMessage(msg);

                }


            }
        }).start();
    }

    private void checkPermission() {
        String[] permStr = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
        // 权限判断
        if (PermissionUtil.checkPermissions(this, permStr)) {
            startOcrThread();
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
                    startOcrThread();
                } else {
                    // 获取权限失败
                    Toast.makeText(ResultActivit.this, "权限获取失败", Toast.LENGTH_SHORT).show();
                    setPermissionApplyDialog();
                }
                break;

        }

    }

    /**
     * 权限申请
     */
    private void setPermissionApplyDialog() {
        try {
            dialogRequestPerm = new AlertDialog.Builder(ResultActivit.this)
                    .setTitle("提示")
                    .setMessage("自动申请权限失败。\\n请点击“设置”-“权限管理”-“存储”手动打开所需权限。").setCancelable(false)
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("设置", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PermissionUtil.startAppSettings(ResultActivit.this);
                        }
                    }).create();
            dialogRequestPerm.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 进行图片识别
     *
     * @param bitmap   待识别图片
     * @param language 识别语言
     * @return 识别结果字符串
     */
    public String doOcr(Bitmap bitmap, String language){
        TessBaseAPI baseApi = new TessBaseAPI();

//        String datapath = getSDPath();
//        File tessdata = new File(datapath + "/tessdata");
//        if(!tessdata.exists()){
//            tessdata.mkdirs();
//        }

        baseApi.init(getSDPath(), language);

        // 必须加此行，tess-two要求BMP必须为此配置
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        baseApi.setImage(bitmap);

        String text = baseApi.getUTF8Text();

        baseApi.clear();
        baseApi.end();

        return text;
    }


    /**
     * 获取sd卡的路径
     *
     * @return 路径的字符串
     */
    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if(sdCardExist){
            sdDir = Environment.getExternalStorageDirectory();// 获取外存目录
        }
        return sdDir.toString();
    }


}
