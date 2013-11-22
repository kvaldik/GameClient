package is.ru.tgra.network;

import is.ru.tgra.OtherPlayers;
import is.ru.tgra.World;
import java.io.Serializable;

/******************************************************************************
 * TcpPayload.java
 * 
 * This is a serializable class that stores all the initial information needed
 * when a new client connects, the map, all players nicks and so fourth.
 *****************************************************************************/


public class TcpPayloadInit implements Serializable {
	static final long serialVersionUID = -50077493051991107L;
	
	// Head of the TcpPayload
	public int typeOfPayload;	// Indicates what information is in the TcpPayload
	public int playerId;		// The id of the player that this TcpPayload belongs to
	public String message;		// To send text between the client and server (nick)
	
	// Initial part, all these variables are only used when the client first connects
	private float[] playersPosX;
	private float[] playersPosY;
	private float[] playersPosZ;
	private float[] playersDirX;
	private float[] playersDirY;
	private float[] playersDirZ;
	private int[] playersKills;
	private int[] playersDeaths;
	private String[] playersNicks;
	private byte[][][] map;
	
	// Constructor
	public TcpPayloadInit(int newTypeOfPayload) {
		this.playersPosX = null;
		this.playersPosY = null;
		this.playersPosZ = null;
		this.playersDirX = null;
		this.playersDirY = null;
		this.playersDirZ = null;
		this.playersKills = null;
		this.playersDeaths = null;
		this.playersNicks = null;
		this.map = null;
		this.typeOfPayload = newTypeOfPayload;
	}
	
	/*
	 * Get and set
	 */
	/* Server side
	public void setMap(World world, int mapSize) {
		this.map = new boolean[mapSize][mapSize][mapSize];
		world.getMap(this.map);
	}*/
	
	public void getMap(World world) {
		world.setMap(this.map);
	}
	/* Server side
	public void setPlayers(Players players) {
		int nrPlayers = players.getNrPlayers();
		this.playersPosX = new float[nrPlayers];
		this.playersPosY = new float[nrPlayers];
		this.playersPosZ = new float[nrPlayers];
		this.playersDirX = new float[nrPlayers];
		this.playersDirY = new float[nrPlayers];
		this.playersDirZ = new float[nrPlayers];
		this.playersNicks = new String[nrPlayers];
		this.playersKills = new int[nrPlayers];
		this.playersDeaths = new int[nrPlayers];
		players.getPlayers(this.playersPosX, this.playersPosY, this.playersPosZ, this.playersDirX, this.playersDirY, this.playersDirZ,
						   this.playersNicks, this.playersKills, this.playersDeaths);
	}*/
	
	public void getPlayers(OtherPlayers otherPlayers) {
		otherPlayers.setPlayers(this.playersPosX, this.playersPosY, this.playersPosZ, this.playersDirX, this.playersDirY, this.playersDirZ, this.playersNicks, this.playersKills, this.playersDeaths);
	}
}