package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

public class UserData {

	private String id = null;
	private Connection con = null;
	private Statement stm = null;
	private boolean login = false;
	private final int range = 100;
	private final int adRange = 1000;

	public UserData() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection("jdbc:mysql://snsdatabase.cd4cgficwte9.ap-northeast-1.rds.amazonaws.com:3306/SNS_USER?useUnicode=true&characterEncoding=utf8", "ec2_user", "mjfeodmd0");
		stm = con.createStatement();

	}

	public String getId(){
		return this.id;
	}

	public UserData(String id, String pass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, LoginException{
		this();
		this.login(id, pass);
	}

	public void signin(String id, String name, double latitude, double longitude) throws LoginException, SQLException{

		ResultSet rs = stm.executeQuery("SELECT id FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		try{

			rs.getString(1);

		}catch(SQLException e){

			String pass = Mailer.createPass();
			Mailer.sendRegistrationMail(pass, id);
			stm.executeUpdate("INSERT INTO SNS_USER.Main VALUE('" + id + "', '" + pass + "', '" + name + "', " + latitude + ", " + longitude + ", '', " + "100" +  ")");

		}

		rs.close();

	}

	public void resetPass(String id) throws SQLException{

		String pass = Mailer.createPass();
		stm.executeUpdate("UPDATE SNS_USER.Main SET password = '" + pass + "' WHERE id = '" + id + "'");
		Mailer.sendResetPassMail(pass, id);

	}

	public void login(String id, String pass) throws SQLException, LoginException{

		ResultSet rs = stm.executeQuery("SELECT password FROM SNS_USER.Main WHERE id = '" + id + "'");
		rs.next();

		if(rs.getString(1).equals(pass)){

			this.id = id;
			this.login = true;

		}

		if(!this.isLogin())
			throw new LoginException("you couldn't login");

		rs.close();
	}

	public boolean isLogin(){

		return this.login;

	}

	public void logout(){

		this.id = null;
		this.login = false;

	}

	public void unsubscride() throws SQLException{

		stm.executeUpdate("DELETE FROM SNS_USER.Main WHERE id = '" + this.id + "'");
		stm.executeUpdate("DELETE FROM SNS_USER.Contribution WHERE id = '" + this.id + "'");
		this.close();
	}

	public void close() throws SQLException{

		this.logout();

		stm.close();
		stm = null;

		con.close();
		con = null;

	}

	public void updatePlace(double latitude, double longitude) throws SQLException{

		if(!login) return;

		stm.executeUpdate("UPDATE SNS_USER.Main SET latitude = " + latitude + " WHERE id = '" + this.id + "'");
		stm.executeUpdate("UPDATE SNS_USER.Main SET longitude = " + longitude + " WHERE id = '" + this.id + "'");

	}

	public void changeName(String name) throws SQLException{

		if(!login) return;

		stm.executeUpdate("UPDATE SNS_USER.Main SET name = '" + name + "' WHERE id = '" + this.id + "'");

	}

	public void changePass(String pass) throws SQLException{

		if(!login) return;

		stm.executeUpdate("UPDATE SNS_USER.Main SET password = '" + pass + "' WHERE id = '" + this.id + "'");

	}

	public void setProfile(String profile) throws SQLException{

		if(!login) return;

		stm.executeUpdate("UPDATE SNS_USER.Main SET profile = '" + profile + "' WHERE id = '" + this.id + "'");

	}

	public void contribution(String contribution) throws SQLException{

		if(!login) return;

		int number;

		ResultSet rs = stm.executeQuery("SELECT MAX(number) FROM SNS_USER.Contribution WHERE id = '" + id + "'");

		rs.next();

		try{

			number = rs.getInt(1) + 1;

		}catch(SQLException e){

			number = 1;

		}

		rs.close();

		rs = stm.executeQuery("SELECT * FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		rs.close();

		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

		stm.executeUpdate("INSERT INTO SNS_USER.Contribution VALUE(" + number + ", '" + id +"', '" + contribution + "', cast('" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "' as datetime))");

	}

	public void deleteContribution(String date) throws SQLException{

		stm.executeUpdate("DELETE FROM SNS_USER.Contribution WHERE id = '" + this.id + "' AND date = '" + date + "'");
	}

	public ArrayList<ContributionData> getTimeLine() throws SQLException{

		if(!login) return null;

		ArrayList<ContributionData> list = new ArrayList<ContributionData>();

		MainUserData data = this.getUserData();

		ArrayList<MainUserData> dali = getWatchableUser(data.getLatitude(), data.getLongitude(), data.getRange());

		for(int i = 0; i < dali.size(); i++){

			list.addAll(getContributionList(dali.get(i).getId()));
			list.addAll(getMeetingList(dali.get(i).getId()));
		}

		sort(list);

		return list;

	}

	public ArrayList<ContributionData> getTimeLine(String id) throws SQLException{

		if(!login) return null;

		ArrayList<ContributionData> list = new ArrayList<ContributionData>();

		list.addAll(this.getContributionList(id));
		list.addAll(this.getMeetingList(id));

		sort(list);

		return list;

	}

	protected ArrayList<MainUserData> getWatchableUser(double latitude, double longitude, int range) throws SQLException{

		ResultSet rs = stm.executeQuery("SELECT * FROM SNS_USER.Main WHERE ABS((latitude - " + latitude + ") * 60 * 1852.25) <= " + range + " AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= " + range  + " AND ABS((latitude - " + latitude + ") * 60 * 1852.25) <= `range` AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= `range`");
		ArrayList<MainUserData> list = new ArrayList<>();

		rs.last();
		int length = rs.getRow();
		rs.first();

		for(int i = 0; i < length; i++){
			list.add(new MainUserData(rs.getString(1), rs.getString(3), rs.getDouble(5), rs.getDouble(4), rs.getString(6), rs.getInt(7)));

			rs.next();
		}

		return list;

	}

	protected ArrayList<ContributionData> getContributionList(String id, String where) throws SQLException{

		ResultSet rs = stm.executeQuery("SELECT name FROM SNS_USER.Main WHERE id = '" + id + "'");
		rs.next();
		String name = rs.getString(1);
		rs.close();

		rs = stm.executeQuery("SELECT * FROM SNS_USER.Contribution WHERE id = '" + id + "' ORDER BY date DESC" + " " + where);
		ArrayList<ContributionData> list = new ArrayList<>();

		rs.last();
		int length = rs.getRow();
		rs.first();

		for(int i = 0; i < length; i++){

			list.add(new ContributionData(rs.getString(2), name, rs.getString(3), rs.getString(4)));

			rs.next();
		}

		return list;

	}

	protected ArrayList<ContributionData> getContributionList(String id) throws SQLException{

		return this.getContributionList(id, "");

	}

	protected ArrayList<ContributionData> getMeetingList(String id) throws SQLException{

		ResultSet rs = stm.executeQuery("SELECT name FROM SNS_USER.Main WHERE id = '" + id + "'");
		rs.next();
		String name = rs.getString(1);
		rs.close();

		rs = stm.executeQuery("SELECT about, date, title, location, eventdate, number FROM SNS_USER.Event WHERE id = '" + id + "' ORDER BY date DESC");

		rs.last();
		int length = rs.getRow();
		rs.first();

		Statement s = con.createStatement();
		ResultSet sub;
		int me = 0;
		ArrayList<ContributionData> list = new ArrayList<>();

		for(int i = 0; i < length; i++){

			sub = s.executeQuery("select OnUser, UndecidedUser, CancelUser FROM SNS_USER.Event" + rs.getInt(6) + " WHERE UserId='" + id + "'");

			if(sub.first()) for(int j = 1; j < 4; j++) if(sub.getInt(j) == 1) me = j;

			sub = s.executeQuery("select SUM(OnUser), SUM(UndecidedUser), SUM(CancelUser) FROM SNS_USER.Event" + rs.getInt(6));
			sub.next();
			list.add(new MeetingData(id, name, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), sub.getInt(1), sub.getInt(2), sub.getInt(3), me));

			sub.close();

			rs.next();

		}
		return list;

	}

	public String getDifContribution(String date, String otherIdList) throws SQLException {

		ArrayList<String> userName = new ArrayList<>();

		String[] List = otherIdList.split(", ");

		for(int i = 0; i < List.length; i++){

			List[i] = List[i].substring(1, List[i].length() - 1);

		}

		List<String> droList = Arrays.asList(List);

		ResultSet rs = null;
		double latitude, longitude;

		rs = stm.executeQuery("SELECT latitude, longitude FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		latitude = rs.getDouble(1);
		longitude = rs.getDouble(2);

		rs.close();


		rs = stm.executeQuery("SELECT * FROM SNS_USER.Main WHERE ABS((latitude - " + latitude + ") * 60 * 1852.25) <= " + range + " AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= " + range  + " AND ABS((latitude - " + latitude + ") * 60 * 1852.25) <= `range` AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= `range`");

		do{

			rs.next();

			userName.add(rs.getString(1));

		}while(!rs.isLast());

		rs.close();


		StringBuilder sb = new StringBuilder();

		ArrayList<String> deleteUser = incomeDifferenceList(droList, userName);

		sb.append("deleteUser, ");

		for(String a: deleteUser){
			sb.append("\"");
			sb.append(a);
			sb.append("\", ");
		}


		sb.append("\n");

		ArrayList<String> addUser = incomeDifferenceList(userName, droList);

		sb.append("addUser, ");

		for(String a: addUser){
			sb.append("\"");
			sb.append(a);
			sb.append("\", ");
		}


		sb.append("\ncontribution, \n");

		ArrayList<ContributionData> list = new ArrayList<ContributionData>();

		Statement s = con.createStatement();


		int j = 0;
		for(int i = 0; i < addUser.size(); i++){

			rs = stm.executeQuery("SELECT name FROM SNS_USER.Main WHERE id = '" + addUser.get(i) + "'");

			rs.first();

			ResultSet r = s.executeQuery("SELECT contribution, date FROM SNS_USER.Contribution WHERE id = '" + addUser.get(i) + "'" + " AND `date` >= cast('" + date + "' as datetime)");

			//System.out.println(addUser.get(i));
			while(r.next() && !r.isLast()){
				list.add(new ContributionData(addUser.get(i), rs.getString(1), r.getString(1), r.getString(2)));
				sb.append(list.get(j).toString());
				sb.append("\n");
				j++;
			}

			rs.close();
			r.close();

		}

		s.close();

		return sb.toString();
	}

	public void sort(ArrayList<ContributionData> list){

		for(int i = 0; i < list.size() - 1; i++){

			for(int j = list.size() - 1; i < j; j--){

				if(list.get(j).getDate().compareTo(list.get(j - 1).getDate()) > 0){

					ContributionData d;

					d = list.get(j);
					list.set(j, list.get(j - 1));
					list.set(j - 1, d);

				}

			}

		}

	}

	public MainUserData getUserData(String id) throws SQLException{

		ResultSet rs = stm.executeQuery("SELECT * FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		String profile = rs.getString(6);

		if(profile == null)
			profile = "";

		MainUserData data = new MainUserData(rs.getString(1), rs.getString(3), rs.getDouble(5), rs.getDouble(4), profile, rs.getInt(7));

		return data;
	}

	public MainUserData getUserData() throws SQLException{

		return getUserData(id);
	}

	public int getNumberOfPeople() throws SQLException{

		ResultSet rs = null;
		double latitude, longitude;

		rs = stm.executeQuery("SELECT latitude, longitude FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		latitude = rs.getDouble(1);
		longitude = rs.getDouble(2);

		rs.close();

		rs = stm.executeQuery("SELECT COUNT(*) FROM SNS_USER.Main WHERE ABS((latitude - " + latitude + ") * 60 * 1852.25) <= " + range + " AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= " + range  + " AND ABS((latitude - " + latitude + ") * 60 * 1852.25) <= `range` AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= `range`");

		rs.next();

		return rs.getInt(1);

	}

	public String getAroundUsers() throws SQLException{

		ResultSet rs = null;
		double latitude, longitude;

		rs = stm.executeQuery("SELECT latitude, longitude FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		latitude = rs.getDouble(1);
		longitude = rs.getDouble(2);

		rs.close();

		rs = stm.executeQuery("SELECT id FROM SNS_USER.Main WHERE ABS((latitude - " + latitude + ") * 60 * 1852.25) <= " + range + " AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= " + range  + " AND ABS((latitude - " + latitude + ") * 60 * 1852.25) <= `range` AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= `range`");

		StringBuilder sb = new StringBuilder();

		do{

			rs.next();
			sb.append("\"");
			sb.append(rs.getString(1));
			sb.append("\", ");

		}while(!rs.isLast());

		return sb.toString();

	}

	public String getIcon(String id) throws IOException{

		BufferedReader reader;

		reader = new BufferedReader(new FileReader("/usr/share/tomcat7/webapps/SNS/WEB-INF/icon/" + id + ".txt"));

		StringBuilder res = new StringBuilder();

		while(reader.ready())
			res.append((char)reader.read());

		reader.close();

		return res.toString();

	}

	public String getImage(String id, String date) throws IOException{

		BufferedReader reader;

		reader = new BufferedReader(new FileReader("/usr/share/tomcat7/webapps/SNS/WEB-INF/image/" + id + date + ".txt"));

		StringBuilder res = new StringBuilder();

		while(reader.ready())
			res.append((char)reader.read());

		reader.close();

		return res.toString();

	}

	private static ArrayList<String> incomeDifferenceList(final List<String> x, final List<String> y){

		boolean flag = false;

		ArrayList<String> list = new ArrayList<>();

		for(String b: x){
			for(String a: y)
				if(a.equals(b)){
					flag = true;
				}
			if(!flag){
				list.add(b);
			}else{
				flag = false;
			}
		}

		return list;

	}

	public void event(String title, String location, String date, String about) throws SQLException {

		if(!login) return;

		int number;

		ResultSet rs = stm.executeQuery("SELECT MAX(number) FROM SNS_USER.Event");

		rs.next();

		try{

			number = rs.getInt(1) + 1;

		}catch(SQLException e){

			number = 1;

		}

		rs.close();

		rs = stm.executeQuery("SELECT * FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		rs.close();

		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

		stm.execute("CREATE TABLE SNS_USER.Event" + number + " (UserId char(45) NOT NULL, OnUser INT(1), UndecidedUser INT(1), CancelUser INT(1))");
		stm.executeUpdate("INSERT INTO SNS_USER.Event VALUE(" + number + ", '" + id +"', '" + title + "', '" + location + "', " +  "cast('" + date + "' as datetime), " + "'" + about + "', "+ "cast('" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "' as datetime))");

	}

	public void vote(String date, String cont) throws SQLException{

		int on = 0, un = 0, can = 0;

		switch(cont){
		case "on":
			on = 1;
			break;
		case "undecided":
			un = 1;
			break;
		case "cancel":
			can = 1;
			break;
		}


		ResultSet rs = stm.executeQuery("SELECT number FROM SNS_USER.Event WHERE date = '" + date + "'");
		rs.next();

		int num = rs.getInt(1);

		stm.execute("DELETE FROM SNS_USER.Event" + num + " WHERE UserId='" + id + "'");
		stm.executeUpdate("INSERT INTO SNS_USER.Event" + num + " VALUE('" + id + "', " + on + ", " + un + ", " + can + ")");

	}

	public void deleteEvent(String date) throws SQLException {
		ResultSet rs = stm.executeQuery("SELECT number FROM SNS_USER.Event WHERE date='" + date + "'");
		rs.next();
		int num = rs.getInt(1);
		stm.execute("DELETE FROM SNS_USER.Event WHERE date='" + date + "'");
		stm.execute("DROP TABLE SNS_USER.Event" + num);
	}

	public String getParticipantsList(String date) throws SQLException{


		ResultSet rs = stm.executeQuery("SELECT number FROM SNS_USER.Event WHERE date='" + date + "'");
		rs.next();
		int num = rs.getInt(1);
		StringBuilder res = new StringBuilder();

		rs = stm.executeQuery("SELECT UserId FROM SNS_USER.Event" + num + " WHERE OnUser = 1");

		Statement s = con.createStatement();
		ResultSet r;

		if(rs.first())
		while(!rs.isAfterLast()){
			r = s.executeQuery("SELECT name FROM SNS_USER.Main WHERE id='" + rs.getString(1) + "'");
			r.next();
			res.append("\"\"");
			res.append(rs.getString(1));
			res.append("\", \"");
			res.append(r.getString(1));
			res.append("\"\"");
			if(!rs.isLast())
				res.append(",");
			rs.next();
		}
		res.append("\n");

		return res.toString();
	}

	public ArrayList<ContributionData> getAd() throws SQLException{

		ArrayList<ContributionData> list = new ArrayList<ContributionData>();
		ResultSet rs = null;
		double latitude, longitude;

		rs = stm.executeQuery("SELECT latitude, longitude FROM SNS_USER.Main WHERE id = '" + id + "'");

		rs.next();

		latitude = rs.getDouble(1);
		longitude = rs.getDouble(2);

		rs.close();

		rs = stm.executeQuery("SELECT id, name FROM SNS_USER.Advertisers WHERE ABS((latitude - " + latitude + ") * 60 * 1852.25) <= " + adRange + " AND ABS((longitude - " + longitude + ") * 60 * 1519.85) <= " + adRange );

		rs.next();

		Statement s = con.createStatement();

		ResultSet r;

		do{

			r = s.executeQuery("SELECT contribution, date FROM SNS_USER.Ad WHERE id = '" + rs.getString(1) + "'");

			if(rs.getString(1).equals("null")) continue;

			do{

				if(!r.next()) break;

				list.add(new AdvertisementData(rs.getString(1), rs.getString(2), r.getString(1), r.getString(2)));

			}while(!r.isLast());

			rs.next();

		}while(!rs.isAfterLast());

		s.close();

		sort(list);

		return list;
	}

	public void contact(String id, String contact) throws MessagingException{
		Mailer.sendMail("【お問い合わせ】" + id, contact, "dondo.info@gmail.com");
	}

	public void changeRange(String range) throws SQLException {

		stm.executeUpdate("UPDATE SNS_USER.Main SET `range` = " + range + " WHERE id = '" + this.id + "'");

	}

}
