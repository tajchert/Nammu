package pl.tajchert.runtimepermissionhelper;

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
public interface PermissionCallback {
    public void permissionGranted();
    public void permissionNotAssigned();
}
