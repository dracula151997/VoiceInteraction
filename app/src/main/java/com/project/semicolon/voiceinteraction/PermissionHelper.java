package com.project.semicolon.voiceinteraction;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionHelper {
    private Context context;

    public PermissionHelper(Context context) {
        this.context = context;
    }

    public boolean isPermissionGranted(String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;

    }

    public boolean checkDevice() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }
}
