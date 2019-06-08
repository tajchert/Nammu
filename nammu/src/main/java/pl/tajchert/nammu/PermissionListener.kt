package pl.tajchert.nammu

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
interface PermissionListener {
  /**
   * Gets called each time we run Nammu.permissionCompare() and some Permission is revoke/granted to us
   * @param permissionChanged
   */
  fun permissionsChanged(permissionChanged: String)

  /**
   * Gets called each time we run Nammu.permissionCompare() and some Permission is granted
   * @param permissionGranted
   */
  fun permissionsGranted(permissionGranted: String)

  /**
   * Gets called each time we run Nammu.permissionCompare() and some Permission is removed
   * @param permissionRemoved
   */
  fun permissionsRemoved(permissionRemoved: String)
}
