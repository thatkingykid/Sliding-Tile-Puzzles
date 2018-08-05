package mobile.labs.acw;

import android.provider.BaseColumns;

public class PuzzleDatabase {
    public PuzzleDatabase(){

    }
    public static abstract class PUZZLE_DB_FORMAT implements BaseColumns{
        public static final String TABLE_NAME = "Puzzles";
        public static final String COLUMN_NAME_NO = "PuzzleNo";
        public static final String COLUMN_NAME_DL = "Downloaded";
        public static final String COLUMN_NAME_PICSET = "PictureSet";
        public static final String COLUMN_NAME_DLAYOUT = "DefaultLayout";
        public static final String COLUMN_NAME_CLAYOUT = "CurrentLayout";
        public static final String COLUMN_NAME_HSCORE = "HighScore";
        public static final String COLUMN_NAME_CSCORE = "CurrentScore";
    }

    public static final String SQL_CREATE_TABLE(){
        return String.format("CREATE TABLE %s (%s int PRIMARY KEY NOT NULL, %s int, %s text, %s text, %s text, %s int, %s int);",
                PUZZLE_DB_FORMAT.TABLE_NAME, PUZZLE_DB_FORMAT.COLUMN_NAME_NO, PUZZLE_DB_FORMAT.COLUMN_NAME_DL, PUZZLE_DB_FORMAT.COLUMN_NAME_PICSET,
                PUZZLE_DB_FORMAT.COLUMN_NAME_DLAYOUT, PUZZLE_DB_FORMAT.COLUMN_NAME_CLAYOUT, PUZZLE_DB_FORMAT.COLUMN_NAME_HSCORE, PUZZLE_DB_FORMAT.COLUMN_NAME_CSCORE);
    }

    public static final String SQL_DELETE_TABLE(){
        return String.format("DROP TABLE IF EXISTS %s", PUZZLE_DB_FORMAT.TABLE_NAME);
    }
}
