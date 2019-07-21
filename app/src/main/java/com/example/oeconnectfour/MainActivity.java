package com.example.oeconnectfour;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Game game;
    float leftTopX = 147.0f;
    float leftTopY = 350.0f;
    float distanceBetweenCells = 168.0f;
    int arrowColumn = 3;    //Used to calculate position of next token to be dropped over board
    int numRows = 6;
    int numCols = 7;

    //Game class
    public class Game {
        Board board = new Board();
        Token currentToken = new Token(1);
        int currentTurn = 1;     //1 if player 1's turn and 2 if player 2's turn

        public int checkWinner() {
            return board.checkWinner();
        }

        public void resetGame() {
            board.resetBoard();
            currentTurn = 1;
            currentToken = new Token(1);
        }

        //Sets image of nextToken to appropriate image
        public void updateNextImage() {
            //Set ImageView image to strength of first token
            ImageView first = findViewById(R.id.nextToken);
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

        //Updates cell image to same as droppedToken
        public void updateBoardToDropped(int row, int col, int droppedTokenImageRes) {
            board.changeCellImage(row, col, droppedTokenImageRes);
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

        //Puts nextToken back over column 3
        public void resetNextTokenPosition() {
            arrowColumn = 3;
            ImageView nextToken = findViewById(R.id.nextToken);
            nextToken.setX(leftTopX + arrowColumn * distanceBetweenCells);
            nextToken.setY(leftTopY - distanceBetweenCells);
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
            //Add tokenImage to the layout
            ConstraintLayout overall = findViewById(R.id.overall);
            for(int i = 0; i < 6; ++i) {
                for(int j = 0; j < 7; ++j) {
                    Token temp = new Token(0);
                    temp.tokenImage.setAdjustViewBounds(true);
                    //Set the Token's position on the board
                    temp.tokenImage.setX(leftTopX + (float) j * distanceBetweenCells);
                    temp.tokenImage.setY(leftTopY + (float) i * distanceBetweenCells);
                    temp.tokenImage.setImageResource(R.drawable.unoccupied);
                    temp.tokenImage.setMaxHeight(140);
                    temp.tokenImage.setMaxWidth(140);
                    board[i][j] = temp;
                    overall.addView(board[i][j].tokenImage);
                }
            }
        }

        //Clears board and resets variables
        public void resetBoard() {
            for(int i = 0; i < 6; ++i) {
                for(int j = 0; j < 7; ++j) {
                    board[i][j].player = 0;
                    board[i][j].strength = 6;
                    board[i][j].tokenImage.setImageResource(R.drawable.unoccupied);
                }
            }
        }

        //Returns 1 if player 1 has a winning combo, 2 if player 2, 0 if neither, 3 if both (tie)
        public int checkWinner() {
            boolean playerOneWin = false;
            boolean playerTwoWin = false;
            int winner = 0;
            //For every coordinate, check in every direction to look for sequence of 4
            for(int i = 0; i < 6; ++i) {
                for(int j = 0; j < 7; ++j) {
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
            //board[row][col].tokenImage = token.tokenImage;
            board[row][col].player = token.player;
            board[row][col].strength = token.strength;
        }

        //Changes ImageView of Token at cell to input
        public void changeCellImage(int row, int col, int resource) {
            board[row][col].tokenImage.setImageResource(resource);
            //Set tokenImage's size
            board[row][col].tokenImage.setAdjustViewBounds(true);
            board[row][col].tokenImage.setMaxHeight(140);
            board[row][col].tokenImage.setMaxWidth(140);
            board[row][col].tokenImage.setX(leftTopX + col * distanceBetweenCells);
            board[row][col].tokenImage.setY(leftTopY + row * distanceBetweenCells);
        }

        //Breaks all tokens that need breaking
        public void breakTokens(int col) {
            int tokenCount;
            int topTokenRow;
            int i = 5;
            while(i >= 0) {
                topTokenRow = lowestRow(col);
                tokenCount = i - topTokenRow - 1;
                //Check if number of tokens exceeds token strength
                if(tokenCount > board[i][col].strength) {
                    //Fade in explosion image and fade it out again
                    //board[i][col].explosionImage.animate().alpha(1.0f).setDuration(100);
                    //board[i][col].explosionImage.animate().alpha(0.0f).setDuration(100);
                    //Fade broken token away
                    AlphaAnimation fadeBroken = new AlphaAnimation(1.0f, 0.0f);
                    fadeBroken.setDuration(200);
                    board[i][col].tokenImage.startAnimation(fadeBroken);

                    //Move all tokens above it down by one spot
                    for(int j = i; j > 0; --j) {
                        //TranslateAnimation moveDown = new TranslateAnimation(board[j][col].tokenImage.getX(), board[j][col].tokenImage.getX(), board[j][col].tokenImage.getY(), board[j][col].tokenImage.getY() + distanceBetweenCells);
                        TranslateAnimation moveDown = new TranslateAnimation(0, 0, 0, distanceBetweenCells);
                        moveDown.setDuration(200);
                        moveDown.setStartOffset(200);
                        board[j][col].tokenImage.startAnimation(moveDown);

                        final int finalCol = col;
                        final int finalRow = j;
                        moveDown.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                //some code to make it wait here?
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if(board[finalRow - 1][finalCol].player == 1) {
                                    if(board[finalRow - 1][finalCol].strength == 6) {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.red1);
                                    }
                                    else if(board[finalRow - 1][finalCol].strength == 3) {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.red2);
                                    }
                                    else {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.red3);
                                    }
                                }
                                else if(board[finalRow - 1][finalCol].player == 2) {
                                    if(board[finalRow - 1][finalCol].strength == 6) {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.yellow1);
                                    }
                                    else if(board[finalRow - 1][finalCol].strength == 3) {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.yellow2);
                                    }
                                    else {
                                        board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.yellow3);
                                    }
                                }
                                else {
                                    board[finalRow][finalCol].tokenImage.setImageResource(R.drawable.unoccupied);
                                }
                                //board[j][col].tokenImage = board[j - 1][col].tokenImage;
                                board[finalRow][finalCol].player = board[finalRow - 1][finalCol].player;
                                board[finalRow][finalCol].strength = board[finalRow - 1][finalCol].strength;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }
                    //Insert new unoccupied token in row 0
                    board[0][col].player = 0;
                    board[0][col].strength = 6;
                    board[0][col].tokenImage.setImageResource(R.drawable.unoccupied);
                    //Reset i to 5
                    i = 5;
                    return;
                }
                else {
                    i--;
                }

            }
            return;
            /*
            for(int i = 5; i >= 0; --i) {
                topTokenRow = lowestRow(col);
                //Only check if the strength is not 6
                if(board[i][col].strength != 6) {
                    tokenCount = i - topTokenRow - 1;
                    //Check if number of tokens exceeds token strength
                    if(tokenCount > board[i][col].strength) {
                        //Fade broken token away
                        //board[i][col].tokenImage.animate().alpha(0.0f).setDuration(200);
                        //Move all tokens above it down by one spot
                        for(int j = i; j > 0; --j) {
                            //board[j][col].tokenImage.animate().translationYBy(distanceBetweenCells);
                            if(board[j - 1][col].player == 1) {
                                if(board[j - 1][col].strength == 6) {
                                    board[j][col].tokenImage.setImageResource(R.drawable.red1);
                                }
                                else if(board[j - 1][col].strength == 3) {
                                    board[j][col].tokenImage.setImageResource(R.drawable.red2);
                                }
                                else {
                                    board[j][col].tokenImage.setImageResource(R.drawable.red3);
                                }
                            }
                            else if(board[j - 1][col].player == 2) {
                                if(board[j - 1][col].strength == 6) {
                                    board[j][col].tokenImage.setImageResource(R.drawable.yellow1);
                                }
                                else if(board[j - 1][col].strength == 3) {
                                    board[j][col].tokenImage.setImageResource(R.drawable.yellow2);
                                }
                                else {
                                    board[j][col].tokenImage.setImageResource(R.drawable.yellow3);
                                }
                            }
                            else {
                                board[j][col].tokenImage.setImageResource(R.drawable.unoccupied);
                            }
                            //board[j][col].tokenImage = board[j - 1][col].tokenImage;
                            board[j][col].player = board[j - 1][col].player;
                            board[j][col].strength = board[j - 1][col].strength;
                        }
                        //Insert new unoccupied token in row 0
                        board[0][col].player = 0;
                        board[0][col].strength = 6;
                        board[0][col].tokenImage.setImageResource(R.drawable.unoccupied);
                        //Reset i to 5
                        i = 5;
                    }
                }
            }
            */
            //Log.i("Info", "Breaking");
            /*
            int tokensToBreak = 0;
            int lowRow = 0;
            //Loop through necessary rows starting from bottom and ending at row 2
            //Row 2 because the weakest token will break only with 2 tokens on top of it
            for(int i = 5; i >= 2; --i) {
                //If no player token is at this location, no need to look at rows above
                //Break from inner loop and move on
                //if(board[i][col].player == 0) {
                    //break;
                //}
                //If player token at [j][col] is breakable, count how many tokens are above it
                if(board[i][col].strength != 6) {
                    int tokenCount = 0;
                    for(int k = i - 1; k > 0; --k) {
                        if(board[k][col].player != 0) {
                            tokenCount++;
                        }
                    }
                    //If there are more tokens than this token can support
                    if(tokenCount > board[i][col].strength) {
                        tokensToBreak++;
                        if(lowRow == 0) {
                            lowRow = i;
                        }
                    }
                }
            }
            if(tokensToBreak > 0) {
                //Break everything in one go to make sure no breaking affects other breaks
                collapseTokens(lowRow, col, tokensToBreak);
            }
            */
        }

        //Returns row index of column that newly dropped token would end up in
        //If column has no room available, return -1 and crash the app
        public int lowestRow(int column) {
            for(int i = 5; i >= 0; i--) {
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
            sequenceFound = true;
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
            sequenceFound = true;
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
            sequenceFound = true;
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
            sequenceFound = true;
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
            sequenceFound = true;
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
            sequenceFound = true;
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
            //Fade images of broken tokens away
            for(int i = 0; i < numberBreaking; i++) {
                board[row - i][col].tokenImage.animate().alpha(0.0f).setDuration(200);
            }
            //TODO: Move tokens from [row - numberBreaking][col] and above, down by numberBreaking * distanceBetweenCells
            //Update board
            for(int i = row; i >= numberBreaking; i--) {
                //Make sure when i == 1 and numberBreaking == 2, i - numberBreaking will screw things up
                //Animate token dropping down
                board[i - numberBreaking][col].tokenImage.animate().translationYBy(distanceBetweenCells * (float) numberBreaking);
                //placeToken(i, col, board[i - numberBreaking][col]);
                //Actually set the variable in the board
                board[i][col].tokenImage = board[i - numberBreaking][col].tokenImage;
                //Make a deep copy of the other variables
                board[i][col].player = board[i - numberBreaking][col].player;
                board[i][col].strength = board[i - numberBreaking][col].strength;
            }
            //Remove views from top row to row just above top token
            //ConstraintLayout overall = findViewById(R.id.overall);
            int topTokenRow = lowestRow(col);
            for(int i = 0; i < topTokenRow; ++i) {
                //overall.removeView(board[i][col].tokenImage);
                board[i][col].player = 0;
                board[i][col].strength = 6;
            }
            Log.i("Info", "Collapsed");
        }
    }
    //---------------------------------Board Class----------------------------------------

    public class Token {
        int player = 0;
        int strength = 0;
        Random random = new Random();
        ImageView tokenImage = new ImageView(getApplicationContext());
        ImageView explosionImage = new ImageView(getApplicationContext());
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
            explosionImage.setImageResource(R.drawable.explosion);
            explosionImage.setAlpha(0.0f);
        }
    }

    public void arrowLeft(View view) {
        if(arrowColumn > 0) {
            arrowColumn--;
            //Move image to new position
            ImageView nextToken = findViewById(R.id.nextToken);
            nextToken.animate().translationXBy(-168).setDuration(50);
            nextToken.setX(leftTopX + arrowColumn * distanceBetweenCells);
        }
    }

    public void arrowRight(View view) {
        if(arrowColumn < 6) {
            arrowColumn++;
            //Move image to new position
            ImageView nextToken = findViewById(R.id.nextToken);
            nextToken.animate().translationXBy(168).setDuration(50);
            nextToken.setX(leftTopX + arrowColumn * distanceBetweenCells);
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

            //Create actual token that is to be dropped
            ImageView droppedToken = new ImageView(this);
            int droppedTokenImageRes;
            //If player 1's turn
            if(game.currentTurn == 1) {
                //Give token appropriate image
                if(game.currentToken.strength == 6) {
                    droppedToken.setImageResource(R.drawable.red1);
                    droppedTokenImageRes = R.drawable.red1;
                }
                else if(game.currentToken.strength == 3) {
                    droppedToken.setImageResource(R.drawable.red2);
                    droppedTokenImageRes = R.drawable.red2;
                }
                else {
                    droppedToken.setImageResource(R.drawable.red3);
                    droppedTokenImageRes = R.drawable.red3;
                }
            }
            //If player 2's turn
            else {
                //Give token appropriate image
                if (game.currentToken.strength == 6) {
                    droppedToken.setImageResource(R.drawable.yellow1);
                    droppedTokenImageRes = R.drawable.yellow1;
                } else if (game.currentToken.strength == 3) {
                    droppedToken.setImageResource(R.drawable.yellow2);
                    droppedTokenImageRes = R.drawable.yellow2;
                } else {
                    droppedToken.setImageResource(R.drawable.yellow3);
                    droppedTokenImageRes = R.drawable.yellow3;
                }
            }
            //Put droppedToken in the same position as nextToken
            droppedToken.setAdjustViewBounds(true);
            droppedToken.setX(147.0f + arrowColumn * distanceBetweenCells);
            droppedToken.setY(182.0f);
            droppedToken.setMaxHeight(140);
            droppedToken.setMaxWidth(140);

            //Add droppedToken to the layout
            ConstraintLayout overall = findViewById(R.id.overall);
            overall.addView(droppedToken);

            //Make nextToken transparent as droppedToken is falling into position
            nextToken.setAlpha(0.0f);

            //Animate droppedToken dropping into position
            //Use boxHeight and tokenRow to calculate how far token has to travel
            int distanceMoved = boxHeight * (tokenRow + 1);
            droppedToken.animate().translationYBy(distanceMoved).setDuration(100);

            //Change image on board to same as droppedToken
            game.updateBoardToDropped(tokenRow, arrowColumn, droppedTokenImageRes);

            //Delete droppedToken
            overall.removeView(droppedToken);

            //TODO: Break any tokens that need breaking. Animations need work
            game.breakTokens(arrowColumn);

            //Reset arrowColumn back to column 3
            //game.resetNextTokenPosition();

            //Check for winner, giving priority to player whose turn it is
            //If there is a winner, stop game and display winner. Display button to clear the board and restart game
            int winner = game.checkWinner();
            //If both have winning sequences, winner is whoever dropped the last token
            if(winner == 3) {
                winner = game.currentTurn;
            }

            //If no winner, just change turn and keep playing
            if(winner == 0) {
                //Change turn
                game.changeTurn();
            }
            //If there is a winner, display message and enable button to restart game
            else {
                TextView winnerMessage = findViewById(R.id.winnerText);
                winnerMessage.setAlpha(1.0f);
                if(winner == 1) {
                    winnerMessage.setText(R.string.winnerOne);
                }
                else {
                    winnerMessage.setText(R.string.winnerTwo);
                }
                winnerMessage.setEnabled(true);
                //Show restart button
                Button restartButton = findViewById(R.id.restartButton);
                restartButton.setEnabled(true);
                restartButton.setAlpha(1.0f);
                //Hide next token
                nextToken.setEnabled(false);
                nextToken.setAlpha(0.0f);
            }

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

    public void restartGame(View view) {
        //Hide winner message
        TextView winnerMessage = findViewById(R.id.winnerText);
        winnerMessage.setEnabled(false);
        winnerMessage.setAlpha(0.0f);
        //Hide restart button
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setEnabled(false);
        restartButton.setAlpha(0.0f);
        //Show next token
        ImageView nextToken = findViewById(R.id.nextToken);
        nextToken.setEnabled(true);
        nextToken.setAlpha(1.0f);
        game.resetGame();
        game.resetNextTokenPosition();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        game = new Game();
    }

    @Override
    protected void onStart() {
        super.onStart();
        game.updateNextImage();     //Figures out which image resource to add to the first nextToken
        TextView winnerMessage = findViewById(R.id.winnerText);
        winnerMessage.setEnabled(false);        //Hide winner message by default
        winnerMessage.setAlpha(0.0f);
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setEnabled(false);
        restartButton.setAlpha(0.0f);
    }
}
