package is.ru.tgra;

import is.ru.tgra.network.TcpClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/******************************************************************************
 * Player.java
 * 
 * This is the input processor of the game. It represents the current player
 * and the moves and actions he can take.
 *****************************************************************************/

public class Player implements InputProcessor {
	// Parts of the game client
	private TcpClient tcpClient;
	private HUD hud;
	private World world;
	private OtherPlayers otherPlayers;
	private Sounds sounds;
	
	// Mouse movements
	private int mouseX;				// Position of the mouse
	private int mouseY;				// Position of the mouse
	private int mouseXm;			// Mouse movement
	private int mouseYm;			// Mouse movement
	private boolean mouseUpdate;	// Has the mouse moved
	private boolean mouseMovement;	// Is mouse movement enabled
	
	// A Camera that represents the player
	private PerspectiveCamera camera;
	
	// Gravity of the player
	private float speedY;		// Tells the speed the player is falling
	private boolean onGround;	// Indicates if the player is on ground or not
	
	
	public Player(World world, HUD newHud, TcpClient newTcpClient, OtherPlayers newOtherPlayers, Sounds newSounds) {
		this.world = world;
		this.hud = newHud;
		this.tcpClient = newTcpClient;
		this.otherPlayers = newOtherPlayers;
		this.sounds = newSounds;
		
		this.mouseX = Gdx.input.getX();
		this.mouseY = Gdx.input.getY();
		this.mouseMovement = true;

		// Start the player high up in a random location
		float mapLength = this.world.getMapScale()*this.world.getMapSize();
		mapLength -= 2.0f;
		this.camera = new PerspectiveCamera(60.0f, 1280/50, 720/50);
		this.camera.translate(6.0f + (float)(Math.random() * (mapLength - 6.0f)), mapLength, 6.0f + (float)(Math.random() * (mapLength - 6.0f)));
		this.camera.lookAt(6.0f + (float)(Math.random() * (mapLength - 6.0f)), mapLength, 6.0f + (float)(Math.random() * (mapLength - 6.0f)));
	}
	
	public boolean update(float deltaTime) {
		boolean returnValue = false;
		Vector3 ble = new Vector3(this.camera.up.x, this.camera.up.y, this.camera.up.z);
		ble.crs(this.camera.direction);
		
		// Mouse movement
		if (mouseMovement) {
			int movedX = this.mouseX - this.mouseXm;
			int movedY = this.mouseY - this.mouseYm;
			if (this.mouseUpdate) {
				if(movedY < 0) 
					this.camera.rotate(-10*movedY*deltaTime, ble.x, ble.y, ble.z);
				if(movedY > 0) 
					this.camera.rotate(-10*movedY*deltaTime, ble.x, ble.y, ble.z);
				if(movedX > 0)
					this.camera.rotate(10*movedX*deltaTime, 0, 1, 0);
				if(movedX < 0)
					this.camera.rotate(10*movedX*deltaTime, 0, 1, 0);
				this.mouseUpdate = false;
			}
			
			Gdx.input.setCursorPosition(500, 500);
			this.mouseX = Gdx.input.getX();
			this.mouseY = Gdx.input.getY();
			this.mouseXm = 0;
			this.mouseYm = 0;
		}
		
		// Move the player
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
				this.move(20*this.camera.direction.x*deltaTime, 20*this.camera.direction.z*deltaTime);
			else
				this.move(10*this.camera.direction.x*deltaTime, 10*this.camera.direction.z*deltaTime);
			returnValue = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			this.move(-10*this.camera.direction.x*deltaTime, -10*this.camera.direction.z*deltaTime);
			returnValue = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			this.strafe(10*ble.x*deltaTime, 10*ble.z*deltaTime);
			returnValue = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			this.strafe(-10*ble.x*deltaTime, -10*ble.z*deltaTime);
			returnValue = true;
		}
		
		// Rotate the camera manually
		if (Gdx.input.isKeyPressed(Input.Keys.UP))
			this.camera.rotate(90*deltaTime, ble.x, ble.y, ble.z);
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
			this.camera.rotate(-90*deltaTime, ble.x, ble.y, ble.z);
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			this.camera.rotate(90*deltaTime, 0, 1, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			this.camera.rotate(-90*deltaTime, 0, 1, 0);
		
		// Jump
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			if (this.onGround) {
				this.speedY = 30.0f;
				this.onGround = false;
			}
		}
		
		// Try to move the player
		if (this.moveY(this.speedY*deltaTime) + this.moveY(-1*deltaTime) != 0) {
			returnValue = true;
		}
		// Check if the player is on ground
		this.onGround = this.world.checkBlock(this.camera.position.x, this.camera.position.y-this.world.getPlayerHeight(), this.camera.position.z);
		// Apply increased speed if applicable
		if (!this.onGround && this.speedY > -50.0f) {
			this.speedY -= 100.0f*deltaTime;
			returnValue = true;
		}
		// Reset the speed if on ground
		if (this.onGround)
			this.speedY = 0;
		return returnValue;
	}
	
	private void move(float moveX, float moveZ) {
		moveX(moveX);
		moveZ(moveZ);
	}
	
	private void strafe(float strafeX, float strafeZ) {
		moveX(strafeX);
		moveZ(strafeZ);
	}
	
	private float moveX(float moveX) {
		float returnValue = 0;
		if (Math.abs(moveX) > this.world.getMaxMovement()) {
			returnValue += moveX(moveX/2);
			returnValue += moveX(moveX - moveX/2);
			return returnValue;
		}
		else {
			if (this.world.checkMoveX(this.camera.position.x, this.camera.position.y, this.camera.position.z, moveX)) {
				this.camera.translate(moveX, 0, 0);
				return moveX;
			}
			else
				return 0;
		}
	}
	
	private float moveY(float moveY) {
		float returnValue = 0;
		if (Math.abs(moveY) > this.world.getMaxMovement()) {
			returnValue += moveY(moveY/2);
			returnValue += moveY(moveY - moveY/2);
			return returnValue;
		}
		else {
			if (this.world.checkMoveY(this.camera.position.x, this.camera.position.y, this.camera.position.z, moveY)) {
				this.camera.translate(0, moveY, 0);
				return moveY;
			}
			else
				return 0;
		}
	}
	
	private float moveZ(float moveZ) {
		float returnValue = 0;
		if (Math.abs(moveZ) > this.world.getMaxMovement()) {
			returnValue += moveZ(moveZ/2);
			returnValue += moveZ(moveZ - moveZ/2);
			return returnValue;
		}
		else {
			if (this.world.checkMoveZ(this.camera.position.x, this.camera.position.y, this.camera.position.z, moveZ)) {
				this.camera.translate(0, 0, moveZ);
				return moveZ;
			}
			else
				return 0;
		}
	}
	
	public void updateCamera() {
		this.camera.update();
		this.camera.apply(Gdx.gl11);
	}
	
	@Override
	public boolean keyUp(int arg0) {
		// Exit the game
		if (Input.Keys.ESCAPE == arg0) 
			Gdx.app.exit();
		// Turn the mouse on and off
		if (Input.Keys.P == arg0)
			this.mouseMovement = !this.mouseMovement;
		// Draw the score board
		if (Input.Keys.TAB == arg0)
			this.hud.drawScoreBoard();
		// Change between placing blocks or shooting
		if (Input.Keys.Q == arg0)
			this.hud.changeGunOrBox();
		// Reload
		if (Input.Keys.R == arg0) {
			this.otherPlayers.reload();
			this.sounds.playReload();
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int arg0, int arg1) {
		// Store this mouse movement and use it when calculating next frame
		this.mouseXm = arg0;
		this.mouseYm = arg1;
		this.mouseUpdate = true;
		return false;
	}

	@Override
	public boolean keyDown(int arg0) {return false;}

	@Override
	public boolean keyTyped(char arg0) {return false;}

	@Override
	public boolean scrolled(int arg0) {
		System.out.printf("Scorlling: %d \n", arg0);
		if (arg0 < 0)
			this.otherPlayers.decSelectedBlock();
		else
			this.otherPlayers.incSelectedBlock();
		return false;
	}

	@Override
	public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
		if (arg3 == Input.Buttons.LEFT)
			if (this.hud.gunOrBox)
				this.removeBlock();
			// Else he has the gun equipped
			else {
				// If the player isn't reloading and has ammo (checked in reloading too) he can shoot
				if (!this.otherPlayers.getReloading()) {
					this.otherPlayers.shoot();
					this.tcpClient.shoot(this.camera.position.x, this.camera.position.y, this.camera.position.z,
							 			 this.camera.direction.x, this.camera.direction.y, this.camera.direction.z);
					this.sounds.playGunShot();
				}
				else {
					this.sounds.playGunEmpty();
				}
			}
		if (arg3 == Input.Buttons.RIGHT)
			if (this.hud.gunOrBox)
				this.addBlock();
		return false;
	}

	@Override
	public boolean touchDragged(int arg0, int arg1, int arg2) {return false;}

	@Override
	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {return false;}
	
	private void removeBlock() {
		Vector3 currentPos = new Vector3(this.camera.position.x, this.camera.position.y, this.camera.position.z);
		Vector3 blockToRemove = this.findBlock(currentPos);
		this.world.changeBlock(blockToRemove.x, blockToRemove.y, blockToRemove.z, (byte)0);
	}
	
	private Vector3 findBlock(Vector3 currentPos) {
		if (this.world.checkBlock(currentPos.x,currentPos.y,currentPos.z))
			return currentPos;
		else
			return this.findBlock(currentPos.add(this.camera.direction));
	}
	
	private void addBlock() {
		Vector3 currentPos = new Vector3(this.camera.position.x, this.camera.position.y, this.camera.position.z);
		currentPos.add(this.camera.direction);
		Vector3 lastPos = new Vector3(this.camera.position.x, this.camera.position.y, this.camera.position.z);
		Vector3 blockToAdd = this.findBlock(currentPos, lastPos);
		this.world.changeBlock(blockToAdd.x, blockToAdd.y, blockToAdd.z, this.otherPlayers.getSelectedBlock());
	}
	
	private Vector3 findBlock(Vector3 currentPos, Vector3 lastPos) {
		if (this.world.checkBlock(currentPos.x,currentPos.y,currentPos.z))
			return lastPos;
		else
			return this.findBlock(currentPos.add(this.camera.direction), lastPos.add(this.camera.direction));
	}
	
	public void die() {
		float mapLength = this.world.getMapScale()*this.world.getMapSize();
		mapLength -= 2.0f;
		this.camera.position.x = 6.0f + (float)(Math.random() * (mapLength - 6.0f));
		this.camera.position.y = mapLength;
		this.camera.position.z = 6.0f + (float)(Math.random() * (mapLength - 6.0f));
	}
	
	/*
	 * Get and set
	 */
	public float getPosX() {
		return camera.position.x;
	}
	
	public float getPosY() {
		return camera.position.y;
	}
	
	public float getPosZ() {
		return camera.position.z;
	}
	
	public float getDirX() {
		return camera.direction.x;
	}
	
	public float getDirY() {
		return camera.direction.y;
	}
	
	public float getDirZ() {
		return camera.direction.z;
	}
	
	public void setInputProcessor() {
		Gdx.input.setInputProcessor(this);
	}

	public boolean getMouseMovement() {
		return this.mouseMovement;
	}
}
