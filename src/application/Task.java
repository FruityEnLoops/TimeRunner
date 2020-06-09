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
	public javafx.util.Duration currentTime;
	private transient Timeline time;
	
	Task(String title){
		this.title = title;
		this.playing = false;
		this.currentTime = new javafx.util.Duration(0);
	}
	
	public String toString() {
		return this.title + " (" + this.getStatus() + ")";
	}
	
	// creates the Timeline object 
	// (as its transient, it needs to be reinitialize each time)
	// and plays the animation defined in the KeyFrame
	public void startTimer() {
		this.time = new Timeline(new KeyFrame(
		        Duration.millis(timerRefreshRate),
		        ae -> timer())
				);
		this.time.setCycleCount(Animation.INDEFINITE);
		if(!playing) {
			this.time.play();
			playing = !playing;
		}
	}
	
	// stops timer and unmarks it as being playing
	public void stopTimer() {
		if(playing) {
			this.time.pause();
			playing = !playing;
		}
	}
	
	// commands to be ran by the timer, each (timerRefreshRate)ms
	// this is where EventController.updateTimer(); should have been used
	// this function is obsolete and could be removed (passing the line it executes rather than the function call)
	public void timer() {
		this.adjustTime(timerRefreshRate);
	}
	
	// returns the status corresponding to the current timer state
	public String getStatus() {
		if(playing) {
			return "En cours";
		}		
		return "Arrêtée";
	}
	
	// adjusts time by a given double value (in ms)
	public void adjustTime(double d) {
		this.currentTime = this.currentTime.add(new Duration(d));
	}
}
