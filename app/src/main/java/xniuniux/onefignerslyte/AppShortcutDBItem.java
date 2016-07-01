package xniuniux.onefignerslyte;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class AppShortcutDBItem {

    /** Table name */
    public static final String TABLE_NAME = "appShortcut";

    /** Id */
    public static final String KEY_ID = "_id";

    /** Variables */
    public static final String COLUMN_IS_GENERAL = "is_general";
    public static final String COLUMN_POSITION_GENERAL = "position_general";
    public static final String COLUMN_APK_NAME = "package_name";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_BACKGROUND_HIGHLIGHT = "highlight";

    /** CREATE string */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_APK_NAME + " TEXT NOT NULL, " +
                    COLUMN_ICON + " TEXT NOT NULL, " +
                    COLUMN_BACKGROUND_HIGHLIGHT + " TEXT NOT NULL " +
                    COLUMN_IS_GENERAL + " INTEGER NOT NULL, " +
                    COLUMN_POSITION_GENERAL + " INTEGER, " +
                    ") ";

    /** Database */
    private SQLiteDatabase db;

    public AppShortcutDBItem(Context ctx){
        db = OneFignerDBHelper.getDatabase(ctx);
    }

    public void close() {
        db.close();
    }

    /** Insert new item */
    public AppInfo insert(AppInfo app){

        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APK_NAME, app.getPackageName());
        cv.put(COLUMN_ICON, app.getIconPath());
        cv.put(COLUMN_BACKGROUND_HIGHLIGHT, app.getHighlightPath());
        cv.put(COLUMN_IS_GENERAL, app.isGeneral()?1:0);
        cv.put(COLUMN_POSITION_GENERAL, app.getPositionGeneral());

        long id = db.insert(TABLE_NAME, null, cv);
        app.setId(id);

        return app;
    }

    /** Update old item */
    public boolean updateByName(AppInfo app){
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APK_NAME, app.getPackageName());
        cv.put(COLUMN_ICON, app.getIconPath());
        cv.put(COLUMN_BACKGROUND_HIGHLIGHT, app.getHighlightPath());
        cv.put(COLUMN_IS_GENERAL, app.isGeneral());
        cv.put(COLUMN_POSITION_GENERAL, app.getPositionGeneral());

        String where = COLUMN_APK_NAME + "=" + app.getPackageName();
        return db.update(TABLE_NAME, cv, where, null)>0;
    }

    public boolean updateBy(AppInfo app, String where){
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_APK_NAME, app.getPackageName());
        cv.put(COLUMN_ICON, app.getIconPath());
        cv.put(COLUMN_BACKGROUND_HIGHLIGHT, app.getHighlightPath());
        cv.put(COLUMN_IS_GENERAL, app.isGeneral());
        cv.put(COLUMN_POSITION_GENERAL, app.getPositionGeneral());

        return db.update(TABLE_NAME, cv, where, null)>0;
    }

    public boolean deleteByName(String name){
        String where = COLUMN_APK_NAME + "=" + name;
        return db.delete(TABLE_NAME, where, null)>0;
    }

    public boolean deleteBy(String where){
        return db.delete(TABLE_NAME, where, null)>0;
    }

    public List<AppInfo> getAllBy(String where){
        List<AppInfo> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getResult(cursor));
        }

        cursor.close();
        return result;
    }

    public AppInfo getResult(Cursor cursor){
        AppInfo app = new AppInfo();
        app.setId(cursor.getLong(0));
        app.setPackageName(cursor.getString(1));
        app.setIconPath(cursor.getString(2));
        app.setHighlightPath(cursor.getString(3));

        if (cursor.getInt(4)==1) {
            app.setIsGeneral(true);
            app.setPositionGeneral(cursor.getInt(5));
        } else {
            app.setIsGeneral(false);
        }

        cursor.close();
        return app;
    }

    public AppInfo getFirstByName(String name){
        AppInfo result = null;

        String where = COLUMN_APK_NAME + "=" + name;
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, "1");

        if (cursor.moveToFirst()){
            result = getResult(cursor);
        }

        cursor.close();
        return result;

    }

    public List<AppInfo> getGeneral(){
        List<AppInfo> result = new ArrayList<>();
        String where = COLUMN_IS_GENERAL + "=1";
        String orderBy = COLUMN_POSITION_GENERAL;
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, orderBy, null);

        while (cursor.moveToNext()) {
            result.add(getResult(cursor));
        }

        cursor.close();
        return result;
    }



}
