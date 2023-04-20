/* *********************************************************************
 *                                                                     *
 *                            PhoneNumber.kt                           *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * ContactPhoneNumber is a Kotlin class designed to store information about
 * an phone or fax number
 *
 * The stored information consists of
 *   .) the actual phone number (e.g. +15551234567)
 *   .) the type of phone number (e.g. WORK, HOME, CUSTOM)
 *      and the technology of the device (e.g. VOICE, FAX, TEXT, PAGER)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for phone numbers:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Phone
 * Among the supported data fields are:
 *     Phone.NUMBER  (= ContactsContract.DataColumns.DATA1)
 *     Phone.TYPE    (= ContactsContract.DataColumns.DATA2)
 *     Phone.LABEL   (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 * Note: The 'type' field in the Phone number is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the Phone.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM       (= 0)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_HOME         (= 1)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE       (= 2)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_WORK         (= 3)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK     (= 4)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME     (= 5)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_PAGER        (= 6)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER        (= 7)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK     (= 8)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_CAR          (= 9)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN (= 10)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_ISDN         (= 11)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_MAIN         (= 12)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX    (= 13)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_RADIO        (= 14)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_TELEX        (= 15)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD      (= 16)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE  (= 17)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER   (= 18)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT    (= 19)
 *    ContactsContract.CommonDataKinds.Phone.TYPE_MMS          (= 20)
 *
 * This structure of phone numbers is also reflected in the vCard 4.0 standard.
 *     See: https://www.rfc-editor.org/rfc/rfc6350#section-6.4.1
 *     Section 6.4.1 - vCard Item "TEL"
 *        To specify the telephone number for telephony communication.
 *     Available types of phone numbers in vCard 4.0 are:
 *        voice, fax, text, cell, video, pager, textphone
 *     These can be combined with a location like "home" or "work" to
 *     form combinations like TYPE="voice,home" or TYPE="work,fax"
 *
 **********************************************************************/

package com.simplemobiletools.commons.models

// FIXME - "value" should be renamed to "number" to match Android ContactContract.Phone
data class PhoneNumber(var value: String, var type: Int, var label: String,
                              var normalizedNumber: String, var isPrimary: Boolean = false) {
    fun deepCopy(): PhoneNumber = PhoneNumber(value, type, label, normalizedNumber, isPrimary)
} // data class PhoneNumber

/* *********************************************************************
 *                            PhoneNumber.kt                           *
 ***********************************************************************/

