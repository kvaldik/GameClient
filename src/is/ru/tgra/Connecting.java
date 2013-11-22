package is.ru.tgra;

import is.ru.tgra.network.TcpClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/******************************************************************************
 * Connecting.java
 * 
 * This class represent stage one of the game, where the player inputs a
 * nickname and connects to the server. The same error appears if the server is
 * full and if the nickname is already in use. There are two public boolean
 * variable to tell the state of the connection, the first one connecting is
 * true while the game is trying to connect. Ones it connects, it changes both
 * variables to indicate that the second/final stage of the game can be
 * created. Once it is created the doneConnecting variable is set to true and
 * the final stage can be rendered
 *****************************************************************************/


public class Connecting {
	// Parts of the game client
	private TcpClient tcpClient;
	
	// Connecting variables
	public boolean connecting;		// Is currently connecting
	public boolean doneConnecting;	// Is done connecting (the game can start)
	
	// Rendering variables
	private Skin skin;
	private Stage stage;
	private TextField textfield;
	private TextButton button;
	
	
	// Constructor
	public Connecting(TcpClient newTcpClient) {
		this.tcpClient = newTcpClient;
		this.connecting = true;
		this.doneConnecting = false;
		this.stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		// Load a skin
		this.skin = new Skin(Gdx.files.internal("assets/skins/skin.json"));

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		this.skin.add("white", new Texture(pixmap));

		// Store the default LibGdx font under the name "default".
		this.skin.add("default", new BitmapFont());
		
		// Create a text field for the nickname
		this.textfield = new TextField("", skin);
		this.textfield.setMessageText("Type a nickname");
		
		// Create a textButton for connecting
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.down = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		this.skin.add("default", textButtonStyle);
		this.button = new TextButton("Connect!", skin);
		this.button.setScale(1.0f);

		// Create a table that fills the screen. Everything else will go inside this table.
		this.skin.add("default", new Table());
		Table table = new Table(skin);
		table.setFillParent(true);
		stage.addActor(table);
		table.add(textfield);
		table.row();
		table.add(" ");
		table.row();
		table.add(button);
		
		// Add a listener for the connect button
		button.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if (connecting) {
					// Check if the nick is available
					boolean responce = tcpClient.checkNick(textfield.getText());
					// If the nick was available advance to the next state
					if (responce) {
						connecting = false;
						doneConnecting = true;
					}
					// Else set the button to indicate the nick wasn't available
					// The server also denies the nick if the server is full
					else
						button.setText("Nick in use or server is full!");
				}
			}
		});
	}
	
	// Render this state
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}
}