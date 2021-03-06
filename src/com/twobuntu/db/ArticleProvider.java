package com.twobuntu.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

// Provides access to the articles.
public class ArticleProvider extends ContentProvider {

	// The authority for the provider.
	private static final String AUTHORITY = "com.twobuntu.articleprovider";
	
	// These URIs are public and intended to be used by other parts of the application.
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/articles");
	public static final Uri CONTENT_LOOKUP_URI = CONTENT_URI; // same thing
	
	// Codes that represent individual URIs.
	private static final int ARTICLES = 1;
	private static final int ARTICLE = 2;
	
	// Used for matching URIs.
	private static UriMatcher mMatcher;
	
	// Initialize the URIs for articles.
	static {
		mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mMatcher.addURI(AUTHORITY, "articles", ARTICLES);
		mMatcher.addURI(AUTHORITY, "articles/#", ARTICLE);
	}
	
	// Provides access to the database.
	private ArticleHelper mHelper;
	
	@Override
	public boolean onCreate() {
		mHelper = new ArticleHelper(getContext());
		return true;
	}
	
	// Provides a means to insert a lot of rows at once.
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		if(mMatcher.match(uri) != ARTICLES)
			throw new IllegalArgumentException();
		// Perform the actual insert operations.
		int numRows = 0;
		for(ContentValues articleValues : values) {
		    mHelper.getWritableDatabase().insert(Article.TABLE_NAME, null, articleValues);
		    ++numRows;
		}
		// Provide a notification that new rows were inserted.
		getContext().getContentResolver().notifyChange(uri, null);
		return numRows;
	}
	
	// As there is currently no need to delete articles, this method is unimplemented.
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
	
	// Returns the type indicated by the specified URI.
	@Override
	public String getType(Uri uri) {
		switch(mMatcher.match(uri)) {
		    case ARTICLES:
		    	return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".articles";
		    case ARTICLE:
		    	return "vnd.android.cursor.item/vnd." + AUTHORITY + ".articles";
		    default:
		    	throw new IllegalArgumentException();
		}
	}
	
	// As this content provider only supports bulk insertions, this method is unimplemented.
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}
	
	// Returns a cursor for the provided query.
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		switch(mMatcher.match(uri)) {
		    case ARTICLES:
			    break;
		    case ARTICLE:
			    selection = Article.COLUMN_ID + " = " + uri.getLastPathSegment();
			    break;
			default:
				throw new IllegalArgumentException();
		}
		// Return the cursor to the user.
		Cursor cursor = mHelper.getReadableDatabase().query(Article.TABLE_NAME, projection,
				selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	// Updates an article in the database.
	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
		if(mMatcher.match(uri) != ARTICLE)
			throw new IllegalArgumentException();
		whereClause = Article.COLUMN_ID + " = " + uri.getLastPathSegment();
		// Perform the update and remember the number of rows affected.
		int rowsAffected = mHelper.getWritableDatabase().update(Article.TABLE_NAME, values,
				whereClause, whereArgs);
		if(rowsAffected != 0)
		    getContext().getContentResolver().notifyChange(uri, null);
		return rowsAffected;
	}
}
