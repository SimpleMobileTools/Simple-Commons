package com.simplemobiletools.commons.helpers

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.PhoneLookup
import android.text.TextUtils
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.SimpleContact

class ContactsHelper(val context: Context) {
    fun getAvailableContacts(callback: (ArrayList<SimpleContact>) -> Unit) {
        ensureBackgroundThread {
            val names = getContactNames()
            var allContacts = getContactPhoneNumbers()
            allContacts.forEach {
                val contactId = it.id
                val contact = names.firstOrNull { it.id == contactId }
                val name = contact?.name
                if (name != null) {
                    it.name = name
                }

                val photoUri = contact?.photoUri
                if (photoUri != null) {
                    it.photoUri = photoUri
                }
            }

            allContacts = allContacts.filter { it.name.isNotEmpty() }.distinctBy {
                val startIndex = Math.max(0, it.phoneNumber.length - 9)
                it.phoneNumber.substring(startIndex)
            }.toMutableList() as ArrayList<SimpleContact>

            allContacts.sort()
            callback(allContacts)
        }
    }

    private fun getContactNames(): List<SimpleContact> {
        val contacts = ArrayList<SimpleContact>()
        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            StructuredName.PREFIX,
            StructuredName.GIVEN_NAME,
            StructuredName.MIDDLE_NAME,
            StructuredName.FAMILY_NAME,
            StructuredName.SUFFIX,
            StructuredName.PHOTO_THUMBNAIL_URI,
            Organization.COMPANY,
            Organization.TITLE,
            ContactsContract.Data.MIMETYPE
        )

        val selection = "${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(
            StructuredName.CONTENT_ITEM_TYPE,
            Organization.CONTENT_ITEM_TYPE
        )

        context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
            val id = cursor.getIntValue(ContactsContract.Data.CONTACT_ID)
            val mimetype = cursor.getStringValue(ContactsContract.Data.MIMETYPE)
            val photoUri = cursor.getStringValue(StructuredName.PHOTO_THUMBNAIL_URI) ?: ""
            val isPerson = mimetype == StructuredName.CONTENT_ITEM_TYPE
            if (isPerson) {
                val prefix = cursor.getStringValue(StructuredName.PREFIX) ?: ""
                val firstName = cursor.getStringValue(StructuredName.GIVEN_NAME) ?: ""
                val middleName = cursor.getStringValue(StructuredName.MIDDLE_NAME) ?: ""
                val familyName = cursor.getStringValue(StructuredName.FAMILY_NAME) ?: ""
                val suffix = cursor.getStringValue(StructuredName.SUFFIX) ?: ""
                if (firstName.isNotEmpty() || middleName.isNotEmpty() || familyName.isNotEmpty()) {
                    val names = arrayOf(prefix, firstName, middleName, familyName, suffix).filter { it.isNotEmpty() }
                    val fullName = TextUtils.join(" ", names)
                    val contact = SimpleContact(id, fullName, photoUri, "")
                    contacts.add(contact)
                }
            }

            val isOrganization = mimetype == Organization.CONTENT_ITEM_TYPE
            if (isOrganization) {
                val company = cursor.getStringValue(Organization.COMPANY) ?: ""
                val jobTitle = cursor.getStringValue(Organization.TITLE) ?: ""
                if (company.isNotEmpty() || jobTitle.isNotEmpty()) {
                    val fullName = "$company $jobTitle".trim()
                    val contact = SimpleContact(id, fullName, photoUri, "")
                    contacts.add(contact)
                }
            }
        }
        return contacts
    }

    private fun getContactPhoneNumbers(): ArrayList<SimpleContact> {
        val contacts = ArrayList<SimpleContact>()
        val uri = CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            CommonDataKinds.Phone.NORMALIZED_NUMBER
        )

        context.queryCursor(uri, projection) { cursor ->
            val id = cursor.getIntValue(ContactsContract.Data.CONTACT_ID)
            val phoneNumber = cursor.getStringValue(CommonDataKinds.Phone.NORMALIZED_NUMBER)
            if (phoneNumber != null) {
                val contact = SimpleContact(id, "", "", phoneNumber)
                contacts.add(contact)
            }
        }
        return contacts
    }


    fun getNameFromPhoneNumber(number: String): String {
        if (!context.hasPermission(PERMISSION_READ_CONTACTS)) {
            return number
        }

        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(
            PhoneLookup.DISPLAY_NAME
        )

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    return cursor.getStringValue(PhoneLookup.DISPLAY_NAME)
                }
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
        }

        return number
    }

    fun getPhotoUriFromPhoneNumber(number: String): String {
        if (!context.hasPermission(PERMISSION_READ_CONTACTS)) {
            return ""
        }

        val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(
            PhoneLookup.PHOTO_URI
        )

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    return cursor.getStringValue(PhoneLookup.PHOTO_URI) ?: ""
                }
            }
        } catch (e: Exception) {
            context.showErrorToast(e)
        }

        return ""
    }
}
