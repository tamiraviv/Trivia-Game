package triviaDB;


import core.*;
import utils.*;


import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.Collection;

import db.dao.*;




public class GUITrivia 
{
	private Display display;
	private Shell shell;
	private Composite headPanel;
	private Composite bottomPanel;
	private Composite middlePanel;
	private Font boldFont;
	private String Player1UserName = "";
	private String Player2UserName = "";
	private gameRunner game = null;

	public void open() 
	{
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	
	private GridData createFillGridData(int num) 
	{
		GridData toReturn = new GridData(GridData.FILL, GridData.CENTER, true,	false);
		toReturn.horizontalSpan = num;
		return toReturn;
	}
	
	private void messageToUser(String s1,String s2,int icon)
	{
		MessageBox messageBox = new MessageBox(shell, icon | SWT.OK);
		messageBox.setText(s1);
		messageBox.setMessage(s2);
		messageBox.open();
	}
	
	private void dispose()
	{
		Control[] children = headPanel.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		Control[] children2 = middlePanel.getChildren();
		for (Control control : children2) {
			control.dispose();
		}

		Control[] children3 = bottomPanel.getChildren();

		for (Control control : children3) {
			control.dispose();
		}

		headPanel.dispose();
		middlePanel.dispose();
		bottomPanel.dispose();
	}
	
	
	private void createShell() {
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Trivia");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(new Point(monitor_bounds.width / 2,
				monitor_bounds.height / 2));

		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);

		try
		{
			game = new gameRunner();
			
		}
		catch (DAOException e)
		{
			messageToUser("Error",e.toString(),SWT.ICON_ERROR);
		} 

		createMainScreen();

	}

	/**
	 * Creates the widgets of the form for trivia file selection
	 */
	private void createMainScreen()
	{



		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,	true));
		headPanel.setLayout(new GridLayout(1, true));

		Label label3 = new Label(headPanel, SWT.CENTER | SWT.WRAP);
		label3.setText("\n\nWelcome to Trivia\n\n\n\n");
		label3.setLayoutData(createFillGridData(1));
		label3.setFont(boldFont);
		
		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,	true, true));
		middlePanel.setLayout(new GridLayout(4, true));

		GridData signInLayoutData = createFillGridData(1);
		signInLayoutData.horizontalAlignment = SWT.CENTER;
		signInLayoutData.widthHint = 150;

		Label label1 = new Label(middlePanel, SWT.CENTER | SWT.WRAP);
		label1.setLayoutData(createFillGridData(1));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		Label label2 = new Label(bottomPanel, SWT.CENTER);
		label2.setLayoutData(createFillGridData(2));

		GridData answerLayoutData = createFillGridData(2);
		answerLayoutData.verticalIndent = 8;
		answerLayoutData.horizontalAlignment = SWT.CENTER;
		answerLayoutData.widthHint = 120;

		if (Player1UserName.equals(""))
		{
			final Button player1SignInButton = new Button(middlePanel, SWT.PUSH);
			player1SignInButton.setText("player 1 - SignIn");
			player1SignInButton.setLayoutData(signInLayoutData);
			player1SignInButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createSignInScreen();
				}
			});
		} 
		else 
		{
			final Text userName = new Text(middlePanel, SWT.SINGLE | SWT.BORDER	| SWT.CENTER | SWT.READ_ONLY);
			userName.setText("Welcome " + Player1UserName);
			userName.setLayoutData(signInLayoutData);

		}


		if (Player2UserName.equals(""))
		{
			final Button player2SignInButton = new Button(middlePanel, SWT.PUSH);
			player2SignInButton.setText("player 2 - SignIn");
			player2SignInButton.setLayoutData(signInLayoutData);
			player2SignInButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					createSignInScreen();
				}
			});
		} 
		else 
		{
			final Text userName2 = new Text(middlePanel, SWT.SINGLE	| SWT.BORDER | SWT.CENTER |SWT.READ_ONLY);
			userName2.setText("Welcome " + Player2UserName);
			userName2.setLayoutData(signInLayoutData);
		}

		final Button updateDBButton = new Button(bottomPanel, SWT.PUSH);
		updateDBButton.setText("updateDB");
		updateDBButton.setLayoutData(answerLayoutData);
		updateDBButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				createUpdateDBScreen();
				
			}
		});
		
		final Button startGameButton = new Button(bottomPanel, SWT.PUSH);
		startGameButton.setText("Start Game");
		startGameButton.setLayoutData(answerLayoutData);
		startGameButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				if(game.getHowPlayerConnected() == 0)
				{
					messageToUser("Error","You must log in to your user to start the game.",SWT.ICON_ERROR);
				}
				else
				{
					try
					{
						game.StartGame();
					}
					catch(DAOException e1)
					{
						messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
					}
					catch(EntityNotFound e2)
					{
						messageToUser("Error",e2.toString(),SWT.ICON_ERROR);
					}
					
					createChooseCountryGame();
				}
				
			}
		});

		final Button statisticButton = new Button(bottomPanel, SWT.PUSH);
		statisticButton.setText("statistic");
		statisticButton.setLayoutData(answerLayoutData);
		statisticButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createStatisticScreen();
			}
		});

		final Button signUpButton = new Button(bottomPanel, SWT.PUSH);
		signUpButton.setText("Sign Up");
		signUpButton.setLayoutData(answerLayoutData);
		signUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createSignUpScreen();
			}
		});

		final Button QuitButton = new Button(bottomPanel, SWT.PUSH);
		QuitButton.setText("Quit");
		QuitButton.setLayoutData(answerLayoutData);
		QuitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION |SWT.OK | SWT.CANCEL);
			    messageBox.setMessage("Are you sure you want to exit?");
			    int rc = messageBox.open();
			    switch (rc) 
			    {
			    case SWT.OK:
			    	shell.dispose();
					break;
			    case SWT.CANCEL:
			    	break;

			    }
			}
		});

		bottomPanel.pack();
		middlePanel.pack();
		headPanel.pack();
		shell.layout();
	}

	private void createSignInScreen() 
	{

		dispose();

		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,	true));
		headPanel.setLayout(new GridLayout(1, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		middlePanel.setLayout(new GridLayout(4, true));

		Label label = new Label(headPanel, SWT.CENTER);
		label.setText("\n\n\nLogin to your account\n\n\n\n\n\n");
		label.setLayoutData(createFillGridData(2));
		label.setFont(boldFont);

		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;

		GridData tempLayoutData2 = createFillGridData(2);
		tempLayoutData2.verticalIndent = 5;
		tempLayoutData2.horizontalAlignment = SWT.CENTER;
		tempLayoutData2.widthHint = 150;

		final Text userName = new Text(headPanel, SWT.SINGLE | SWT.BORDER| SWT.CENTER);
		userName.setText("userName");
		userName.setLayoutData(tempLayoutData2);

		
		
		final Text passWord = new Text(headPanel, SWT.SINGLE | SWT.BORDER| SWT.CENTER | SWT.PASSWORD);
		passWord.setText("passWord");
		passWord.setLayoutData(tempLayoutData2);

		
		
		final Button LoginButton = new Button(headPanel, SWT.PUSH);
		LoginButton.setText("Login");
		LoginButton.setLayoutData(tempLayoutData);
		LoginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				if(!userName.getText().equals(Player1UserName))
				{
					try
					{
						game.logInUser(userName.getText(), passWord.getText());
						messageToUser("successfully","Sign In went successfully,Welcome "+userName.getText(),SWT.ICON_WORKING);
						if(Player1UserName.equals(""))
						{
							Player1UserName=userName.getText();
						}
						else
						{
							Player2UserName=userName.getText();
						}
						dispose();
						createMainScreen();
						
					}
					catch(DAOException e1)
					{
						
					}
					catch(DataNotFoundException e2)
					{
						
					}
					catch(EntityNotFound e3)
					{
						
					}
					
				}
				else
				{
					messageToUser("Error",userName.getText()+" is already loged in, please choose another user",SWT.ICON_ERROR);
				}
			}
		});
		
		final Button backButton = new Button(bottomPanel, SWT.PUSH);
		backButton.setText("back");
		backButton.setLayoutData(tempLayoutData);
		backButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				dispose();
				createMainScreen();
			}
		});

		bottomPanel.getParent().layout();

	}

	
	private void createSignUpScreen() 
	{
		dispose();

		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		headPanel.setLayout(new GridLayout(1, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		middlePanel.setLayout(new GridLayout(4, true));


		Label label = new Label(headPanel, SWT.CENTER);
		label.setText("\n\n\nCreate your account\n\n\n\n\n\n\n");
		label.setLayoutData(createFillGridData(2));
		label.setFont(boldFont);
		
		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;
		tempLayoutData.widthHint = 100;

		GridData tempLayoutData2 = createFillGridData(2);
		tempLayoutData2.verticalIndent = 5;
		tempLayoutData2.horizontalAlignment = SWT.CENTER;
		tempLayoutData2.widthHint = 150;

		final Text filePathField = new Text(headPanel, SWT.SINGLE | SWT.BORDER
				| SWT.CENTER);
		filePathField.setText("userName");
		filePathField.setLayoutData(tempLayoutData2);

		final Text filePathField2 = new Text(headPanel, SWT.SINGLE | SWT.BORDER
				| SWT.CENTER);
		filePathField2.setText("password");
		filePathField2.setLayoutData(tempLayoutData2);

		final Text filePathField3 = new Text(headPanel, SWT.SINGLE | SWT.BORDER
				| SWT.CENTER);
		filePathField3.setText("verify password");
		filePathField3.setLayoutData(tempLayoutData2);

		final Button updateDBButton = new Button(headPanel, SWT.PUSH);
		updateDBButton.setText("Sign Up");
		updateDBButton.setLayoutData(tempLayoutData);
		updateDBButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				if(!filePathField.getText().equals(""))
				{				
					if(filePathField2.getText().equals(filePathField3.getText()))
					{
						try
						{
							game.registerUser(filePathField.getText(),filePathField2.getText());
							messageToUser("successfully","SignUp went successfully, you may log in now",SWT.ICON_WORKING);
							dispose();

							createMainScreen();
						
						}
						catch(DAOException e1)
						{
						messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
						}
					
					}
					else
					{
						messageToUser("Error","Password isn't the same, please Enter the same password in both places",SWT.ICON_ERROR);
					}
				}
				else
				{
					messageToUser("Error","UserName must contain something",SWT.ICON_ERROR);
				}
			}
		});
		
		final Button backButton = new Button(bottomPanel, SWT.PUSH);
		backButton.setText("back");
		backButton.setLayoutData(tempLayoutData);
		backButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				dispose();
				createMainScreen();
			}
		});

		bottomPanel.getParent().layout();
	}

	private void createStatisticScreen()
	{
		dispose();

		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		headPanel.setLayout(new GridLayout(1, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		middlePanel.setLayout(new GridLayout(2, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;

		GridData tempLayoutData2 = createFillGridData(2);
		tempLayoutData2.horizontalAlignment = SWT.CENTER;

		Table table = new Table(middlePanel, SWT.BORDER | SWT.CENTER | SWT.FILL);
		
		String[] titles = { "userName", "Score", "Date" };
		
		try{
			for (int loopIndex = 0; loopIndex < titles.length; loopIndex++)
			{
				TableColumn column = new TableColumn(table, SWT.CENTER | SWT.FILL);
				column.setText(titles[loopIndex]);
			}
			
			table.setHeaderVisible(true);
			
			Collection<UserIDScoreDate> playres = game.getBestScores();
			String[] userNames = new String[3];
			for(UserIDScoreDate user:  playres)
			{
				userNames[0] = game.getUserNameById(user);
				userNames[1] = String.valueOf(user.getScore());
				userNames[2] = String.valueOf(user.getDate());
				TableItem item = new TableItem(table, SWT.FILL);
				item.setText(userNames);
				
				
			}
			
			for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) 
			{
				table.getColumn(loopIndex).pack();
			}
			table.setLayoutData(tempLayoutData2);
			
		}
		catch(DAOException e)
		{
			
		} catch (EntityNotFound e1) {
			
		}
		

		Label label3 = new Label(headPanel, SWT.CENTER | SWT.WRAP);
		label3.setText("\n\n\nWelcome to The Hell Of Fame\n\n");
		label3.setLayoutData(createFillGridData(1));
		label3.setFont(boldFont);
		

		final Button backButton = new Button(bottomPanel, SWT.PUSH);
		backButton.setText("back");
		backButton.setLayoutData(tempLayoutData);
		backButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dispose();

				createMainScreen();
			}
		});

		bottomPanel.getParent().layout();
	}

	private void createSoloGameScreen() 
	{
		dispose();

		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,true));
		headPanel.setLayout(new GridLayout(1, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		middlePanel.setLayout(new GridLayout(2, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		GridData textDate = createFillGridData(1);
		textDate.horizontalAlignment = SWT.CENTER;
		textDate.widthHint = 180;
		
		
		Label questionLabel = new Label(headPanel, SWT.CENTER | SWT.WRAP);
		
		try{
			String  question= game.getCurrentQuestion();
			questionLabel.setText("Please answer this Question:" + "\n\n"	+ question + "\n\n");
			questionLabel.setFont(boldFont);
		}
		catch(Exception e)
		{
			messageToUser("Error",e.toString(),SWT.ICON_ERROR);
		}
		questionLabel.setLayoutData(createFillGridData(2));

		
		final Text score = new Text(headPanel, SWT.SINGLE| SWT.BORDER | SWT.CENTER |SWT.READ_ONLY);
		score.setText(game.getCurrentUserName()+ " score is:  " + String.valueOf(game.getCurrentUserScore()));
		score.setLayoutData(textDate);
		
		final Text wrongAnswers = new Text(headPanel, SWT.SINGLE| SWT.BORDER | SWT.CENTER |SWT.READ_ONLY);
		wrongAnswers.setText(game.getCurrentUserName()+ " has:  " + String.valueOf(game.getCurrentUserWorngAnswerCount()) +" Wrong Answers");
		wrongAnswers.setLayoutData(textDate);
		
		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;
		tempLayoutData.widthHint = 100;

		String[] answers = {"Error","Error","Error","Error"};
		ArrayList<String> a = game.getAnswers();	
		if(a != null)
		{
			answers = a.toArray(new String[a.size()]);
		}

		for (int i = 0; i < answers.length; i++) 
		{
			Button answerButton = new Button(middlePanel, SWT.PUSH | SWT.WRAP);
			answerButton.setText(answers[i]);
			GridData answerLayoutData = createFillGridData(1);
			answerLayoutData.verticalAlignment = SWT.FILL;
			answerButton.setLayoutData(answerLayoutData);
			answerButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) 
				{
					try
					{
						if(game.checkPlayerAnswer(answerButton.getText()))
						{
							messageToUser("Right","That's the right answer!",SWT.ICON_WORKING);
						}
						else
						{
							messageToUser("Wrong","That's the wrong answer!",SWT.ICON_ERROR);
						}										
						createChooseCountryGame();									
					}
					catch(Exception e1)
					{
						if(e1.getLocalizedMessage().equals("Game Is Over, Init game to play again"))
						{
							createGameOverScreen();
						}
						else
						{
							messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
						}
						
					}
					
					
				}

			});
		}

		Button passButton = new Button(bottomPanel, SWT.CENTER);
		passButton.setText("Pass");
		passButton.setLayoutData(tempLayoutData);
		passButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				try
				{
					
					game.passQuestion();
					createSoloGameScreen();
				}
				catch(Exception e1)
				{
					messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
				}
			
			}

		});
		
		final Button quitButton = new Button(bottomPanel, SWT.PUSH);
		quitButton.setText("Quit");
		quitButton.setLayoutData(tempLayoutData);
		quitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				Player1UserName= "";
				Player2UserName = "";			
				game.CloseGameRunner();
				try
				{
					game = new gameRunner();
					dispose();
					createMainScreen();
				}
				catch (DAOException e1)
				{
					messageToUser("Error",e.toString(),SWT.ICON_ERROR);
				}
			}
		});

		bottomPanel.getParent().layout();

	}


	/**
	 * Opens the main window and executes the event loop of the application
	 */

	private void createChooseCountryGame() 
	{
		dispose();
		
		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		headPanel.setLayout(new GridLayout(1, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true));
		middlePanel.setLayout(new GridLayout(2, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));

		Label label = new Label(headPanel, SWT.CENTER);
		label.setText("\n\n\n\nPlease select a country you what to be ask on:\n\n\n\n\n\n\n");
		label.setFont(boldFont);
		label.setLayoutData(createFillGridData(2));

		Label label2 = new Label(middlePanel, SWT.CENTER | SWT.WRAP);
		label2.setLayoutData(createFillGridData(1));

		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;
		tempLayoutData.widthHint = 100;

		GridData tempLayoutData2 = createFillGridData(2);
		tempLayoutData2.verticalIndent = 5;
		tempLayoutData2.horizontalAlignment = SWT.CENTER;
		tempLayoutData2.widthHint = 200;

		final Combo combo = new Combo(headPanel, SWT.CENTER | SWT.READ_ONLY);
		try
		{
			Collection<IDName> a = game.getAllCountries();
			String[] country = new String[a.size()+1];
			country[0] = "General Country";
			int j=1;
			for(IDName id: a)
			{
				country[j] = id.getName();
				j++;
			}
			
			for (int i = 0; i < country.length; i++) 
			{
				combo.add(country[i]);
			}
			combo.setLayoutData(tempLayoutData2);
			
		}
		catch(DAOException e)
		{
			messageToUser("Error",e.toString(),SWT.ICON_ERROR);
			dispose();
			createMainScreen();
			return;
		}
		catch(Exception e1)
		{
			messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
			dispose();
			createMainScreen();
			return;
		}

		final Button startButton = new Button(bottomPanel, SWT.PUSH);
		startButton.setText("Get Question");
		startButton.setLayoutData(tempLayoutData);
		startButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				if(!combo.getText().equals("General Country") || !combo.getText().equals(""))
				{
					try{
						Collection<IDName> id = game.getAllCountries();
						for(IDName d : id)
						{
							if(d.getName().equals(combo.getText()))
							{
								game.setCurrUserFavCountry(d);
								
							}
						}
					}
					catch(DAOException e1)
					{
						messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
					}
				}
				else
				{
					game.setCurrUserFavCountry(null);
				}
				
				
				try
				{
					game.passQuestion();
					createSoloGameScreen();
				}
				catch(Exception e1)
				{
					try
					{
						game.passQuestion();
						createSoloGameScreen();
					}
					catch(Exception e2)
					{
						messageToUser("Error",e2.toString(),SWT.ICON_ERROR);
					}
				}
				
				
				try
				{
					game.passQuestion();
					createSoloGameScreen();
				}
				catch(Exception e1)
				{
					try
					{
						game.passQuestion();
						createSoloGameScreen();
					}
					catch(Exception e2)
					{
						messageToUser("Error",e2.toString(),SWT.ICON_ERROR);
					}
				}			
		
				
			}
		});

		final Button quitButton = new Button(bottomPanel, SWT.PUSH);
		quitButton.setText("Quit");
		quitButton.setLayoutData(tempLayoutData);
		quitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				Player1UserName= "";
				Player2UserName = "";			
				game.CloseGameRunner();
				try
				{
					game = new gameRunner();
					dispose();
					createMainScreen();
				}
				catch (DAOException e1)
				{
					messageToUser("Error",e.toString(),SWT.ICON_ERROR);
				}
			}
		});

		bottomPanel.getParent().layout();

	}
	
	
	
	
	
	
	
	private void createUpdateDBScreen() 
	{
		dispose();
		
		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,	true));
		headPanel.setLayout(new GridLayout(1, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		middlePanel.setLayout(new GridLayout(2, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));
		
		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(20); 
		Font boldFont2 = new Font(shell.getDisplay(), fontData);
		
		final Color red = display.getSystemColor(SWT.COLOR_RED);
		Label label = new Label(headPanel, SWT.CENTER);
		label.setText("\n\nWarning: This process may take a few hours to complete.\n\n");
		label.setFont(boldFont2);
		label.setForeground(red);
		label.setLayoutData(createFillGridData(2));
		
		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;
		tempLayoutData.widthHint = 100;

		GridData tempLayoutData2 = createFillGridData(2);
		tempLayoutData2.verticalIndent = 5;
		tempLayoutData2.horizontalAlignment = SWT.CENTER;
		tempLayoutData2.widthHint = 200;
		
		GridData tempLayoutData3 = createFillGridData(2);
		tempLayoutData3.horizontalAlignment = SWT.CENTER;
		
		GridData tempLayoutData4 = createFillGridData(2);
		tempLayoutData4.verticalIndent = 5;
		tempLayoutData4.horizontalAlignment = SWT.CENTER;
		tempLayoutData4.widthHint = 400;
		tempLayoutData4.heightHint= 100;

		final Text warnningText = new Text(middlePanel,  SWT.MULTI | SWT.BORDER	| SWT.CENTER | SWT.READ_ONLY | SWT.WRAP);
		warnningText.setText("This process will delete all the tables that are yago-related from the DB,\n and only then will update the DB.\n"+"\nif you still want to proceed, make sure that the list of file below are in your yago folder:");
		warnningText.setLayoutData(tempLayoutData4);
		
		
		Table table = new Table(middlePanel, SWT.BORDER | SWT.CENTER | SWT.FILL);

		String[] files = { "yagoLabels.tsv", "yagoTransitiveType.tsv", "yagoDateFacts.tsv" , "yagoFacts.tsv", "yagoLiteralFacts.tsv"};

		TableColumn column = new TableColumn(table, SWT.CENTER | SWT.FILL);
		column.setText("Files:");
		
		table.setHeaderVisible(true);

		for (int loopIndex = 0; loopIndex < 5; loopIndex++)
		{
			TableItem item = new TableItem(table, SWT.FILL | SWT.CENTER);
			item.setText(files[loopIndex]);

		}

		table.getColumn(0).pack();

		table.setLayoutData(tempLayoutData3);
		
		final Button continueUpdate = new Button(bottomPanel, SWT.PUSH);
		continueUpdate.setText("continue");
		continueUpdate.setLayoutData(tempLayoutData);
		continueUpdate.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				
				
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION |SWT.OK | SWT.CANCEL);
			    messageBox.setMessage("Are you sure you want to do that?");
			    int rc = messageBox.open();
			    switch (rc) 
			    {
			    case SWT.OK:
			    	try
					{
						game.updateDataFromSource("yago");
						messageToUser("successfully","Update is executed, please wait a few second",SWT.ICON_WORKING);
					}
					catch(DAOException e1)
					{
						 messageToUser("Error",e1.toString(),SWT.ICON_ERROR);
					}
					catch(IOException e2)
					{
						messageToUser("Error",e2.toString(),SWT.ICON_ERROR);
					}
					dispose();

					createMainScreen();
					break;
			    case SWT.CANCEL:
			    	break;

			    }
				
			}
		});

		
		
		final Button quitButton = new Button(bottomPanel, SWT.PUSH);
		quitButton.setText("Quit");
		quitButton.setLayoutData(tempLayoutData2);
		quitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{						   			
				dispose();
				createMainScreen();
			}
		});

		bottomPanel.getParent().layout();
		
	}
	
	
	
	
	
	private void createGameOverScreen() 
	{
		dispose();
		
		headPanel = new Composite(shell, SWT.NONE);
		headPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,	true));
		headPanel.setLayout(new GridLayout(1, true));

		middlePanel = new Composite(shell, SWT.CENTER);
		middlePanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		middlePanel.setLayout(new GridLayout(2, true));

		bottomPanel = new Composite(shell, SWT.NONE);
		bottomPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,true, true));
		bottomPanel.setLayout(new GridLayout(2, true));
		
		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(30); 
		Font boldFont2 = new Font(shell.getDisplay(), fontData);
		
		if(game.getHowPlayerConnected() == 1)
		{
			final Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			Label label = new Label(middlePanel, SWT.CENTER);
			label.setText("\nGame is Over!\n\n"+game.getCurrentUserName() +" has final score of: "+game.getCurrentUserScore());
			label.setFont(boldFont2);
			label.setForeground(blue);
			label.setLayoutData(createFillGridData(2));
		}
		else
		{
			final Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			Label label = new Label(middlePanel, SWT.CENTER);
			label.setText("\n"+game.getCurrentUserName() +" has won the game!\n\n Your final score is: "+game.getCurrentUserScore());
			label.setFont(boldFont2);
			label.setForeground(blue);
			label.setLayoutData(createFillGridData(2));
		}

		
		
		
		GridData tempLayoutData = createFillGridData(2);
		tempLayoutData.verticalIndent = 5;
		tempLayoutData.horizontalAlignment = SWT.CENTER;
		tempLayoutData.widthHint = 200;
		
		final Button quitButton = new Button(bottomPanel, SWT.PUSH);
		quitButton.setText("Back to Main Screen");
		quitButton.setLayoutData(tempLayoutData);
		quitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{	
				Player1UserName= "";
				Player2UserName = "";
				game.CloseGameRunner();
				try
				{
					game = new gameRunner();
					dispose();
					createMainScreen();
				}
				catch (DAOException e1)
				{
					messageToUser("Error",e.toString(),SWT.ICON_ERROR);
				} 

			}
		});

		bottomPanel.getParent().layout();
		
	}

	private void runApplication()
	{
		shell.open();
		display = shell.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
		if(game!=null)
		{
			game.CloseGameRunner();
		}
	}
}



