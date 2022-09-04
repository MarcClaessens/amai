package be.artbystep.amai;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;

public class TestEmailProcessor {
	public static void check(Properties properties) {
		try {

			Session emailSession = Session.getDefaultInstance(properties);

			// create the imap store object and connect to the imap server
			Store store = emailSession.getStore("imaps");

			String authenticationFile = properties.getProperty("mail.authentication.file");
			String userEmail;
			String password;

			try (FileReader fis = new FileReader(new File(authenticationFile));
					BufferedReader br = new BufferedReader(fis)) {
				userEmail = br.readLine();
				password = br.readLine();
			}

			store.connect(properties.getProperty("mail.imap.host"), userEmail, password);
			password = null;

			// create the inbox object and open it
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_WRITE);

			// retrieve the messages from the folder in an array and print it
			Message[] messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			System.out.println("messages.length---" + messages.length);

			Message spotifyEmail = null;
			for (int i = 0, n = messages.length; i < n; i++) {
				Message message = messages[i];
				// message.setFlag(Flag.SEEN, true);
				/*
				 * System.out.println("---------------------------------");
				 * System.out.println("Email Number " + (i + 1));
				 */
				System.out.println("Subject: " + message.getSubject());
				/*
				 * System.out.println("From: " + message.getFrom()[0]);
				 *
				 * System.out.println("Text: " + message.getContent().toString());
				 */

				if (message.getSubject() != null && message.getSubject().startsWith("Bohemian Rhapsody")) {
					spotifyEmail = message;
				}
			}

			if (spotifyEmail == null) {
				System.out.println("Oeps... mail is weg of gemarkeerd als gelezen");
			} else {
				MimeMultipart content = (MimeMultipart) spotifyEmail.getContent();
				String htmlcontent = content.getBodyPart(0).getContent().toString();

				// quick and dirty remove of HTML tags
				int startPos = htmlcontent.indexOf('<');
				while (startPos > -1) {
					int endPos = htmlcontent.indexOf('>', startPos + 1);
					htmlcontent = htmlcontent.substring(0, startPos) + htmlcontent.substring(endPos + 1);
					startPos = htmlcontent.indexOf('<');
				}
				// remove multiple breaks
				htmlcontent.replace("\r\n\r\n", "\r\n");

				System.out.println(htmlcontent);
				// mark as unread (so we can run this test multiple times
				spotifyEmail.setFlag(Flag.SEEN, false);
			}

			inbox.close(false);
			store.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		Properties props = new Properties();
		File f = new File("amai.properties");
		try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
			props.load(is);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		check(props);
		System.out.println("done");
	}

}
