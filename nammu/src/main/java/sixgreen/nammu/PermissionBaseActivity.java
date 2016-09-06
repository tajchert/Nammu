package sixgreen.nammu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Arrays;

import pl.tajchert.nammu.BuildConfig;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import sixgreen.nammu.PermissionCallbackBuilder.PermissionCallbackHost;

public abstract class PermissionBaseActivity extends AppCompatActivity implements
																	   OnPermissionRequestCallback,
																	   PermissionCallbackHost {
	
	private static final String TAG = "PermissionBaseActivity";

	private boolean debugPermissions = false;

	public boolean isDebugPermissions() {
		return BuildConfig.DEBUG && debugPermissions;
	}

	public void setDebugPermissions(boolean debugPermissions) {
		this.debugPermissions = debugPermissions;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (isDebugPermissions()) {
			Log.d(TAG,
					"##### ACTIVITY onRequestPermissionsResult : requestCode=" + requestCode +
					", permissions=" +
					Arrays.toString(permissions) + ", grantResults=" +
					Arrays.toString(grantResults));
		}
		Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public boolean checkHasPermissions(String... permissions) {
		if (isDebugPermissions()) {
			Log.d(TAG,
					"##### ACTIVITY Checking if the application has the requestioned permissions:");
			Log.d(TAG,
					"##### ACTIVITY \t" + Arrays.toString(permissions));
		}
		if (needsPermissionCheck()) {
			final boolean haspermissions = Nammu.hasPermission(this, permissions);
			Log.d(TAG,
					"##### ACTIVITY The application has the permissions: " + haspermissions);
			return haspermissions;
		} else if (isDebugPermissions()) {
			Log.d(TAG,
					"##### ACTIVITY Permission check not required for this version of Android: " +
					VERSION.SDK_INT);
		}
		return true;
	}


	Activity getPermissionsCheckActivityHandle() {
		return this;
	}


	@Override
	public void doRequestPermissions(int rationalResId, int rationalTitalResId, final PermissionCallback callback, final String... permissions) {
		if (isDebugPermissions()) {
			Log.d(TAG,
					"##### ACTIVITY Requesting permissions:");
			Log.d(TAG,
					"##### ACTIVITY \t" + Arrays.toString(permissions));
		}
		if (Nammu.shouldShowRequestPermissionRationale(this, permissions)) {

			if (isDebugPermissions()) {
				Log.d(TAG,
						"##### ACTIVITY The Android system is requesting we show our rational to the user.");
			}

			final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(rationalTitalResId);
			dialogBuilder
					.setMessage(rationalResId);
			dialogBuilder
					.setPositiveButton(this
							.getString(android.R.string.ok), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							if (isDebugPermissions()) {
								Log.d(TAG,
										"##### ACTIVITY The user has accepted the rational, and we will now request the permission.");
							}

							// Request the permission
							dialog.dismiss();
							Nammu.askForPermission(getPermissionsCheckActivityHandle(), permissions, callback);
						}
					});
			dialogBuilder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (isDebugPermissions()) {
						Log.d(TAG,
								"##### ACTIVITY The user has rejected the rational, and we will not request the permissions.");
					}
					dialog.dismiss();
				}
			});
			dialogBuilder.show();

		} else {
			if (isDebugPermissions()) {
				Log.d(TAG,
						"##### ACTIVITY Asking the user to grant the permissions.");
			}
			Nammu.askForPermission(getPermissionsCheckActivityHandle(), permissions, callback);
		}
	}

	public boolean needsPermissionCheck() {
		// Versions of android after 23 will require special permissions checks.
		return VERSION.SDK_INT >= 23;
	}

	@Override
	@NonNull
	public PermissionCallback getPermissionCallback(int grantedResId, int deniedResId, int settingsButtonResId, final String packageId, @Nullable final PermissionCallback callback) {
		return PermissionCallbackBuilder
				.getPermissionCallback(this, grantedResId, deniedResId, settingsButtonResId, packageId, callback);
	}
}
