package mobile.labs.acw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final Integer DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "puzzles.db";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database){
        database.execSQL(PuzzleDatabase.SQL_CREATE_TABLE());
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        //TODO
    }
}
