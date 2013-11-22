package is.ru.tgra.network;

import java.io.Serializable;

/******************************************************************************
 * TcpPayload.java
 * 
 * This is a serializable class that stores changes mad on a player or the map.
 * The client and server exchange this class between them.
 *****************************************************************************/


public class TcpPayload implements Serializable {
	static final long serialVersionUID = -50077493051991107L;
	
	// Head of the TcpPayload
	public int typeOfPayload;	// Indicates what information is in the TcpPayload
	public int playerId;		// The id of the player that this TcpPayload belongs to
	public String message;		// To send text between the client and server
	public int playerId2;		// If there is another player involved (killed)
	
	// Map part of the TcpPayload
	public int mapX;			// X coordinate of a block to change
	public int mapY;			// Y coordinate of a block to change
	public int mapZ;			// Z coordinate of a block to change
	public byte mapValue;	// The new block value
		
	// Player part of the TcpPayload
	public float playerPosX;
	public float playerPosY;
	public float playerPosZ;
	public float playerDirX;
	public float playerDirY;
	public float playerDirZ;
	
	public TcpPayload(int newTypeOfPayload) {
		this.typeOfPayload = newTypeOfPayload;
	}
}