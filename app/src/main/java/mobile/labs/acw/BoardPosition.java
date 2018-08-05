package mobile.labs.acw;

public class BoardPosition {
    private final int x;
    private final int y;
    private Tile mTile;

    public BoardPosition(int x, int y, Tile pTile){
        //set the X and Y this space operates and the bitmap on top of it
        this.x = x;
        this.y = y;
        this.mTile = pTile;
    }

    //gets the X
    public int GetX(){
        return x;
    }

    //gets the Y
    public int GetY(){
        return y;
    }

    //do we have a tile at this position?
    public boolean HasTile(){
        return mTile != null;
    }

    //return the tile at this poistion
    public Tile GetTile(){
        return mTile;
    }

    //set the tile at this position
    public void SetTile(Tile newTile){
        this.mTile = newTile;
    }
}
