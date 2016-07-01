package xniuniux.onefignerslyte;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class OneFignerDBHelper extends SQLiteOpenHelper{

    /** Database name */
    public static final String DATABASE_NAME = "oneFigner.db";
    /** Version */
    public static final int VERSION = 1;
    /** Object */
    private static SQLiteDatabase database;

    public OneFignerDBHelper(Context context, String name, CursorFactory factory,
                             int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new OneFignerDBHelper(context, DATABASE_NAME,
                    null, VERSION).getWritableDatabase();
        }

        return database;
    }



    @Override
    public  void onCreate(SQLiteDatabase db){
        db.execSQL(AppShortcutDBItem.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + AppShortcutDBItem.TABLE_NAME);
        onCreate(db);
    }
}
