package cs2410.components;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ScorePanel extends JPanel {
	private JButton startBtn = new JButton("Start");
	private int highScore;
	private int score;
	private JLabel highScoreLabel;
	private JLabel scoreLabel;
	
	public ScorePanel() {
		scoreLabel = new JLabel("Score: " + score, SwingConstants.CENTER);
		highScoreLabel = new JLabel("High Score: " + highScore, SwingConstants.CENTER);
		
		setLayout(new GridLayout(1, 3));
		
		add(scoreLabel);
		add(startBtn);
		add(highScoreLabel);		
	}
	
	public JButton getStartBtn() {
		return startBtn;
	}
	
	public void reset() {
		score = 0;
		scoreLabel.setText("Score: " + score);
		update(scoreLabel.getGraphics());
	}
	
	public void incrScore() {
		score++;
		scoreLabel.setText("Score: " + score);
		update(scoreLabel.getGraphics());
		checkHighScore();
	}
	
	private void checkHighScore() {
		if (score > highScore) {
			highScore = score;
			highScoreLabel.setText("High Score: " + highScore);
			this.update(this.getGraphics());
		}
	}
	
	public int getScore() {
		return score;
	}
	
	public int getHighScore() {
		return highScore;
	}
}
