package is.ru.tgra;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/******************************************************************************
 * Sounds.java
 * 
 * This class contains all the sounds of the game and functions to start and
 * stop playing them.
 *****************************************************************************/


public class Sounds {
	private Sound gunShot;
	private Sound gunEmpty;
	private Sound reload;
	
	
	// Constructor
	public Sounds() {
		this.gunShot = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/gunShot.wav"));
		this.gunEmpty = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/gunEmpty.wav"));
		this.reload = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/reload.wav"));
	}
	
	public void playGunShot() {
		this.gunShot.play();
	}
	
	public void playGunEmpty() {
		this.gunEmpty.play();
	}
	
	public void playReload() {
		this.reload.play();
	}
}
