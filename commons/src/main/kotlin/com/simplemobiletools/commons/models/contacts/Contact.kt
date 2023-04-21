package com.simplemobiletools.commons.models.contacts

import android.graphics.Bitmap
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.simplemobiletools.commons.extensions.normalizePhoneNumber
import com.simplemobiletools.commons.extensions.normalizeString
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.PhoneNumber

data class Contact(
    var id: Int,
    var name: ContactName,
    var nicknames: ArrayList<ContactNickname> = ArrayList(),
    var phoneNumbers: ArrayList<PhoneNumber> = ArrayList(),
    var emails: ArrayList<Email> = ArrayList(),
    var addresses: ArrayList<Address> = ArrayList(),
    var IMs: ArrayList<IM> = ArrayList(),
    var events: ArrayList<Event> = ArrayList(),
    var notes: String= "",
    var organization: Organization = Organization.getEmptyOrganization(),
    var websites: ArrayList<ContactWebsite> = ArrayList(),
    var relations: ArrayList<ContactRelation> = ArrayList(),
    var groups: ArrayList<Group> = ArrayList(),
    var thumbnailUri: String= "",
    var photoUri: String= "",
    var photo: Bitmap? = null,
    var starred: Int = 0,
    var ringtone: String? = "",
    var contactId: Int,
    var source: String= "",
    var mimetype: String = ""
) : Comparable<Contact> {
    val rawId = id
    val displayname = getNameToDisplay(contactListShowFormattedName, contactListNameFormat)
    var birthdays = events.filter { it.type == ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY }.map { it.startDate }.toMutableList() as ArrayList<String>
    var anniversaries = events.filter { it.type == ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY }.map { it.startDate }.toMutableList() as ArrayList<String>

    companion object {
        private var sorting = 0
        private var startWithSurname = false

        // The following variables should actually be part of MainActivity or MyViewPagerFragment!
        private var contactListShowFormattedName: Boolean = false
        private var contactListNameFormat: ContactNameFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN
        private var contactListNameFieldOrder: String = "FG"
        private var contactListSortBy: ContactNameSortBy = ContactNameSortBy.NAMESORTBY_FAMILY_NAME
        private var contactListInverseSortOrder: Boolean = false

        fun setNameFormat(showFormattedName: Boolean, nameFormat: ContactNameFormat) {
            contactListShowFormattedName = showFormattedName
            contactListNameFormat = nameFormat
            startWithSurname =
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN) or
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_M) or
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE) or
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_MIDDLE_GIVEN) or
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX) or
                (nameFormat == ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX)

            contactListNameFieldOrder = when(nameFormat) {
                ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN         -> "FGDC"       // Family, Given
                ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_M       -> "FGMDC"      // Family, Given M.
                ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE  -> "FGMDC"      // Family, Given Middle
                ContactNameFormat.NAMEFORMAT_FAMILY_MIDDLE_GIVEN  -> "FMGDC"      // Family Middle Given - Chinese/Japanese!
                ContactNameFormat.NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX -> "FPGMSDC" // Family, Prefix Given Middle, Suffix
                ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX -> "FGMPSDC"  // Family Given Middle Prefix Suffix  -- Used for Sorting only!
                ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY         -> "GFDC"       // Given Family
                ContactNameFormat.NAMEFORMAT_GIVEN_M_FAMILY       -> "GMFDC"      // Given M. Family
                ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY  -> "GMFDC"      // Given Middle Family
                ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY_MIDDLE  -> "GFMDC"      // Given Family Middle
                ContactNameFormat.NAMEFORMAT_PREFIX_GIVEN_MIDDLE_FAMILY_SUFFIX -> "PGMFSDC" // Prefix Given Middle Family, Suffix
                ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX -> "GMFPSDC" // Given Middle Family Prefix Suffix -- Used for Sorting only!
                else -> { "FG" }
            }
        } // Contact.setNameFormat()

        fun setNameFormat(startNameWithSurname: Boolean) {
            if (startNameWithSurname)
                setNameFormat(false, ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN)
            else
                setNameFormat(false, ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY)
        } // Contact.setNameFormat()

        fun setSortOrder(sortBy: ContactNameSortBy, invertSort: Boolean) {
            contactListSortBy = sortBy
            contactListInverseSortOrder = invertSort
            sorting = when(sortBy) {
                ContactNameSortBy.NAMESORTBY_GIVEN_NAME -> SORT_BY_FIRST_NAME
                ContactNameSortBy.NAMESORTBY_MIDDLE_NAME -> SORT_BY_MIDDLE_NAME
                ContactNameSortBy.NAMESORTBY_FAMILY_NAME -> SORT_BY_SURNAME
                ContactNameSortBy.NAMESORTBY_FORMATTED_NAME -> SORT_BY_FULL_NAME
                ContactNameSortBy.NAMESORTBY_DISPLAY_NAME -> SORT_BY_FULL_NAME
                ContactNameSortBy.NAMESORTBY_CONTACT_ID -> SORT_BY_DATE_CREATED
            }
            if (invertSort) {
                sorting = sorting or SORT_BY_DATE_CREATED
            }
        } // Contact.setNameFormat()

        fun setSortOrder(sorting: Int) {
            contactListSortBy = when {
                ((sorting and SORT_BY_FIRST_NAME) != 0) -> ContactNameSortBy.NAMESORTBY_GIVEN_NAME
                ((sorting and SORT_BY_MIDDLE_NAME) != 0) -> ContactNameSortBy.NAMESORTBY_MIDDLE_NAME
                ((sorting and SORT_BY_SURNAME) != 0) -> ContactNameSortBy.NAMESORTBY_FAMILY_NAME
                ((sorting and SORT_BY_FULL_NAME) != 0) -> ContactNameSortBy.NAMESORTBY_FORMATTED_NAME
                ((sorting and SORT_BY_DATE_CREATED) != 0) -> ContactNameSortBy.NAMESORTBY_CONTACT_ID
                else -> ContactNameSortBy.NAMESORTBY_FAMILY_NAME
            }
            contactListInverseSortOrder = ((sorting and SORT_DESCENDING) != 0)
        } // Contact.setNameFormat()

        fun getEmptyContact() = Contact(0, ContactName.getEmptyName(), ArrayList(), ArrayList(),
            ArrayList(), ArrayList(), ArrayList(), ArrayList(), "",
            Organization.getEmptyOrganization(), ArrayList(), ArrayList(), ArrayList(),
            "", "", null, 0, null, 0, "", DEFAULT_MIMETYPE)

        // val EMPTY_CONTACT = Contact(0, EMPTY_NAME, ArrayList(), ArrayList(),
        //     ArrayList(), ArrayList(), ArrayList(), ArrayList(), "",
        //     EMPTY_ORGANISATION, ArrayList(), ArrayList(), ArrayList(),
        //     "", "", null, 0, null, 0, "", DEFAULT_MIMETYPE)
    } // companion object

    // *****************************************************************

    fun deepCopy(): Contact {
        val nicknamesCopy: ArrayList<ContactNickname> = ArrayList()
        nicknames.forEach { nicknamesCopy.add(ContactNickname(it.name, it.type, it.label)) }

        val phoneNumbersCopy: ArrayList<PhoneNumber> = ArrayList()
        phoneNumbers.forEach { phoneNumbersCopy.add(PhoneNumber(it.value, it.type, it.label, it.normalizedNumber, it.isPrimary)) }

        val emailsCopy: ArrayList<Email> = ArrayList()
        emails.forEach { emailsCopy.add(it.deepCopy()) }

        val addressesCopy: ArrayList<Address> = ArrayList()
        addresses.forEach { addressesCopy.add(it.deepCopy()) }

        val IMsCopy: ArrayList<IM> = ArrayList()
        IMs.forEach { IMsCopy.add(it.deepCopy()) }

        val eventsCopy: ArrayList<Event> = ArrayList()
        events.forEach { eventsCopy.add(it.deepCopy()) }

        val websitesCopy: ArrayList<ContactWebsite> = ArrayList()
        websites.forEach { websitesCopy.add(it.deepCopy()) }

        val relationsCopy: ArrayList<ContactRelation> = ArrayList()
        relations.forEach { relationsCopy.add(it.deepCopy()) }

        val groupsCopy: ArrayList<Group> = ArrayList()
        groups.forEach { groupsCopy.add(it.deepCopy()) }

        return(Contact(id, name.deepCopy(), nicknamesCopy, phoneNumbersCopy,
            emailsCopy, addressesCopy, IMsCopy, eventsCopy, notes,
            organization.deepCopy(), websitesCopy, relationsCopy,
            groupsCopy, thumbnailUri, photoUri, photo,
            starred, ringtone, contactId, source, mimetype))
    } // Contact.deepCopy()

    // *****************************************************************

    override fun compareTo(other: Contact): Int {
        return(compareTo(other, contactListSortBy, contactListInverseSortOrder, contactListShowFormattedName, contactListNameFormat))
    } // Contact.CompareTo()

    // *****************************************************************

    fun compareTo(other: Contact, sortBy: ContactNameSortBy, invertSort: Boolean,
                  showFormattedName: Boolean, nameFormat: ContactNameFormat): Int {
        var result: Int
        if (sortBy == ContactNameSortBy.NAMESORTBY_CONTACT_ID)
           result = this.id - other.id
        else {
            var thisKey: String = getPrimarySortKey(sortBy, showFormattedName, nameFormat)
            var otherKey: String = other.getPrimarySortKey(sortBy, showFormattedName, nameFormat)
            result = ContactName.compareNameStrings(thisKey, otherKey)

            if (result == 0) {
                val fieldCnt = contactListNameFieldOrder.length
                var pos = 0

                while ((pos < fieldCnt) && (result == 0)) {
                    when(contactListNameFieldOrder[pos]) {
                        'F' -> { thisKey = name.familyName   // Family name
                                 otherKey = other.name.familyName }
                        'M' -> { thisKey = name.middleName   // Family name
                                 otherKey = other.name.middleName }
                        'G' -> { thisKey = name.givenName   // Given name
                                 otherKey = other.name.givenName }
                        'P' -> { thisKey = name.prefix      // Prefix
                                 otherKey = other.name.prefix }
                        'S' -> { thisKey = name.suffix      // Suffix
                                 otherKey = other.name.suffix }
                        'D' -> { thisKey = name.formattedName      // Formatted/Display Name
                                 otherKey = other.name.formattedName }
                        'C' -> { thisKey = organization.company      // Organization/Company
                                 otherKey = other.organization.company }
                        else ->{ thisKey = name.formattedName  // Formatted Name
                                 otherKey = other.name.formattedName }
                    }
                    thisKey = thisKey.trim().normalizeString()
                    otherKey = otherKey.trim().normalizeString()
                    result = ContactName.compareNameStrings(thisKey, otherKey)
                    pos++
                } // while (pos < fieldCnt)

                // FIXME - If the primary sort keys are equal and all used
                // name fields are equal, should we go on to compare nicknames
                // and/or phonetic names and/or organisation names, or should
                // we just declare the two entries equal and compare by ID?
                // Decision: Just use IDs...

                if (result == 0) {
                    result = this.id - other.id
                }
            } // if (result == 0)
        }

        if (invertSort)
            result = -result

        return(result)
    } // Contact.CompareTo()

    // *****************************************************************

    private fun getPrimarySortKey(sortBy: ContactNameSortBy,
                                  showFormattedName: Boolean, nameFormat: ContactNameFormat): String {
        var sortKey: String = when (sortBy) {
            ContactNameSortBy.NAMESORTBY_DISPLAY_NAME   -> name.getDisplayName(showFormattedName, nameFormat)
            ContactNameSortBy.NAMESORTBY_FORMATTED_NAME -> name.formattedName
            ContactNameSortBy.NAMESORTBY_GIVEN_NAME     -> name.givenName
            ContactNameSortBy.NAMESORTBY_MIDDLE_NAME    -> name.middleName
            ContactNameSortBy.NAMESORTBY_FAMILY_NAME    -> name.familyName
            // PersonalNameSortBy.NAMESORTBY_CONTACT_ID     -> "***"
            else -> "***"
        }

        if (sortKey.isEmpty() && name.isCoreSegmEmpty()) {
            sortKey = getFullCompany()
            if (sortKey.isEmpty() && emails.isNotEmpty())
                sortKey = emails.first().address
        }

        return(sortKey.trim().normalizeString())
    } // Contact.getPrimarySortKey()

    // *****************************************************************

    fun getBubbleText() = when {
        ((sorting and SORT_BY_FIRST_NAME) != 0) -> name.givenName
        ((sorting and SORT_BY_MIDDLE_NAME) != 0) -> name.middleName
        else -> name.familyName
    } // Contact.getBubbleText()

    // *****************************************************************

    fun getNameToDisplay(showFormattedName: Boolean = contactListShowFormattedName,
                         nameFormat: ContactNameFormat = contactListNameFormat): String {
        val displayName: String = name.getDisplayName(showFormattedName, nameFormat)
        if (displayName.isNotEmpty())
            return(displayName)

        if (nicknames.isNotEmpty() && nicknames[0].name.isNotEmpty())
            return(nicknames[0].name)

        val companyName = getFullCompany()
        if (companyName.isNotEmpty())
            return(companyName)

        return(emails.firstOrNull()?.address?.trim()) ?: "***"
    } // Contact.getNameToDisplay()

    // *****************************************************************

    // John, Sir Elton Hercules, CH, CBE --> JE
    // Sir Elton Hercules John CH, CBE   --> EJ
    fun getNameForLetterPlaceholder(useFamilyNameForPlaceholderIcon: Boolean): String {
        val gotGivenName = name.givenName.isNotEmpty()
        val gotFamilyName = name.familyName.isNotEmpty()

        if (gotFamilyName && gotGivenName) {
            if (useFamilyNameForPlaceholderIcon) {
                return name.familyName[0].toString() //  + givenName[0].toString()
            } else {
                return name.givenName[0].toString() //  + familyName[0].toString()
            }
        } else if (gotFamilyName) {
            return name.familyName[0].toString()
        } else if (gotGivenName) {
            return name.givenName[0].toString()
        } else if (name.formattedName.isNotEmpty()) {
            return name.formattedName[0].toString()
        } else if (nicknames.isNotEmpty() && nicknames[0].name.isNotEmpty()) {
            return nicknames[0].name[0].toString()
        } else if (organization.company.isNotEmpty()) {
            val company = getFullCompany()
            return company[0].toString()
        } else {
            val email = emails.firstOrNull()?.address?.trim() ?: ""
            if (email.isNotEmpty()) {
                return email[0].toString()
            } else {
                return ("*")
            }
        }
    } // Contact.getNameForLetterPlaceholder()

    // *****************************************************************

    fun getLetterForFastScroller(sortBy: ContactNameSortBy,
                                 showFormattedName: Boolean, nameFormat: ContactNameFormat): String {
        val sortKey: String = getPrimarySortKey(sortBy, showFormattedName, nameFormat)
        return(sortKey[0].toString())
    } // Contact.getLetterForFastScroller()

    // *****************************************************************

    fun getNameForShortcutList(showFormattedName: Boolean, startWithFamilyName: Boolean): String {
        var C: Char
        if (showFormattedName && name.formattedName.isNotEmpty()) {
            C = name.formattedName[0]
        } else if (startWithFamilyName && name.familyName.isNotEmpty()) {
            C = name.familyName[0]
        } else if (!startWithFamilyName && name.givenName.isNotEmpty()) {
            C = name.givenName[0]
        } else if (nicknames.isNotEmpty() && nicknames[0].name.isNotEmpty()) {
            C = nicknames[0].name[0]
        } else if (organization.company.isNotEmpty()) {
            val company = getFullCompany()
            C = company[0]
        } else {
            val email = emails.firstOrNull()?.address?.trim() ?: ""
            if (email.isNotEmpty())
                C = email[0]
            else
                C = '*'
        }
        return(C.toString().uppercase().normalizeString())
    } // Contact.getNameForShortcutList()

    // *****************************************************************

    // photos stored locally always have different hashcodes. Avoid constantly refreshing the contact lists as the app thinks something changed.
    fun getHashWithoutPrivatePhoto(): Int {
        val photoToUse = if (isPrivate()) null else photo
        return copy(photo = photoToUse).hashCode()
    } // Contact.getNameForShortcutList()()

    // *****************************************************************

    fun getStringToCompare(): String {
        val photoToUse = if (isPrivate()) null else photo
        return Contact(
            id = 0,
            name = ContactName("", getNameToDisplay(contactListShowFormattedName, contactListNameFormat).toLowerCase(), "", "", "", "",
                "", "", ""),
            nicknames = ArrayList(),
            phoneNumbers = ArrayList(),
            emails = ArrayList(),
            addresses = ArrayList(),
            IMs = ArrayList(),
            events = ArrayList(),
            notes = "",
            organization = Organization.getEmptyOrganization(),
            websites = ArrayList(),
            relations = ArrayList(),
            groups = ArrayList(),
            thumbnailUri = "",
            photo = photoToUse,
            photoUri = "",
            starred = 0,
            ringtone = "",
            contactId = 0,
            source = "",
            mimetype = this.mimetype
        ).toString()
    } // Contact.getStringToCompare()

    // *****************************************************************

    fun getHashToCompare() = getStringToCompare().hashCode()

    // *****************************************************************

    fun getFullCompany(): String {
        var fullOrganization = if (organization.company.isEmpty()) "" else "${organization.company}, "
        fullOrganization += organization.jobTitle
        return fullOrganization.trim().trimEnd(',')
    } // Contact.getFullCompany()

    // *****************************************************************

    fun isABusinessContact() = name.isEmpty() && organization.isNotEmpty()

    // *****************************************************************

    fun doesContainPhoneNumber(text: String, convertLetters: Boolean = false): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = if (convertLetters) text.normalizePhoneNumber() else text
            phoneNumbers.any {
                PhoneNumberUtils.compare(it.normalizedNumber, normalizedText) ||
                    it.value.contains(text) ||
                    it.normalizedNumber.contains(normalizedText) ||
                    it.value.normalizePhoneNumber().contains(normalizedText)
            }
        } else {
            false
        }
    } // Contact.doesContainPhoneNumber()

    // *****************************************************************

    fun doesHavePhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber == text
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                        phoneNumber == text ||
                        phoneNumber.normalizePhoneNumber() == normalizedText ||
                        phoneNumber == normalizedText
                }
            }
        } else {
            false
        }
    } // Contact.doesHavePhoneNumber()

    // *****************************************************************

    fun isPrivate() = source == SMT_PRIVATE

    fun getSignatureKey() = if (photoUri.isNotEmpty()) photoUri else hashCode()

    fun getPrimaryNumber(): String? {
        val primaryNumber = phoneNumbers.firstOrNull { it.isPrimary }
        return primaryNumber?.normalizedNumber ?: phoneNumbers.firstOrNull()?.normalizedNumber
    }
} // class Contact
