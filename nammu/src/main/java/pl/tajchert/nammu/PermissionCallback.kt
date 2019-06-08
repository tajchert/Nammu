package pl.tajchert.nammu

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
interface PermissionCallback {
  fun permissionGranted()
  fun permissionRefused()
}
