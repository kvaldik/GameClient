package is.ru.tgra;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import is.ru.tgra.network.TcpClient;
import java.nio.FloatBuffer;

/******************************************************************************
 * GameClient.java
 * 
 * This is the main class that contains all the parts (classes) of the game.
 * It starts of running the connecting part and then switches to the main game
 * part. A short review of all the parts:
 * 
 * Connecting		Is the stage where the game asks for a nick anme and
 * 					connects to the server
 * Game				Is the second/final stage of the game
 * Player			Represents the player, his movements and his actions
 * World			Is the map the player is in
 * OtherPlayers		Represents all the other players in the game, their
 * 					nickname, position and so fourth. It also contains this
 * 					players ammo and health
 * HUD				Is the Heads Up Display and draws information about the
 * 					game status, also draws the score board
 * TcpClient		Is a separate thread that communicates with the server
 * 
 * There is only one instance of each of these classes in the game.
 *****************************************************************************/


public class GameClient implements ApplicationListener {
	// Parts of the game client
	private Connecting connecting;		// Initial state of the game
	private Player player;				// Represents the current player
	private World world;				// The world/map of the game
	private OtherPlayers otherPlayers;	// Represents other players in the game
	private TcpClient tcpClient;		// A network thread to communicate with the server
	private HUD hud;					// The heads up display
	private Game game;					// The second and final state of the game
	private Sounds sounds;
	
	// Game variables
	private boolean moved;
	
	// Rendering variables
	private FloatBuffer vertexBuffer;
	

	@Override
	public void create() {
		this.sounds = new Sounds();
		this.world = new World(50);
		this.otherPlayers = new OtherPlayers(16, this.world.getMapScale(), this.vertexBuffer);
		this.tcpClient = new TcpClient("89.17.129.52", 5050, this.world, this.otherPlayers);
		this.connecting = new Connecting(this.tcpClient);
		this.hud = new HUD(this.otherPlayers);
		this.player = new Player(this.world, this.hud, this.tcpClient, this.otherPlayers, this.sounds);
		this.game = new Game(this.player);
		
		this.world.setTcpClient(this.tcpClient);
		this.otherPlayers.setHud(this.hud);
		this.otherPlayers.setPlayer(this.player);
	}
	
	// Update is a part of the second state of the game and represents all changes in the game
	private void update() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Update of the player returns true if the player moved, if so, the position is sent to the server
		this.moved = this.player.update(deltaTime);
		if (this.moved) {
			this.tcpClient.updatePlayer(this.player.getPosX(), this.player.getPosY(), this.player.getPosZ(), this.player.getDirX(), this.player.getDirY(), this.player.getDirZ());
			this.moved = false;
		}
		
		// Update other players (reloading)
		this.otherPlayers.update();
	}

	private void display() {
		// Set up the game
		this.game.drawGame();
		// Draw the world
		this.world.drawWorld();
		// Draw other players in the game
		this.otherPlayers.drawPlayers();
		// Draw the HUD
		this.hud.drawHud();
	}

	@Override
	public void render() {
		// If in the first state of the game
		if (this.connecting.connecting) {
			this.connecting.render();
		}
		// If done in the first state, prepare for the next state
		else if (this.connecting.doneConnecting) {
			// Create the game
			this.game.createGame();
			// Set variables of different parts of client game
			this.player.setInputProcessor();
			this.vertexBuffer = this.game.getVertexBuffer();
			this.otherPlayers.setVertexBuffer(vertexBuffer);
			this.otherPlayers.setPlayerId(this.tcpClient.getPlayerId());
			// Start the network thread to get changes from the server
			this.tcpClient.start();
			this.connecting.doneConnecting = false;		// Done preparing the next state
			System.out.printf("Connected! \n");
		}
		// In the second / final state of the game
		else {
			this.update();
			this.display();
		}
	}

	@Override public void resize(int width, int height) {
		this.hud.updateCamera();
	}
	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void dispose() {}
}