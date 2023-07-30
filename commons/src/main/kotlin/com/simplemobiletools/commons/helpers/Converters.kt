package com.simplemobiletools.commons.helpers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.models.PhoneNumber
import com.simplemobiletools.commons.models.contacts.*
import java.util.ArrayList

class Converters {
    private val gson = Gson()
    private val longType = object : TypeToken<List<Long>>() {}.type
    private val stringType = object : TypeToken<List<String>>() {}.type
    private val numberType = object : TypeToken<List<PhoneNumber>>() {}.type
    private val numberConverterType = object : TypeToken<List<PhoneNumberConverter>>() {}.type
    private val emailType = object : TypeToken<List<Email>>() {}.type
    private val addressType = object : TypeToken<List<Address>>() {}.type
    private val eventType = object : TypeToken<List<Event>>() {}.type
    private val imType = object : TypeToken<List<IM>>() {}.type

    @TypeConverter
    fun jsonToStringList(value: String): ArrayList<String> = gson.fromJson(value, stringType)

    @TypeConverter
    fun stringListToJson(list: ArrayList<String>): String = gson.toJson(list)

    @TypeConverter
    fun jsonToLongList(value: String): ArrayList<Long> = gson.fromJson(value, longType)

    @TypeConverter
    fun longListToJson(list: ArrayList<Long>): String = gson.toJson(list)

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
    fun phoneNumberListToJson(list: ArrayList<PhoneNumber>): String = gson.toJson(list)

    @TypeConverter
    fun jsonToEmailList(value: String): ArrayList<Email> = gson.fromJson(value, emailType)

    @TypeConverter
    fun emailListToJson(list: ArrayList<Email>): String = gson.toJson(list)

    @TypeConverter
    fun jsonToAddressList(value: String): ArrayList<Address> = gson.fromJson(value, addressType)

    @TypeConverter
    fun addressListToJson(list: ArrayList<Address>): String = gson.toJson(list)

    @TypeConverter
    fun jsonToEventList(value: String): ArrayList<Event> = gson.fromJson(value, eventType)

    @TypeConverter
    fun eventListToJson(list: ArrayList<Event>): String = gson.toJson(list)

    @TypeConverter
    fun jsonToIMsList(value: String): ArrayList<IM> = gson.fromJson(value, imType)

    @TypeConverter
    fun iMsListToJson(list: ArrayList<IM>): String = gson.toJson(list)
}
