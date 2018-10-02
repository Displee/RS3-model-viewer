package com.displee;

import com.displee.ui.PreLoaderController;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A class serving as a pre-loader for the main application.
 * @author Displee
 */
public class MainPreLoader extends Preloader {

	private Stage stage;

	private PreLoaderController controller;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.stage = primaryStage;
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pre_loader.fxml"));
		final Parent root = loader.load();
		primaryStage.setTitle("RS3 Model viewer by Displee");
		primaryStage.setScene(new Scene(root, 300, 130));
		primaryStage.show();
		controller = loader.getController();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification pn) {
		if (pn instanceof com.displee.ui.notify.ProgressNotification) {
			com.displee.ui.notify.ProgressNotification p = (com.displee.ui.notify.ProgressNotification) pn;
			controller.update(p.getProgress(), p.getMessage());
		}
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
		if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
			stage.hide();
		}
	}

}