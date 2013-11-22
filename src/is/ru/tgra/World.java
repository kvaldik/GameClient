package is.ru.tgra;

import is.ru.tgra.network.TcpClient;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

/******************************************************************************
 * World.java
 * 
 * This class represents the world or map that the player/players are in. The
 * map itself is a byte matrix where the value 0 is just air and other numbers
 * are the type of blocks. Currently there are 7 different types of blocks.
 * The map is stored on the server and the Connecting class should retrieve the
 * map from the server and load it into this class.
 * This class is accessed from both the main thread and the networking thread
 * so there are read and locks on all the functions that access the data.
 *****************************************************************************/


public class World {
	// Parts of the game client
	private TcpClient tcpClient;
	
	// Variables for the world
	private byte map[][][];
	private int mapScale;
	private int mapSize;
	private float blockDist;
	private float maxMovement;
	private float playerHeight;

	// Locks for threading
	private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();
	private final Lock fReadLock = fLock.readLock();
	private final Lock fWriteLock = fLock.writeLock();
	
	// Rendering variables
	private Texture[] texBlock;
	private FloatBuffer vertexBuffer;
	private FloatBuffer texCoordBufferBlock;
	
	
	
	public World(int size) {
		this.mapSize = size;
		this.map = new byte[this.mapSize][this.mapSize][this.mapSize];
		
		for (int x = 0; x < this.mapSize; x++)
			for (int y = 0; y < this.mapSize; y++)
				for (int z = 0; z < this.mapSize; z++)
					this.map[x][y][z] = 0;
		
		this.mapScale = 4;
		this.blockDist = 1.5f;
		this.maxMovement = 0.05f;
		this.playerHeight = this.mapScale+this.mapScale/2+0.1f;
		
		// The vertex buffer
		this.vertexBuffer = BufferUtils.newFloatBuffer(72);
		this.vertexBuffer.put(new float[]    {-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
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
		this.vertexBuffer.rewind();
		
		// Textures
		this.texBlock = new Texture[8];
		this.texBlock[1] = new Texture(Gdx.files.internal("assets/textures/crate01.jpg"));
		this.texBlock[2] = new Texture(Gdx.files.internal("assets/textures/crate02.jpg"));
		this.texBlock[3] = new Texture(Gdx.files.internal("assets/textures/crate03.jpg"));
		this.texBlock[4] = new Texture(Gdx.files.internal("assets/textures/crate04.jpg"));
		this.texBlock[5] = new Texture(Gdx.files.internal("assets/textures/brick.jpg"));
		this.texBlock[6] = new Texture(Gdx.files.internal("assets/textures/floor.jpg"));
		this.texBlock[7] = new Texture(Gdx.files.internal("assets/textures/Wood_Box_Texture.jpg"));
		// Texture buffer for the block
		this.texCoordBufferBlock = BufferUtils.newFloatBuffer(48);
		this.texCoordBufferBlock.put(new float[]   {0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
													0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
													0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
													0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
													0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
													0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f});
		texCoordBufferBlock.rewind();
	}
	
	public void setMap(byte[][][] newMap) {
		this.fWriteLock.lock();
		try {
			for (int x = 0; x < this.mapSize; x++)
				for (int y = 0; y < this.mapSize; y++)
					for (int z = 0; z < this.mapSize; z++)
						this.map[x][y][z] = newMap[x][y][z];
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public void drawWorld() {
		this.fReadLock.lock();
		try {
			for (int x = 0; x < this.mapSize; x++) {
				for (int y = 0; y < this.mapSize; y++) {
					for (int z = 0; z < this.mapSize; z++) {
						if (this.map[x][y][z] != 0) {
							Gdx.gl11.glPushMatrix();
							Gdx.gl11.glTranslatef(x*this.mapScale+this.mapScale/2, y*this.mapScale+this.mapScale/2, z*this.mapScale+this.mapScale/2);
							Gdx.gl11.glScalef(this.mapScale, this.mapScale, this.mapScale);
							// Set up the shading
							Gdx.gl11.glShadeModel(GL11.GL_SMOOTH);
							Gdx.gl11.glVertexPointer(3, GL11.GL_FLOAT, 0, this.vertexBuffer);
							// Set up the texture
							Gdx.gl11.glEnable(GL11.GL_TEXTURE_2D);
							Gdx.gl11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
							this.texBlock[this.map[x][y][z]].bind();
							Gdx.gl11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, this.texCoordBufferBlock);
							this.drawBox();
							Gdx.gl11.glPopMatrix();
							Gdx.gl11.glDisable(GL11.GL_TEXTURE_2D);
							Gdx.gl11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						}
					}
				}
			}
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	// This functions takes in a position and returns true if there is a block there
	// Also returns true if the position is outside of the world
	public boolean checkBlock(float posX, float posY, float posZ) {
		this.fReadLock.lock();
		try {
			int currentBlockX = (int)posX/this.mapScale;
			int currentBlockY = (int)posY/this.mapScale;
			int currentBlockZ = (int)posZ/this.mapScale;
			if (currentBlockX < 0 || currentBlockX == this.mapSize ||
				currentBlockY < 0 || currentBlockY == this.mapSize || posY < 0 ||
				currentBlockZ < 0 || currentBlockZ == this.mapSize)
				return true;
			return (this.map[currentBlockX][currentBlockY][currentBlockZ] != 0);
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public void changeBlock(float posX, float posY, float posZ, byte newValue) {
		this.fWriteLock.lock();
		try {
			int currentBlockX = (int)posX/this.mapScale;
			int currentBlockY = (int)posY/this.mapScale;
			int currentBlockZ = (int)posZ/this.mapScale;
			if (!(currentBlockX < 0 || currentBlockX == this.mapSize ||
					currentBlockY < 0 || currentBlockY == this.mapSize ||
					currentBlockZ < 0 || currentBlockZ == this.mapSize)) {
				this.map[currentBlockX][currentBlockY][currentBlockZ] = newValue;
				// Send this to the server
				this.tcpClient.updateMap(currentBlockX, currentBlockY, currentBlockZ, this.map[currentBlockX][currentBlockY][currentBlockZ]);
			}
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public void changeBlock(int currentBlockX, int currentBlockY, int currentBlockZ, byte newValue) {
		this.fWriteLock.lock();
		try {
			this.map[currentBlockX][currentBlockY][currentBlockZ] = newValue;
		} finally {
			this.fWriteLock.unlock();
		}
	}
	
	public boolean checkMoveX(float posX, float posY, float posZ, float moveX) {
		this.fReadLock.lock();
		try {
			// The position on the current block
			float blockPosX = posX%this.mapScale;
			float blockPosY = posY%this.mapScale;
			float blockPosZ = posZ%this.mapScale;
			// The current block numbers
			int currentBlockX = (int)posX/this.mapScale;
			int currentBlockY = (int)posY/this.mapScale;
			int currentBlockZ = (int)posZ/this.mapScale;
			// The blocks to look at
			byte moveBlock1;
			byte moveBlock2;
			
			// The player is located at the middle (y-axis) of a block, if he is under the middle
			// he is considered to be in the block below
			if (blockPosY < this.mapScale/2)
				currentBlockY -= 1;
			
			// Moving forward or backwards on the axis
			if (moveX > 0) {
				// Check if this is the end of the map
				if (currentBlockX ==  this.mapSize-1)
					if (blockPosX + moveX > this.mapScale - this.blockDist)
						return false;
					else
						return true;
				// Check for blocks in front of the current block
				moveBlock1 = this.map[currentBlockX+1][currentBlockY][currentBlockZ];
				moveBlock2 = this.map[currentBlockX+1][currentBlockY-1][currentBlockZ];
				if (moveBlock1 != 0 || moveBlock2 != 0)
					if (blockPosX + moveX > this.mapScale - this.blockDist)
						return false;
				// Check for blocks diagonally to the left
				if (currentBlockX < this.mapSize-1 && currentBlockZ > 0) {
					moveBlock1 = this.map[currentBlockX+1][currentBlockY][currentBlockZ-1];
					moveBlock2 = this.map[currentBlockX+1][currentBlockY-1][currentBlockZ-1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosZ < this.blockDist)
							if (blockPosX + moveX > this.mapScale - this.blockDist)
								return false;
				}
				// Check for blocks diagonally to the right
				if (currentBlockX < this.mapSize-1 && currentBlockZ < this.mapSize-1) {
					moveBlock1 = this.map[currentBlockX+1][currentBlockY][currentBlockZ+1];
					moveBlock2 = this.map[currentBlockX+1][currentBlockY-1][currentBlockZ+1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosZ > this.mapScale - this.blockDist)
							if (blockPosX + moveX > this.mapScale - this.blockDist)
								return false;
				}
			}
			else if (moveX < 0) {
				// Check if this is the end of the map
				if (currentBlockX ==  0)
					if (blockPosX + moveX < this.blockDist)
						return false;
					else
						return true;
				// Check for blocks in back of the current block
				moveBlock1 = this.map[currentBlockX-1][currentBlockY][currentBlockZ];
				moveBlock2 = this.map[currentBlockX-1][currentBlockY-1][currentBlockZ];
				if (moveBlock1 != 0|| moveBlock2 != 0)
					if (blockPosX + moveX < this.blockDist)
						return false;
				// Check for blocks diagonally to the left
				if (currentBlockX > 0 && currentBlockZ < this.mapSize-1) {
					moveBlock1 = this.map[currentBlockX-1][currentBlockY][currentBlockZ+1];
					moveBlock2 = this.map[currentBlockX-1][currentBlockY-1][currentBlockZ+1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosZ > this.mapScale - this.blockDist)
							if (blockPosX + moveX < this.blockDist)
								return false;
				}
				// Check for blocks diagonally to the right
				if (currentBlockX > 0 && currentBlockZ > 0) {
					moveBlock1 = this.map[currentBlockX-1][currentBlockY][currentBlockZ-1];
					moveBlock2 = this.map[currentBlockX-1][currentBlockY-1][currentBlockZ-1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosZ < this.blockDist)
							if (blockPosX + moveX < this.blockDist)
								return false;
				}
			}
			return true;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public boolean checkMoveY(float posX, float posY, float posZ, float moveY) {
		this.fReadLock.lock();
		try {
			// The position on the current block
			float blockPosX = posX%this.mapScale;
			float blockPosY = posY%this.mapScale;
			float blockPosZ = posZ%this.mapScale;
			// The current block numbers
			int currentBlockX = (int)posX/this.mapScale;
			int currentBlockY = (int)posY/this.mapScale;
			int currentBlockZ = (int)posZ/this.mapScale;
			// The block to look at
			byte moveBlock;
			
			// Moving forward or backwards on the axis
			if (moveY > 0) {
				// Check if this is the end of the map
				if (currentBlockY ==  this.mapSize-1)
					if (blockPosY + moveY > this.mapScale - this.blockDist)
						return false;
					else
						return true;
				// Check for a block above of the current block
				moveBlock = this.map[currentBlockX][currentBlockY+1][currentBlockZ];
				if (moveBlock != 0)
					if (blockPosY + moveY > this.mapScale - this.blockDist)
						return false;
				// Check for blocks to the sides above of the current block
				if (currentBlockX < this.mapSize-1) {
					moveBlock = this.map[currentBlockX+1][currentBlockY+1][currentBlockZ];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockX > 0) {
					moveBlock = this.map[currentBlockX-1][currentBlockY+1][currentBlockZ];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX][currentBlockY+1][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX][currentBlockY+1][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosZ < this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				// Check for blocks diagonally above the current block
				if (currentBlockX < this.mapSize-1 && currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX+1][currentBlockY+1][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist || blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockX > 0 && currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX-1][currentBlockY+1][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist || blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockX < this.mapSize-1 && currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX+1][currentBlockY+1][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist || blockPosZ < this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
				if (currentBlockX > 0 && currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX-1][currentBlockY+1][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist || blockPosZ < this.blockDist)
							if (blockPosY + moveY > this.mapScale - this.blockDist)
								return false;
				}
			}
			else if (moveY < 0) {
				// Check if this is the end of the map
				if (currentBlockY ==  1)
					if (blockPosY + moveY < this.mapScale/2)
						return false;
					else
						return true;
				// Check for a block bellow of the current block
				moveBlock = this.map[currentBlockX][currentBlockY-2][currentBlockZ];
				if (moveBlock != 0)
					if (blockPosY + moveY < this.mapScale/2)
						return false;
				// Check for blocks to the sides below of the current block
				if (currentBlockX < this.mapSize-1) {
					moveBlock = this.map[currentBlockX+1][currentBlockY-2][currentBlockZ];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockX > 0) {
					moveBlock = this.map[currentBlockX-1][currentBlockY-2][currentBlockZ];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX][currentBlockY-2][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX][currentBlockY-2][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosZ < this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				// Check for blocks diagonally below the current block
				if (currentBlockX < this.mapSize-1 && currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX+1][currentBlockY-2][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist || blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockX > 0 && currentBlockZ < this.mapSize-1) {
					moveBlock = this.map[currentBlockX-1][currentBlockY-2][currentBlockZ+1];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist || blockPosZ > this.mapScale - this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockX < this.mapSize-1 && currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX+1][currentBlockY-2][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosX > this.mapScale - this.blockDist || blockPosZ < this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
				if (currentBlockX > 0 && currentBlockZ > 0) {
					moveBlock = this.map[currentBlockX-1][currentBlockY-2][currentBlockZ-1];
					if (moveBlock != 0)
						if (blockPosX < this.blockDist || blockPosZ < this.blockDist)
							if (blockPosY + moveY < this.mapScale/2)
								return false;
				}
			}
			return true;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public boolean checkMoveZ(float posX, float posY, float posZ, float moveZ) {
		this.fReadLock.lock();
		try {
			// The position on the current block
			float blockPosX = posX%this.mapScale;
			float blockPosY = posY%this.mapScale;
			float blockPosZ = posZ%this.mapScale;
			// The current block numbers
			int currentBlockX = (int)posX/this.mapScale;
			int currentBlockY = (int)posY/this.mapScale;
			int currentBlockZ = (int)posZ/this.mapScale;
			// The blocks to look at
			byte moveBlock1;
			byte moveBlock2;
			
			// The player is located at the middle (y-axis) of a block, if he is under the middle
			// he is considered to be in the block below
			if (blockPosY < this.mapScale/2)
				currentBlockY -= 1;
			
			// Moving forward or backwards on the axis
			if (moveZ > 0) {
				// Check if this is the end of the map
				if (currentBlockZ ==  this.mapSize-1)
					if (blockPosZ + moveZ > this.mapScale - this.blockDist)
						return false;
					else
						return true;
				// Check for blocks in front of the current block
				moveBlock1 = this.map[currentBlockX][currentBlockY][currentBlockZ+1];
				moveBlock2 = this.map[currentBlockX][currentBlockY-1][currentBlockZ+1];
				if (moveBlock1 != 0 || moveBlock2 != 0)
					if (blockPosZ + moveZ> this.mapScale - this.blockDist)
						return false;
				// Check for blocks diagonally to the left
				if (currentBlockX < this.mapSize-1 && currentBlockZ < this.mapSize-1) {
					moveBlock1 = this.map[currentBlockX+1][currentBlockY][currentBlockZ+1];
					moveBlock2 = this.map[currentBlockX+1][currentBlockY-1][currentBlockZ+1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosX > this.mapScale - this.blockDist)
							if (blockPosZ + moveZ > this.mapScale - this.blockDist)
								return false;
				}
				// Check for blocks diagonally to the right
				if (currentBlockX > 0 && currentBlockZ < this.mapSize-1) {
					moveBlock1 = this.map[currentBlockX-1][currentBlockY][currentBlockZ+1];
					moveBlock2 = this.map[currentBlockX-1][currentBlockY-1][currentBlockZ+1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosX < this.blockDist)
							if (blockPosZ + moveZ > this.mapScale - this.blockDist)
								return false;
				}
			}
			else if (moveZ < 0) {
				// Check if this is the end of the map
				if (currentBlockZ ==  0)
					if (blockPosZ + moveZ < this.blockDist)
						return false;
					else
						return true;
				// Check for blocks in back of the current block
				moveBlock1 = this.map[currentBlockX][currentBlockY][currentBlockZ-1];
				moveBlock2 = this.map[currentBlockX][currentBlockY-1][currentBlockZ-1];
				if (moveBlock1 != 0|| moveBlock2 != 0)
					if (blockPosZ + moveZ < this.blockDist)
						return false;
				// Check for blocks diagonally to the left
				if (currentBlockX > 0 && currentBlockZ > 0) {
					moveBlock1 = this.map[currentBlockX-1][currentBlockY][currentBlockZ-1];
					moveBlock2 = this.map[currentBlockX-1][currentBlockY-1][currentBlockZ-1];
					if (moveBlock1 != 0|| moveBlock2 != 0)
						if (blockPosX < this.blockDist)
							if (blockPosZ + moveZ < this.blockDist)
								return false;
				}
				// Check for blocks diagonally to the right
				if (currentBlockX < this.mapSize-1 && currentBlockZ > 0) {
					moveBlock1 = this.map[currentBlockX+1][currentBlockY][currentBlockZ-1];
					moveBlock2 = this.map[currentBlockX+1][currentBlockY-1][currentBlockZ-1];
					if (moveBlock1 != 0 || moveBlock2 != 0)
						if (blockPosX > this.mapScale - this.blockDist)
							if (blockPosZ + moveZ < this.blockDist)
								return false;
				}
			}
			return true;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	private void drawBox() {
		Gdx.gl11.glNormal3f(0.0f, 0.0f, -1.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		Gdx.gl11.glNormal3f(1.0f, 0.0f, 0.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
		Gdx.gl11.glNormal3f(0.0f, 0.0f, 1.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 8, 4);
		Gdx.gl11.glNormal3f(-1.0f, 0.0f, 0.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 12, 4);
		Gdx.gl11.glNormal3f(0.0f, 1.0f, 0.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 16, 4);
		Gdx.gl11.glNormal3f(0.0f, -1.0f, 0.0f);
		Gdx.gl11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 20, 4);
	}
	
	/*
	 * Get and set
	 */
	public float getPlayerHeight() {
		this.fReadLock.lock();
		try {
			return this.playerHeight;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public byte getBlock(int x, int y, int z) {
		this.fReadLock.lock();
		try {
			return this.map[x][y][z];
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public float getMaxMovement() {
		this.fReadLock.lock();
		try {
			return this.maxMovement;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public float getMapScale() {
		this.fReadLock.lock();
		try {
			return this.mapScale;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public float getMapSize() {
		this.fReadLock.lock();
		try {
			return this.mapSize;
		} finally {
			this.fReadLock.unlock();
		}
	}
	
	public void setTcpClient(TcpClient newTcpClient) {
		this.fWriteLock.lock();
		try {
			this.tcpClient = newTcpClient;
		} finally {
			this.fWriteLock.unlock();
		}
	}
}