/* *********************************************************************
 *                                                                     *
 *                              Address.kt                             *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * Address is a Kotlin class designed to store a postal/delivery address
 *
 * Such a name will generally consist of:
 *   .) a street address
 *   .) a city
 *   .) a province/state
 *   .) a postal code
 *   .) a country
 *
 * Sometimes it considered useful to have
 *   .) an extended street address (e.g. House name, apartment)
 *   .) neighborhood information (to distinguish streets with the
 *         same name in different districts of the same town)
 *   .) a dedicated place to specify a post office box
 * While these extra fields are often supported by address management
 * classes (like this one) their actual usefulness is questionable.
 * Usually the contents of these fields could just as well be written
 * into the street address field.
 *
 * Given the five 'core' fields it is possible to create a formatted
 * address that can be printed on the address label of a letter. However
 * the rules the proper arrangement of fields within the formatted address
 * depend on country that the address belongs to.
 * Since some programmers don't want to keep track of all these rules
 * some address management classes add a distinct "formatted address"
 * field, where the complete address (correctly formatted by a human)
 * can be stored. Obviously storing the address information twice (once
 * structured and once formatted) is a ready source of inconsistency
 * when an address management application updates one field but not the
 * other.
 *
 * Side note: Keeping track of the proper rules for address formatting
 * is quite tricky, since there are lots of countries around the world
 * and lots of rules have to be collected/maintained. Also selecting
 * the correct rule is tricky since the country field might be empty or
 * might contain the name of the country in the local language of the
 * mobile phone user or in the language (one of the languages) of the
 * country in question. Many countries have an 'official' full name
 * (generally in multiple languages for external usage) that can be
 * used in official documents, but that is hardly used in day-to-day
 * addressing where phrases like "republic of" are generally dropped.
 * Sometime country names are abbreviated and the 2-letter country code
 * of ISO 3166 are used (ccTLD) or just a single letter is used when the
 * remaining context is clear.
 * e.g. Austria could be Republik Österreich, Österreich, Österr., Ö,
 * Austria, Republic of Austria, Autriche, République d'Autriche,
 * República de Austria, AT (ISO 3166), AUT (sports code) and many,
 * many more choices (plus spelling errors) that make it tricky to
 * select the right address format rules.
 * If an application want to select formatting rules based on the
 * target country it will probably need to require that the user
 * does not freely enter any text in the country field, but pick
 * an entry from a predefined list of countries of the world.
 *
 * Many application will forego the task of picking the correct address
 * format for a given address and will instead format all addresses
 * with a single standard format (derived from the locale selected for
 * the application (or even worse, just one global format selected by
 * the application programmer))
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for addresses:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.StructuredPostal
 * Among the supported data fields are:
 *     StructuredPostal.FORMATTED_ADDRESS (= ContactsContract.DataColumns.DATA1)
 *     StructuredPostal.TYPE     (= ContactsContract.DataColumns.DATA2) (e.g. CUSTOM/HOME/WORK/OTHER)
 *     StructuredPostal.LABEL    (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *     StructuredPostal.STREET   (= ContactsContract.DataColumns.DATA4)
 *     StructuredPostal.POBOX    (= ContactsContract.DataColumns.DATA5)
 *     StructuredPostal.NEIGHBORHOOD (= ContactsContract.DataColumns.DATA6)
 *     StructuredPostal.CITY     (= ContactsContract.DataColumns.DATA7)
 *     StructuredPostal.REGION   (= ContactsContract.DataColumns.DATA8)
 *     StructuredPostal.POSTCODE (= ContactsContract.DataColumns.DATA9)
 *     StructuredPostal.COUNTRY  (= ContactsContract.DataColumns.DATA10)
 *
 * Note: The 'type' field in the Address is an integer, when it actually
 * should be an enum. This is due to the fact that Android also uses
 * plain integers to store the StructuredPostal.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM (= 0)
 *    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME   (= 1)
 *    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK   (= 2)
 *    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER  (= 3)
 *    (requires: import android.provider.ContactsContract)
 *
 * This structure of addresses is also reflected in the vCard standard.
 *   vCard 4.0: See https://www.rfc-editor.org/rfc/rfc6350#section-6.3.1
 *     Section 6.3.1 - vCard Item "ADR" (Address) -
 *             Specify the components of the delivery address
 *     The structured type value corresponds, in sequence, to
 *        .) the post office box
 *        .) the extended address (e.g., apartment or suite number)
 *        .) the street address
 *        .) the locality (e.g., city)
 *        .) the region (e.g., state or province)
 *        .) the postal code
 *        .) the country name
 *     While all these components exist in a vCard ADR item, experience
 *     with vCard 3.0 implementations has shown that there are interoperability
 *     issues with the "post office box" and "extended address" fields
 *     and it is recommended that these field should remain empty.
 *     Note that there is no 'official' "formatted address" field in the
 *     vCard standard. Applications using vCard are expected to create
 *     their own properly formatted address from the structured address
 *     fields.
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

import android.provider.ContactsContract
import android.util.Log

data class Address (var formattedAddress: String,
                    var street: String, var postOfficeBox: String, var neighborhood: String,
                    var city: String, var region: String, var postalCode: String,
                    var country: String, var type: Int, var label: String) {

    // *****************************************************************

    fun deepCopy(): Address = Address(formattedAddress,
                   street, postOfficeBox, neighborhood,
                   city, region, postalCode, country, type, label)

    // *****************************************************************

    companion object {
        // Alternative Address Type: ContactsContract.CommonDataKinds.StructuredPostal
        // Data Fields:
        // %A Street
        // %D Neighborhood (should be empty according to https://www.rfc-editor.org/rfc/rfc6350#section-6.3.1)
        // %B POBox        (should be empty according to https://www.rfc-editor.org/rfc/rfc6350#section-6.3.1)
        // %C City
        // %S State/Region
        // %Z PostalCode
        // %N Nation/Country
        // %% %
        // %n NewLine

        // Austrian Address Format: ("%A%n%D%n%B%n{%Z}[ ]{%C}[ ]{(%S)}%n%N%n")
        //   Street
        //   Neighborhood
        //   POBox
        //   PostalCode City (Region)
        //   Country
        // const val AddressFormatAustria = "%A%n%D%n%B%n{%Z}[ ]{%C}[ ]{(%S)}%n%N%n"
        // const val AddressFormatAustria = "%A%n%D%n%B%n{%Z}[ ]{%C}%n%S%n%N%n" - Alternative with Region in a separate line

        // US Style Address Format
        //   Street
        //   Neighborhood
        //   POBox
        //   City, State PostalCode
        //   Country
        // const val AddressFormatUSA = "%A%n%D%n%B%n{%C}[, ]{%S}[ ]{%Z}%n%N%n"

        const val AddressFormatAustria = "%A%n%D%n%B%n{%Z}[ ]{%C}[ ]{(%S)}%n%N%n"
        // const val AddressFormatAustria = "%A%n%D%n%B%n{%Z}[ ]{%C}%n%S%n%N%n" - Alternative with Region in a separate line
        const val AddressFormatUSA = "%A%n%D%n%B%n{%C}[, ]{%S}[ ]{%Z}%n%N%n"
        var defaultAddressFormat = AddressFormatUSA

        fun setAddressFormat(format : String) {
            if (format.trim().isNotEmpty())
                defaultAddressFormat = format.trim()
            else
                defaultAddressFormat = AddressFormatUSA
        } /* Address.setAddressFormat() */

        // *************************************************************
        /**
         *  A simple demo to show of the functions of getFormattedPostalAddress
         *
         *  This is just for demonstation purposes and should not be part
         *  of release code...
         */
        /*
        fun formattedAddressDemo() {
            val AustrianAddress: Address = Address(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME, "", "",
                "Karlsplatz 13", "", "", "Wien", "", "1040", "Österreich")
            val USAddress: Address = Address(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME, "", "",
                "51 Prospect Street", "", "", "New Haven", "CT", "06511", "USA")
            var formattedAddress: String
            formattedAddress = AustrianAddress.getFormattedPostalAddress(AddressFormatAustria)
            Log.d("Austria A", formattedAddress + "\n")
            AustrianAddress.region = "Wieden"
            formattedAddress = AustrianAddress.getFormattedPostalAddress(AddressFormatAustria)
            Log.d("Austria B", formattedAddress + "\n")
            AustrianAddress.postalCode = ""
            formattedAddress = AustrianAddress.getFormattedPostalAddress(AddressFormatAustria)
            Log.d("Austria C", formattedAddress + "\n")

            formattedAddress = USAddress.getFormattedPostalAddress(AddressFormatUSA)
            Log.d("USA A", formattedAddress + "\n")
            USAddress.region = ""
            formattedAddress = USAddress.getFormattedPostalAddress(AddressFormatUSA)
            Log.d("USA B", formattedAddress + "\n")
        } /* Address.formattedAddressDemo() */
        */
    } /* companion object */

    // *****************************************************************

    /**
     *  Clear the POBox and Neighborhood fields in an address
     *
     *  The vCard 4.0 Standard (RFC 6350 - https://www.rfc-editor.org/rfc/rfc6350)
     *  states that the POBox and extended Address/Neighborhood fields are plagued
     *  with compatibility issues and recommends that these fields should be left
     *  empty (Section 6.3.1).
     *
     *  We can enforce this recommendation and copy any data in these fields to
     *  the Street field (and follow the example of the address book of Thunderbird).
     */
    fun clearObsoleteFields() {
        if (postOfficeBox.isEmpty() && neighborhood.isEmpty())
            return
        val itemSeparator = "\n"  // Possible alternative: ", "

        val extStreet =
            if (street.isNotEmpty() && neighborhood.isNotEmpty())
                "$street$itemSeparator$neighborhood"
            else if (street.isNotEmpty())
                street
            else if (neighborhood.isNotEmpty())
                neighborhood
            else
                ""

        street =
            if (postOfficeBox.isNotEmpty() && extStreet.isNotEmpty())
                "$postOfficeBox$itemSeparator$extStreet"
            else if (postOfficeBox.isNotEmpty())
                postOfficeBox
            else
                extStreet

        postOfficeBox = ""
        neighborhood = ""
    } /* Address.clearObsoleteFields() */

    // *****************************************************************

    /**
     *  Build a (human-readable) formatted address from a structured address
     *
     *  The format of a 'correctly' formatted postal address varies a
     *  lot from one country to the next. getFormattedPostalAddress() will
     *  try to create a nice formatted address given a structured address
     *  and a format string describing the customary order of address fields
     *  for a country.
     *
     *  The format string can contain 'ordinary' characters that are simply
     *  copied 1-to-1 from the format string to the formatted address as
     *  well as escape sequences that are replaced with appropriate contents.
     *  Recognized escape sequences are:
     *
     *    '%A' -> Street
     *    '%D' -> Neighborhood
     *    '%B' -> PostOfficeBox
     *    '%C' -> City
     *    '%S' -> Region/State
     *    '%Z' -> PostalCode
     *    '%N' -> Country
     *    '%n' -> Newline
     *    '%%' -> the '%' character
     *    '%{', '%}', '%[' and '%]' -> '{', '}', '[' and ']'
     *
     *  If a given line of the formatted address shall contains just a
     *  single item (e.g. the street), it can be specified as e.g. "%A%n".
     *  If the given item is empty in the source data, the line will be
     *  skipped (including the newline).
     *
     *  If a line contains several data items (e.g. city, state and postal code)
     *  it is possible to specify data segments in curly brackets and have
     *  separators specified in square brackets in between. A data segment
     *  is considered valid, if it contains at least one address item that
     *  actually contains text. Invalid data segments are discarded (even if
     *  they contain plain characters directly copied from the format string).
     *  Separators are considered valid if there was a previous valid data
     *  segment and the following data segment is valid. Invalid separators
     *  are discarded.
     *
     *  Thus e.g. "{%C}[, ]{%S}[ ]{%Z}%n" will specify a line with the city,
     *  state and zip code of a US address.
     *
     *  If we have an address with city, state and zip code this will
     *  result in e.g. "New Haven, CT 06511". If the city is missing we
     *  will get "CT 06511", and if the state is missing we will get
     *  "New Haven 06511". Note that the first separator was dropped and
     *  the second separator made it to the formatted string, because
     *  the third data segment was valid and thus the separator directly
     *  before that was used.
     */
    fun getFormattedPostalAddress(addressFormat: String) : String {
        var addrFormat = addressFormat
        var addrFormatLen = addrFormat.length
        var pos = 0
        var formattedAddr = ""
        var prevSegment = ""
        var currSegment = ""
        var segmentValid = false
        var segmentActive = false
        var separator = ""
        var separatorActive = false
        var line = ""
        var data : String
        var C : Char
        var prev = ' '

        if (addressFormat.isEmpty()) {
            addrFormat = defaultAddressFormat
            addrFormatLen = addrFormat.length
        }

        while (pos < addrFormatLen) {
            var useAddressSegment = false
            data = ""
            C = addrFormat[pos]
            if (C == '[') {
                separator = ""
                separatorActive = true
            }
            else if (C == ']') {
                separatorActive = false
            }
            else if (C == '{') {
                currSegment = ""
                segmentActive = true
                segmentValid = false
                separatorActive = false
            }
            else if (C == '}') {
                if (segmentValid && currSegment.isNotEmpty()) {
                    if (prevSegment.isNotEmpty() && separator.isNotEmpty()) {
                        line = line + separator + currSegment
                        prevSegment = currSegment
                        currSegment = ""
                        separator = ""
                    } else {
                        line = line + currSegment
                        prevSegment = currSegment
                        currSegment = ""
                        separator = ""
                    }
                } else
                    prev = ' '
                segmentActive = false
                segmentValid = false
            }
            else if (C == '%') {
                pos++;
                C = addrFormat[pos]
                useAddressSegment = true
                when (C) {
                    'n' -> {
                        line = line.trim()
                        if (line.length > 0) {
                            formattedAddr = formattedAddr + line + "\n"
                            line = ""
                        }
                        data = ""
                        useAddressSegment = false
                    }
                    'A' -> data = street
                    'D' -> data = neighborhood
                    'B' -> data = postOfficeBox
                    'C' -> data = city
                    'S' -> data = region
                    'Z' -> data = postalCode
                    'N' -> data = country
                    // '%' -> data = "%"
                    else  -> { data = C.toString(); useAddressSegment = false }
                }
                if (useAddressSegment)
                    data = data.trim()
            }
            else if ((C == ' ') && (prev == ' ')) {
                // No Action - Collect multiple spaces into one
            }
            else {
                data = C.toString()
            }

            if (data.length > 0) {
                if (segmentActive) {
                    currSegment = currSegment + data
                    segmentValid = segmentValid || useAddressSegment
                }
                else if (separatorActive)
                    separator = separator + data
                else
                    line = line + data

                prev = data[data.length -1]
            } else
                prev = C

            pos++
        } /* while (pos < addrFormatLen) */
        return(formattedAddr.trim())
    } /* Address.getFormattedPostalAddress() */
} /* data class Address */

/* Legacy definition of Address:
 *
 * Before April 2023, only the street address, type and label values of
 * an address were stored as part of a LocalContact. While we are now using
 * the modern address format, that supports all the fields of Android
 * ContactContract we still need to be able to read the old format, when
 * a user updates from an old version of SimpleContacts.
 *
 * See: models/contacts/LocalContact.tk
 *      helpers/Converters.tk
 */
data class AddressConverter(var value: String, var type: Int, var label: String)

/* *********************************************************************
 *                              Address.kt                             *
 ***********************************************************************/
