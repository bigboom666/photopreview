package android.bignerdranch.photopreview;

import android.graphics.Rect;
import android.graphics.RectF;

public class Utils {

    private Utils() {

    }


    public static boolean isEmpty(Rect rect) {
        return rect == null || rect.isEmpty();
    }

    public static Rect round(RectF rect) {
        return new Rect(Math.round(rect.left), Math.round(rect.top),
                Math.round(rect.right), Math.round(rect.bottom));
    }


    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
