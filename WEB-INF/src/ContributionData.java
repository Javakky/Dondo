package server;

public class ContributionData{

	private String id = null;
	private String contribution = null;
	private String name = null;
	private String date = null;

	ContributionData(String id, String name, String contribution, String date){

		this.id = id;
		this.name = name;
		this.contribution = contribution;
		this.date = date;

	}

	public String getId(){
		return this.id;
	}

	public String getName(){
		return this.name;
	}

	public String getContribution(){
		return this.contribution;
	}

	public String getDate(){
		return this.date;
	}

	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();

		sb.append("\"");
		sb.append(this.id);
		sb.append("\", \"");
		sb.append(this.name);
		sb.append("\", \"");
		sb.append(this.contribution);
		sb.append("\", \"");
		sb.append(this.date);
		sb.append("\"");

		return sb.toString();
	}
}