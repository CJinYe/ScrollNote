package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-1 14:25
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteDbHelper extends SQLiteOpenHelper {
    public NoteDbHelper(Context context) {
        super(context, NoteDbConf.DB_NAME, null, NoteDbConf.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NoteDbConf.CREATE_ITEM_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
