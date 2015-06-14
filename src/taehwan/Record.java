package taehwan;

import processing.data.TableRow;

public class Record {
	private int id,pos,week,total,time;
	private String name,platform,publisher,genre,esrb,date;
	
	public Record(TableRow r) {
		id = r.getInt("id");
		pos = r.getInt("pos");
		week = r.getInt("week");
		total = r.getInt("total");
		time = r.getInt("time");
		date = r.getString("date");
		name = r.getString("name");
		platform = r.getString("platform");
		publisher = r.getString("publisher");
		genre = r.getString("genre");
		esrb = r.getString("esrb");
	}

	public int getId() {
		return id;
	}

	public int getPos() {
		return pos;
	}

	public int getWeek() {
		return week;
	}

	public int getTotal() {
		return total;
	}

	public int getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public String getPlatform() {
		return platform;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getGenre() {
		return genre;
	}

	public String getEsrb() {
		return esrb;
	}
	

}
