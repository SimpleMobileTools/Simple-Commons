/* *********************************************************************
 *                                                                     *
 *                          ContactWebsite.kt                          *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * ContactWebsite is a Kotlin class designed to store information about
 * a website.
 *
 * The stored information consists of
 *   .) the URL (Uniform Resource Locator) of the website
 *   .) standardized type information (e.g. HOME, WORK, OTHER)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for websites:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Website
 * Among the supported data fields are:
 *     Website.URL          (= ContactsContract.DataColumns.DATA1)
 *     Website.TYPE         (= ContactsContract.DataColumns.DATA2)
 *     Website.LABEL        (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *
 * Note: The 'type' field in the Website is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the Website.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM   (= 0)
 *    ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE (= 1)
 *    ContactsContract.CommonDataKinds.Website.TYPE_BLOG     (= 2)
 *    ContactsContract.CommonDataKinds.Website.TYPE_PROFILE  (= 3)
 *    ContactsContract.CommonDataKinds.Website.TYPE_HOME     (= 4)
 *    ContactsContract.CommonDataKinds.Website.TYPE_WORK     (= 5)
 *    ContactsContract.CommonDataKinds.Website.TYPE_FTP      (= 6)
 *    ContactsContract.CommonDataKinds.Website.TYPE_OTHER    (= 7)
 *    (requires: import android.provider.ContactsContract)
 *
 * The structure of websites (and other URLs) is also reflected in the
 *   vCard 4.0 standard. See: https://www.rfc-editor.org/rfc/rfc6350#section-6.7.8
 *     Section 6.7.8 - vCard Item "URL"
 *        To specify a uniform resource locator associated with the
 *        object to which the vCard refers.  Examples for individuals
 *        include personal web sites, blogs, and social networking site
 *        identifiers.
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class ContactWebsite(var URL: String, var type: Int, var label: String) : Cloneable {
    public override fun clone(): ContactWebsite = super.clone() as ContactWebsite
    fun deepCopy(): ContactWebsite = ContactWebsite(URL, type, label)
} // data class ContactWebsite

/* *********************************************************************
 *                          ContactWebsite.kt                          *
 ***********************************************************************/
