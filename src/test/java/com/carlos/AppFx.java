package com.carlos;

import com.carlos.panes.MainPane;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AppFx extends Application {

	public static void main(String[] args) {
        launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("JMGroupTool");
		primaryStage.setResizable(false);

		Pane root = new Pane();
		MainPane mainPane = new MainPane(primaryStage);
		root.getChildren().add(mainPane.getPane());
		
		primaryStage.setScene(new Scene(root, 1090, 630));
		primaryStage.show();
	}
}