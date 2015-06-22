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
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;

@SuppressWarnings("serial")
public class Drawing extends PApplet {
	Table table;
	Table genrecolor;
	Table yearcolor;
	Table publisherColor;
	Table esrbColor;
	PFont DIN,GOT;
	PImage CROWN,Footer,Header,stackIcon;
	final int w = 30; // 사각형의 크기
	int d = 50; // 사각형간의 차이
	final int rbx = 110; // 첫 사각형의 x 좌표 
	final int h = 10;
	
	final int rectNum = 30;
	final int weekNum = 408; // 51 * (year)
	final float wInterval = 3.0F;
	int dif;
	
	int nrbx = 0;
	int nrby = 0;
	
	float zoom1 = 0.78f;
	float zoom2 = 0.226f;
	int MAXWEEK,MINWEEK;
	
	Camera worldCamera;
	Set<String> nameSet = new LinkedHashSet<String>();
	Set<String> genreSet = new LinkedHashSet<String>();
	Set<String> platformSet = new LinkedHashSet<String>();
	Set<Integer> whenYearSet = new LinkedHashSet<Integer>();
	Set<String> publisherSet = new LinkedHashSet<String>();

	Set<String> esrbSet = new LinkedHashSet<String>();

	float temp1 = 3.5F;
	float temp2 = 3.5F;
	float temp3 = 3.3F;
	float temp4 = 2.9F;
	float temp5 = 2.5F;
	float temp6 = 2.0F;
	float temp7 = 1.7F;
	float temp8 = 1.6F;
	float temp9 = 1.5F;

	ArrayList<Table> tables = new ArrayList<Table>();
	
	LinkedHashMap<Integer,Table> subRecords = new LinkedHashMap<Integer,Table>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	
	LinkedHashMap<String,Integer> nameColorMap = new LinkedHashMap<String,Integer>(); // 게임 제목마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> genreColorMap = new LinkedHashMap<String,Integer>(); // 게임 장르마다 색깔을 담는 Map
	LinkedHashMap<Integer,Integer> whenYearColorMap = new LinkedHashMap<Integer, Integer>();
	LinkedHashMap<String,Integer> platformColorMap = new LinkedHashMap<String,Integer>(); // 게임 플랫폼마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> publisherColorMap = new LinkedHashMap<String,Integer>(); // 게임 플랫폼마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> esrbColorMap = new LinkedHashMap<String,Integer>();
	
	LinkedHashMap<Integer,Integer> maxWeekByRank = new LinkedHashMap<Integer, Integer>();
	LinkedHashMap<String,Boolean> isNameClicked = new LinkedHashMap<String,Boolean>();
	
	Random forColor = new Random(); // color 를 랜덤으로 주기 위해서

	
	int UiColor = color(94,31,47);
	int color = 0;
	final int xBase = 0;
	final int yBase = -110;
	boolean iswhenChangeClicked = false;
	boolean isGenreChangeClicked = true; // 가장 최초이므로
	boolean isEsrbChangeClicked = false;	
	boolean isPlatformChangeClicked = false; 
	boolean isPublisherChangedClicked = false;
	boolean isRedraw = false;
	boolean isStacked = false;
	boolean isSales = false;
	
	String overedName = new String();
	int overedWeek = 0;
	String overedplatform = new String();
	String overedgenre = new String();
	String overedesrb = new String();
	int overedWhen = 0;
	
	public void setup() {
		size(1920,1080);
		
		DIN = createFont("DIN-MEDIUM",14);
		GOT = createFont("GAME OF THRONES",14);
		CROWN = loadImage("../crown.png");
		Footer = loadImage("../Footer.png");
		Header =  loadImage("../Header.png");
		stackIcon = loadImage("../stackicon.png");
		
		d = height/rectNum; // 사각형간의 차이
		dif = 0;
		
		nrby = height;
		nrbx = width;
		
		table = loadTable("../all.csv","header");
		genrecolor = loadTable("../genrecolor.csv","header");
		yearcolor = loadTable("../yearcolor.csv","header");
		esrbColor = loadTable("../esrb.csv","header");
		publisherColor = loadTable("../publishercolor.csv","header");
		
		MINWEEK = table.getIntList("week").min();
		MAXWEEK = table.getIntList("week").max();
		
		// Table 을 30개씩 쪼개기 위함
		for (int week = 0 ; week < weekNum ; week++) {
			int end = rectNum+week*rectNum ;
			Table subTable = new Table();
			
			for (int col = 0 ; col < table.getColumnCount() ; col++)
				subTable.addColumn(table.getColumnTitle(col));
				
			for (int start = rectNum * week; start < end ; start++) {
				TableRow row = table.getRow(start);
				
				subTable.addRow(row);
				nameSet.add(row.getString("name"));
				genreSet.add(row.getString("genre"));
				platformSet.add(row.getString("platform"));
				whenYearSet.add(row.getInt("whenyear"));
				esrbSet.add(row.getString("esrb"));
				publisherSet.add(row.getString("publisher"));
			}
			subRecords.put(week,subTable);
		}
		
		// 이름 별로 랭킹 및 색을 저장.
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			Arrays.fill(positions, -1);
			
			namePos.put(name, positions);
			nameColorMap.put(name, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		}
		
		int idx = 0;
		// 각 주마다 게임 이름별로 랭킹을 담고 있는 namePos를 담고 있음

		for (Entry<Integer,Table> records : subRecords.entrySet()) {
			//records.getValue().sort("platform");
			//records.getValue().sort("genre");
			
			//records.getValue().sort("when");

			/*
			for (int i = 0 ; i < records.getValue().getRowCount()-1 ; i++)
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
			platformColorMap.put(platform,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		
		for (Integer when : whenYearSet) {
			TableRow rgb = yearcolor.findRow(Integer.toString(when), "year");
			whenYearColorMap.put(when,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}		
		for (String esrb : esrbSet) {
			TableRow rgb = esrbColor.findRow(esrb, "esrb");
			esrbColorMap.put(esrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		
		for (String publisher : publisherSet) {
			/*
			TableRow rgb = publisherColor.findRow(publisher, "publisher");
			try {
				publisherColorMap.put(publisher,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
			} catch (NullPointerException e) {
				publisherColorMap.put(publisher, color(192));
			}
			*/
			
			publisherColorMap.put(publisher, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		}
		
		worldCamera = new Camera(xBase,yBase,zoom2);

	}
	
	public void draw() {
		noStroke();

		background(51,49,50);
		int when = 0;
		String name = "";
		String genre = "";
		String esrb = "";
		String platform= "";
		String publisher ="";
	
		int pos = 0;

		int dx = 0;
		int dy = 0;
	
		Table sub = new Table();
		
		// camera에 적용받지 않는 도형을 그리기 위해
		if (keyPressed) {
			 if (key == 'w') dy += 50; 
			 if (key == 's') dy -= 50; 
		     if (key == 'a') dx+= 50; 
		     if (key == 'd') dx -= 50;
		 
			if( key == 'o'){
				zoom2 *= 1.1;
				key = 'h';
			}
			if(key == 'p'){
				zoom2 *= 0.9;
				key = 'h';
			}
			if(key == '1'){
				temp1 += 0.1;
				key = 'h';
			}
			if(key == '2'){
				temp2 += 0.1;
				key = 'h';
			}
			if(key == '3'){
				temp3 += 0.1;
				key = 'h';
			}
			if(key == '4'){
				temp4 += 0.1;
				key = 'h';
			}
			if(key == '5'){
				temp5 += 0.1;
				key = 'h';
			}
			if(key == '6'){
				temp6 += 0.1;
				key = 'h';
			}
			if(key == '7'){
				temp7 += 0.1;
				key = 'h';
			}
			if(key == '8'){
				temp8 += 0.1;
				key = 'h';
			}
			if(key == '9'){
				temp9 += 0.1;
				key = 'h';
			}
		}

		translate(-worldCamera.pos.x, -worldCamera.pos.y); 
		worldCamera.draw();
		scale(zoom2);
		
		int idx = 0;
		
		for (Entry<Integer, Table> each : subRecords.entrySet()) {
			int wn = each.getKey(); // get index
			dif = 0;
			sub = each.getValue();
			
			if (isStacked) {
				if(iswhenChangeClicked) {
					sub.sort("whenyear");
				}
				else if (isEsrbChangeClicked) {
					sub.sort("esrb");
				}
				else if (isGenreChangeClicked) {
					sub.sort("genre");
				}
				else if (isPlatformChangeClicked) {
					sub.sort("platform");
				}
				else if (isPublisherChangedClicked) {
					sub.sort("publisher");
				}
				else {
					sub.sort("id");
				}
				
				
				for (int i = 0 ; i < sub.getRowCount() ; i++)
					sub.getRow(i).setInt("pos", i+1);
				

				for (TableRow row : sub.rows()) {
					namePos.get(row.getString("name"))[idx] = row.getInt("pos");
					
				}
				
				idx++;
				
			}
			else {
				idx++;
				
			}
			
		
			int tempWeek = 0;
			int nextrY = 0;
			int nextWeek = 0;
			float temprY = 800;
			int nextPos = 0;
			Table before = subRecords.get(wn+1);
			
			/*
			if(isRedraw) {
				for (int i = 0 ; i < each.getValue().getRowCount() ; i++)
					each.getValue().getRow(i).setInt("pos", i);
				

				for (TableRow row : each.getValue().rows()) {
					namePos.get(row.getString("name"))[idx] = row.getInt("pos");
				}
				
				idx++;
				
			}
			*/
			for (TableRow row : sub.rows()) {
				pos = row.getInt("pos");
				
				when = row.getInt("whenyear");
				name = row.getString("name");
				genre = row.getString("genre");
				esrb = row.getString("esrb");
				platform = row.getString("platform");
				publisher = row.getString("publisher");
				int week = row.getInt("week");
				
				if(wn+1>=408)
					nextPos = -1;
				else
					nextPos = namePos.get(name)[wn+1];
				
				
				if (nextPos==-1) {
					nextWeek = 0;
				}
				else if (nextPos-1 <0)
					nextWeek = 0;
				else
					nextWeek = before.getInt(nextPos-1, "week");
					
				
				if(iswhenChangeClicked)
					color = whenYearColorMap.get(when);
				else if (isEsrbChangeClicked)
					color = esrbColorMap.get(esrb);
				else if (isGenreChangeClicked)
					color = genreColorMap.get(genre);
				else if (isPlatformChangeClicked)
					color = platformColorMap.get(platform);
				else if (isPublisherChangedClicked)
					color = publisherColorMap.get(publisher);

				
			
				Rectangle cur;
				
				if (isSales) {
				

					int id = row.getInt("id");
	
					float nrY = 0;
					float nNrY = 0;
	
					if(week<30000){
						nrY = 800 - week/300*temp1; 
					}else if(week<40000){
						nrY = 700 - week/400*temp2;
					}else if(week<50000){
						nrY = 600 - week/500*temp3;
					}else if(week<60000){
						nrY = 500 - week/600*temp4;
					}else if(week<80000){
						nrY = 400 - week/800*temp5;
					}else if(week<100000){
						nrY = 300 - week/1000*temp6;
					}else if(week<150000){
						nrY = 200 - week/1500*temp7;
					}else if(week<250000){
						nrY = 100 - week/2500*temp8;
					}else if(week<400000){
						nrY = 100 - week/4000*temp8;
					}else{
						nrY = -week/10000*temp9;
					}
	
					if (Math.abs(temprY-nrY)<h){
						nrY = temprY - h;
					}
	
					if(nextWeek<30000){
						nNrY = 800 - nextWeek/300*temp1; 
					}else if(nextWeek<40000){
						nNrY = 700 - nextWeek/400*temp2;
					}else if(nextWeek<50000){
						nNrY = 600 - nextWeek/500*temp3;
					}else if(nextWeek<60000){
						nNrY = 500 - nextWeek/600*temp4;
					}else if(nextWeek<80000){
						nNrY = 400 - nextWeek/800*temp5;
					}else if(nextWeek<100000){
						nNrY = 300 - nextWeek/1000*temp6;
					}else if(nextWeek<150000){
						nNrY = 200 - nextWeek/1500*temp7;
					}else if(nextWeek<250000){
						nNrY = 100 - nextWeek/2500*temp8;
					}else if(nextWeek<400000){
						nNrY = 100 - nextWeek/4000*temp8;
					}else{
						nNrY = -nextWeek/10000*temp9;
					}
	
					if (Math.abs(temprY-nrY)<h){
						nrY = temprY - h;
					}
					nrY += 200;
					nNrY += 200;
					
					int nrX = (int) (nrbx+(wn-1)*(wInterval*w-1)-1050);
					tempWeek = week;
					temprY = nrY;
			
					
					cur = new Rectangle(nrX,(int) nrY, w,h);
					
					if (mousePressed && cur.contains((mouseX+worldCamera.pos.x)/zoom2, (mouseY+worldCamera.pos.y)/zoom2)) {
						//마우스를 댄 그 사각형
						overedName = name;
						overedplatform = platform;
						overedWhen = when;
						overedWeek = week;	
						overedgenre = genre;
						overedesrb = esrb;
								
						TableRow rgb1 = yearcolor.findRow(Integer.toString(when), "year");
						TableRow rgb2 = esrbColor.findRow((esrb), "esrb");
						TableRow rgb3 = genrecolor.findRow((genre), "genrename");
					
						if (whenYearColorMap.get(overedWhen)==color){
							whenYearColorMap.put(overedWhen, color(rgb1.getInt("r"),rgb1.getInt("g"),rgb1.getInt("b"),255));
							color = whenYearColorMap.get(overedWhen);						
							whenYearColorMap.put(overedWhen,color(rgb1.getInt("r"),rgb1.getInt("g"),rgb1.getInt("b"),rgb1.getInt("a")));							
						}else if(esrbColorMap.get(overedesrb)==color){
							esrbColorMap.put(overedesrb, color(rgb2.getInt("r"),rgb2.getInt("g"),rgb2.getInt("b"),255));
							color = esrbColorMap.get(overedesrb);						
							esrbColorMap.put(overedesrb,color(rgb2.getInt("r"),rgb2.getInt("g"),rgb2.getInt("b"),rgb2.getInt("a")));
						}else if(genreColorMap.get(overedgenre)==color){
							genreColorMap.put(overedgenre, color(rgb3.getInt("r"),rgb3.getInt("g"),rgb3.getInt("b"),255));
							color = genreColorMap.get(overedgenre);						
							genreColorMap.put(overedgenre,color(rgb3.getInt("r"),rgb3.getInt("g"),rgb3.getInt("b"),rgb3.getInt("a")));
						}		
						
					}
					else {
						if (overedName.equals(name) && overedplatform.equals(platform) && overedWhen == when) {
							//같은 이름
							TableRow rgb = yearcolor.findRow(Integer.toString(when), "year");
							if (whenYearColorMap.get(overedWhen)==color) {
								whenYearColorMap.put(overedWhen, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = whenYearColorMap.get(overedWhen);
								whenYearColorMap.put(overedWhen,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}							
						if (overedName.equals(name) && overedplatform.equals(platform) && overedesrb.equals(esrb)){
							//같은 이름
							TableRow rgb = esrbColor.findRow((esrb), "esrb");
							if (esrbColorMap.get(overedesrb)==color) {
								esrbColorMap.put(overedesrb, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = esrbColorMap.get(overedesrb);
								esrbColorMap.put(overedesrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}															
					    }						
						if (overedName.equals(name) && overedplatform.equals(platform) && overedgenre.equals(genre)){
							//같은 이름
							TableRow rgb = genrecolor.findRow((genre), "genre");
							if (genreColorMap.get(overedgenre)==color) {
								genreColorMap.put(overedgenre, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = genreColorMap.get(overedgenre);
								genreColorMap.put(overedgenre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
					}
					
					
					
					
					
					if (nextPos != -1) {
						strokeWeight(7);
						stroke(color);
						line(nrX+w,nrY+h/2,nrX+w+d*wInterval,nNrY+h/2);
					}
					
					
					noStroke();
					fill(color);
					rect(nrX,nrY,w,h);
					
				}
				else {
					int rX = (int) (rbx+(wn-1)*(wInterval*w-1));
					int rY = rbx+pos*(w+d);
					cur = new Rectangle(rX, rY, w,w);
					
					if (mousePressed && cur.contains((mouseX+worldCamera.pos.x)/zoom2, (mouseY+worldCamera.pos.y)/zoom2)) {
						//마우스를 댄 그 사각형
						overedName = name;
						overedplatform = platform;
						overedWhen = when;
						overedWeek = week;	
						overedgenre = genre;
						overedesrb = esrb;
								
						TableRow rgb1 = yearcolor.findRow(Integer.toString(when), "year");
						TableRow rgb2 = esrbColor.findRow((esrb), "esrb");
						TableRow rgb3 = genrecolor.findRow((genre), "genrename");
					
						if (whenYearColorMap.get(overedWhen)==color){
							whenYearColorMap.put(overedWhen, color(rgb1.getInt("r"),rgb1.getInt("g"),rgb1.getInt("b"),255));
							color = whenYearColorMap.get(overedWhen);						
							whenYearColorMap.put(overedWhen,color(rgb1.getInt("r"),rgb1.getInt("g"),rgb1.getInt("b"),rgb1.getInt("a")));							
						}else if(esrbColorMap.get(overedesrb)==color){
							esrbColorMap.put(overedesrb, color(rgb2.getInt("r"),rgb2.getInt("g"),rgb2.getInt("b"),255));
							color = esrbColorMap.get(overedesrb);						
							esrbColorMap.put(overedesrb,color(rgb2.getInt("r"),rgb2.getInt("g"),rgb2.getInt("b"),rgb2.getInt("a")));
						}else if(genreColorMap.get(overedgenre)==color){
							genreColorMap.put(overedgenre, color(rgb3.getInt("r"),rgb3.getInt("g"),rgb3.getInt("b"),255));
							color = genreColorMap.get(overedgenre);						
							genreColorMap.put(overedgenre,color(rgb3.getInt("r"),rgb3.getInt("g"),rgb3.getInt("b"),rgb3.getInt("a")));
						}		
						
					}
					else {
						if (overedName.equals(name) && overedplatform.equals(platform) && overedWhen == when) {
							//같은 이름
							TableRow rgb = yearcolor.findRow(Integer.toString(when), "year");
							if (whenYearColorMap.get(overedWhen)==color) {
								whenYearColorMap.put(overedWhen, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = whenYearColorMap.get(overedWhen);
								whenYearColorMap.put(overedWhen,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}							
						if (overedName.equals(name) && overedplatform.equals(platform) && overedesrb.equals(esrb)){
							//같은 이름
							TableRow rgb = esrbColor.findRow((esrb), "esrb");
							if (esrbColorMap.get(overedesrb)==color) {
								esrbColorMap.put(overedesrb, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = esrbColorMap.get(overedesrb);
								esrbColorMap.put(overedesrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}															
					    }						
						if (overedName.equals(name) && overedplatform.equals(platform) && overedgenre.equals(genre)){
							//같은 이름
							TableRow rgb = genrecolor.findRow((genre), "genrename");
							if (genreColorMap.get(overedgenre)==color) {
								genreColorMap.put(overedgenre, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = genreColorMap.get(overedgenre);
								genreColorMap.put(overedgenre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
					}
					
					
					if (nextPos != -1) {
						strokeWeight(7);
						stroke(color);
						line(rX+w,rY+w/2,rbx+wn*(wInterval*w-1),rbx+nextPos*(w+d)+w/2);
					}
					
					
					
					noStroke();
					fill(color);
					rect(rX,rY,w,w);
				}
				
				
				
				
				
				
				
				
			}
		}
		
		isStacked =false;
		// UI 부분으로 카메라의 적용을 받지 않음
		// 모든 좌표에 항상 카메라의 좌표와 dx,dy 를 각각 더해줘야함
		
		scale(1/zoom2);
			fill(UiColor);
				//위 사각형
				float upperRectX = worldCamera.pos.x+dx;
				float upperRectY = worldCamera.pos.y+dy;
				image(Header,upperRectX,upperRectY);
    			//rect(upperRectX,upperRectY,width,100);
    			// 아래쪽
    			float footerX = worldCamera.pos.x+dx;
    			float footerY = worldCamera.pos.y+dy+height-204;
    			image(Footer,footerX,footerY);
    			
    			//float downRectX = worldCamera.pos.x+dx;
				//float downRectY = worldCamera.pos.y+dy+height-100;
    			float downRectX = footerX+230;
    			float downRectY = footerY+60;
    			
    			//genreChange click
    			Rectangle genreChange = new Rectangle((int) downRectX+47,(int) downRectY+25,46,46);
    			
    			genreChange.x-=worldCamera.pos.x;
    			genreChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && genreChange.contains(mouseX, mouseY)) {
    				iswhenChangeClicked = false;
    				isEsrbChangeClicked = false;
    				isPlatformChangeClicked = false;
    				isPublisherChangedClicked =false;
    				isGenreChangeClicked = true;
    			}
    			
    			if(isGenreChangeClicked)
    				fill(216,82,62);
    			else
    				fill(255,255,255,0);
    			
    			rect(downRectX+47,downRectY+25,45.709f,45.833f);
    			
    			//plaform
    			Rectangle plaformhange = new Rectangle((int) downRectX+240,(int) downRectY+25,46,46);
    			
    			plaformhange.x-=worldCamera.pos.x;
    			plaformhange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && plaformhange.contains(mouseX, mouseY)) {
    			
    				iswhenChangeClicked = false;
    				isEsrbChangeClicked = false;
    				isPublisherChangedClicked =false;
    				isGenreChangeClicked = false;
    				isPlatformChangeClicked = true;
    			}
    			
    			if(isPlatformChangeClicked)
    				fill(216,82,62);
    			else
    				fill(255,255,255,0);
    			rect(downRectX+240,downRectY+25,45.709f,45.833f);
    			
    			//plaform draw
    			
    			//esrbChange click
    			Rectangle esrbChange = new Rectangle((int) downRectX+478,(int) downRectY+25,46,46);
    			esrbChange.x-=worldCamera.pos.x;
    			esrbChange.y-=worldCamera.pos.y;
    			
    			//esrbChange
    			if (mousePressed && esrbChange.contains(mouseX, mouseY)) {
    				iswhenChangeClicked = false;
    				isPlatformChangeClicked = false;
    				
    				isPublisherChangedClicked =false;
    				isGenreChangeClicked = false;
    				isEsrbChangeClicked = true;
    			}
    			
    			if(isEsrbChangeClicked)
    				fill(216,82,62);
    			else
    				fill(255,255,255,0);
    			
    			rect(downRectX+478,downRectY+25,45.709f,45.833f);
    			
    			
    			//publisherChange click
    			Rectangle publisherChange = new Rectangle((int) downRectX+665,(int) downRectY+25,46,46);
    			publisherChange.x-=worldCamera.pos.x;
    			publisherChange.y-=worldCamera.pos.y;
    			
    			//publisherChange
    			if (mousePressed && publisherChange.contains(mouseX, mouseY)) {
    				iswhenChangeClicked = false;
    				isPlatformChangeClicked = false;
    				isGenreChangeClicked = false;
    				isEsrbChangeClicked = false;
    				isPublisherChangedClicked =true;
    			}
    			
    			if(isPublisherChangedClicked)
    				fill(216,82,62);
    			else
    				fill(255,255,255,0);
    			
    			rect(downRectX+665,downRectY+25,45.709f,45.833f);
    			
    			//whenChange click
    			Rectangle whenChange = new Rectangle((int) downRectX+915,(int) downRectY+25,46,46);
    			
    			whenChange.x-=worldCamera.pos.x;
    			whenChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && whenChange.contains(mouseX, mouseY)) {
    				isPlatformChangeClicked = false;
    				isGenreChangeClicked = false;
    				isEsrbChangeClicked = false;
    				isPublisherChangedClicked =false;
    				
    				iswhenChangeClicked = true;
    			}
    			
    			if(iswhenChangeClicked)
    				fill(216,82,62);
    			else
    				fill(255,255,255,0);
    			
    			rect(downRectX+915,downRectY+25,45.709f,45.833f);
    			
    			
    			//stack click
    			Rectangle stacked = new Rectangle((int) downRectX+1165,(int) downRectY+12,100,50);
    			stacked.x-=worldCamera.pos.x;
    			stacked.y-=worldCamera.pos.y;
    			
    			//stack change
    			if (mousePressed && stacked.contains(mouseX, mouseY)) {
    				
    				if(isStacked)
    					isStacked = false;
    				else
    					isStacked = true;
    				delay(500);
    				
    			}
    			
    			if(isStacked)
    				image(stackIcon,downRectX+1165,downRectY+12);
    			
    			
    			fill(255,255,255,0);
    			rect(downRectX+1165,downRectY+12,61,83);
    			
    			//salesChange click
    			Rectangle salesChange = new Rectangle((int) downRectX+1405,(int) downRectY+25,100,50);
    			
    			salesChange.x-=worldCamera.pos.x;
    			salesChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && salesChange.contains(mouseX, mouseY)) {
    				fill(255,0,0);
    				if(isSales)
    					isSales = false;
    				else 
    					isSales = true;
    				delay(500);
    			}
    			else {
    				fill(0,255,0);
    				
    			}
    			rect(downRectX+1405,downRectY+25,45.709f,45.833f);
    			//salesChange
    			textSize(25);
    			fill(0);
    			text("Sales",downRectX+1405,downRectY+50);
    			
    			
    			
    			
			fill(255);
			text(overedName+"...week: "+overedWeek,worldCamera.pos.x+dx+700,worldCamera.pos.y+dy+65);

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
		    	  zoom2 = basicZoom;
		      }
		    } 

		  }
		}  
			
	}
	
	
	




