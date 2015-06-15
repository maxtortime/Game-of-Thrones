package taehwan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
	final int w = 50;
	final int d = 15; // 사각형간의 차이
	final int rbx = 20; 
	final int rectNum = 30;
	final int weekNum = 52;
	
	float zoom = 1;
	float h = 0;
	
	Camera worldCamera;
	Set<String> nameSet = new LinkedHashSet<String>();
	Set<String> genreSet = new LinkedHashSet<String>();
	
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // 게임 이름마다 주별로 랭킹을 담고 있는 Map 
	LinkedHashMap<String,Integer> nameColor = new LinkedHashMap<String,Integer>(); // 게임 제목마다 색깔을 담는 Map
	LinkedHashMap<String,Integer> genreColor = new LinkedHashMap<String,Integer>(); // 게임 장르마다 색깔을 담는 Map
	Random forColor = new Random(); // color 를 랜덤으로 주기 위해서
	
	public void setup() {
		size(1024,640);
		table = loadTable("../2007_.csv","header");
		
		println(table.getRowCount() + " total rows in table");
		
		
		for(TableRow row : table.rows()) {
			//nameSet.add(row.getString("name"));
			genreSet.add(row.getString("genre"));
			records.add(new Record(row));
		}
		
		
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			
			for (int week = 1 ; week < weekNum ; week++) {
				int end = rectNum * week-1;
				
				for (int start = rectNum*week-rectNum; start < end ; start++) {
					if(records.get(start).getName().equals(name)) {
						positions[week-1] =  records.get(start).getPos()-1;
						break;
					}
					else {
						positions[week-1] = -1;
					}
					
				}
			}
			
			nameColor.put(name, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
			namePos.put(name,positions);
		}
		
		for (String genre : genreSet)
			genreColor.put(genre,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		
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
		
		for(int wn = 0 ; wn < weekNum-1 ; wn++)
			for (int pos = 0 ; pos < rectNum-1 ; pos++) {
				int nextPos = 0;
				int colors  = 0;
				
				fill(0);
				
				
				for (Entry<String,int[]> each : namePos.entrySet()) {
					if(pos == each.getValue()[wn]) {
						
						for (Record r : records) {
							if (r.getName().equals(each.getKey())) {
								colors = genreColor.get(r.getGenre());

								break;
							}
						}
						//colors = nameColor.get(each.getKey());
						
						nextPos = each.getValue()[wn+1];
						
						if (nextPos ==-1)
							nextPos = pos;
						
						//not height
						if (each.getKey().length()<15)
							text(each.getKey(),rbx+w*(wn*2-2),rbx+pos*(w+d));
						else
							text(each.getKey().substring(0,7)+"...",rbx+w*(wn*2-2),rbx+pos*(w+d));
						
						break;
					}
					else {
						colors = 192;
						nextPos = -100; // null 인 애들은 quad를 안 그려주려고
					}
				}
				
				
				
				fill(colors);
				rect(rbx+w*(wn*2-2),rbx+pos*(w+d),w,w);

				
				if (nextPos != -100) 
					quad(rbx+w*(wn*2-1),rbx+pos*(w+d),
						rbx+w*wn*2,rbx+nextPos*(w+d),
						rbx+w*wn*2,rbx+w+nextPos*(w+d),
						rbx+w*(wn*2-1),rbx+pos*(w+d)+w);
				
				
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


