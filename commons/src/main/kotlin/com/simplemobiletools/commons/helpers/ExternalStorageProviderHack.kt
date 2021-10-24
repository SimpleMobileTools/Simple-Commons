package com.simplemobiletools.commons.helpers

import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.DocumentsContract
import com.simplemobiletools.commons.extensions.getStringValue

// On Android 11, ExternalStorageProvider no longer returns Android/data and Android/obb as children
// of the Android directory on primary storage. However, the two child directories are actually
// still accessible.
// https://github.com/zhanghai/MaterialFiles/blob/master/app/src/main/java/me/zhanghai/android/files/provider/document/resolver/ExternalStorageProviderPrimaryAndroidDataHack.kt
object ExternalStorageProviderHack {
    private const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DOCUMENT_ID = "primary:Android"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID =
        "primary:Android/data"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DISPLAY_NAME = "data"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID =
        "primary:Android/obb"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DISPLAY_NAME = "obb"

    private val CHILD_DOCUMENTS_CURSOR_COLUMN_NAMES = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_SIZE,
    )

    fun transformQueryResult(uri: Uri, cursor: Cursor): Cursor {
        val documentId = DocumentsContract.getDocumentId(uri)
        if (uri.authority == EXTERNAL_STORAGE_PROVIDER_AUTHORITY && documentId == EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DOCUMENT_ID) {
            var hasDataRow = false
            var hasObbRow = false
            try {
                while (cursor.moveToNext()) {
                    when (cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)) {
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID ->
                            hasDataRow = true
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID ->
                            hasObbRow = true
                    }
                    if (hasDataRow && hasObbRow) {
                        break
                    }
                }
            } finally {
                cursor.moveToPosition(-1)
            }
            if (hasDataRow && hasObbRow) {
                return cursor
            }
            val extraCursor = MatrixCursor(CHILD_DOCUMENTS_CURSOR_COLUMN_NAMES)
            if (!hasDataRow) {
                extraCursor.newRow()
                    .add(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DISPLAY_NAME
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.MIME_TYPE_DIR
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                       System.currentTimeMillis()
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_SIZE,
                        0L
                    )
            }
            if (!hasObbRow) {
                extraCursor.newRow()
                    .add(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DISPLAY_NAME
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                        DocumentsContract.Document.MIME_TYPE_DIR
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                        System.currentTimeMillis()
                    )
                    .add(
                        DocumentsContract.Document.COLUMN_SIZE,
                        0L
                    )
            }
            return MergeCursor(arrayOf(cursor, extraCursor))
        }
        return cursor
    }
}
