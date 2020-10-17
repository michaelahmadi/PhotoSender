import java.util.ArrayList;

public class Recipient {
	
	private final String name;	// Recipient's Name as will appear in the email (e.g. Uncle Joe, Aunt Eliza, Frank)
	private final String email;	// Email to send photos to
	private ArrayList<String> photos; // List of the photos being sent to this recipient
	
	// Constructor
	public Recipient(String name, String email)
	{
		this.name = name;
		this.email = email;
		this.photos = new ArrayList<String>(); // Initialize photos ArrayList
	}

	// Getters and Setters
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}

	public ArrayList<String> getPhotos() {
		return photos;
	}

	public void setPhotos(ArrayList<String> photos) {
		this.photos = photos;
	}
	
	public void addPhoto(String photo)
	{
		this.photos.add(photo);
	}

	public int getNumPhotos()
	{
		return photos.size();
	}
}
