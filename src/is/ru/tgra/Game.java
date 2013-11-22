package is.ru.tgra;

import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.utils.BufferUtils;

/******************************************************************************
 * Game.java
 * 
 * This is the second/final stage of the game. This class just sets up lights
 * and so fourth for the scene.
 *****************************************************************************/


public class Game {
	// Parts of the game client
	private Player player;
	
	// Rendering variables
	private FloatBuffer vertexBuffer;
	
	// Constructor
	public Game(Player newPlayer) {
		this.player = newPlayer;
	}
	
	// Create the game
	public void createGame() {
		Gdx.gl11.glEnable(GL11.GL_LIGHTING);
		Gdx.gl11.glEnable(GL11.GL_LIGHT1);
		Gdx.gl11.glEnable(GL11.GL_LIGHT0);
		Gdx.gl11.glEnable(GL11.GL_DEPTH_TEST);
		Gdx.gl11.glShadeModel(GL11.GL_SMOOTH);
		
		Gdx.gl11.glClearColor(0.0f, 0.0f, 0.6f, 1.0f);

		Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
		Gdx.gl11.glLoadIdentity();
		Gdx.glu.gluPerspective(Gdx.gl11, 90, 1.333333f, 1.0f, 10.0f);

		Gdx.gl11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		vertexBuffer = BufferUtils.newFloatBuffer(72);
		vertexBuffer.put(new float[]   {-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
										0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
										0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
										0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
										0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
										-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
										-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
										-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
										-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f,
										0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f,
										-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,
										0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f});
		vertexBuffer.rewind();
		Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
	}
	
	// Draw the game
	public void drawGame() {
		Gdx.gl11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		this.player.updateCamera();
		
		// Configure light
		Gdx.gl11.glLightfv(GL11.GL_LIGHT1, GL11.GL_AMBIENT, new float[] {2.0f,	2.0f, 2.0f, 5.0f}, 0);

		// Set diffuse material.
		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE, new float[] {1.0f, 1.0f, 1.0f, 1.0f}, 0);
		Gdx.gl11.glMaterialfv(GL11.GL_FRONT, GL11.GL_SPECULAR, new float[] {0.2f, .2f, .2f, 1.0f}, 0);
		Gdx.gl11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 100);

		Gdx.gl11.glEnable(GL11.GL_LIGHTING);
		Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, this.vertexBuffer);

		Gdx.gl11.glMatrixMode(GL11.GL_PROJECTION);
		Gdx.gl11.glLoadIdentity();
		Gdx.glu.gluPerspective(Gdx.gl11, 60.0f, (float)Gdx.graphics.getWidth()/Gdx.graphics.getHeight(), 1.0f, 90.0f);
		
		Gdx.gl11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	/*
	 * Set and get 
	 */
	public FloatBuffer getVertexBuffer() {
		return this.vertexBuffer;
	}
}
