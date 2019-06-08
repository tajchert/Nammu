package pl.tajchert.nammusample

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.view.buttonContacts
import kotlinx.android.synthetic.main.activity_main.view.buttonLocation
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import pl.tajchert.nammu.PermissionListener

class MyFragment : Fragment() {

  /**
   * Used to handle result of askForPermission for Contacts Permission, in better way than onRequestPermissionsResult() and handling with big switch statement
   */
  internal val permissionContactsCallback: PermissionCallback = object : PermissionCallback {
    override fun permissionGranted() {
      val hasAccess = Tools.accessContacts(context!!)
      Toast.makeText(context, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }

    override fun permissionRefused() {
      val hasAccess = Tools.accessContacts(context!!)
      Toast.makeText(context, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }
  }

  /**
   * Used to handle result of askForPermission for Location, in better way than onRequestPermissionsResult() and handling with big switch statement
   */
  internal val permissionLocationCallback: PermissionCallback = object : PermissionCallback {
    override fun permissionGranted() {
      val hasAccess = Tools.accessLocation(context!!)
      Toast.makeText(context, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }

    override fun permissionRefused() {
      val hasAccess = Tools.accessLocation(context!!)
      Toast.makeText(context, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.fragment_my, container, false)
    view.buttonContacts.setOnClickListener(onClickListenerContactsButton)
    view.buttonLocation.setOnClickListener(onClickListenerLocationButton)
    return view
  }

  override fun onResume() {
    super.onResume()
    Nammu.permissionCompare(object : PermissionListener {
      override fun permissionsChanged(permissionRevoke: String) {
        //Toast is not needed as always either permissionsGranted() or permissionsRemoved() will be called
        //Toast.makeText(MainActivity.this, "Access revoked = " + permissionRevoke, Toast.LENGTH_SHORT).show();
      }

      override fun permissionsGranted(permissionGranted: String) {
        Toast.makeText(context, "Access granted = $permissionGranted", Toast.LENGTH_SHORT)
            .show()
      }

      override fun permissionsRemoved(permissionRemoved: String) {
        Toast.makeText(context, "Access removed = $permissionRemoved", Toast.LENGTH_SHORT)
            .show()
      }
    })
  }

  private val onClickListenerContactsButton = View.OnClickListener {
    //Lets see if we can access Contacts
    if (Nammu.checkPermission(Manifest.permission.READ_CONTACTS)) {
      //We have a permission, easy peasy
      val hasAccess = Tools.accessContacts(context!!)
      Toast.makeText(context, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    } else {
      //We do not own this permission
      if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(
            view!!, "Here we explain user why we need to know his/her contacts.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") { Nammu.askForPermission(this@MyFragment, Manifest.permission.READ_CONTACTS, permissionContactsCallback) }
            .show()
      } else {
        //First time asking for permission
        // or phone doesn't offer permission
        // or user marked "never ask again"
        Nammu.askForPermission(this, Manifest.permission.READ_CONTACTS, permissionContactsCallback)
      }
    }
  }

  private val onClickListenerLocationButton = View.OnClickListener {
    if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
      val hasAccess = Tools.accessLocation(context!!)
      Toast.makeText(context, "Access granted fine= $hasAccess", Toast.LENGTH_SHORT)
          .show()
    } else {
      if (Nammu.shouldShowRequestPermissionRationale(this@MyFragment, Manifest.permission.ACCESS_FINE_LOCATION)) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(
            view!!, "Here we explain user why we need to know his/her location.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") { Nammu.askForPermission(this@MyFragment, Manifest.permission.ACCESS_FINE_LOCATION, permissionLocationCallback) }
            .show()
      } else {
        //First time asking for permission
        // or phone doesn't offer permission
        // or user marked "never ask again"
        Nammu.askForPermission(this@MyFragment, Manifest.permission.ACCESS_FINE_LOCATION, permissionLocationCallback)
      }
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  companion object {

    fun newInstance(): MyFragment {
      return MyFragment()
    }
  }

}// Required empty public constructor
