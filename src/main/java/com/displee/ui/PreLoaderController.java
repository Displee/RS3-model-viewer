package com.displee.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * A class representing the UI controller used in the pre-loader.
 * @author Displee
 */
public class PreLoaderController implements Initializable {

	@FXML
	private Label loadingText;

	@FXML
	private ProgressBar progressBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void update(double progress, String message) {
		progressBar.setProgress(progress);
		loadingText.setText(message);
	}

}
