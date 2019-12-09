package android.bignerdranch.photopreview;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageDelegate {
    private static final String TAG = "ImageDelegate";



    private enum State {
        NONE, SRC, LOAD, INIT, FREE
    }

    //获取屏幕宽高
    private DisplayMetrics mDisplayMetrics;
    private Callback mCallback;
    private PhotoPreviewHandler mHandler;
    private State mState = State.NONE;
    private Image mImage;


    public ImageDelegate(DisplayMetrics metrics, Callback callback) {
        mDisplayMetrics = metrics;
        mCallback = callback;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new PhotoPreviewHandler(handlerThread.getLooper());
    }


    //ImageDecoder类
    public interface ImageDecoder {
        BitmapRegionDecoder newRegionDecoder() throws IOException;
    }

    public static class ImagePathDecoder implements ImageDecoder {
        private String mPath;

        public ImagePathDecoder(String path) {
            mPath = path;
        }

        @Override
        public BitmapRegionDecoder newRegionDecoder() throws IOException {
            return BitmapRegionDecoder.newInstance(mPath, false);
        }
    }

    public static class ImageFileDecoder implements ImageDecoder {
        private File mFile;

        public ImageFileDecoder(File file) {
            mFile = file;
        }

        @Override
        public BitmapRegionDecoder newRegionDecoder() throws IOException {
            return BitmapRegionDecoder.newInstance(mFile.getAbsolutePath(), false);
        }
    }

    public static class ImageInputStreamDecoder implements ImageDecoder {
        private InputStream mInputStream;

        public ImageInputStreamDecoder(InputStream inputStream) {
            mInputStream = inputStream;
        }

        @Override
        public BitmapRegionDecoder newRegionDecoder() throws IOException {
            return BitmapRegionDecoder.newInstance(mInputStream, false);
        }
    }



    public void setMinimumScale(float minimumScale) {
        if (minimumScale <= mMaximumScale) {
            mMinimumScale = minimumScale;
            if (mState.ordinal() > State.INIT.ordinal()) {
                mState = State.INIT;
                requestInvalidate();
                requestAwakenScrollBars();
            }
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
                        Log.w(TAG, throwable);
                    }
                    break;
            }
        }

    }

    private class Image {

        BitmapRegionDecoder mImageRegionDecoder;

        int mImageSampleSize;
        Bitmap mImageCache;

        int mImageWidth;
        int mImageHeight;

        IntensifyImageCache mImageCaches;

        volatile Pair<RectF, Rect> mCurrentState;

        private Image(ImageDecoder decoder) {
            try {
                mImageRegionDecoder = decoder.newRegionDecoder();
            } catch (IOException e) {
                throw new RuntimeException("无法访问图片");
            }

            mImageCaches = new IntensifyImageCache(5,
                    mDisplayMetrics.widthPixels * mDisplayMetrics.heightPixels << 4,
                    BLOCK_SIZE, mImageRegionDecoder);
        }

        public void release() {
            mImageRegionDecoder.recycle();
            if (mImageCache != null && !mImageCache.isRecycled()) {
                mImageCache.recycle();
            }
            this.mImageCaches.evictAll();
            mCurrentState = null;
        }
    }

}



