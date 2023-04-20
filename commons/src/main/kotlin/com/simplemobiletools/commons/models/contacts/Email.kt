/* *********************************************************************
 *                                                                     *
 *                               EMail.kt                              *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * Email is a Kotlin class designed to store information about an email
 * address.
 *
 * The stored information consists of
 *   .) the actual email address (e.g. john.doe@example.com)
 *   .) the type of email address (e.g. WORK, HOME, OTHER)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * has a similar structure for emails:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Email
 * Among the supported data fields are:
 *     Email.ADDRESS      (= ContactsContract.DataColumns.DATA1)
 *     Email.TYPE         (= ContactsContract.DataColumns.DATA2)
 *     Email.LABEL        (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *
 * Note: The 'type' field in the EMail is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the EMail.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM (= 0)
 *    ContactsContract.CommonDataKinds.Email.TYPE_HOME   (= 1)
 *    ContactsContract.CommonDataKinds.Email.TYPE_WORK   (= 2)
 *    ContactsContract.CommonDataKinds.Email.TYPE_OTHER  (= 3)
 *    ContactsContract.CommonDataKinds.Email.TYPE_MOBILE (= 4)
 *    (requires: import android.provider.ContactsContract)
 *
 * The structure of emails is also reflected in the vCard 4.0 standard.
 *     See: https://www.rfc-editor.org/rfc/rfc6350#section-6.4.2
 *     Section 6.4.2 - vCard Item "EMAIL"
 *        To specify the electronic mail address.
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class Email(var address: String, var type: Int, var label: String) {
    fun deepCopy(): Email = Email(address, type, label)
} // data class Email/*

/* Legacy definition of Email:
 *
 * Before April 2023, the address field was given the generic name of
 * 'value' and stored as such as part of a LocalContact. While we are
 * now using the modern email format, that supports all the fields of Android
 * ContactContract we still need to be able to read the old format, when
 * a user updates from an old version of SimpleContacts.
 *
 * See: models/contacts/LocalContact.tk
 *      helpers/Converters.tk
 */
data class EmailConverter(var value: String, var type: Int, var label: String)

/* *********************************************************************
 *                               EMail.kt                              *
 ***********************************************************************/
