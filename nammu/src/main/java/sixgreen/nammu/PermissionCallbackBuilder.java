package sixgreen.nammu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import pl.tajchert.nammu.PermissionCallback;

/**
 * Created by bpappin on 2015-10-21.
 */
public class PermissionCallbackBuilder {
	
	private static String TAG = "PermissionCallbackBldr";

	public static interface PermissionCallbackHost {
		public PermissionCallback getPermissionCallback(int grantedResId, int deniedResId, int settingsButtonResId, final String packageId, @Nullable final PermissionCallback callback);
	}

	/**
	 * USed to build a callback object to automate some of the permission handling code.
	 *
	 * @param context
	 * 		the context we are running in.
	 * @param grantedResId
	 * 		The Toast message to show when the permissions was granted.
	 * @param deniedResId
	 * 		the Dialog message to display when the permission was denied. This should explain that the
	 * 		features will not work properly.
	 * @param settingsButtonResId
	 * 		The label for the settings button. This button, when clicked, will take the user to the
	 * 		system settings for the application, this would typically be the string "Settings".
	 * @param packageId
	 * 		the pacakge id of the application settings to launch if the user wants to inspect them.
	 * 		Can
	 * 		use BuildConfig.APPLICATION_ID.
	 * @param callback
	 * 		a callback the application can use to receive notification of the permission request. If
	 * 		its not needed, it may be set to null.
	 * @return
	 */
	@NonNull
	public static PermissionCallback getPermissionCallback(final Context context, final int grantedResId, final int deniedResId, final int settingsButtonResId, final String packageId, @Nullable final PermissionCallback callback) {
		return new PermissionCallback() {
			@Override
			public void permissionGranted() {
				Log.i(TAG, "Permissions GRANTED");

				Toast.makeText(context.getApplicationContext(), grantedResId, Toast.LENGTH_SHORT);

				if (callback != null) {
					callback.permissionGranted();
				}
			}

			@Override
			public void permissionRefused() {
				Log.i(TAG, "Permissions DENIED");

				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context
						.getApplicationContext());
				//dialogBuilder.setTitle(permissionRationalTitleResId);
				dialogBuilder
						.setMessage(deniedResId);
				dialogBuilder
						.setPositiveButton(settingsButtonResId, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startApplicationDetails(context
										.getApplicationContext(), packageId); // BuildConfig.APPLICATION_ID
							}
						});
				dialogBuilder.setNegativeButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogBuilder.show();

				if (callback != null) {
					callback.permissionRefused();
				}
			}
		};
	}


	public static final void startApplicationDetails(Context context, String packageId) {
		final int apiLevel = Build.VERSION.SDK_INT;
		Intent intent = new Intent();
		if (apiLevel >= 9) {
			//TODO get working on gb
			//Toast.makeText(SDMove.this, "Gingerbread Not Currently Supported", Toast.LENGTH_LONG).show();
			context.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
					Uri.parse("package:" + packageId)));
		} else {
			final String appPkgName = (apiLevel == 8 ? "pkg"
													 : "com.android.settings.ApplicationPkgName");
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, packageId);
			context.startActivity(intent);
		}
	}
}
