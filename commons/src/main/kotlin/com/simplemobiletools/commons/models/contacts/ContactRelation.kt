/* *********************************************************************
 *                                                                     *
 *                          ContactRelation.kt                         *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * ContactRelation is a Kotlin class designed to store information about
 * a relations between the person in a contact and other persons.
 *
 * The stored information consists of
 *   .) the name of the other person
 *   .) standardized relation information (e.g. FATHER, DAUGHTER, FRIEND)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for relations:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Relation
 * The supported data fields are:
 *     Relation.NAME         (= ContactsContract.DataColumns.DATA1)
 *     Relation.TYPE         (= ContactsContract.DataColumns.DATA2)
 *     Relation.LABEL        (= ContactsContract.DataColumns.DATA3) (Description if (TYPE == TYPE_CUSTOM))
 *
 * Note: The 'type' field in the ContactRelation is an integer, when it
 * actually should be an enum. This is due to the fact that Android also
 * uses plain integers to store the Relation.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM      (= 0)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_ASSISTANT   (= 1)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER     (= 2)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_CHILD       (= 3)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_DOMESTIC_PARTNER (= 4)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_FATHER      (= 5)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND      (= 6)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_MANAGER     (= 7)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER      (= 8)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_PARENT      (= 9)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_PARTNER     (=10)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_REFERRED_BY (=11)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_RELATIVE    (=12)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_SISTER      (=13)
 *    ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE      (=14)
 *    (requires: import android.provider.ContactsContract)
 *
 * The structure of relations between persons is also reflected in the
 *   vCard 4.0 standard. See: https://www.rfc-editor.org/rfc/rfc6350#section-6.6.6
 *     Section 6.6.6. - vCard Item "RELATED"
 *        To specify a relationship between another entity and the
 *        entity represented by this vCard..
 *   vCard supports the following kinds of relations:
 *        "contact" / "acquaintance" / "friend" / "met" / "co-worker" /
 *        "colleague" / "co-resident" / "neighbor" / "child" / "parent" /
 *        "sibling" / "spouse" / "kin" / "muse" / "crush" / "date" /
 *        "sweetheart" / "me" / "agent" / "emergency"
 *   Thus while there are some types of relations that are common to Android
 *   and vCard there is no 1-to-1 mapping. Additionally vCard permits URLs
 *   as target information (e.g. links to vCards of the target persons),
 *   while Android expects a plain name. All programs that try to synchronize
 *   Android contacts via CardDAV will run in interesting transcoding issues...
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class ContactRelation(var name: String, var type: Int, var label: String) {
    fun deepCopy(): ContactRelation = ContactRelation(name, type, label)
    companion object {
        /**
         * Relation Types:
         *
         * Android defines only a few types of 'standard'-relations, that
         * are just copied to TYPE_* #1..14. Most 'advanced' types of
         * relations must be handled using TYPE_CUSTOM.
         *
         * vCard defines some more types (TYPE_* #51..66 plus some duplicates
         * from Android), but these too show wide gaps. Note that vCard
         * does NOT support a 'TYPE_CUSTOM' equivalent. Thus all types
         * that are not in the vCard standard must somehow be squeezed
         * into these types when exporting an Android contact to vCard/CardDAV.
         *
         * For our purposes we shall define many more types (in particular
         * family relations and some business hierarchy). These types are
         * neither part of the standard Android types of relations nor
         * part of the vCard standard. Thus we can not store these types
         * directly to Android or vCard. For Android, we can use the escape
         * route via TYPE_CUSTOM and LABEL. For vCard we need to coerce
         * these types to standard vCard types, generally under a severe
         * loss of precision (e.g. 'grandfather', 'brother-in-law' and 'aunt'
         * all map to 'kin').
         *
         * Note: These constants are defined as plain integers, rather
         * than as enum, since the Android type field is also an integer.
         */
        // Relation types defined in Android: ContactContract.Relation
        const val TYPE_CUSTOM: Int = 0
        const val TYPE_ASSISTANT: Int = 1
        const val TYPE_BROTHER: Int = 2
        const val TYPE_CHILD: Int = 3
        const val TYPE_DOMESTIC_PARTNER = 4
        const val TYPE_FATHER: Int = 5
        const val TYPE_FRIEND: Int = 6
        const val TYPE_MANAGER: Int = 7
        const val TYPE_MOTHER: Int = 8
        const val TYPE_PARENT: Int = 9
        const val TYPE_PARTNER: Int = 10
        const val TYPE_REFERRED_BY: Int = 11
        const val TYPE_RELATIVE: Int = 12
        const val TYPE_SISTER: Int = 13
        const val TYPE_SPOUSE: Int = 14

        // Relation types defined in vCard 4.0
        const val TYPE_CONTACT: Int = 51
        const val TYPE_ACQUAINTANCE: Int = 52
        // const val TYPE_FRIEND: Int = 6
        const val TYPE_MET: Int = 53
        const val TYPE_CO_WORKER: Int = 54
        const val TYPE_COLLEAGUE: Int = 55
        const val TYPE_CO_RESIDENT: Int = 56
        const val TYPE_NEIGHBOR: Int = 57
        // const val TYPE_CHILD: Int = 3
        // const val TYPE_PARENT: Int = 9
        const val TYPE_SIBLING: Int = 58
        // const val TYPE_SPOUSE: Int = 14
        const val TYPE_KIN: Int = 59
        const val TYPE_MUSE: Int = 60
        const val TYPE_CRUSH: Int = 61
        const val TYPE_DATE: Int = 62
        const val TYPE_SWEETHEART: Int = 63
        const val TYPE_ME: Int = 64
        const val TYPE_AGENT: Int = 65
        const val TYPE_EMERGENCY: Int = 66

        // Additional custom types
        const val TYPE_SUPERIOR: Int = 101
        const val TYPE_SUBORDINATE: Int = 102
        const val TYPE_HUSBAND: Int = 103
        const val TYPE_WIFE: Int = 104
        const val TYPE_SON: Int = 105
        const val TYPE_DAUGHTER: Int = 106
        const val TYPE_GRANDPARENT: Int = 107
        const val TYPE_GRANDFATHER: Int = 108
        const val TYPE_GRANDMOTHER: Int = 109
        const val TYPE_GRANDCHILD: Int = 110
        const val TYPE_GRANDSON: Int = 111
        const val TYPE_GRANDDAUGHTER: Int = 112
        const val TYPE_UNCLE: Int = 113
        const val TYPE_AUNT: Int = 114
        const val TYPE_NEPHEW: Int = 115
        const val TYPE_NIECE: Int = 116
        const val TYPE_FATHER_IN_LAW: Int = 117
        const val TYPE_MOTHER_IN_LAW: Int = 118
        const val TYPE_SON_IN_LAW: Int = 119
        const val TYPE_DAUGHTER_IN_LAW: Int = 120
        const val TYPE_BROTHER_IN_LAW: Int = 121
        const val TYPE_SISTER_IN_LAW: Int = 122
    } // companion object
} // class ContactRelation
