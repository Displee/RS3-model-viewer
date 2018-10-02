package com.displee.ui.notify;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.displee.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * An utility class used to handle notification dialogs.
 * @author Displee
 */
public class NotificationCenter {

	public static Optional<String> notifyText(String title, String header, String content) {
		final TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);
		return dialog.showAndWait();
	}

	public static void notify(String title, String header, String content) {
		notify(AlertType.INFORMATION, title, header, content, false);
	}

	public static Optional<ButtonType> notify(AlertType type, String title, String header, String content, boolean showAndWait) {
		final Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().addAll(Main.stage.getIcons());
		if (showAndWait) {
			return alert.showAndWait();
		} else {
			alert.show();
			return null;
		}
	}

	public static boolean notifyContinue(AlertType type, String title, String header, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.getDialogPane().setPrefSize(500, 200);

		ButtonType yesButton = new ButtonType("Yes, I wan to continue");
		ButtonType noButton = new ButtonType("No, I want to stop");

		alert.getButtonTypes().setAll(yesButton, noButton);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().addAll(Main.stage.getIcons());
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yesButton) {
			return true;
		}
		return false;
	}

	public static void notifyException(Exception ex) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Dialog");
		alert.setHeaderText("An exception has occurred. Please do not kill your self.");
		alert.setContentText("View the text below for more information");
		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}
}