package pl.tajchert.nammusample

import android.content.Context
import android.database.Cursor
import android.location.Criteria
import android.location.LocationManager
import android.provider.ContactsContract

/**
 * Created by Tajchert on 21.08.2015.
 */
object Tools {
  fun accessContacts(context: Context): Boolean {
    try {
      val cursor = context.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
      cursor?.close()
    } catch (e: SecurityException) {
      //No android.permission-group.CONTACTS
      return false
    }

    return true
  }

  fun accessLocation(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val criteria = Criteria()
    criteria.powerRequirement = Criteria.POWER_LOW
    val bestProvider = locationManager.getBestProvider(criteria, false)
        ?: //No android.permission-group.LOCATION
        return false
    return true
  }
}
