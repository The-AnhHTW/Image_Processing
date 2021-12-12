// IP WS2021/22 Zoom Template
//
// Copyright (C) 2021 by Klaus Jung
// All rights reserved.
// Date: 2021-11-12

package ip_ws2122;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	private static List<List<Vektor2d>> svgPaths = new ArrayList<List<Vektor2d>>();
	private static final String initialFileName = "klein.png";
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
		markPolygonsFromPath();
		drawOverlay();
		for (List<Vektor2d> subpath : svgPaths) {
			for (Vektor2d coordinate : subpath) {
				System.out.println("x:" + coordinate.x + " || y:" + coordinate.y);
			}
		}

	}

	private void markPolygonsFromPath() {
		for (Path p : paths) {
			List<Vektor2d> subPath = new ArrayList<Vektor2d>();
			List<Vektor2d> pathCoordinates = transformToVektors(p.getEckPunkte());

			Vektor2d c0 = new Vektor2d(0, 0);
			Vektor2d c1 = new Vektor2d(0, 0);

			// Pivot Element ist das Element, das wir gerade betrachten
			Vektor2d pivotElement = pathCoordinates.get(0);
			subPath.add(pivotElement);

			Set<String> directions = new HashSet<String>();
			for (int i = 1; i < pathCoordinates.size(); i++) {
				Vektor2d toBeAnalyzed = pathCoordinates.get(i);
				Vektor2d previousAnalyzed = pathCoordinates.get(i - 1);
				directions.add(determineDirectionFromTwoPoints(previousAnalyzed, toBeAnalyzed));
				Vektor2d vItoVk = new Vektor2d(toBeAnalyzed.x - pivotElement.x, toBeAnalyzed.y - pivotElement.y);
				boolean toManyDirections = directions.size() > 3;
				boolean isNotStraight = isNotStraightPath(vItoVk, c0, c1);
				boolean isLastElement = i == pathCoordinates.size() - 1;
				if (toManyDirections || isNotStraight || isLastElement) {
					if (isLastElement) {
						pivotElement = toBeAnalyzed;
					} else {
						pivotElement = previousAnalyzed;
					}
					directions.clear();
					subPath.add(pivotElement);
					c0.x = 0;
					c0.y = 0;
					c1.x = 0;
					c1.y = 0;
					continue;

				}
				updateConstraints(vItoVk, c0, c1);

			}
			svgPaths.add(subPath);
		}
	}

	private boolean isNotStraightPath(Vektor2d direction, Vektor2d c0, Vektor2d c1) {
		if (Vektor2d.crossProduct(c0, direction) < 0 || Vektor2d.crossProduct(c1, direction) > 0) {
			return true;
		}
		return false;
	}

	private void updateConstraints(Vektor2d a, Vektor2d c0, Vektor2d c1) {
		if (a.x <= 1 && a.y <= 1)
			return;

		Vektor2d d0 = new Vektor2d(0, 0);
		Vektor2d d1 = new Vektor2d(0, 0);

		if (a.y >= 0 && (a.y > 0 || a.x < 0)) {
			d0.x = a.x + 1;
		} else {
			d0.x = a.x - 1;
		}

		if (a.x <= 0 && (a.x < 0 || a.y < 0)) {
			d0.y = a.y + 1;
		} else {
			d0.y = a.y - 1;
		}

		// c1
		if (a.y <= 0 && (a.y < 0 || a.x < 0)) {
			d1.x = a.x + 1;
		} else {
			d1.x = a.x - 1;
		}
		// c1
		if (a.x >= 0 && (a.x > 0 || a.y < 0)) {
			d1.y = a.x + 1;
		} else {
			d1.y = a.x - 1;
		}

		if (Vektor2d.crossProduct(c0, d0) >= 0) {
			c0 = d0;
		}
		if (Vektor2d.crossProduct(c1, d1) >= 0) {
			c1 = d1;
		}

	}

	private String determineDirectionFromTwoPoints(Vektor2d startpoint, Vektor2d endpoint) {
		if (startpoint.x < endpoint.x) {
			return "RIGHT";
		} else if (startpoint.x > endpoint.x) {
			return "LEFT";
		} else if (startpoint.y < endpoint.y) {
			return "TOP";
		} else if (startpoint.y > endpoint.y) {
			return "BOTTOM";
		}

		throw new Error("CONDITIONS ARE NOT SUFFICIENT?");
	}

	private List<Vektor2d> transformToVektors(List<int[]> coordinates) {
		return coordinates.stream().map(coordinate -> new Vektor2d(coordinate[0], coordinate[1]))
				.collect(Collectors.toList());
	}

	private void drawPath(List<Path> paths) {
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
					gc.setStroke(Color.BLUE);
				}
				gc.beginPath();
				gc.moveTo(coordinate.get(0)[0] * zoom, coordinate.get(0)[1] * zoom);
				for (int i = 1; i < coordinate.size(); i++) {
					int[] current = coordinate.get(i);
					gc.lineTo(current[0] * zoom, current[1] * zoom);
					gc.stroke();
				}
			}
		}
	}

	private void drawOverlay() {
		// countours
		// drawPath(paths);

		List<Path> convertedSvgPaths = svgPaths.stream().map(vektorPath -> new Path(
				vektorPath.stream().map(vektor -> new int[] { vektor.x, vektor.y }).collect(Collectors.toList()), false,
				false)).collect(Collectors.toList());
		drawPath(convertedSvgPaths);

		// ATTENTION: JavaFX throws an exception if zoom is too high

	}

}
