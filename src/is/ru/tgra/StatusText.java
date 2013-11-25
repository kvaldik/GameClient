package is.ru.tgra;

/******************************************************************************
 * StatusText.java
 * 
 * This is a class for a status text
 *****************************************************************************/
public class StatusText {
	public String message;
	public long timeInserted;
	
	public StatusText(String newMessage, long time) {
		this.message = newMessage;
		this.timeInserted = time;
	}
}
