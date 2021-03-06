// IP Ue1 WS 2021/22 Vorgabe
//
// Copyright (C) 2020 by Klaus Jung
// All rights reserved.
// Date: 2020-10-02

package ip_ws2122;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class BinarizeViewController {

	public enum MethodeType {
		//COPY("Copy Image"), THRESHOLD("Threshold"), ISODATA("ISO Data"), FLOODFILL_DFS("Floodfill DFS"), FLOODFILL_BFS("Floodfill BFS"), FLOODFILL_SL("Floodfill SL");
		COPY("Copy Image"), THRESHOLD("Threshold"), ISODATA("ISO Data"), CONTOUR("Contour");
		private final String name;

		private MethodeType(String s) {
			name = s;
		}

		public String toString() {
			return this.name;
		}
	};

	private static final String initialFileName = "tools1.png";
	private static File fileOpenPath = new File(".");

	@FXML
	private ImageView originalImageView;

	@FXML
	private ImageView binarizedImageView;

	@FXML
	private ComboBox<MethodeType> methodeSelection;

	@FXML
	private CheckBox outline;

	// TODO: add a Slider for threshold adjustment, add it to the GIU as well

	private int threshold;

	@FXML
	private Slider slider;

	@FXML
	private Label messageLabel;

	@FXML
	public void initialize() {
		// set combo boxes items
		methodeSelection.getItems().addAll(MethodeType.values());
		methodeSelection.setValue(MethodeType.COPY);

		// initialize parameters
		methodeChanged();

		// load and process default image
		new RasterImage(new File(initialFileName)).setToView(originalImageView);
		processImage();

		slider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				// new slider value in new_val.doubleValue()
				threshold = (int) new_val.doubleValue();
				processImage();
			}
		});
	}

//	@FXML
//	public void sliderChanged() {
//		this.threshold = (int) slider.getValue();
//    	processImage();
//	}
//	
	@FXML
	void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			new RasterImage(selectedFile).setToView(originalImageView);
			processImage();
			messageLabel.getScene().getWindow().sizeToScene();
		}
	}

	@FXML
	void methodeChanged() {
		outline.setDisable(methodeSelection.getValue() == MethodeType.COPY);
		processImage();
	}

	@FXML
	void outlineChanged() {
		processImage();

	}

	private void processImage() {
		if (originalImageView.getImage() == null)
			return; // no image: nothing to do

		long startTime = System.currentTimeMillis();

		RasterImage origImg = new RasterImage(originalImageView);
		RasterImage binImg = new RasterImage(origImg); // create a clone of origImg

		switch (methodeSelection.getValue()) {
		case THRESHOLD:
			binImg.binarizeWithThreshold(this.threshold);
			break;
		case ISODATA:	
			threshold = binImg.binarizeWithIsoData();
			break;
		case CONTOUR:
			binImg = new RasterImage(origImg);
			binImg.findContour();
			binImg.setPathDatatype();
			break;
//		case FLOODFILL_DFS:
//			binImg = new RasterImage(origImg);
//			binImg.floodFill("DFS");
//			break;
//		case FLOODFILL_SL:
//			binImg = new RasterImage(origImg);
//			binImg.sequentialLabeling2();
//			break;
		default:
			break;
		}

		if (outline.isSelected() && methodeSelection.getValue() != MethodeType.COPY) {
			RasterImage outlineImg = new RasterImage(binImg.width, binImg.height);
			Filter.outline(binImg, outlineImg);
			outlineImg.setToView(binarizedImageView);

			
			
		} else {
			binImg.setToView(binarizedImageView);
		}

		messageLabel.setText(
				"Processing time: " + (System.currentTimeMillis() - startTime) + " ms, threshold = " + threshold);
	}

}
