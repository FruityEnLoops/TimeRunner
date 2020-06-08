package application;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;

public class StatisticsEventController {
	@FXML
	PieChart chart;
	
	public void initialize() {
		refreshPie();
	}

	public void refreshPie() {
		chart = new PieChart();
		for(int i = 0; i < Main.taskList.size(); i++) {
			System.out.println("[DEBUG] Added to PieChart : " + Main.taskList.get(i));
			chart.getData().add(new PieChart.Data(Main.taskList.get(i).toString(), Main.taskList.get(i).currentTime.toSeconds()));
		}
		chart.setLegendSide(Side.LEFT);
	}
}
