/* *********************************************************************
 *                                                                     *
 *                          ContactNickname.kt                         *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * ContactWebsite is a Kotlin class designed to store a nickname.
 *
 * Note that while a person can only have one 'regular' name there
 * can be any number of nicknames (which are not considered part of
 * the regular personal name).
 *
 * The stored information consists of
 *   .) the actual nickname address (e.g. "Mr. Incredible")
 *   .) the type of nickname (usually TYPE_DEFAULT)
 *   .) a label containing additional type information if the
 *      standardized type is TYPE_CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for nicknames:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Nickname
 * Among the supported data fields are:
 *     Nickname.NAME    (= ContactsContract.DataColumns.DATA1)
 *     Nickname.TYPE    (= ContactsContract.DataColumns.DATA2)
 *     Nickname.LABEL   (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *
 * Note: The 'type' field in the Nickname is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the Nickname.TYPE field.
 * Available values are:
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_CUSTOM      (= 0)
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT     (= 1)
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_OTHER_NAME  (= 2)
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_MAIDEN_NAME (= 3)
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_SHORT_NAME  (= 4)
 *     ContactsContract.CommonDataKinds.Nickname.TYPE_INITIALS    (= 5)
 *
 * This structure of nicknames is also reflected in the vCard 4.0 standard.
 *     See: https://www.rfc-editor.org/rfc/rfc6350#section-6.2.3
 *     Section 6.2.3 - vCard Item "NICKNAME"
 *        To specify the text corresponding to the nickname
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class ContactNickname(var name: String, var type: Int, var label: String) {
    fun deepCopy(): ContactNickname = ContactNickname(name, type, label)
} // data class ContactNickname

/* *********************************************************************
 *                          ContactNickname.kt                         *
 ***********************************************************************/
