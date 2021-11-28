// IP WS2021/22 Zoom Template
//
// Copyright (C) 2021 by Klaus Jung
// All rights reserved.
// Date: 2021-11-12

package ip_ws2122;
	
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane root = (BorderPane)loadFXML("ZoomTemplateView.fxml");
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Zoom Template - <Your name>"); // TODO: add your name(s)
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private Object loadFXML(String resourceName) throws Exception {
		String path = System.getProperty("user.home") + File.separator + "src" + File.separator + "Java" + File.separator + "PixelatedImageView.jar";
		try {
			URL url = new File(path).toURI().toURL();
			URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
			Method addMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addMethod.setAccessible(true);
			addMethod.invoke(classLoader, url);
			Method method = classLoader.loadClass("de.htw.lcs.fx.FXMLLoaderEx").getMethod("load", InputStream.class);
			Object object = method.invoke(null, getClass().getResourceAsStream(resourceName));
			return object;
		} catch (Exception e) {
			System.out.println("Cannot load methods from " + path);
    		return FXMLLoader.load(getClass().getResource(resourceName));
	    }		
	}
}
