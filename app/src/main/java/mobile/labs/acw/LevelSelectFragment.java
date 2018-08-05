package mobile.labs.acw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.io.InputStream;
import java.util.ArrayList;

public class LevelSelectFragment extends Fragment {
    ArrayList<Integer> mDownloadedLevels = new ArrayList<Integer>();
    ListSelectionInterface mSelectorInterface;

    //override the onAttach method and attempt to add the main activity as a List Selection Interface for when we click on a puzzle
    @Override
    public void onAttach(Context context){
        try{
            mSelectorInterface = (ListSelectionInterface)context;
        }
        catch(Exception e){
            Log.w("Fragment Error.", "Main activity does not implement ListSelectionInterface");
        }
        super.onAttach(context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle){
        //get the view and listview of the fragment
        View fragmentView =  inflater.inflate(R.layout.level_select_layout, container, false);
        final ListView listView = (ListView)fragmentView.findViewById(R.id.PuzzleListView);

        //build a new list of 'Puzzle' string objects
        ArrayList<String> items = new ArrayList<String>();
        for(int i = 0; i < 160; i++){
            items.add(i, String.format(getContext().getString(R.string.puzzle), i+1));
        }

        //check to see what levels we've downloaded and try and add them to the existing list
        DownloadsChecker downloadsChecker = new DownloadsChecker();
        try {
            SwapLevelLists(downloadsChecker.execute((Void) null).get());
        } catch (Exception e){
            Log.w("Failed to d/l level", e.getMessage());
        }

        //create a new listadapter for the listview, and pass in our items and what levels have been downloaded
        final LevelAdapter adapter = new LevelAdapter(getContext(), items, mDownloadedLevels);
        listView.setAdapter(adapter);
        //create a new onclick listener and work out whether we should play a level or update the view
        // based on how many new levels we downloaded
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Integer> newLevels = new ArrayList<Integer>();
                try {
                    newLevels = new LevelDownloader().execute(position).get();
                } catch (Exception e){
                    Log.w("Try/Catch", e.getMessage());
                }
                if(newLevels.size() != 0) {
                    SwapLevelLists(newLevels);
                    adapter.NewLevels(newLevels);
                    RebuildFragment();
                } else {
                    mSelectorInterface.OnListSelection(position);
                }
            }
        });
        //return the fragment view
        return fragmentView;
    }

    final void SwapLevelLists(ArrayList<Integer> newList){
        //if we have a massive new list
        if(newList.size() == 160){
            //assume to update the whole list
            mDownloadedLevels = newList;
        } else{
            //if not, just append it on to what we've got
            mDownloadedLevels.addAll(newList);
        }
        Log.i("Swap Occurred", "See tag");
    }

    final void RebuildFragment(){
        //rebuild the fragment
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    public interface ListSelectionInterface{
        //interface for deciding which level to open based off which element of a list view as touched
        void OnListSelection(int index);
    }

    public class DownloadsChecker extends AsyncTask<Void, Void, ArrayList<Integer>>{
        @Override
        protected ArrayList<Integer> doInBackground(Void... voids) {
            //get a new DB helper
            DatabaseHelper helper = new DatabaseHelper(getContext());
            //create an empty list and a new link to the SQLite DB
            ArrayList<Integer> downloaded = new ArrayList<Integer>();
            SQLiteDatabase database = helper.getReadableDatabase();

            //get the results of querying which levels we've downloaded and loop through them to add them to the downloaded levels list
            Cursor downloadedLevels = database.rawQuery(String.format("SELECT %s FROM %s WHERE %s = 1",
                    PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME, PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DL), null);
            int size = downloadedLevels.getCount();
            downloadedLevels.moveToNext();
            for(int i = 0; i < size; i++){
                downloaded.add((downloadedLevels.getInt(0) - 1));
                downloadedLevels.moveToNext();
            }
            //close the DB and return the list
            database.close();;
            return downloaded;
        }
    }

    public class LevelDownloader extends AsyncTask<Integer, Void, ArrayList<Integer>>{

        @Override
        protected ArrayList<Integer> doInBackground(Integer... integers) {
            //get DB helper
            DatabaseHelper helper = new DatabaseHelper(getContext());
            //create new list of recently downloaded levels
            ArrayList<Integer> downloadedLevels = new ArrayList<Integer>();
            try {
                //loop through the list of integers we recieved
                for (int i = 0; i < integers.length; i++) {
                    //make a new DB connection
                    SQLiteDatabase database = helper.getWritableDatabase();
                    //get the level selection
                    int level = integers[i] + 1;

                    //check we don't already have the level in the database
                    String existQuery = String.format("Select * from %s where %s = %d and %s = %d",
                            PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME, PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, level,
                            PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DL, 1);

                    if(database.rawQuery(existQuery, null).getCount() != 0){
                        database.close();
                        continue;
                    }

                    //define a file path and URLs to the puzzle info, and download the info
                    String puzzleFile = String.format("puzzle%d.json", level);
                    String puzzleURL = "http://www.simongrey.net/08027/slidingPuzzleAcw/puzzles/" + puzzleFile;
                    InputStream stream = (InputStream)new URL(puzzleURL).getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String levelInfo = reader.readLine();
                    stream.close();
                    reader.close();

                    //get the picture and layout info from the puzzle info JSON we downloaded above
                    JSONObject levelJSON = new JSONObject(levelInfo);
                    String pictureSet = levelJSON.getString("PictureSet");
                    String layoutSet = levelJSON.getString("layout");

                    //open a new streamreader to the URL of the layout file, and download
                    stream = (InputStream)new URL("http://www.simongrey.net/08027/slidingPuzzleAcw/layouts/" + layoutSet).getContent();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    String rawLayout = "";
                    String line = "";
                    while(line != null){
                        rawLayout += line;
                        line = reader.readLine();
                    }
                    stream.close();
                    reader.close();
                    //create a pruned JSON array of the layout info
                    JSONArray layoutArray = new JSONObject(rawLayout).getJSONArray("layout");

                    //create a file directory to write the image to
                    String imageDir = String.format("%s/images/%s", getContext().getFilesDir(), puzzleFile);
                    File directory = new File(imageDir);
                    if(!directory.exists()){
                        directory.mkdirs();
                    }

                    //loop through the layout file, skip over any empties and save the image file to the phone
                    for(int y = 0; y < layoutArray.length(); y++){
                        JSONArray row = layoutArray.getJSONArray(y);
                        for(int x = 0; x < row.length(); x++){
                            String entry = row.getString(x);
                            if(!entry.equals("empty")){
                                File imageFile = new File(directory, String.format("%s.jpg", entry));
                                stream = (InputStream)new URL(String.format("http://www.simongrey.net/08027/slidingPuzzleAcw/images/%s/%s", pictureSet, entry)).getContent();
                                Bitmap image = BitmapFactory.decodeStream(stream);
                                FileOutputStream outputStream = new FileOutputStream(imageFile);
                                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.close();
                                stream.close();
                            }
                        }
                    }

                    //using content values, update the database with the new values of the level we just downloaded
                    ContentValues values = new ContentValues();
                    values.clear();
                    values.put(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DL, 1);
                    values.put(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_PICSET, imageDir);
                    values.put(PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_DLAYOUT, layoutArray.toString());

                    database.update(PuzzleDatabase.PUZZLE_DB_FORMAT.TABLE_NAME, values, String.format("%s=%d",
                            PuzzleDatabase.PUZZLE_DB_FORMAT.COLUMN_NAME_NO, level), null);
                    //add the level to the list and close the DB
                    downloadedLevels.add(level - 1);
                    database.close();
                }
                return downloadedLevels;
            } catch (Exception e){
                Log.w("Failed to d/l level", e.getMessage());
                return downloadedLevels;
            }
        }
    }
}
