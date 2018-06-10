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

package pl.tajchert.nammusample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import pl.tajchert.nammu.PermissionListener;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int REQUEST_CODE_CONTACTS = 123;
  private static final int REQUEST_CODE_LOCATION = 124;
  private static final int REQUEST_CODE_BOTH = 125;

  @BindView(R.id.main_layout) View mLayout;

  /**
   * Used to handle result of askForPermission for Contacts Permission, in better way than
   * onRequestPermissionsResult() and handling with big switch statement
   */
  final PermissionCallback permissionContactsCallback = new PermissionCallback() {
    @Override public void permissionGranted() {
      boolean hasAccess = Tools.accessContacts(MainActivity.this);
      Toast.makeText(MainActivity.this, "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
    }

    @Override public void permissionRefused() {
      boolean hasAccess = Tools.accessContacts(MainActivity.this);
      Toast.makeText(MainActivity.this, "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
    }
  };

  /**
   * Used to handle result of askForPermission for Location, in better way than
   * onRequestPermissionsResult() and handling with big switch statement
   */
  final PermissionCallback permissionLocationCallback = new PermissionCallback() {
    @Override public void permissionGranted() {
      boolean hasAccess = Tools.accessLocation(MainActivity.this);
      Toast.makeText(MainActivity.this, "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
    }

    @Override public void permissionRefused() {
      boolean hasAccess = Tools.accessLocation(MainActivity.this);
      Toast.makeText(MainActivity.this, "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    Nammu.init(getApplicationContext());
  }

  @Override protected void onResume() {
    super.onResume();
    Nammu.permissionCompare(new PermissionListener() {
      @Override public void permissionsChanged(String permissionRevoke) {
        //Toast is not needed as always either permissionsGranted() or permissionsRemoved() will be called
        //Toast.makeText(MainActivity.this, "Access revoked = " + permissionRevoke, Toast.LENGTH_SHORT).show();
      }

      @Override public void permissionsGranted(String permissionGranted) {
        Toast.makeText(MainActivity.this, "Access granted = " + permissionGranted,
            Toast.LENGTH_SHORT).show();
      }

      @Override public void permissionsRemoved(String permissionRemoved) {
        Toast.makeText(MainActivity.this, "Access removed = " + permissionRemoved,
            Toast.LENGTH_SHORT).show();
      }
    });
  }

  @OnClick(R.id.buttonSpecialChangeSettings) public void clickButtChangeSettings() {
    if (Nammu.checkSpecialPermission(Manifest.permission.WRITE_SETTINGS)) {
      Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show();
    } else {
      Nammu.askForSpecialPermission(this, Manifest.permission.WRITE_SETTINGS,
          new PermissionCallback() {
            @Override public void permissionGranted() {
              Toast.makeText(MainActivity.this, "Access granted", Toast.LENGTH_SHORT).show();
            }

            @Override public void permissionRefused() {
              Log.d(TAG, "permissionRefused: ");
              Toast.makeText(MainActivity.this, "Access refused", Toast.LENGTH_SHORT).show();
            }
          });
    }
  }

  @OnClick(R.id.buttonContacts) public void clickButtContacts() {
    //Lets see if we can access Contacts
    if (Nammu.checkPermission(Manifest.permission.READ_CONTACTS)) {
      //We have a permission, easy peasy
      boolean hasAccess = Tools.accessContacts(this);
      Toast.makeText(this, "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
    } else {
      //We do not own this permission
      if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(mLayout, "Here we explain user why we need to know his/her contacts.",
            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
          @Override public void onClick(View view) {
            Nammu.askForPermission(MainActivity.this, Manifest.permission.READ_CONTACTS,
                permissionContactsCallback);
          }
        }).show();
      } else {
        //First time asking for permission
        // or phone doesn't offer permission
        // or user marked "never ask again"
        Nammu.askForPermission(this, Manifest.permission.READ_CONTACTS, permissionContactsCallback);
      }
    }
  }

  @OnClick(R.id.buttonLocation) public void clickButtLocation() {
    if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
      boolean hasAccess = Tools.accessLocation(this);
      Toast.makeText(this, "Access granted fine= " + hasAccess, Toast.LENGTH_SHORT).show();
    } else {
      if (Nammu.shouldShowRequestPermissionRationale(this,
          Manifest.permission.ACCESS_FINE_LOCATION)) {
        //User already refused to give us this permission or removed it
        //Now he/she can mark "never ask again" (sic!)
        Snackbar.make(mLayout, "Here we explain user why we need to know his/her location.",
            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
          @Override public void onClick(View view) {
            Nammu.askForPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
                permissionLocationCallback);
          }
        }).show();
      } else {
        //First time asking for permission
        // or phone doesn't offer permission
        // or user marked "never ask again"
        Nammu.askForPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION,
            permissionLocationCallback);
      }
    }
  }

  @OnClick(R.id.buttonStartFragmentActivity) public void clickStartActivityWithFragment() {
    Intent activity = new Intent(this, FragmentActivity.class);
    startActivity(activity);
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}