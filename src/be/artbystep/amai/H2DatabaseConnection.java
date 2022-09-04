package be.artbystep.amai;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

public class H2DatabaseConnection {
	public static void main(String[] args) throws Exception {

		Properties props = new Properties();
		File f = new File("amai.properties");
		try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
			props.load(is);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String authenticationFile = props.getProperty("database.authentication.file");
		String url = props.getProperty("database.url");
		String user;
		String password;

		try (FileReader fis = new FileReader(new File(authenticationFile));
				BufferedReader br = new BufferedReader(fis)) {
			user = br.readLine();
			password = br.readLine();
		}

		Connection c = DriverManager.getConnection(url, user, password);
		password = null;
		createTables(c);
		insertDummyData(c);
		testRecordCount(c);
		System.out.println("done");
	}

	private static void testRecordCount(Connection c) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement("select count(*) from orders;")) {
			ResultSet rs = ps.executeQuery();
			rs.next();
			System.out.println(rs.getLong(1));
		}

	}

	private static void insertDummyData(Connection c) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement("insert into orders values ( ?, ?, ?, ?, ?)")) {
			for (int i = 0; i < 10000; i++) {
				ps.setString(1, String.format("ORDERNUMBER %4d", i));
				ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				ps.setString(3, "dummytestadres1234567890@invalid.com");
				ps.setString(4, "BORGERHOUT");
				ps.setString(5, "onverwerkt");
				ps.executeUpdate();
			}
		}

	}

	private static void createTables(Connection c) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement(
				"create table if not exists orders (ordernumber VARCHAR(60) NOT NULL PRIMARY KEY, creationdate TIMESTAMP without time zone, emailaddress VARCHAR(300), item VARCHAR(300), status VARCHAR(100))")) {
			ps.execute();
		}
	}

	public void cleanUpOldData() {
		// TODO
	}

}
