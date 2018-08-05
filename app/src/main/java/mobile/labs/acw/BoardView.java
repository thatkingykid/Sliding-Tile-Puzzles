package mobile.labs.acw;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.json.JSONArray;

public class BoardView extends SurfaceView {

    private Context mContext;
    private SurfaceHolder mHolder;
    private Board mBoard = null;
    private float mWidth;
    private float mHeight;
    private float mScreenWidth;
    private float mScreenHeight;
    private float mImageDisplayWidth;
    private float mImageDisplayHeight;

    public BoardView(Context context, Board board){
        //call the parent constructor
        super(context);
        //get the content, board, and certain size metrics we're going to use in calculations
        mContext = context;
        this.mBoard = board;
        this.mWidth = mBoard.GetXSize();
        this.mHeight = mBoard.GetYSize();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mImageDisplayWidth = (mScreenWidth / mBoard.mXsize);
        mImageDisplayHeight = (mScreenHeight / mBoard.mYsize);

        //add a holder and callback object for the surface we're drawing to
        mHolder = getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                setWillNotDraw(false);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //lock the canvas whilst we draw to it
                Canvas canvas = holder.lockCanvas(null);
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    //default constructor
    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
    }

    //default constructor
    public BoardView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        mContext = context;
    }

    public boolean TouchEvent(MotionEvent event){
        //if it's not a mouse down event we don't care
        if(event.getAction() != MotionEvent.ACTION_DOWN){
            return super.onTouchEvent(event);
        }
        //if it is a mouse down, get the X and Y of the event
        int[] coordinates = GetRealXandY(event.getRawX(), event.getRawY());
        if(this.mBoard != null) { //check we have a board first
            if(mBoard.Move(coordinates[0], coordinates[1])) { //make the move and if it succeeds increment the move counter
                mBoard.IncrementMove();
            }
        }
        return true;
    }

    public int[] GetRealXandY(float paramX, float paramY){
        int actualX = (int)(paramX / mImageDisplayWidth); //divide the X of the touch by how wide the display of each image is and round down
        int actualY = (int)(paramY/ mImageDisplayHeight); //same as above but with height
        return new int[]{actualX, actualY}; //return int array cause Java has no ref parameters like C++ or C#
    }

    @Override
    protected void onDraw(Canvas canvas){
        //make a pure white background
        Paint background = new Paint();
        int colour = Color.parseColor("#FFFFFF");
        background.setColor(colour);

        //make a paint for adding grid lines to the display
        Paint stroke = new Paint();
        int strokeColor = Color.parseColor("#000000");
        stroke.setColor(strokeColor);
        stroke.setStrokeWidth(20);
        stroke.setStyle(Paint.Style.STROKE);
        int tileWidth = 200;
        int tileHeight = 200;
        float imageWidth = 0;
        float imageHeight = 0;

        //get the size of a random image (assuming there's one there)
        if(mBoard.getGridSpaceAtXY(1, 1).HasTile()) {
            imageWidth = mBoard.getGridSpaceAtXY(1, 1).GetTile().image.getWidth();
            imageHeight = mBoard.getGridSpaceAtXY(1, 1).GetTile().image.getWidth();
        } else {
            imageWidth = mBoard.getGridSpaceAtXY(1, 0).GetTile().image.getWidth();
            imageHeight = mBoard.getGridSpaceAtXY(1, 0).GetTile().image.getWidth();
        }

        //define a source rectangle from the bitmap which is the area to be drawn
        Rect srcRect = new Rect((int)0, (int)0, (int)imageWidth, (int)imageHeight);


        if(this.mBoard != null) { // if we have a board
            canvas.drawRect(0, 0, mScreenWidth, mScreenHeight, background); //draw our background
            float top = 0; //set the top offset to 0
            for(int y = 0; y < mBoard.mYsize; y++){
                float left = 0; //set the left offset to 0
                for(int x = 0; x < mBoard.mXsize; x++){
                    //define a rectangle for how the image will be drawn, and then offset it by how far along in the grid we are
                    Rect displayRect = new Rect((int)0, (int)0, (int) mImageDisplayWidth, (int) mImageDisplayHeight);
                    displayRect.offset((int)left, (int)top);
                    //draw the bitmap if there is one, or if not just draw some white
                    if(!mBoard.getGridSpaceAtXY(x, y).HasTile()) {
                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.white), srcRect, displayRect, stroke);
                    } else {
                        canvas.drawBitmap(mBoard.getGridSpaceAtXY(x, y).GetTile().GetImage(), srcRect, displayRect, stroke);
                    }
                    //move forward our offsets
                    left += mImageDisplayWidth;
                }
                top += mImageDisplayHeight;
            }
        }

        //construct a paint for drawing our text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setTextSize(100);
        float x = 20;
        float y = (mScreenWidth + 500);
        //draw the text and then draw it again for stroke effects
        canvas.drawText(String.format(mContext.getString(R.string.moves), mBoard.GetMoves()), x, y, textPaint);
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(7.5f);
        canvas.drawText(String.format(mContext.getString(R.string.moves), mBoard.GetMoves()), x, y, textPaint);
        invalidate();
    }

    //simply loops through the board, pulls the file name of each image and writes it to a JSON array
    public JSONArray CreateLayout(){
        JSONArray layout = new JSONArray();
        if(mBoard != null){
            for(int y = 0; y < mBoard.mYsize; y++){
                JSONArray row = new JSONArray();
                for(int x = 0; x < mBoard.mXsize; x++){
                    if(!mBoard.getGridSpaceAtXY(x, y).HasTile()){
                        row.put("empty");
                    } else {
                        row.put(mBoard.getGridSpaceAtXY(x, y).GetTile().GetId());
                    }
                }
                layout.put(row);
            }
        }
        return layout;
    }

    //returns how many moves have been made
    public int GetMoves(){
        return mBoard.GetMoves();
    }

    //returns whether the game has been won
    public boolean WonGame(JSONArray layout){
        return mBoard.WonGame(layout);
    }
}
