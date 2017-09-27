package pl.tajchert.nammu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
  private static final String KEY_IGNORED_PERMISSIONS = "ignored_permissions";
  private static ArrayList<PermissionRequest> permissionRequests =
      new ArrayList<PermissionRequest>();
  public static final int SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE = 1971;
  public static final int WRITE_SETTINGS_PERMISSION_REQ_CODE = 1970;

  public static void init(Context context) {
    sharedPreferences =
        context.getSharedPreferences("pl.tajchert.runtimepermissionhelper", Context.MODE_PRIVATE);
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
   * Returns true if the Activity has access to given permissions.
   */
  public static boolean hasPermission(Activity activity, String permission) {
    return ContextCompat.checkSelfPermission(activity, permission)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Returns true if the Activity has access to a all given permission.
   */
  public static boolean hasPermission(Activity activity, String[] permissions) {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(activity, permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  /*
   * If we override other methods, lets do it as well, and keep name same as it is already weird enough.
   * Returns true if we should show explanation why we need this permission.
   */
  public static boolean shouldShowRequestPermissionRationale(Activity activity,
      String permissions) {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions);
  }

  public static boolean shouldShowRequestPermissionRationale(Fragment fragment,
      String permissions) {
    return fragment.shouldShowRequestPermissionRationale(permissions);
  }

  public static boolean shouldShowRequestPermissionRationale(android.app.Fragment fragment,
      String permissions) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return fragment.shouldShowRequestPermissionRationale(permissions);
    }
    return false;
  }

  public static void askForPermission(Activity activity, String permission,
      PermissionCallback permissionCallback) {
    askForPermission(activity, new String[] { permission }, permissionCallback);
  }

  public static void askForPermission(Activity activity, String[] permissions,
      PermissionCallback permissionCallback) {
    if (permissionCallback == null) {
      return;
    }
    if (hasPermission(activity, permissions)) {
      permissionCallback.permissionGranted();
      return;
    }
    PermissionRequest permissionRequest =
        new PermissionRequest(new ArrayList<String>(Arrays.asList(permissions)),
            permissionCallback);
    permissionRequests.add(permissionRequest);

    ActivityCompat.requestPermissions(activity, permissions, permissionRequest.getRequestCode());
  }

  /**
   * There are a couple of permissions that don't behave like normal and dangerous permissions.
   * SYSTEM_ALERT_WINDOW and WRITE_SETTINGS are particularly sensitive, so most apps should not use
   * them.
   * If an app needs one of these permissions, it must declare the permission in the manifest, and
   * send an intent requesting the user's authorization.
   * The system responds to the intent by showing a detailed management screen to the user.
   */
  @TargetApi(Build.VERSION_CODES.M) @SuppressLint("ValidFragment")
  public static void askForSpecialPermission(final Activity activity, String permission,
      final PermissionCallback permissionCallback) {
    android.app.Fragment fragment;
    android.app.FragmentTransaction fragmentTransaction;

    if (permissionCallback == null) {
      return;
    }

    switch (permission) {
      case Manifest.permission.SYSTEM_ALERT_WINDOW:
        if (Settings.canDrawOverlays(activity)) {
          permissionCallback.permissionGranted();
          return;
        }
        fragment = new android.app.Fragment() {
          @Override public void onAttach(Context context) {
            super.onAttach(context);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE);
          }

          @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE) {
              if (Settings.canDrawOverlays(activity)) {
                permissionCallback.permissionGranted();
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
              } else {
                permissionCallback.permissionRefused();
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
              }
            }
            super.onActivityResult(requestCode, resultCode, data);
          }
        };
        fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(fragment, "getpermission");
        fragmentTransaction.commit();
        break;
      case Manifest.permission.WRITE_SETTINGS:
        if (Settings.System.canWrite(activity)) {
          permissionCallback.permissionGranted();
          return;
        }
        fragment = new android.app.Fragment() {
          @Override public void onAttach(Context context) {
            super.onAttach(context);
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, WRITE_SETTINGS_PERMISSION_REQ_CODE);
          }

          @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == WRITE_SETTINGS_PERMISSION_REQ_CODE) {
              if (Settings.System.canWrite(activity)) {
                permissionCallback.permissionGranted();
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
              } else {
                permissionCallback.permissionRefused();
                getActivity().getFragmentManager().beginTransaction().remove(this).commit();
              }
            }
            super.onActivityResult(requestCode, resultCode, data);
          }
        };
        fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(fragment, "getpermission");
        fragmentTransaction.commit();
        break;
    }
  }

  public static void askForPermission(Fragment fragment, String permission,
      PermissionCallback permissionCallback) {
    askForPermission(fragment, new String[] { permission }, permissionCallback);
  }

  public static void askForPermission(android.app.Fragment fragment, String permission,
      PermissionCallback permissionCallback) {
    askForPermission(fragment, new String[] { permission }, permissionCallback);
  }

  private static void askForPermission(Fragment fragment, String[] permissions,
      PermissionCallback permissionCallback) {
    if (permissionCallback == null) {
      return;
    }
    if (hasPermission(fragment.getActivity(), permissions)) {
      permissionCallback.permissionGranted();
      return;
    }
    PermissionRequest permissionRequest =
        new PermissionRequest(new ArrayList<String>(Arrays.asList(permissions)),
            permissionCallback);
    permissionRequests.add(permissionRequest);

    fragment.requestPermissions(permissions, permissionRequest.getRequestCode());
  }

  private static void askForPermission(android.app.Fragment fragment, String[] permissions,
      PermissionCallback permissionCallback) {
    if (permissionCallback == null) {
      return;
    }
    if (hasPermission(fragment.getActivity(), permissions)) {
      permissionCallback.permissionGranted();
      return;
    }
    PermissionRequest permissionRequest =
        new PermissionRequest(new ArrayList<String>(Arrays.asList(permissions)),
            permissionCallback);
    permissionRequests.add(permissionRequest);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      fragment.requestPermissions(permissions, permissionRequest.getRequestCode());
    }
  }

  public static void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    PermissionRequest requestResult = new PermissionRequest(requestCode);
    if (permissionRequests.contains(requestResult)) {
      PermissionRequest permissionRequest =
          permissionRequests.get(permissionRequests.indexOf(requestResult));
      if (verifyPermissions(grantResults)) {
        //Permission has been granted
        permissionRequest.getPermissionCallback().permissionGranted();
      } else {
        permissionRequest.getPermissionCallback().permissionRefused();
      }
      permissionRequests.remove(requestResult);
    }
    refreshMonitoredList();
  }

  //Permission monitoring part below

  /**
   * Get list of currently granted permissions, without saving it inside Nammu
   *
   * @return currently granted permissions
   */
  public static ArrayList<String> getGrantedPermissions() {
    if (context == null) {
      throw new RuntimeException("Must call init() earlier");
    }
    ArrayList<String> permissions = new ArrayList<String>();
    ArrayList<String> permissionsGranted = new ArrayList<String>();
    //Group location
    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
    permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
    //Group Calendar
    permissions.add(Manifest.permission.WRITE_CALENDAR);
    permissions.add(Manifest.permission.READ_CALENDAR);
    //Group Camera
    permissions.add(Manifest.permission.CAMERA);
    //Group Contacts
    permissions.add(Manifest.permission.WRITE_CONTACTS);
    permissions.add(Manifest.permission.READ_CONTACTS);
    permissions.add(Manifest.permission.GET_ACCOUNTS);
    //Group Microphone
    permissions.add(Manifest.permission.RECORD_AUDIO);
    //Group Phone
    permissions.add(Manifest.permission.CALL_PHONE);
    permissions.add(Manifest.permission.READ_PHONE_STATE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      permissions.add(Manifest.permission.READ_CALL_LOG);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      permissions.add(Manifest.permission.WRITE_CALL_LOG);
    }
    permissions.add(Manifest.permission.ADD_VOICEMAIL);
    permissions.add(Manifest.permission.USE_SIP);
    permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
    //Group Body sensors
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
      permissions.add(Manifest.permission.BODY_SENSORS);
    }
    //Group SMS
    permissions.add(Manifest.permission.SEND_SMS);
    permissions.add(Manifest.permission.READ_SMS);
    permissions.add(Manifest.permission.RECEIVE_SMS);
    permissions.add(Manifest.permission.RECEIVE_WAP_PUSH);
    permissions.add(Manifest.permission.RECEIVE_MMS);
    //Group Storage
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(context, permission)
          == PackageManager.PERMISSION_GRANTED) {
        permissionsGranted.add(permission);
      }
    }
    return permissionsGranted;
  }

  /**
   * Refresh currently granted permission list, and save it for later comparing using
   * @permissionCompare()
   */
  public static void refreshMonitoredList() {
    ArrayList<String> permissions = getGrantedPermissions();
    Set<String> set = new HashSet<String>(permissions);
    sharedPreferences.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply();
  }

  /**
   * Get list of previous Permissions, from last refreshMonitoredList() call and they may be
   * outdated,
   * use getGrantedPermissions() to get current
   */
  public static ArrayList<String> getPreviousPermissions() {
    ArrayList<String> prevPermissions = new ArrayList<String>();
    prevPermissions.addAll(
        sharedPreferences.getStringSet(KEY_PREV_PERMISSIONS, new HashSet<String>()));
    return prevPermissions;
  }

  public static ArrayList<String> getIgnoredPermissions() {
    ArrayList<String> ignoredPermissions = new ArrayList<String>();
    ignoredPermissions.addAll(
        sharedPreferences.getStringSet(KEY_IGNORED_PERMISSIONS, new HashSet<String>()));
    return ignoredPermissions;
  }

  /**
   * Lets see if we already ignore this permission
   */
  public static boolean isIgnoredPermission(String permission) {
    if (permission == null) {
      return false;
    }
    if (getIgnoredPermissions().contains(permission)) {
      return true;
    }
    return false;
  }

  /**
   * Use to ignore to particular Permission - even if user will deny or add it we won't receive a
   * callback.
   *
   * @param permission Permission to ignore
   */
  public static void ignorePermission(String permission) {
    if (!isIgnoredPermission(permission)) {
      ArrayList<String> ignoredPermissions = getIgnoredPermissions();
      ignoredPermissions.add(permission);
      Set<String> set = new HashSet<String>();
      set.addAll(ignoredPermissions);
      sharedPreferences.edit().putStringSet(KEY_IGNORED_PERMISSIONS, set).apply();
    }
  }

  /**
   * Used to trigger comparing process - @permissionListener will be called each time Permission was
   * revoked, or added (but only once).
   *
   * @param permissionListener Callback that handles all permission changes
   */
  public static void permissionCompare(PermissionListener permissionListener) {
    if (context == null) {
      throw new RuntimeException(
          "Before comparing permissions you need to call Nammu.init(context)");
    }
    ArrayList<String> previouslyGranted = getPreviousPermissions();
    ArrayList<String> currentPermissions = getGrantedPermissions();
    ArrayList<String> ignoredPermissions = getIgnoredPermissions();
    for (String permission : ignoredPermissions) {
      if (previouslyGranted != null && !previouslyGranted.isEmpty()) {
        if (previouslyGranted.contains(permission)) {
          previouslyGranted.remove(permission);
        }
      }

      if (currentPermissions != null && !currentPermissions.isEmpty()) {
        if (currentPermissions.contains(permission)) {
          currentPermissions.remove(permission);
        }
      }
    }
    for (String permission : currentPermissions) {
      if (previouslyGranted.contains(permission)) {
        //All is fine, was granted and still is
        previouslyGranted.remove(permission);
      } else {
        //We didn't have it last time
        if (permissionListener != null) {
          permissionListener.permissionsChanged(permission);
          permissionListener.permissionsGranted(permission);
        }
      }
    }
    if (previouslyGranted != null && !previouslyGranted.isEmpty()) {
      //Something was granted and removed
      for (String permission : previouslyGranted) {
        if (permissionListener != null) {
          permissionListener.permissionsChanged(permission);
          permissionListener.permissionsRemoved(permission);
        }
      }
    }
    refreshMonitoredList();
  }

  /**
   * Not that needed method but if we override others it is good to keep same.
   */
  public static boolean checkPermission(String permissionName) {
    if (context == null) {
      throw new RuntimeException(
          "Before comparing permissions you need to call Nammu.init(context)");
    }
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context,
        permissionName);
  }

  /**
   * Check for special permission.
   *
   * @param permissionName: can be one of SYSTEM_ALERT_WINDOW or WRITE_SETTINGS
   * @return permission status
   */
  public static boolean checkSpecialPermission(String permissionName) {
    if (context == null) {
      throw new RuntimeException(
          "Before comparing permissions you need to call PermissionManager.init(context)");
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      Log.e("Nammu",
          "Special permission cannot be checked as Android version is below Android 6.0");
      return false;
    }
    switch (permissionName) {
      case Manifest.permission.SYSTEM_ALERT_WINDOW:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          return Settings.canDrawOverlays(context);
        }
        break;
      case Manifest.permission.WRITE_SETTINGS:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          return Settings.System.canWrite(context);
        }
        break;
      default:
        return false;
    }
    return false;
  }
}
