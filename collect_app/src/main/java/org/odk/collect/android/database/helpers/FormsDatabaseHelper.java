/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.database.helpers;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_DELETE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.AUTO_SEND;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.BASE64_RSA_PUBLIC_KEY;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DATE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DESCRIPTION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_NAME;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.DISPLAY_SUBTEXT;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.FORM_MEDIA_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.GEOMETRY_XPATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JRCACHE_FILE_PATH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_VERSION;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LANGUAGE;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.MD5_HASH;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.SUBMISSION_URI;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class FormsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "forms.db";
    public static final String DATABASE_PATH = Collect.METADATA_PATH + File.separator + DATABASE_NAME;
    public static final String FORMS_TABLE_NAME = "forms";

    static final int DATABASE_VERSION = 8;

    private static final String[] COLUMN_NAMES_V7 = {_ID, DISPLAY_NAME, DESCRIPTION,
            JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
            SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
            LAST_DETECTED_FORM_VERSION_HASH};

    private static final String[] COLUMN_NAMES_V8 = {_ID, DISPLAY_NAME, DESCRIPTION,
        JR_FORM_ID, JR_VERSION, MD5_HASH, DATE, FORM_MEDIA_PATH, FORM_FILE_PATH, LANGUAGE,
        SUBMISSION_URI, BASE64_RSA_PUBLIC_KEY, JRCACHE_FILE_PATH, AUTO_SEND, AUTO_DELETE,
        LAST_DETECTED_FORM_VERSION_HASH, GEOMETRY_XPATH};

    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V8;

    // These exist in database versions 2 and 3, but not in 4...
    private static final String TEMP_FORMS_TABLE_NAME = "forms_v4";
    private static final String MODEL_VERSION = "modelVersion";

    private static boolean isDatabaseBeingMigrated;

    public FormsDatabaseHelper() {
        super(new DatabaseContext(Collect.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFormsTableV8(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            switch (oldVersion) {
                case 1:
                    upgradeToVersion2(db);
                case 2:
                case 3:
                    upgradeToVersion4(db, oldVersion);
                case 4:
                    upgradeToVersion5(db);
                case 5:
                    upgradeToVersion6(db);
                case 6:
                    upgradeToVersion7(db);
                case 7:
                    upgradeToVersion8(db);
                    break;
                default:
                    Timber.i("Unknown version %s", oldVersion);
            }

            Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
            createFormsTableV8(db);

            Timber.i("Downgrading database from %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        onCreate(db);
    }

    private void upgradeToVersion4(SQLiteDatabase db, int oldVersion) {
        // adding BASE64_RSA_PUBLIC_KEY and changing type and name of
        // integer MODEL_VERSION to text VERSION
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
        createFormsTableV4(db, TEMP_FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + TEMP_FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH
                + ", "
                + FORM_FILE_PATH
                + ", "
                + LANGUAGE
                + ", "
                + SUBMISSION_URI
                + ", "
                + JR_VERSION
                + ", "
                + ((oldVersion != 3) ? ""
                : (BASE64_RSA_PUBLIC_KEY + ", "))
                + JRCACHE_FILE_PATH
                + ") SELECT "
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH
                + ", "
                + FORM_FILE_PATH
                + ", "
                + LANGUAGE
                + ", "
                + SUBMISSION_URI
                + ", "
                + "CASE WHEN "
                + MODEL_VERSION
                + " IS NOT NULL THEN "
                + "CAST("
                + MODEL_VERSION
                + " AS TEXT) ELSE NULL END, "
                + ((oldVersion != 3) ? ""
                : (BASE64_RSA_PUBLIC_KEY + ", "))
                + JRCACHE_FILE_PATH + " FROM "
                + FORMS_TABLE_NAME);

        // risky failures here...
        SQLiteUtils.dropTable(db, FORMS_TABLE_NAME);
        createFormsTableV4(db, FORMS_TABLE_NAME);
        db.execSQL("INSERT INTO "
                + FORMS_TABLE_NAME
                + " ("
                + _ID
                + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH + ", "
                + FORM_FILE_PATH + ", "
                + LANGUAGE + ", "
                + SUBMISSION_URI + ", "
                + JR_VERSION + ", "
                + BASE64_RSA_PUBLIC_KEY + ", "
                + JRCACHE_FILE_PATH + ") SELECT "
                + _ID + ", "
                + DISPLAY_NAME
                + ", "
                + DISPLAY_SUBTEXT
                + ", "
                + DESCRIPTION
                + ", "
                + JR_FORM_ID
                + ", "
                + MD5_HASH
                + ", "
                + DATE
                + ", " // milliseconds
                + FORM_MEDIA_PATH + ", "
                + FORM_FILE_PATH + ", "
                + LANGUAGE + ", "
                + SUBMISSION_URI + ", "
                + JR_VERSION + ", "
                + BASE64_RSA_PUBLIC_KEY + ", "
                + JRCACHE_FILE_PATH + " FROM "
                + TEMP_FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, TEMP_FORMS_TABLE_NAME);
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_SEND, "text");
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, AUTO_DELETE, "text");
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, LAST_DETECTED_FORM_VERSION_HASH, "text");
    }

    private void upgradeToVersion7(SQLiteDatabase db) {
        String temporaryTable = FORMS_TABLE_NAME + "_tmp";
        SQLiteUtils.renameTable(db, FORMS_TABLE_NAME, temporaryTable);
        createFormsTableV7(db);
        SQLiteUtils.copyRows(db, temporaryTable, COLUMN_NAMES_V7, FORMS_TABLE_NAME);
        SQLiteUtils.dropTable(db, temporaryTable);
    }

    private void upgradeToVersion8(SQLiteDatabase db) {
        SQLiteUtils.addColumn(db, FORMS_TABLE_NAME, GEOMETRY_XPATH, "text");
    }

    private void createFormsTableV4(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DISPLAY_SUBTEXT + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text);");
    }

    private void createFormsTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
            + _ID + " integer primary key, "
            + DISPLAY_NAME + " text not null, "
            + DESCRIPTION + " text, "
            + JR_FORM_ID + " text not null, "
            + JR_VERSION + " text, "
            + MD5_HASH + " text not null, "
            + DATE + " integer not null, " // milliseconds
            + FORM_MEDIA_PATH + " text not null, "
            + FORM_FILE_PATH + " text not null, "
            + LANGUAGE + " text, "
            + SUBMISSION_URI + " text, "
            + BASE64_RSA_PUBLIC_KEY + " text, "
            + JRCACHE_FILE_PATH + " text not null, "
            + AUTO_SEND + " text, "
            + AUTO_DELETE + " text, "
            + LAST_DETECTED_FORM_VERSION_HASH + " text);");
    }

    private void createFormsTableV8(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORMS_TABLE_NAME + " ("
                + _ID + " integer primary key, "
                + DISPLAY_NAME + " text not null, "
                + DESCRIPTION + " text, "
                + JR_FORM_ID + " text not null, "
                + JR_VERSION + " text, "
                + MD5_HASH + " text not null, "
                + DATE + " integer not null, " // milliseconds
                + FORM_MEDIA_PATH + " text not null, "
                + FORM_FILE_PATH + " text not null, "
                + LANGUAGE + " text, "
                + SUBMISSION_URI + " text, "
                + BASE64_RSA_PUBLIC_KEY + " text, "
                + JRCACHE_FILE_PATH + " text not null, "
                + AUTO_SEND + " text, "
                + AUTO_DELETE + " text, "
                + LAST_DETECTED_FORM_VERSION_HASH + " text, "
                + GEOMETRY_XPATH + " text);");
    }

    public static void databaseMigrationStarted() {
        isDatabaseBeingMigrated = true;
    }

    public static boolean isDatabaseBeingMigrated() {
        return isDatabaseBeingMigrated;
    }

    public static boolean databaseNeedsUpgrade() {
        boolean isDatabaseHelperOutOfDate = false;
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(FormsDatabaseHelper.DATABASE_PATH, null, SQLiteDatabase.OPEN_READONLY);
            isDatabaseHelperOutOfDate = FormsDatabaseHelper.DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }
}
