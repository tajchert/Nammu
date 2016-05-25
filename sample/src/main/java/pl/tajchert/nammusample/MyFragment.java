package pl.tajchert.nammusample;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import pl.tajchert.nammu.PermissionListener;


public class MyFragment extends Fragment {

    /**
     * Used to handle result of askForPermission for Contacts Permission, in better way than onRequestPermissionsResult() and handling with big switch statement
     */
    final PermissionCallback permissionContactsCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            boolean hasAccess = Tools.accessContacts(getContext());
            Toast.makeText(getContext(), "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void permissionRefused() {
            boolean hasAccess = Tools.accessContacts(getContext());
            Toast.makeText(getContext(), "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Used to handle result of askForPermission for Location, in better way than onRequestPermissionsResult() and handling with big switch statement
     */
    final PermissionCallback permissionLocationCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            boolean hasAccess = Tools.accessLocation(getContext());
            Toast.makeText(getContext(), "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void permissionRefused() {
            boolean hasAccess = Tools.accessLocation(getContext());
            Toast.makeText(getContext(), "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
        }
    };


    public MyFragment() {
        // Required empty public constructor
    }

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Nammu.permissionCompare(new PermissionListener() {
            @Override
            public void permissionsChanged(String permissionRevoke) {
                //Toast is not needed as always either permissionsGranted() or permissionsRemoved() will be called
                //Toast.makeText(MainActivity.this, "Access revoked = " + permissionRevoke, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void permissionsGranted(String permissionGranted) {
                Toast.makeText(getContext(), "Access granted = " + permissionGranted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void permissionsRemoved(String permissionRemoved) {
                Toast.makeText(getContext(), "Access removed = " + permissionRemoved, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @OnClick(R.id.buttonContacts)
    public void clickButtContacts() {
        //Lets see if we can access Contacts
        if(Nammu.checkPermission(Manifest.permission.READ_CONTACTS)) {
            //We have a permission, easy peasy
            boolean hasAccess = Tools.accessContacts(getContext());
            Toast.makeText(getContext(), "Access granted = " + hasAccess, Toast.LENGTH_SHORT).show();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                //User already refused to give us this permission or removed it
                //Now he/she can mark "never ask again" (sic!)
                Snackbar.make(getView(), "Here we explain user why we need to know his/her contacts.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(MyFragment.this, Manifest.permission.READ_CONTACTS, permissionContactsCallback);
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

    @OnClick(R.id.buttonLocation)
    public void clickButtLocation() {
        if(Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            boolean hasAccess = Tools.accessLocation(getContext());
            Toast.makeText(getContext(), "Access granted fine= " + hasAccess, Toast.LENGTH_SHORT).show();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(MyFragment.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //User already refused to give us this permission or removed it
                //Now he/she can mark "never ask again" (sic!)
                Snackbar.make(getView(), "Here we explain user why we need to know his/her location.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(MyFragment.this, Manifest.permission.ACCESS_FINE_LOCATION, permissionLocationCallback);
                            }
                        }).show();
            } else {
                //First time asking for permission
                // or phone doesn't offer permission
                // or user marked "never ask again"
                Nammu.askForPermission(MyFragment.this, Manifest.permission.ACCESS_FINE_LOCATION, permissionLocationCallback);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
