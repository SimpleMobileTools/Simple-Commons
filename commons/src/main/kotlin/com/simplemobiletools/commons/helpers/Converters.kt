package com.simplemobiletools.commons.helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.models.PhoneNumber
import com.simplemobiletools.commons.models.contacts.*

class Converters {
    private val gson = Gson()
    private val longType = object : TypeToken<List<Long>>() {}.type
    private val stringType = object : TypeToken<List<String>>() {}.type
    private val nicknameType = object : TypeToken<List<ContactNickname>>() {}.type
    private val numberType = object : TypeToken<List<PhoneNumber>>() {}.type
    private val numberConverterType = object : TypeToken<List<PhoneNumberConverter>>() {}.type
    private val emailType = object : TypeToken<List<Email>>() {}.type
    private val emailConverterType = object : TypeToken<List<EmailConverter>>() {}.type
    private val addressType = object : TypeToken<List<Address>>() {}.type
    private val addressConverterType = object : TypeToken<List<AddressConverter>>() {}.type
    private val imType = object : TypeToken<List<IM>>() {}.type
    private val imConverterType = object : TypeToken<List<IMConverter>>() {}.type
    private val eventType = object : TypeToken<List<Event>>() {}.type
    private val eventConverterType = object : TypeToken<List<EventConverter>>() {}.type
    private val organizationType = object : TypeToken<Organization>() {}.type
    private val organizationConverterType = object : TypeToken<OrganizationConverter>() {}.type
    private val websiteType = object : TypeToken<List<ContactWebsite>>() {}.type
    private val relationType = object : TypeToken<List<ContactRelation>>() {}.type

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToStringList(value: String) = gson.fromJson<ArrayList<String>>(value, stringType)

    @TypeConverter
    fun stringListToJson(list: ArrayList<String>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToLongList(value: String) = gson.fromJson<ArrayList<Long>>(value, longType)

    @TypeConverter
    fun longListToJson(list: ArrayList<Long>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToNicknameList(value: String): ArrayList<ContactNickname> {
        var nicknames = gson.fromJson<ArrayList<ContactNickname>>(value, nicknameType)
        if (nicknames == null) {
            nicknames = ArrayList()
            val nickname = value
            if ((nickname != null) && nickname.isNotEmpty())
                nicknames.add(ContactNickname(nickname, DEFAULT_NICKNAME_TYPE, ""))
        }
        return(nicknames)
    }

    @TypeConverter
    fun nicknameListToJson(list: ArrayList<ContactNickname>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    // some hacky converting is needed since PhoneNumber model has been added to proguard rules, but obfuscated json was stored in database
    // convert [{"a":"678910","b":2,"c":"","d":"678910","e":false}] to PhoneNumber(value=678910, type=2, label=, normalizedNumber=678910, isPrimary=false)
    @TypeConverter
    fun jsonToPhoneNumberList(value: String): ArrayList<PhoneNumber> {
        val numbers = gson.fromJson<ArrayList<PhoneNumber>>(value, numberType)
        return if (numbers.any { it.value == null }) {
            val phoneNumbers = ArrayList<PhoneNumber>()
            val numberConverters = gson.fromJson<ArrayList<PhoneNumberConverter>>(value, numberConverterType)
            numberConverters.forEach { converter ->
                val phoneNumber = PhoneNumber(converter.a, converter.b, converter.c, converter.d, converter.e)
                phoneNumbers.add(phoneNumber)
            }
            phoneNumbers
        } else {
            numbers
        }
    }

    @TypeConverter
    fun phoneNumberListToJson(list: ArrayList<PhoneNumber>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToEmailList(value: String): ArrayList<Email> {
        var emails = gson.fromJson<ArrayList<Email>>(value, emailType)
        if (emails == null) {
            emails = ArrayList()
            val emailConverters = gson.fromJson<ArrayList<EmailConverter>>(value, emailConverterType)
            emailConverters.forEach { converter ->
                val email = Email(converter.value, converter.type, converter.label)
                emails.add(email)
            }
        }
        return(emails)
    }

    @TypeConverter
    fun emailListToJson(list: ArrayList<Email>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToAddressList(value: String): ArrayList<Address> {
        var addresses = gson.fromJson<ArrayList<Address>>(value, addressType)
        if (addresses == null) {
            addresses = ArrayList()
            val addressConverters = gson.fromJson<ArrayList<AddressConverter>>(value, addressConverterType)
            addressConverters.forEach { converter ->
                val address = Address(converter.value,
                    converter.value, "", "", "", "" ,"", "",
                    converter.type, converter.label)
                addresses.add(address)
            }
        }
        return(addresses)
    }
    @TypeConverter
    fun addressListToJson(list: ArrayList<Address>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToIMsList(value: String): ArrayList<IM> {
        var IMs = gson.fromJson<ArrayList<IM>>(value, imType)
        if (IMs == null) {
            IMs = ArrayList()
            val IMConverters = gson.fromJson<ArrayList<IMConverter>>(value, imConverterType)
            IMConverters.forEach { converter ->
                val im = IM(converter.value, DEFAULT_IM_TYPE, "",
                            converter.type, converter.label)
                IMs.add(im)
            }
        }
        return(IMs)
    }

    @TypeConverter
    fun IMsListToJson(list: ArrayList<IM>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToEventList(value: String): ArrayList<Event> {
        var events = gson.fromJson<ArrayList<Event>>(value, eventType)
        if (events == null) {
            events = ArrayList()
            val eventConverters = gson.fromJson<ArrayList<EventConverter>>(value, eventConverterType)
            eventConverters.forEach { converter ->
                val event = Event(converter.value, converter.type, "")
                events.add(event)
            }
        }
        return(events)
    }

    @TypeConverter
    fun eventListToJson(list: ArrayList<Event>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToOrganisation(value: String): Organization {
        var organization = gson.fromJson<Organization>(value, organizationType)
        if (organization == null) {
            val converter = gson.fromJson<OrganizationConverter>(value, organizationConverterType)
            organization = Organization(converter.company, converter.jobTitle,
                "", "", "", "", "", DEFAULT_ORGANIZATION_TYPE, "")
        }
        return(organization)
    }

    @TypeConverter
    fun organisationToJson(data: Organization) = gson.toJson(data)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToWebsiteList(value: String): ArrayList<ContactWebsite> {
        var websites = gson.fromJson<ArrayList<ContactWebsite>>(value, websiteType)
        if (websites == null) {
            websites = ArrayList()
            val websiteConverters = gson.fromJson<ArrayList<String>>(value, stringType)
            websiteConverters.forEach { converter ->
                val website = ContactWebsite(converter, DEFAULT_WEBSITE_TYPE, "")
                websites.add(website)
            }
        }
        return(websites)
    }

    @TypeConverter
    fun websiteListToJson(list: ArrayList<ContactWebsite>) = gson.toJson(list)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @TypeConverter
    fun jsonToRelationList(value: String): ArrayList<ContactRelation> {
        return (gson.fromJson<ArrayList<ContactRelation>>(value, relationType) ?: ArrayList())
    }

    @TypeConverter
    fun relationListToJson(list: ArrayList<ContactRelation>) = gson.toJson(list)
}
