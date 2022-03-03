package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * GUI klassen til admin, som skal starte serveren
 */
public class TjenerUI extends Application {

    Button btnStart = new Button("Start server");

    final int BREDDE = 700;
    final int HØYDE = 600;

    static BorderPane main = new BorderPane();
    static HBox mainContainer = new HBox(10);
    static BorderPane left = new BorderPane();
    static BorderPane right = new BorderPane();
    static HBox leftTittelContainer = new HBox();
    static HBox rightTittelContainer = new HBox();
    static Label leftTittel = new Label("Aktive rom");
    static Label rightTittel = new Label("Aktivitet");
    static VBox romContainer = new VBox();
    static ScrollPane scroll = new ScrollPane();
    static VBox scrollContainer = new VBox();
    static ScrollPane scrollRight = new ScrollPane();
    static VBox aktivitetContainer = new VBox();

    Stage stage;

    static int max_visninger = 20;

    static LinkedList<String> aktivitetList = new LinkedList<>();

    @Override
    public void start(Stage stage) throws Exception{
        this.stage = stage;
        VBox startServer = new VBox();
        startServer.getChildren().add(btnStart);
        startServer.setAlignment(Pos.CENTER);
        lagGUI();
        stage.setTitle("Admin konsoll");
        Scene scene = new Scene(main, BREDDE, HØYDE);
        stage.setScene(new Scene(startServer, BREDDE, HØYDE));
        stage.show();

        btnStart.setOnAction(e -> {
            Thread t = new Thread(new HandleSession());
            t.start();
            stage.setScene(scene);

            oppdaterAktivitetdisplay("Server er startet");

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent e) {
                    Platform.exit();
                    // Exit
                    t.stop();
                    System.exit(0);
                }
            });
        });
    }

    private void lagGUI(){
        mainContainer.setAlignment(Pos.CENTER);
        main.setCenter(mainContainer);
        mainContainer.getChildren().addAll(left, right);
        left.setCenter(scroll);
        scroll.setContent(romContainer);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        romContainer.setAlignment(Pos.TOP_CENTER);
        left.setTop(leftTittelContainer);
        leftTittelContainer.getChildren().addAll(leftTittel);

        scrollRight.setContent(aktivitetContainer);
        scrollRight.setFitToWidth(true);
        scrollRight.maxHeightProperty().bind(right.heightProperty());
        right.maxHeightProperty().bind(mainContainer.heightProperty());
        right.setCenter(scrollContainer);
        scrollContainer.getChildren().add(scrollRight);
        right.setTop(rightTittelContainer);
        rightTittelContainer.getChildren().addAll(rightTittel);

        left.prefWidthProperty().bind(stage.widthProperty().multiply(0.375));
        right.prefWidthProperty().bind(stage.widthProperty().multiply(0.575));

        tildelCSS();
    }

    private void tildelCSS(){
        leftTittelContainer.setStyle(" -fx-border-color: black;\n" +
                "    -fx-border-width: 3;\n" +
                "    -fx-border-style: solid;\n" +
                "    -fx-padding: 5;");
        leftTittelContainer.setAlignment(Pos.CENTER);
        rightTittelContainer.setStyle(" -fx-border-color: black;\n" +
                "    -fx-border-width: 3;\n" +
                "    -fx-border-style: solid;\n" +
                "    -fx-padding: 5;");
        rightTittelContainer.setAlignment(Pos.CENTER);
        leftTittel.setFont(new Font(18));
        rightTittel.setFont(new Font(18));

        romContainer.setStyle(" -fx-border-color: black;\n" +
                "    -fx-border-width: 3;\n" +
                "    -fx-border-style: solid;\n" +
                "    -fx-padding: 5;");
        scrollContainer.setStyle(" -fx-border-color: black;\n" +
                "    -fx-border-width: 3;\n" +
                "    -fx-border-style: solid;\n" +
                "    -fx-padding: 5;");
    }

    /**
     *
     * @param aktiveRom rom som skal displayes
     */
    public static void oppdaterRomdisplay(ArrayList<Rom> aktiveRom){
        romContainer.getChildren().clear();
        for(Rom rom: aktiveRom){
            Label navn = new Label(rom.romNavn);
            Label brukere = new Label(rom.brukereTilkoblet.size() + " brukere tilkoblet");

            navn.setGraphic(brukere);
            navn.setContentDisplay(ContentDisplay.BOTTOM);

            navn.setAlignment(Pos.CENTER);
            navn.setContentDisplay(ContentDisplay.BOTTOM);

            navn.setWrapText(true);
            navn.setStyle("-fx-background-color: white;");
            navn.setStyle("-fx-border-width: 1px;");
            navn.setStyle("-fx-border-style: solid;");
            navn.setStyle("-fx-border-color: black;");
            navn.setPadding(new Insets(10));
            navn.prefWidthProperty().bind(romContainer.widthProperty());

            romContainer.getChildren().add(navn);
        }

    }

    /**
     *
     * @param tekst ny aktivitet som skal displayes
     */
    public static void oppdaterAktivitetdisplay(String tekst){

        if(aktivitetList.size() == max_visninger)
            aktivitetList.removeLast();

        aktivitetList.push(tekst);
        aktivitetContainer.getChildren().clear();
        for(String hendelse: aktivitetList){
            Label mld = new Label(hendelse);
            Label timestamp = new Label(new Date().toString());
            timestamp.setFont(new Font(9));
            mld.setGraphic(timestamp);
            mld.setAlignment(Pos.CENTER);
            mld.setContentDisplay(ContentDisplay.BOTTOM);

            mld.setWrapText(true);
            mld.setStyle("-fx-background-color: white;");
            mld.setStyle("-fx-border-width: 1px;");
            mld.setStyle("-fx-border-style: solid;");
            mld.setStyle("-fx-border-color: black;");
            mld.setPadding(new Insets(10));
            mld.prefWidthProperty().bind(aktivitetContainer.widthProperty());

            aktivitetContainer.getChildren().add(mld);
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        launch(args);
    }
}
