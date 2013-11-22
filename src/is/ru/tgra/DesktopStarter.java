package is.ru.tgra;

import java.awt.Dimension;
import java.awt.Toolkit;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/******************************************************************************
 * DesktopStarter.java
 * 
 * This class simply starts up the game client
 *****************************************************************************/


public class DesktopStarter {
	public static void main(String[] args) {
		// Getting the current desktop screen resolution.
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrnsize = toolkit.getScreenSize();

		// Create Lwjg configuration. This is an alternative way for configuring
		// our applications
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		// Set the screen size to same as the desktop screen size.
		cfg.width = scrnsize.width/4*3;
		cfg.height = scrnsize.height/4*3;
		cfg.useGL20 = false;

		// Set OpenGL to game mode (full screen.)
		cfg.fullscreen = false;
		
		// Enable vSync
		cfg.vSyncEnabled = true;

		new LwjglApplication(new GameClient(), cfg);
	}
}
