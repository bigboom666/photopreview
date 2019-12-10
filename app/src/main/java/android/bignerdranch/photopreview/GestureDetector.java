package android.bignerdranch.photopreview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GestureDetector implements View.OnTouchListener{

    private PhotoPreview mPhotoPreview;
    private ScaleGestureDetector mScaleGestureDetector;
    private android.view.GestureDetector mGestureDetector;

    public GestureDetector(PhotoPreview photoPreview) {
        mPhotoPreview = photoPreview;
        Context context = mPhotoPreview.getContext();
        mScaleGestureDetector = new ScaleGestureDetector(context, new OnScaleGestureAdapter());
        mGestureDetector = new android.view.GestureDetector(context, new OnGestureAdapter());
        mPhotoPreview.setOnTouchListener(this);
    }

    //继承View.OnTouchListener需要实现的方法
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) | mScaleGestureDetector.onTouchEvent(event);
    }


    private class OnScaleGestureAdapter extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mPhotoPreview.addScale(detector.getScaleFactor(),
                    detector.getFocusX(), detector.getFocusY());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mIntensifyView.home();
        }
    }

    private class OnGestureAdapter extends android.view.GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            mPhotoPreview.onTouch(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mPhotoPreview.doubleTap(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mPhotoPreview.scroll(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mPhotoPreview.fling(-velocityX, -velocityY);
            return true;
        }

    }
}
