package code;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.Webcam;

import code.Recognizer.Results;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import org.opencv.core.Core; 
import org.opencv.core.Mat; 
import org.opencv.core.MatOfRect; 
import org.opencv.core.Point; 
import org.opencv.core.Rect; 
import org.opencv.core.Scalar; 
import org.opencv.imgcodecs.Imgcodecs; 
import org.opencv.imgproc.Imgproc; 
import org.opencv.objdetect.CascadeClassifier;


/**
 * This example demonstrates how to use Webcam Capture API in a JavaFX application.
 * 
 * @author Rakesh Bhatt (rakeshbhatt10)
 */
public class WebCamAppLauncher extends Application {

	private class WebCamInfo {

		private String webCamName;
		private int webCamIndex;

		public String getWebCamName() {
			return webCamName;
		}

		public void setWebCamName(String webCamName) {
			this.webCamName = webCamName;
		}

		public int getWebCamIndex() {
			return webCamIndex;
		}

		public void setWebCamIndex(int webCamIndex) {
			this.webCamIndex = webCamIndex;
		}

		@Override
		public String toString() {
			return webCamName;
		}
	}

	private FlowPane bottomCameraControlPane;
	private FlowPane topPane;
	private BorderPane root;
	private String cameraListPromptText = "Choose Camera";
	private ImageView imgWebCamCapturedImage;
	private Webcam webCam = null;
	private boolean stopCamera = false;
	private BufferedImage grabbedImage;
	private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
	private BorderPane webCamPane;
	private Button btnCamreaStop;
	private Button btnCamreaStart;
	private Button btnCameraDispose;
	private Button btnCameraDetect;
	
	private List<MatOfRect> faces = null;
	private CascadeClassifier faceDetector = new CascadeClassifier(); 
	public Boolean recognizing = false;
	public Boolean detectEthn = false;
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setTitle("Connecting Camera Device Using Webcam Capture API");

		root = new BorderPane();
		topPane = new FlowPane();
		topPane.setAlignment(Pos.CENTER);
		topPane.setHgap(20);
		topPane.setOrientation(Orientation.HORIZONTAL);
		topPane.setPrefHeight(40);
		root.setTop(topPane);
		webCamPane = new BorderPane();
		webCamPane.setStyle("-fx-background-color: #ccc;");
		imgWebCamCapturedImage = new ImageView();
		webCamPane.setCenter(imgWebCamCapturedImage);
		root.setCenter(webCamPane);
		createTopPanel();
		bottomCameraControlPane = new FlowPane();
		bottomCameraControlPane.setOrientation(Orientation.HORIZONTAL);
		bottomCameraControlPane.setAlignment(Pos.CENTER);
		bottomCameraControlPane.setHgap(20);
		bottomCameraControlPane.setVgap(10);
		bottomCameraControlPane.setPrefHeight(40);
		bottomCameraControlPane.setDisable(true);
		createCameraControls();
		root.setBottom(bottomCameraControlPane);

		primaryStage.setScene(new Scene(root));
		primaryStage.setHeight(700);
		primaryStage.setWidth(600);
		primaryStage.centerOnScreen();
		primaryStage.show();

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				setImageViewSize();
			}
		});

	}

	protected void setImageViewSize() {

		double height = webCamPane.getHeight();
		double width = webCamPane.getWidth();

		imgWebCamCapturedImage.setFitHeight(height);
		imgWebCamCapturedImage.setFitWidth(width);
		imgWebCamCapturedImage.prefHeight(height);
		imgWebCamCapturedImage.prefWidth(width);
		imgWebCamCapturedImage.setPreserveRatio(true);

	}

	private void createTopPanel() {

		int webCamCounter = 0;
		Label lbInfoLabel = new Label("Select Your WebCam Camera");
		ObservableList<WebCamInfo> options = FXCollections.observableArrayList();

		topPane.getChildren().add(lbInfoLabel);

		for (Webcam webcam : Webcam.getWebcams()) {
			WebCamInfo webCamInfo = new WebCamInfo();
			webCamInfo.setWebCamIndex(webCamCounter);
			webCamInfo.setWebCamName(webcam.getName());
			options.add(webCamInfo);
			webCamCounter++;
		}

		ComboBox<WebCamInfo> cameraOptions = new ComboBox<WebCamInfo>();
		cameraOptions.setItems(options);
		cameraOptions.setPromptText(cameraListPromptText);
		cameraOptions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<WebCamInfo>() {

			@Override
			public void changed(ObservableValue<? extends WebCamInfo> arg0, WebCamInfo arg1, WebCamInfo arg2) {
				if (arg2 != null) {
					System.out.println("WebCam Index: " + arg2.getWebCamIndex() + ": WebCam Name:" + arg2.getWebCamName());
					initializeWebCam(arg2.getWebCamIndex());
				}
			}
		});
		topPane.getChildren().add(cameraOptions);
	}

	protected void initializeWebCam(final int webCamIndex) {

		Task<Void> webCamTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				if (webCam != null) {
					disposeWebCamCamera();
				}

				webCam = Webcam.getWebcams().get(webCamIndex);
				webCam.open();
				//Load Face detector
		        faceDetector.load("haarcascade_frontalface_alt.xml"); 

				startWebCamStream();
				
				

				return null;
			}
		};

		Thread webCamThread = new Thread(webCamTask);
		webCamThread.setDaemon(true);
		webCamThread.start();

		bottomCameraControlPane.setDisable(false);
		btnCamreaStart.setDisable(true);
	}

	protected void startWebCamStream() {

		stopCamera = false;

		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				final AtomicReference<WritableImage> ref = new AtomicReference<>();
				BufferedImage img = null;

				while (!stopCamera) {
					try {
						if ((img = webCam.getImage()) != null && recognizing == false) {

							
							Mat matImg = ImageUtils.bufferedImageToMat(img);

					        // Detecting faces 
					        MatOfRect faceDetections = new MatOfRect(); 
					        faceDetector.detectMultiScale(matImg, faceDetections); 
					        
					        // Creating a rectangular box showing faces detected 
					        if(!faceDetections.empty()) {
					        	if(!detectEthn) {
						        	for (Rect rect : faceDetections.toArray()) 
							        { 
							            Imgproc.rectangle(matImg, new Point(rect.x, rect.y), 
							             new Point(rect.x + rect.width, rect.y + rect.height), 
							                                           new Scalar(0, 255, 0)); 
							        }
					        	} else {
					        		// Call recognizing
					        		recognizing = true;
					        		Recognizer r = new Recognizer(matImg);
					        		stopWebCamCamera();
					        		Results res = r.getRequest();
					        		matImg = ImageUtils.addResults(res, ref);
					        	}
					        } else {
					        	Imgproc.putText(matImg, "Cannot dectect faces!", new Point(0, matImg.height()/2 ), Font.PLAIN, 1.0, new Scalar(255,0,0));
					        }
					        
					        // Send processed Img to viewer
					        ref.set(SwingFXUtils.toFXImage(ImageUtils.matToBufferedImage(matImg), ref.get()));
							img.flush();

							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									imageProperty.set(ref.get());
								}
							});
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				return null;
			}
		};

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		imgWebCamCapturedImage.imageProperty().bind(imageProperty);

	}

	private void createCameraControls() {

		btnCamreaStop = new Button();
		btnCamreaStop.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				stopWebCamCamera();
			}
		});
		btnCamreaStop.setText("Stop Camera");
		btnCamreaStart = new Button();
		btnCamreaStart.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				startWebCamCamera();
			}
		});
		btnCamreaStart.setText("Start Camera");
		btnCameraDispose = new Button();
		btnCameraDispose.setText("Dispose Camera");
		btnCameraDispose.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				disposeWebCamCamera();
			}
		});
		
		btnCameraDetect = new Button();
		btnCameraDetect.setText("Detect Person");
		btnCameraDetect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				detectPerson();
			}
		});
		
		bottomCameraControlPane.getChildren().add(btnCamreaStart);
		bottomCameraControlPane.getChildren().add(btnCamreaStop);
		bottomCameraControlPane.getChildren().add(btnCameraDispose);
		bottomCameraControlPane.getChildren().add(btnCameraDetect);
	}

	protected void detectPerson() {
		detectEthn = true;
		System.out.println("Detecting...");
	}
	
	protected void disposeWebCamCamera() {
		stopCamera = true;
		webCam.close();
		btnCamreaStart.setDisable(true);
		btnCamreaStop.setDisable(true);
	}

	protected void startWebCamCamera() {
		stopCamera = false;
		recognizing = false;
		detectEthn = false;
		startWebCamStream();
		btnCamreaStop.setDisable(false);
		btnCamreaStart.setDisable(true);
	}

	protected void stopWebCamCamera() {
		stopCamera = true;
		recognizing = false;
		btnCamreaStart.setDisable(false);
		btnCamreaStop.setDisable(true);
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
		launch(args);
	}
}