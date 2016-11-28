import javafx.event.EventHandler;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.chart.LineChart;

public class LineCharts extends StackPane {

    private LineChart<String,Number> lineChart;

    public LineCharts(ArrayList<HashMap<String, Integer>> data) {

        super();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setLabel("Values");
        lineChart = new LineChart<String, Number>(xAxis, yAxis);
        lineChart.setTitle("");
        this.addData(data);
        this.getChildren().add(lineChart);
        for(XYChart.Series<String, Number> series : lineChart.getData()) {
            for (final XYChart.Data<String, Number> node : series.getData()) {
                node.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Label caption = new Label("");
                        caption.setStyle("-fx-font: 35 arial;");
                        caption.setStyle("-fx-background-color: #FFFFFF");
                        caption.setTranslateX(event.getSceneX());
                        caption.setTranslateY(event.getSceneY());
                        caption.setText(String.valueOf(node.getYValue()));
                        getChildren().add(caption);
                    }
                });
                node.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        getChildren().remove(getChildren().size() - 1);
                    }
                });
            }
        }
    }

    private void addData(ArrayList <HashMap<String,Integer>> data){

        //populating the series with data
        int i = 0;
        for (HashMap<String, Integer> temp : data) {
            XYChart.Series series = new XYChart.Series();
            series.setName("Country" + Integer.toString(i++));
            for (String date : temp.keySet()) {
                series.getData().add(new XYChart.Data(date, temp.get(date)));
            }
            lineChart.getData().add(series);
        }
    }
}