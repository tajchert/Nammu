package pl.tajchert.nammu

import java.util.ArrayList
import java.util.Random

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
class PermissionRequest {
  private val permissions: ArrayList<String> = arrayListOf()
  var requestCode: Int = 0
    private set
  var permissionCallback: PermissionCallback? = null

  constructor(requestCode: Int) {
    this.requestCode = requestCode
  }

  constructor(
    permissions: ArrayList<String>,
    permissionCallback: PermissionCallback
  ) {
    this.permissions.clear()
    this.permissions.addAll(permissions)
    this.permissionCallback = permissionCallback
    if (random == null) {
      random = Random()
    }
    this.requestCode = random!!.nextInt(255)
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) {
      return false
    }
    return if (other is PermissionRequest) {
      other.requestCode == this.requestCode
    } else false
  }

  override fun hashCode(): Int {
    return requestCode
  }

  companion object {
    private var random: Random? = null
  }
}
