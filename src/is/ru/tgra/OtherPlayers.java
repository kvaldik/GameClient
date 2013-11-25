package is.ru.tgra;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;

/******************************************************************************
 * OtherPlayers.java
 * 
 * Information on all other players connected to the server are stored here.
 * Also attributes of the current player are stored here, such as health, ammo
 * and so fourth.
 * This class is accessed from both the main thread and the networking thread
 * so there are read and locks on all the functions that access the data.
 *****************************************************************************/


public class OtherPlayers {
	// Parts of the game client
	private HUD hud;
	private Player player;
	private float mapScale;
	
	// Variables for other players
	private float[] playersPosX;
	private float[] playersPosY;
	private float[] playersPosZ;
	private float[] playersDirX;
	private float[] playersDirY;
	private float[] playersDirZ;
	private float[] playersColorR;
	private float[] playersColorG;
	private float[] playersColorB;
	private String[] playersNicks;
	private int[] playersKills;
	private int[] playersDeaths;
	private int nrPlayers;
	
	// Variables for this player
	private int playerId;
	private int health;
	private int ammo;
	private byte selectedBlock;
	private boolean isReloading;
	private long reloadingStartTime = 0;
	private long reloadingCurrentTime = 0;
	
	// Locks for threading
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	private final Lock fReadLock = fLock.readLock();
	private final Lock fWriteLock = fLock.writeLock();

	// Rendering variables
	private StillModel model;
	private FloatBuffer vertexBuffer;
	
	
	// Constructor
	public OtherPlayers(int newNrPlayers, float newMapScale, FloatBuffer newVertexBuffer) {
		this.vertexBuffer = newVertexBuffer;
		this.mapScale = newMapScale;
		
		this.playersPosX = new float[newNrPlayers];
		this.playersPosY = new float[newNrPlayers];
		this.playersPosZ = new float[newNrPlayers];
		this.playersDirX = new float[newNrPlayers];
		this.playersDirY = new float[newNrPlayers];
		this.playersDirZ = new float[newNrPlayers];
		this.playersColorR = new float[newNrPlayers];
		this.playersColorG = new float[newNrPlayers];
		this.playersColorB = new float[newNrPlayers];
		this.playersNicks = new String[newNrPlayers];
		this.playersKills = new int[newNrPlayers];
		this.playersDeaths = new int[newNrPlayers];
		this.nrPlayers = newNrPlayers;
		
		this.health = 100;
		this.ammo = 5;
		this.selectedBlock = 1;
		this.isReloading = false;
		
		ObjLoader loader = new ObjLoader();
		model = loader.loadObj(Gdx.files.internal("assets/model/character.obj"), true);
		
		// Set the players color
		for (int i = 0; i < newNrPlayers; i++) {
			this.playersColorR[i] = (float)Math.random();
			this.playersColorG[i] = (float)Math.random();
			this.playersColorB[i] = (float)Math.random();
		}

		// Player 0 is always red
		this.playersColorR[0] = 1.0f;
		this.playersColorG[0] = 0.0f;
		this.playersColorB[0] = 0.0f;
		// Player 1 is always green
		this.playersColorR[1] = 0.0f;
		this.playersColorG[1] = 1.0f;
		this.playersColorB[1] = 0.0f;
		// Player 2 is always blue
		this.playersColorR[2] = 0.0f;
		this.playersColorG[2] = 0.0f;
		this.playersColorB[2] = 1.0f;
	}
	
	// Add a new player
	public void newPlayer(int newPlayerId, String newNickname) {
		this.fWriteLock.lock();
		try {
			this.playersNicks[newPlayerId] = newNickname;
			System.out.printf("%s joined the game \n", newNickname);
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Remove a player
	public void removePlayer(int playerId) {
		this.fWriteLock.lock();
		try {
			this.playersPosX[playerId] = 0;
			this.playersPosY[playerId] = 0;
			this.playersPosZ[playerId] = 0;
			this.playersDirX[playerId] = 0;
			this.playersDirY[playerId] = 0;
			this.playersDirZ[playerId] = 0;
			System.out.printf("%s left the game \n", this.playersNicks[playerId]);
			this.playersNicks[playerId] = null;
			this.playersKills[playerId] = 0;
			this.playersDeaths[playerId] = 0;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Update the location of a player
	public void UpdatePlayer(int playerId, float posX, float posY, float posZ, float dirX, float dirY, float dirZ) {
		this.fWriteLock.lock();
		try {
			this.playersPosX[playerId] = posX;
			this.playersPosY[playerId] = posY;
			this.playersPosZ[playerId] = posZ;
			this.playersDirX[playerId] = dirX;
			this.playersDirY[playerId] = dirY;
			this.playersDirZ[playerId] = dirZ;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Set the current players id
	public void setPlayerId(int newPlayerId) {
		this.fWriteLock.lock();
		try {
			this.playerId = newPlayerId;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Draw all other players
	public void drawPlayers() {
		this.fReadLock.lock();
		try {
			Gdx.gl11.glDisable(GL11.GL_LIGHT1);
			for (int i = 0; i < this.nrPlayers; i++) {
				if (this.playersPosY[i] != 0) {
					if (i != this.playerId) {
						Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, new float[] {this.playersColorR[i], this.playersColorG[i], this.playersColorB[i], 1.0f}, 0);
						Gdx.gl11.glPushMatrix();
						Gdx.gl11.glTranslatef(this.playersPosX[i], this.playersPosY[i]-6f, this.playersPosZ[i]);
						Gdx.gl11.glRotatef(this.angleOfPlayer(i)-90, 0, 1, 0);
						Gdx.gl11.glScalef(this.mapScale, this.mapScale, this.mapScale);
						model.render();
						Gdx.gl11.glPopMatrix();
						// We need to load the vertex buffer again!
						Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
					}
				}
			}
			Gdx.gl11.glEnable(GL11.GL_LIGHT1);
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	private float angleOfPlayer(int playerId) {
		float x = this.playersDirX[playerId];
		float z = this.playersDirZ[playerId];
		float returnValue = (float)Math.toDegrees(Math.atan(x/z));
		if (z == 0)
			z = 0.00000000000001f;
		if (z < 0)
			returnValue += 180.0f;
		return returnValue;
	}
	
	// Update the status of other players (reload)
	public void update() {
		this.fWriteLock.lock();
		try {
			this.reloadingCurrentTime = System.currentTimeMillis();
			if (this.isReloading && this.reloadingStartTime+3000 < this.reloadingCurrentTime) {
				this.isReloading = false;
				this.ammo = 5;
			}
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// The gun has been shot!
	public void incSelectedBlock() {
		this.fWriteLock.lock();
		try {
			if (this.selectedBlock < 7)
				this.selectedBlock++;
		} finally {
			this.fWriteLock.unlock();
		}
		
	}

	// The gun has been shot!
	public void decSelectedBlock() {
		this.fWriteLock.lock();
		try {
			if (this.selectedBlock > 1)
				this.selectedBlock--;
		} finally {
			this.fWriteLock.unlock();
		}
		
	}
	
	// The gun has been shot!
	public void shoot() {
		this.fWriteLock.lock();
		try {
			if (this.ammo > 0)
				this.ammo -= 1;
		} finally {
			this.fWriteLock.unlock();
		}
		
	}
	
	// The is being reloaded
	public void reload() {
		this.fWriteLock.lock();
		try {
			this.isReloading = true;
			this.reloadingStartTime = System.currentTimeMillis();
		} finally {
			this.fWriteLock.unlock();
		}
		
	}
	
	// Add a kill score
	public void addKill(int playerId) {
		this.fWriteLock.lock();
		try {
			this.playersKills[playerId]++;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Add a death score
	public void addDeath(int playerId) {
		this.fWriteLock.lock();
		try {
			this.playersDeaths[playerId]++;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// The current player takes a hit
	public void takeAHit(int hitPoints) {
		this.fWriteLock.lock();
		try {
			this.hud.setHurt();
			this.health -= hitPoints;
			if (this.health <= 0) {
				this.health = 100;
				this.ammo = 5;
				this.hud.setDead();
				this.player.die();
			}
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	/*
	 * Get and set
	 */
	public void setPlayers(float[] newPlayersPosX, float[] newPlayersPosY, float[] newPlayersPosZ, float[] newPlayersDirX, float[] newPlayersDirY, float[] newPlayersDirZ,
						   String[] newPlayersNicks, int[] newPlayersKills, int[] newPlayersDeaths) {
		this.fWriteLock.lock();
		try {
			for (int i = 0; i < this.nrPlayers; i++) {
				this.playersPosX[i] = newPlayersPosX[i];
				this.playersPosY[i] = newPlayersPosY[i];
				this.playersPosZ[i] = newPlayersPosZ[i];
				this.playersDirX[i] = newPlayersDirX[i];
				this.playersDirY[i] = newPlayersDirY[i];
				this.playersDirZ[i] = newPlayersDirZ[i];
				this.playersNicks[i] = newPlayersNicks[i];
				this.playersKills[i] = newPlayersKills[i];
				this.playersDeaths[i] = newPlayersDeaths[i];
			}
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	// Returns true if the player is reloading or if he has no ammo
	public boolean getReloading() {
		this.fReadLock.lock();
		try {
			return (this.isReloading || this.ammo == 0);
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public int getNrPlayers() {
		this.fReadLock.lock();
		try {
			return this.nrPlayers;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public String getPlayersNick(int playerId) {
		this.fReadLock.lock();
		try {
			return this.playersNicks[playerId];
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public int getPlayersKills(int playerId) {
		this.fReadLock.lock();
		try {
			return this.playersKills[playerId];
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public int getPlayersDeaths(int playerId) {
		this.fReadLock.lock();
		try {
			return this.playersDeaths[playerId];
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public void setVertexBuffer(FloatBuffer newVertexBuffer) {
		this.fWriteLock.lock();
		try {
			this.vertexBuffer = newVertexBuffer;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public void setHud(HUD newHud) {
		this.fWriteLock.lock();
		try {
			this.hud = newHud;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public void setPlayer(Player newPlayer) {
		this.fWriteLock.lock();
		try {
			this.player = newPlayer;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public int getPlayerHealth() {
		this.fReadLock.lock();
		try {
			return this.health;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public int getPlayerAmmo() {
		this.fReadLock.lock();
		try {
			return this.ammo;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public byte getSelectedBlock() {
		this.fReadLock.lock();
		try {
			return this.selectedBlock;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public String getCurrentPlayerNick() {
		this.fReadLock.lock();
		try {
			return this.playersNicks[this.playerId];
		} finally {
			this.fReadLock.unlock();
		}
	}
}