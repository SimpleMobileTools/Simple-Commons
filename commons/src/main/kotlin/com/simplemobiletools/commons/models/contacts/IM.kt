/* *********************************************************************
 *                                                                     *
 *                      IM.kt (Instant Messenger)                      *
 *                                                                     *
 ***********************************************************************
 * 321098765432109876543210987654321+123456789012345678901234567890123 *
 *
 * This file is part of "Simple Mobile Tools"
 *    https://www.simplemobiletools.com/
 *    https://github.com/SimpleMobileTools
 */
/**
 * IM.kt is a Kotlin class designed to store information about an instant
 * messenger (IM) address.
 *
 * The stored information consists of
 *   .) the actual instant messenger address (e.g. john.doe@example.com)
 *   .) the type of instant messenger address (e.g. WORK, HOME, OTHER)
 *   .) a label containing additional type information if the
 *      standardized type is CUSTOM (and an empty string otherwise)
 *   .) the type of instant messenger protocol (e.g. MATRIX, XMPP, SKYPE, CUSTOM)
 *   .) a custom protocol containing additional type information if the
 *      standardized protocol is CUSTOM (and an empty string otherwise)
 *
 * The Android Contacts Provider system for managing contact information
 * also has a similar structure for emails:
 *     See: https://developer.android.com/guide/topics/providers/contacts-provider
 *          https://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Im
 * Among the supported data fields are:
 *     Im.DATA            (= ContactsContract.DataColumns.DATA1)
 *     Im.TYPE            (= ContactsContract.DataColumns.DATA2)
 *     Im.LABEL           (= ContactsContract.DataColumns.DATA3)
 *     Im.PROTOCOL        (= ContactsContract.DataColumns.DATA5)
 *     Im.CUSTOM_PROTOCOL (= ContactsContract.DataColumns.DATA6)
 * Note: ContactsContract.DataColumns.DATA4 is not used!
 *
 * Note: The 'type' field in the Instant Messenger is an integer,
 * when it actually should be an enum. This is due to the fact that
 * Android also uses plain integers to store the Im.TYPE field.
 * Available values are:
 *    ContactsContract.CommonDataKinds.Im.TYPE_HOME   (= 1)
 *    ContactsContract.CommonDataKinds.Im.TYPE_WORK   (= 2)
 *    ContactsContract.CommonDataKinds.Im.TYPE_OTHER  (= 3)
 *    (requires: import android.provider.ContactsContract)
 *
 * The Im.Protocol field used to contain an integer specified the
 * instant messenger protocol to use. This field has been deprecated
 * in Android API level 31 (probably the number of instant messengers
 * protocols that appeared (and disappeared) on the net could simply
 * be no longer tracked in a reliable fashion by a fixed API field).
 * Thus the currently recommended Im.PROTOCOL is just:
 *    ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM  (= -1)
 * The following protocols are still available, but deprecated:
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM    (= 0) - AOL Instant Messenger
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN    (= 1) - Microsoft Network - Windows Live
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO  (= 2) - Yahoo
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE  (= 3) - Microsoft Skype
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ     (= 4) - Tencent QQ (Asia)
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK (= 5) - Hangouts - Google Chats
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ    (= 6) - "I Seek You" (AOL -> Mail.RU)
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER (= 7) - Jabber/XMPP
 *     ContactsContract.CommonDataKinds.Im.PROTOCOL_NETMEETING (= 8) - Microsoft
 *
 * This structure of instant messaging and presence protocol addresses
 * is also reflected in the vCard 4.0 standard.
 *     See: https://www.rfc-editor.org/rfc/rfc6350#section-6.4.3
 *     Section 6.4.3 - vCard Item "IMPP"
 *        To specify the URI for instant messaging and presence protocol
 *        (IMPP) communications (e.g. [matrix], XMPP/Jabber, Skype, etc.)
 *     Note: The protocol of the used instant messenger is encoded in
 *     the protocol part of the URI.
 *      e.g. matrix:u/john.doe:example.org          - [matrix]
 *           xmpp:john.doe@example.org              - XMPP/Jabber
 *           (jabber:john.doe@example.org           - XMPP/Jabber - deprecated?)
 *           irc://irc.example.org/john.doe,isuser  - Internet Relay Chat
 *           irc6://irc.example.org/john.doe,isuser - Internet Relay Chat over IPv6
 *           ircs://irc.example.org/john.doe,isuser - Internet Relay Chat Secure
 *           sip:1-555-123-4567@voip.example.org    - Voice-over-IP
 *           skype:johndoe                          - Microsoft Skype
 *           mumble://john.doe@example.org          - Mumble
 *
 **********************************************************************/

package com.simplemobiletools.commons.models.contacts

data class IM(var data: String, var type: Int, var label: String,
              var protocol: Int, var custom_protocol: String) : Cloneable {
    fun deepCopy(): IM = IM (data, type, label, protocol, custom_protocol)

    companion object {
        const val PROTOCOL_CUSTOM: Int = -1

        // The following protocols are/were defined by Android
        // They have been deprecated in API 31 (Android 12 "Snow Cone" - 2021)
        // It is recommended that only protocol PROTOCOL_CUSTOM is used and
        // the actual protocol is stored as text in CUSTOM_PROTOCOL. That way
        // android does not need to keep track which protocols come and go.
        const val PROTOCOL_AIM: Int = 0  // AOL Instant Messenger - https://www.aol.com
        const val PROTOCOL_MSN: Int = 1  // Windows Live - https://outlook.live.com/
        const val PROTOCOL_YAHOO: Int = 2  // Yahoo - https://www.yahoo.com/
        const val PROTOCOL_SKYPE: Int = 3  // Microsoft Skype - URI Format: "skype:john_doe"
        const val PROTOCOL_QQ: Int = 4  // Tencent QQ (Asia) - https://im.qq.com
        const val PROTOCOL_GOOGLE_TALK: Int = 5  //  -> Google Hangouts -> Google Chats
        const val PROTOCOL_ICQ: Int = 6  // "I Seek You" (AOL -> Mail.RU) - https://icq.com
        const val PROTOCOL_JABBER: Int = 7  //  Jabber/XMPP - https://xmpp.org/ - URI Format: "xmpp:john_doe@example.org"
        const val PROTOCOL_NETMEETING: Int = 8  //  Microsoft NetMeeting (Discontinued)

        // The following protocols are NOT defined by Android. We shall still
        // define protocol IDs for them, so that we can identify them in dialogs.
        const val PROTOCOL_SIP: Int = 101        // Session Initiation Protocol - URI Format: "sip:1-555-123-4567@voip.example.org"
        const val PROTOCOL_IRC: Int = 102        // Internet Relay Chat - URI Format: "ircs://irc.example.org/john_doe\,isuser"

        const val PROTOCOL_MATRIX: Int = 103     // https://www.matrix.org - URI Format: "matrix:u/john_doe:example.org"
        const val PROTOCOL_MASTODON: Int = 104   // https://mastodon.social
        const val PROTOCOL_SIGNAL: Int = 105     // https://www.signal.org
        const val PROTOCOL_TELEGRAM: Int = 106   // https://telegram.org
        const val PROTOCOL_DIASPORA: Int = 107   // https://diasporafoundation.org/
        const val PROTOCOL_VIBER: Int = 108      // https://www.viber.com
        const val PROTOCOL_THREEMA: Int = 109    // https://threema.ch
        const val PROTOCOL_DISCORD: Int = 110    // https://discord.com
        const val PROTOCOL_MUMBLE: Int = 111     // https://www.mumble.info
        const val PROTOCOL_OLVID: Int = 112      // https://olvid.io
        const val PROTOCOL_TEAMSPEAK: Int = 113  // https://teamspeak.com
        const val PROTOCOL_FACEBOOK: Int = 114   // https://www.facebook.com   - URI Format: "https://www.facebook.com/JohnDoe" or @JohnDoe
        const val PROTOCOL_INSTAGRAM: Int = 115  // https://www.instagram.com  - URI Format: "https://www.instagram.com/JohnDoe" or @JohnDoe
        const val PROTOCOL_WHATSAPP: Int = 116   // https://www.whatsapp.com   - URI Format: "https://wa.me/15551234567 (see: https://faq.whatsapp.com/425247423114725)
        const val PROTOCOL_TWITTER: Int = 117    // https://twitter.com        - URI Format: "https://twitter.com/JohnDoe"
        const val PROTOCOL_WECHAT: Int = 118     // https://www.wechat.com/
        const val PROTOCOL_WEIBO: Int = 119      // https://weibo.com
        const val PROTOCOL_TIKTOK: Int = 120     // https://www.tiktok.com
        const val PROTOCOL_TUMBLR: Int = 121     // https://www.tumblr.com
        const val PROTOCOL_FLICKR: Int = 122     // https://www.flickr.com
        const val PROTOCOL_LINKEDIN: Int = 123   // https://www.linkedin.com
        const val PROTOCOL_XING: Int = 124       // https://www.xing.com
        const val PROTOCOL_KIK: Int = 125        // https://www.kik.com
        const val PROTOCOL_LINE: Int = 126       // https://line.me/de/
        const val PROTOCOL_KAKAOTALK: Int = 127  // https://www.kakaocorp.com/service/KakaoTalk
        const val PROTOCOL_ZOOM: Int = 128       // https://zoom.us/
        const val PROTOCOL_GITHUB: Int = 129     // https://github.com/
        const val PROTOCOL_GOOGLEPLUS: Int = 130 // https://plus.google.com
        const val PROTOCOL_PINTEREST: Int = 131  // https://www.pinterest.com/
        // const val PROTOCOL_QZONE: Int = 132 - See: PROTOCOL_QQ // https://qzone.qq.com
        const val PROTOCOL_YOUTUBE: Int = 133    // https://www.youtube.com/
        const val PROTOCOL_SNAPCHAT: Int = 134   // https://www.snapchat.com
        const val PROTOCOL_TEAMS: Int = 135      // https://teams.microsoft.com/
        const val PROTOCOL_GOOGLEMEET: Int = 136      // https://workspace.google.com
        const val PROTOCOL_TEAMVIEWERMEET: Int = 137  // https://www.teamviewer.com
        const val PROTOCOL_NEXTCLOUDTALK: Int = 138   // https://nextcloud.com
        const val PROTOCOL_SLACK: Int = 139           // https://slack.com
        const val PROTOCOL_JITSI: Int = 140           // https://jitsi.org
        const val PROTOCOL_WEBEX: Int = 141           // https://www.webex.com
        const val PROTOCOL_GOTOMEETING: Int = 141     // https://www.goto.com
        const val PROTOCOL_BIGBLUEBUTTON: Int = 142   // https://bigbluebutton.org
    } // companion object
} // data class IM

/* Legacy definition of IM (Instant Messenger):
 *
 * Before April 2023, only the data, protocol and custom_protocol values
 * of an IM were stored as part of a LocalContact. While we are now using
 * the modern IM format, that supports all the fields of Android ContactContract
 * we still need to be able to read the old format, when a user updates
 * from an old version of SimpleContacts.
 *
 * See: models/contacts/LocalContact.tk
 *      helpers/Converters.tk
 */
data class IMConverter(var value: String, var type: Int, var label: String)

/* *********************************************************************
 *                      IM.kt (Instant Messenger)                      *
 ***********************************************************************/

