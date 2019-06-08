package pl.tajchert.nammu

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
object Nammu {
  private val TAG = Nammu::class.java.simpleName
  private var context: WeakReference<Context>? = null
  private var sharedPreferences: SharedPreferences? = null
  private val KEY_PREV_PERMISSIONS = "previous_permissions"
  private val KEY_IGNORED_PERMISSIONS = "ignored_permissions"
  private val permissionRequests = ArrayList<PermissionRequest>()
  val SYSTEM_ALERT_WINDOW_PERMISSION_REQ_CODE = 1971
  val WRITE_SETTINGS_PERMISSION_REQ_CODE = 1970

  //Permission monitoring part below

  /**
   * Get list of currently granted permissions, without saving it inside Nammu
   *
   * @return currently granted permissions
   */
  //Group location
  //Group Calendar
  //Group Camera
  //Group Contacts
  //Group Microphone
  //Group Phone
  //Group Body sensors
  //Group SMS
  //Group Storage
  val grantedPermissions: ArrayList<String>
    get() {
      if (context == null || context!!.get() == null) {
        throw RuntimeException("Must call init() earlier")
      }
      val permissions = ArrayList<String>()
      val permissionsGranted = ArrayList<String>()
      permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
      permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
      permissions.add(Manifest.permission.WRITE_CALENDAR)
      permissions.add(Manifest.permission.READ_CALENDAR)
      permissions.add(Manifest.permission.CAMERA)
      permissions.add(Manifest.permission.WRITE_CONTACTS)
      permissions.add(Manifest.permission.READ_CONTACTS)
      permissions.add(Manifest.permission.GET_ACCOUNTS)
      permissions.add(Manifest.permission.RECORD_AUDIO)
      permissions.add(Manifest.permission.CALL_PHONE)
      permissions.add(Manifest.permission.READ_PHONE_STATE)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        permissions.add(Manifest.permission.READ_CALL_LOG)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        permissions.add(Manifest.permission.WRITE_CALL_LOG)
      }
      permissions.add(Manifest.permission.ADD_VOICEMAIL)
      permissions.add(Manifest.permission.USE_SIP)
      permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        permissions.add(Manifest.permission.BODY_SENSORS)
      }
      permissions.add(Manifest.permission.SEND_SMS)
      permissions.add(Manifest.permission.READ_SMS)
      permissions.add(Manifest.permission.RECEIVE_SMS)
      permissions.add(Manifest.permission.RECEIVE_WAP_PUSH)
      permissions.add(Manifest.permission.RECEIVE_MMS)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
      }
      permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(context!!.get()!!, permission) == PackageManager.PERMISSION_GRANTED) {
          permissionsGranted.add(permission)
        }
      }
      return permissionsGranted
    }

  /**
   * Get list of previous Permissions, from last refreshMonitoredList() call and they may be
   * outdated,
   * use getGrantedPermissions() to get current
   */
  val previousPermissions: ArrayList<String>
    get() {
      val prevPermissions = ArrayList<String>()
      prevPermissions.addAll(
          sharedPreferences!!.getStringSet(KEY_PREV_PERMISSIONS, HashSet())!!
      )
      return prevPermissions
    }

  val ignoredPermissions: ArrayList<String>
    get() {
      val ignoredPermissions = ArrayList<String>()
      ignoredPermissions.addAll(
          sharedPreferences!!.getStringSet(KEY_IGNORED_PERMISSIONS, HashSet())!!
      )
      return ignoredPermissions
    }

  fun init(context: Context) {
    sharedPreferences = context.getSharedPreferences("pl.tajchert.runtimepermissionhelper", Context.MODE_PRIVATE)
    Nammu.context = WeakReference(context)
  }

  /**
   * Check that all given permissions have been granted by verifying that each entry in the
   * given array is of the value [PackageManager.PERMISSION_GRANTED].
   */
  fun verifyPermissions(grantResults: IntArray): Boolean {
    for (result in grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false
      }
    }
    return true
  }

  /**
   * Returns true if the Activity has access to given permissions.
   */
  fun hasPermission(
    activity: Activity,
    permission: String
  ): Boolean {
    return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
  }

  /**
   * Returns true if the Activity has access to a all given permission.
   */
  fun hasPermission(
    activity: Activity?,
    permissions: Array<String>
  ): Boolean {
    for (permission in permissions) {
      if (ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
        return false
      }
    }
    return true
  }

  /*
   * If we override other methods, lets do it as well, and keep name same as it is already weird enough.
   * Returns true if we should show explanation why we need this permission.
   */
  fun shouldShowRequestPermissionRationale(
    activity: Activity,
    permissions: String
  ): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions)
  }

  fun shouldShowRequestPermissionRationale(
    fragment: Fragment,
    permissions: String
  ): Boolean {
    return fragment.shouldShowRequestPermissionRationale(permissions)
  }

  fun shouldShowRequestPermissionRationale(
    fragment: android.app.Fragment,
    permissions: String
  ): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      fragment.shouldShowRequestPermissionRationale(permissions)
    } else false
  }

  fun askForPermission(
    activity: Activity,
    permission: String,
    permissionCallback: PermissionCallback
  ) {
    askForPermission(activity, arrayOf(permission), permissionCallback)
  }

  fun askForPermission(
    activity: Activity,
    permissions: Array<String>,
    permissionCallback: PermissionCallback?
  ) {
    if (permissionCallback == null) {
      return
    }
    if (hasPermission(activity, permissions)) {
      permissionCallback.permissionGranted()
      return
    }
    val permissionRequest = PermissionRequest(
        ArrayList(Arrays.asList(*permissions)),
        permissionCallback
    )
    permissionRequests.add(permissionRequest)

    ActivityCompat.requestPermissions(activity, permissions, permissionRequest.requestCode)
  }

  fun askForPermission(
    fragment: Fragment,
    permission: String,
    permissionCallback: PermissionCallback
  ) {
    askForPermission(fragment, arrayOf(permission), permissionCallback)
  }

  fun askForPermission(
    fragment: android.app.Fragment,
    permission: String,
    permissionCallback: PermissionCallback
  ) {
    askForPermission(fragment, arrayOf(permission), permissionCallback)
  }

  fun askForPermission(
    fragment: Fragment,
    permissions: Array<String>,
    permissionCallback: PermissionCallback?
  ) {
    if (permissionCallback == null) {
      return
    }
    if (hasPermission(fragment.activity, permissions)) {
      permissionCallback.permissionGranted()
      return
    }
    val permissionRequest = PermissionRequest(
        ArrayList(Arrays.asList(*permissions)),
        permissionCallback
    )
    permissionRequests.add(permissionRequest)

    fragment.requestPermissions(permissions, permissionRequest.requestCode)
  }

  fun askForPermission(
    fragment: android.app.Fragment,
    permissions: Array<String>,
    permissionCallback: PermissionCallback?
  ) {
    if (permissionCallback == null) {
      return
    }
    if (hasPermission(fragment.activity, permissions)) {
      permissionCallback.permissionGranted()
      return
    }
    val permissionRequest = PermissionRequest(
        ArrayList(Arrays.asList(*permissions)),
        permissionCallback
    )
    permissionRequests.add(permissionRequest)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      fragment.requestPermissions(permissions, permissionRequest.requestCode)
    }
  }

  fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    val requestResult = PermissionRequest(requestCode)
    if (permissionRequests.contains(requestResult)) {
      val permissionRequest = permissionRequests[permissionRequests.indexOf(requestResult)]
      if (verifyPermissions(grantResults)) {
        //Permission has been granted
        permissionRequest.permissionCallback!!.permissionGranted()
      } else {
        permissionRequest.permissionCallback!!.permissionRefused()
      }
      permissionRequests.remove(requestResult)
    }
    refreshMonitoredList()
  }

  /**
   * Refresh currently granted permission list, and save it for later comparing using
   * @permissionCompare()
   */
  fun refreshMonitoredList() {
    val permissions = grantedPermissions
    val set = HashSet(permissions)
    sharedPreferences!!.edit()
        .putStringSet(KEY_PREV_PERMISSIONS, set)
        .apply()
  }

  /**
   * Lets see if we already ignore this permission
   */
  fun isIgnoredPermission(permission: String?): Boolean {
    if (permission == null) {
      return false
    }
    return if (ignoredPermissions.contains(permission)) {
      true
    } else false
  }

  /**
   * Use to ignore to particular Permission - even if user will deny or add it we won't receive a
   * callback.
   *
   * @param permission Permission to ignore
   */
  fun ignorePermission(permission: String) {
    if (!isIgnoredPermission(permission)) {
      val ignoredPermissions = ignoredPermissions
      ignoredPermissions.add(permission)
      val set = HashSet<String>()
      set.addAll(ignoredPermissions)
      sharedPreferences!!.edit()
          .putStringSet(KEY_IGNORED_PERMISSIONS, set)
          .apply()
    }
  }

  /**
   * Used to trigger comparing process - @permissionListener will be called each time Permission was
   * revoked, or added (but only once).
   *
   * @param permissionListener Callback that handles all permission changes
   */
  fun permissionCompare(permissionListener: PermissionListener?) {
    if (context == null) {
      throw RuntimeException(
          "Before comparing permissions you need to call Nammu.init(context)"
      )
    }
    val previouslyGranted = previousPermissions
    val currentPermissions = grantedPermissions
    val ignoredPermissions = ignoredPermissions
    for (permission in ignoredPermissions) {
      if (previouslyGranted.isNotEmpty()) {
        if (previouslyGranted.contains(permission)) {
          previouslyGranted.remove(permission)
        }
      }

      if (currentPermissions.isNotEmpty()) {
        if (currentPermissions.contains(permission)) {
          currentPermissions.remove(permission)
        }
      }
    }
    for (permission in currentPermissions) {
      if (previouslyGranted.contains(permission)) {
        //All is fine, was granted and still is
        previouslyGranted.remove(permission)
      } else {
        //We didn't have it last time
        if (permissionListener != null) {
          permissionListener.permissionsChanged(permission)
          permissionListener.permissionsGranted(permission)
        }
      }
    }
    if (previouslyGranted.isNotEmpty()) {
      //Something was granted and removed
      for (permission in previouslyGranted) {
        if (permissionListener != null) {
          permissionListener.permissionsChanged(permission)
          permissionListener.permissionsRemoved(permission)
        }
      }
    }
    refreshMonitoredList()
  }

  /**
   * Not that needed method but if we override others it is good to keep same.
   */
  fun checkPermission(permissionName: String): Boolean {
    if (context == null) {
      throw RuntimeException(
          "Before comparing permissions you need to call Nammu.init(context)"
      )
    }
    return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        context!!.get()!!,
        permissionName
    )
  }

  /**
   * Check for special permission.
   *
   * @param permissionName: can be one of SYSTEM_ALERT_WINDOW or WRITE_SETTINGS
   * @return permission status
   */
  fun checkSpecialPermission(permissionName: String): Boolean {
    if (context == null) {
      throw RuntimeException(
          "Before comparing permissions you need to call PermissionManager.init(context)"
      )
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      Log.e(
          "Nammu",
          "Special permission cannot be checked as Android version is below Android 6.0"
      )
      return false
    }
    when (permissionName) {
      Manifest.permission.SYSTEM_ALERT_WINDOW -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.canDrawOverlays(context!!.get()!!)
      }
      Manifest.permission.WRITE_SETTINGS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return Settings.System.canWrite(context!!.get()!!)
      }
      else -> return false
    }
    return false
  }
}
