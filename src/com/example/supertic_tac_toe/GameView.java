package com.example.supertic_tac_toe;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends Activity {
	
	// Members ******************
		
		private enum MoveType { X, O, Empty, WinPath;
			
			public String getLabel(Context context)
			{
				Resources res = context.getResources();
		        int resId = res.getIdentifier(this.name(), "string", context.getPackageName());
		        if (0 != resId) {
		            return (res.getString(resId));
		        }
		        return (name());
			}
		
		};
		
		public class Move {
			
			private MoveType _type;
			private boolean _winMove;
			
			public Move(MoveType type)
			{
				_type = type;
				_winMove = false;
			}
			
			public Move(Move copyThis)
			{
				_type = copyThis.getType();
				_winMove = copyThis.getWinMove();
			}
			
			public void setType(MoveType type) { _type = type; }
			public MoveType getType() { return _type; }
			
			public void setWinMove(boolean winMove) { _winMove = winMove; }
			public boolean getWinMove() { return _winMove; }
			
		}
		
		
		private Move[][] GameBoard = null;
		private static boolean gameIsOver = false;
		private static MoveType currentPlayer = null;
		
	// Setup ********************
	
		@Override
	    protected void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_game_view);
	        
	        newGame();
	        
	    }
	
	// On Click Methods ************************
		
		public void newGameClick(View v)
		{
			newGame();
		}
		
		protected void boardButtonClick(View v)
		{
			String boardRefTag = (String)v.getTag();
			String[] boardRef = boardRefTag.split(",");
			
			chooseMove(Integer.parseInt(boardRef[0]), Integer.parseInt(boardRef[1]));
		}
		
		
	// Game Methods *****************************
		
		protected void generateGameBoard()
		{
			int gameBoardSize = 3;
			
			GameBoard = new Move[gameBoardSize][gameBoardSize];
			
			for(int rowCtr = 0; rowCtr < gameBoardSize; rowCtr++)
			{
				for(int colCtr = 0; colCtr < gameBoardSize; colCtr++)
				{
					GameBoard[rowCtr][colCtr] = new Move(MoveType.Empty);
				}
			}
		}
		
		protected void newGame()
		{
			gameIsOver = false;
			switchPlayers();
			generateGameBoard();
			updateGameBoardView();
			checkForAIMove();
		}
		
		protected void gameMessage(String message)
		{
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
		
		protected void updateGameBoardView()
		{
			addRowsAndColumnsToGameBoardTable();
			updateHUD();
		}
		
		protected void updateHUD()
		{
			TextView playersTurn = (TextView)findViewById(R.id.playersTurn);
			playersTurn.setText(currentPlayer.getLabel(this));
		}
		
		protected void addRowsAndColumnsToGameBoardTable()
		{
			TableLayout gameBoardTable = (TableLayout)findViewById(R.id.gameBoardTable);
			
			// Clear Out Old Table *******
			gameBoardTable.removeAllViews();
			
			int cellCount = 0;
			
			for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
			{
				TableRow row = new TableRow(this);
				int numColumns = GameBoard[rowCtr].length;
				float columnWeight = 1 / numColumns;
				int buttonSize = 100;
				
				for(int colCtr = 0; colCtr < numColumns; colCtr++)
				{
					Move move = GameBoard[rowCtr][colCtr];
					String moveLabel = (move.getType() != MoveType.Empty) ? move.getType().getLabel(this) : "";
					
					TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, columnWeight);
					
					Button button = new Button(this);
					button.setText(moveLabel);
					button.setTypeface(Typeface.DEFAULT_BOLD);
					button.setTextSize(60.0f);
					button.setLayoutParams(buttonParams);
					button.setTag(rowCtr + "," + colCtr);
					
					if ((move.getType() != MoveType.Empty) || gameIsOver)
					{
						button.setEnabled(false);
					}
					else
					{
						button.setOnClickListener(new OnClickListener(){
							
							@Override
							public void onClick(View v) {
								
								boardButtonClick(v);
							}
							
						});
					}
					
					if (move.getWinMove())
					{
						lightUpWinSquare(button);
					}
					
					row.addView(button);
					cellCount++;
					
					
				}
				
				gameBoardTable.addView(row);
				
				
			}
		}
		
		protected void setMove(int rowIndex, int colIndex)
		{
			GameBoard[rowIndex][colIndex].setType(currentPlayer);
		}
		
		protected void switchPlayers()
		{
			if (currentPlayer == MoveType.X)
			{
				currentPlayer = MoveType.O;
			}
			else
			{
				currentPlayer = MoveType.X;
			}
		}
		
		protected void chooseMove(int rowIndex, int colIndex)
		{
			setMove(rowIndex, colIndex);
			
			if (playerWon())
			{
				gameMessage("Player " + currentPlayer.getLabel(this) + " wins, yay!");
				gameIsOver = true;
			}
			else if (isCatsGame())
			{
				gameMessage("Cat's game! BOO!");
				gameIsOver = true;
			}
			else
			{
				switchPlayers();
			}
			
			updateGameBoardView();
			checkForAIMove();
		}
		
		// AI ************************
		
			protected void checkForAIMove()
			{
				CheckBox enableAICheck = (CheckBox) findViewById(R.id.enableAI);
				
				if ((currentPlayer == MoveType.O) && (!gameIsOver) && (enableAICheck.isChecked()))
				{
					//aiMoveEasy();
					aiMoveMedium();
				}
			}
			
			protected void aiMoveEasy()
			{
				// Purely Random Move **************
				
				int rowIndex = 0;
				int colIndex = 0;
				boolean moveIsUnavailable = true;
				
				Random rand = new Random();
				
				while(moveIsUnavailable)
				{
					rowIndex = rand.nextInt(GameBoard.length);
					colIndex = rand.nextInt(GameBoard.length);
					
					moveIsUnavailable = !moveIsAvailable(GameBoard, rowIndex, colIndex);
				}
				
				Log.v("Matt", "AI Easy: [" + rowIndex + ", " + colIndex + "]");
				chooseMove(rowIndex, colIndex);
			}
			
			protected void aiMoveMedium()
			{
				// Random Move, but along a win path ************
				
				Move[][] blockingWinPathMoves = getBlockingWinPathMoves();
				
				if (winPathIsAvailable(blockingWinPathMoves))
				{
					int rowIndex = 0;
					int colIndex = 0;
					boolean moveIsUnavailable = true;
					
					Random rand = new Random();
					
					while(moveIsUnavailable)
					{
						rowIndex = rand.nextInt(blockingWinPathMoves.length);
						colIndex = rand.nextInt(blockingWinPathMoves.length);
						
						moveIsUnavailable = !moveIsWinPath(blockingWinPathMoves, rowIndex, colIndex);
					}
					
					Log.v("Matt", "AI Medium: [" + rowIndex + ", " + colIndex + "]");
					chooseMove(rowIndex, colIndex); 
				}
				else
				{
					aiMoveEasy();
				}
			}
			
			protected void aiMoveHard()
			{
				// Random Move, but along a the shortest win path ************
				
				Move[][] blockingWinPathMoves = getBlockingWinPathMoves();
				
				if (winPathIsAvailable(blockingWinPathMoves))
				{
					blockingWinPathMoves = removeLongBlockingWinPathMoves(blockingWinPathMoves);
					
					int rowIndex = 0;
					int colIndex = 0;
					boolean moveIsUnavailable = true;
					
					Random rand = new Random();
					
					while(moveIsUnavailable)
					{
						rowIndex = rand.nextInt(blockingWinPathMoves.length);
						colIndex = rand.nextInt(blockingWinPathMoves.length);
						
						moveIsUnavailable = !moveIsWinPath(blockingWinPathMoves, rowIndex, colIndex);
					}
					
					Log.v("Matt", "AI Hard: [" + rowIndex + ", " + colIndex + "]");
					chooseMove(rowIndex, colIndex); 
				}
				else
				{
					aiMoveEasy();
				}
			}
			
			protected boolean moveIsAvailable(Move[][] board, int rowIndex, int colIndex)
			{
				return (board[rowIndex][colIndex].getType() == MoveType.Empty);
			}
			
			protected boolean moveIsWinPath(Move[][] board, int rowIndex, int colIndex)
			{
				return (board[rowIndex][colIndex].getType() == MoveType.WinPath);
			}
			
			protected boolean winPathIsAvailable(Move[][] board)
			{
				for(int rowCtr = 0; rowCtr < board.length; rowCtr++)
				{
					for(int colCtr = 0; colCtr < board[rowCtr].length; colCtr++)
					{
						if (board[rowCtr][colCtr].getType() == MoveType.WinPath)
						{
							return true;
						}
					}
				}
				
				return false;
			}
			
			protected Move[][] copyGameBoard()
			{
				Move[][] copy = new Move[GameBoard.length][GameBoard.length];
				
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					for(int colCtr = 0; colCtr < GameBoard[rowCtr].length; colCtr++)
					{
						copy[rowCtr][colCtr] = new Move(GameBoard[rowCtr][colCtr]);
					}
				}
				
				return copy;
			}
			
			protected Move[][] getBlockingWinPathMoves()
			{
				Move[][] possibleMoves = copyGameBoard();
				
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					for(int colCtr = 0; colCtr < GameBoard[rowCtr].length; colCtr++)
					{
						if (possibleMoves[rowCtr][colCtr].getType() == MoveType.X)
						{
							// Flag the Horizontal and Vertical spaces ************
							
								possibleMoves = fillMoveRow(possibleMoves, rowCtr, MoveType.WinPath);
								possibleMoves = fillMoveColumn(possibleMoves, colCtr, MoveType.WinPath);
						}
					}
				}
				
				return possibleMoves;
			}
			
			protected Move[][] removeLongBlockingWinPathMoves(Move[][] board)
			{
				// First find the shortest Win Path ******************
				
					int shortestWinPath = 999;
					int currentCount = 0;
					
					// Rows **************
					
					for(int rowCtr = 0; rowCtr < board.length; rowCtr++)
					{
						currentCount = winPathRowCount(board, rowCtr);
						if ((currentCount > 0) && (currentCount < shortestWinPath)) { shortestWinPath = currentCount; }
					}
					
					// Columns **************
					
					for(int colCtr = 0; colCtr < board.length; colCtr++)
					{
						currentCount = winPathColumnCount(board, colCtr);
						if ((currentCount > 0) && (currentCount < shortestWinPath)) { shortestWinPath = currentCount; }
					}
				
				// Remove any path longer than the shortest Win Path **********
				
					// Rows **************
					
					for(int rowCtr = 0; rowCtr < board.length; rowCtr++)
					{
						currentCount = winPathRowCount(board, rowCtr);
						if (currentCount > shortestWinPath) { board = fillMoveRow(board, rowCtr, MoveType.Empty); }
					}
					
					// Columns **************
					
					for(int colCtr = 0; colCtr < board.length; colCtr++)
					{
						currentCount = winPathColumnCount(board, colCtr);
						if (currentCount > shortestWinPath) { board = fillMoveColumn(board, colCtr, MoveType.Empty); }
					}
				
				
				return board;
			}
			
			protected int winPathRowCount(Move[][] board, int rowIndex)
			{
				int count = 0;
				
				for(int colCtr = 0; colCtr < board[rowIndex].length; colCtr++)
				{
					if (board[rowIndex][colCtr].getType() == MoveType.WinPath)
					{
						count++;
					}
				}
				
				return count;
			}
			
			protected int winPathColumnCount(Move[][] board, int colIndex)
			{
				int count = 0;
				
				for(int rowCtr = 0; rowCtr < board.length; rowCtr++)
				{
					if (board[rowCtr][colIndex].getType() == MoveType.WinPath)
					{
						count++;
					}
				}
				
				return count;
			}
			
			protected Move[][] fillMoveRow(Move[][] possibleMoves, int rowIndex, MoveType fill)
			{
				for(int colCtr = 0; colCtr < possibleMoves.length; colCtr++)
				{
					if (moveIsAvailable(GameBoard, rowIndex, colCtr))
					{
						possibleMoves[rowIndex][colCtr].setType(fill);
					}
				}
				
				return possibleMoves;
			}
			
			protected Move[][] fillMoveColumn(Move[][] possibleMoves, int colIndex, MoveType fill)
			{
				for(int rowCtr = 0; rowCtr < possibleMoves.length; rowCtr++)
				{
					if (moveIsAvailable(GameBoard, rowCtr, colIndex))
					{
						possibleMoves[rowCtr][colIndex].setType(fill);
					}
				}
				
				return possibleMoves;
			}
			
		// Win Conditions ********************
		
			protected boolean playerWon()
			{
				// Rows **************
				
					for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
					{
						if (checkRowWin(rowCtr))
						{
							
							return true;
						}
					}
				
				// Columns ******************
					
					for(int colCtr = 0; colCtr < GameBoard[0].length; colCtr++)
					{
						if (checkColumnWin(colCtr))
						{
							return true;
						}	
					}
					
				// Diagonals *********************
					
					if (checkDiagonalPositiveWin() || checkDiagonalNegativeWin())
					{
						return true;
					}
					
				clearAllMoveWins();
					
				return false;
			}
			
			protected boolean isCatsGame()
			{
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					TableRow row = new TableRow(this);
					int numColumns = GameBoard[rowCtr].length;
					
					for(int colCtr = 0; colCtr < numColumns; colCtr++)
					{
						if (GameBoard[rowCtr][colCtr].getType() == MoveType.Empty)
						{
							return false;
						}
					}
				
				}
				
				return true;
			}
			
			protected boolean meetsWinCondition(Move move)
			{
				return (move.getType() == currentPlayer);
			}
			
			protected boolean checkRowWin(int rowIndex)
			{
				clearAllMoveWins();
				
				for(int ctr = 0; ctr < GameBoard[rowIndex].length; ctr++)
				{
					Move currentMove = GameBoard[rowIndex][ctr];
					
					if (!meetsWinCondition(currentMove))
					{
						return false;
					}
					else
					{
						currentMove.setWinMove(true);
					}
				}
				
				return true;
			}
			
			protected boolean checkColumnWin(int colIndex)
			{
				
				clearAllMoveWins();
				
				int rowCount = GameBoard.length;
				
				for(int ctr = 0; ctr < rowCount; ctr++)
				{
					Move currentMove = GameBoard[ctr][colIndex];
					
					if (!meetsWinCondition(currentMove))
					{
						return false;
					}
					else
					{
						currentMove.setWinMove(true);
					}
				}
				
				return true;
			}
			
			protected boolean checkDiagonalPositiveWin()
			{
				clearAllMoveWins();
				
				// Check where (Column Counter) = (Row Counter) ********************
				
				for(int boardCtr = 0; boardCtr < GameBoard.length; boardCtr++)
				{
					Move currentMove = GameBoard[boardCtr][boardCtr];
					
					if (!meetsWinCondition(currentMove))
					{
						return false;
					}
					else
					{
						currentMove.setWinMove(true);
					}
				}
				
				return true;
			}
			
			protected boolean checkDiagonalNegativeWin()
			{
				clearAllMoveWins();
				
				// Check where (Column Counter) = (Number of Rows - 1) - (Row Counter) ********************
				
				int numRows = GameBoard.length;
				
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					TableRow row = new TableRow(this);
					int numColumns = GameBoard[rowCtr].length;
					
					for(int colCtr = 0; colCtr < numColumns; colCtr++)
					{
						//Log.v("Matt", "Diag Neg [" + rowCtr + ", " + colCtr + "] (" + colCtr + " ?= " + ((numRows-1) - rowCtr) + "): " + (colCtr == ((numRows-1) - rowCtr)));
						if (colCtr == ((numRows-1) - rowCtr))
						{
							Move currentMove = GameBoard[rowCtr][colCtr];
							
							if (!meetsWinCondition(currentMove))
							{
								return false;
							}
							else
							{
								currentMove.setWinMove(true);
							}
						}
					}
				}
				
				return true;
			}
		
		// Light Up Wins *****************************
			
			public void clearAllMoveWins()
			{
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					TableRow row = new TableRow(this);
					int numColumns = GameBoard[rowCtr].length;
					
					for(int colCtr = 0; colCtr < numColumns; colCtr++)
					{
						GameBoard[rowCtr][colCtr].setWinMove(false);
					}
				}
				
			}
			
			public void clearCheck()
			{
				// Clear Check *****************
				
				Log.v("Matt", "Clear Check");
				
				for(int rowCtr = 0; rowCtr < GameBoard.length; rowCtr++)
				{
					TableRow row = new TableRow(this);
					int numColumns = GameBoard[rowCtr].length;
					String test = "";
					for(int colCtr = 0; colCtr < numColumns; colCtr++)
					{
						test += GameBoard[rowCtr][colCtr].getWinMove() + " ";
					}
					
					Log.v("Matt", test);
				}
			}
			
			protected void lightUpWinSquare(Button button)
			{
				if (button != null)
				{
					button.setBackgroundColor(getResources().getColor(R.color.green));
				}
			}
			
			
	
}
