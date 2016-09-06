package sixgreen.nammu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.BuildConfig;
import android.support.v4.app.Fragment;

import pl.tajchert.nammu.PermissionCallback;
import sixgreen.nammu.PermissionCallbackBuilder.PermissionCallbackHost;

/**
 * Created by bpappin on 2015-10-20.
 */
public class PermissionBaseFragment extends Fragment implements PermissionCallbackHost {
	
	
	private static final String TAG = "PermissionBaseFragment";

	protected OnPermissionRequestCallback permissionsCallback;
	private boolean debugPermissions = false;

	public boolean isDebugPermissions() {
		return BuildConfig.DEBUG && debugPermissions;
	}

	public void setDebugPermissions(boolean debugPermissions) {
		this.debugPermissions = debugPermissions;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			permissionsCallback = (OnPermissionRequestCallback) context;
		} catch (ClassCastException e) {
			throw new ClassCastException((context != null ? context.getClass().getName() : null)
										 + " must implement OnPermissionRequestCallback");
		}
	}

	@Override
	@NonNull
	public PermissionCallback getPermissionCallback(int grantedResId, int deniedResId, int settingsButtonResId, final String packageId, @Nullable final PermissionCallback callback) {
		return PermissionCallbackBuilder
				.getPermissionCallback(getActivity(), grantedResId, deniedResId, settingsButtonResId, packageId, callback);
	}
}
