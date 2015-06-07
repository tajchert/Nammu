package pl.tajchert.nammu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
public class Nammu {
    private static final String TAG = Nammu.class.getSimpleName();
    private static Context context;
    private static SharedPreferences sharedPreferences;
    private static final String KEY_PREV_PERMISSIONS = "previous_permissions";
    private static ArrayList<PermissionRequest> permissionRequests = new ArrayList<PermissionRequest>();

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("pl.tajchert.runtimepermissionhelper", Context.MODE_PRIVATE);
        Nammu.context = context;
    }

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

    private static boolean isMNC() {
        /*
         TODO: In the Android M Preview release, checking if the platform is M is done through
         the codename, not the version code. Once the API has been finalised, the following check
         should be used: */
        // return Build.VERSION.SDK_INT == Build.VERSION_CODES.MNC

        return "MNC".equals(Build.VERSION.CODENAME);
    }

    public static void askForPermission(Activity activity, String permission, PermissionCallback permissionCallback) {
        askForPermission(activity, new String[]{permission}, permissionCallback);
    }

    public static void askForPermission(Activity activity, String[] permissions, PermissionCallback permissionCallback) {
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
                permissionRequest.getPermissionCallback().permissionRefused();

            }
            permissionRequests.remove(requestResult);
        }
    }


    //Listening part

    /**
     * Save permission when we got granted it - for later use to detect when it will got revoke
     * @param permissions
     */
    public static void savePermission(String[] permissions) {
        if(permissions == null) {
            return;
        }
        Set<String> set = new HashSet<String>();
        for(String perm : permissions) {
            set.add(perm);
        }
        set.addAll(getPrevPermissions());
        sharedPreferences.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply();
    }

    /**
     * Save permission when we got granted it - for later use to detect when it will got revoke
     * @param permissions
     */
    public static void savePermission(ArrayList<String> permissions) {
        if(permissions == null) {
            return;
        }
        Set<String> set = new HashSet<String>();
        set.addAll(permissions);
        set.addAll(getPrevPermissions());
        sharedPreferences.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply();
    }

    /**
     * Save permission when we got granted it - for later use to detect when it will got revoke
     * @param permission
     */
    public static void savePermission(String permission) {
        if(permission == null) {
            return;
        }
        Set<String> set = new HashSet<String>();
        set.add(permission);
        set.addAll(getPrevPermissions());
        sharedPreferences.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply();
    }

    /**
     * Get list of previous Permission that we are listening to, past tense as they are quite possible outdated (saved with savePermission())
     * @return
     */
    public static ArrayList<String> getPrevPermissions() {
        ArrayList<String> prevPermissions = new ArrayList<String>();
        prevPermissions.addAll(sharedPreferences.getStringSet(KEY_PREV_PERMISSIONS, new HashSet<String>()));
        return prevPermissions;
    }

    /**
     * Check if we are subscribed to give Permission
     * @param permission
     * @return
     */
    public static boolean containsPermission(String permission) {
        if(permission == null) {
            return false;
        }
        if(getPrevPermissions().contains(permission)) {
            return true;
        }
        return false;
    }

    /**
     * Use to unsubscribe to particular Permission
     * @param permission
     */
    public static void removePermission(String permission) {
        if(containsPermission(permission)) {
            ArrayList<String> prevPermissions = getPrevPermissions();
            prevPermissions.remove(permission);
            Set<String> set = new HashSet<String>();
            set.addAll(prevPermissions);
            sharedPreferences.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply();
        }
    }

    /**
     * Used to trigger comparing process - @permissionListener will be called each time Permission was revoked
     * @param permissionListener
     */
    public static void permissionCompare(PermissionListener permissionListener) {
        if(context == null) {
            throw new RuntimeException("Before comparing permissions you need to call Nammu.init(context)");

        }
        ArrayList<String> prevPermissions = getPrevPermissions();
        for(String permission : prevPermissions) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                //is ok, we still have this permission
            } else {
                //We lost access to this permission, remove it from list of saved granted permissions and inform listener
                if (permissionListener != null) {
                    permissionListener.permissionsChanged(permission);
                }
                removePermission(permission);
            }
        }
    }

    public static boolean checkPermission(String permissionName) {
        if(context == null) {
            throw new RuntimeException("Before comparing permissions you need to call Nammu.init(context)");
        }
        return PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(permissionName);
    }
}
