package pl.tajchert.nammusample;

import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.LocationManager;
import android.provider.ContactsContract;

/**
 * Created by Tajchert on 21.08.2015.
 */
public class Tools {
    public static boolean accessContacts(Context context) {
        try {
            Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                cursor.close();
            }
        } catch (SecurityException e) {
            //No android.permission-group.CONTACTS
            return false;
        }
        return true;
    }

    public static boolean accessLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (bestProvider == null) {
            //No android.permission-group.LOCATION
            return false;
        }
        return true;
    }
}
