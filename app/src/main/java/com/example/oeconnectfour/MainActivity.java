package com.example.oeconnectfour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Game game = new Game();
    int arrowColumn = 3;    //Used to calculate position of next token to be dropped over board
    int numRows = 6;
    int numCols = 7;


    //Game class
    public class Game {
        Board board = new Board();
        Token currentToken = new Token(1);
        int currentTurn = 1;     //1 if player 1's turn and 2 if player 2's turn

        //Sets image of nextToken to appropriate image
        public void updateNextImage() {
            //Set ImageView image to strength of first token
            ImageView first = (ImageView) findViewById(R.id.nextToken);
            if(currentToken.strength == 6) {
                first.setImageResource(R.drawable.red1);
            }
            else if(currentToken.strength == 3) {
                first.setImageResource(R.drawable.red2);
            }
            else {
                first.setImageResource(R.drawable.red3);
            }
        }

        //Adds token to board and returns row that token ended up in
        public int dropToken() {
            //Change board
            int choiceRow = board.lowestRow(arrowColumn);
            //Pop up a toast to tell player that column is unavailable
            if(choiceRow == -1) {
                Toast.makeText(getApplicationContext(), "Column has no space", Toast.LENGTH_SHORT).show();
            }
            else {
                board.placeToken(choiceRow, arrowColumn, currentToken);
            }
            return choiceRow;
        }

        //Breaks all tokens that are under too much stress
        public void breakTokens(int col) {
            board.breakTokens(col);
        }

        //Changes turn and generates new token for currentToken
        public void changeTurn() {
            //Change player turn
            if(currentTurn == 1) {
                currentTurn = 2;
            }
            else {
                currentTurn = 1;
            }
            currentToken = new Token(currentTurn);
        }
    }
    //-------------------------------Game class------------------------------------

    //Board class
    /*
    Board class contains a 2D array of ints that store either -1 or 0 or 1
    1 indicates player 1's coin
    0 indicates unoccupied
    2 indicates player 2's coin
     */
    public class Board {
        Token[][] board = new Token[6][7];

        public Board() {
            for (Token[] row : board) {
                Arrays.fill(row, new Token(0));
            }
            /*
            for(int i = 0; i < numRows; ++i) {
                for(int j = 0; j < numCols; ++j) {
                    board[i][j] = new Token(0);
                }
            }
            */
            //Token[] row = new Token[7];
            //Arrays.fill(row, new Token(0));
            //Arrays.fill(board, row);
        }

        //Returns 1 if player 1 has a winning combo, 2 if player 2, 0 if neither, 3 if both (tie)
        public int checkWinner() {
            boolean playerOneWin = false;
            boolean playerTwoWin = false;
            int winner = 0;
            //For every coordinate, check in every direction to look for sequence of 4
            for(int i = 0; i < numRows; ++i) {
                for(int j = 0; j < numCols; ++j) {
                    winner = ifWinningSequence(i, j);
                    if(winner == 1) {
                        playerOneWin = true;
                    }
                    else if(winner == 2) {
                        playerTwoWin = true;
                    }
                }
            }
            if(playerOneWin && playerTwoWin) {
                return 3;
            }
            else if(playerOneWin) {
                return 1;
            }
            else if(playerTwoWin) {
                return 2;
            }
            return 0;
        }

        //Places token into input position
        public void placeToken(int row, int col, Token token) {
            board[row][col].player = token.player;
            board[row][col].strength = token.strength;
        }

        //Breaks all tokens that need breaking
        public void breakTokens(int col) {
            int tokensToBreak = 0;
            int lowRow = 0;
            //Loop through necessary rows starting from bottom and ending at row 2
            //Row 2 because the weakest token will break only with 2 tokens on top of it
            for(int j = 5; j > 2; --j) {
                //If no player token is at this location, no need to look at rows above
                //Break from inner loop and move on
                if(board[j][col].player == 0) {
                    break;
                }
                //If player token at [j][col] is breakable, count how many tokens are above it
                if(board[j][col].strength != 6) {
                    int tokenCount = 0;
                    for(int k = j - 1; k > 0; --k) {
                        if(board[k][col].player != 0) {
                            tokenCount++;
                        }
                    }
                    //If there are more tokens than this token can support, move all tokens above it down 1 spot and insert unoccupied spot in [0][col]
                    if(tokenCount > board[j][col].strength) {
                        tokensToBreak++;
                        if(lowRow != 0) {
                            lowRow = j;
                        }
                    }
                }
            }
            if(tokensToBreak > 0) {
                //Break everything in one go to make sure no breaking affects other breaks
                collapseTokens(lowRow, col, tokensToBreak);
            }
        }

        //Returns row index of column that newly dropped token would end up in
        //If column has no room available, return -1 and crash the app
        public int lowestRow(int column) {
            for(int i = 5; i > 0; i--) {
                if(board[i][column].player == 0) {
                    return i;
                }
            }
            return -1;
        }

        //--------------------Helper functions--------------------------------------
        //Returns 1 if player 1 has a winning sequence in row
        //2 if player 2 has a winning sequence in row
        //0 if neither
        private int ifWinningSequence(int row, int col) {
            int sequenceElt = board[row][col].player;
            boolean sequenceFound = true;
            //If space is empty then return 0
            if(sequenceElt == 0) {
                return 0;
            }
            //Up, only check if row 3 or below
            if(row >= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row - i][col].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            sequenceFound = true;
            //Down, only check if row 2 or above
            if(row <= 2) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row + i][col].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Left, only check if column 3 or to the right
            if(col >= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row][col - i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Right, only check if column 3 or to the left
            if(col <= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row][col + i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Up and Left, only check if row and column are greater or equal to 3
            if(row >= 3 && col >= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row - i][col - i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Up and Right, only check if row geq 3 and column leq 3
            if(row >= 3 && col <= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row - i][col + i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Down and Left, only check if row leq 2 and column geq 3
            if(row <= 2 && col >= 3) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row + i][col - i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            //Down and Right, only check if row and column leq 2
            if(row <= 2 && col <= 2) {
                for(int i = 1; i < 4; ++i) {
                    if(board[row + i][col + i].player != sequenceElt) {
                        sequenceFound = false;
                        break;
                    }
                }
                if(sequenceFound) {
                    return sequenceElt;
                }
            }
            return 0;
        }

        //Takes in the position of lowest token that will break and number of tokens breaking (1 or 2)
        private void collapseTokens(int row, int col, int numberBreaking) {
            //TODO: Delete broken tokens

            //TODO: Move tokens from [row - numberBreaking][col] and above, down by numberBreaking * 48dp

            //Update board
            for(int i = row; i > numberBreaking; --i) {
                //Make a deep copy
                board[i][col].player = board[i - numberBreaking][col].player;
                board[i][col].strength = board[i - numberBreaking][col].strength;
            }
            board[0][col] = new Token(0);
            if(numberBreaking == 2) {
                board[1][col] = new Token(0);
            }
        }
    }
    //---------------------------------Board Class----------------------------------------

    public class Token {
        int player = 0;
        int strength = 0;
        Random random = new Random();
        //Create token for player 1 or 2 and give it a strength
        public Token(int playerIndex) {
            this.player = playerIndex;
            if(playerIndex == 0) {
                this.strength = 6;
            }
            else {
                int randInt = random.nextInt(10);
                if(randInt >= 0 && randInt <= 1) {
                    this.strength = 6;
                }
                else if(randInt >= 2 && randInt <= 6) {
                    this.strength = 3;
                }
                else {
                    this.strength = 2;
                }
            }
        }
    }

    public void arrowLeft(View view) {
        if(arrowColumn > 0) {
            arrowColumn--;
            //Move image to new position
            ImageView nextToken = findViewById(R.id.nextToken);
            nextToken.animate().translationXBy(-168).setDuration(100);
        }
    }

    public void arrowRight(View view) {
        if(arrowColumn < 6) {
            arrowColumn++;
            //Move image to new position
            ImageView nextToken = findViewById(R.id.nextToken);
            nextToken.animate().translationXBy(168).setDuration(100);
        }
    }

    public void tokenDrop(View view) {
        int boxHeight = 168;        //Used to calculate how far imageview has to be translated by

        //Change the board member variable in game object
        int tokenRow = game.dropToken();

        //Only if valid column was chosen by player
        if(tokenRow != -1) {
            //Disable drop button and arrow buttons to prevent player from messing things up as token is dropping
            Button drop = findViewById(R.id.drop);
            Button leftButton = findViewById(R.id.left);
            Button rightButton = findViewById(R.id.right);
            drop.setEnabled(false);
            leftButton.setEnabled(false);
            rightButton.setEnabled(false);

            //Store position of nextToken (token hanging over the board)
            ImageView nextToken = findViewById(R.id.nextToken);
            float originalX = nextToken.getX();
            float originalY = nextToken.getY();

            //Create actual token that is to be dropped
            ImageView droppedToken = new ImageView(this);
            //If player 1's turn
            if(game.currentTurn == 1) {
                //Give token appropriate image
                if(game.currentToken.strength == 6) {
                    droppedToken.setImageResource(R.drawable.red1);
                }
                else if(game.currentToken.strength == 3) {
                    droppedToken.setImageResource(R.drawable.red2);
                }
                else {
                    droppedToken.setImageResource(R.drawable.red3);
                }
            }
            //If player 2's turn
            else {
                //Give token appropriate image
                if (game.currentToken.strength == 6) {
                    droppedToken.setImageResource(R.drawable.yellow1);
                } else if (game.currentToken.strength == 3) {
                    droppedToken.setImageResource(R.drawable.yellow2);
                } else {
                    droppedToken.setImageResource(R.drawable.yellow3);
                }
            }
            //Put droppedToken in the same position as nextToken
            //droppedToken.setX(originalX);
            //droppedToken.setY(originalY);
            droppedToken.setLayoutParams(nextToken.getLayoutParams());  //Copy layout params
            //Add droppedToken to the layout
            ConstraintLayout overall = findViewById(R.id.overall);
            overall.addView(droppedToken);

            //Make nextToken transparent as droppedToken is falling into position
            nextToken.setAlpha(0.0f);

            //TODO: Animate droppedToken dropping into position
            //Use boxHeight and tokenRow to calculate how far token has to travel
            int distanceMoved = boxHeight * (tokenRow + 1);
            droppedToken.animate().translationYBy(distanceMoved).setDuration(100);

            //TODO: Change image on board to same as droppedToken

            //TODO: Break any tokens that need breaking
            //game.breakTokens(arrowColumn);

            //Reset arrowColumn back to 3
            //arrowColumn = 3;

            //Change turn
            game.changeTurn();

            //Change nextToken image source and make opaque once more
            //If player 1's turn
            if(game.currentTurn == 1) {
                //Give token appropriate image
                if(game.currentToken.strength == 6) {
                    nextToken.setImageResource(R.drawable.red1);
                }
                else if(game.currentToken.strength == 3) {
                    nextToken.setImageResource(R.drawable.red2);
                }
                else {
                    nextToken.setImageResource(R.drawable.red3);
                }
            }
            //If player 2's turn
            else {
                //Give token appropriate image
                if (game.currentToken.strength == 6) {
                    nextToken.setImageResource(R.drawable.yellow1);
                } else if (game.currentToken.strength == 3) {
                    nextToken.setImageResource(R.drawable.yellow2);
                } else {
                    nextToken.setImageResource(R.drawable.yellow3);
                }
            }
            nextToken.setAlpha(1.0f);      //Make nextToken opaque once more

            //Enable drop and arrow buttons
            drop.setEnabled(true);
            leftButton.setEnabled(true);
            rightButton.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        game.updateNextImage();     //Figures out which image resource to add to the first nextToken
    }
}
