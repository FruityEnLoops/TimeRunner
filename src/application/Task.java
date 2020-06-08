package application;

import java.io.Serializable;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Task implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2583133132080240971L;
	
	private final int timerRefreshRate = 100;
	public String title;
	public boolean playing;
	public boolean done;
	public javafx.util.Duration currentTime;
	private transient Timeline time;
	
	Task(String title){
		this.title = title;
		this.playing = false;
		this.done = false;
		this.currentTime = new javafx.util.Duration(0);
	}
	
	public String toString() {
		return this.title;
	}
	
	public void startTimer() {
		this.time = new Timeline(new KeyFrame(
		        Duration.millis(100),
		        ae -> timer())
				);
		this.time.setCycleCount(Animation.INDEFINITE);
		if(!playing) {
			this.time.play();
			playing = !playing;
		}
	}
	
	public void stopTimer() {
		if(playing) {
			this.time.pause();
			playing = !playing;
		}
	}
	
	public void timer() {
		this.adjustTime(timerRefreshRate);
	}
	
	public String getStatus() {
		if(done) {
			return "Terminé";
		}
		if(playing) {
			return "En cours";
		}		
		return "Arrêtée";
	}
	
	public void adjustTime(double d) {
		this.currentTime = this.currentTime.add(new Duration(d));
	}
}
