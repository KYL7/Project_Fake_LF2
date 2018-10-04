package application;

import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Firework {
	ImageView imv;
	Image imageArray[] = new Image[35];
	Timeline timeline;
	private StackPane sp;
	int frame = 32;
	int count = 0;
	private Label name;

	public Firework(StackPane sp, String winname) {
		this.sp = sp;
		for (int i = 0; i < frame; i++) {
			imageArray[i] = new Image("firework/firework" + Integer.toString(i + 1) + ".png");
		}
		timeline = new Timeline();
		setGraph(sp, winname);
		fire();
	}

	public void fire() {
		timeline.setCycleCount(32);
		// Duration每次多少秒,然後觸發事件
		final KeyFrame kf = new KeyFrame(Duration.millis(20), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// 偵測collision
				imv.setImage(imageArray[count]);
				count++;
			}
		});
		timeline.getKeyFrames().add(kf);
		timeline.play();
		timeline.setAutoReverse(true);
	}

	public void setGraph(StackPane sp, String winname) {
		imv = new ImageView();

		name = new Label(winname);
		Platform.runLater(() -> {
			imv.setTranslateY(0);
			imv.setTranslateX(0);
			sp.getChildren().add(imv);
			name.setTranslateX(0);
			name.setTranslateY(0);
			name.setPrefSize(100, 80);
			sp.getChildren().add(name);
		});

	}
}
