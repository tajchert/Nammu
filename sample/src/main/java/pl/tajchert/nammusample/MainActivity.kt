/*
* Copyright 2015 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package pl.tajchert.nammusample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.buttonContacts
import kotlinx.android.synthetic.main.activity_main.buttonLocation
import kotlinx.android.synthetic.main.activity_main.buttonStartFragmentActivity
import kotlinx.android.synthetic.main.activity_main.mainLayout
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import pl.tajchert.nammu.PermissionListener

class MainActivity : AppCompatActivity() {

  /**
   * Used to handle result of askForPermission for Contacts Permission, in better way than
   * onRequestPermissionsResult() and handling with big switch statement
   */
  private val permissionContactsCallback: PermissionCallback = object : PermissionCallback {
    override fun permissionGranted() {
      val hasAccess = Tools.accessContacts(this@MainActivity)
      Toast.makeText(this@MainActivity, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }

    override fun permissionRefused() {
      val hasAccess = Tools.accessContacts(this@MainActivity)
      Toast.makeText(this@MainActivity, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }
  }

  /**
   * Used to handle result of askForPermission for Location, in better way than
   * onRequestPermissionsResult() and handling with big switch statement
   */
  private val permissionLocationCallback: PermissionCallback = object : PermissionCallback {
    override fun permissionGranted() {
      val hasAccess = Tools.accessLocation(this@MainActivity)
      Toast.makeText(this@MainActivity, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }

    override fun permissionRefused() {
      val hasAccess = Tools.accessLocation(this@MainActivity)
      Toast.makeText(this@MainActivity, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Nammu.init(applicationContext)

    buttonLocation.setOnClickListener(onClickListenerLocationButton)
    buttonContacts.setOnClickListener(onClickListenerContactsButton)
    buttonStartFragmentActivity.setOnClickListener(onClickListenerStartFragmentButton)
  }

  override fun onResume() {
    super.onResume()
    Nammu.permissionCompare(object : PermissionListener {
      override fun permissionsChanged(permissionChanged: String) {
        //Toast is not needed as always either permissionsGranted() or permissionsRemoved() will be called
        //Toast.makeText(MainActivity.this, "Access revoked = " + permissionRevoke, Toast.LENGTH_SHORT).show();
      }

      override fun permissionsGranted(permissionGranted: String) {
        Toast.makeText(
            this@MainActivity, "Access granted = $permissionGranted",
            Toast.LENGTH_SHORT
        )
            .show()
      }

      override fun permissionsRemoved(permissionRemoved: String) {
        Toast.makeText(
            this@MainActivity, "Access removed = $permissionRemoved",
            Toast.LENGTH_SHORT
        )
            .show()
      }
    })
  }

  private val onClickListenerContactsButton = View.OnClickListener {
    //Lets see if we can access Contacts
    if (Nammu.checkPermission(Manifest.permission.READ_CONTACTS)) {
      //We have a permission, easy peasy
      val hasAccess = Tools.accessContacts(this)
      Toast.makeText(this, "Access granted = $hasAccess", Toast.LENGTH_SHORT)
          .show()
    } else {
      //We do not own this permission
      if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(
            mainLayout!!, "Here we explain user why we need to know his/her contacts.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") {
              Nammu.askForPermission(
                  this@MainActivity, Manifest.permission.READ_CONTACTS,
                  permissionContactsCallback
              )
            }
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
      val hasAccess = Tools.accessLocation(this@MainActivity)
      Toast.makeText(this@MainActivity, "Access granted fine= $hasAccess", Toast.LENGTH_SHORT)
          .show()
    } else {
      if (Nammu.shouldShowRequestPermissionRationale(
              this@MainActivity,
              Manifest.permission.ACCESS_FINE_LOCATION
          )
      ) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(
            mainLayout!!, "Here we explain user why we need to know his/her location.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") {
              Nammu.askForPermission(
                  this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION,
                  permissionLocationCallback
              )
            }
            .show()
      } else {
        //First time asking for permission
        // or phone doesn't offer permission
        // or user marked "never ask again"
        Nammu.askForPermission(
            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION,
            permissionLocationCallback
        )
      }
    }
  }

  private val onClickListenerStartFragmentButton = View.OnClickListener {
    val activity = Intent(this, FragmentActivity::class.java)
    startActivity(activity)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_CODE_CONTACTS = 123
    private val REQUEST_CODE_LOCATION = 124
    private val REQUEST_CODE_BOTH = 125
  }
}