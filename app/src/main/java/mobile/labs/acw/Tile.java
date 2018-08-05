package mobile.labs.acw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Tile {
    Bitmap image;
    String name;
    public Tile(Context context){
        //default constructor, here for debug and in case the app poops itself
        image = BitmapFactory.decodeResource(context.getResources(), R.drawable.white);
    }
    public Tile (Context context, Bitmap bitmap, String paramName){
        //take in a bitmap and a corresponding file name for it, also context being passed into the constructor is deprecated code
        image = bitmap;
        name = paramName;
    }
    //gets the image file name
    public String GetId(){
        return name;
    }
    //gets the bitmap
    public Bitmap GetImage(){
        return image;
    }
}
