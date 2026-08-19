package com.sap.codelab.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by M.Çağatay
 * Created on 18.08.2026
 *
 * Handles the database migration required after changing
 * reminderLatitude and reminderLongitude from Long to Double.

 * The migration preserves existing user data while updating
 * the database schema to support more precise location values.
 */
internal val DatabaseMigratorFrom1To2 = object : Migration(1, 2) {

    private val TEMPORARY_TABLE_NAME = "temporary_memo_table"

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE ${TEMPORARY_TABLE_NAME} (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                reminderDate INTEGER NOT NULL,
                reminderLatitude REAL NOT NULL,
                reminderLatitude REAL NOT NULL,
                isDone INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO ${TEMPORARY_TABLE_NAME} (
                id,
                title,
                description,
                reminderDate,
                reminderLatitude,
                reminderLongitude,
                isDone
            )
            SELECT
                id,
                title,
                description,
                reminderDate,
                CAST(reminderLatitude AS REAL),
                CAST(reminderLongitude AS REAL),
                isDone
            FROM ${MemoDao.MEMO_TABLE_NAME}
            """.trimIndent()
        )

        db.execSQL("DROP TABLE ${MemoDao.MEMO_TABLE_NAME}")

        db.execSQL(
            "ALTER TABLE ${TEMPORARY_TABLE_NAME} RENAME TO ${MemoDao.MEMO_TABLE_NAME}"
        )
    }
}