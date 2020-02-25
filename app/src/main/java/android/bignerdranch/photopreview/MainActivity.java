package android.bignerdranch.photopreview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String[] mPictures;
    private static final String PIC_DIR = "pictures";
    ZoomImageView zoomImageView;
    private static int PictureNum = 9;
    private static int currentPicture = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomImageView = findViewById(R.id.zoomimageview);

        try {
            mPictures = getAssets().list(PIC_DIR);
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        Log.d(TAG, "onCreate: mPictures size" + mPictures.length);

        InputStream fis = null;
        try {
            fis = getAssets().open(PIC_DIR + "/" + mPictures[currentPicture]);
            if (fis == null) {
                Log.d(TAG, "fis is null ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap1 = BitmapFactory.decodeStream(fis);
        Bitmap bitmap3 = zoomBitmap(bitmap1,1080,960);
        Bitmap bitmap2 = scaleBitmap(bitmap1,100,200);
        Bitmap bitmap = getDecodeBitmap(fis,1080,960);

        if (bitmap == null) {
            Log.d(TAG, "bitmap is null ");
        }
        zoomImageView.setSourceImageBitmap(bitmap, getApplicationContext());
        //zoomImageView.setSourceBitmap(bitmap);


    }

    public void switchpictures(boolean next) {
        InputStream fis = null;
        if (next) {
            if (currentPicture < (PictureNum - 1)) currentPicture++;
            else currentPicture = 0;
        } else {
            if (currentPicture > 0) currentPicture--;
            else currentPicture = (PictureNum - 1);
        }
        try {
            fis = getAssets().open(PIC_DIR + "/" + mPictures[currentPicture]);
            if (fis == null) {
                Log.d(TAG, "fis is null ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap1 = BitmapFactory.decodeStream(fis);
        Bitmap bitmap3 = zoomBitmap(bitmap1,1080,960);
        Bitmap bitmap2 = scaleBitmap(bitmap1,100,200);
        Bitmap bitmap = getDecodeBitmap(fis,1080,960);
        if (bitmap == null) {
            Log.d(TAG, "bitmap is null ");
        }
        zoomImageView.setSourceImageBitmap(bitmap, getApplicationContext());
        //zoomImageView.setSourceBitmap(bitmap);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);


        MenuInflater inflater = new MenuInflater(MainActivity.this);

        //MenuInflater inflater = getMenuInflater();//另一种方法
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
                switchpictures(true);
                break;
            case R.id.prew:
                Toast.makeText(this, "prew", Toast.LENGTH_SHORT).show();
                switchpictures(false);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,float w,float h){
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        float x = 0,y = 0,scaleWidth = width,scaleHeight = height;
        Bitmap newbmp;
        //Log.e("gacmy","width:"+width+" height:"+height);
        if(w > h){//比例宽度大于高度的情况
            float scale = w/h;
            float tempH = width/scale;
            if(height > tempH){//
                x = 0;
                y=(height-tempH)/2;
                scaleWidth = width;
                scaleHeight = tempH;
            }else{
                scaleWidth = height*scale;
                x = (width - scaleWidth)/2;
                y= 0;
            }


            Log.e("gacmy","scale:"+scale+" scaleWidth:"+scaleWidth+" scaleHeight:"+scaleHeight);
        }else if(w < h){//比例宽度小于高度的情况
            float scale = h/w;
            float tempW = height/scale;
            if(width > tempW){
                y = 0;
                x = (width -tempW)/2;
                scaleWidth = tempW;
                scaleHeight = height;
            }else{
                scaleHeight = width*scale;
                y = (height - scaleHeight)/2;
                x = 0;
                scaleWidth = width;
            }

        }else{//比例宽高相等的情况
            if(width > height){
                x= (width-height)/2;
                y = 0;
                scaleHeight = height;
                scaleWidth = height;
            }else {
                y=(height - width)/2;
                x = 0;
                scaleHeight = width;
                scaleWidth = width;
            }
        }
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) scaleWidth, (int) scaleHeight, null, false);// createBitmap()方法中定义的参数x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight()
            //bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }


    /**
     * 按宽/高缩放图片到指定大小并进行裁剪得到中间部分图片
     * @param bitmap 源bitmap
     * @param w 缩放后指定的宽度
     * @param h 缩放后指定的高度
     * @return 缩放后的中间部分图片 Bitmap
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.i("TAG", "zoomBitmap---" + "width:" + width + "---" + "height:" + height);
        float scaleWidht, scaleHeight, x, y;
        Bitmap newbmp;
        Matrix matrix = new Matrix();
        if (width > height) {
            scaleWidht = ((float) h / height);
            scaleHeight = ((float) h / height);
            x = (width - w * height / h) / 2;// 获取bitmap源文件中x做表需要偏移的像数大小
            y = 0;
        } else if (width < height) {
            scaleWidht = ((float) w / width);
            scaleHeight = ((float) w / width);
            x = 0;
            y = (height - h * width / w) / 2;// 获取bitmap源文件中y做表需要偏移的像数大小
        } else {
            scaleWidht = ((float) w / width);
            scaleHeight = ((float) w / width);
            x = 0;
            y = 0;
        }
        matrix.postScale(scaleWidht, scaleHeight);
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) (width - x*2), (int) (height - y*2), matrix, true);// createBitmap()方法中定义的参数x+width要小于或等于bitmap.getWidth()，y+height要小于或等于bitmap.getHeight()
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }

    private Bitmap createRoundConnerImage(Bitmap source, float radius) {
        Bitmap target=source;
        if (source == null) {
            return null;
        }

        RectF roundRectF = new RectF(0, 0, source.getWidth(), source.getHeight());
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        Canvas canvas = new Canvas(target);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));
        canvas.drawRoundRect(roundRectF, radius , radius , paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        paint.setXfermode(null);
        return target;
    }




    //----------------------------------------------------------------------------------------------


    /**
     * get decode bitMap
     * @param resources
     * @param resId the bitmap id
     * @param reqWidth the loading view width
     * @param reqHeight the loading view height
     * @return decode bitmap
     */
    private Bitmap getDecodeBitmap(Resources resources, int resId, int reqWidth, int reqHeight){

        //创建option
        BitmapFactory.Options option =new BitmapFactory.Options();
        //加载原图，计算原图宽/高
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources,resId,option);

        //计算SampleSize
        option.inSampleSize = getCalculateInSampleSize(reqWidth, reqHeight, option);

        return BitmapFactory.decodeResource(resources,resId,option);
    }



    private Bitmap getDecodeBitmap(InputStream fis,  int reqWidth, int reqHeight){

        //创建option
        BitmapFactory.Options option =new BitmapFactory.Options();
        //加载原图，计算原图宽/高
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fis,null,option);

        //计算SampleSize
        option.inSampleSize = getCalculateInSampleSize(reqWidth, reqHeight, option);
        option.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(fis,null,option);
    }





    /**
     * Calculate inSampleSize
     * @param reqWidth the loading view width
     * @param reqHeight the loading view Height
     * @param option BitmapFactory of Option
     * @return the BitmapFactory of Option's InSampleSize
     */
    private int getCalculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options option) {

        int height = option.outHeight;
        int width = option.outWidth;
        int sampleSize = 1;

        if(height > reqHeight || width > reqWidth){

            int halfHeight = height/2;
            int halfWidth = width/2;
            while(halfHeight/sampleSize > reqHeight || halfWidth/sampleSize > halfWidth){
                sampleSize *= 2;
            }
        }

        return sampleSize;
    }


}
