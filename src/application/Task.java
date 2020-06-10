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
	
	// crée l'objet timeline
	// (car il est transient, puisque Timeline n'est pas sérializable, il doit être réinitialisé a chaque démarrage du programme)
	// puis lance le timer
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
	
	// arrête le timer
	public void stopTimer() {
		if(playing) {
			this.time.pause();
			playing = !playing;
		}
	}

	// fonction démarrée a chaque (timerRefreshRate)ms
	// c'est ici que EventController.updateTimer(); aurait du être
	// j'ai gardé cette fonction au cas ou je trouve un fix
	// mais a ce stade cette fonction est obsolète et la ligne devrait être passée a la KeyFrame pour éviter un appel de fonction
	public void timer() {
		this.adjustTime(timerRefreshRate);
	}
	
	// renvoi le statut correspondant
	public String getStatus() {
		if(playing) {
			return "En cours";
		}		
		return "Arrêtée";
	}
	
	// ajoute (d)ms a la tâche courante (négatif, ou positif)
	public void adjustTime(double d) {
		this.currentTime = this.currentTime.add(new Duration(d));
	}
}
