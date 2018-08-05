package mobile.labs.acw;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;

public class PuzzleFragment extends Fragment {
    //are we playing a game?
    boolean mActive = false;
    //have we won the game?
    boolean mWon = false;
    //the custom board view object
    BoardView mBoardView;
    //the app context
    Context mContext;
    //the high score - not currently used
    int mHighScore;
    //default layout to be reset to on completion of the puzzle
    JSONArray mDefaultLayout;
    //the current layout of the puzzle
    JSONArray mCurrentLayout;
    //fragment updater interface
    FragmentUpdater mUpdater;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    //attach our main activity as the interface for swapping fragments, and retrieve the context to be stored as a member
    public void onAttach(Context context) {
        try{
            mUpdater = (FragmentUpdater) context;
        }
        catch(Exception e){
            Log.w("Fragment Error.", "Main activity does not implement FragmentUpdaterInterface");
        }
        super.onAttach(context);
        this.mContext = context;
    }

    //creates the view of the fragment
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle bundle){
        //get the view created by the inflater
        final View fragmentView = inflater.inflate(R.layout.puzzle_layout, container, false);
        //find the boardview in the fragment view
        mBoardView = (BoardView)container.findViewById(R.id.BoardView);
        //get the level we chose
        int level = this.getArguments().getInt("level");
        if(!mActive) {
            try {
                //try and fetch it from storage
                mDefaultLayout = new LevelFetcher().execute(level).get();
            } catch (Exception e) {
                Log.w("Level download failed", e.getMessage());
            }
        }
        //build a new board based on the level we just downloaded
        Board board = new Board(mCurrentLayout, mContext, getContext().getFilesDir().toString(), level + 1);
        //remove the old boardview and reattach a new one with our board and a click listener
        container.removeView(mBoardView);
        mBoardView = new BoardView(this.mContext, board);
        mBoardView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean eventResponse = mBoardView.TouchEvent(event);
                return UpdateFragment();
            }
        });
        //add the custom boardview to the view container and return the fragment view
        container.addView(mBoardView);
        return fragmentView;
    }
    private boolean UpdateFragment(){
        //set the game to active
        mActive = true;
        //recreate the board layout
        UpdateLayout();
        //check if we've won the game
        mWon = mBoardView.WonGame(mCurrentLayout);
        //save the database
        SaveDatabase();
        //attempt to clear the back stack so we can display the updated fragment
        try {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e){
            Log.w("Pop backstack failed", e.toString());
        }
        //get a new transactor
        android.support.v4.app.FragmentTransaction transactor = getFragmentManager().beginTransaction();
        if(!mWon){
            //if we've not won, update the view
            transactor.attach(this).commit();
        } else{
            //TODO:if we've won, do some stuff - doesn't actually work :(
            WonGame();
            mUpdater.UpdateFragment(mHighScore, mBoardView.GetMoves(), this);
        }
        //force through our transactions
        getFragmentManager().executePendingTransactions();
        return true;
    }

    //if we've won, reset the layout
    private void WonGame(){
        mCurrentLayout = mDefaultLayout;
    }

    //fetch the current layout of the board
    private void UpdateLayout(){
        mCurrentLayout = mBoardView.CreateLayout();
    }

    //update the level's database entry with the new layout
    private void SaveDatabase(){
        int level = this.getArguments().getInt("level") + 1;
        try{
            DatabaseHelper helper = new DatabaseHelper(mContext);
            SQLiteDatabase database = helper.getWritableDatabase();
            String updateQuery = String.format("UPDATE %s SET %s = '%s' WHERE %s = %d",
                    PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME, PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_CLAYOUT, mCurrentLayout.toString(),
                    PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, level);
            database.rawQuery(updateQuery, null);
            database.close();
        } catch(Exception e){
            Log.w("Failed to save level", e.getMessage());
        }
    }

    private class LevelFetcher extends AsyncTask<Integer, Void, JSONArray>{
        //get a new DB helper
        DatabaseHelper mHelper = new DatabaseHelper(getContext());
        @Override
        protected JSONArray doInBackground(Integer... voids) {
            //get a new database
            SQLiteDatabase database = mHelper.getReadableDatabase();

            //check that we've actually downloaded the level we're after
            int level = voids[0];
            String existsQuery = String.format("SELECT * FROM %s WHERE %s = %d", PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME,
                    PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, level+1);
            Cursor results = database.rawQuery(existsQuery, null);
            if(results.getCount() == 0){
                Log.w("Level not found!", "Ahh");
                return null;
            }

            //move to the index we just gathered, and check for a current layout
            results.moveToNext();
            //get high-score TODO
            mHighScore = results.getInt(results.getColumnIndexOrThrow(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_HSCORE));
            //if we've not got a current layout
            if(results.getString(results.getColumnIndexOrThrow(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_CLAYOUT)) == null){
                JSONArray layoutDefault = new JSONArray();
                try {
                    layoutDefault = new JSONArray(results.getString(results.getColumnIndexOrThrow(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DLAYOUT)));
                } catch (Exception e){
                    Log.w("Failed to d/l layout", e.getMessage());
                }

                //read in the default, set it to the current layout, and return it
                database.close();
                mCurrentLayout = layoutDefault;
                return layoutDefault;
            } else{
                //if we do
                JSONArray layoutDefault = new JSONArray();
                try{
                    //read in both layouts
                    layoutDefault = new JSONArray(results.getString(results.getColumnIndexOrThrow(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DLAYOUT)));
                    mCurrentLayout = new JSONArray(results.getString(results.getColumnIndexOrThrow(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_CLAYOUT)));
                } catch (Exception e){
                    Log.w("Failed to d/l layout", e.getMessage());
                }
                database.close();
                return layoutDefault;
            }
        }
    }
    public interface FragmentUpdater{
        //interface for updating fragment on completion - doesn't work TODO
        void UpdateFragment(int high, int achieved, Fragment fragment);
    }
}
