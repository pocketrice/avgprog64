package io.github.pocketrice;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.scenes.scene2d.Stage;
import io.github.pocketrice.App;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(120);
		config.setTitle("average programmer 64");
		config.setHdpiMode(HdpiMode.Pixels);
		config.setWindowedMode(960, 680);
		config.setResizable(false);
		new Lwjgl3Application(new App(), config);

	}
}
