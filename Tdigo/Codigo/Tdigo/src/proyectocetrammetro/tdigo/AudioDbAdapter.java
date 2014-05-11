package proyectocetrammetro.tdigo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AudioDbAdapter {
	
	public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
	public static final String KEY_ROWID = "_id";
	
	private static final String TAG = "AudioDbAdapter";
	private DatabaseHelper mDbHelper; 
	private SQLiteDatabase mDb;
	
	//private static final String DATABASE_CREATE =
	 //       "create table audioparameters (_id integer primary key autoincrement, "
	  //      + "body text not null);";
	
	private static final String DATABASE_NAME = "audiodata";
    private static final String DATABASE_TABLE = "audioparameters";
    private static final int DATABASE_VERSION = 2;
    
	private static final String DATABASE_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + " (" +
            KEY_ROWID + " integer primary key autoincrement, " + 
            KEY_TITLE + " TEXT, " +
            KEY_BODY + " FLOAT);";
	
    
    private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("Drop table if exists " + DATABASE_TABLE);
            onCreate(db);
        }
    }
	
	public AudioDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
	
	 public AudioDbAdapter open() throws SQLException {
	        mDbHelper = new DatabaseHelper(mCtx);
	        mDb = mDbHelper.getWritableDatabase();
	        return this;
	 }

	 public void close() {
	     mDbHelper.close();
	 }
	 
	 public long createData(String title, float body) {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_TITLE, title);
	        initialValues.put(KEY_BODY, body);

	        return mDb.insert(DATABASE_TABLE, null, initialValues);
	 }
	 
	 public boolean deleteData(long rowId) {
		 
	        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	 }
	 
	 public Cursor fetchAllData() {

	       return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
	               KEY_BODY}, null, null, null, null, null);
	        //return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
	         //       KEY_BODY}, null, null, null, null, null);

	 }
	 
	 public Cursor fetchData(long rowId) throws SQLException {

	        Cursor mCursor =
	            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
	                     KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
	                    null, null, null, null);
	        if (mCursor != null) {
	            mCursor.moveToFirst();
	        }
	        return mCursor;
	}
	 
	 public boolean updateData(long rowId, String title, Float body) {
	        ContentValues args = new ContentValues();
	        args.put(KEY_TITLE, title);
	        args.put(KEY_BODY, body);

	        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	 
}
