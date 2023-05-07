package com.simplemobiletools.commons.models.contacts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simplemobiletools.commons.models.PhoneNumber

// When changing the definition of LocalContact always think of incrementing
// the version number in ContactDatabase.kt and adding a suitable converter!!
// To have a look at the SQL code that the compiler generates for the
// DAO (Database Access Object) check out:
// ./app/build/generated/source/kapt/coreDebug/com/simplemobiletools/contacts/pro/databases/ContactsDatabase_Impl.java
// FIXME - Should we rename firstName and surname to givenName and familyName
//    (Variables only - Not columns!!) to match the naming conventions of Android?
// We already renamed company -> organization
@Entity(tableName = "contacts", indices = [(Index(value = ["id"], unique = true))])
data class LocalContact(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "display_name") var displayName: String,
    @ColumnInfo(name = "prefix") var prefix: String,
    @ColumnInfo(name = "first_name") var firstName: String,
    @ColumnInfo(name = "middle_name") var middleName: String,
    @ColumnInfo(name = "surname") var surname: String,
    @ColumnInfo(name = "suffix") var suffix: String,
    @ColumnInfo(name = "phonetic_given_name") var phoneticGivenName: String,
    @ColumnInfo(name = "phonetic_middle_name") var phoneticMiddleName: String,
    @ColumnInfo(name = "phonetic_family_name") var phoneticFamilyName: String,
    @ColumnInfo(name = "nickname") var nicknames: ArrayList<ContactNickname>,
    @ColumnInfo(name = "phone_numbers") var phoneNumbers: ArrayList<PhoneNumber>,
    @ColumnInfo(name = "emails") var emails: ArrayList<Email>,
    @ColumnInfo(name = "addresses") var addresses: ArrayList<Address>,
    @ColumnInfo(name = "ims") var IMs: ArrayList<IM>,
    @ColumnInfo(name = "events") var events: ArrayList<Event>,
    @ColumnInfo(name = "notes") var notes: String,
    @ColumnInfo(name = "company") var organization: Organization,
    @ColumnInfo(name = "job_position") var jobPosition: String,  // Obsolete! - Just for Conversion from old format!
    @ColumnInfo(name = "websites") var websites: ArrayList<ContactWebsite>,
    @ColumnInfo(name = "relations") var relations: ArrayList<ContactRelation>,
    @ColumnInfo(name = "groups") var groups: ArrayList<Long>,
    @ColumnInfo(name = "photo", typeAffinity = ColumnInfo.BLOB) var photo: ByteArray?,
    @ColumnInfo(name = "photo_uri") var photoUri: String,
    @ColumnInfo(name = "starred") var starred: Int,
    @ColumnInfo(name = "ringtone") var ringtone: String?
) {
    override fun equals(other: Any?) = id == (other as? LocalContact?)?.id

    override fun hashCode() = id ?: 0
}
