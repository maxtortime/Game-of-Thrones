package taehwan;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;


@SuppressWarnings("serial")
public class Drawing extends PApplet {
	Table table;
	ArrayList<Record> records = new ArrayList<Record>();

	
	final int w = 50; // 사각형의 크기
	final int d = 50; // 사각형간의 차이
	final int rbx = 110; // 첫 사각형의 x 좌표 
	final int rectNum = 30;
	final int weekNum = 408; // 51 * (year)
	final int wInterval = 3;
	
	float zoom = 0.226f;
	int MAXWEEK,MINWEEK;
	
	Camera worldCamera;
	Set<String> nameSet = new LinkedHashSet<String>();
	Set<String> genreSet = new LinkedHashSet<String>();
	Set<String> platformSet = new LinkedHashSet<String>();
	Set<String> whenYearSet = new LinkedHashSet<String>();
	
	ArrayList<Table> tables = new ArrayList<Table>();
	
	LinkedHashMap<Integer,Table> subRecords = new LinkedHashMap<Integer,Table>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,Integer> nameColor = new LinkedHashMap<String,Integer>(); // 게임 제목마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> genreColorMap = new LinkedHashMap<String,Integer>(); // 게임 장르마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> whenYearColorMap = new LinkedHashMap<String, Integer>();
	LinkedHashMap<String,Integer> platformColor = new LinkedHashMap<String,Integer>(); // 게임 플랫폼마다 색깔을 담는 Map
	LinkedHashMap<Integer,Integer> maxWeekByRank = new LinkedHashMap<Integer, Integer>();
	
	Random forColor = new Random(); // color 를 랜덤으로 주기 위해서
	
	int UiColor = color(94,31,47);
	int color = 0;
	final int xBase = 0;
	final int yBase = -110;
	boolean iswhenChangeClicked = false;
	boolean isGenreChangeClicked = false;
	
	public void setup() {
		size(1920,960);
		
		table = loadTable("../all.csv","header");
		Table genrecolor = loadTable("../genrecolor.csv","header");
		Table yearcolor = loadTable("../yearcolor.csv","header");
		/*
		for (int year=2007 ; year < 2012 ; year++) {
			String filename = "../" + year + "_.csv";
			tables.add(loadTable(filename,"header"));
		}
		*/
		//table = tables.get(0);
		
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
				whenYearSet.add(row.getString("whenyear"));
				
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
			
			//records.getValue().sort("when");
			
			for (int i = 0 ; i < records.getValue().getRowCount() ; i++)
				records.getValue().getRow(i).setInt("pos", i);
			
			for (TableRow row : records.getValue().rows()) {
				namePos.get(row.getString("name"))[idx] = row.getInt("pos");
			}
			
			idx++;
		}
		
		for (String genre : genreSet) {
			TableRow rgb = genrecolor.findRow(genre, "genrename");
			try {
				genreColorMap.put(genre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
			} catch (NullPointerException e) {
				genreColorMap.put(genre, color(255,255,255,120));
			}
			
		}
		
		/*
		for (String platform : platformSet)
			platformColor.put(platform,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		*/
		for (String when : whenYearSet) {
			TableRow rgb = yearcolor.findRow(when, "year");
			
			whenYearColorMap.put(when,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		
		worldCamera = new Camera(xBase,yBase,zoom); 
	}
	
	public void draw() {
		noStroke();
		background(200);
		String when = "";
		String name = "";
		String genre = "";

		
		int dx = 0;
		int dy = 0;
	
		if (keyPressed) {
			if( key == 'o'){
				zoom *= 1.1;
				key = '1';
			}
			if(key == 'p'){
				zoom *= 0.9;
				key = '2';
			}
			// 카메라가 변해도 UI 가 변하게 하지 않기 위해서
			if (key == 'w') dy += 50; 
			if (key == 's') dy -= 50; 
		    if (key == 'a') dx += 50; 
		    if (key == 'd') dx -= 50;
		}
		
		
		translate(-worldCamera.pos.x, -worldCamera.pos.y);
		worldCamera.draw();
		scale(zoom);
		
		
		for (Entry<Integer, Table> each : subRecords.entrySet()) {
			int wn = each.getKey();
			
			Table sub = each.getValue();

			for (TableRow row : sub.rows()) {
				int pos = row.getInt("pos");
				when = row.getString("whenyear");
				name = row.getString("name");
				genre = row.getString("genre");
				/*
				if (!iswhenChangeClicked)
					color = nameColor.get(name);
				else 
					color = whenYearColorMap.get(when);
				
				if(!isGenreChangeClicked)
					color = nameColor.get(name);
				else 
					color = whenYearColorMap.get(genre);
				*/
				if(isGenreChangeClicked)
					color = genreColorMap.get(genre);
				else if(iswhenChangeClicked)
					color = whenYearColorMap.get(when);
				else
					color = nameColor.get(name);
			
				//int color = genreColorMap.get(genre);
				int nextPos = namePos.get(name)[wn];
				
				//int rX = rbx+w*(wn*wInterval-2);
				int rX = rbx+(wn-1)*(wInterval*w-1);
				int rY = rbx+pos*(w+d);
				/*
				fill(0);
				// name is too long
				textSize(12);
				if (name.length()<15)
					text(name,rbx+w*(wn*wInterval-2),rbx+pos*(w+d));
				else
					text(name.substring(0,7)+"...",rbx+w*(wn*wInterval-2),rbx+pos*(w+d));
				*/
				noStroke();
				fill(color);
				rect(rX,rY,w,w);
				if (nextPos != -1) {
					strokeWeight(5);
					stroke(color);
				
					line(rX+w,rY+w/2,rbx+wn*(wInterval*w-1),rbx+nextPos*(w+d)+w/2);
				}
			}
		}
		
		// UI 부분으로 카메라의 적용을 받지 않음
		// 모든 좌표에 항상 카메라의 좌표와 dx,dy 를 각각 더해줘야함
		scale(1/zoom);
			fill(UiColor);
				//위 사각형
				float upperRectX = worldCamera.pos.x+dx;
				float upperRectY = worldCamera.pos.y+dy;
				
    			rect(upperRectX,upperRectY,width,100);
    			// 아래 사각형
    			float downRectX = worldCamera.pos.x+dx;
				float downRectY = worldCamera.pos.y+dy+height-100;
    			rect(downRectX,downRectY,width,100);
    			
    			//whenChange click
    			Rectangle whenChange = new Rectangle((int) downRectX+200,(int) downRectY+25,100,50);
    			
    			whenChange.x-=worldCamera.pos.x;
    			whenChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && whenChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				iswhenChangeClicked = true;
    				//redraw();
    			}
    			else {
    				fill(0,255,0);
    				iswhenChangeClicked = false;
    			}
    			//whenChange	
    			rect(downRectX+200,downRectY+25,100,50);
    			
    			//genreChange click
    			Rectangle genreChange = new Rectangle((int) downRectX+350,(int) downRectY+25,100,50);
    			genreChange.x-=worldCamera.pos.x;
    			genreChange.y-=worldCamera.pos.y;
    			
    			//genreChange	

    			if (mousePressed && genreChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				isGenreChangeClicked = true;
    				//redraw();
    			}
    			else {
    				fill(0,255,0);
    				isGenreChangeClicked = false;
    			}
    			rect(downRectX+350,downRectY+25,100,50);
			fill(255);
				textSize(50);
				text("GAME OF THORNES",worldCamera.pos.x+dx+110,worldCamera.pos.y+dy+65);

	}
	
	class Camera { 
		  PVector pos; //Camera's position  
		  //The Camera should sit in the top left of the window
		  float basicZoom = 0;
		  float basicX = 0;
		  float basicY = 0;
		  Camera() { 
		    pos = new PVector(0, 0); 
		    //You should play with the program and code to see how the staring position can be changed 
		  } 
		  public Camera(int x, int y) {
			 pos = new PVector(x,y);
			 basicX = x;
			 basicY = y;
		  }
		  public Camera(int x, int y,float uZoom) {
				 pos = new PVector(x,y);
				 basicX = x;
				 basicY = y;
				 basicZoom = uZoom;
				
			  }
		void draw() { 
		    //I used the mouse to move the camera 
		    //The mouse's position is always relative to the screen and not the camera's position 
		    //E.g. if the mouse is at 1000,1000 then the mouse's position does not add 1000,1000 to keep up with the camera

		  
		    //I noticed on the web the program struggles to find the mouse so I made it key pressed 

		    if (keyPressed) { 
		    	
		      if (key == 'w') 
		    	  pos.y -= 50; 
		      if (key == 's') 
		    	  pos.y += 50; 
		      if (key == 'a') 
		    	  pos.x -= 50; 
		      if (key == 'd') 
		    	  pos.x += 50; 
		      if(key == 'e'){
		    	  pos.x = basicX;
		    	  pos.y = basicY;
		    	  zoom = basicZoom;
		      }
		    } 
		  }
	}  

}


