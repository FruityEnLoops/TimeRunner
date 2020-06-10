package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	static Scene scene;
	static ArrayList<Task> taskList;
	static final String versionNumber = "1.1";
	
	public void start(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("interface.fxml"));
		Parent root = loader.load();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("TimeRunner");
		stage.setResizable(false);
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		stage.show();
	}

	public static void main(String[] args) {
		System.out.println("[DEBUG] Starting up...");
		
		// initialise l'ArrayList, au cas ou on doit générer un fichier vide
		Main.taskList = new ArrayList<Task>();
		
		Application.launch(args);
	}
        
	public void stop() {
		// vérifie qu'aucune tâche n'est encore en cours
		boolean check = checkForRunningTasks();
		if(check) {
			// avertis l'utilisateur si c'était le cas
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Attention");
			alert.setHeaderText("Une ou plusieurs tâches étaient encore en cours.");
			alert.setContentText("Veillez a arrêter vos tâches avant de fermer TimeRunner. La ou les tâches ont été arrêtées.");
			alert.showAndWait();
		}
		save();
		System.out.println("[DEBUG] Stop.");
	}
	
	public static void save() {
		System.out.println("[DEBUG] Saving...");
		ObjectOutputStream saveFile = null;
	    try {
	    	// essaye d'ouvrir l'outputFile
			final FileOutputStream fichier = new FileOutputStream("list.tasks");
			saveFile = new ObjectOutputStream(fichier);
			// écrit l'objet dans le fichier
			saveFile.writeObject(taskList);
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (saveFile != null) {
					saveFile.flush();
					saveFile.close();
				}
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
	    }
	}
	
	// méthode statique pour créer une tâche
	public static void addTask(Task t) {
		taskList.add(t);
		System.out.println("[DEBUG] Added task");
		System.out.println("[DEBUG] " + t);
	}
	
	// méthode statique pour supprimer une tâche
	public static void removeTask(Task t) {
		// vérifier qu'elle est bien arrêtée avant de la supprimer
		// (si la tâche n'était pas en cours, cette méthode n'a aucun effet)
		t.stopTimer();
		// supprimer la tâche
		taskList.remove(t);
		System.out.println("[DEBUG] Removed task");
		System.out.println("[DEBUG] " + t);
	}
	
	@SuppressWarnings("unchecked")
	// importe les données sérialisées
	public static void importSerialized() {
		FileInputStream inputFile = null;
		try {
			// essaye d'ouvrir l'inputFile
			inputFile = new FileInputStream("list.tasks");
		} catch (FileNotFoundException e) {
			// si fichier vide
			// avertir l'utilisateur, et permettre la génération d'un fichier vide
			System.out.println("[ERROR] File not found.");
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Erreur");
			alert.setHeaderText("Impossible de lancer TimeRunner");
			alert.setContentText("Fichier list.tasks non trouvé.");
			ButtonType generate = new ButtonType("Continuer (fichier vide)");
			ButtonType cancel = new ButtonType("Quitter", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(generate, cancel);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == cancel){
				// sinon quitter
				System.exit(1);
			}
			if (result.get() == generate){
				System.out.println("[DEBUG] Creating a dummy file (empty)");
				ObjectOutputStream saveFile = null;
			    try {
			    	final FileOutputStream fichier = new FileOutputStream("list.tasks");
			    	saveFile = new ObjectOutputStream(fichier);
			    	saveFile.writeObject(taskList);
			    } catch (final java.io.IOException e1) {
			    	e1.printStackTrace();
			    } finally {
			    	try {
			        if (saveFile != null) {
			        	saveFile.flush();
			        	saveFile.close();
			        }
			      } catch (final IOException ex) {
			        ex.printStackTrace();
			      }
			    }
			}
		}
		try {
			// le fichier est correct, donc on lit les données
			inputFile = new FileInputStream("list.tasks");
			ObjectInputStream taskInput = new ObjectInputStream(inputFile);
			Main.taskList = (ArrayList<Task>) taskInput.readObject();
			taskInput.close();
		} catch (IOException | ClassNotFoundException e) {
			// fichier incorrect : on évite de lire des données invalides / corrompues
			System.out.println("[ERROR] Invalid file.");
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setHeaderText("Impossible de lancer TimeRunner");
			alert.setContentText("Fichier list.tasks corrompu / contient des données invalides.");
			alert.showAndWait();
			System.exit(1);
		}
	}
		
	// vérifie si des tâches sont encore actives
	// très important car sinon les tâches seraient stockées dans un êtat "en cours", et le programme crasherait, car il essaierait d'arrêter une Timeline nulle, car objet transient
	// tout ce qui est sauvegardé ne doit PAS être "en cours"
	public boolean checkForRunningTasks() {
		boolean flag = false;
		for(int i = 0; i < Main.taskList.size(); i++) {
			if(Main.taskList.get(i).playing) {
				Main.taskList.get(i).stopTimer();
				flag = true;
			}
		}
		return flag;
	}
}