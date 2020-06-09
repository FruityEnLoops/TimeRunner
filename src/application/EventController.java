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
	
	// makes sure every label has its content, and default texts
	public void initialize() {
		taskView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		taskView.getSelectionModel().getSelectedItems().addListener(new ListListener());
		statusBar.setText("Tâches en cours : " + currentlyRunningTaskCount);
		versionLabel.setText("TimeRunner v" + Main.versionNumber);
		System.out.println("[DEBUG] Loading serialized data : list.tasks");
		Main.importSerialized();
		updateListView();
	}

	// handles quit MenuItem
	public void clickedQuitter(ActionEvent event){
		Platform.exit();
	}
	
	// handles about Window
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
	
	// delegate method so it can be called by EventController to Main
	public void createTask() {
		Main.addTask(new Task(createTaskNameField.getText()));
		updateListView();
	}
	
	// handles manual save
	public void clickedSave(ActionEvent event) {
		Main.save();
		System.out.println("[DEBUG] (manual save triggered)");
	}
	
	// handy to refresh whenever any function needs it
	public void updateListView() {
		taskView.getItems().clear();
		taskView.getItems().addAll(Main.taskList);
	}
	
	// handles selection changes on the listView, to update the right panel
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
	
	// handles updating task info
	public void renameTask(ActionEvent event) {
		if(this.taskView.getSelectionModel().getSelectedItem() != null) {
			this.taskView.getSelectionModel().getSelectedItem().title = this.taskNameEditField.getText();
		}		
		updateListView();
	}
	
	// handles parsing the textField, and safely passing it to the adjustTime(); function
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
	
	// handles starting and pausing the timer, while updating any label that needs update (and updating the listView)
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
	
	// unused code
	// would update the timer info on the right panel
	// would need to be static to work (as to be called by Task) 
	// however setting it to static just wouldn't work as any variable in it would need to be static as well
	public void updateTimer() {
		currentTime.setText(taskView.getSelectionModel().getSelectedItem().currentTime.toSeconds() + "s");
	}
	
	// delegate method, with a warning to the user 
	// (and update so you can't select any null item, even though all functions are secured against null items)
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
	
	// opens a simple window with a PieChart with current data
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
