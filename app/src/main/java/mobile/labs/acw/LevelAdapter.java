package mobile.labs.acw;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class LevelAdapter extends ArrayAdapter<String> {
    Context mContext;
    ArrayList<String> mListContent;
    ArrayList<Integer> mDownloadedContent;

    public LevelAdapter(@NonNull Context context, ArrayList<String> list, ArrayList<Integer> downloads) {
        //fetch the context, list of items to populate the list view with and any levels we've downloaded
        super(context, 0, list);
        mContext = context;
        mListContent = list;
        mDownloadedContent = downloads;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //get the current list item
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.listview_level_layout, parent, false);
        }

        //divide the levels up into easy/intermediate/tough/brutal colour coding
        if (position <= 40 && mDownloadedContent.contains(position)) {
            listItem.setBackgroundColor(Color.parseColor("#92FE4A"));
        } else if (position <= 80 && mDownloadedContent.contains(position)) {
            listItem.setBackgroundColor(Color.parseColor("#FFC500"));
        } else if (position <= 120 && mDownloadedContent.contains(position)) {
            listItem.setBackgroundColor(Color.parseColor("#E86A0C"));
        } else if (position <= 160 && mDownloadedContent.contains(position)) {
            listItem.setBackgroundColor(Color.parseColor("#FA6A6A"));
        } else {
            listItem.setBackgroundColor(Color.parseColor("#E5E5E5"));
        }

        String level = mListContent.get(position);
        TextView textView = (TextView) listItem.findViewById(R.id.PuzzleIDView);
        textView.setText(level);
        //TODO: high score display
        textView = (TextView) listItem.findViewById(R.id.HighScoreView);
        textView.setText(mContext.getString(R.string.score));
        return listItem;
    }
    //update our downloaded list
    public void NewLevels(ArrayList<Integer> newDownloads){
        mDownloadedContent = newDownloads;
        super.notifyDataSetChanged();
    }
}
