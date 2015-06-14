package taehwan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	ArrayList<Record> records = new ArrayList<Record>();;
	final int w = 50;
	final int d = 10; // 사각형간의 차이
	final int rbx = 20; 
	final int rectNum = 30;
	final int weekNum = 52;
	
	int curDate;
	Camera worldCamera;
	Set<String> nameSet = new HashSet<String>();
	
	HashMap<String,int[]> namePos = new HashMap<String,int[]>();
	HashMap<String,Integer> nameColor = new HashMap<String,Integer>();
	Random forColor = new Random();
	
	public void setup() {
		size(640,480);
		table = loadTable("../2007_.csv","header");
		
		println(table.getRowCount() + " total rows in table"); 
		
		for(TableRow row : table.rows()) {
			nameSet.add(row.getString("name"));
			
			records.add(new Record(row));
		}
		

		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			
			for (int week = 1 ; week < weekNum ; week++) {
				int end = rectNum * week-1;
				
				for (int start = rectNum*week-rectNum; start < end ; start++) {
					if(records.get(start).getName().contains(name)) {
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
		
		worldCamera = new Camera(); 
	}
	
	public void draw() {
		//noStroke();
		background(200);
		
		translate(-worldCamera.pos.x, -worldCamera.pos.y); 
		worldCamera.draw(); 
		
		// 날짜가 같다면 ry가 같아야 함
		// w는 상수 
		// 이전 rect 그릴 때 좌표
		// rbx = qbx-w, rby = rbx+id*(w+d) = qby, w, w
		//rect(20,100,30,30);
		
		// quad 그릴 때 좌표
		/*
		 * qx = qbx+w = rbx+w*2, 
		 * qy = qby+w = rby+w = rbx+id*(w+d)+w , 
		 * qbx+w = rbx+w*2, 
		 * qy+w = rby+w*2 = rbx+id*(w+d)+w*2, 
		 * qbx = rbx + w,
		 * qby+w = rbx+id*(w+d)+w, 
		 * qbx = rbx+w, 
		 * qby = rby = rbx+id*(w+d)
		 */
		 
		//quad(80+30,130,80+30,130+30,50,100+30,50,100);
		
		// 다음 rect 그릴 떄 좌표
		/* rnx = qx = rbx+w*2, 
		 * rny = qy = rbx+id*(w+d)+w, 
		 * w, w
		 */
		//rect(80+30,130,30,30);
		
//		for (int id = 0 ; id < rectNum ; id++) {
//				rect(rbx,rbx+id*(w+d),w,w);
//				quad(rbx+w*2,
//						rbx+id*(w+d)+w,
//						rbx+w*2,
//						rbx+id*(w+d)+w*2,
//						rbx + w,
//						rbx+id*(w+d)+w,
//						rbx+w,
//						rbx+id*(w+d));
//				rect(rbx+w*2,rbx+id*(w+d)+w,w,w);
//		}
		
		for(int wn = 2 ; wn < weekNum ; wn++)
			for (int pos = 1 ; pos < rectNum-1 ; pos++) {
					
				fill(0);
				//text(wn+","+pos,rbx+w*(wn*2-2),rbx+pos*(w+d));
				int colors  = 0;
				
				for (Entry<String,int[]> each : namePos.entrySet()) {
					if(pos == each.getValue()[wn-1]) {
						colors = nameColor.get(each.getKey());
						text(each.getKey(),rbx+w*(wn*10-2),rbx+pos*(w+d));// wn *2 원래
						break;
					}
					else {
						colors = 192;
					}
				}
				
				fill(colors);
				rect(rbx+w*(wn*10-2),rbx+pos*(w+d),w,w); // wn *2 원래
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
		    	  pos.x =0;
		    	  pos.y = 0 ;
		      }
		    } 
		  } 
	}  
}


