class TicTacToe {

  int[] row0;
  int[] row1;
  int[] row2;
  int whoseturn;
  int movesmade;
  int[] pieces = {1,2};

  // Initializes a Tic Tac Toe object.
  public boolean init() {
    row0 = new int[3];
    row1 = new int[3];
    row2 = new int[3];

    whoseturn = 0;
    movesmade = 0;
    return true;
  }

  public boolean MoveRow(int[] row, int column) {
    boolean moved;

    if(column < 0){
        moved = false;
    }else if(2 < column){
        moved = false;
    }else if(0 < row[column]){
        moved = false;
    }
    else{
        row[column] = pieces[whoseturn];
        movesmade=movesmade+1;
        moved = true;
    }
    return moved;
  }
  // Tries to make a move at row, column. If it's valid the move is made
  // and true is returned. Else nothing is done and false is returned.

  public boolean Move(int row, int column) {
    boolean mov;
    if(row == 0){
        mov = MoveRow(row0,column);
    }else if (row == 1){
        mov = MoveRow(row1,column);
    }
    else if (row == 2){
        mov = MoveRow(row2,column);
    }else{
        mov = false;
    }
    return mov;
  }

  // Returns true if indexes passed to the method are inbounds.
  public boolean inbounds(int row, int column) {
    boolean in;
    in = true;
    if (row < 0)
      in =  false;
    else if (column < 0)
      in =  false;
    else if (2 < row) 
      in =  false;
    else if (2 < column) 
      in = false;
    return in;
  }

  // Changes whose turn it is.
  public boolean changeturn() {
    whoseturn = 1 - whoseturn;
    return true;
  }

  // Returns the current player's name.
  public int getCurrentPlayer() {
    return whoseturn+1;
  }



  // Returns a character signifying the winner.
  public int winner() {
    int[] array;
    int winner;
    int i;
    winner = 0-1;
    array = new int[3];
    // Check for three X's or O's in a row.
    if (BoardBase.sameArray(row0) && 0 < row0[0]){
        winner = row0[0];
    }
    else if (BoardBase.sameArray(row1) && 0 < row1[0]){
        winner =  row1[0];
    }
    else if (BoardBase.sameArray(row2) && 0 < row2[0]){
        winner =  row2[0];
    } 
    else{
        i = 0;
        while(winner < 1 && i < 3){
            array[0] = row0[i];
            array[1] = row1[i];
            array[2] = row2[i];
            if (BoardBase.sameArray(array) && 0 < array[0]){
                winner = array[0];
            }
            i=i+1;
        }
        if(winner < 1){
            array[0] = row0[0];
            array[1] = row1[1];
            array[2] = row2[2];
            if (BoardBase.sameArray(array) && 0 < array[0]){
                winner = array[0];
            }else{
                array[0] = row0[2];
                array[1] = row1[1];
                array[2] = row2[0];
                if (BoardBase.sameArray(array) && 0 < array[0]){
                    winner = array[0];
                }
            }
        }
    }
    if (winner < 1 && movesmade == 9)
        winner = 0;
    return winner;
  }


  public static void main(String[] args) {
    // Create the TicTacToe object.
    TicTacToe mygame;
    int win;
    boolean done;
    int[] move;
    int player;
    mygame = new TicTacToe();
    mygame.init();
    // Play as long as there is no winner or tie.
    while (mygame.winner() == -1) {
      done = false;
      // Read in a move & check if it's valid.
      while(!done){
	
        BoardBase.printBoard(mygame.row0, mygame.row1, mygame.row2);

        player = mygame.getCurrentPlayer();            
        move = BoardBase.playerTurn(player);
	    
      		
        if (!mygame.inbounds(move[0],move[1])) 
          BoardBase.wrongMove();
        else {
          if (!mygame.Move(move[0],move[1]))
             BoardBase.placeTaken();
          else
            done = true;
       } 
      }
	
      // Change who's turn it is.
      mygame.changeturn();
    }

    // Print out a message with the winner.
    BoardBase.printBoard(mygame.row0, mygame.row1, mygame.row2);
    win = mygame.winner();

    BoardBase.printWinner(win);

  }
}
