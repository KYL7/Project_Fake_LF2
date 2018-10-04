package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Duration;

public class Lose {
	ImageView imv;
	Image imageArray[] = new Image[5];
	Timeline timeline;
	private StackPane sp;
	int frame = 1;
	int count = 0;
	private Label name;

	public Lose(StackPane sp, String winname) {
		this.sp = sp;
		for (int i = 0; i < frame; i++) {
			imageArray[i] = new Image("lose/lose.png");
		}
		timeline = new Timeline();
		setGraph(sp, winname);
		fire();
	}

	public void fire() {
		timeline.setCycleCount(9);
		// Duration每次多少秒,然後觸發事件
		final KeyFrame kf = new KeyFrame(Duration.millis(200), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				// 偵測collision
				imv.setImage(imageArray[count]);
				count++;
				if (count > frame) {
					count = 0;
				}
			}
		});
		timeline.getKeyFrames().add(kf);
		timeline.play();
		timeline.setAutoReverse(true);
	}

	public void setGraph(StackPane sp, String winname) {
		imv = new ImageView();

		name = new Label("贏家: "+winname);
		Platform.runLater(() -> {
			imv.setTranslateY(0);
			imv.setTranslateX(0);

			name.setTranslateX(0);
			name.setTranslateY(-240);
			name.setPrefSize(150, 40);
			name.setStyle("-fx-background-color: #000000;");
			name.setTextFill(Color.WHITE);
			name.setFont(Font.font("Arial", FontPosture.ITALIC, 30));
			name.setAlignment(Pos.CENTER);

			sp.getChildren().add(name);
			sp.getChildren().add(imv);
		});

	}
}
