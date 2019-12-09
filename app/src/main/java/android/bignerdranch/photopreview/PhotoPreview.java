package android.bignerdranch.photopreview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class PhotoPreview extends View {
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
    }
}
