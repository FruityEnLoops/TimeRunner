package application;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class EventController {
	@FXML
	Label statusBar;
	@FXML
	Label versionLabel;
	@FXML
	Button createTaskButton;
	@FXML
	TextField createTaskNameField;
	@FXML
	ListView<Task> taskView;
	@FXML
	TextField taskNameEditField;
	@FXML
	Button taskNameEditButton;
	@FXML
	Label currentStatus;
	@FXML
	Button onOff;
	@FXML
	Button minusTime;
	@FXML
	Button plusTime;
	@FXML
	TextField timeModTextField;
	@FXML
	Label currentTime;
	@FXML
	Label taskPanelTitle;
	@FXML
	Button deleteTaskButton;
	
	public int currentlyRunningTaskCount = 0;
	
	// force les textes par défaut des label et des textfield
	public void initialize() {
		taskView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		taskView.getSelectionModel().getSelectedItems().addListener(new ListListener());
		statusBar.setText("Tâches en cours : " + currentlyRunningTaskCount);
		versionLabel.setText("TimeRunner v" + Main.versionNumber);
		System.out.println("[DEBUG] Loading serialized data : list.tasks");
		Main.importSerialized();
		updateListView();
	}

	// ferme le stage depuis l'eventcontroller
	public void clickedQuitter(ActionEvent event){
		Platform.exit();
	}
	
	// fait apparaitre la fenêtre "a propos"
	public void clickedAbout(ActionEvent event) throws IOException {
		System.out.println("[DEBUG] Opening aboutWindow...");
		Stage aboutWindow = new Stage();
		Scene scene;
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("aboutWindow.fxml"));
		Parent root = loader.load();
		scene = new Scene(root);
		aboutWindow.setResizable(false);
		aboutWindow.setScene(scene);
		aboutWindow.setTitle("A propos de TimeRunner");
		aboutWindow.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		aboutWindow.show();
	}
	
	// méthode déléguée de Main.addTask();, se charge aussi de rafraichir la listView
	public void createTask() {
		Main.addTask(new Task(createTaskNameField.getText()));
		updateListView();
	}
	
	// se charge de la sauvegarde manuelle, méthode déléguée de Main.save();
	public void clickedSave(ActionEvent event) {
		Main.save();
		System.out.println("[DEBUG] (manual save triggered)");
	}
	
	// pratique pour mettre a jour la listView quand une fonction en a besoin
	public void updateListView() {
		taskView.getItems().clear();
		taskView.getItems().addAll(Main.taskList);
	}
	
	// listener pour mettre a jour la partie droite de l'interface quand on selectionne une tâche de la listView
	class ListListener implements ListChangeListener<Task> {
	    public void onChanged(javafx.collections.ListChangeListener.Change<? extends Task> c){
	    	if(!c.getList().isEmpty()) {
		    	currentTime.setText(c.getList().get(0).currentTime.toSeconds() + "s");
		    	currentStatus.setText(c.getList().get(0).getStatus());
		    	taskPanelTitle.setText(c.getList().get(0).title);
		    	taskNameEditField.setText(c.getList().get(0).title);
	    	}
	    }
  	}
	
	// permet de renommer une tâche
	public void renameTask(ActionEvent event) {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			this.taskView.getSelectionModel().getSelectedItem().title = this.taskNameEditField.getText();
		}		
		updateListView();
	}
	
	// sert a récuperer le temps contenu dans le textField, le vérifier, puis le passer a Task.adjustTime();
	public void adjustTimePlus(ActionEvent event) {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			int time = 0;
			try {
				time = Integer.parseInt(this.timeModTextField.getText()) * 1000;
				
			} catch (NumberFormatException e) {
				System.out.println("[ERROR] Text not recognized as int.");
				time = 0;
			}
			System.out.println("[DEBUG] Adjusting time by " + time + ".");
			this.taskView.getSelectionModel().getSelectedItem().adjustTime(time);
			currentTime.setText(taskView.getSelectionModel().getSelectedItem().currentTime.toSeconds() + "s");
		} else {
			System.out.println("[DEBUG] No tasks are selected.");
		}
	}
	
	public void adjustTimeMinus(ActionEvent event) {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			int time = 0;
			try {
				time = -Integer.parseInt(this.timeModTextField.getText()) * 1000;
				
			} catch (NumberFormatException e) {
				System.out.println("[ERROR] Text not recognized as int.");
				time = 0;
			}
			System.out.println("[DEBUG] Adjusting time by " + time + ".");
			this.taskView.getSelectionModel().getSelectedItem().adjustTime(time);
			currentTime.setText(taskView.getSelectionModel().getSelectedItem().currentTime.toSeconds() + "s");
		} else {
			System.out.println("[DEBUG] No tasks are selected.");
		}
	}
	
	// démarre et arrête le timer, et met a jour les label / listView
	public void timerButton(ActionEvent event) {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			if(this.taskView.getSelectionModel().getSelectedItem().playing) {
				this.taskView.getSelectionModel().getSelectedItem().stopTimer();
				System.out.println("[DEBUG] Stopped timer for task : " + this.taskView.getSelectionModel().getSelectedItem());
				currentlyRunningTaskCount--;
				statusBar.setText("Tâches en cours : " + currentlyRunningTaskCount);
				currentTime.setText(taskView.getSelectionModel().getSelectedItem().currentTime.toSeconds() + "s");
			} else {
				this.taskView.getSelectionModel().getSelectedItem().startTimer();
				System.out.println("[DEBUG] Started timer for task : " + this.taskView.getSelectionModel().getSelectedItem());
				currentlyRunningTaskCount++;
				statusBar.setText("Tâches en cours : " + currentlyRunningTaskCount);
			}
			currentStatus.setText(this.taskView.getSelectionModel().getSelectedItem().getStatus());
			updateListView();
		}
	}
	
	// code non utilisé
	// devait mettre a jour le temps actuel de la tâche, mais ne peut être appelé que depuis l'event controller (non statique)
	// rendre cette fonction statique impliquerait que toutes les variables sont statiques
	// donc inutilisée
	public void updateTimer() {
		currentTime.setText(taskView.getSelectionModel().getSelectedItem().currentTime.toSeconds() + "s");
	}
	
	// méthode déleguée, qui se charge d'avertir l'utilisateur avant la suppression
	// (et mettre a jour pour que la listView affiche le changement en temps réel)
	public void removeTask() {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Attention");
			alert.setHeaderText("Confirmation");
			alert.setContentText("Êtes vous sur de vouloir supprimer la tache " + this.taskView.getSelectionModel().getSelectedItem().title);
			ButtonType confirm = new ButtonType("Oui, supprimer");
			ButtonType cancel = new ButtonType("Non", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(confirm, cancel);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == confirm){
				Main.removeTask(taskView.getSelectionModel().getSelectedItem());
				updateListView();
			}
		}
	}
	
	// ouvre une fenêtre contenant une PieChart avec les données des tâches actuelles
	public void openStatistics(ActionEvent event) throws IOException {
		Stage stat = new Stage();
		PieChart chart = new PieChart();
		for(int i = 0; i < Main.taskList.size(); i++) {
			System.out.println("[DEBUG] Added to PieChart : " + Main.taskList.get(i));
			chart.getData().add(new PieChart.Data(Main.taskList.get(i).title, Main.taskList.get(i).currentTime.toSeconds()));
		}
		chart.setLegendSide(Side.LEFT);
		chart.setPrefSize(500, 500);

		StackPane root = new StackPane(chart);
		Scene statistics = new Scene(root);
		stat.setScene(statistics);
		stat.setTitle("TimeRunner - Statistiques");
		stat.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
		stat.show();
	}
}
