package sixgreen.nammu;

import pl.tajchert.nammu.PermissionCallback;

/**
 * Created by bpappin on 16-04-16.
 */
public interface OnPermissionRequestCallback {
	boolean checkHasPermissions(String... permissions);
	void doRequestPermissions(int rationalResId, int rationalTitalResId, PermissionCallback callback, String... permissions);
}
