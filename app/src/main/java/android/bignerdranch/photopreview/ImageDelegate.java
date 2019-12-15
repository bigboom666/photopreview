package android.bignerdranch.photopreview;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageDelegate {
    private static final String TAG = "ImageDelegate";

    private static final int MSG_IMAGE_SRC = 0;
    private static final int MSG_IMAGE_LOAD = 1;
    private static final int MSG_IMAGE_INIT = 2;
    private static final int MSG_IMAGE_SCALE = 3;
    private static final int MSG_IMAGE_DRAW = 4;
    private static final int MSG_IMAGE_RELEASE = 5;
    private static final int MSG_QUIT = 6;

    //todo 图片的宽高
    //始终表示着真正的图片的边界，需要计算显示的就是与可视区域的交集部分，每次当缩放，滑动等操作时都会去计算并修改RectF对象
    private RectF mImageArea = new RectF();
    private volatile List<ImageDrawable> mDrawables = new ArrayList<>();

    private enum State {
        NONE, SRC, LOAD, INIT, FREE
    }

    //获取屏幕宽高
    private DisplayMetrics mDisplayMetrics;
    private Callback mCallback;
    private PhotoPreviewHandler mHandler;
    private State mState = State.NONE;
    private Image mImage;
    private float mMinimumScale = 0f;
    private float mMaximumScale = Float.MAX_VALUE;
    private float mTempScale = 1f;

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


    public void setScale(float scale) {
        if (scale < 0.0f) return;
        mTempScale = scale;
        if (mState.ordinal() > State.INIT.ordinal()) {
            mState = State.INIT;
            requestInvalidate();
        }
    }



    public RectF getImageArea() {
        return mImageArea;
    }

    public List<ImageDrawable> obtainImageDrawables(Rect drawingRect) {
        if (Utils.isEmpty(drawingRect) || isNeedPrepare(drawingRect)) {
            return Collections.emptyList();
        }


        ArrayList<ImageDrawable> drawables = obtainBaseDrawables();
        Log.e(TAG,"ArrayList<ImageDrawable> drawables:"+drawables.size());

        //之前有 prepareDraw(Rect rect) 准备好mDrawables
        drawables.addAll(mDrawables);
        Log.e(TAG,"ArrayList<ImageDrawable> drawables  addall:"+drawables.size());

        //todo
        if (!Utils.equals(mImage.mCurrentState, Pair.create(mImageArea, drawingRect))) {
            mHandler.removeMessages(MSG_IMAGE_DRAW);
            sendMessage(MSG_IMAGE_DRAW, drawingRect);
        }

        return drawables;
    }

    //把mImage.mImageCache加入ImageDrawable
    public ArrayList<ImageDrawable> obtainBaseDrawables() {
        ArrayList<ImageDrawable> drawables = new ArrayList<>();
        drawables.add(new ImageDrawable(mImage.mImageCache,
                bitmapRect(mImage.mImageCache), Utils.round(mImageArea)));
        return drawables;
    }

    //todo 放到utils
    public static Rect bitmapRect(Bitmap bitmap) {
        return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * 判断是否需要更新状态
     *
     * @param drawingRect 绘制区域
     * @return true 需要准备
     */
    public boolean isNeedPrepare(Rect drawingRect) {
        mHandler.removeCallbacksAndMessages(null);
        switch (mState) {
            case NONE:
                return true;
            case SRC:
                sendMessage(MSG_IMAGE_LOAD);
                return true;
            case LOAD:
                sendMessage(MSG_IMAGE_INIT, drawingRect);
                return true;
            case INIT:
                sendMessage(MSG_IMAGE_SCALE, drawingRect);
                return mImageArea.isEmpty();  //todo ??
        }
        return false;
    }

    private void sendMessage(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private void sendMessage(int what, Object obj) {
        mHandler.obtainMessage(what, obj).sendToTarget();
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    //Delegate会调用view的这些函数
    public interface Callback {
        void onRequestInvalidate();
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


    //@WorkerThread
    private void prepareDraw(Rect rect) {
        float curScale = getScale();
        int sampleSize = getSampleSize(1f / curScale);
        Pair<RectF, Rect> newState = Pair.create(new RectF(mImageArea), new Rect(rect));

        if (mImage.mImageSampleSize > sampleSize) {
            RectF drawingRect = new RectF(rect);

            if (drawingRect.intersect(mImageArea)) {
                drawingRect.offset(-mImageArea.left, -mImageArea.top);
            }

            float blockSize = BLOCK_SIZE * curScale * sampleSize;
            Rect blocks = Utils.blocks(drawingRect, blockSize);

            List<ImageDrawable> drawables = new ArrayList<>();
            int roundLeft = Math.round(mImageArea.left);
            int roundTop = Math.round(mImageArea.top);
            IntensifyImageCache.ImageCache imageCache = mImage.mImageCaches.get(sampleSize);
            if (imageCache != null) {
                for (int i = blocks.top; i <= blocks.bottom; i++) {
                    for (int j = blocks.left; j <= blocks.right; j++) {
                        Bitmap bitmap = imageCache.createGet(new Point(j, i));
                        if (bitmap == null) continue;
                        Rect src = bitmapRect(bitmap);
                        Rect dst = Utils.blockRect(j, i, blockSize, roundLeft, roundTop);
                        if (src.bottom * sampleSize != BLOCK_SIZE
                                || src.right * sampleSize != BLOCK_SIZE) {

                            dst.set(src.left + dst.left, src.top + dst.top,
                                    Math.round(src.right * sampleSize * curScale) + dst.left,
                                    Math.round(src.bottom * sampleSize * curScale) + dst.top);
                        }
                        drawables.add(new ImageDrawable(bitmap, src, dst));
                    }
                }
            }

            mDrawables.clear();
            if (Utils.equals(newState, Pair.create(new RectF(mImageArea), new Rect(rect)))) {
                mDrawables.addAll(drawables);
            }
        } else mDrawables.clear();

        mImage.mCurrentState = Pair.create(new RectF(mImageArea), new Rect(rect));
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


    public static class ImageDrawable {
        //todo mBitmap是啥？？？
        Bitmap mBitmap;
        Rect mSrc;
        Rect mDst;

        public ImageDrawable(Bitmap bitmap, Rect src, Rect dst) {
            this.mBitmap = bitmap;
            this.mSrc = src;
            this.mDst = dst;
        }
    }




}



