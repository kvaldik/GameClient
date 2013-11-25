package is.ru.tgra;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/******************************************************************************
 * HUD.java
 * 
 * This class draws information about the game to the player as a Heads Up
 * Display. The HUD also draws the screen red when a player is hurt or dead.
 *****************************************************************************/


public class HUD {
	// Parts of the game client
	private OtherPlayers otherPlayers;

	// HUD variables
	private boolean isHurt;
	private long hurtStartTime = 0;
	private long hurtCurrentTime = 0;
	private boolean isDead;
	private long deadStartTime = 0;
	private long deadCurrentTime = 0;
	private boolean drawScoreBoard;
	private long startTime = 0;
	private long currentTime = 0;
	public boolean gunOrBox;
	
	// Chat and status text
	public boolean isChating;
	public String chatMessage;
	public List<StatusText> statusTexts;
	
	// Rendering variables
	private SpriteBatch spriteBatch; 
	private BitmapFont font;
	private OrthographicCamera secondCamera;
	private Texture crossHair;
	private Texture gun;
	private Texture box;
	private Texture hurt;
	private Texture dead;
	
	
	// Constructor
	public HUD(OtherPlayers newOtherPlayers) {
		// New camera for the HUD
		this.secondCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.spriteBatch = new SpriteBatch();
		this.font = new BitmapFont();
		this.crossHair = new Texture(Gdx.files.internal("assets/hud/crosshair.png"));
		this.gun = new Texture(Gdx.files.internal("assets/hud/gun.png"));
		this.box = new Texture(Gdx.files.internal("assets/hud/box.png"));
		this.otherPlayers = newOtherPlayers;
		this.gunOrBox = true;
		
		// Dead and hurt textures
		Pixmap pixmap = new Pixmap(2048, 2048, Format.RGBA8888);
		pixmap.setColor(1.0f, 0.0f, 0.0f, 0.5f);
		pixmap.fillRectangle(0, 0, 2048, 2048);
		this.hurt = new Texture(pixmap);
		pixmap.setColor(1.0f, 0.0f, 0.0f, 1.0f);
		pixmap.fillRectangle(0, 0, 2048, 2048);
		this.dead = new Texture(pixmap);
		pixmap.dispose();
		
		// Chat
		this.isChating = false;
		this.chatMessage = "";
		this.statusTexts = new LinkedList<StatusText>();
	}
	
	// Draw the HUD
	public void drawHud() {
		this.spriteBatch.setProjectionMatrix(this.secondCamera.combined);
		this.secondCamera.update();
		Gdx.gl11.glDisable(GL11.GL_LIGHTING);
		
		this.spriteBatch.begin();
		
		// Draw the cross hair
		this.spriteBatch.draw(crossHair, -64, -64);
		
		// Draw the gun or box
		if (this.gunOrBox) {
			this.spriteBatch.draw(box, Gdx.graphics.getWidth()/2-133, -Gdx.graphics.getHeight()/2+10);
			this.font.setColor(1f,0f,0f, 1f);
			this.font.setScale(3.0f);
			this.font.draw(this.spriteBatch, String.format("%d", this.otherPlayers.getSelectedBlock()), Gdx.graphics.getWidth()/2-125, -Gdx.graphics.getHeight()/2+75);
		}
		else
			this.spriteBatch.draw(gun, Gdx.graphics.getWidth()/2-133, -Gdx.graphics.getHeight()/2+10);
		
		// Draw the HUD
		String healthText = String.format("HEALTH: %d", this.otherPlayers.getPlayerHealth());
		String ammoText = String.format("AMMO: %d", this.otherPlayers.getPlayerAmmo());
		this.font.setColor(1f,0f,0f, 1f);
		this.font.setScale(3.0f);
		this.font.draw(this.spriteBatch, healthText, -Gdx.graphics.getWidth()/2+5, -Gdx.graphics.getHeight()/2+50);
		this.font.draw(this.spriteBatch, ammoText, -Gdx.graphics.getWidth()/2+5, -Gdx.graphics.getHeight()/2+100);
		
		// Draw the score board
		if (this.drawScoreBoard) {
			this.currentTime = System.currentTimeMillis();
			if (this.currentTime > this.startTime+4000)
				this.drawScoreBoard = false;
			this.font.setColor(1f,0f,0f, 1f);
			this.font.setScale(1.5f);
			this.font.draw(this.spriteBatch, "PLAYER", -200, 1080/4*3/2-150);
			this.font.draw(this.spriteBatch, "KILLS", 50, 1080/4*3/2-150);
			this.font.draw(this.spriteBatch, "DEATHS", 150, 1080/4*3/2-150);
			String nick;
			for (int i = 0; i < this.otherPlayers.getNrPlayers(); i++) {
				nick = this.otherPlayers.getPlayersNick(i);
				if (nick == null)
					nick = " ";
				this.font.draw(this.spriteBatch, String.format("%s", nick), -200, 1080/4*3/2-175-25*i);
				this.font.draw(this.spriteBatch, String.format("%d", this.otherPlayers.getPlayersKills(i)), 50, 1080/4*3/2-175-25*i);
				this.font.draw(this.spriteBatch, String.format("%d", this.otherPlayers.getPlayersDeaths(i)), 150, 1080/4*3/2-175-25*i);
			}
		}
		
		// Draw hurt and dead textures
		if (this.isHurt) {
			this.spriteBatch.draw(hurt, -1024, -1024);
			this.hurtCurrentTime = System.currentTimeMillis();
			if (this.hurtStartTime+100 < this.hurtCurrentTime) {
				this.isHurt = false;
			}
		}
		if (this.isDead) {
			this.spriteBatch.draw(dead, -1024, -1024);
			this.deadCurrentTime = System.currentTimeMillis();
			if (this.deadStartTime+1000 < this.deadCurrentTime) {
				this.isDead = false;
			}
		}
		
		// Chat
		this.font.setColor(1f,0f,0f, 1f);
		this.font.setScale(1.5f);
		
		if (this.isChating)
			this.font.draw(this.spriteBatch, String.format("Say: %s", this.chatMessage), -64, -64);

		for (int i = 0; i < this.statusTexts.size(); i++) {
			this.font.draw(this.spriteBatch, this.statusTexts.get(i).message, -Gdx.graphics.getWidth()/2+5, -25*i);
			this.currentTime = System.currentTimeMillis();
			if (this.statusTexts.get(i).timeInserted + 5000 < this.currentTime)
				this.statusTexts.remove(i);
		}
		
		this.spriteBatch.end();
	}
	
	/*
	 * Get and set	
	 */
	public void addStatusText(String message) {
		StatusText newStatusText = new StatusText(message, System.currentTimeMillis());
		this.statusTexts.add(newStatusText);
	}
	
	public void setHurt() {
		this.isHurt = true;
		this.hurtStartTime = System.currentTimeMillis();
	}
	
	public void setDead() {
		this.isDead = true;
		this.deadStartTime = System.currentTimeMillis();
	}
	
	public void drawScoreBoard() {
		this.startTime = System.currentTimeMillis();
		this.drawScoreBoard = true;
	}
	
	public void changeGunOrBox() {
		this.gunOrBox = !this.gunOrBox;
	}
	
	public void updateCamera() {
		this.secondCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
}
