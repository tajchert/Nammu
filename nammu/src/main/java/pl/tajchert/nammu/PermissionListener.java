package pl.tajchert.nammu;

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
public interface PermissionListener {
    /**
     * Gets called each time we run Nammu.permissionCompare() and some Permission is revoke from us
     * @param permissionRevoke
     */
    public void permissionsChanged(String permissionRevoke);
}
