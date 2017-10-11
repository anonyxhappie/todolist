/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class TaskContentProvider extends ContentProvider {

    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS, TASKS);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#", TASK_WITH_ID);
        return uriMatcher;
    }

    TaskDbHelper mTaskDbHelper;
    @Override
    public boolean onCreate() {
         mTaskDbHelper = new TaskDbHelper(getContext());
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (match){
            case TASKS:
                long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
                if (id > 0){
                    // success
                    returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                }else{
                    throw new SQLException("Failed to insert new row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;
        switch (match){
            case TASKS:
                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};
                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int numberOfDeletions=0;
        switch (match){
            case TASK_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};
                numberOfDeletions = db.delete(TaskContract.TaskEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return numberOfDeletions;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
