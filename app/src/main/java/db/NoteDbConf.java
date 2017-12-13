package db;

/**
 * @author Administrator
 * @version $Rev$
 * @time 2017-4-2 9:35
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class NoteDbConf {
    public static final String DB_NAME = "note.db";//数据库名
    public static final int DB_VERSION = 1;//数据库版本

    public static final String ITEM_TABLE = "notetable";//表名
    public static final String ITEM_BOOK_LOCATION = "booklocation";//课本的位置
    public static final String ITEM_TIME = "time";//保存的时间
    public static final String ITEM_NOTE_PATH = "path";//保存的路径
    public static final String ITEM_NOTE_ZIP_PATH = "zippath";//保存的压缩包路径
    public static final String ITEM_NOTE_TEXT = "text";//笔记的文本
    public static final String ITEM_NOTE_PASSWORD = "password";//笔记的文本

    public static final String CREATE_ITEM_TABLE_SQL = "create table " +
            NoteDbConf.ITEM_TABLE + "(" +
//            "_id integer primary key autoincrement," +
//            NoteDbConf.ITEM_BOOK_LOCATION + " varchar unique," +
            NoteDbConf.ITEM_BOOK_LOCATION + " varchar," +
            NoteDbConf.ITEM_TIME + " varchar," +
            NoteDbConf.ITEM_NOTE_ZIP_PATH + " varchar," +
            NoteDbConf.ITEM_NOTE_TEXT + " varchar," +
            NoteDbConf.ITEM_NOTE_PATH + " varchar," +
            NoteDbConf.ITEM_NOTE_PASSWORD + " varchar);";
}
