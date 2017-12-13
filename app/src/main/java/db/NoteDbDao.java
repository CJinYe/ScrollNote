package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import bean.NoteBean;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-2 9:51
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteDbDao {

    private final NoteDbHelper mDbHelper;
    private SQLiteDatabase mDataBase;

    public NoteDbDao(Context context) {
        mDbHelper = new NoteDbHelper(context);
    }

    public boolean saveNote(String bookLocation, String time, String path,
                            String text,String zipPath,String password) {
        mDataBase = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteDbConf.ITEM_BOOK_LOCATION, bookLocation);
        values.put(NoteDbConf.ITEM_TIME, time);
        values.put(NoteDbConf.ITEM_NOTE_PATH, path);
        values.put(NoteDbConf.ITEM_NOTE_TEXT, text);
        values.put(NoteDbConf.ITEM_NOTE_ZIP_PATH, zipPath);
        values.put(NoteDbConf.ITEM_NOTE_PASSWORD, password);
        long insert = mDataBase.insert(NoteDbConf.ITEM_TABLE, null, values);//返回-1表示不成功
        mDataBase.close();
        return insert != -1;
    }

    public NoteBean queryNote(String booklocation) {
        mDataBase = mDbHelper.getWritableDatabase();
        Cursor cursor = mDataBase.query(NoteDbConf.ITEM_TABLE, null,
                NoteDbConf.ITEM_BOOK_LOCATION + " = ?", new String[]{booklocation}, null, null, null);
        while (cursor.moveToNext()) {
            NoteBean noteBean = new NoteBean();
            String bookLocation = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_BOOK_LOCATION));
            String time = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_TIME));
            String path = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_PATH));
            String text = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_TEXT));
            noteBean.bookLocation = bookLocation;
            noteBean.path = path;
            noteBean.time = time;
            noteBean.text = text;
            return noteBean;
        }
        cursor.close();
        mDataBase.close();
        return null;
    }

    public NoteBean queryNoteTime(String time) {
        mDataBase = mDbHelper.getWritableDatabase();
        Cursor cursor = mDataBase.query(NoteDbConf.ITEM_TABLE, null,
                NoteDbConf.ITEM_TIME + " = ?", new String[]{time}, null, null, null);
        while (cursor.moveToNext()) {
            NoteBean noteBean = new NoteBean();
            String bookLocation = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_BOOK_LOCATION));
            String times = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_TIME));
            String path = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_PATH));
            String text = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_TEXT));
            String password = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_PASSWORD));
            noteBean.bookLocation = bookLocation;
            noteBean.path = path;
            noteBean.time = times;
            noteBean.text = text;
            noteBean.password = password;
            return noteBean;
        }
        cursor.close();
        mDataBase.close();
        return null;
    }

    public NoteBean queryNotePaht(String path) {
        mDataBase = mDbHelper.getWritableDatabase();
        Cursor cursor = mDataBase.query(NoteDbConf.ITEM_TABLE, null,
                NoteDbConf.ITEM_NOTE_PATH + " = ?", new String[]{path}, null, null, null);
        while (cursor.moveToNext()) {
            NoteBean noteBean = new NoteBean();
            String bookLocation = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_BOOK_LOCATION));
            String times = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_TIME));
            String text = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_TEXT));
            noteBean.bookLocation = bookLocation;
            noteBean.path = path;
            noteBean.time = times;
            noteBean.text = text;
            return noteBean;
        }
        cursor.close();
        mDataBase.close();
        return null;
    }

    public NoteBean queryNoteZipPaht(String zipPath ,String path) {
        mDataBase = mDbHelper.getWritableDatabase();
        Cursor cursor = mDataBase.query(NoteDbConf.ITEM_TABLE, null,
                NoteDbConf.ITEM_NOTE_PATH + " = ? and "+NoteDbConf.ITEM_NOTE_ZIP_PATH+" = ?", new String[]{path,zipPath}, null, null, null);
        while (cursor.moveToNext()) {
            NoteBean noteBean = new NoteBean();
            String bookLocation = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_BOOK_LOCATION));
            String times = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_TIME));
            String text = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_TEXT));
            String password = cursor.getString(cursor.getColumnIndex(NoteDbConf.ITEM_NOTE_PASSWORD));
            noteBean.bookLocation = bookLocation;
            noteBean.path = path;
            noteBean.zipPath = zipPath;
            noteBean.time = times;
            noteBean.text = text;
            noteBean.password = password;
            return noteBean;
        }
        cursor.close();
        mDataBase.close();
        return null;
    }

    public boolean updateNote(String bookLocation, String time, String path, String text) {
        mDataBase = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteDbConf.ITEM_TIME, time);
        values.put(NoteDbConf.ITEM_NOTE_PATH, path);
        values.put(NoteDbConf.ITEM_NOTE_TEXT, text);
        int update = mDataBase.update(NoteDbConf.ITEM_TABLE, values
                , NoteDbConf.ITEM_BOOK_LOCATION + " = ?"
                , new String[]{bookLocation});
        mDataBase.close();
        return update > 0;
    }

    public boolean updateZipNote(String bookLocation, String time, String path,
                                 String text,String zipPath,String password) {
        mDataBase = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteDbConf.ITEM_TIME, time);
        values.put(NoteDbConf.ITEM_NOTE_PATH, path);
        values.put(NoteDbConf.ITEM_NOTE_TEXT, text);
        values.put(NoteDbConf.ITEM_BOOK_LOCATION, bookLocation);
        values.put(NoteDbConf.ITEM_NOTE_PASSWORD, password);
        int update = mDataBase.update(NoteDbConf.ITEM_TABLE, values
                , NoteDbConf.ITEM_NOTE_ZIP_PATH + " = ?"
                , new String[]{zipPath});
        mDataBase.close();
        return update > 0;
    }

    public boolean deleteNote(String bookLocation) {
        mDataBase = mDbHelper.getWritableDatabase();
        int delete = mDataBase.delete(NoteDbConf.ITEM_TABLE,
                NoteDbConf.ITEM_BOOK_LOCATION + " = ?",
                new String[]{bookLocation});
        return delete > 0;
    }


    public boolean deleteItem(String path) {
        mDataBase = mDbHelper.getWritableDatabase();
        int delete = mDataBase.delete(NoteDbConf.ITEM_TABLE,
                NoteDbConf.ITEM_NOTE_PATH + " = ?",
                new String[]{path});
        return delete > 0;
    }

    public boolean deleteZipItem(String zipPath) {
        mDataBase = mDbHelper.getWritableDatabase();
        int delete = mDataBase.delete(NoteDbConf.ITEM_TABLE,
                NoteDbConf.ITEM_NOTE_ZIP_PATH + " = ?",
                new String[]{zipPath});
        return delete > 0;
    }

}
