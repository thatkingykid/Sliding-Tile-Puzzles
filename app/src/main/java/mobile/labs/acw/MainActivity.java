package mobile.labs.acw;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MainActivity extends FragmentActivity implements LevelSelectFragment.ListSelectionInterface, PuzzleFragment.FragmentUpdater{
    String versionCode;
    boolean alreadyRun = true;

    protected void onCreate(Bundle savedInstanceState){
        //run super.onCreate()
        super.onCreate(savedInstanceState);
        //check if we've run the app before, and if not initialse the app for the first time
        if(!CheckForFirstRun()){
            InitForFirstRun();
        }
        //set content view and open a fresh level select fragment
        setContentView(R.layout.activity_main);
        OpenFragment(new LevelSelectFragment());
    }

    //begin a fragment transaction and pop the new fragment onto the backstack
    public void OpenFragment(final Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.LevelFragmentFrame, fragment).addToBackStack(fragment.toString());
        transaction.commit();
    }

    private boolean CheckForFirstRun(){
        try{
            //get the version code of the application we're running
            versionCode = String.valueOf(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            //get the version code we've stored prior
            String storedVersionCode = getPreferences(Context.MODE_PRIVATE).getString("versionCode", "DEFAULT");
            //check if we've already run the application before
            boolean storedRun = getPreferences(Context.MODE_PRIVATE).getBoolean("alreadyRun", false);
            //return the result of comparing our two values
            return ((versionCode.equals(storedVersionCode)) && (alreadyRun == storedRun));
        } catch (Exception e){
            Log.w("Start up check failed", e.getMessage());
            return false;
        }
    }
    private void InitForFirstRun(){
        //download the index list
        new IndexDownloader().execute("http://www.simongrey.net/08027/slidingPuzzleAcw/index.json");
        try {
            //get the version code
            versionCode = String.valueOf(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (Exception e){
            Log.w("Oh boy", e.getMessage());
        }
        //write the version code and the fact we've run the app to sharedprefs
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("versionCode", versionCode);
        editor.putBoolean("alreadyRun", true);
        editor.commit();
    }

    //open up a new puzzle fragment with an index provided by an interface implemented on the list view
    @Override
    public void OnListSelection(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("level", index);
        PuzzleFragment newFragment = new PuzzleFragment();
        newFragment.setArguments(bundle);
        OpenFragment(newFragment);
    }

    //supposed to be the interface for when a puzzle is completed
    //doesn't work due to the method in which we update the puzzle fragment view
    @Override
    public void UpdateFragment(int high, int achieved, Fragment fragment) {
        if(high < achieved){
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setTitle(getString(R.string.game_over));
            alert.setMessage(getString(R.string.high_score));
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            AlertDialog alert = new AlertDialog.Builder(this).create();
            alert.setTitle(getString(R.string.game_over));
            alert.setMessage(getString(R.string.no_high_score));
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.detach(fragment);
        transaction.commit();

    }

    //async task for downloading the JSON index file
    public class IndexDownloader extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... args){
            //get the database helper
            String jsonString = "";
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            try{
                //get a link to our SQLite DB
                SQLiteDatabase database = helper.getWritableDatabase();

                //open up the stream with the URL provided and read in the JSON content
                InputStream stream = (InputStream)new URL(args[0]).getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while(line!= null){
                    jsonString += line;
                    line = reader.readLine();
                }

                //create a new JSON object out of the content of the read in JSON
                JSONObject json = new JSONObject(jsonString);
                JSONArray levels = json.getJSONArray("PuzzleIndex");

                //create a new contentvalue to write in the puzzles in an initialised state into the DB
                ContentValues values = new ContentValues();
                for(int i = 1; i < levels.length() + 1; i++){
                    values.clear();
                    values.put(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, i);
                    values.put(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DL, 0);
                    database.insert(PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME, null, values);
                }
                //close the DB
                database.close();
                Log.i("Index list downloaded", "WOO");
                return true;

            } catch (Exception e){
                Log.w("Failed to d/l index", e.getMessage());
                return false;
            }
        }
    }
}
