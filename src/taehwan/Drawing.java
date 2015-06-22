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

	final int w = 50; // �簢���� ũ��
	final int d = 50; // �簢������ ����
	final int rbx = 110; // ù �簢���� x ��ǥ 
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
	Set<String> esrbSet = new LinkedHashSet<String>();
	
	ArrayList<Table> tables = new ArrayList<Table>();
	
	LinkedHashMap<Integer,Table> subRecords = new LinkedHashMap<Integer,Table>(); // ���� �̸����� �ֺ��� ��ŷ�� ��� �ִ� Map 
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // ���� �̸����� �ֺ��� ��ŷ�� ��� �ִ� Map 
	LinkedHashMap<String,Integer> nameColor = new LinkedHashMap<String,Integer>(); // ���� ���񸶴� ������ ��� Map
	LinkedHashMap<String,Integer> genreColorMap = new LinkedHashMap<String,Integer>(); // ���� �帣���� ������ ��� Map
	LinkedHashMap<String,Integer> whenYearColorMap = new LinkedHashMap<String, Integer>();
	LinkedHashMap<String,Integer> platformColor = new LinkedHashMap<String,Integer>(); // ���� �÷������� ������ ��� Map
	LinkedHashMap<String,Integer> esrbColorMap = new LinkedHashMap<String,Integer>();
	LinkedHashMap<Integer,Integer> maxWeekByRank = new LinkedHashMap<Integer, Integer>();
	
	Random forColor = new Random(); // color �� �������� �ֱ� ���ؼ�
	
	int UiColor = color(94,31,47);
	int color = 0;
	final int xBase = 0;
	final int yBase = -110;
	boolean iswhenChangeClicked = false;
	boolean isGenreChangeClicked = false;
	boolean isEsrbChangeClicked = false;
	
	public void setup() {
		size(1920,960);
		
		table = loadTable("../all.csv","header");
		Table genrecolor = loadTable("../genrecolor.csv","header");
		Table yearcolor = loadTable("../yearcolor.csv","header");
		Table esrbColor = loadTable("../esrb.csv","header");
	
		MINWEEK = table.getIntList("week").min();
		MAXWEEK = table.getIntList("week").max();
		
		// Table �� 30���� �ɰ��� ����
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
				esrbSet.add(row.getString("esrb"));
			}
			subRecords.put(week,subTable);
		}
		
		// �̸� ���� ��ŷ �� ���� ����.
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			Arrays.fill(positions, -1);
			
			namePos.put(name, positions);
			nameColor.put(name, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		}
		
		int idx = 0;
		
		// �� �ָ��� ���� �̸����� ��ŷ�� ��� �ִ� namePos�� ��� ����
		for (Entry<Integer,Table> records : subRecords.entrySet()) {
			//records.getValue().sort("platform");
			//records.getValue().sort("genre");
			
			//records.getValue().sort("when");
			/*
			for (int i = 0 ; i < records.getValue().getRowCount() ; i++)
				records.getValue().getRow(i).setInt("pos", i);
			*/
			for (TableRow row : records.getValue().rows()) {
				namePos.get(row.getString("name"))[idx] = row.getInt("pos");
			}
			
			idx++;
		}
		
		for (String genre : genreSet) {
			TableRow rgb = genrecolor.findRow(genre, "genrename");

			genreColorMap.put(genre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		for (String platform : platformSet)
			platformColor.put(platform,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		
		for (String when : whenYearSet) {
			TableRow rgb = yearcolor.findRow(when, "year");
			whenYearColorMap.put(when,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		
		for (String esrb : esrbSet) {
			TableRow rgb = esrbColor.findRow(esrb, "esrb");
			esrbColorMap.put(esrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b")));
		}
		
		worldCamera = new Camera(xBase,yBase,zoom); 
	}
	
	public void draw() {
		noStroke();
		background(235,235,235);
		String when = "";
		String name = "";
		String genre = "";
		String esrb = "";

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
			// ī�޶� ���ص� UI �� ���ϰ� ���� �ʱ� ���ؼ�
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
				esrb = row.getString("esrb");
				
				if(iswhenChangeClicked)
					color = whenYearColorMap.get(when);
				else if (isEsrbChangeClicked)
					color = esrbColorMap.get(esrb);
				else if (isGenreChangeClicked)
					color = genreColorMap.get(genre);
				else
					color = genreColorMap.get(genre);
			
			
				int nextPos = namePos.get(name)[wn];

				int rX = rbx+(wn-1)*(wInterval*w-1);
				int rY = rbx+pos*(w+d);
				
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
		
		// UI �κ����� ī�޶��� ������ ���� ����
		// ��� ��ǥ�� �׻� ī�޶��� ��ǥ�� dx,dy �� ���� ���������
		scale(1/zoom);
			fill(UiColor);
				//�� �簢��
				float upperRectX = worldCamera.pos.x+dx;
				float upperRectY = worldCamera.pos.y+dy;
				
    			rect(upperRectX,upperRectY,width,100);
    			// �Ʒ� �簢��
    			float downRectX = worldCamera.pos.x+dx;
				float downRectY = worldCamera.pos.y+dy+height-100;
    			rect(downRectX,downRectY,width,100);
    			
    			//genreChange click
    			Rectangle genreChange = new Rectangle((int) downRectX+50,(int) downRectY+25,100,50);
    			
    			genreChange.x-=worldCamera.pos.x;
    			genreChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && genreChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				
    				iswhenChangeClicked = false;
    				isEsrbChangeClicked = false;
    				isGenreChangeClicked = true;
    			}
    			else {
    				fill(0,255,0);
    				
    			}
    			rect(downRectX+50,downRectY+25,100,50);
    			//genreChange
    			textSize(25);
    			fill(0);
    			text("genre",downRectX+50,downRectY+50);
    			
    			//whenChange click
    			Rectangle whenChange = new Rectangle((int) downRectX+200,(int) downRectY+25,100,50);
    			
    			whenChange.x-=worldCamera.pos.x;
    			whenChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && whenChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				isGenreChangeClicked = false;
    				isEsrbChangeClicked = false;
    				iswhenChangeClicked = true;
    			}
    			else {
    				fill(0,255,0);
    			}
    			rect(downRectX+200,downRectY+25,100,50);
    			//whenChange
    			textSize(25);
    			fill(0);
    			text("When",downRectX+200,downRectY+50);
    			
    			//esrbChange click
    			Rectangle esrbChange = new Rectangle((int) downRectX+500,(int) downRectY+25,100,50);
    			esrbChange.x-=worldCamera.pos.x;
    			esrbChange.y-=worldCamera.pos.y;
    			
    			//esrbChange
    			if (mousePressed && esrbChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				iswhenChangeClicked = false;
    				isGenreChangeClicked = false;
    				isEsrbChangeClicked = true;
    			}
    			else {
    				fill(0,255,0);
    			}
    			
    			rect(downRectX+500,downRectY+25,100,50);
    			textSize(25);
    			fill(0);
    			text("esrb",downRectX+500,downRectY+50);
    			
    			
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


