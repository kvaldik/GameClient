package is.ru.tgra.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import is.ru.tgra.World;
import is.ru.tgra.OtherPlayers;

/******************************************************************************
 * TcpClient.java
 * 
 * This is a network thread that communicates with the server. It updates
 * changes on the world and other players, the current player can of course
 * also send changes, such as when he moves or removes or adds a block.
 *****************************************************************************/


public class TcpClient extends Thread {
	// Parts of the game client
	private World world;
	private OtherPlayers otherPlayers;

	// TcpClient tools
	private Socket socket;
	private OutputStream oStream;
	private ObjectOutputStream ooStream;
	private InputStream iStream;
	private ObjectInputStream oiStream;
	
	// TcpClient variables
	private int playerId;
	
	// Constructor
	public TcpClient(String hostname, int port, World newWorld, OtherPlayers newOtherPlayers) {
		// Get game client parts
		this.world = newWorld;
		this.otherPlayers = newOtherPlayers;
		// Set the player id to -1 since it hasn't been allocated by the server
		this.playerId = -1;
		
		// Set up the TcpClient
		try {
			this.socket = new Socket(hostname, port);
			
			this.oStream = this.socket.getOutputStream();
			this.ooStream = new ObjectOutputStream(oStream);
			
			this.iStream = this.socket.getInputStream();
			this.oiStream = new ObjectInputStream(iStream);
		}
		catch (UnknownHostException e) {
			System.out.println("Don't know about host: " + hostname);
			e.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Couldn't get I/O for the connection to: " +
				hostname + ":" + port);
			e.printStackTrace();
		}
	}
	
	// The run function, reads changes from the server
	public void run() {
		TcpPayload payload;
		
		while (true) {
			try {
		    	payload = new TcpPayload(0);
				payload = (TcpPayload) oiStream.readObject();
				switch (payload.typeOfPayload) {
				case 10: // Player connected
					this.otherPlayers.newPlayer(payload.playerId, payload.message);
					break;
				case 20: // Player disconnected
					this.otherPlayers.removePlayer(payload.playerId);
					break;
				case 30: // Player position update
					this.otherPlayers.UpdatePlayer(payload.playerId, payload.playerPosX, payload.playerPosY, payload.playerPosZ,
												   payload.playerDirX, payload.playerDirY, payload.playerDirZ);
					break;
				case 40: // Map update (changed block)
					this.world.changeBlock(payload.mapX, payload.mapY, payload.mapZ, payload.mapValue);
					break;
				case 60: // Someone got hit (might be this player)
					// Check if it was the current player that got hit, else do nothing
					if (payload.playerId2 == this.playerId) {
						this.otherPlayers.takeAHit(25);
					}
					break;
				case 70: // Someone got a fatal hit (might be this player)
					// Check if it was the current player that got hit, else do nothing
					if (payload.playerId2 == this.playerId) {
						this.otherPlayers.takeAHit(25);
					}
					this.otherPlayers.addKill(payload.playerId);
					this.otherPlayers.addDeath(payload.playerId2);
					break;
				default:
					break;
				}
			}
			catch (ClassNotFoundException | IOException e) {
				System.out.printf("Error in TcpClient, run() \n");
				e.printStackTrace();
			}
		}
	}
	
	// The updateMap functions updates a block in the map
	public void updateMap(int x, int y, int z, byte newValue) {
		TcpPayload payload = new TcpPayload(40);
		payload.mapX = x;
		payload.mapY = y;
		payload.mapZ = z;
		payload.mapValue = newValue;
		try {
	    	ooStream.writeObject(payload);
		}
		catch (IOException e) {
			System.out.printf("Error in TcpClient, updateMap() \n");
			e.printStackTrace();
		}
	}
	
	// The updatePlayer functions updates the GameClients position
	public void updatePlayer(float posX, float posY, float posZ, float dirX, float dirY, float dirZ) {
    	TcpPayload payload = new TcpPayload(30);
    	payload.playerId = this.playerId;
    	payload.playerPosX = posX;
    	payload.playerPosY = posY;
    	payload.playerPosZ = posZ;
    	payload.playerDirX = dirX;
    	payload.playerDirY = dirY;
    	payload.playerDirZ = dirZ;
		try {
	    	ooStream.writeObject(payload);
		}
		catch (IOException e) {
			System.out.printf("Error in TcpClient, update() \n");
			e.printStackTrace();
		}
	}
	
	// The shoot functions sends a bullet to the server
	public void shoot(float posX, float posY, float posZ, float dirX, float dirY, float dirZ) {
		TcpPayload payload = new TcpPayload(50);
    	payload.playerId = this.playerId;
    	payload.playerPosX = posX;
    	payload.playerPosY = posY;
    	payload.playerPosZ = posZ;
    	payload.playerDirX = dirX;
    	payload.playerDirY = dirY;
    	payload.playerDirZ = dirZ;
		try {
	    	ooStream.writeObject(payload);
		}
		catch (IOException e) {
			System.out.printf("Error in TcpClient, shoot() \n");
			e.printStackTrace();
		}
	}
	
	// The functions checkNick sends a nick to the server to see if it's available
	// It also checks if there is space on the server
	// The server sends a positive player id if both the nick is available and there is space on the server
	public boolean checkNick(String nickname) {
    	TcpPayloadInit payload = new TcpPayloadInit(10);
    	payload.message = nickname;
    	
		try {
			ooStream.writeObject(payload);
			
	    	// Get response from the server
	    	payload = new TcpPayloadInit(0);
			payload = (TcpPayloadInit) oiStream.readObject();
			if (payload.playerId != -1) {
				this.playerId = payload.playerId;
				payload.getMap(world);
				payload.getPlayers(otherPlayers);
				return true;
			}
		}
		catch (ClassNotFoundException | IOException e) {
			System.out.printf("Error in TcpClient, checkNick() \n");
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Get and set
	 */
	public int getPlayerId() {
		return this.playerId;
	}
}