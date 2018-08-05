package mobile.labs.acw;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;

public class Board {
    Context mContext;
    int mXsize;
    int mYsize;
    private int mCurrentScore;
    BoardPosition[][] board;

    public Board(JSONArray layout, Context context, String fileDir, Integer level){
        try {
            //get the X and Y size of our new game and the attaching context
            mYsize = layout.length();
            mXsize = layout.getJSONArray(0).length();
            mContext = context;
            board = new BoardPosition[mYsize][mXsize];

            //define a new board, and go through to build the board positions and their corresponding tile images
            for(int y = 0; y < layout.length(); y++){
                JSONArray row = layout.getJSONArray(y);
                for(int x = 0; x < row.length(); x++){
                    if(row.getString(x).equals("empty")){
                        board[y][x] = new BoardPosition(x, y, null);
                    } else {
                        String folderPath = String.format("puzzle%d.json", level);
                        board[y][x] = new BoardPosition(x, y, new Tile(mContext, BitmapFactory.decodeFile(String.format("%s/images/%s/%s.jpg",
                                fileDir, folderPath, row.getString(x))), row.getString(x)));
                    }
                }
            }
        } catch(Exception e){
            Log.w("Failed to build board", e.getMessage());
        }
    }

    //checks the X and Y being moved by the player, and creates a list of co-ordinates to check based on possible edge cases
    public boolean Move(int chosenX, int chosenY){
        ArrayList<Integer> checkCoordinates = new ArrayList<Integer>();
        if(chosenX >= mXsize || chosenX < 0 || chosenY >= mYsize || chosenY < 0){
            return false;
        }
        if(chosenX == 0){
            if(chosenY == 0){
                checkCoordinates.add(1);
                checkCoordinates.add(0);
                checkCoordinates.add(0);
                checkCoordinates.add(1);
            }
            else if(chosenY == mYsize - 1){
                checkCoordinates.add(1);
                checkCoordinates.add(chosenY);
                checkCoordinates.add(0);
                checkCoordinates.add(chosenY - 1);
            }
            else{
                checkCoordinates.add(1);
                checkCoordinates.add(chosenY);
                checkCoordinates.add(0);
                checkCoordinates.add(chosenY - 1);
                checkCoordinates.add(0);
                checkCoordinates.add(chosenY + 1);
            }
        }
        else if(chosenX == mXsize - 1){
            if(chosenY == 0){
                checkCoordinates.add(chosenX-1);
                checkCoordinates.add(0);
                checkCoordinates.add(chosenX);
                checkCoordinates.add(1);
            }
            else if(chosenY == mYsize - 1){
                checkCoordinates.add(chosenX-1);
                checkCoordinates.add(chosenY);
                checkCoordinates.add(chosenX);
                checkCoordinates.add(chosenY-1);
            }
            else{
                checkCoordinates.add(chosenX - 1);
                checkCoordinates.add(chosenY);
                checkCoordinates.add(chosenX);
                checkCoordinates.add(chosenY - 1);
                checkCoordinates.add(chosenX);
                checkCoordinates.add(chosenY + 1);
            }
        }
        else if(chosenY == 0){
            checkCoordinates.add(chosenX + 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX - 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX);
            checkCoordinates.add(chosenY + 1);
        }
        else if(chosenY == mYsize - 1){
            checkCoordinates.add(chosenX + 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX - 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX);
            checkCoordinates.add(chosenY-1);
        }
        else{
            checkCoordinates.add(chosenX - 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX + 1);
            checkCoordinates.add(chosenY);
            checkCoordinates.add(chosenX);
            checkCoordinates.add(chosenY - 1);
            checkCoordinates.add(chosenX);
            checkCoordinates.add(chosenY + 1);
        }
        //check a move can be made
        return MakeMove(checkCoordinates, chosenX, chosenY);
    }

    private boolean MakeMove(ArrayList<Integer> listOfCoords, int myX, int myY){
        try {
            for (int i = 0; i < listOfCoords.size() - 1; i += 2) { //loop through the array
                if (!board[listOfCoords.get(i + 1)][listOfCoords.get(i)].HasTile()) { //check we don't have a tile at the X and Y specified
                    Tile temp = board[myY][myX].GetTile(); //if not, move the current tile to a temp variable and make the swap
                    board[myY][myX].SetTile(null);
                    board[listOfCoords.get(i + 1)][listOfCoords.get(i)].SetTile(temp);
                    return true;
                }
            }
        } catch (Exception e){
            Log.w("Failed to move", e.getMessage());
        }
        return false;
    }

    //takes in a JSON array and checks the layout to see if it matches predefined win conditions
    public boolean WonGame(JSONArray currentPosition){
        try {
            if (mXsize == 3 && mYsize == 3) {
                JSONArray winArray = new JSONArray("[[\"empty\",\"21\",\"31\"],[\"12\",\"22\",\"32\"],[\"13\",\"23\",\"33\"]]");
                return currentPosition.equals(winArray);
            } else if (mXsize == 4 && mYsize == 3){
                JSONArray winArray = new JSONArray("[[\"empty\",\"21\",\"31\",\"41\"],[\"12\",\"22\",\"32\",\"42\"],[\"13\",\"23\",\"33\",\"43\"]]");
                return currentPosition.equals(winArray);
            } else if(mXsize == 3 && mYsize == 4){
                JSONArray winArray = new JSONArray("[[\"empty\",\"21\",\"31\"],[\"12\",\"22\",\"32\"],[\"13\",\"23\",\"33\"],[\"14\",\"24\",\"34\"]]");
                return currentPosition.equals(winArray);
            } else {
                JSONArray winArray = new JSONArray("[[\"empty\",\"21\",\"31\",\"41\"],[\"12\",\"22\",\"32\",\"42\"],[\"13\",\"23\",\"33\",\"43\"],[\"14\",\"24\",\"34\",\"44\"]]");
                return currentPosition.equals(winArray);
            }
        } catch (Exception e){
            Log.w("Failed to check for win", e.getMessage());
        }
        return false;
    }

    //increment the move counter
    public void IncrementMove(){
        mCurrentScore++;
    }

    //get the number of moves
    public int GetMoves(){
        return mCurrentScore;
    }

    //get the boardposition object at a given X and Y
    public BoardPosition getGridSpaceAtXY(int x, int y){
        return board[y][x];
    }

    //get the X size of the board
    public int GetXSize(){
        return mXsize;
    }

    //get the Y size of the board
    public int GetYSize(){
        return mYsize;
    }
}
