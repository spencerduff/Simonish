package cs2410.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cs2410.components.ColorPanel;
import cs2410.components.ScorePanel;

public class Simonish extends JFrame implements MouseListener, ActionListener {
	Color[] colorArr = { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN };
	ColorPanel[] panelArr = new ColorPanel[4];
	ScorePanel scorePanel;
	JPanel gamePanel;
	ArrayList<ColorPanel> seq = new ArrayList<ColorPanel>();
	Iterator seqItr;
	Random rand = new Random();
	boolean gameOver = true;
	JMenuBar mainBar;
	JMenu settings, stats, help;
	JMenuItem chooseColor, chooseMode, highScoreList, history, about, rules;
	JOptionPane colorChooserPane = new JOptionPane();
	int delay;
	int[] highScores;
	String highScoreFileName, historyFileName;
	File highScoreFile, historyFile;
	double totalAvgScore;
	int totalGamesPlayed;
	Thread t1;
	boolean playerTurn;

	Container pane;

	public Simonish() {
		this.setTitle("Simonish");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);

		playerTurn = true;
		delay = 1000;
		highScoreFileName = "HighScoreList.txt";
		historyFileName = "History.txt";
		highScoreFile = new File(highScoreFileName);
		historyFile = new File(historyFileName);
		highScores = new int[10];
		readHighScores();
		totalAvgScore = readHistoryAvgScore();
		totalGamesPlayed = readHistoryGamesPlayed();

		mainBar = new JMenuBar();
		settings = new JMenu("Settings");
		stats = new JMenu("Stats");
		help = new JMenu("Help");

		chooseColor = new JMenuItem("Choose Color");
		chooseColor.addActionListener(this);
		settings.add(chooseColor);
		chooseMode = new JMenuItem("Choose Mode");
		chooseMode.addActionListener(this);
		settings.add(chooseMode);

		highScoreList = new JMenuItem("High Score List");
		highScoreList.addActionListener(this);
		stats.add(highScoreList);
		history = new JMenuItem("History");
		history.addActionListener(this);
		stats.add(history);

		about = new JMenuItem("About");
		about.addActionListener(this);
		help.add(about);
		rules = new JMenuItem("Rules");
		rules.addActionListener(this);
		help.add(rules);

		mainBar.add(settings);
		mainBar.add(stats);
		mainBar.add(help);

		setJMenuBar(mainBar);

		pane = this.getContentPane();
		pane.setLayout(new BorderLayout());

		pane.setPreferredSize(new Dimension(400, 475));
		scorePanel = new ScorePanel();
		scorePanel.setBounds(0, 0, 400, 50);
		scorePanel.getStartBtn().addActionListener(this);

		gamePanel = new JPanel();
		gamePanel.setLayout(new GridLayout(2, 2));
		gamePanel.setBounds(0, 50, 400, 400);

		pane.add(scorePanel, BorderLayout.SOUTH);
		pane.add(gamePanel, BorderLayout.CENTER);

		for (int i = 0; i < panelArr.length; i++) {
			panelArr[i] = new ColorPanel(colorArr[i]);
			panelArr[i].addMouseListener(this);
			gamePanel.add(panelArr[i]);
		}

		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void computerTurn() {
		playerTurn = false;
		seq.add(panelArr[rand.nextInt(panelArr.length)]);

		for (ColorPanel i : seq) {
			try {
				Thread.sleep(delay / 2);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			i.pressed();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i.released();
		}

		seqItr = seq.iterator();
		playerTurn = true;
	}

	public class CompThread extends Thread {
		public void run() {
			computerTurn();
		}
	}

	private void gameOver() {
		gameOver = true;
		String msg = "Game Over";
		int i = 0;
		int currHighScore = highScores[i];
		do {
			currHighScore = highScores[i];
			if (scorePanel.getScore() > currHighScore) {
				for (int j = highScores.length - 1; j > i; j--) {
					highScores[j] = highScores[j - 1];
				}
				highScores[i] = scorePanel.getScore();
			}
			i++;
		} while (i < highScores.length && scorePanel.getScore() < currHighScore);

		rewriteHighScore();
		double temp = totalGamesPlayed * totalAvgScore;
		temp += scorePanel.getScore();
		totalGamesPlayed++;
		totalAvgScore = temp / totalGamesPlayed;
		rewriteHistory();

		if (scorePanel.getHighScore() >= currHighScore) {
			msg = msg + "\nYou got a high score!";
		}
		JOptionPane.showMessageDialog(this, msg);
	}

	public static void main(String[] args) {
		new Simonish();
	}

	// Clears the file and writes in the new high scores
	public void rewriteHighScore() {
		try {
			PrintWriter writer = new PrintWriter(highScoreFile);
			writer.print("");
			writer.close();
			writer = new PrintWriter(highScoreFile);
			for (int i = 0; i < highScores.length; i++) {
				writer.println(highScores[i]);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void rewriteHistory() {
		try {
			PrintWriter writer = new PrintWriter(historyFile);
			writer.print("");
			writer.close();
			writer = new PrintWriter(historyFile);

			writer.println(totalGamesPlayed);
			writer.println(totalAvgScore);

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readHighScores() {
		FileReader fileReader;
		try {
			fileReader = new FileReader(highScoreFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				String line = bufferedReader.readLine();
				for (int i = 0; line != null; i++) {
					if (line != null) {
						int temp = (Integer.parseInt(line));
						highScores[i] = temp;
					}
					line = bufferedReader.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readHistory() {
		FileReader fileReader;
		String history = "";
		try {
			fileReader = new FileReader(historyFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				String line = bufferedReader.readLine();
				StringBuffer strbuff = new StringBuffer();
				strbuff.append("Number Of Games Played: " + line);
				line = bufferedReader.readLine();
				strbuff.append("\nAverage Score: " + line);
				history = strbuff.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return history;
	}

	public int readHistoryGamesPlayed() {
		FileReader fileReader;
		int gamesPlayed = 0;
		try {
			fileReader = new FileReader(historyFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				String line = bufferedReader.readLine();
				gamesPlayed = Integer.parseInt(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gamesPlayed;
	}

	public double readHistoryAvgScore() {
		FileReader fileReader;
		double avgScore = 0;
		try {
			fileReader = new FileReader(historyFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			try {
				String line = bufferedReader.readLine();
				line = bufferedReader.readLine();
				avgScore = Double.parseDouble(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return avgScore;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == scorePanel.getStartBtn()) {
			gameOver = false;
			for (ColorPanel i : panelArr) {
				i.reset();
			}
			scorePanel.reset();
			seq.clear();
			new CompThread().start();
		}

		if (e.getSource() == chooseColor) {
			Object[] possibilities = { "Red", "Green", "Yellow", "Blue" };
			Icon icon = null;
			String s = (String) JOptionPane.showInputDialog(this, "Which color would you like to change?\n",
					"Color Chooser", JOptionPane.PLAIN_MESSAGE, icon, possibilities, "Red");
			if (s.equals("Red")) {
				panelArr[0].setColor();
			} else if (s.equals("Blue")) {
				panelArr[1].setColor();
			} else if (s.equals("Yellow")) {
				panelArr[2].setColor();
			} else {
				panelArr[3].setColor();
			}
			update(getGraphics());
		}

		if (e.getSource() == chooseMode) {
			Object[] possibilities = { "Slow", "Medium", "Fast" };
			Icon icon = null;
			String s = (String) JOptionPane.showInputDialog(this, "How fast do you want to play?\n",
					"Gamespeed Chooser", JOptionPane.PLAIN_MESSAGE, icon, possibilities, "Slow");
			if (s.equals("Slow")) {
				delay = 1000;
			} else if (s.equals("Medium")) {
				delay = 500;
			} else {
				delay = 250;
			}

		}

		if (e.getSource() == highScoreList) {
			StringBuffer hiBuffer = new StringBuffer();
			String msg = "";
			for (int i = 0; i < highScores.length; i++) {
				hiBuffer.append((i + 1) + ": ");
				hiBuffer.append(highScores[i]);
				hiBuffer.append("\n");
				msg = hiBuffer.toString();
			}
			JOptionPane.showMessageDialog(null, msg, "High Score List", JOptionPane.INFORMATION_MESSAGE);
			;
		}

		if (e.getSource() == history) {
			JOptionPane.showMessageDialog(null, readHistory(), "History", JOptionPane.INFORMATION_MESSAGE);
		}

		if (e.getSource() == about) {
			String msg = "This is the in-class version of Simonish with add-ons done\n" + "by Spencer Duff."
					+ "\nThere are no bonus features." + "\nVersion 1.0";
			JOptionPane.showMessageDialog(null, msg, "About", JOptionPane.INFORMATION_MESSAGE);
		}

		if (e.getSource() == rules) {
			String msg = "This is the childhood game of Simon.\n"
					+ "To play, press Start and then click on the colors that light up.\n"
					+ "You have to press the colors in the order that they light up in,\n"
					+ "with one more color added each time you complete the sequence."
					+ "\nYou can choose the color of the panels and the speed of the game in the Settings."
					+ "\nThe Stats menu has the High Score list and History of games."
					+ "\nThe Help menu has information about the game and these rules.";
			JOptionPane.showMessageDialog(null, msg, "Rules", JOptionPane.INFORMATION_MESSAGE);

		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameOver)
			return;
		if (playerTurn == false)
			return;

		ColorPanel tmp = ((ColorPanel) e.getSource());
		tmp.pressed();

		if (tmp != seqItr.next()) {
			gameOver();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (gameOver)
			return;
		if (playerTurn == false)
			return;

		ColorPanel tmp = ((ColorPanel) e.getSource());
		tmp.released();
		if (!seqItr.hasNext()) {
			scorePanel.incrScore();
			new CompThread().start();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
