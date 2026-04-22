package com.example.attendance.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendance.database.DatabaseHelper;

/**
 * Content Provider for managing subject attendance data.
 * Provides standardized access to the SQLite database.
 */
public class SubjectProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.attendance.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/subjects");

    private static final int SUBJECTS = 1;
    private static final int SUBJECT_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "subjects", SUBJECTS);
        uriMatcher.addURI(AUTHORITY, "subjects/#", SUBJECT_ID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            dbHelper = DatabaseHelper.getInstance(context);
        }
        return dbHelper != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                cursor = db.query(
                        DatabaseHelper.TABLE_SUBJECTS,
                        projection,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            case SUBJECT_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query(
                        DatabaseHelper.TABLE_SUBJECTS,
                        projection,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id},
                        null, null,
                        sortOrder
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".subjects";
            case SUBJECT_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".subjects";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != SUBJECTS) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(DatabaseHelper.TABLE_SUBJECTS, null, values);

        if (id > 0) {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(resultUri, null);
            }
            return resultUri;
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                count = db.delete(DatabaseHelper.TABLE_SUBJECTS, selection, selectionArgs);
                break;
            case SUBJECT_ID:
                String id = uri.getLastPathSegment();
                count = db.delete(
                        DatabaseHelper.TABLE_SUBJECTS,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id}
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case SUBJECTS:
                count = db.update(DatabaseHelper.TABLE_SUBJECTS, values, selection, selectionArgs);
                break;
            case SUBJECT_ID:
                String id = uri.getLastPathSegment();
                count = db.update(
                        DatabaseHelper.TABLE_SUBJECTS,
                        values,
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{id}
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}