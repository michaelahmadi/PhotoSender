# PhotoSender
An application I wrote in Java to deliver photos using the Google Drive API and Gmail API

I wrote a simple and straightforward GUI using JavaFX. Here's a screenshot:

![Screenshot of PhotoSender GUI](screenshot.jpg)

It's pretty basic since I only made this application for personal use. 
First, a recipient folder is created using a name and an email, and the "Add Recipient" button.
Then, the GUI allows me to drag and drop photos onto the folder of the recipient I want them to be delivered to.
Lastly, after all the appropriate folders have been created and populated, I press the "Upload and Send" button.

The "Upload and Send" button will create the necessary folders on my Google Drive (or whosever Drive that logs in), upload the photos to their respective folders, and then send an email with each folder's sharing link to their respective recipient.

Finally my friends can receive their photos in a timely manner, and I can deliver them hassle free.