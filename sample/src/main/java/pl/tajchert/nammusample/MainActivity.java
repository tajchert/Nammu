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
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewAnimator;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import pl.tajchert.nammu.PermissionListener;
import pl.tajchert.nammusample.camera.CameraPreviewFragment;
import pl.tajchert.nammusample.contacts.ContactsFragment;

/**
 * Launcher Activity that demonstrates the use of runtime permissions for Android M.
 * It contains a summary sample description, sample log and a Fragment that calls callbacks on this
 * Activity to illustrate parts of the runtime permissions API.
 * <p>
 * This Activity requests permissions to access the camera ({@link Manifest.permission#CAMERA})
 * when the 'Show Camera' button is clicked to display the camera preview.
 * Contacts permissions (({@link Manifest.permission#READ_CONTACTS} and ({@link
 * Manifest.permission#WRITE_CONTACTS})) are requested when the 'Show and Add Contacts'
 * button is
 * clicked to display the first contact in the contacts database and to add a dummy contact
 * directly
 * to it. First, permissions are checked if they have already been granted through {@link
 * Activity#checkSelfPermission(String)} (wrapped in {@link
 * PermissionUtil#hasSelfPermission(Activity, String)} and {@link PermissionUtil#hasSelfPermission(Activity,
 * String[])} for compatibility). If permissions have not been granted, they are requested through
 * {@link Activity#requestPermissions(String[], int)} and the return value checked in {@link
 * Activity#onRequestPermissionsResult(int, String[], int[])}.
 * <p>
 * If this sample is executed on a device running a platform version below M, all permissions
 * declared
 * in the Android manifest file are always granted at install time and cannot be requested at run
 * time.
 * <p>
 * This sample targets the M platform and must therefore request permissions at runtime. Change the
 * targetSdk in the file 'Application/build.gradle' to 22 to run the application in compatibility
 * mode.
 * Now, if a permission has been disable by the system through the application settings, disabled
 * APIs provide compatibility data.
 * For example the camera cannot be opened or an empty list of contacts is returned. No special
 * action is required in this case.
 * <p>
 * (This class is based on the MainActivity used in the SimpleFragment sample template.)
 */
public class MainActivity extends FragmentActivity implements PermissionListener {
    public static final String TAG = "MainActivity";
    /**
     * Permissions required to read and write contacts. Used by the {@link ContactsFragment}.
     */
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};

    // Whether the Log Fragment is currently shown.
    private boolean mLogShown;


    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showCamera(View view) {
        Log.i(TAG, "Show camera button pressed. Checking permission.");
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        Nammu.askForPermission(this, Manifest.permission.CAMERA, new PermissionCallback() {
            @Override
            public void permissionGranted() {
                showCameraPreview();
                Nammu.savePermission(Manifest.permission.CAMERA);
                Toast.makeText(MainActivity.this, R.string.permision_available_camera, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void permissionRefused() {
                Toast.makeText(MainActivity.this, R.string.permissions_not_granted, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showContacts(View v) {
        Log.i(TAG, "Show contacts button pressed. Checking permissions.");
        // Verify that all required contact permissions have been granted.
        Nammu.askForPermission(this, PERMISSIONS_CONTACT, new PermissionCallback() {
            @Override
            public void permissionGranted() {
                showContactDetails();
                Nammu.savePermission(PERMISSIONS_CONTACT);
                Toast.makeText(MainActivity.this, R.string.permision_available_contacts, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void permissionRefused() {
                Toast.makeText(MainActivity.this, R.string.permissions_not_granted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display the {@link CameraPreviewFragment} in the content area if the required Camera
     * permission has been granted.
     */
    private void showCameraPreview() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sample_content_fragment, CameraPreviewFragment.newInstance())
                .addToBackStack("contacts")
                .commitAllowingStateLoss();
    }

    /**
     * Display the {@link ContactsFragment} in the content area if the required contacts
     * permissions
     * have been granted.
     */
    private void showContactDetails() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sample_content_fragment, ContactsFragment.newInstance())
                .addToBackStack("contacts")
                .commitAllowingStateLoss();
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Note: Methods and definitions below are only used to provide the UI for this sample and are
    not relevant for the execution of the runtime permissions API. */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onBackClick(View view) {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //We can init here or onCreate in Application class
        Nammu.init(MainActivity.this);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            RuntimePermissionsFragment fragment = new RuntimePermissionsFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Nammu.permissionCompare(this, this);//We need to call this in Activity each time we want to check if any permission wasn't revoke
    }

    /**
     * Called each time user revokes some permission that we have saved earlier on using PermissionHelper.savePermission()
     * Called as a result of Nammu.permissionCompare()
     * @param permissionRevoke
     */
    @Override
    public void permissionsChanged(String permissionRevoke) {
        Log.d(TAG, "permissions revoked by user :" + permissionRevoke);
        Toast.makeText(MainActivity.this, "Revoked: " + permissionRevoke, Toast.LENGTH_SHORT).show();
    }
}
