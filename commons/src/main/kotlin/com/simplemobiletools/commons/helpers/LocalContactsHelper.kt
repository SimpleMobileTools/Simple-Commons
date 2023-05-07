package com.simplemobiletools.commons.helpers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.MediaStore
import com.simplemobiletools.commons.extensions.contactsDB
import com.simplemobiletools.commons.extensions.getByteArray
import com.simplemobiletools.commons.extensions.getEmptyContact
import com.simplemobiletools.commons.models.SimpleContact
import com.simplemobiletools.commons.models.contacts.Contact
import com.simplemobiletools.commons.models.contacts.*
import com.simplemobiletools.commons.models.contacts.LocalContact

class LocalContactsHelper(val context: Context) {
    fun getAllContacts(favoritesOnly: Boolean = false): ArrayList<Contact> {
        val contacts = if (favoritesOnly) context.contactsDB.getFavoriteContacts() else context.contactsDB.getContacts()
        contacts.forEach {
            if (it.jobPosition != "") {
                it.organization.jobTitle = it.jobPosition
                it.jobPosition = ""
            }
        }
        val storedGroups = ContactsHelper(context).getStoredGroupsSync()
        return (contacts.map { convertLocalContactToContact(it, storedGroups) }.toMutableList() as? ArrayList<Contact>) ?: arrayListOf()
    }

    fun getContactWithId(id: Int): Contact? {
        val storedGroups = ContactsHelper(context).getStoredGroupsSync()
        val contact = context.contactsDB.getContactWithId(id)
        if ((contact != null) && (contact.jobPosition != "")) {
            contact.organization.jobTitle = contact.jobPosition
            contact.jobPosition = ""
        }
        return convertLocalContactToContact(contact, storedGroups)
    }

    fun insertOrUpdateContact(contact: Contact): Boolean {
        val localContact = convertContactToLocalContact(contact)
        return context.contactsDB.insertOrUpdate(localContact) > 0
    }

    fun addContactsToGroup(contacts: ArrayList<Contact>, groupId: Long) {
        contacts.forEach {
            val localContact = convertContactToLocalContact(it)
            val newGroups = localContact.groups
            newGroups.add(groupId)
            newGroups.distinct()
            localContact.groups = newGroups
            context.contactsDB.insertOrUpdate(localContact)
        }
    }

    fun removeContactsFromGroup(contacts: ArrayList<Contact>, groupId: Long) {
        contacts.forEach {
            val localContact = convertContactToLocalContact(it)
            val newGroups = localContact.groups
            newGroups.remove(groupId)
            localContact.groups = newGroups
            context.contactsDB.insertOrUpdate(localContact)
        }
    }

    fun deleteContactIds(ids: MutableList<Long>) {
        ids.chunked(30).forEach {
            context.contactsDB.deleteContactIds(it)
        }
    }

    fun toggleFavorites(ids: Array<Int>, addToFavorites: Boolean) {
        val isStarred = if (addToFavorites) 1 else 0
        ids.forEach {
            context.contactsDB.updateStarred(isStarred, it)
        }
    }

    fun updateRingtone(id: Int, ringtone: String) {
        context.contactsDB.updateRingtone(ringtone, id)
    }

    private fun getPhotoByteArray(uri: String): ByteArray {
        if (uri.isEmpty()) {
            return ByteArray(0)
        }

        val photoUri = Uri.parse(uri)
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)

        val fullSizePhotoData = bitmap.getByteArray()
        bitmap.recycle()

        return fullSizePhotoData
    }

    private fun convertLocalContactToContact(localContact: LocalContact?, storedGroups: ArrayList<Group>): Contact? {
        if (localContact == null) {
            return null
        }

        val contactPhoto = if (localContact.photo == null) {
            null
        } else {
            try {
                BitmapFactory.decodeByteArray(localContact.photo, 0, localContact.photo!!.size)
            } catch (e: OutOfMemoryError) {
                null
            }
        }

        return context.getEmptyContact().apply {
            id = localContact.id!!
            name = ContactName(localContact.displayName,
                    localContact.prefix, localContact.firstName, localContact.middleName, localContact.surname, localContact.suffix,
                    localContact.phoneticGivenName, localContact.phoneticMiddleName, localContact.phoneticFamilyName)
            nicknames = localContact.nicknames
            phoneNumbers = localContact.phoneNumbers
            emails = localContact.emails
            addresses = localContact.addresses
            IMs = localContact.IMs
            events = localContact.events
            notes = localContact.notes
            organization = localContact.organization
            websites = localContact.websites
            relations = localContact.relations
            groups = storedGroups.filter { localContact.groups.contains(it.id) } as ArrayList<Group>
            thumbnailUri = ""
            photo = contactPhoto
            photoUri = localContact.photoUri
            starred = localContact.starred
            ringtone = localContact.ringtone
            contactId = localContact.id!!
            source = SMT_PRIVATE
            mimetype = DEFAULT_MIMETYPE
        }
    }

    private fun convertContactToLocalContact(contact: Contact): LocalContact {
        val photoByteArray = if (contact.photoUri.isNotEmpty()) {
            getPhotoByteArray(contact.photoUri)
        } else {
            contact.photo?.getByteArray()
        }

        return getEmptyLocalContact().apply {
            id = if (contact.id <= FIRST_CONTACT_ID) null else contact.id
            displayName = contact.name.formattedName
            prefix = contact.name.prefix
            firstName = contact.name.givenName
            middleName = contact.name.middleName
            surname = contact.name.familyName
            suffix = contact.name.suffix
            nicknames = contact.nicknames
            phoneNumbers = contact.phoneNumbers
            emails = contact.emails
            addresses = contact.addresses
            IMs = contact.IMs
            events = contact.events
            notes = contact.notes
            organization = contact.organization
            jobPosition = ""  // Obsolete
            websites = contact.websites
            relations = contact.relations
            groups = contact.groups.map { it.id }.toMutableList() as ArrayList<Long>
            photo = photoByteArray
            photoUri = contact.photoUri
            starred = contact.starred
            ringtone = contact.ringtone
        }
    }

    fun getPrivateSimpleContactsSync(favoritesOnly: Boolean, withPhoneNumbersOnly: Boolean) = getAllContacts(favoritesOnly).mapNotNull {
        convertContactToSimpleContact(it, withPhoneNumbersOnly)
    }
    companion object{
        fun convertContactToSimpleContact(contact: Contact?, withPhoneNumbersOnly: Boolean): SimpleContact? {
            return if ((contact == null) || (withPhoneNumbersOnly && contact.phoneNumbers.isEmpty())) {
                null
            } else {
                val birthdays = contact.events.filter { it.type == Event.TYPE_BIRTHDAY }.map { it.startDate }.toMutableList() as ArrayList<String>
                val anniversaries = contact.events.filter { it.type == Event.TYPE_ANNIVERSARY }.map { it.startDate }.toMutableList() as ArrayList<String>
                SimpleContact(contact.id, contact.id, contact.getNameToDisplay(), contact.photoUri, contact.phoneNumbers, birthdays, anniversaries)
            }
        }
    }
}
