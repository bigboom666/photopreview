package android.bignerdranch.photopreview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

import java.util.List;
import android.bignerdranch.photopreview.ImageDelegate.ImageDrawable;

public class PhotoPreview extends View implements ImageDelegate.Callback {
    private static final String TAG = "PhotoPreview";
    private static final boolean DEBUG = true;

    ImageDelegate mImageDelegate ;

    private Paint mPaint;

    private Paint mTextPaint;

    private Paint mBoardPaint;
    private OverScroller mScroller;

    //绘制区域
    private volatile Rect mDrawingRect = new Rect();
    private ImageDelegate mDelegate;


    public PhotoPreview(Context context) {
        this(context, null, 0);
    }

    public PhotoPreview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);

    }

    public PhotoPreview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr){
        mImageDelegate = new ImageDelegate(getResources().getDisplayMetrics(), this);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(1f);
        mPaint.setStyle(Paint.Style.STROKE);

        //debug画框
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStrokeWidth(1f);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(24);

        mBoardPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mBoardPaint.setColor(Color.RED);
        mBoardPaint.setStrokeWidth(2f);
        mBoardPaint.setStyle(Paint.Style.STROKE);

        new GestureDetector(this);
        mScroller = new OverScroller(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mDrawingRect);

        List<ImageDrawable> drawables = mDelegate.obtainImageDrawables(mDrawingRect);

        Log.e(TAG,"List<ImageDrawable>"+drawables.size());

        int save = canvas.save();
        int i = 0;
        for (ImageDrawable drawable : drawables) {
            if (drawable == null || drawable.mBitmap.isRecycled()) {
                continue;
            }
            canvas.drawBitmap(drawable.mBitmap, drawable.mSrc, drawable.mDst, mPaint);
            if (DEBUG) {
                canvas.drawRect(drawable.mDst, mPaint);
                canvas.drawText(String.valueOf(++i), drawable.mDst.left + 4,
                        drawable.mDst.top + mTextPaint.getTextSize(), mTextPaint);
            }
        }
        if (DEBUG) {
            canvas.drawRect(mDrawingRect, mBoardPaint);
            canvas.drawRect(mDelegate.getImageArea(), mBoardPaint);
        }

        canvas.restoreToCount(save);
    }






    //Delegate调用view的这些函数
    @Override
    public void onRequestInvalidate() {

    }

    @Override
    public void onScaleChange(float scale) {

    }





}
