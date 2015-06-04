package pl.tajchert.runtimepermissionhelper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
public class PermissionHelper {
    private static ArrayList<PermissionRequest> permissionRequests = new ArrayList<PermissionRequest>();

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     */
    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the Activity has access to all given permissions.
     * Always returns true on platforms below M.
     */
    public static boolean hasPermission(Activity activity, String permission) {
        if (!isMNC()) {
            return true;
        }

        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns true if the Activity has access to a given permission.
     * Always returns true on platforms below M.
     */
    public static boolean hasPermission(Activity activity, String[] permissions) {
        if (!isMNC()) {
            return true;
        }

        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMNC() {
        /*
         TODO: In the Android M Preview release, checking if the platform is M is done through
         the codename, not the version code. Once the API has been finalised, the following check
         should be used: */
        // return Build.VERSION.SDK_INT == Build.VERSION_CODES.MNC

        return "MNC".equals(Build.VERSION.CODENAME);
    }

    public void runWithPermission(Activity activity, String permission, PermissionCallback permissionCallback) {
        runWithPermission(activity, new String[]{permission}, permissionCallback);
    }

    public static void runWithPermission(Activity activity, String[] permissions, PermissionCallback permissionCallback) {
        if (permissionCallback == null) {
            return;
        }
        if (hasPermission(activity, permissions)) {
            permissionCallback.permissionGranted();
            return;
        }
        PermissionRequest permissionRequest = new PermissionRequest(new ArrayList<String>(Arrays.asList(permissions)), permissionCallback);
        permissionRequests.add(permissionRequest);

        activity.requestPermissions(permissions, permissionRequest.getRequestCode());
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionRequest requestResult = new PermissionRequest(requestCode);
        if (permissionRequests.contains(requestResult)) {
            PermissionRequest permissionRequest = permissionRequests.get(permissionRequests.indexOf(requestResult));
            if (verifyPermissions(grantResults)) {
                //Permission has been granted
                permissionRequest.getPermissionCallback().permissionGranted();
            } else {
                permissionRequest.getPermissionCallback().permissionNotAssigned();

            }
            permissionRequests.remove(requestResult);
        }
    }

}
