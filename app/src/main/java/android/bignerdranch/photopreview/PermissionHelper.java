package android.bignerdranch.photopreview;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class PermissionHelper {


    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean hasPermissions(Activity activity) {
        for (String perm : PERMISSIONS) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }
        return true;
    }

    public static void requsetPermissions(Activity activity,int requestCode){
        activity.requestPermissions(PERMISSIONS,requestCode);
    }

}