package server;

public class MeetingData extends ContributionData {

	private String title;
	private String location;
	private String eventDate;
	private int on;
	private int undecided;
	private int cancel;
	private int myChoice;

	MeetingData(String id, String name, String contribution, String date, String title, String location, String eventDate, int on, int undecided, int cancel, int myChoice) {
		super(id, name, contribution, date);
		this.title = title;
		this.location = location;
		this.eventDate = eventDate;
		this.on = on;
		this.undecided = undecided;
		this.cancel = cancel;
		this.myChoice = myChoice;
	}

	public String getTitle(){
		return this.title;
	}

	public String getLocation(){
		return this.location;
	}

	public String getEventDate(){
		return this.eventDate;
	}

	public int getOn(){
		return this.on;
	}

	public int getUndecided(){
		return this.undecided;
	}

	public int getCancel(){
		return this.cancel;
	}

	public int getMyChoice(){
		return this.myChoice;
	}

	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();

		sb.append(super.toString());
		sb.append(", \"");
		sb.append(this.title);
		sb.append("\", \"");
		sb.append(this.location);
		sb.append("\", \"");
		sb.append(this.eventDate);
		sb.append("\", \"");
		sb.append(this.on);
		sb.append("\", \"");
		sb.append(this.undecided);
		sb.append("\", \"");
		sb.append(this.cancel);
		sb.append("\", \"");
		sb.append(this.myChoice);
		sb.append("\"");

		return sb.toString();
	}

}
