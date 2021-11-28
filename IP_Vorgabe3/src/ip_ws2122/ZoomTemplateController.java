// IP WS2021/22 Zoom Template
//
// Copyright (C) 2021 by Klaus Jung
// All rights reserved.
// Date: 2021-11-12

package ip_ws2122;

import java.io.File;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class ZoomTemplateController {
	private static List<Path> paths;
	private static final String initialFileName = "head.png";
	private static File fileOpenPath = new File(".");
	private static final double maxZoom = 50.0;
	private static final int maxZoomedImageDimension = 4000;
	private static final double initialZoom = 8.0;

	private RasterImage image;
	private double zoom = initialZoom;

	@FXML
	private Slider zoomSlider;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private ImageView imageView;

	@FXML
	private Canvas overlayCanvas;

	@FXML
	public void initialize() {
		zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				zoom = new_val.doubleValue();
				zoomChanged();
			}
		});
		// load and process default image
		image = new RasterImage(new File(initialFileName));
		image.setToView(imageView);
		resetZoom();
		processImage();
	}

	@FXML
	void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			image = new RasterImage(selectedFile);
			image.setToView(imageView);
			paths.clear();
			resetZoom();
			processImage();
		}
	}

	private void resetZoom() {
		double max = Math.min(maxZoomedImageDimension / image.width, maxZoomedImageDimension / image.height);
		if (max > maxZoom)
			max = maxZoom;
		zoomSlider.setMax(max);
		zoom = initialZoom;
		zoomSlider.setValue(zoom);
		zoomChanged();
	}

	private void zoomChanged() {
		double zoomedWidth = Math.ceil(zoom * image.width);
		double zoomedHeight = Math.ceil(zoom * image.height);

		imageView.setFitWidth(zoomedWidth);
		imageView.setFitHeight(zoomedHeight);
		drawOverlay();
	}

	private void processImage() {
		RasterImage origImg = new RasterImage(imageView);
		RasterImage binImg = new RasterImage(origImg); // create a clone of origImg
		binImg.findContour();

		paths = binImg.getPath();
		origImg.setToView(imageView);
		// draw results into overlay
    	drawOverlay();
	}

	private void drawOverlay() {
		double zoomedWidth = Math.ceil(zoom * image.width);
		double zoomedHeight = Math.ceil(zoom * image.height);
		overlayCanvas.setWidth(zoomedWidth);
    	overlayCanvas.setHeight(zoomedHeight);
		GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
		if (paths != null) {
	
			for (Path p : paths) {
				List<int[]> coordinate = p.getEckPunkte();

				if (p.isAussere()) {
					gc.setStroke(Color.RED);
				} else {
					gc.setStroke(Color.YELLOW);
				}
					gc.beginPath();
					gc.moveTo(coordinate.get(0)[0] * zoom, coordinate.get(0)[1]*zoom);
					for (int i = 1; i< coordinate.size(); i++) {
						int[] current = coordinate.get(i);
				    	gc.lineTo(current[0] *zoom , current[1] * zoom);
						gc.stroke();
					}
		}}


    	// ATTENTION: JavaFX throws an exception if zoom is too high
    

	}

}
