/* *********************************************************************
 *                                                                     *
 *                               Event.kt                              *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * Event is a Kotlin class designed to store information about
 * an events such as birthdays, anniversaries and other events
 *
 * The stored information consists of
 *   .) a date that may consist of a full date (year/month/day) or
 *      drop the year to specify just month/day.
 *   .) the type of event (e.g. BIRTHDAY, ANNIVERSARY, OTHER)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for events:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Event
 * Among the supported data fields are:
 *     Event.START_DATE   (= ContactsContract.DataColumns.DATA1)
 *     Event.TYPE         (= ContactsContract.DataColumns.DATA2)
 *     Event.LABEL        (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *
 * Note: The 'type' field in the Event is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the Event.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM      (= 0)
 *    ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY (= 1)
 *    ContactsContract.CommonDataKinds.Event.TYPE_OTHER       (= 2)
 *    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY    (= 3)
 *    (requires: import android.provider.ContactsContract)
 *
 * This structure of events is also reflected in the vCard 4.0 standard.
 *     See: https://www.rfc-editor.org/rfc/rfc6350#section-6.2.5
 *     Section 6.2.5 - vCard Item "BDAY"
 *        To specify the birth date of the object the vCard represents.
 *     Section 6.2.6 - vCard Item "ANNIVERSARY"
 *        The date of marriage, or equivalent, of the object the
 *        vCard represents.
 *     Note: There are two individual vCard Items for BDAY and ANNIVERSARY.
 *     There is no general concept of "event" and thus there is no way
 *     to deal with "other" events or to store the type of event that
 *     has its anniversary on the specified date (need not be date of
 *     marriage, might be date of engagement or first meeting, name day
 *     (saint's day), foundation of company or some other event of
 *     significance!!)
 *     Additionally vCard can also only handle a single BDAY and
 *     ANNIVERSARY for each contact.
 *
 * Note: Due to the limitations of vCard, the "label" field might not
 * strictly be necessary, however we shall still support it. Even
 * if _we_ currently don't place any (non-empty) values there, we
 * shall not destroy any values placed there by other applications.
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class Event(var startDate: String, var type: Int, var label: String) {
    fun deepCopy(): Event = Event(startDate, type, label)
} // class Event

/* Legacy definition of Event:
 *
 * Before April 2023, only the start date and type values of an Event
 * were stored as part of a LocalContact. While we are now using the
 * modern Event format, that supports all the fields of Android ContactContract
 * we still need to be able to read the old format, when a user updates
 * from an old version of SimpleContacts.
 *
 * See: models/contacts/LocalContact.tk
 *      helpers/Converters.tk
 */
data class EventConverter(var value: String, var type: Int)

/* *********************************************************************
 *                               Event.kt                              *
 ***********************************************************************/

