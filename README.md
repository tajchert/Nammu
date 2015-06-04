Nammu - Runtime Permission Helper
=======

Nammu is a simple library to speed up working with new Runtime Permissions introduced in Android M. It allows you to easily ask for a permission, as well as to monitor if all permission that you haved ask are still there, if not it will callback with name of permission that was revoke by user.

###What are Runtime Permissions?
Google docs is [here](https://developer.android.com/preview/features/runtime-permissions.html).
TLDR: like old-loved permissions that were ask during intallation but this time they are more dynamic (should be ask only when they are needed) and can be revoked by user at any time.

<img src="image/screenshot.png" width="400" height="672" alt="Source of all evil"/>

###Why should I care?
Beacause your user can revoke most essential part of your app and quite probably there will be a lot of app crashes.
Current solution you can see here - [Google sample](https://github.com/googlesamples/android-RuntimePermissions) basically there is a lot that happens with Activity that is used to check and grant permissions. Also permissions rights are checked many times in the code.

###Easy asking for permissions
It removes a bit of boiler plate to keep request id, and thus simplify your code inside Activity class.
call `Nammu.askForPermission(Activity, PermissionString , PermissionCallback)` which offers a nice callback with either succes or fail method. To use this only thing you need to add is in your Activity that you are using.
```java
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```

###Monitor permissions
To keep track of access to particular permissions, all you need is init Nammu `Nammu.init(Context);` (can be in Application class onCreate or Activity) and add permissions to monitor such `Nammu.savePermission(PermissionString);` so for example `Nammu.savePermission(Manifest.permission.CAMERA);`.
After that each time you want to make sure if permissions are still there - `Nammu.permissionCompare(Activity, PermissionListener);` and PermissionListener will be called if some Permission was removed.

###Extras
As library monitors your Permissions at any point of time (just call `init()`) you can get list of monitored Persmissions -`Nammu.getPrevPermissions()`, removed some `Nammu.removePermission(PermissionString)`, or check if is monitored `Nammu.containsPermission(PermissionString)`.

###How to import it?
As for now it is NOT hosted at Maven/JCenter etc. as it is based on preview build of Android M which SDK is not available on those platforms. Till that time just copy/paste all classes from library module or even whole module.
