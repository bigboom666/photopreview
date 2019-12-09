package android.bignerdranch.photopreview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

public class ImageDelegate {
    private static final String TAG = "ImageDelegate";

    enum ScaleType {

        FIT_AUTO(1),

        FIT_CENTER(2),

        CENTER(3),

        CENTER_INSIDE(4);

        ScaleType(int ni) {
            defaultType = ni;
        }
         int defaultType;
    }

    //获取屏幕宽高
    private DisplayMetrics mDisplayMetrics;
    private Callback mCallback;
    private PhotoPreviewHandler mHandler;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    public ImageDelegate(DisplayMetrics metrics, Callback callback) {
        mDisplayMetrics = metrics;
        mCallback = callback;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new PhotoPreviewHandler(handlerThread.getLooper());


    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        Log.e(TAG, "set ScaleType: "+mScaleType);

        if (mState.ordinal() >= State.INIT.ordinal()) {
            mState = State.INIT;
            mImage.mCurrentState = null;
            requestInvalidate();
        }
    }



    //Delegate会调用view的这些函数
    public interface Callback {
        void onRequestInvalidate();

        boolean onRequestAwakenScrollBars();

        void onScaleChange(float scale);
    }

    private class PhotoPreviewHandler extends Handler {

        public PhotoPreviewHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IMAGE_DRAW:
                    prepareDraw((Rect) msg.obj);
                    requestInvalidate();
                    break;

                case MSG_IMAGE_SCALE:
                    initScaleType((Rect) msg.obj);
                    requestInvalidate();
                    requestAwakenScrollBars();
                    break;

                case MSG_IMAGE_SRC:
                    prepare((ImageDecoder) msg.obj);
                    requestInvalidate();
                    break;

                case MSG_IMAGE_LOAD:
                    load();
                    requestInvalidate();
                    break;

                case MSG_IMAGE_INIT:
                    initialize((Rect) msg.obj);
                    requestInvalidate();
                    break;

                case MSG_IMAGE_RELEASE:
                    release();
                    break;

                case MSG_QUIT:
                    release();
                    try {
                        getLooper().quit();
                    } catch (Throwable throwable) {
                        Logger.w(TAG, throwable);
                    }
                    break;
            }
        }

}



