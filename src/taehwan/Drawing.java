package taehwan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntDict;
import processing.data.Table;
import processing.data.TableRow;
import controlP5.*;

@SuppressWarnings("serial")
public class Drawing extends PApplet {
	Table table;
	ArrayList<Record> records = new ArrayList<Record>();
	
	final int w = 50;
	final int d = 50; // 사각형간의 차이
	final int rbx = 20; 
	final int rectNum = 30;
	final int weekNum = 52;
	final int wRatio = 2;
	
	float zoom = 1;
	int MAXWEEK,MINWEEK;
	
	Camera worldCamera;
	Set<String> nameSet = new LinkedHashSet<String>();
	Set<String> genreSet = new LinkedHashSet<String>();
	Set<String> platformSet = new LinkedHashSet<String>();
	
	ArrayList<Table> tables = new ArrayList<Table>();
	
	LinkedHashMap<Integer,Table> subRecords = new LinkedHashMap<Integer,Table>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,Integer> nameColor = new LinkedHashMap<String,Integer>(); // 게임 제목마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> genreColor = new LinkedHashMap<String,Integer>(); // 게임 장르마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> platformColor = new LinkedHashMap<String,Integer>(); // 게임 플랫폼마다 색깔을 담는 Map
	LinkedHashMap<Integer,Integer> maxWeekByRank = new LinkedHashMap<Integer, Integer>();
	
	Random forColor = new Random(); // color 를 랜덤으로 주기 위해서
	
	ControlP5 cp5;
	Accordion accordion;
	IntDict rankPos;
	
	
	public void setup() {
		size(960,960);
		//table = loadTable("../2007_.csv","header");
		
		for (int year=2007 ; year < 2012 ; year++) {
			String filename = "../" + year + "_.csv";
			tables.add(loadTable(filename,"header"));
		}
		
		table = tables.get(0);
		MINWEEK = table.getIntList("week").min();
		MAXWEEK = table.getIntList("week").max();
		
		// Table 을 30개씩 쪼개기 위함
		for (int week = 1 ; week < weekNum ; week++) {
			int end = rectNum * week - 1;

			Table subTable = new Table();
			
			for (int col = 0 ; col < table.getColumnCount() ; col++)
				subTable.addColumn(table.getColumnTitle(col));
			
			for (int start = rectNum*(week-1); start < end ; start++) {
				TableRow row = table.getRow(start);
				
				subTable.addRow(row);
				
				nameSet.add(row.getString("name"));
				genreSet.add(row.getString("genre"));
				platformSet.add(row.getString("platform"));
				
				records.add(new Record(row));
				
			}
			subRecords.put(week,subTable);
		}
		
		// 이름 별로 랭킹을 담는 역할
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			Arrays.fill(positions, -1);
			
			namePos.put(name, positions);
			nameColor.put(name, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		}
		
		int idx = 0;
		
		for (Entry<Integer,Table> records : subRecords.entrySet()) {
			//records.getValue().sort("platform");
			//records.getValue().sort("genre");
			
			for (int i = 0 ; i < records.getValue().getRowCount() ; i++)
				records.getValue().getRow(i).setInt("pos", i);
			
			for (TableRow row : records.getValue().rows()) {
				namePos.get(row.getString("name"))[idx] = row.getInt("pos");
			}
			
			idx++;
		}
		
		for (String genre : genreSet)
			genreColor.put(genre,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		
		for (String platform : platformSet)
			platformColor.put(platform,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));

		worldCamera = new Camera(); 
	}
	
	public void draw() {
		noStroke();
		background(200);
		
		if( key == 'o'){
			zoom *= 1.1;
			key = '1';
		}
		if(key == 'p'){
			zoom *= 0.9;
			key = '2';
		}

		translate(-worldCamera.pos.x, -worldCamera.pos.y); 
		worldCamera.draw();
		scale(zoom);
		
		
		for (Entry<Integer, Table> each : subRecords.entrySet()) {
			int wn = each.getKey();
			
			Table sub = each.getValue();

			for (TableRow row : sub.rows()) {
				int pos = row.getInt("pos");
				String name = row.getString("name");
				
				int nextPos = namePos.get(name)[wn];
				int color = nameColor.get(name);
				
				fill(0);
				// name is too long
				if (name.length()<15)
					text(name,rbx+w*(wn*2-2),rbx+pos*(w+d));
				else
					text(name.substring(0,7)+"...",rbx+w*(wn*2-2),rbx+pos*(w+d));
				
				fill(color);
				
				rect(rbx+w*(wn*2-2),rbx+pos*(w+d),w,w);
				//rect(rbx+w*(wn*2-2),rbx+maxH+pos*d,w,h);
				
				if (nextPos != -1)
					quad(rbx+w*(wn*2-1),rbx+pos*(w+d),
						rbx+w*wn*2,rbx+nextPos*(w+d),
						rbx+w*wn*2,rbx+w+nextPos*(w+d),
						rbx+w*(wn*2-1),rbx+pos*(w+d)+w);
			}
		}
	}
	
	class Camera { 
		  PVector pos; //Camera's position  
		  //The Camera should sit in the top left of the window
		  
		  Camera() { 
		    pos = new PVector(0, 0); 
		    //You should play with the program and code to see how the staring position can be changed 
		  } 
		  void draw() { 
		    //I used the mouse to move the camera 
		    //The mouse's position is always relative to the screen and not the camera's position 
		    //E.g. if the mouse is at 1000,1000 then the mouse's position does not add 1000,1000 to keep up with the camera

		  
		    //I noticed on the web the program struggles to find the mouse so I made it key pressed 

		    if (keyPressed) { 
		      if (key == 'w') pos.y -= 50; 
		      if (key == 's') pos.y += 50; 
		      if (key == 'a') pos.x -= 50; 
		      if (key == 'd') pos.x += 50; 
		      if(key == 'e'){
		    	  pos.x = 0;
		    	  pos.y = 0;
		    	  zoom = 1;
		      }
		    } 
		  } 
	}  
}


