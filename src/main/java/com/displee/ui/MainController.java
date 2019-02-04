package com.displee.ui;

import com.displee.Constants;
import com.displee.Main;
import com.displee.cache.ModelDefinition;
import com.displee.render.GLWrapper;
import com.displee.render.impl.DefaultGLRenderer;
import com.displee.ui.notify.NotificationCenter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.displee.CacheLibrary;
import org.displee.CacheLibraryMode;
import org.displee.progress.AbstractProgressListener;
import org.lwjgl.util.stream.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * A class representing the main UI controller of this application.
 * @author Displee
 */
public class MainController implements Initializable {

	@FXML
	private ListView<ModelDefinition> modelList;

	@FXML
	private AnchorPane imageViewWrapper;

	@FXML
	private ImageView imageView;

	@FXML
	private MenuItem openMenuItem;

	@FXML
	private MenuItem exportRawRS2MenuItem;

	@FXML
	private MenuItem exportRawRS3MenuItem;

	@FXML
	private MenuItem exportMQOMenuItem;

	@FXML
	private MenuItem quitMenuItem;

	@FXML
	private Label statusText;

	@FXML
	private Label fpsLabel;

	@FXML
	private ComboBox<Integer> fpsComboBox;

	@FXML
	private CheckBox showPolygons;

	@FXML
	private CheckBox enableTextures;

	@FXML
	private ComboBox<StreamUtil.RenderStreamFactory> renderOptions;

	/**
	 * The current cache.
	 */
	private CacheLibrary cacheLibrary;

	/**
	 * Our GL renderer.
	 */
	private GLWrapper<ModelDefinition> renderer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageView.fitWidthProperty().bind(imageViewWrapper.widthProperty());
		imageView.fitHeightProperty().bind(imageViewWrapper.heightProperty());
		openMenuItem.setOnAction(e -> {
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			final File directory = directoryChooser.showDialog(Main.stage);
			if (directory != null) {
				loadCache(directory.getAbsolutePath() + (directory.getAbsolutePath().endsWith("/") ? "" : "/"));
			}
		});
		exportRawRS2MenuItem.setOnAction(e -> {
			//TODO Export as raw RS2 model
		});
		exportRawRS3MenuItem.setOnAction(e -> {
			final ModelDefinition model = modelList.getSelectionModel().getSelectedItem();
			if (model == null) {
				NotificationCenter.notify(Alert.AlertType.WARNING, "Unknown model", "No model selected.", "Please choose a model first.", false);
				return;
			}
			final FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary files", ".dat"));
			final File file = fileChooser.showSaveDialog(Main.stage);
			if (file == null) {
				return;
			}
			try {
				Files.write(file.toPath(), cacheLibrary.getIndex(7).getArchive(model.getId()).getFile(0).getData());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		exportMQOMenuItem.setOnAction(e -> {
			//TODO Export as MQO file
		});
		quitMenuItem.setOnAction(e -> System.exit(0));
		modelList.setOnMouseClicked(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				final ModelDefinition definition = modelList.getSelectionModel().getSelectedItem();
				if (definition == null) {
					return;
				}
				definition.decode(cacheLibrary);
				renderer.setContext(definition);
			}
		});
		fpsComboBox.getItems().addAll(60, 100, 150);
		fpsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (renderer != null) {
				renderer.setFpsLimit(newValue);
			}
		});
		fpsComboBox.getSelectionModel().select(0);
		enableTextures.selectedProperty().addListener((ob, o, n) -> {
			Constants.ENABLE_TEXTURES = n;
		});
		showPolygons.selectedProperty().addListener((ob, o, n) -> {
			Constants.SHOW_POLYGONS = n;
		});
		renderOptions.valueProperty().addListener((obv, oldV, newV) -> {
			GLWrapper.runLater(() -> {
				renderer.setRenderStreamFactory(newV);
			});
		});
		new Thread(() -> {
			renderer = new DefaultGLRenderer(imageView);
			renderOptions.getItems().addAll(StreamUtil.getRenderStreamImplementations());
			Platform.runLater(() -> {
				renderOptions.getSelectionModel().select(0);
			});
			renderer.run(fpsLabel);
		}).start();
		Main.stage.setOnCloseRequest(e -> {
			if (renderer != null) {
				renderer.terminate();
			}
		});
	}

	/**
	 * Load a cache.
	 * @param path The path of the cache to load.
	 */
	private void loadCache(String path) {
		statusText.setText("Loading cache...");
		final AbstractProgressListener listener = new AbstractProgressListener() {
			@Override
			public void finish(String s, String s1) {

			}

			@Override
			public void change(double v, String s) {
				Platform.runLater(() -> {
					statusText.setText(s);
					if (v == 100) {
						statusText.setText("Ready");
					}
				});
			}
		};
		new Thread(() -> {
			try {
				cacheLibrary = new CacheLibrary(path, CacheLibraryMode.UN_CACHED, listener);
				if (!cacheLibrary.isRS3()) {
					cacheLibrary.close();
					cacheLibrary = null;
					Platform.runLater(() -> NotificationCenter.notify(Alert.AlertType.ERROR, "Unsupported cache", "The cache you've chosen is not valid.", "Only RS3 caches are allowed.", false));
					return;
				}
				Platform.runLater(() -> {
					modelList.getItems().clear();
					for (int i : cacheLibrary.getIndex(7).getArchiveIds()) {
						modelList.getItems().add(new ModelDefinition(i));
					}
				});
			} catch (IOException e) {
				Platform.runLater(() -> NotificationCenter.notifyException(e));
			}
		}).start();
	}

}
