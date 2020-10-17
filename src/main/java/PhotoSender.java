import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PhotoSender extends Application {
	
	
	private static final String APPLICATION_NAME = "Photo Sender";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE, GmailScopes.GMAIL_SEND);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\micha\\eclipse-workspace\\PhotoSender\\src\\main\\resources\\credentials.json";
    
	private Scene scene; 			
	private BorderPane brd;
	
	// Main Scene Elements
	private Text newRecipientName;
	private TextField newRecipientNameField;
	private HBox newRecipientNameHBox;
	private Text newRecipientEmail;
	private TextField newRecipientEmailField;
	private HBox newRecipientEmailHBox;
	private VBox newRecipientVBox;
	private Button addRecipientsBtn;

	private Recipient recipient;
	private HBox recipientsHBox;
	private VBox addedRecipientVBox;
	private Image emptyFolder;
	private Image populatedFolder;
	private ImageView folderView;
	
	private Button launchBtn;
	private HBox launchBtnHBox;

	private BorderPane uploadPane;
	private ListView<String> status; // Used to print status of files & emails after pressing launchBtn
	private Button exitBtn;
	private HBox exitBtnHBox;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		newRecipientName = new Text("New Recipient Name: ");
		newRecipientNameField = new TextField();
		newRecipientNameHBox = new HBox(20, newRecipientName, newRecipientNameField);
		newRecipientNameHBox.setAlignment(Pos.CENTER);
		
		newRecipientEmail = new Text("New Recipient Email: ");
		newRecipientEmailField = new TextField();
		newRecipientEmailHBox = new HBox(20, newRecipientEmail, newRecipientEmailField);
		newRecipientEmailHBox.setAlignment(Pos.CENTER);
		
		status = new ListView<String>();
		exitBtn = new Button("Exit");
		exitBtnHBox = new HBox(exitBtn);
		exitBtnHBox.setAlignment(Pos.CENTER);
		
		uploadPane = new BorderPane();
		uploadPane.setCenter(status);
		uploadPane.setBottom(exitBtnHBox);
		
		addRecipientsBtn = new Button("Add Recipient");

        emptyFolder = new Image("/emptyfolder.png", 100, 100, true, false);
        populatedFolder = new Image("/populatedfolder.png", 100, 100, true, false);
		
        EventHandler<DragEvent> pictureDragOverHandler = new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.LINK);
                
			}
		};
		
		EventHandler<DragEvent> pictureDragDroppedHandler = new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
            	
                ImageView target = (ImageView) event.getGestureTarget();
                Recipient recip = (Recipient) target.getUserData();
                
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;

                if(db.hasFiles())
                {
                	List<java.io.File> theFiles = db.getFiles();
                	for (java.io.File x : theFiles) {
                	
                		recip.addPhoto(x.getAbsolutePath());
                                        		
                        target.setImage(populatedFolder);
                        
                        VBox parentVBox = (VBox) target.getParent();
                        Text fileText = (Text) parentVBox.getChildren().get(2);
                        fileText.setText("Files: " + recip.getNumPhotos());
                        
                        success = true;
                	}
                }
                
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);
                
                event.consume();
			}
		};
		
		addRecipientsBtn.setOnAction(new EventHandler<ActionEvent> () {
			public void handle(ActionEvent action) {

				
				recipient = new Recipient(newRecipientNameField.getText(), newRecipientEmailField.getText());
				
				folderView = new ImageView(emptyFolder);
				folderView.setUserData(recipient);
				folderView.setOnDragOver(pictureDragOverHandler);
				folderView.setOnDragDropped(pictureDragDroppedHandler);
				
				addedRecipientVBox = new VBox(5, new Text(newRecipientNameField.getText()), folderView, new Text("Files: 0"));
				addedRecipientVBox.setAlignment(Pos.CENTER);
				
				
                
				recipientsHBox.getChildren().addAll(addedRecipientVBox);
                
                newRecipientNameField.clear();
                newRecipientEmailField.clear();				
			}
		});

		exitBtn.setOnAction(new EventHandler<ActionEvent> () {
			public void handle(ActionEvent action) {
				Platform.exit();
				System.exit(0); 
			}
		});
       
		
		newRecipientVBox = new VBox(20, newRecipientNameHBox, newRecipientEmailHBox, addRecipientsBtn);
		newRecipientVBox.setAlignment(Pos.CENTER);

		recipientsHBox = new HBox(40);
		recipientsHBox.setAlignment(Pos.CENTER);
	
		
		launchBtn = new Button("Upload and Send");
		
		launchBtn.setOnAction(new EventHandler<ActionEvent> () {
			public void handle(ActionEvent action) {
				
				scene.setRoot(uploadPane); // Unfortunately the UI thread is blocked, and this line will not run until after the upload process is complete. So thread concurrency is one improvement I could make on this application.
				
				NetHttpTransport HTTP_TRANSPORT = null;
				
				try {
					HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
				} catch (GeneralSecurityException | IOException e2) {
					e2.printStackTrace();
				}
				
		        
				Credential credentials = null;
				try {
					credentials = getCredentials(HTTP_TRANSPORT);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
		        
				Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
					  .setApplicationName(APPLICATION_NAME)
					  .build();
				
				Gmail gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
		                .setApplicationName(APPLICATION_NAME)
		                .build();
		                
				for (Node child : recipientsHBox.getChildren()) { // For each recipient
				    VBox rVBox = (VBox) child;
				    ImageView imgView = (ImageView) rVBox.getChildren().get(1);
				    Recipient recip = (Recipient) imgView.getUserData();
				    String name = recip.getName();
				    // Create and upload a folder named "RECIPIENT_NAME Photos Folder"
				    status.getItems().add("Starting " + name + " folder");
				    File folderMetadata = new File();
			        folderMetadata.setName(name + " Photos Folder");  // Name Folder
			        folderMetadata.setMimeType("application/vnd.google-apps.folder");
			        
			        File folder = null;
					try {
						folder = driveService.files().create(folderMetadata)
						    .setFields("id, webViewLink")
						    .execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
			        String folderId = folder.getId();
			        String link = folder.getWebViewLink();
			        
			        // Make the folder viewable to anyone with a link
			        try {
						driveService.permissions().create(folderId, 
						    new Permission().setRole("reader").setType("anyone").setAllowFileDiscovery(false)).execute();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			        
			        
			        // Next, upload each photo under the new folder
				    ArrayList<String> photos = recip.getPhotos();				
				    for(String x : photos) 
				    {
				    	File photoMetadata = new File();
				        photoMetadata.setParents(Collections.singletonList(folderId));
				        java.io.File filePath = new java.io.File(x);
				        FileContent mediaContent = new FileContent("image/jpeg", filePath);
				        status.getItems().add("Uploading " + x);
				        try {
							driveService.files().create(photoMetadata, mediaContent).execute();
						} catch (IOException e) {
							e.printStackTrace();
						}
				        
				        status.getItems().add("File uploaded!");
		
				    	
				    }
				    // Next, send out the email with the link to the folder
				    status.getItems().add("Finished " + name + " folder, sending email!");
				    try {
						sendGmail(gmailService, recip, link);
					} catch (MessagingException | IOException e) {
						e.printStackTrace();
					}	
				    status.getItems().add("Sent!");
				    status.getItems().add("");
				}	
				status.getItems().add("All Done!");
			}
		});
		
		launchBtnHBox = new HBox(20, launchBtn);
		launchBtnHBox.setAlignment(Pos.CENTER);
		
		
		brd = new BorderPane();

		brd.setTop(newRecipientVBox);
		brd.setCenter(recipientsHBox);
		brd.setBottom(launchBtnHBox);

		scene = new Scene(brd, 800, 800);
		
		
				
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	/**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     * From Google Drive API QuickStart sample.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    private void sendGmail(Gmail gmailService, Recipient recipient, String link) throws AddressException, MessagingException, IOException
    {
    	// Create Email
        
        String to = recipient.getEmail();
        String from = "";
        String subject = "Here are your photos";
        String bodyText = "Hello " + recipient.getName() + ", here is the link to your photos \n" + link + "\n\nThese photos "
        		+ "were delivered automatically, via an application created by Michael Ahmadi.";
        Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		MimeMessage email = new MimeMessage(session);
		
		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
		new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);
		
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        
    	message = gmailService.users().messages().send("me", message).execute();
    }
}
