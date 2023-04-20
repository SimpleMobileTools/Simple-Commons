/* *********************************************************************
 *                                                                     *
 *                            ContactName.kt                           *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * ContactName is a Kotlin class designed to store the name of a person.
 *
 * Such a name will generally consist (in western societies) of:
 *   .) a prefix - e.g. Mr, Ms, Dr., Sir
 *   .) a given name (= first name) - e.g. Alan
 *   .) optional middle names (= additional given names) - e.g. Mathison
 *   .) a family name (= last name) - e.g. Turing
 *   .) a suffix - e.g. Sr, Jr, PhD, MBA, OBE
 *
 * These components can be arranged to form a formatted name. How such an
 * arrangement shall be performed depends on the active/local language
 * and/or on the purpose that the formatted name shall be used for.
 * For example many people in the US abbreviate their middle name and
 * just use the middle initial, while Germans generally don't use their
 * middle name at all when forming the formatted name. Sometime a nickname is
 * added when forming the formatted name or a family relation is used as a
 * formatted name (e.g. Mom/Dad).
 * Thus the formatted name can not easily be obtained using an algorithm and
 * an explicit "formatted name" field is part of PersonalName.
 *
 * If the person that is referred to in the contact comes from a country with
 * a foreign alphabet that is not readable for the user of the contact list,
 * a transliteration to the local alphabet will come handy. Thus the given,
 * middle and family names can also be stored in a phonetic version.
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for names:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.StructuredName
 * Among the supported data fields are:
 *     StructuredName.DISPLAY_NAME (= ContactsContract.DataColumns.DATA1)
 *     StructuredName.GIVEN_NAME   (= ContactsContract.DataColumns.DATA2)
 *     StructuredName.FAMILY_NAME  (= ContactsContract.DataColumns.DATA3)
 *     StructuredName.PREFIX       (= ContactsContract.DataColumns.DATA4)
 *     StructuredName.MIDDLE_NAME  (= ContactsContract.DataColumns.DATA5)
 *     StructuredName.SUFFIX       (= ContactsContract.DataColumns.DATA6)
 *     StructuredName.PHONETIC_GIVEN_NAME   (= ContactsContract.DataColumns.DATA7)
 *     StructuredName.PHONETIC_MIDDLE_NAME  (= ContactsContract.DataColumns.DATA8)
 *     StructuredName.PHONETIC_FAMILY_NAME  (= ContactsContract.DataColumns.DATA9)
 *
 * This structure of personal names is also reflected in the vCard standard.
 *   vCard 4.0: See https://www.rfc-editor.org/rfc/rfc6350#section-6.2.1
 *     Section 6.2.1 - vCard Item "FN" (FormattedName) -
 *             Specify the formatted text corresponding to the name
 *     Section 6.2.2 - vCard Item "N" (Name) -
 *             The structured property value corresponds, in sequence,
 *             to the Family Names (also known as surnames), Given Names,
 *             Additional Names, Honorific Prefixes, and Honorific Suffixes.
 *   Note that there is no phonetic name in vCard.
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

import com.simplemobiletools.commons.extensions.normalizeString

// *********************************************************************

/**
 * Select a way to create a complete formatted name from a structured name
 */
enum class ContactNameFormat {
    // NAMEFORMAT_DEFAULT,            // As specified in the PersonalName companion class
    NAMEFORMAT_FORMATTED_NAME,       // FormattedName
    NAMEFORMAT_FAMILY_GIVEN,         // Family, Given
    NAMEFORMAT_FAMILY_GIVEN_M,       // Family, Given M.
    NAMEFORMAT_FAMILY_GIVEN_MIDDLE,  // Family, Given Middle
    NAMEFORMAT_FAMILY_MIDDLE_GIVEN,  // Family Middle Given - Chinese/Japanese!
    NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX, // Family, Prefix Given Middle, Suffix
    NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX, // Family Given Middle Prefix Suffix  -- Used for Sorting only!
    NAMEFORMAT_GIVEN_FAMILY,         // Given Family
    NAMEFORMAT_GIVEN_M_FAMILY,       // Given M. Family
    NAMEFORMAT_GIVEN_MIDDLE_FAMILY,  // Given Middle Family
    NAMEFORMAT_GIVEN_FAMILY_MIDDLE,  // Given Family Middle
    NAMEFORMAT_PREFIX_GIVEN_MIDDLE_FAMILY_SUFFIX, // Prefix Given Middle Family, Suffix
    NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX; // Given Middle Family Prefix Suffix -- Used for Sorting only!

    fun startsWithFamilyName(): Boolean {
        return((this >= NAMEFORMAT_FAMILY_GIVEN) &&
            (this <= NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX))
/*
        return((this == NAMEFORMAT_FAMILY_GIVEN) ||
               (this == NAMEFORMAT_FAMILY_GIVEN_M) ||
               (this == NAMEFORMAT_FAMILY_GIVEN_MIDDLE) ||
               (this == NAMEFORMAT_FAMILY_MIDDLE_GIVEN) ||
               (this == NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX) ||
               (this == NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX))
*/
    } // ContactNameFormat.startsWithFamilyName()
} // enum class ContactNameFormat

// *********************************************************************

enum class ContactNameSortBy {
    NAMESORTBY_DISPLAY_NAME,
    NAMESORTBY_FORMATTED_NAME,
    NAMESORTBY_GIVEN_NAME,
    NAMESORTBY_MIDDLE_NAME,
    NAMESORTBY_FAMILY_NAME,
    NAMESORTBY_CONTACT_ID
} // enum class ContactNameSortBy

// *********************************************************************

data class ContactName(var formattedName: String,
                       var prefix: String,
                       var givenName: String, var middleName: String, var familyName: String,
                       var suffix: String,
                       var phoneticGivenName: String, var phoneticMiddleName: String, var phoneticFamilyName: String) {

    // *****************************************************************

    fun deepCopy(): ContactName = ContactName(formattedName,
        prefix, givenName, middleName, familyName, suffix,
        phoneticGivenName, phoneticMiddleName, phoneticFamilyName)

    // *****************************************************************

    companion object {
        // val EMPTY_NAME = ContactName("", "", "", "", "", "", "", "","")
        fun getEmptyName() = ContactName("",
            "", "", "", "", "",
            "", "","")
        // val EMPTY_NAME = ContactName("", "", "", "", "", "", "", "","")

        fun compareNameStrings(x: String, y:String) : Int {
            // Empty strings are sorted behind non-empty strings
            if (x.isEmpty()) {
                return if (y.isEmpty())
                    (0)  // X and Y are both empty -> equal -> 0
                else
                    (1)  // X is empty, but Y is not empty -> X is sorted behind Y -> +1
            }
            // We now know that X is not empty!
            if (y.isEmpty())
                return(-1)     // Y is empty, but X is not empty -> X is sorted before Y -> -1

            // We now know that X and Y are both not empty!
            // Letters are sorted before numbers/symbols
            if (x[0].isLetter() != y[0].isLetter()) {
                return if (x[0].isLetter())
                    (-1)  // X starts with a letter, while Y starts with a number or symbol -> X is sorted before Y -> -1
                else
                    (1)   // X starts with a number or symbol, while Y starts with a letter -> X is sorted behind Y -> +1
            }

            val xx = x.normalizeString()
            val yy = y.normalizeString()
            return(xx.compareTo(yy, true))
        } // ContactName.compareNameStrings()

    } // companion object

    // *****************************************************************

    fun isCoreSegmEmpty(): Boolean {
        return (givenName.isEmpty() && middleName.isEmpty() && familyName.isEmpty())
    } // ContactName.isCoreSegmEmpty()

    // *****************************************************************

    fun isEmpty(): Boolean {
        return (formattedName.isEmpty() && prefix.isEmpty() &&
            givenName.isEmpty() && middleName.isEmpty() && familyName.isEmpty() &&
            suffix.isEmpty())
    } // ContactName.isEmpty()

    // *****************************************************************
    // A ContactName contains two copies of the name information: On the
    // one hand there is the preformatted name that has possibly be optimized
    // by hand (augmented with nicknames, family relation etc) and on the
    // other hand there is a structured name that can be used to assemble
    // a name suitable for display.
    // Both copies may or may not contain information and we need to deal
    // with situations where one copy, or possibly even both copies (in case
    // of organisations) are empty.
    // Thus getDisplayName() will first select which source for name data
    // is preferred and try to use that. If the preferred source is empty,
    // it will automatically use the other source. If that is empty too,
    // we can only report an empty string...
    // If getDisplayName() tries to deliver the display name based on the
    // structured name data, it uses displayNameFormat to select the rules
    // for assembling a display name.
    fun getDisplayName(showFormattedName: Boolean,
                       nameFormat: ContactNameFormat): String {
        if (showFormattedName && formattedName.isNotEmpty())
            return(formattedName)

        val builtDisplayName = buildDisplayName(nameFormat)
        if (builtDisplayName.isNotEmpty())
            return(builtDisplayName)
        else
            return(formattedName)
    } // ContactName.getDisplayName()

    // *****************************************************************

    fun getHashCode(): Int {
        return(calcHashCode())
    } // ContactName.getHashCode()

    // *****************************************************************

    fun getSortString(format: ContactNameFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX): String {
        val startWithFamilyName: Boolean = format.startsWithFamilyName()
        var sortString: String = ""

        if ((startWithFamilyName) && (familyName.isNotEmpty()))
            sortString = "$familyName "
        if (givenName.isNotEmpty())
            sortString += "$givenName "
        // Note: If we are sorting by given name, and the given names
        // are equal, do we want to proceed with the family names
        // or check middle names first?
        if ((!startWithFamilyName) && (familyName.isNotEmpty()))
            sortString += "$familyName "
        if (middleName.isNotEmpty())
            sortString += "$middleName "
        // if ((!startWithFamilyName) && (familyName.isNotEmpty()))
        //    sortStringCache += "$familyName "
        if (prefix.isNotEmpty())
            sortString += "$prefix "
        if (suffix.isNotEmpty())
            sortString += suffix
        sortString = sortString.trim().normalizeString()

        return(sortString)
    } // ContactName.getSortString()

    // *****************************************************************

    fun getNameForLetterPlaceholder(startWithFamilyName: Boolean): String {
        val gotGivenName = givenName.isNotEmpty()
        val gotFamilyName = familyName.isNotEmpty()

        if (gotFamilyName && gotGivenName) {
            if (startWithFamilyName) {
                return familyName[0].toString() //  + givenName[0].toString()
            } else {
                return givenName[0].toString() //  + familyName[0].toString()
            }
        } else if (gotFamilyName) {
            return familyName[0].toString()
        } else if (gotGivenName) {
            return givenName[0].toString()
        } else if (formattedName.isNotEmpty()) {
            return formattedName[0].toString()
        } else
            return ""
    } // ContactName.getNameForLetterPlaceholder()

    // *****************************************************************

    fun getNameForShortcutList(showFormattedName: Boolean, startWithFamilyName: Boolean): String {
        var C: Char = '*'
        if (showFormattedName && formattedName.isNotEmpty())
            C = formattedName[0]
        if (startWithFamilyName && familyName.isNotEmpty())
            C = familyName[0]
        else if (!startWithFamilyName && givenName.isNotEmpty())
            C = givenName[0]
        else
            return("")

        return(C.toString().uppercase().normalizeString())
    } // ContactName.getNameForLetterPlaceholder()

    // *****************************************************************

    fun update(formattedName : String? = null,
               prefix : String? = null,
               givenName : String? = null, middleName : String? = null, familyName : String? = null,
               suffix : String? = null) {

        if (formattedName != null)
            this.formattedName = formattedName.trim()

        if (prefix != null)
            this.prefix = prefix.trim()

        if (givenName != null)
            this.givenName = givenName.trim()

        if (middleName != null)
            this.middleName = middleName.trim()

        if (familyName != null)
            this.familyName = familyName.trim()

        if (suffix != null)
            this.suffix = suffix.trim()
    } // ContactName.update()

    // *****************************************************************

    fun buildDisplayName(nameFormat: ContactNameFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN,
                         separator: String = ",") : String {
        var usedFormat : ContactNameFormat = nameFormat

        // if (usedFormat == PersonalNameFormat.NAMEFORMAT_DEFAULT)
        //     usedFormat = defaultNameFormat

        if ((usedFormat <= ContactNameFormat.NAMEFORMAT_FORMATTED_NAME) ||
            (usedFormat > ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX))
            usedFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN

        val format : String = when(usedFormat) {
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN         -> "fG"       // Family, Given
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_M       -> "fGm"      // Family, Given M.
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE  -> "fGM"      // Family, Given Middle
            ContactNameFormat.NAMEFORMAT_FAMILY_MIDDLE_GIVEN  -> "FMG"      // Family Middle Given - Chinese/Japanese!
            ContactNameFormat.NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX -> "fPGMs" // Family, Prefix Given Middle, Suffix
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX -> "FGMPS"  // Family Given Middle Prefix Suffix  -- Used for Sorting only!
            ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY         -> "GF"       // Given Family
            ContactNameFormat.NAMEFORMAT_GIVEN_M_FAMILY       -> "GmF"      // Given M. Family
            ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY  -> "GMF"      // Given Middle Family
            ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY_MIDDLE  -> "GFM"      // Given Family Middle
            ContactNameFormat.NAMEFORMAT_PREFIX_GIVEN_MIDDLE_FAMILY_SUFFIX -> "PGMFs" // Prefix Given Middle Family, Suffix
            ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX -> "GMFPS" // Given Middle Family Prefix Suffix -- Used for Sorting only!
            else -> { "fG" }
        }
        val formatLen = format.length
        var pos = 0
        var formattedName : String = ""
        var currSegm : String = ""
        var prevSep : String = ""
        var nextSep : String

        while (pos < formatLen) {
            nextSep = ""
            when(format[pos]) {
                'F' -> { currSegm = familyName }   // Family name
                'f' -> { currSegm = familyName; nextSep = separator }  // Family name with trailing comma separator
                'M' -> { currSegm = middleName }   // Middle name
                'm' -> { currSegm = if (middleName.isNotEmpty()) (middleName[0] + ".") else "" }   // Middle initial
                'G' -> { currSegm = givenName }    // Given name
                'P' -> { currSegm = prefix }       // Prefix
                'S' -> { currSegm = suffix }       // Suffix
                's' -> { currSegm = suffix; prevSep = separator } // Suffix with leading comma separator
                else -> { currSegm = "" }
            }

            if (currSegm.isNotEmpty()) {
                if (formattedName.isNotEmpty())
                    formattedName = "$formattedName$prevSep $currSegm"
                else
                    formattedName = currSegm
                prevSep = nextSep
            }

            pos++
        }

        return(formattedName)
    } // ContactName.buildFormattedName()

    // *****************************************************************

    fun buildPhoneticName(nameFormat: ContactNameFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN,
                          separator: String = ",") : String {
        var usedFormat : ContactNameFormat = nameFormat

        // if (usedFormat == PersonalNameFormat.NAMEFORMAT_DEFAULT)
        //     usedFormat = defaultNameFormat

        if ((usedFormat <= ContactNameFormat.NAMEFORMAT_FORMATTED_NAME) ||
            (usedFormat > ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX))
            usedFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN

        val format : String = when(usedFormat) {
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN         -> "fG"       // Family, Given
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_M       -> "fGm"      // Family, Given M.
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE  -> "fGM"      // Family, Given Middle
            ContactNameFormat.NAMEFORMAT_FAMILY_MIDDLE_GIVEN  -> "FMG"      // Family Middle Given - Chinese/Japanese!
            ContactNameFormat.NAMEFORMAT_FAMILY_PREFIX_GIVEN_MIDDLE_SUFFIX -> "fGM" // Family, Given Middle
            ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN_MIDDLE_PREFIX_SUFFIX -> "FGM"  // Family Given Middle
            ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY         -> "GF"       // Given Family
            ContactNameFormat.NAMEFORMAT_GIVEN_M_FAMILY       -> "GmF"      // Given M. Family
            ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY  -> "GMF"      // Given Middle Family
            ContactNameFormat.NAMEFORMAT_GIVEN_FAMILY_MIDDLE  -> "GFM"      // Given Family Middle
            ContactNameFormat.NAMEFORMAT_PREFIX_GIVEN_MIDDLE_FAMILY_SUFFIX -> "GMF" // Prefix Given Middle
            ContactNameFormat.NAMEFORMAT_GIVEN_MIDDLE_FAMILY_PREFIX_SUFFIX -> "GMF" // Given Middle Family
            else -> { "fG" }
        }
        val formatLen = format.length
        var pos = 0
        var formattedName : String = ""
        var currSegm : String = ""
        var prevSep : String = ""
        var nextSep : String

        while (pos < formatLen) {
            nextSep = ""
            when(format[pos]) {
                'F' -> { currSegm = phoneticFamilyName }   // Family name
                'f' -> { currSegm = phoneticFamilyName; nextSep = separator }  // Family name with trailing comma separator
                'm', 'M' -> { currSegm = phoneticMiddleName }   // Middle name
                // 'm' -> { currSegm = if (middleName.isNotEmpty()) (middleName[0] + ".") else "" }   // Middle initial
                'G' -> { currSegm = phoneticGivenName }    // Given name
                // 'P' -> { currSegm = prefix }       // Prefix
                // 'S' -> { currSegm = suffix }       // Suffix
                // 's' -> { currSegm = suffix; prevSep = separator } // Suffix with leading comma separator
                else -> { currSegm = "" }
            }

            if (currSegm.isNotEmpty()) {
                if (formattedName.isNotEmpty())
                    formattedName = "$formattedName$prevSep $currSegm"
                else
                    formattedName = currSegm
                prevSep = nextSep
            }

            pos++
        }

        return(formattedName)
    } // ContactName.buildPhoneticName()

    // *****************************************************************

    private fun calcHashCode() : Int {
        val summary = ">$formattedName< - >$prefix< >$givenName< >$middleName< >$familyName< >$suffix< - >$phoneticGivenName< >$phoneticMiddleName< >$phoneticFamilyName<"
        return(summary.hashCode())
    } // ContactName.calcHashCode()

    // *****************************************************************

    fun compareTo(other: ContactName, sortBy: ContactNameSortBy = ContactNameSortBy.NAMESORTBY_FAMILY_NAME,
                  showFormattedName: Boolean = true,
                  nameFormat: ContactNameFormat = ContactNameFormat.NAMEFORMAT_FAMILY_GIVEN): Int {
        var result: Int

        result = when (sortBy) {
            ContactNameSortBy.NAMESORTBY_DISPLAY_NAME -> compareNameStrings(this.getDisplayName(showFormattedName, nameFormat),
                other.getDisplayName(showFormattedName, nameFormat))
            ContactNameSortBy.NAMESORTBY_FORMATTED_NAME -> compareNameStrings(this.formattedName, other.formattedName)
            ContactNameSortBy.NAMESORTBY_GIVEN_NAME -> compareNameStrings(this.givenName, other.givenName)
            ContactNameSortBy.NAMESORTBY_MIDDLE_NAME -> compareNameStrings(this.middleName, other.middleName)
            ContactNameSortBy.NAMESORTBY_FAMILY_NAME -> compareNameStrings(this.familyName, other.familyName)
            // ContactNameSortBy.NAMESORTBY_CONTACT_ID -> compareNameStrings(this.familyName, other.familyName)
            else -> 0
        }
        if (result != 0)
            return(result)

        if (sortBy != ContactNameSortBy.NAMESORTBY_FAMILY_NAME) {
            result = compareNameStrings(this.familyName, other.familyName)
            if (result != 0)
                return(result)
        }

        if (sortBy != ContactNameSortBy.NAMESORTBY_GIVEN_NAME) {
            result = compareNameStrings(this.givenName, other.givenName)
            if (result != 0)
                return(result)
        }

        if (sortBy != ContactNameSortBy.NAMESORTBY_MIDDLE_NAME) {
            result = compareNameStrings(this.middleName, other.middleName)
            if (result != 0)
                return(result)
        }

        if (sortBy != ContactNameSortBy.NAMESORTBY_FORMATTED_NAME) {
            result = compareNameStrings(this.formattedName, other.formattedName)
            if (result != 0)
                return(result)
        }

        result = compareNameStrings(this.prefix, other.prefix)
        if (result != 0)
            return(result)

        result = compareNameStrings(this.suffix, other.suffix)
        return(result)
    } // ContactName.compareTo()

} // data class ContactName

/* *********************************************************************
 *                            ContactName.kt                           *
 ***********************************************************************/
