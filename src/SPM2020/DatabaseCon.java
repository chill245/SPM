package SPM2020;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseCon {
	public static void main(String args[]) {
		try {
			Connection conn = null;
			conn = DriverManager.getConnection("jdbc:mysql://localhost/spm_test", "root", "");
			System.out.print("Database is connected !");
			conn.close();
		} catch (Exception e) {
			System.out.print("Do not connect to DB - Error:" + e);
		}
	}
}