

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javafx.application.Application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

public class Main extends Application {
    //Variables
    private static final int DEF_SIZE = 80;
    private static final int RADIUS = DEF_SIZE / 2;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLUMNS = 7;
    private Disc [][] grid = new Disc[NUM_COLUMNS][NUM_ROWS];
    private Pane discroot = new Pane();
    private boolean RED_MOVE = true;
    private Stage popup = new Stage();


    private Parent CreateContent() {
        Pane root = new Pane();
        root.getChildren().add(discroot);

        Shape gridShape = makeBoard();

        root.getChildren().add(gridShape);
        root.getChildren().addAll(HIGHLIGHT());

        return root;
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Set title
        stage.setTitle("4Across");
        // Game Title
        Text myText = new Text();
        myText.setText("4Across");
        myText.setFont(Font.font("Consolas",100));
        Rectangle myRect = new Rectangle(400,100);
        myRect.setFill(Color.WHITE);
        myRect.setStroke(Color.BLACK);
        myRect.setStrokeWidth(10);
        // Add Background
        Image myImage = new Image(new FileInputStream(
                "C:\\Users\\georg\\IdeaProjects\\ConnectFour\\src\\main\\java\\img.png"),0,0,false,false);
        BackgroundImage backgroundImage = new BackgroundImage
                (myImage, BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.CENTER,BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);

        // Create and assign Buttons
        Button start = new Button("START GAME");
        start.setFont(Font.font("Consolas",30));
        start.setOnAction(event ->

                stage.setScene(new Scene(CreateContent()))
        );
        Button exit = new Button("EXIT");
        exit.setFont(Font.font("Consolas",30));
        exit.setOnAction(event->
                System.exit(0)
        );
        //Stackpane for title box
        StackPane myStack = new StackPane();
        myStack.getChildren().addAll(myRect,myText);
        // Create and add to Vbox
        VBox myBox = new VBox(10);
        myBox.getChildren().add(myStack);
        myBox.getChildren().add(start);
        myBox.getChildren().add(exit);
        myBox.setAlignment(Pos.CENTER);
        myBox.setBackground(background);
        // Create Scene
        Scene scene = new Scene(myBox,500,500);
        // Center and show my Scene
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

    }

    // Code for Game

    private Shape makeBoard() {
        //Creates the Board/rectangle
        Shape RECT = new Rectangle((NUM_COLUMNS + 1) * DEF_SIZE, (NUM_ROWS + 1) * DEF_SIZE);
        for (int a = 0; a < NUM_COLUMNS; a++) {
            for (int b = 0; b < NUM_ROWS; b++) {
                // itterates through the loop cutting out the circles to make the board
                Circle CIRCLE = new Circle(RADIUS);
                CIRCLE.setCenterX(RADIUS);
                CIRCLE.setCenterY(RADIUS);
                CIRCLE.setTranslateX(a * (DEF_SIZE + 5) + DEF_SIZE / 4);
                CIRCLE.setTranslateY(b * (DEF_SIZE + 5) + DEF_SIZE / 4);
                RECT = Shape.subtract(RECT, CIRCLE);
            }
        }
        // makes the board blue
        RECT.setFill(Color.BLUE);


        return RECT;
    }


    public List<Rectangle> HIGHLIGHT() {
        List<Rectangle> LIST = new ArrayList<>();
        for (int a = 0; a < NUM_COLUMNS; a++) {
            //Goes through the columns creating a rectangle
            Rectangle H_BAR = new Rectangle(DEF_SIZE, (NUM_ROWS + 1) * DEF_SIZE);
            H_BAR.setTranslateX(a * (DEF_SIZE + 5) + DEF_SIZE / 4);
            // Set first as transparent
            H_BAR.setFill(Color.TRANSPARENT);
            // Bar turns gray when mouse enter the column
            H_BAR.setOnMouseEntered(event -> {
                H_BAR.setFill(Color.rgb(211,211,211,0.50));
            });
            // Goes back to transparent
            H_BAR.setOnMouseExited(event  -> {
                H_BAR.setFill(Color.TRANSPARENT);
            });
            final int COLUMN = a;

            H_BAR.setOnMouseClicked(event -> {
                DROP_PIECE(new Disc(RED_MOVE),COLUMN);
            });

            LIST.add(H_BAR);
        }
        return LIST;
    }
    private static class Disc extends Circle
    {
        private final boolean RED;
        public Disc(boolean RED)
        {
            super(RADIUS,RED ? Color.RED: Color.YELLOW);
            this.RED = RED;
            setCenterX(RADIUS);
            setCenterY(RADIUS);

        }


    }

    private void DROP_PIECE(Disc Disc, int COLUMN)
    {
        int ROW = NUM_ROWS -1;
        do
        {
            if(!GET_PIECE(COLUMN,ROW).isPresent())
                break;
            ROW --;
        } while (ROW >= 0);

        if(ROW < 0)
            return;
        grid[COLUMN][ROW] = Disc;
        discroot.getChildren().add(Disc);
        Disc.setTranslateX (COLUMN * (DEF_SIZE + 5) + DEF_SIZE / 4);;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5),Disc);
        animation.setToY(ROW * (DEF_SIZE + 5) + DEF_SIZE / 4);;
        final int CUR_ROW = ROW;
        animation.setOnFinished( event ->
        {
            if(GAME_ENDED(COLUMN, CUR_ROW))
            {
                GAME_OVER();
            }

            RED_MOVE = ! RED_MOVE;
        });

        animation.play();

    }

    private Optional<Disc> GET_PIECE(int COLUMN, int ROW)
    {
        if(COLUMN < 0 || COLUMN >= NUM_COLUMNS
        || ROW < 0 || ROW >= NUM_ROWS)
            return Optional.empty();
        return Optional.ofNullable(grid[COLUMN][ROW]);

    }


    private boolean GAME_ENDED(int COLUMN, int ROW) {
        List<Point2D> VERTICAL = IntStream.rangeClosed(ROW - 3, ROW + 3)
                .mapToObj(r -> new Point2D.Double(COLUMN, r))  // Use Point2D.Double
                .filter(p -> isValidPoint((int)p.getX(), (int)p.getY()))
                .collect(Collectors.toList());

        List<Point2D> HORZONTAL = IntStream.rangeClosed(COLUMN - 3, COLUMN + 3)
                .mapToObj(c -> new Point2D.Double(c, ROW))  // Use Point2D.Double
                .filter(p -> isValidPoint((int)p.getX(), (int)p.getY()))
                .collect(Collectors.toList());

        Point2D TOP_LEFT = new Point2D.Double(COLUMN - 3, ROW - 3);  // Use Point2D.Double
        List<Point2D> DIAGONAL_1 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> new Point2D.Double(TOP_LEFT.getX() + i, TOP_LEFT.getY() + i))  // Use Point2D.Double
                .filter(p -> isValidPoint((int)p.getX(), (int)p.getY()))
                .collect(Collectors.toList());

        Point2D BOTTOM_LEFT = new Point2D.Double(COLUMN - 3, ROW + 3);  // Use Point2D.Double
        List<Point2D> DIAGONAL_2 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> new Point2D.Double(BOTTOM_LEFT.getX() + i, BOTTOM_LEFT.getY() - i))  // Use Point2D.Double
                .filter(p -> isValidPoint((int)p.getX(), (int)p.getY()))
                .collect(Collectors.toList());

        return CHECK_RANGE(VERTICAL) || CHECK_RANGE(HORZONTAL)
                || CHECK_RANGE(DIAGONAL_1) || CHECK_RANGE(DIAGONAL_2);
    }

    private boolean isValidPoint(int COLUMN, int ROW) {
        // Check if the column and row are within valid bounds
        return COLUMN >= 0 && COLUMN < NUM_COLUMNS && ROW >= 0 && ROW < NUM_ROWS;
    }




    private boolean CHECK_RANGE(List<Point2D> POINTS)
    {
        int RESULT = 0;
        for(Point2D p : POINTS)
        {
            int COLUMN = (int) p.getX();
            int ROW = (int) p.getY();
            Disc Disc = GET_PIECE(COLUMN,ROW).orElse(new Disc(! RED_MOVE));

            if(Disc.RED == RED_MOVE)
            {
                RESULT ++;
                if(RESULT == 4)
                {
                    return true;
                }
            }
            else
            {
                RESULT = 0;
            }
        }
        return false;
    }

    private void GAME_OVER()
    {
        //  Set title
        popup.setTitle("4Across");
        // Create pop up message
       Popup game_Over = new Popup();
       Text myText = new Text();
       myText.setText("Winner is : " + (RED_MOVE ? "RED" : "YELLOW" ));
       myText.setFont(Font.font("Consolas",50));
        // Retry and Exit Buttons/Conditions
        Button playAgain = new Button("Play Again");
        playAgain.setFont(Font.font("Consolas",30));
        playAgain.setOnAction(event ->
                playAgain()
        );
        Button exit = new Button("EXIT");
        exit.setFont(Font.font("Consolas",30));
        exit.setOnAction(event->
                System.exit(0)
        );
        // Create Vbox
        VBox myBox = new VBox(10);
        // Create background for when RED or Yellow player wins
        BackgroundFill red = new BackgroundFill(Color.RED,CornerRadii.EMPTY, Insets.EMPTY);
        Background rojo = new Background(red);
        BackgroundFill yellow = new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY,Insets.EMPTY);
        Background amarillo = new Background(yellow);
        // Display RED or YELlOW depending on who wins
        myBox.setBackground(RED_MOVE? rojo : amarillo);
        myBox.getChildren().addAll(myText,playAgain,exit);
        myBox.setAlignment(Pos.CENTER);
        // Add Buttons and Message to Popup
        Scene myScene = new Scene(myBox,500,500);
        popup.setScene(myScene);
        popup.centerOnScreen();
        popup.show();
        game_Over.show(popup);
    }

    private void playAgain()
    {
        // Close the Winner Message
        popup.close();
        // Reset the game
        grid = new Disc[NUM_COLUMNS][NUM_ROWS];
        discroot.getChildren().clear();
        RED_MOVE = false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}




