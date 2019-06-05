package server;

public class MainUserData{

	private String name = null;
	private String id = null;
	private double longitude = 0;
	private double latitude = 0;
	private String profile = null;
	private int range = 100;

	MainUserData(String id, String name, double longitude, double latitude, String profile, int range){
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.profile = profile;
		this.range = range;
	}

	public String getId(){
		return this.id;
	}

	public String getName(){
		return this.name;
	}

	public double getLatitude(){
		return this.latitude;
	}

	public double getLongitude(){
		return this.longitude;
	}

	public String getProfile(){
		return this.profile;
	}

	public int getRange(){
			return this.range;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append("\"");
		sb.append(this.id);
		sb.append("\", \"");
		sb.append(this.name);
		sb.append("\", \" ");
		sb.append(this.latitude);
		sb.append("\", \"");
		sb.append(this.longitude);
		sb.append("\", \"");
		sb.append(this.profile);
		sb.append("\"");

		return sb.toString();
	}

}