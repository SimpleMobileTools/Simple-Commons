package com.simplemobiletools.commons.helpers

import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.*
import android.provider.MediaStore
import android.text.TextUtils
import android.util.SparseArray
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.PhoneNumber
import com.simplemobiletools.commons.models.contacts.*
import com.simplemobiletools.commons.overloads.times
import java.util.Locale

class ContactsHelper(val context: Context) {
    private val BATCH_SIZE = 50
    private var displayContactSources = ArrayList<String>()

    fun getContacts(
        getAll: Boolean = false,
        gettingDuplicates: Boolean = false,
        ignoredContactSources: HashSet<String> = HashSet(),
        callback: (ArrayList<Contact>) -> Unit
    ) {
        ensureBackgroundThread {
            val contacts = SparseArray<Contact>()
            displayContactSources = context.getVisibleContactSources()

            if (getAll) {
                displayContactSources = if (ignoredContactSources.isEmpty()) {
                    context.getAllContactSources().map { it.name }.toMutableList() as ArrayList
                } else {
                    context.getAllContactSources().filter {
                        it.getFullIdentifier().isNotEmpty() && !ignoredContactSources.contains(it.getFullIdentifier())
                    }.map { it.name }.toMutableList() as ArrayList
                }
            }

            getDeviceContacts(contacts, ignoredContactSources, gettingDuplicates)

            if (displayContactSources.contains(SMT_PRIVATE)) {
                LocalContactsHelper(context).getAllContacts().forEach {
                    contacts.put(it.id, it)
                }
            }

            val contactsSize = contacts.size()
            val showOnlyContactsWithNumbers = context.baseConfig.showOnlyContactsWithNumbers
            val tempContacts = ArrayList<Contact>(contactsSize)
            val resultContacts = ArrayList<Contact>(contactsSize)

            (0 until contactsSize).filter {
                if (ignoredContactSources.isEmpty() && showOnlyContactsWithNumbers) {
                    contacts.valueAt(it).phoneNumbers.isNotEmpty()
                } else {
                    true
                }
            }.mapTo(tempContacts) {
                contacts.valueAt(it)
            }

            if (context.baseConfig.mergeDuplicateContacts && ignoredContactSources.isEmpty() && !getAll) {
                tempContacts.filter { displayContactSources.contains(it.source) }.groupBy { it.getNameToDisplay().toLowerCase() }.values.forEach { it ->
                    if (it.size == 1) {
                        resultContacts.add(it.first())
                    } else {
                        val sorted = it.sortedByDescending { it.getStringToCompare().length }
                        resultContacts.add(sorted.first())
                    }
                }
            } else {
                resultContacts.addAll(tempContacts)
            }

            // groups are obtained with contactID, not rawID, so assign them to proper contacts like this
            val groups = getContactGroups(getStoredGroupsSync())
            val size = groups.size()
            for (i in 0 until size) {
                val key = groups.keyAt(i)
                resultContacts.firstOrNull { it.contactId == key }?.groups = groups.valueAt(i)
            }

            Contact.setSortOrder(context.baseConfig.sorting)
            Contact.setNameFormat(context.baseConfig.startNameWithSurname)
            resultContacts.sort()

            Handler(Looper.getMainLooper()).post {
                callback(resultContacts)
            }
        }
    }

    private fun getContentResolverAccounts(): HashSet<ContactSource> {
        val sources = HashSet<ContactSource>()
        arrayOf(Groups.CONTENT_URI, Settings.CONTENT_URI, RawContacts.CONTENT_URI).forEach {
            fillSourcesFromUri(it, sources)
        }

        return sources
    }

    private fun fillSourcesFromUri(uri: Uri, sources: HashSet<ContactSource>) {
        val projection = arrayOf(
            RawContacts.ACCOUNT_NAME,
            RawContacts.ACCOUNT_TYPE
        )

        context.queryCursor(uri, projection) { cursor ->
            val name = cursor.getStringValue(RawContacts.ACCOUNT_NAME) ?: ""
            val type = cursor.getStringValue(RawContacts.ACCOUNT_TYPE) ?: ""
            var publicName = name
            if (type == TELEGRAM_PACKAGE) {
                publicName = context.getString(R.string.telegram)
            }

            val source = ContactSource(name, type, publicName)
            sources.add(source)
        }
    }

    private fun getDeviceContacts(contacts: SparseArray<Contact>, ignoredContactSources: HashSet<String>?, gettingDuplicates: Boolean) {
        if (!context.hasContactPermissions()) {
            return
        }

        val ignoredSources = ignoredContactSources ?: context.baseConfig.ignoredContactSources
        val uri = Data.CONTENT_URI
        val projection = getContactProjection()

        arrayOf(CommonDataKinds.Organization.CONTENT_ITEM_TYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).forEach { mimetype ->
            val selection = "${Data.MIMETYPE} = ?"
            val selectionArgs = arrayOf(mimetype)
            val sortOrder = getSortString()

            context.queryCursor(uri, projection, selection, selectionArgs, sortOrder, true) { cursor ->
                val accountName = cursor.getStringValue(RawContacts.ACCOUNT_NAME) ?: ""
                val accountType = cursor.getStringValue(RawContacts.ACCOUNT_TYPE) ?: ""

                if (ignoredSources.contains("$accountName:$accountType")) {
                    return@queryCursor
                }

                val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
                var contactId = 0

                var name = ContactName.getEmptyName()

                // ignore names at Organization type contacts
                if (mimetype == CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
                    val displayName = cursor.getStringValue(CommonDataKinds.StructuredName.DISPLAY_NAME) ?: ""
                    val prefix = cursor.getStringValue(CommonDataKinds.StructuredName.PREFIX) ?: ""
                    val firstName = cursor.getStringValue(CommonDataKinds.StructuredName.GIVEN_NAME) ?: ""
                    val middleName = cursor.getStringValue(CommonDataKinds.StructuredName.MIDDLE_NAME) ?: ""
                    val familyName = cursor.getStringValue(CommonDataKinds.StructuredName.FAMILY_NAME) ?: ""
                    val suffix = cursor.getStringValue(CommonDataKinds.StructuredName.SUFFIX) ?: ""
                    val phoneticGivenName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME) ?: ""
                    val phoneticMiddleName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME) ?: ""
                    val phoneticFamilyName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME) ?: ""
                    name = ContactName(displayName,
                                prefix, firstName, middleName, familyName, suffix,
                                phoneticGivenName, phoneticMiddleName, phoneticFamilyName)
                }
                val nicknames = ArrayList<ContactNickname>()
                val phoneNumbers = ArrayList<PhoneNumber>()          // proper value is obtained below
                val emails = ArrayList<Email>()
                val addresses = ArrayList<Address>()
                val ims = ArrayList<IM>()
                val events = ArrayList<Event>()
                val notes = ""
                val organization: Organization = Organization.getEmptyOrganization()
                val websites = ArrayList<ContactWebsite>()
                val relations = ArrayList<ContactRelation>()
                val groups = ArrayList<Group>()

                var thumbnailUri = ""
                var photoUri = ""
                var starred = 0
                var ringtone: String? = null

                if (!gettingDuplicates) {
                    thumbnailUri = cursor.getStringValue(CommonDataKinds.StructuredName.PHOTO_THUMBNAIL_URI) ?: ""
                    photoUri = cursor.getStringValue(CommonDataKinds.StructuredName.PHOTO_URI) ?: ""
                    starred = cursor.getIntValue(CommonDataKinds.StructuredName.STARRED)
                    contactId = cursor.getIntValue(Data.CONTACT_ID)
                    ringtone = cursor.getStringValue(CommonDataKinds.StructuredName.CUSTOM_RINGTONE) ?: ""
                }

                val contact = Contact(id,
                    name, nicknames, phoneNumbers, emails, addresses, ims, events,
                    notes, organization, websites, relations, groups,
                    thumbnailUri, photoUri, null, starred, ringtone,
                    contactId, accountName, mimetype)

                contacts.put(id, contact)
            }
        }

        val emails = getEmails()
        var size = emails.size()
        for (i in 0 until size) {
            val key = emails.keyAt(i)
            contacts[key]?.emails = emails.valueAt(i)
        }

        val organizations = getOrganizations()
        size = organizations.size()
        for (i in 0 until size) {
            val key = organizations.keyAt(i)
            contacts[key]?.organization = organizations.valueAt(i)
        }

        // no need to fetch some fields if we are only getting duplicates of the current contact
        if (gettingDuplicates) {
            return
        }

        val phoneNumbers = getPhoneNumbers(null)
        size = phoneNumbers.size()
        for (i in 0 until size) {
            val key = phoneNumbers.keyAt(i)
            if (contacts[key] != null) {
                val numbers = phoneNumbers.valueAt(i)
                contacts[key].phoneNumbers = numbers
            }
        }

        val nicknames = getNicknames()
        size = nicknames.size()
        for (i in 0 until size) {
            val key = nicknames.keyAt(i)
            contacts[key]?.nicknames = nicknames.valueAt(i)
        }

        // emails already done above...
        val addresses = getAddresses()
        size = addresses.size()
        for (i in 0 until size) {
            val key = addresses.keyAt(i)
            contacts[key]?.addresses = addresses.valueAt(i)
        }

        val IMs = getIMs()
        size = IMs.size()
        for (i in 0 until size) {
            val key = IMs.keyAt(i)
            contacts[key]?.IMs = IMs.valueAt(i)
        }

        val events = getEvents()
        size = events.size()
        for (i in 0 until size) {
            val key = events.keyAt(i)
            contacts[key]?.events = events.valueAt(i)
        }

        val notes = getNotes()
        size = notes.size()
        for (i in 0 until size) {
            val key = notes.keyAt(i)
            contacts[key]?.notes = notes.valueAt(i)
        }

        // organization already done above...

        val websites = getWebsites()
        size = websites.size()
        for (i in 0 until size) {
            val key = websites.keyAt(i)
            contacts[key]?.websites = websites.valueAt(i)
        }

        val relations = getRelations()
        size = relations.size()
        for (i in 0 until size) {
            val key = relations.keyAt(i)
            contacts[key]?.relations = relations.valueAt(i)
        }
    }

    private fun getNicknames(contactId: Int? = null): SparseArray<ArrayList<ContactNickname>> {
        val nicknames = SparseArray<ArrayList<ContactNickname>>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Nickname.NAME,
            CommonDataKinds.Nickname.TYPE,
            CommonDataKinds.Nickname.LABEL
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val nickname = cursor.getStringValue(CommonDataKinds.Nickname.NAME) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Nickname.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Nickname.LABEL) ?: ""

            if (nicknames[id] == null) {
                nicknames.put(id, ArrayList())
            }
            nicknames[id].add(ContactNickname(nickname.trim(), type, label.trim()))
        }

        return nicknames
    } // ContactHelpers.getNicknames()

    private fun getPhoneNumbers(contactId: Int? = null): SparseArray<ArrayList<PhoneNumber>> {
        val phoneNumbers = SparseArray<ArrayList<PhoneNumber>>()
        val uri = CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Phone.NUMBER,
            CommonDataKinds.Phone.NORMALIZED_NUMBER,
            CommonDataKinds.Phone.TYPE,
            CommonDataKinds.Phone.LABEL,
            CommonDataKinds.Phone.IS_PRIMARY
        )

        val selection = if (contactId == null) getSourcesSelection() else "${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = if (contactId == null) getSourcesSelectionArgs() else arrayOf(contactId.toString())

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val number = cursor.getStringValue(CommonDataKinds.Phone.NUMBER) ?: return@queryCursor
            val normalizedNumber = cursor.getStringValue(CommonDataKinds.Phone.NORMALIZED_NUMBER) ?: number.normalizePhoneNumber()
            val type = cursor.getIntValue(CommonDataKinds.Phone.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Phone.LABEL) ?: ""
            val isPrimary = (cursor.getIntValue(CommonDataKinds.Phone.IS_PRIMARY) != 0)

            if (phoneNumbers[id] == null) {
                phoneNumbers.put(id, ArrayList())
            }

            val phoneNumber = PhoneNumber(number.trim(), type, label.trim(), normalizedNumber.trim(), isPrimary)
            phoneNumbers[id].add(phoneNumber)
        }

        return phoneNumbers
    } // ContactHelpers.getPhoneNumbers()

    private fun getEmails(contactId: Int? = null): SparseArray<ArrayList<Email>> {
        val emails = SparseArray<ArrayList<Email>>()
        val uri = CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Email.DATA,
            CommonDataKinds.Email.TYPE,
            CommonDataKinds.Email.LABEL
        )

        val selection = if (contactId == null) getSourcesSelection() else "${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = if (contactId == null) getSourcesSelectionArgs() else arrayOf(contactId.toString())

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val email = cursor.getStringValue(CommonDataKinds.Email.DATA) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Email.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Email.LABEL) ?: ""

            if (emails[id] == null) {
                emails.put(id, ArrayList())
            }

            emails[id]!!.add(Email(email, type, label))
        }

        return emails
    } // ContactHelpers.getEmails()

    private fun getAddresses(contactId: Int? = null): SparseArray<ArrayList<Address>> {
        val addresses = SparseArray<ArrayList<Address>>()
        val uri = CommonDataKinds.StructuredPostal.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            CommonDataKinds.StructuredPostal.TYPE,
            CommonDataKinds.StructuredPostal.LABEL,
            CommonDataKinds.StructuredPostal.STREET,
            CommonDataKinds.StructuredPostal.POBOX,
            CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
            CommonDataKinds.StructuredPostal.CITY,
            CommonDataKinds.StructuredPostal.REGION,
            CommonDataKinds.StructuredPostal.POSTCODE,
            CommonDataKinds.StructuredPostal.COUNTRY,
        )

        val selection = if (contactId == null) getSourcesSelection() else "${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = if (contactId == null) getSourcesSelectionArgs() else arrayOf(contactId.toString())

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val formatted_address = cursor.getStringValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.StructuredPostal.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.StructuredPostal.LABEL) ?: ""
            val street = cursor.getStringValue(CommonDataKinds.StructuredPostal.STREET) ?: ""
            val pobox = cursor.getStringValue(CommonDataKinds.StructuredPostal.POBOX) ?: ""
            val neighborhood = cursor.getStringValue(CommonDataKinds.StructuredPostal.NEIGHBORHOOD) ?: ""
            val city = cursor.getStringValue(CommonDataKinds.StructuredPostal.CITY) ?: ""
            val region = cursor.getStringValue(CommonDataKinds.StructuredPostal.REGION) ?: ""
            val postcode = cursor.getStringValue(CommonDataKinds.StructuredPostal.POSTCODE) ?: ""
            val country = cursor.getStringValue(CommonDataKinds.StructuredPostal.COUNTRY) ?: ""

            if (addresses[id] == null) {
                addresses.put(id, ArrayList())
            }

            addresses[id]!!.add(Address(formatted_address.trim(),
                street.trim(), pobox.trim(), neighborhood.trim(),
                city.trim(),  region.trim(), postcode.trim(), country.trim(),
                type, label))
        }

        return addresses
    } // ContactHelpers.getAddresses()

    private fun getIMs(contactId: Int? = null): SparseArray<ArrayList<IM>> {
        val IMs = SparseArray<ArrayList<IM>>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Im.DATA,
            CommonDataKinds.Im.TYPE,
            CommonDataKinds.Im.LABEL,
            CommonDataKinds.Im.PROTOCOL,
            CommonDataKinds.Im.CUSTOM_PROTOCOL
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Im.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val IM = cursor.getStringValue(CommonDataKinds.Im.DATA) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Im.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Im.LABEL) ?: ""
            val protocol = cursor.getIntValue(CommonDataKinds.Im.PROTOCOL)
            val custom_protocol = cursor.getStringValue(CommonDataKinds.Im.CUSTOM_PROTOCOL) ?: ""

            if (IMs[id] == null) {
                IMs.put(id, ArrayList())
            }

            val (editProtocol, editCustomProtocol) =
                        getIMEditProtocolFromAndroidProtocol(protocol, custom_protocol)
            IMs[id]!!.add(IM(IM.trim(), type, label.trim(), editProtocol, editCustomProtocol.trim()))
        }

        return IMs
    } // ContactHelpers.getIMs()

    private fun getIMEditProtocolFromAndroidProtocol(protocol: Int, custom_protocol: String): Pair<Int, String> {
        if (protocol != IM.PROTOCOL_CUSTOM) {
            return Pair(protocol, "")
        } else {
            val detectProtocol = when (custom_protocol.trim().lowercase()) {
                "" -> IM.PROTOCOL_CUSTOM
                context.getString(R.string.im_label_sip) -> IM.PROTOCOL_SIP
                context.getString(R.string.im_label_irc) -> IM.PROTOCOL_IRC
                context.getString(R.string.im_label_matrix) -> IM.PROTOCOL_MATRIX
                context.getString(R.string.im_label_mastodon) -> IM.PROTOCOL_MASTODON
                context.getString(R.string.im_label_signal) -> IM.PROTOCOL_SIGNAL
                context.getString(R.string.im_label_telegram) -> IM.PROTOCOL_TELEGRAM
                context.getString(R.string.im_label_diaspora) -> IM.PROTOCOL_DIASPORA
                context.getString(R.string.im_label_viber) -> IM.PROTOCOL_VIBER
                context.getString(R.string.im_label_threema) -> IM.PROTOCOL_THREEMA
                context.getString(R.string.im_label_discord) -> IM.PROTOCOL_DISCORD
                context.getString(R.string.im_label_mumble) -> IM.PROTOCOL_MUMBLE
                context.getString(R.string.im_label_olvid) -> IM.PROTOCOL_OLVID
                context.getString(R.string.im_label_teamspeak) -> IM.PROTOCOL_TEAMSPEAK
                context.getString(R.string.im_label_facebook) -> IM.PROTOCOL_FACEBOOK
                context.getString(R.string.im_label_instagram) -> IM.PROTOCOL_INSTAGRAM
                context.getString(R.string.im_label_whatsapp) -> IM.PROTOCOL_WHATSAPP
                context.getString(R.string.im_label_twitter) -> IM.PROTOCOL_TWITTER
                context.getString(R.string.im_label_wechat) -> IM.PROTOCOL_WECHAT
                context.getString(R.string.im_label_weibo) -> IM.PROTOCOL_WEIBO
                context.getString(R.string.im_label_tiktok) -> IM.PROTOCOL_TIKTOK
                context.getString(R.string.im_label_tumblr) -> IM.PROTOCOL_TUMBLR
                context.getString(R.string.im_label_flickr) -> IM.PROTOCOL_FLICKR
                context.getString(R.string.im_label_linkedin) -> IM.PROTOCOL_LINKEDIN
                context.getString(R.string.im_label_xing) -> IM.PROTOCOL_XING
                context.getString(R.string.im_label_kik) -> IM.PROTOCOL_KIK
                context.getString(R.string.im_label_line) -> IM.PROTOCOL_LINE
                context.getString(R.string.im_label_kakaotalk) -> IM.PROTOCOL_KAKAOTALK
                context.getString(R.string.im_label_zoom) -> IM.PROTOCOL_ZOOM
                context.getString(R.string.im_label_github) -> IM.PROTOCOL_GITHUB
                context.getString(R.string.im_label_googleplus) -> IM.PROTOCOL_GOOGLEPLUS
                context.getString(R.string.im_label_pinterest) -> IM.PROTOCOL_PINTEREST
             // context.getString(R.string.im_label_qzone) -> IM.PROTOCOL_QZONE
                context.getString(R.string.im_label_youtube) -> IM.PROTOCOL_YOUTUBE
                context.getString(R.string.im_label_snapchat) -> IM.PROTOCOL_SNAPCHAT
                context.getString(R.string.im_label_teams) -> IM.PROTOCOL_TEAMS
                context.getString(R.string.im_label_googlemeet) -> IM.PROTOCOL_GOOGLEMEET
                context.getString(R.string.im_label_teamviewermeet) -> IM.PROTOCOL_TEAMVIEWERMEET
                context.getString(R.string.im_label_nextcloudtalk) -> IM.PROTOCOL_NEXTCLOUDTALK
                context.getString(R.string.im_label_slack) -> IM.PROTOCOL_SLACK
                context.getString(R.string.im_label_jitsi) -> IM.PROTOCOL_JITSI
                context.getString(R.string.im_label_webex) -> IM.PROTOCOL_WEBEX
                context.getString(R.string.im_label_gotomeeting) -> IM.PROTOCOL_GOTOMEETING
                context.getString(R.string.im_label_bigbluebutton) -> IM.PROTOCOL_BIGBLUEBUTTON
                else -> IM.PROTOCOL_CUSTOM
            }
            return if (detectProtocol == IM.PROTOCOL_CUSTOM)
                Pair(detectProtocol, custom_protocol)
            else
                Pair(detectProtocol, "")
        }
    } // ContactsHelper.getIMEditProtocolFromAndroidProtocol()

    private fun getIMAndroidProtocolFromEditProtocol(protocol: Int, custom_protocol: String): Pair<Int, String> {
        return when (protocol) {
            IM.PROTOCOL_CUSTOM -> Pair(IM.PROTOCOL_CUSTOM, custom_protocol.trim())
            IM.PROTOCOL_AIM -> Pair(IM.PROTOCOL_AIM, "")
            IM.PROTOCOL_MSN -> Pair(IM.PROTOCOL_MSN, "")
            IM.PROTOCOL_YAHOO -> Pair(IM.PROTOCOL_YAHOO, "")
            IM.PROTOCOL_SKYPE -> Pair(IM.PROTOCOL_SKYPE, "")
            IM.PROTOCOL_QQ -> Pair(IM.PROTOCOL_QQ, "")
            IM.PROTOCOL_GOOGLE_TALK -> Pair(IM.PROTOCOL_GOOGLE_TALK, "")
            IM.PROTOCOL_ICQ -> Pair(IM.PROTOCOL_ICQ, "")
            IM.PROTOCOL_JABBER -> Pair(IM.PROTOCOL_JABBER, "")
            IM.PROTOCOL_NETMEETING -> Pair(IM.PROTOCOL_NETMEETING, "")

            IM.PROTOCOL_SIP -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_sip))
            IM.PROTOCOL_IRC -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_irc))

            IM.PROTOCOL_MATRIX -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_matrix))
            IM.PROTOCOL_MASTODON -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_mastodon))
            IM.PROTOCOL_SIGNAL -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_signal))
            IM.PROTOCOL_TELEGRAM -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_telegram))
            IM.PROTOCOL_DIASPORA -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_diaspora))
            IM.PROTOCOL_VIBER -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_viber))
            IM.PROTOCOL_THREEMA -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_threema))
            IM.PROTOCOL_DISCORD -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_discord))
            IM.PROTOCOL_MUMBLE -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_mumble))
            IM.PROTOCOL_OLVID -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_olvid))
            IM.PROTOCOL_TEAMSPEAK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_teamspeak))
            IM.PROTOCOL_FACEBOOK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_facebook))
            IM.PROTOCOL_INSTAGRAM -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_instagram))
            IM.PROTOCOL_WHATSAPP -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_whatsapp))
            IM.PROTOCOL_TWITTER -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_twitter))
            IM.PROTOCOL_WECHAT -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_wechat))
            IM.PROTOCOL_WEIBO -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_weibo))
            IM.PROTOCOL_TIKTOK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_tiktok))
            IM.PROTOCOL_TUMBLR -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_tumblr))
            IM.PROTOCOL_FLICKR -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_flickr))
            IM.PROTOCOL_LINKEDIN -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_linkedin))
            IM.PROTOCOL_XING -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_xing))
            IM.PROTOCOL_KIK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_kik))
            IM.PROTOCOL_LINE -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_line))
            IM.PROTOCOL_KAKAOTALK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_kakaotalk))
            IM.PROTOCOL_ZOOM -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_zoom))
            IM.PROTOCOL_GITHUB -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_github))
            IM.PROTOCOL_GOOGLEPLUS -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_googleplus))
            IM.PROTOCOL_PINTEREST -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_pinterest))
            // IM.PROTOCOL_QZONE -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_qzone))
            IM.PROTOCOL_YOUTUBE -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_youtube))
            IM.PROTOCOL_SNAPCHAT -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_snapchat))
            IM.PROTOCOL_TEAMS -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_teams))
            IM.PROTOCOL_GOOGLEMEET -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_googlemeet))
            IM.PROTOCOL_TEAMVIEWERMEET -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_teamviewermeet))
            IM.PROTOCOL_NEXTCLOUDTALK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_nextcloudtalk))
            IM.PROTOCOL_SLACK -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_slack))
            IM.PROTOCOL_JITSI -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_jitsi))
            IM.PROTOCOL_WEBEX -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_webex))
            IM.PROTOCOL_GOTOMEETING -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_gotomeeting))
            IM.PROTOCOL_BIGBLUEBUTTON -> Pair(IM.PROTOCOL_CUSTOM, context.getString(R.string.im_label_bigbluebutton))

            else -> Pair(IM.PROTOCOL_CUSTOM, custom_protocol.trim())
        }
    } // ContactsHelper.getIMAndroidProtocolFromEditProtocol()

    private fun getEvents(contactId: Int? = null): SparseArray<ArrayList<Event>> {
        val events = SparseArray<ArrayList<Event>>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Event.START_DATE,
            CommonDataKinds.Event.TYPE,
            CommonDataKinds.Event.LABEL
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Event.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val startDate = cursor.getStringValue(CommonDataKinds.Event.START_DATE) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Event.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Event.LABEL) ?: ""

            if (events[id] == null) {
                events.put(id, ArrayList())
            }

            events[id]!!.add(Event(startDate.trim(), type, label.trim()))
        }

        return events
    } // ContactHelpers.getEvents()

    private fun getNotes(contactId: Int? = null): SparseArray<String> {
        val notes = SparseArray<String>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Note.NOTE
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Note.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val note = cursor.getStringValue(CommonDataKinds.Note.NOTE) ?: return@queryCursor
            notes.put(id, note)
        }

        return notes
    } // ContactHelpers.getNotes()

    private fun getOrganizations(contactId: Int? = null): SparseArray<Organization> {
        val organizations = SparseArray<Organization>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Organization.COMPANY,
            CommonDataKinds.Organization.TYPE,
            CommonDataKinds.Organization.LABEL,
            CommonDataKinds.Organization.TITLE,
            CommonDataKinds.Organization.DEPARTMENT,
            CommonDataKinds.Organization.JOB_DESCRIPTION,
            CommonDataKinds.Organization.SYMBOL,
            CommonDataKinds.Organization.PHONETIC_NAME,
            CommonDataKinds.Organization.OFFICE_LOCATION
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Organization.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val company = cursor.getStringValue(CommonDataKinds.Organization.COMPANY) ?: ""
            val type = cursor.getIntValue(CommonDataKinds.Organization.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Organization.LABEL) ?: ""
            val title = cursor.getStringValue(CommonDataKinds.Organization.TITLE) ?: ""
            val department = cursor.getStringValue(CommonDataKinds.Organization.DEPARTMENT) ?: ""
            val job_description = cursor.getStringValue(CommonDataKinds.Organization.JOB_DESCRIPTION) ?: ""
            val symbol = cursor.getStringValue(CommonDataKinds.Organization.SYMBOL) ?: ""
            val phonetic_name = cursor.getStringValue(CommonDataKinds.Organization.PHONETIC_NAME) ?: ""
            val office_location = cursor.getStringValue(CommonDataKinds.Organization.OFFICE_LOCATION) ?: ""
            if (company.isEmpty() && title.isEmpty()) {
                return@queryCursor
            }

            val organization = Organization(company.trim(), title.trim(),
                                    department.trim(), job_description.trim(),
                                    symbol.trim(), phonetic_name.trim(),
                                    office_location.trim(), type, label.trim())
            organizations.put(id, organization)
        }

        return organizations
    } // ContactHelpers.getOrganizations()

    private fun getWebsites(contactId: Int? = null): SparseArray<ArrayList<ContactWebsite>> {
        val websites = SparseArray<ArrayList<ContactWebsite>>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Website.URL,
            CommonDataKinds.Website.TYPE,
            CommonDataKinds.Website.LABEL
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Website.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val url = cursor.getStringValue(CommonDataKinds.Website.URL) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Website.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Website.LABEL) ?: ""

            if (websites[id] == null) {
                websites.put(id, ArrayList())
            }

            websites[id]!!.add(ContactWebsite(url.trim(), type, label.trim()))
        }

        return websites
    } // ContactHelpers.getWebsites()

    private fun getRelations(contactId: Int? = null): SparseArray<ArrayList<ContactRelation>> {
        val relations = SparseArray<ArrayList<ContactRelation>>()
        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.RAW_CONTACT_ID,
            CommonDataKinds.Relation.NAME,
            CommonDataKinds.Relation.TYPE,
            CommonDataKinds.Relation.LABEL
        )

        val selection = getSourcesSelection(true, contactId != null)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.Relation.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
            val name = cursor.getStringValue(CommonDataKinds.Relation.NAME) ?: return@queryCursor
            val type = cursor.getIntValue(CommonDataKinds.Relation.TYPE)
            val label = cursor.getStringValue(CommonDataKinds.Relation.LABEL) ?: ""

            if (relations[id] == null) {
                relations.put(id, ArrayList())
            }

            val (editType, editLabel) = getRelationEditTypeLabelFromAndroidTypeLabel(type, label)
            relations[id]!!.add(ContactRelation(name.trim(), editType, editLabel.trim()))
        }

        return relations
    } // ContactHelpers.getRelations()

    private fun getRelationEditTypeLabelFromAndroidTypeLabel(type: Int, label: String): Pair<Int, String> {
        if (type != ContactRelation.TYPE_CUSTOM) {
            return Pair(type, "")
        } else {
            val detectType = when (label.trim().lowercase()) {
                "" -> ContactRelation.TYPE_CUSTOM
                context.getString(R.string.relation_label_assistant) -> ContactRelation.TYPE_ASSISTANT
                context.getString(R.string.relation_label_brother) -> ContactRelation.TYPE_BROTHER
                context.getString(R.string.relation_label_child) -> ContactRelation.TYPE_CHILD
                context.getString(R.string.relation_label_domestic_partner) -> ContactRelation.TYPE_DOMESTIC_PARTNER
                context.getString(R.string.relation_label_father) -> ContactRelation.TYPE_FATHER
                context.getString(R.string.relation_label_friend) -> ContactRelation.TYPE_FRIEND
                context.getString(R.string.relation_label_manager) -> ContactRelation.TYPE_MANAGER
                context.getString(R.string.relation_label_mother) -> ContactRelation.TYPE_MOTHER
                context.getString(R.string.relation_label_parent) -> ContactRelation.TYPE_PARENT
                context.getString(R.string.relation_label_partner) -> ContactRelation.TYPE_PARTNER
                context.getString(R.string.relation_label_referred_by) -> ContactRelation.TYPE_REFERRED_BY
                context.getString(R.string.relation_label_relative) -> ContactRelation.TYPE_RELATIVE
                context.getString(R.string.relation_label_sister) -> ContactRelation.TYPE_SISTER
                context.getString(R.string.relation_label_spouse) -> ContactRelation.TYPE_SPOUSE
                context.getString(R.string.relation_label_contact) -> ContactRelation.TYPE_CONTACT
                context.getString(R.string.relation_label_acquaintance) -> ContactRelation.TYPE_ACQUAINTANCE
                context.getString(R.string.relation_label_met) -> ContactRelation.TYPE_MET
                context.getString(R.string.relation_label_co_worker) -> ContactRelation.TYPE_CO_WORKER
                context.getString(R.string.relation_label_colleague) -> ContactRelation.TYPE_COLLEAGUE
                context.getString(R.string.relation_label_co_resident) -> ContactRelation.TYPE_CO_RESIDENT
                context.getString(R.string.relation_label_neighbor) -> ContactRelation.TYPE_NEIGHBOR
                context.getString(R.string.relation_label_sibling) -> ContactRelation.TYPE_SIBLING
                context.getString(R.string.relation_label_kin) -> ContactRelation.TYPE_KIN
                context.getString(R.string.relation_label_kin_alt) -> ContactRelation.TYPE_KIN
                context.getString(R.string.relation_label_muse) -> ContactRelation.TYPE_MUSE
                context.getString(R.string.relation_label_crush) -> ContactRelation.TYPE_CRUSH
                context.getString(R.string.relation_label_date) -> ContactRelation.TYPE_DATE
                context.getString(R.string.relation_label_sweetheart) -> ContactRelation.TYPE_SWEETHEART
                context.getString(R.string.relation_label_agent) -> ContactRelation.TYPE_AGENT
                context.getString(R.string.relation_label_emergency) -> ContactRelation.TYPE_EMERGENCY
                context.getString(R.string.relation_label_me) -> ContactRelation.TYPE_ME
                context.getString(R.string.relation_label_superior) -> ContactRelation.TYPE_SUPERIOR
                context.getString(R.string.relation_label_subordinate) -> ContactRelation.TYPE_SUBORDINATE
                context.getString(R.string.relation_label_husband) -> ContactRelation.TYPE_HUSBAND
                context.getString(R.string.relation_label_wife) -> ContactRelation.TYPE_WIFE
                context.getString(R.string.relation_label_son) -> ContactRelation.TYPE_SON
                context.getString(R.string.relation_label_daughter) -> ContactRelation.TYPE_DAUGHTER
                context.getString(R.string.relation_label_grandparent) -> ContactRelation.TYPE_GRANDPARENT
                context.getString(R.string.relation_label_grandfather) -> ContactRelation.TYPE_GRANDFATHER
                context.getString(R.string.relation_label_grandmother) -> ContactRelation.TYPE_GRANDMOTHER
                context.getString(R.string.relation_label_grandchild) -> ContactRelation.TYPE_GRANDCHILD
                context.getString(R.string.relation_label_grandson) -> ContactRelation.TYPE_GRANDSON
                context.getString(R.string.relation_label_granddaughter) -> ContactRelation.TYPE_GRANDDAUGHTER
                context.getString(R.string.relation_label_uncle) -> ContactRelation.TYPE_UNCLE
                context.getString(R.string.relation_label_aunt) -> ContactRelation.TYPE_AUNT
                context.getString(R.string.relation_label_nephew) -> ContactRelation.TYPE_NEPHEW
                context.getString(R.string.relation_label_niece) -> ContactRelation.TYPE_NIECE
                context.getString(R.string.relation_label_father_in_law) -> ContactRelation.TYPE_FATHER_IN_LAW
                context.getString(R.string.relation_label_mother_in_law) -> ContactRelation.TYPE_MOTHER_IN_LAW
                context.getString(R.string.relation_label_son_in_law) -> ContactRelation.TYPE_SON_IN_LAW
                context.getString(R.string.relation_label_daughter_in_law) -> ContactRelation.TYPE_DAUGHTER_IN_LAW
                context.getString(R.string.relation_label_brother_in_law) -> ContactRelation.TYPE_BROTHER_IN_LAW
                context.getString(R.string.relation_label_sister_in_law) -> ContactRelation.TYPE_SISTER_IN_LAW
                else -> ContactRelation.TYPE_CUSTOM
            }
            return if (detectType == ContactRelation.TYPE_CUSTOM)
                Pair(detectType, label)
            else
                Pair(detectType, "")
        }
    } // ContactsHelper.getRelationEditTypeLabelFromAndroidTypeLabel()

    private fun getRelationAndroidTypeLabelFromEditTypeLabel(type: Int, label: String): Pair<Int, String> {
        return when (type) {
            ContactRelation.TYPE_CUSTOM -> Pair(ContactRelation.TYPE_CUSTOM, label.trim())
            ContactRelation.TYPE_ASSISTANT -> Pair(ContactRelation.TYPE_ASSISTANT, "")
            ContactRelation.TYPE_BROTHER -> Pair(ContactRelation.TYPE_BROTHER, "")
            ContactRelation.TYPE_CHILD -> Pair(ContactRelation.TYPE_CHILD, "")
            ContactRelation.TYPE_DOMESTIC_PARTNER -> Pair(ContactRelation.TYPE_DOMESTIC_PARTNER, "")
            ContactRelation.TYPE_FATHER -> Pair(ContactRelation.TYPE_FATHER, "")
            ContactRelation.TYPE_FRIEND -> Pair(ContactRelation.TYPE_FRIEND, "")
            ContactRelation.TYPE_MANAGER -> Pair(ContactRelation.TYPE_MANAGER, "")
            ContactRelation.TYPE_MOTHER -> Pair(ContactRelation.TYPE_MOTHER, "")
            ContactRelation.TYPE_PARENT -> Pair(ContactRelation.TYPE_PARENT, "")
            ContactRelation.TYPE_PARTNER -> Pair(ContactRelation.TYPE_PARTNER, "")
            ContactRelation.TYPE_REFERRED_BY -> Pair(ContactRelation.TYPE_REFERRED_BY, "")
            ContactRelation.TYPE_RELATIVE -> Pair(ContactRelation.TYPE_RELATIVE, "")
            ContactRelation.TYPE_SISTER -> Pair(ContactRelation.TYPE_SISTER, "")
            ContactRelation.TYPE_SPOUSE -> Pair(ContactRelation.TYPE_SPOUSE, "")

            // Relation types defined in vCard 4.0
            ContactRelation.TYPE_CONTACT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_contact))
            ContactRelation.TYPE_ACQUAINTANCE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_acquaintance))
            // ContactRelation.TYPE_FRIEND -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_friend))
            ContactRelation.TYPE_MET -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_met))
            ContactRelation.TYPE_CO_WORKER -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_co_worker))
            ContactRelation.TYPE_COLLEAGUE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_colleague))
            ContactRelation.TYPE_CO_RESIDENT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_co_resident))
            ContactRelation.TYPE_NEIGHBOR -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_neighbor))
            // ContactRelation.TYPE_CHILD -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_child))
            // ContactRelation.TYPE_PARENT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_parent))
            ContactRelation.TYPE_SIBLING -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_sibling))
            // ContactRelation.TYPE_SPOUSE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_spouse))
            ContactRelation.TYPE_KIN -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_kin))
            ContactRelation.TYPE_MUSE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_muse))
            ContactRelation.TYPE_CRUSH -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_crush))
            ContactRelation.TYPE_DATE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_date))
            ContactRelation.TYPE_SWEETHEART -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_sweetheart))
            ContactRelation.TYPE_ME -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_me))
            ContactRelation.TYPE_AGENT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_agent))
            ContactRelation.TYPE_EMERGENCY -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_emergency))

            ContactRelation.TYPE_SUPERIOR -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_superior))
            ContactRelation.TYPE_SUBORDINATE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_subordinate))
            ContactRelation.TYPE_HUSBAND -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_husband))
            ContactRelation.TYPE_WIFE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_wife))
            ContactRelation.TYPE_SON -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_son))
            ContactRelation.TYPE_DAUGHTER -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_daughter))
            ContactRelation.TYPE_GRANDPARENT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_grandparent))
            ContactRelation.TYPE_GRANDFATHER -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_grandfather))
            ContactRelation.TYPE_GRANDMOTHER -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_grandmother))
            ContactRelation.TYPE_GRANDCHILD -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_grandchild))
            ContactRelation.TYPE_GRANDSON -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_grandson))
            ContactRelation.TYPE_GRANDDAUGHTER -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_granddaughter))
            ContactRelation.TYPE_UNCLE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_uncle))
            ContactRelation.TYPE_AUNT -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_aunt))
            ContactRelation.TYPE_NEPHEW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_nephew))
            ContactRelation.TYPE_NIECE -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_niece))
            ContactRelation.TYPE_FATHER_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_father_in_law))
            ContactRelation.TYPE_MOTHER_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_mother_in_law))
            ContactRelation.TYPE_SON_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_son_in_law))
            ContactRelation.TYPE_DAUGHTER_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_daughter_in_law))
            ContactRelation.TYPE_BROTHER_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_brother_in_law))
            ContactRelation.TYPE_SISTER_IN_LAW -> Pair(ContactRelation.TYPE_CUSTOM, context.getString(R.string.relation_label_sister_in_law))

            else -> Pair(ContactRelation.TYPE_CUSTOM, label.trim())
        }
    } // ContactsHelper.getRelationAndroidTypeLabelFromEditTypeLabel()

    private fun getContactGroups(storedGroups: ArrayList<Group>, contactId: Int? = null): SparseArray<ArrayList<Group>> {
        val groups = SparseArray<ArrayList<Group>>()
        if (!context.hasContactPermissions()) {
            return groups
        }

        val uri = Data.CONTENT_URI
        val projection = arrayOf(
            Data.CONTACT_ID,
            CommonDataKinds.GroupMembership.GROUP_ROW_ID
        )

        val selection = getSourcesSelection(true, contactId != null, false)
        val selectionArgs = getSourcesSelectionArgs(CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, contactId)

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getIntValue(Data.CONTACT_ID)
            val newRowId = cursor.getLongValue(CommonDataKinds.GroupMembership.GROUP_ROW_ID)

            val groupTitle = storedGroups.firstOrNull { it.id == newRowId }?.title ?: return@queryCursor
            val group = Group(newRowId, groupTitle)
            if (groups[id] == null) {
                groups.put(id, ArrayList())
            }
            groups[id]!!.add(group)
        }

        return groups
    } // ContactHelpers.getContactGroups()

    private fun getQuestionMarks() = ("?," * displayContactSources.filter { it.isNotEmpty() }.size).trimEnd(',')

    private fun getSourcesSelection(addMimeType: Boolean = false, addContactId: Boolean = false, useRawContactId: Boolean = true): String {
        val strings = ArrayList<String>()
        if (addMimeType) {
            strings.add("${Data.MIMETYPE} = ?")
        }

        if (addContactId) {
            strings.add("${if (useRawContactId) Data.RAW_CONTACT_ID else Data.CONTACT_ID} = ?")
        } else {
            // sometimes local device storage has null account_name, handle it properly
            val accountnameString = StringBuilder()
            if (displayContactSources.contains("")) {
                accountnameString.append("(")
            }
            accountnameString.append("${RawContacts.ACCOUNT_NAME} IN (${getQuestionMarks()})")
            if (displayContactSources.contains("")) {
                accountnameString.append(" OR ${RawContacts.ACCOUNT_NAME} IS NULL)")
            }
            strings.add(accountnameString.toString())
        }

        return TextUtils.join(" AND ", strings)
    }

    private fun getSourcesSelectionArgs(mimetype: String? = null, contactId: Int? = null): Array<String> {
        val args = ArrayList<String>()

        if (mimetype != null) {
            args.add(mimetype)
        }

        if (contactId != null) {
            args.add(contactId.toString())
        } else {
            args.addAll(displayContactSources.filter { it.isNotEmpty() })
        }

        return args.toTypedArray()
    }

    fun getStoredGroups(callback: (ArrayList<Group>) -> Unit) {
        ensureBackgroundThread {
            val groups = getStoredGroupsSync()
            Handler(Looper.getMainLooper()).post {
                callback(groups)
            }
        }
    }

    fun getStoredGroupsSync(): ArrayList<Group> {
        val groups = getDeviceStoredGroups()
        groups.addAll(context.groupsDB.getGroups())
        return groups
    }

    private fun getDeviceStoredGroups(): ArrayList<Group> {
        val groups = ArrayList<Group>()
        if (!context.hasContactPermissions()) {
            return groups
        }

        val uri = Groups.CONTENT_URI
        val projection = arrayOf(
            Groups._ID,
            Groups.TITLE,
            Groups.SYSTEM_ID
        )

        val selection = "${Groups.AUTO_ADD} = ? AND ${Groups.FAVORITES} = ?"
        val selectionArgs = arrayOf("0", "0")

        context.queryCursor(uri, projection, selection, selectionArgs, showErrors = true) { cursor ->
            val id = cursor.getLongValue(Groups._ID)
            val title = cursor.getStringValue(Groups.TITLE) ?: return@queryCursor

            val systemId = cursor.getStringValue(Groups.SYSTEM_ID) ?: ""
            if (groups.map { it.title }.contains(title) && systemId != null) {
                return@queryCursor
            }

            groups.add(Group(id, title))
        }
        return groups
    }

    fun createNewGroup(title: String, accountName: String, accountType: String): Group? {
        if (accountType == SMT_PRIVATE) {
            val newGroup = Group(null, title)
            val id = context.groupsDB.insertOrUpdate(newGroup)
            newGroup.id = id
            return newGroup
        }

        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newInsert(Groups.CONTENT_URI).apply {
            withValue(Groups.TITLE, title)
            withValue(Groups.GROUP_VISIBLE, 1)
            withValue(Groups.ACCOUNT_NAME, accountName)
            withValue(Groups.ACCOUNT_TYPE, accountType)
            operations.add(build())
        }

        try {
            val results = context.contentResolver.applyBatch(AUTHORITY, operations)
            val rawId = ContentUris.parseId(results[0].uri!!)
            return Group(rawId, title)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
        return null
    }

    fun renameGroup(group: Group) {
        val operations = ArrayList<ContentProviderOperation>()
        ContentProviderOperation.newUpdate(Groups.CONTENT_URI).apply {
            val selection = "${Groups._ID} = ?"
            val selectionArgs = arrayOf(group.id.toString())
            withSelection(selection, selectionArgs)
            withValue(Groups.TITLE, group.title)
            operations.add(build())
        }

        try {
            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    fun deleteGroup(id: Long) {
        val operations = ArrayList<ContentProviderOperation>()
        val uri = ContentUris.withAppendedId(Groups.CONTENT_URI, id).buildUpon()
            .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
            .build()

        operations.add(ContentProviderOperation.newDelete(uri).build())

        try {
            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    fun getContactWithId(id: Int, isLocalPrivate: Boolean): Contact? {
        if (id == 0) {
            return null
        } else if (isLocalPrivate) {
            return LocalContactsHelper(context).getContactWithId(id)
        }

        val selection = "(${Data.MIMETYPE} = ? OR ${Data.MIMETYPE} = ?) AND ${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = arrayOf(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE, id.toString())
        return parseContactCursor(selection, selectionArgs)
    }

    fun getContactFromUri(uri: Uri): Contact? {
        val key = getLookupKeyFromUri(uri) ?: return null
        return getContactWithLookupKey(key)
    }

    private fun getLookupKeyFromUri(lookupUri: Uri): String? {
        val projection = arrayOf(ContactsContract.Contacts.LOOKUP_KEY)
        val cursor = context.contentResolver.query(lookupUri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(ContactsContract.Contacts.LOOKUP_KEY) ?: ""
            }
        }
        return null
    }

    fun getContactWithLookupKey(key: String): Contact? {
        val selection = "(${Data.MIMETYPE} = ? OR ${Data.MIMETYPE} = ?) AND ${Data.LOOKUP_KEY} = ?"
        val selectionArgs = arrayOf(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE, key)
        return parseContactCursor(selection, selectionArgs)
    }

    private fun parseContactCursor(selection: String, selectionArgs: Array<String>): Contact? {
        val storedGroups = getStoredGroupsSync()
        val uri = Data.CONTENT_URI
        val projection = getContactProjection()

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val id = cursor.getIntValue(Data.RAW_CONTACT_ID)
                var name = ContactName.getEmptyName()

                var prefix = ""
                var firstName = ""
                var middleName = ""
                var surname = ""
                var suffix = ""
                var mimetype = cursor.getStringValue(Data.MIMETYPE) ?: ""

                // If first line is an Organization type contact, go to next line
                if (mimetype != CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
                    if (cursor.moveToNext()) {
                        mimetype = cursor.getStringValue(Data.MIMETYPE) ?: ""
                    }
                }

                // Ignore names at Organization type contacts
                if (mimetype == CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
                    val displayName = cursor.getStringValue(CommonDataKinds.StructuredName.DISPLAY_NAME) ?: ""
                    val prefix = cursor.getStringValue(CommonDataKinds.StructuredName.PREFIX) ?: ""
                    val firstName = cursor.getStringValue(CommonDataKinds.StructuredName.GIVEN_NAME) ?: ""
                    val middleName = cursor.getStringValue(CommonDataKinds.StructuredName.MIDDLE_NAME) ?: ""
                    val familyName = cursor.getStringValue(CommonDataKinds.StructuredName.FAMILY_NAME) ?: ""
                    val suffix = cursor.getStringValue(CommonDataKinds.StructuredName.SUFFIX) ?: ""
                    val phoneticGivenName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME) ?: ""
                    val phoneticMiddleName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME) ?: ""
                    val phoneticFamilyName = cursor.getStringValue(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME) ?: ""
                    name = ContactName(displayName,
                        prefix, firstName, middleName, familyName, suffix,
                        phoneticGivenName, phoneticMiddleName, phoneticFamilyName)
                }

                val nicknames = getNicknames(id)[id] ?: ArrayList()
                val phoneNumbers = getPhoneNumbers(id)[id] ?: ArrayList()
                val emails = getEmails(id)[id] ?: ArrayList()
                val addresses = getAddresses(id)[id] ?: ArrayList()
                val ims = getIMs(id)[id] ?: ArrayList()
                val events = getEvents(id)[id] ?: ArrayList()
                val notes = getNotes(id)[id] ?: ""
                val organization = getOrganizations(id)[id] ?: Organization.getEmptyOrganization()
                val websites = getWebsites(id)[id] ?: ArrayList()
                val relations = getRelations(id)[id] ?: ArrayList()
                val contactId = cursor.getIntValue(Data.CONTACT_ID)
                val groups = getContactGroups(storedGroups, contactId)[contactId] ?: ArrayList()
                val thumbnailUri = cursor.getStringValue(CommonDataKinds.StructuredName.PHOTO_THUMBNAIL_URI) ?: ""
                val photoUri = cursor.getStringValueOrNull(CommonDataKinds.Phone.PHOTO_URI) ?: ""
                val starred = cursor.getIntValue(CommonDataKinds.StructuredName.STARRED)
                val ringtone = cursor.getStringValue(CommonDataKinds.StructuredName.CUSTOM_RINGTONE) ?: ""
                val accountName = cursor.getStringValue(RawContacts.ACCOUNT_NAME) ?: ""
                return Contact(id,
                    name, nicknames, phoneNumbers, emails, addresses, ims, events,
                    notes, organization, websites, relations, groups,
                    thumbnailUri, photoUri, null, starred, ringtone,
                    contactId, accountName, mimetype)
            }
        }

        return null
    }

    fun getContactSources(callback: (ArrayList<ContactSource>) -> Unit) {
        ensureBackgroundThread {
            callback(getContactSourcesSync())
        }
    }

    private fun getContactSourcesSync(): ArrayList<ContactSource> {
        val sources = getDeviceContactSources()
        sources.add(context.getPrivateContactSource())
        return ArrayList(sources)
    }

    fun getSaveableContactSources(callback: (ArrayList<ContactSource>) -> Unit) {
        ensureBackgroundThread {
            val ignoredTypes = arrayListOf(
                SIGNAL_PACKAGE,
                TELEGRAM_PACKAGE,
                WHATSAPP_PACKAGE,
                THREEMA_PACKAGE
            )

            val contactSources = getContactSourcesSync()
            val filteredSources = contactSources.filter { !ignoredTypes.contains(it.type) }.toMutableList() as ArrayList<ContactSource>
            callback(filteredSources)
        }
    }

    fun getDeviceContactSources(): LinkedHashSet<ContactSource> {
        val sources = LinkedHashSet<ContactSource>()
        if (!context.hasContactPermissions()) {
            return sources
        }

        if (!context.baseConfig.wasLocalAccountInitialized) {
            initializeLocalPhoneAccount()
            context.baseConfig.wasLocalAccountInitialized = true
        }

        val accounts = AccountManager.get(context).accounts
        accounts.forEach {
            if (ContentResolver.getIsSyncable(it, AUTHORITY) == 1) {
                var publicName = it.name
                if (it.type == TELEGRAM_PACKAGE) {
                    publicName = context.getString(R.string.telegram)
                } else if (it.type == VIBER_PACKAGE) {
                    publicName = context.getString(R.string.viber)
                }
                val contactSource = ContactSource(it.name, it.type, publicName)
                sources.add(contactSource)
            }
        }

        var hadEmptyAccount = false
        val allAccounts = getContentResolverAccounts()
        val contentResolverAccounts = allAccounts.filter {
            if (it.name.isEmpty() && it.type.isEmpty() && allAccounts.none { it.name.lowercase(Locale.getDefault()) == "phone" }) {
                hadEmptyAccount = true
            }

            it.name.isNotEmpty() && it.type.isNotEmpty() && !accounts.contains(Account(it.name, it.type))
        }
        sources.addAll(contentResolverAccounts)

        if (hadEmptyAccount) {
            sources.add(ContactSource("", "", context.getString(R.string.phone_storage)))
        }

        return sources
    }

    // make sure the local Phone contact source is initialized and available
    // https://stackoverflow.com/a/6096508/1967672
    private fun initializeLocalPhoneAccount() {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).apply {
                withValue(RawContacts.ACCOUNT_NAME, null)
                withValue(RawContacts.ACCOUNT_TYPE, null)
                operations.add(build())
            }

            val results = context.contentResolver.applyBatch(AUTHORITY, operations)
            val rawContactUri = results.firstOrNull()?.uri ?: return
            context.contentResolver.delete(rawContactUri, null, null)
        } catch (ignored: Exception) {
        }
    }

    private fun getContactSourceType(accountName: String) = getDeviceContactSources().firstOrNull { it.name == accountName }?.type ?: ""

    private fun getContactProjection() = arrayOf(
        Data.MIMETYPE,
        Data.CONTACT_ID,
        Data.RAW_CONTACT_ID,
        CommonDataKinds.StructuredName.DISPLAY_NAME,
        CommonDataKinds.StructuredName.PREFIX,
        CommonDataKinds.StructuredName.GIVEN_NAME,
        CommonDataKinds.StructuredName.MIDDLE_NAME,
        CommonDataKinds.StructuredName.FAMILY_NAME,
        CommonDataKinds.StructuredName.SUFFIX,
        CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME,
        CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME,
        CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME,
        CommonDataKinds.StructuredName.PHOTO_URI,
        CommonDataKinds.StructuredName.PHOTO_THUMBNAIL_URI,
        CommonDataKinds.StructuredName.STARRED,
        CommonDataKinds.StructuredName.CUSTOM_RINGTONE,
        RawContacts.ACCOUNT_NAME,
        RawContacts.ACCOUNT_TYPE
    )

    private fun getSortString(): String {
        val sorting = context.baseConfig.sorting
        return when {
            sorting and SORT_BY_FIRST_NAME != 0 -> "${CommonDataKinds.StructuredName.GIVEN_NAME} COLLATE NOCASE"
            sorting and SORT_BY_MIDDLE_NAME != 0 -> "${CommonDataKinds.StructuredName.MIDDLE_NAME} COLLATE NOCASE"
            sorting and SORT_BY_SURNAME != 0 -> "${CommonDataKinds.StructuredName.FAMILY_NAME} COLLATE NOCASE"
            sorting and SORT_BY_FULL_NAME != 0 -> CommonDataKinds.StructuredName.DISPLAY_NAME
            else -> Data.RAW_CONTACT_ID
        }
    }

    private fun getRealContactId(id: Long): Int {
        val uri = Data.CONTENT_URI
        val projection = getContactProjection()
        val selection = "(${Data.MIMETYPE} = ? OR ${Data.MIMETYPE} = ?) AND ${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = arrayOf(CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE, id.toString())

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getIntValue(Data.CONTACT_ID)
            }
        }

        return 0
    }

    fun insertContact(contact: Contact): Boolean {
        return insertOrUpdateContact(true, contact, null,
            if (contact.photoUri.isNotEmpty()) PHOTO_ADDED else PHOTO_UNCHANGED)
    } // ContactsHelper.insertContact()

    fun updateContact(contact: Contact, origContact: Contact?, photoUpdateStatus: Int): Boolean {
        return insertOrUpdateContact(false, contact, origContact, photoUpdateStatus)
    } // ContactsHelper.updateContact()

    fun insertOrUpdateContact(insert: Boolean, contact: Contact,
                              origContact: Contact?, photoUpdateStatus: Int): Boolean {
        if (insert)
            context.toast(R.string.inserting)
        else
            context.toast(R.string.updating)

        if (contact.isPrivate()) {
            return LocalContactsHelper(context).insertOrUpdateContact(contact)
        }

        try {
            val operations = ArrayList<ContentProviderOperation>()
            var builder: ContentProviderOperation.Builder
            if (insert) {
                builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                builder.apply {
                    withValue(RawContacts.ACCOUNT_NAME, contact.source)
                    withValue(RawContacts.ACCOUNT_TYPE, getContactSourceType(contact.source))
                    operations.add(build())
                }
            }

            // Names
            if (insert) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                builder.apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                }
            } else {
                builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                builder.apply {
                    val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
                    val selectionArgs = arrayOf(contact.id.toString(), contact.mimetype)
                    withSelection(selection, selectionArgs)
                }
            }
            builder.apply {
                withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name.formattedName)
                withValue(CommonDataKinds.StructuredName.PREFIX, contact.name.prefix)
                withValue(CommonDataKinds.StructuredName.GIVEN_NAME, contact.name.givenName)
                withValue(CommonDataKinds.StructuredName.MIDDLE_NAME, contact.name.middleName)
                withValue(CommonDataKinds.StructuredName.FAMILY_NAME, contact.name.familyName)
                withValue(CommonDataKinds.StructuredName.SUFFIX, contact.name.suffix)
                withValue(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, contact.name.phoneticGivenName)
                withValue(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, contact.name.phoneticMiddleName)
                withValue(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, contact.name.phoneticFamilyName)
                operations.add(build())
            }

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            var changed = (!insert && ((origContact == null) || (origContact.nicknames != contact.nicknames)))
            // Delete nicknames
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
            }

            // Add nicknames
            if (changed || insert) {
                contact.nicknames.forEach { nickname ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Nickname.NAME, nickname.name)
                        withValue(CommonDataKinds.Nickname.TYPE, nickname.type)
                        withValue(CommonDataKinds.Nickname.LABEL, nickname.label)
                        operations.add(build())
                    }
                } // forEach(Nickname)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.phoneNumbers != contact.phoneNumbers)))
            // Delete phone numbers
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            }

            // Add phone numbers
            if (changed || insert) {
                contact.phoneNumbers.forEach { phoneNumber ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Phone.NUMBER, phoneNumber.value)
                        withValue(CommonDataKinds.Phone.NORMALIZED_NUMBER, phoneNumber.normalizedNumber)
                        withValue(CommonDataKinds.Phone.TYPE, phoneNumber.type)
                        withValue(CommonDataKinds.Phone.LABEL, phoneNumber.label)
                        withValue(CommonDataKinds.Phone.IS_PRIMARY, phoneNumber.isPrimary)
                        operations.add(build())
                    }
                } // forEach(PhoneNumber)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.emails != contact.emails)))
            // Delete emails
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            }

            // Add emails
            if (changed || insert) {
                contact.emails.forEach { email ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Email.DATA, email.address)
                        withValue(CommonDataKinds.Email.TYPE, email.type)
                        withValue(CommonDataKinds.Email.LABEL, email.label)
                        operations.add(build())
                    }
                } // forEach(Email)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.addresses != contact.addresses)))
            // Delete addresses
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
            }

            // Add addresses
            if (changed || insert) {
                contact.addresses.forEach { address ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address.formattedAddress)
                        withValue(CommonDataKinds.StructuredPostal.TYPE, address.type)
                        withValue(CommonDataKinds.StructuredPostal.LABEL, address.label)
                        withValue(CommonDataKinds.StructuredPostal.STREET, address.street)
                        withValue(CommonDataKinds.StructuredPostal.POBOX, address.postOfficeBox)
                        withValue(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, address.neighborhood)
                        withValue(CommonDataKinds.StructuredPostal.CITY, address.city)
                        withValue(CommonDataKinds.StructuredPostal.REGION, address.region)
                        withValue(CommonDataKinds.StructuredPostal.POSTCODE, address.postalCode)
                        withValue(CommonDataKinds.StructuredPostal.COUNTRY, address.country)
                        operations.add(build())
                    }
                } // forEach(StructuredPostal)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.IMs != contact.IMs)))
            // Delete instant messenger addresses
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Im.CONTENT_ITEM_TYPE)
            }

            // Add instant messenger addresses
            if (changed || insert) {
                contact.IMs.forEach { IM ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Im.DATA, IM.data)
                        withValue(CommonDataKinds.Im.TYPE, IM.type)
                        withValue(CommonDataKinds.Im.LABEL, IM.label)
                        val (protocol, custom_protocol) = getIMAndroidProtocolFromEditProtocol(IM.protocol, IM.custom_protocol)
                        withValue(CommonDataKinds.Im.PROTOCOL, protocol)
                        withValue(CommonDataKinds.Im.CUSTOM_PROTOCOL, custom_protocol)
                        operations.add(build())
                    }
                } // forEach(Im)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.events != contact.events)))
            // Delete events
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
            }

            // Add events
            if (changed || insert) {
                contact.events.forEach { event ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Event.START_DATE, event.startDate)
                        withValue(CommonDataKinds.Event.TYPE, event.type)
                        withValue(CommonDataKinds.Event.LABEL, event.label)
                        operations.add(build())
                    }
                } // forEach(Event)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.notes != contact.notes)))
            // Delete notes
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
            }

            // Add notes
            if (changed || insert) {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    if (insert) {
                        withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    } else {
                        withValue(Data.RAW_CONTACT_ID, contact.id)
                    }
                    withValue(Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Note.NOTE, contact.notes)
                    operations.add(build())
                }
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.organization != contact.organization)))
            // Delete organization
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
            }

            // Add organization
            if (changed || insert) {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    if (insert) {
                        withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    } else {
                        withValue(Data.RAW_CONTACT_ID, contact.id)
                    }
                    withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Organization.COMPANY, contact.organization.company)
                    withValue(CommonDataKinds.Organization.TYPE, contact.organization.type)
                    withValue(CommonDataKinds.Organization.LABEL, contact.organization.label)
                    withValue(CommonDataKinds.Organization.TITLE, contact.organization.jobTitle)
                    withValue(CommonDataKinds.Organization.DEPARTMENT, contact.organization.department)
                    withValue(CommonDataKinds.Organization.JOB_DESCRIPTION, contact.organization.jobDescription)
                    withValue(CommonDataKinds.Organization.SYMBOL, contact.organization.symbol)
                    withValue(CommonDataKinds.Organization.PHONETIC_NAME, contact.organization.phoneticName)
                    withValue(CommonDataKinds.Organization.OFFICE_LOCATION, contact.organization.location)
                    operations.add(build())
                }
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.websites != contact.websites)))
            // Delete websites
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
            }

            // Add websites
            if (changed || insert) {
                contact.websites.forEach { website ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            builder.withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            builder.withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.Website.URL, website.URL)
                        withValue(CommonDataKinds.Website.TYPE, website.type)
                        withValue(CommonDataKinds.Website.LABEL, website.label)
                        operations.add(build())
                    }
                } // forEach(Website)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.relations != contact.relations)))
            // Delete relations
            if (changed) {
                deleteContactContent(operations, contact.id, CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
            }

            // Add relations
            if (changed || insert) {
                contact.relations.forEach { relation ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                        val (type, label) = getRelationAndroidTypeLabelFromEditTypeLabel(relation.type, relation.label)
                        withValue(CommonDataKinds.Relation.NAME, relation.name)
                        withValue(CommonDataKinds.Relation.TYPE, type)
                        withValue(CommonDataKinds.Relation.LABEL, label)
                        operations.add(build())
                    }
                } // forEach(Relation)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            changed = (!insert && ((origContact == null) || (origContact.groups != contact.groups)))
            // Delete groups
            if (changed) {
                val relevantGroupIDs = getStoredGroupsSync().map { it.id }
                if (relevantGroupIDs.isNotEmpty()) {
                    val IDsString = TextUtils.join(",", relevantGroupIDs)
                    ContentProviderOperation.newDelete(Data.CONTENT_URI).apply {
                        val selection = "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = ? AND ${CommonDataKinds.GroupMembership.GROUP_ROW_ID} IN ($IDsString)"
                        val selectionArgs = arrayOf(contact.contactId.toString(), CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                        withSelection(selection, selectionArgs)
                        operations.add(build())
                    }
                }
            }

            // Add groups
            if (changed || insert) {
                contact.groups.forEach { group ->
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                        if (insert) {
                            withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        } else {
                            withValue(Data.RAW_CONTACT_ID, contact.id)
                        }
                        withValue(Data.MIMETYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                        withValue(CommonDataKinds.GroupMembership.GROUP_ROW_ID, group.id)
                        operations.add(build())
                    }
                } // forEach(Group)
            } // if (changed || insert)

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            val contactID: Int
            if (insert) {
                val results = context.contentResolver.applyBatch(AUTHORITY, operations)
                val rawId = ContentUris.parseId(results[0].uri!!)

                // Storing contacts on some devices seems to be messed up and
                // they move on Phone instead, or disappear completely.
                // Try storing a lighter contact version with this oldschool
                // version too just so it wont disappear, future edits work well.
                if (getContactSourceType(contact.source).contains(".sim")) {
                    val simUri = Uri.parse("content://icc/adn")
                    ContentValues().apply {
                        put("number", contact.phoneNumbers.firstOrNull()?.value ?: "")
                        put("tag", contact.getNameToDisplay())
                        context.contentResolver.insert(simUri, this)
                    }
                }

                // Photo (inspired by https://gist.github.com/slightfoot/5985900)
                // Fullsize photo
                // FIXME - Does this really work? This just writes a file to file system.
                // I believe it is necessary to call addPhoto to change the contact database too...
                var fullSizePhotoData: ByteArray? = null
                if (contact.photoUri.isNotEmpty()) {
                    val photoUri = Uri.parse(contact.photoUri)
                    fullSizePhotoData = context.contentResolver.openInputStream(photoUri)?.readBytes()
                    if (fullSizePhotoData != null) {
                        addFullSizePhoto(rawId, fullSizePhotoData)
                    }
                }
                contactID = getRealContactId(rawId)
            } else {
                // Photo
                when (photoUpdateStatus) {
                    PHOTO_ADDED, PHOTO_CHANGED -> addPhoto(contact, operations)
                    PHOTO_REMOVED -> removePhoto(contact, operations)
                }

                context.contentResolver.applyBatch(AUTHORITY, operations)
                contactID = contact.contactId
            }

            // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            // Favorite, Ringtone
            try {
                val uri = Uri.withAppendedPath(Contacts.CONTENT_URI, contactID.toString())
                val contentValues = ContentValues(2)
                contentValues.put(Contacts.STARRED, contact.starred)
                contentValues.put(Contacts.CUSTOM_RINGTONE, contact.ringtone)
                context.contentResolver.update(uri, contentValues, null, null)
            } catch (e: Exception) {
                context.showErrorToast(e)
            }

            return true
        } catch (e: Exception) {
            context.showErrorToast(e)
            return false
        }
    }

    private fun deleteContactContent(operations: ArrayList<ContentProviderOperation>, contactID: Int, itemType: String) {
        ContentProviderOperation.newDelete(Data.CONTENT_URI).apply {
            val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ? "
            val selectionArgs = arrayOf(contactID.toString(), itemType)
            withSelection(selection, selectionArgs)
            operations.add(build())
        }
    }

    private fun addPhoto(contact: Contact, operations: ArrayList<ContentProviderOperation>): ArrayList<ContentProviderOperation> {
        if (contact.photoUri.isNotEmpty()) {
            val photoUri = Uri.parse(contact.photoUri)
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)

            val thumbnailSize = context.getPhotoThumbnailSize()
            val scaledPhoto = Bitmap.createScaledBitmap(bitmap, thumbnailSize, thumbnailSize, false)
            val scaledSizePhotoData = scaledPhoto.getByteArray()
            scaledPhoto.recycle()

            val fullSizePhotoData = bitmap.getByteArray()
            bitmap.recycle()

            ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                withValue(Data.RAW_CONTACT_ID, contact.id)
                withValue(Data.MIMETYPE, CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                withValue(CommonDataKinds.Photo.PHOTO, scaledSizePhotoData)
                operations.add(build())
            }

            addFullSizePhoto(contact.id.toLong(), fullSizePhotoData)
        }
        return operations
    }

    private fun addFullSizePhoto(contactId: Long, fullSizePhotoData: ByteArray) {
        val baseUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, contactId)
        val displayPhotoUri = Uri.withAppendedPath(baseUri, RawContacts.DisplayPhoto.CONTENT_DIRECTORY)
        val fileDescriptor = context.contentResolver.openAssetFileDescriptor(displayPhotoUri, "rw")
        val photoStream = fileDescriptor!!.createOutputStream()
        photoStream.write(fullSizePhotoData)
        photoStream.close()
        fileDescriptor.close()
    }

    private fun removePhoto(contact: Contact, operations: ArrayList<ContentProviderOperation>): ArrayList<ContentProviderOperation> {
        ContentProviderOperation.newDelete(Data.CONTENT_URI).apply {
            val selection = "${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?"
            val selectionArgs = arrayOf(contact.id.toString(), CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
            withSelection(selection, selectionArgs)
            operations.add(build())
        }

        return operations
    }

    fun addContactsToGroup(contacts: ArrayList<Contact>, groupId: Long) {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            contacts.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValue(Data.RAW_CONTACT_ID, it.id)
                    withValue(Data.MIMETYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                    operations.add(build())
                }

                if (operations.size % BATCH_SIZE == 0) {
                    context.contentResolver.applyBatch(AUTHORITY, operations)
                    operations.clear()
                }
            }

            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    fun removeContactsFromGroup(contacts: ArrayList<Contact>, groupId: Long) {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            contacts.forEach {
                ContentProviderOperation.newDelete(Data.CONTENT_URI).apply {
                    val selection = "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = ? AND ${Data.DATA1} = ?"
                    val selectionArgs = arrayOf(it.contactId.toString(), CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, groupId.toString())
                    withSelection(selection, selectionArgs)
                    operations.add(build())
                }

                if (operations.size % BATCH_SIZE == 0) {
                    context.contentResolver.applyBatch(AUTHORITY, operations)
                    operations.clear()
                }
            }
            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }
/* // FIXME DELETEME
    fun Obsolete_insertContact(contact: Contact): Boolean {  // FIXME DELETEME
        if (contact.isPrivate()) {
            return LocalContactsHelper(context).insertOrUpdateContact(contact)
        }

        try {
            val operations = ArrayList<ContentProviderOperation>()
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).apply {
                withValue(RawContacts.ACCOUNT_NAME, contact.source)
                withValue(RawContacts.ACCOUNT_TYPE, getContactSourceType(contact.source))
                operations.add(build())
            }

            // names
            ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                withValueBackReference(Data.RAW_CONTACT_ID, 0)
                withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                withValue(CommonDataKinds.StructuredName.PREFIX, contact.prefix)
                withValue(CommonDataKinds.StructuredName.GIVEN_NAME, contact.firstName)
                withValue(CommonDataKinds.StructuredName.MIDDLE_NAME, contact.middleName)
                withValue(CommonDataKinds.StructuredName.FAMILY_NAME, contact.surname)
                withValue(CommonDataKinds.StructuredName.SUFFIX, contact.suffix)
                operations.add(build())
            }

            // nickname
            ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                withValueBackReference(Data.RAW_CONTACT_ID, 0)
                withValue(Data.MIMETYPE, CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                withValue(CommonDataKinds.Nickname.NAME, contact.nickname)
                operations.add(build())
            }

            // phone numbers
            contact.phoneNumbers.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Phone.NUMBER, it.value)
                    withValue(CommonDataKinds.Phone.NORMALIZED_NUMBER, it.normalizedNumber)
                    withValue(CommonDataKinds.Phone.TYPE, it.type)
                    withValue(CommonDataKinds.Phone.LABEL, it.label)
                    withValue(CommonDataKinds.Phone.IS_PRIMARY, it.isPrimary)
                    operations.add(build())
                }
            }

            // emails
            contact.emails.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Email.DATA, it.value)
                    withValue(CommonDataKinds.Email.TYPE, it.type)
                    withValue(CommonDataKinds.Email.LABEL, it.label)
                    operations.add(build())
                }
            }

            // addresses
            contact.addresses.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, it.value)
                    withValue(CommonDataKinds.StructuredPostal.TYPE, it.type)
                    withValue(CommonDataKinds.StructuredPostal.LABEL, it.label)
                    operations.add(build())
                }
            }

            // IMs
            contact.IMs.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Im.DATA, it.value)
                    withValue(CommonDataKinds.Im.PROTOCOL, it.type)
                    withValue(CommonDataKinds.Im.CUSTOM_PROTOCOL, it.label)
                    operations.add(build())
                }
            }

            // events
            contact.events.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Event.START_DATE, it.value)
                    withValue(CommonDataKinds.Event.TYPE, it.type)
                    operations.add(build())
                }
            }

            // notes
            ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                withValueBackReference(Data.RAW_CONTACT_ID, 0)
                withValue(Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                withValue(CommonDataKinds.Note.NOTE, contact.notes)
                operations.add(build())
            }

            // organization
            if (contact.organization.isNotEmpty()) {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Organization.COMPANY, contact.organization.company)
                    withValue(CommonDataKinds.Organization.TYPE, DEFAULT_ORGANIZATION_TYPE)
                    withValue(CommonDataKinds.Organization.TITLE, contact.organization.jobPosition)
                    withValue(CommonDataKinds.Organization.TYPE, DEFAULT_ORGANIZATION_TYPE)
                    operations.add(build())
                }
            }

            // websites
            contact.websites.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.Website.URL, it)
                    withValue(CommonDataKinds.Website.TYPE, DEFAULT_WEBSITE_TYPE)
                    operations.add(build())
                }
            }

            // groups
            contact.groups.forEach {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).apply {
                    withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    withValue(Data.MIMETYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                    withValue(CommonDataKinds.GroupMembership.GROUP_ROW_ID, it.id)
                    operations.add(build())
                }
            }

            // photo (inspired by https://gist.github.com/slightfoot/5985900)
            var fullSizePhotoData: ByteArray? = null
            if (contact.photoUri.isNotEmpty()) {
                val photoUri = Uri.parse(contact.photoUri)
                fullSizePhotoData = context.contentResolver.openInputStream(photoUri)?.readBytes()
            }

            val results = context.contentResolver.applyBatch(AUTHORITY, operations)

            // storing contacts on some devices seems to be messed up and they move on Phone instead, or disappear completely
            // try storing a lighter contact version with this oldschool version too just so it wont disappear, future edits work well
            if (getContactSourceType(contact.source).contains(".sim")) {
                val simUri = Uri.parse("content://icc/adn")
                ContentValues().apply {
                    put("number", contact.phoneNumbers.firstOrNull()?.value ?: "")
                    put("tag", contact.getNameToDisplay())
                    context.contentResolver.insert(simUri, this)
                }
            }

            // fullsize photo
            val rawId = ContentUris.parseId(results[0].uri!!)
            if (contact.photoUri.isNotEmpty() && fullSizePhotoData != null) {
                addFullSizePhoto(rawId, fullSizePhotoData)
            }

            // favorite, ringtone
            val userId = getRealContactId(rawId)
            if (userId != 0) {
                val uri = Uri.withAppendedPath(Contacts.CONTENT_URI, userId.toString())
                val contentValues = ContentValues(2)
                contentValues.put(Contacts.STARRED, contact.starred)
                contentValues.put(Contacts.CUSTOM_RINGTONE, contact.ringtone)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            return true
        } catch (e: Exception) {
            context.showErrorToast(e)
            return false
        }
    }
*/
    fun getContactMimeTypeId(contactId: String, mimeType: String): String {
        val uri = Data.CONTENT_URI
        val projection = arrayOf(Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE)
        val selection = "${Data.MIMETYPE} = ? AND ${Data.RAW_CONTACT_ID} = ?"
        val selectionArgs = arrayOf(mimeType, contactId)


        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(Data._ID)
            }
        }
        return ""
    }

    fun addFavorites(contacts: ArrayList<Contact>) {
        ensureBackgroundThread {
            toggleLocalFavorites(contacts, true)
            if (context.hasContactPermissions()) {
                toggleFavorites(contacts, true)
            }
        }
    }

    fun removeFavorites(contacts: ArrayList<Contact>) {
        ensureBackgroundThread {
            toggleLocalFavorites(contacts, false)
            if (context.hasContactPermissions()) {
                toggleFavorites(contacts, false)
            }
        }
    }

    private fun toggleFavorites(contacts: ArrayList<Contact>, addToFavorites: Boolean) {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            contacts.filter { !it.isPrivate() }.map { it.contactId.toString() }.forEach {
                val uri = Uri.withAppendedPath(Contacts.CONTENT_URI, it)
                ContentProviderOperation.newUpdate(uri).apply {
                    withValue(Contacts.STARRED, if (addToFavorites) 1 else 0)
                    operations.add(build())
                }

                if (operations.size % BATCH_SIZE == 0) {
                    context.contentResolver.applyBatch(AUTHORITY, operations)
                    operations.clear()
                }
            }
            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    private fun toggleLocalFavorites(contacts: ArrayList<Contact>, addToFavorites: Boolean) {
        val localContacts = contacts.filter { it.isPrivate() }.map { it.id }.toTypedArray()
        LocalContactsHelper(context).toggleFavorites(localContacts, addToFavorites)
    }

    fun updateRingtone(contactId: String, newUri: String) {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            val uri = Uri.withAppendedPath(Contacts.CONTENT_URI, contactId)
            ContentProviderOperation.newUpdate(uri).apply {
                withValue(Contacts.CUSTOM_RINGTONE, newUri)
                operations.add(build())
            }

            context.contentResolver.applyBatch(AUTHORITY, operations)
        } catch (e: Exception) {
            context.showErrorToast(e)
        }
    }

    fun deleteContact(originalContact: Contact, deleteClones: Boolean = false, callback: (success: Boolean) -> Unit) {
        ensureBackgroundThread {
            if (deleteClones) {
                getDuplicatesOfContact(originalContact, true) { contacts ->
                    ensureBackgroundThread {
                        if (deleteContacts(contacts)) {
                            callback(true)
                        }
                    }
                }
            } else {
                if (deleteContacts(arrayListOf(originalContact))) {
                    callback(true)
                }
            }
        }
    }

    fun deleteContacts(contacts: ArrayList<Contact>): Boolean {
        val localContacts = contacts.filter { it.isPrivate() }.map { it.id.toLong() }.toMutableList()
        LocalContactsHelper(context).deleteContactIds(localContacts)

        return try {
            val operations = ArrayList<ContentProviderOperation>()
            val selection = "${RawContacts._ID} = ?"
            contacts.filter { !it.isPrivate() }.forEach {
                ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).apply {
                    val selectionArgs = arrayOf(it.id.toString())
                    withSelection(selection, selectionArgs)
                    operations.add(build())
                }

                if (operations.size % BATCH_SIZE == 0) {
                    context.contentResolver.applyBatch(AUTHORITY, operations)
                    operations.clear()
                }
            }

            if (context.hasPermission(PERMISSION_WRITE_CONTACTS)) {
                context.contentResolver.applyBatch(AUTHORITY, operations)
            }
            true
        } catch (e: Exception) {
            context.showErrorToast(e)
            false
        }
    }

    fun getDuplicatesOfContact(contact: Contact, addOriginal: Boolean, callback: (ArrayList<Contact>) -> Unit) {
        ensureBackgroundThread {
            getContacts(true, true) { contacts ->
                val duplicates =
                    contacts.filter { it.id != contact.id && it.getHashToCompare() == contact.getHashToCompare() }.toMutableList() as ArrayList<Contact>
                if (addOriginal) {
                    duplicates.add(contact)
                }
                callback(duplicates)
            }
        }
    }
}
