package view;

import exchange_rate.ExchangeRatesPane;
import api_model.Country;
import api_model.Indicator;
import api_model.Model;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import news_feed.NewsFeedPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main extends Application {


    /**
     * Reference to the Scrolling pane holding all countries checkboxes
     */
    private ScrollPane paneCountries;
    /**
     * Reference to the right-most pane shown in the graph generator, the proceed one
     */
    private BorderPane proceedPane;
    /**
     * Reference to the first icon, linked to the graph generator
     */
    private Pane graphIconPane;
    /**
     * Reference to the second icon, linked to the rss feed
     */
    private Pane rssIconPane;
    /**
     * Reference to the third icon, linked to the exchange rates pane
     */
    private Pane exchangeIconPane;
    /**
     * Reference to the exchange rates pane
     */
    private ScrollPane exchangeRatesPane;
    /**
     * Reference to the Vbox used to hold the countries checkboxes
     */
    private VBox countriesPlaceholder;
    /**
     * Reference to the VBox holding the labels for "current selected" fields
     */
    private VBox proceedInfo;
    /**
     * Reference to the main pane
     */
    private BorderPane mainPane;
    /**
     * Reference to the graph generator pane
     */
    private HBox settingsPane;
    /**
     * Reference to the Accordion used to display all the indicators
     */
    private Accordion indicatorsPlaceholder;
    /**
     * Reference to the left-most bar used to navigate through the application
     */
    private VBox controlBar;
    /**
     * Reference to the topbar
     */
    private BorderPane topBar;
    /**
     * Reference to the proceed button
     */
    private Button proceedButton;
    /**
     * Reference to the rss feed pane
     */
    private ScrollPane rssPane;
    /**
     * Reference to the currently opened stages linked with their indicator. We settled on this storing approached
     * as to be able to update panes currently open with more countries, without making duplicate windows
     */
    private HashMap<String, DataDisplayWrapper> openedStages = new HashMap<>();
    /**
     * Reference to all the radio buttons used (for indicators)
     */
    private ArrayList<RadioButton> buttons = new ArrayList<>();
    /**
     * Reference to the label used to show the current indicator selected
     */
    private Label currentIndicatorLabel = new Label();
    /**
     * reference to the label used to show the current countries selected
     */
    private Label currentCountriesLabel = new Label();
    /**
     * Reference to the about pane icon
     */
    private Pane aboutIconPane;
    /**
     * Reference to the about pane
     */
    private ScrollPane aboutpane;

    /**
     * Starting point of the application. All the view creation, styling, view-related logic is applied inside here.
     * @param primaryStage JavaFx standard, the primary stage is passed as an argument
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //get instance of model
        Model.getInstance();

        //load css
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        //set title
        primaryStage.setTitle("Ecru Economics");

        primaryStage.getIcons().add(new Image("icon_statistics.png"));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        //init all variables + gets references from fxml ids
        getReferences(scene);
        //applies css + manual styling
        stylePanes(scene);

        implementScreensSwitcher();


        aboutpane = new AboutPane(this);
        populateGraphsFilters();

        implementAdditionalPanes();

        //populate labels from proceed part
        populateProceedInformation();
        //logic for close all mechanism
        implementCloseAll();

        //Prevents resizing stage to smaller than initial size
        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMinWidth(primaryStage.getWidth());

        //show main stage
        primaryStage.show();
    }

    /**
     * Method used to launch the app
     * @param args extra args currently not in use
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Method used to redirect the user from Rss feed to an external browser
     * @param url
     */
    public void showLink(String url) {
        //external process method used to redirect from rss feed to the web
        getHostServices().showDocument(url);
    }

    private void getReferences(Scene scene) {
        mainPane = (BorderPane) scene.lookup("#main-pane");
        settingsPane = (HBox) scene.lookup("#graph-pane");
        controlBar = (VBox) scene.lookup("#control-bar");
        graphIconPane = (Pane) scene.lookup("#icon-1-placeholder");
        rssIconPane = (Pane) scene.lookup("#icon-2-placeholder");
        exchangeIconPane = (Pane) scene.lookup("#icon-3-placeholder");
        indicatorsPlaceholder = (Accordion) scene.lookup("#indicators-wrapper");
        paneCountries = (ScrollPane) scene.lookup("#pane-countries");
        countriesPlaceholder = (VBox) paneCountries.getContent().lookup("#countries-wrapper");
        proceedPane = (BorderPane) scene.lookup("#pane-proceed");
        topBar = (BorderPane) scene.lookup("#top-bar");
        proceedButton = (Button) scene.lookup("#proceed-button");
        proceedInfo = (VBox) scene.lookup("#proceed-info");

        aboutIconPane = (Pane) scene.lookup("#icon-4-placeholder");

        rssPane = new NewsFeedPane(this);
        exchangeRatesPane = new ExchangeRatesPane();

    }

    private void stylePanes(Scene scene) {

        scene.getStylesheets().add(this.getClass().getClassLoader()
                .getResource("styling.css").toExternalForm());

        topBar.setLeft(generateTitleText("Graph Generator"));
        controlBar.setStyle("-fx-background-color: #2c3138");
        topBar.setStyle("-fx-background-color: #F55028");
        topBar.setPadding(new Insets(10));
        settingsPane.setStyle("-fx-background-color: #3e4249");
        graphIconPane.setStyle("-fx-effect: dropshadow(gaussian, #000, 15, 0, 0,0);");
        proceedButton.setAlignment(Pos.CENTER);
        currentIndicatorLabel.setStyle("-fx-text-fill: #F55028; -fx-font-size: 18px");
        currentCountriesLabel.setWrapText(true);
        currentCountriesLabel.setStyle("-fx-text-fill: #F55028; -fx-font-size: 18px");
        currentIndicatorLabel.setWrapText(true);

    }

    private void populateGraphsFilters() {
        // adding all checkboxes
        countriesPlaceholder.getChildren().addAll(generateCheckBoxes(Model.countries));
        //adding indicators
        indicatorsPlaceholder.getPanes().addAll(generateTitledPanels(Model.indicators));
        //hide countries
        paneCountries.setVisible(false);
        //hide proceed
        proceedPane.setVisible(false);
    }

    private void implementCloseAll() {
        Button button = new Button("Close all windows");
        button.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: white;");
        button.setOnMouseClicked(e -> {
            if (openedStages.size() > 0) {
                openedStages.entrySet().forEach(entry -> {
                    entry.getValue().close();
                    openedStages = new HashMap<>();
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No opened window");
                alert.setHeaderText("Sorry, but there are no opened windows");
                alert.setContentText("To generate a new window please make sure you chose both" +
                        " the Indicator and countries wanted and finally press the Show it button");

                alert.showAndWait();
            }
        });

        topBar.setRight(button);
    }

    private void implementAdditionalPanes() {

        //this button will generate new graphs in different windows
        // a record of opened stages is kept in openedStage hashmap such that we can have external access to them.
        proceedButton.setOnMouseClicked(e -> {
            String indicator = Model.currentIndicator;
            if (!openedStages.keySet().contains(Model.currentIndicator)) {
                DataDisplayWrapper wrapper = new DataDisplayWrapper();
                wrapper.show();
                wrapper.setOnCloseRequest(e1 -> {
                    openedStages.remove(indicator);
                    wrapper.clearData();
                    unselectRadioButton(indicator);
                });
                openedStages.put(indicator, wrapper);
            } else if (!openedStages.get(indicator).getInCountries().equals(Model.currentCountries)) {
                openedStages.get(indicator).startThread();
                openedStages.get(indicator).toFront();
            }

        });
    }

    private void populateProceedInformation() {

        Label ci = new Label("Selected indicator: ");
        ci.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        Label cc = new Label("Selected countries: ");
        cc.setStyle("-fx-text-fill: white; -fx-font-size: 20px; ");
        cc.setPadding(new Insets(20, 0, 0, 0));
        proceedInfo.getChildren().addAll(ci, currentIndicatorLabel, cc, currentCountriesLabel);
    }

    private HBox generateCheckbox(Country country) {
        CheckBox cb = new CheckBox(country.getName());
        cb.setMaxHeight(30);
        cb.setPrefHeight(30);
        cb.setMinHeight(30);
        cb.setMnemonicParsing(false);
        cb.setPadding(new Insets(5));

        cb.setOnMouseClicked(event -> {
            Integer index = Arrays.asList(Model.countries).indexOf(country);
            if (cb.isSelected()) {
                // user want to add
                // check for limit
                if (Model.currentCountries.size() < 8) {
                    //add to current countries
                    if (!Model.currentCountries.contains(index)) {
                        Model.currentCountries.add(index);
                    }
                } else {
                    //too many countries
                    //alert the user
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Too many countries");
                    alert.setHeaderText("Sorry, but you can't chose more than 8 countries at the same time");
                    alert.setContentText("We have implemented this limitation as to make sure data " +
                            "visualisation is still keeps its informative purpose");

                    alert.showAndWait();
                    cb.setSelected(false);
                }


            } else {
                // remove from current countries
                if (Model.currentCountries.contains(index)) {
                    Model.currentCountries.remove(index);
                }

            }

            //checker to see if last pane should be shown
            if (howManyChecked(countriesPlaceholder) > 0) {
                if (!proceedPane.isVisible()) {
                    proceedPane.setVisible(true);
                    getAnimationFor(proceedPane, true).playFromStart();
                }

            } else {
                // getAnimationFor(proceedPane, false).play();
                // ^ not working yet :(
                proceedPane.setVisible(false);

            }


            //update the text in the rightmost pane such that it reflects all current countries after a selection is done
            updateLabel(currentCountriesLabel, currentCountriesToString());
        });
        HBox toReturn = new HBox();
        Image image = country.loadFlag();

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(25);
        imageView.setFitHeight(15);
        imageView.setLayoutY(5);
        toReturn.setPadding(new Insets(5));
        toReturn.getChildren().addAll(imageView, cb);
        toReturn.setAlignment(Pos.CENTER_LEFT);
        return toReturn;
    }

    private ArrayList<HBox> generateCheckBoxes(Country[] countries) {
        //tbh i love java 8
        ArrayList<Country> countriesConverted = new ArrayList<>(Arrays.asList(countries));
        return countriesConverted.stream().map(this::generateCheckbox).collect(Collectors.toCollection(ArrayList::new));
    }

    private TitledPane generateIndicator(Indicator indicator) {
        //make titled pane
        TitledPane indicatorPane = new TitledPane();
        indicatorPane.setText(indicator.getName());

        Pane pane = new Pane();
        pane.setPadding(new Insets(0));

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(0));

        ToggleGroup toggleGroup = new ToggleGroup();

        //populate from indicators
        for (String option : indicator.getSubIndicatorsLabels()) {
            RadioButton optionButton = generateIndicatorOption(option, true);
            optionButton.setOnMouseClicked(event -> {
                Model.currentIndicator = indicator.getCodeFromLabel(option);
                Model.currentObjectIndicator = indicator;
                if (howManyChecked(countriesPlaceholder) == 0) {
                    paneCountries.setVisible(true);
                    getAnimationFor(paneCountries, true).playFromStart();

                }
                updateLabel(currentIndicatorLabel, getIndicatorLabelFromCode(Model.currentIndicator));
            });
            optionButton.setToggleGroup(toggleGroup);
            vBox.getChildren().add(optionButton);
        }

        pane.getChildren().add(vBox);
        indicatorPane.setContent(pane);

        return indicatorPane;

    }

    private RadioButton generateIndicatorOption(String text, boolean active) {

        RadioButton button = new RadioButton(text);
        button.setPadding(new Insets(5));
        button.setMnemonicParsing(false);

        // deprecated. was used before when we though we might have indicators that we didnt implement yet
        // if (!active) button.setDisable(true);
        buttons.add(button);
        return button;
    }

    private ArrayList<TitledPane> generateTitledPanels(ArrayList<Indicator> indicators) {
        //so beautiful
        return indicators.stream().map(this::generateIndicator).collect(Collectors.toCollection(ArrayList::new));
    }

    private int howManyChecked(VBox placeholder) {
        int count = 0;
        for (Node node : placeholder.getChildren()) {
            HBox checkboxPlaceholder = (HBox) node;
            CheckBox checkBox = (CheckBox) checkboxPlaceholder.getChildren().get(1);
            if (checkBox.isSelected()) {
                count++;
            }
        }
        return count;
    }

    private void implementScreensSwitcher() {

        //icon for graph generator
        ImageView graphImage = new ImageView();
        graphImage.setImage(new Image(getClass().getClassLoader().getResourceAsStream("icon_statistics.png")));
        graphImage.setSmooth(true);
        graphImage.setCache(true);
        graphImage.setFitWidth(50);
        graphImage.setFitHeight(50);
        graphImage.setLayoutX(25);
        graphImage.setLayoutY(25);
        graphImage.setId("graph-icon");
        graphIconPane.getChildren().add(graphImage);

        //image for rss feed
        ImageView rssImage = new ImageView();
        rssImage.setImage(new Image(getClass().getClassLoader().getResourceAsStream("icon_rss.png")));
        rssImage.setSmooth(true);
        rssImage.setCache(true);
        rssImage.setFitHeight(50);
        rssImage.setFitWidth(50);
        rssImage.setLayoutX(25);
        rssIconPane.getChildren().add(rssImage);

        //image for exchange feed
        ImageView exchangeImage = new ImageView();
        exchangeImage.setImage(new Image(getClass().getClassLoader().getResourceAsStream("exchange-rates.png")));
        exchangeImage.setSmooth(true);
        exchangeImage.setCache(true);
        exchangeImage.setFitHeight(50);
        exchangeImage.setFitWidth(50);
        exchangeImage.setLayoutX(25);
        exchangeIconPane.getChildren().add(exchangeImage);


        ImageView aboutImage = new ImageView();
        aboutImage.setImage(new Image(getClass().getClassLoader().getResourceAsStream("about-icon.png")));
        aboutImage.setSmooth(true);
        aboutImage.setCache(true);
        aboutImage.setFitHeight(50);
        aboutImage.setFitWidth(50);
        aboutImage.setLayoutX(25);
        aboutIconPane.getChildren().add(aboutImage);


        aboutIconPane.setOnMouseClicked(event -> {
            if(mainPane.getCenter() != aboutpane){
                mainPane.getChildren().remove(mainPane.getCenter());
                aboutIconPane.setStyle("-fx-effect: dropshadow(gaussian, #000, 15, 0, 0,0);");
                graphIconPane.setStyle("");
                rssIconPane.setStyle("");
                mainPane.setCenter(aboutpane);
                topBar.setLeft(generateTitleText("About"));
            }
        });

        exchangeIconPane.setOnMouseClicked( event -> {
            if(mainPane.getCenter() != exchangeRatesPane){
                mainPane.getChildren().remove(mainPane.getCenter());
                exchangeIconPane.setStyle("-fx-effect: dropshadow(gaussian, #000, 15, 0, 0,0);");
                graphIconPane.setStyle("");
                rssIconPane.setStyle("");
                mainPane.setCenter(exchangeRatesPane);
                topBar.setLeft(generateTitleText("Exchange Rates"));
            }
        });

        rssIconPane.setOnMouseClicked(event -> {
            if (mainPane.getCenter() != rssPane) {
                mainPane.getChildren().remove(mainPane.getCenter());
                getAnimationFor(rssPane, true).playFromStart();
                rssIconPane.setStyle("-fx-effect: dropshadow(gaussian, #000, 15, 0, 0,0);");
                graphIconPane.setStyle("");
                exchangeIconPane.setStyle("");
                mainPane.setCenter(rssPane);
                topBar.setLeft(generateTitleText("News Feed"));
            }
        });

        graphIconPane.setOnMouseClicked(event -> {
            if (mainPane.getCenter() != settingsPane) {
                mainPane.getChildren().remove(mainPane.getCenter());
                mainPane.setCenter(settingsPane);
                graphIconPane.setStyle("-fx-effect: dropshadow(gaussian, #000, 15, 0, 0,0);");
                rssIconPane.setStyle("");
                exchangeIconPane.setStyle("");
                getAnimationFor(settingsPane, true).playFromStart();
                topBar.setLeft(generateTitleText("Graph Generator"));
            }


        });
    }


    private static Label generateTitleText(String text) {
        //factory method
        Label textElement = new Label(text);
        textElement.setPadding(new Insets(0));
        textElement.setAlignment(Pos.CENTER_LEFT);
        return textElement;
    }

    private FadeTransition getAnimationFor(Node node, boolean b) {
        //factory method
        FadeTransition ft = new FadeTransition(Duration.seconds(1), node);
        if (b) {
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
        }
        ft.setCycleCount(1);
        return ft;
    }

    private void unselectRadioButton(String indicator) {
        // helper method to unselect a given radio button (indicator) given its code, but not his name
        buttons.forEach(button -> {
            if(Objects.equals(button.getText(), getIndicatorLabelFromCode(indicator))){
                button.setSelected(false);
            }
        });
    }

    private void updateLabel(Label target, String text) {
        // if I really think about it I don't really find a purpose for this method
        target.setText(text);
    }

    private String currentCountriesToString() {

        // converts all current countries to a string with entries separated by ","
        String toReturn = "";
        for(Integer i : Model.currentCountries) {
            toReturn = toReturn + Model.countries[i].getName() + ", ";
        }
        // remove last ", " if we have entries in the string
        if (toReturn.length() > 2) {
            return toReturn.substring(0, toReturn.length() - 2);
        }
        return toReturn;
    }


    private String getIndicatorLabelFromCode(String code) {

        //iterates though indicators as to get, given its code, its name
        String toReturn = "";
        for(Indicator indicator : Model.indicators) {
            String[] codes = indicator.getSubIndicatorsCodes().toArray(new String[indicator.getSubIndicatorsCodes().size()]);
            String[] labels =  indicator.getSubIndicatorsLabels().toArray(new String[indicator.getSubIndicatorsLabels().size()]);
            for(int i = 0 ;i < codes.length; i++){
                if(codes[i].equals(code)) {
                    toReturn = labels[i];
                    break;
                }
            }
        }
        return toReturn;
    }


}


