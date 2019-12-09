package android.bignerdranch.photopreview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class PhotoPreview extends View implements ImageDelegate.Callback {
    private static final String TAG = "PhotoPreview";

    ImageDelegate mImageDelegate ;

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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IntensifyImageView);


        Log.e(TAG,"initialize before ScaleType");
        mImageDelegate.setScaleType(ScaleType.valueOf(
                a.getInt(R.styleable.IntensifyImageView_scaleType, ScaleType.FIT_CENTER.nativeInt)));
        mImageDelegate.setMinimumScale(
                a.getFloat(R.styleable.IntensifyImageView_minimumScale, 0f));

        mImageDelegate.setMaximumScale(
                a.getFloat(R.styleable.IntensifyImageView_maximumScale, Float.MAX_VALUE));

        mImageDelegate.setScale(a.getFloat(R.styleable.IntensifyImageView_scale, -1f));

        a.recycle();
    }



    //Delegate调用view的这些函数
    @Override
    public void onRequestInvalidate() {

    }

    @Override
    public boolean onRequestAwakenScrollBars() {
        return false;
    }

    @Override
    public void onScaleChange(float scale) {

    }
}
