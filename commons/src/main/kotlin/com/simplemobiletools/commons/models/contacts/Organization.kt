/* *********************************************************************
 *                                                                     *
 *                           Organization.kt                           *
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * Copyright 2023 Simple Mobile Tools
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * Organization is a Kotlin class designed to store information about
 * a organisation and the position of a person within that organisation.
 *
 * The stored information consists of
 *   .) the name of the company/organisation
 *   .) the name of the department
 *   .) the job title of the person
 *   .) the job description of the person
 *   .) the location of the persons office (e.g. postal address or office number)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for organisations:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Organization
 * Among the supported data fields are:
 *     Organization.COMPANY         (= ContactsContract.DataColumns.DATA1)
 *     Organization.TYPE            (= ContactsContract.DataColumns.DATA2)
 *     Organization.LABEL           (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *     Organization.TITLE           (= ContactsContract.DataColumns.DATA4)
 *     Organization.DEPARTMENT      (= ContactsContract.DataColumns.DATA5)
 *     Organization.JOB_DESCRIPTION (= ContactsContract.DataColumns.DATA6)
 *     Organization.SYMBOL          (= ContactsContract.DataColumns.DATA7)
 *     Organization.PHONETIC_NAME   (= ContactsContract.DataColumns.DATA8)
 *     Organization.OFFICE_LOCATION (= ContactsContract.DataColumns.DATA9)
 *
 * For the purpose of out contact management, we shall only support the
 * fields for company (vCard:ORG or Organization.COMPANY) and jobPosition
 * (vCard:TITLE or Organization.TITLE), however if other fields are set
 * by some contact management software we shall retain the values of these
 * fields. Thus the Organization class shall contain data elements for
 * (almost all) of the other data fields...
 *
 * Note: The 'type' field in the Organisation is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the Organization.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM (= 0)
 *    ContactsContract.CommonDataKinds.Organization.TYPE_WORK   (= 1)
 *    ContactsContract.CommonDataKinds.Organization.TYPE_OTHER  (= 2)
 *    (requires: import android.provider.ContactsContract)
 *
 * This structure of organisations and positions with them is also reflected
 *   in the vCard 4.0 standard. See: https://www.rfc-editor.org/rfc/rfc6350#section-6.6
 *     Section 6.6 - vCard Organizational Properties
 *        These properties are concerned with information associated
 *        with characteristics of the organization or organizational
 *        units of the object that the vCard represents.
 *   Among the supported data fields are:
 *      6.6.1 - TITLE (e.g. Research Scientist)
 *      6.6.2 - ROLE (e.g. Project Leader)
 *      6.6.3 - LOGO (e.g. URI to an image or direct image data)
 *      6.6.4 - ORG (e.g. ABC\, Inc.;North American Division;Marketing)
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

import com.simplemobiletools.commons.helpers.DEFAULT_ORGANIZATION_TYPE
import ezvcard.parameter.RelatedType

data class Organization(var company: String, var jobTitle: String,
                        var department: String, var jobDescription: String,
                        var symbol: String, var phoneticName: String,
                        var location: String, var type: Int, var label: String) {
    fun deepCopy(): Organization = Organization(company, jobTitle,
                        department, jobDescription,
                        symbol, phoneticName, location, type, label)
    companion object {
        fun getEmptyOrganization() = Organization("", "", "", "",
            "", "", "", DEFAULT_ORGANIZATION_TYPE, "")
        // val EMPTY_ORGANIZATION = Organization("", "", "", "", "", "", "", DEFAULT_ORGANIZATION_TYPE, "")
    }

    fun isEmpty() = company.isEmpty() && jobTitle.isEmpty()
    fun isNotEmpty() = !isEmpty()
} // data class Organization


/* Legacy definition of Organisation:
 *
 * Before April 2023, only the company and job position values of an
 * Organisation were stored as part of a LocalContact. While we are now using
 * the modern Organisation format, that supports all the fields of Android
 * ContactContract we still need to be able to read the old format, when a
 * user updates from an old version of SimpleContacts.
 *
 * See: models/contacts/LocalContact.tk
 *      helpers/Converters.tk
 */
data class OrganizationConverter(var company: String, var jobTitle: String) {
    fun isEmpty() = company.isEmpty() && jobTitle.isEmpty()
    fun isNotEmpty() = !isEmpty()
}
