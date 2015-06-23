package taehwan;





import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;

@SuppressWarnings("serial")
public class Drawing extends PApplet {
	public static void main(String args[])
    {
      PApplet.main(new String[] { taehwan.Drawing.class.getName() });
    }
	

	Table table;
	Table genrecolor;
	Table yearcolor;
	Table publisherColor;
	Table esrbColor;
	Table platformColor;
	PFont DIN,GOT;
	PImage CROWN,Footer,Header,stackIcon,salesIcon;
	final int w = 30; // �簢���� ũ��
	int d = 50; // �簢������ ����
	final int rbx = 110; // ù �簢���� x ��ǥ 
	final int h = 10;
	
	final int rectNum = 30;
	final int weekNum = 408; // 51 * (year)
	final float wInterval = 3.0F;
	int dif;
	
	int nrbx = 0;
	int nrby = 0;
	
	float sales = 0.1439f;
	float normal = 0.226f;
	
	float zoom = normal;
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
	
	LinkedHashMap<Integer,Table> subRecords = new LinkedHashMap<Integer,Table>(); // ���� �̸����� �ֺ��� ��ŷ�� ��� �ִ� Map 
	LinkedHashMap<String,int[]> namePos = new LinkedHashMap<String,int[]>(); // ���� �̸����� �ֺ��� ��ŷ�� ��� �ִ� Map 
	
	LinkedHashMap<String,Integer> nameColorMap = new LinkedHashMap<String,Integer>(); // ���� ���񸶴� ������ ��� Map
	LinkedHashMap<String,Integer> genreColorMap = new LinkedHashMap<String,Integer>(); // ���� �帣���� ������ ��� Map
	LinkedHashMap<Integer,Integer> whenYearColorMap = new LinkedHashMap<Integer, Integer>();
	LinkedHashMap<String,Integer> platformColorMap = new LinkedHashMap<String,Integer>(); // ���� �÷������� ������ ��� Map
	LinkedHashMap<String,Integer> publisherColorMap = new LinkedHashMap<String,Integer>(); // ���� �÷������� ������ ��� Map
	LinkedHashMap<String,Integer> esrbColorMap = new LinkedHashMap<String,Integer>();
	
	LinkedHashMap<Integer,Integer> maxWeekByRank = new LinkedHashMap<Integer, Integer>();
	LinkedHashMap<String,Boolean> isNameClicked = new LinkedHashMap<String,Boolean>();
	
	Random forColor = new Random(); // color �� �������� �ֱ� ���ؼ�

	Stack<LinkedHashMap<Integer,Table>> forRecordStack = new Stack<LinkedHashMap<Integer,Table>>();
	
	int UiColor = color(94,31,47);
	int color = 0;
	final int xBase = 0;
	final int yBase = -250;
	boolean iswhenChangeClicked = false;
	boolean isGenreChangeClicked = true; // ���� �����̹Ƿ�
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
	String overedpublisher = new String();
	int overedWhen = 0;
	
	public void setup() {
		size(1920,1080);
		
		DIN = createFont("DIN-MEDIUM",14);
		GOT = createFont("GAME OF THRONES",14);
		CROWN = loadImage("../crown.png");
		Footer = loadImage("../Footer.png");
		Header =  loadImage("../Header.png");
		stackIcon = loadImage("../stackicon.png");
		salesIcon =  loadImage("../salesicon.png");
		
		d = height/rectNum; // �簢������ ����
		dif = 0;
		
		nrby = height;
		nrbx = width;
		
		table = loadTable("../all.csv","header");
		genrecolor = loadTable("../genrecolor.csv","header");
		yearcolor = loadTable("../yearcolor.csv","header");
		esrbColor = loadTable("../esrb.csv","header");
		publisherColor = loadTable("../publishercolor.csv","header");
		platformColor = loadTable("../platformcolor.csv","header");
		
		MINWEEK = table.getIntList("week").min();
		MAXWEEK = table.getIntList("week").max();
		
		// Table �� 30���� �ɰ��� ����
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
		
		// �̸� ���� ��ŷ �� ���� ����.
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			Arrays.fill(positions, -1);
			
			namePos.put(name, positions);
			nameColorMap.put(name, color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
		}
		
		int idx = 0;
		// �� �ָ��� ���� �̸����� ��ŷ�� ��� �ִ� namePos�� ��� ����

		for (Entry<Integer,Table> records : subRecords.entrySet()) {
			for (TableRow row : records.getValue().rows()) {
				namePos.get(row.getString("name"))[idx] = row.getInt("pos");
				
			}
			
			idx++;
		}
		
		forRecordStack.push(subRecords);
		
		for (String genre : genreSet) {
			TableRow rgb = genrecolor.findRow(genre, "genre");
			genreColorMap.put(genre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		for (String platform : platformSet) {
			//platformColorMap.put(platform,color(forColor.nextInt(255),forColor.nextInt(255),forColor.nextInt(255)));
			
			TableRow rgb = platformColor.findRow(platform, "platform");
			try {
				platformColorMap.put(platform,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
			} catch (NullPointerException e) {
				platformColorMap.put(platform,color(192,192,192));
			}
			
		}
			
		
		for (Integer when : whenYearSet) {
			TableRow rgb = yearcolor.findRow(Integer.toString(when), "whenyear");
			whenYearColorMap.put(when,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}		
		for (String esrb : esrbSet) {
			TableRow rgb = esrbColor.findRow(esrb, "esrb");
			esrbColorMap.put(esrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
		}
		
		for (String publisher : publisherSet) {
			
			TableRow rgb = publisherColor.findRow(publisher, "publisher");
			try {
				publisherColorMap.put(publisher,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
			} catch (NullPointerException e) {
				publisherColorMap.put(publisher, color(192));
			}
			
		}
		
		worldCamera = new Camera(xBase,yBase,zoom);
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
		int year = 2007;
	
		int pos = 0;
		int wn = 0;
		int dx = 0;
		int dy = 0;
	
		Table sub = new Table();
		
		// camera�� ������� �ʴ� ������ �׸��� ����
		if (keyPressed) {
			 if (key == 'w') dy += 50; 
			 if (key == 's') dy -= 50; 
		     if (key == 'a') dx+= 50; 
		     if (key == 'd') dx -= 50;
		 
			if( key == 'o'){
				zoom *= 1.1;
				key = 'h';
			}
			if(key == 'p'){
				zoom *= 0.9;
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
		scale(zoom);
		
		for (Entry<Integer, Table> each : forRecordStack.peek().entrySet()) {
			wn = each.getKey(); // get index
			
			
			if(wn % 51 == 0 || wn == 407) {
				//rect(rbx+wn*(wInterval*w-1),0,width,height);
				strokeWeight(30);
				stroke(150,150,150,70);
				line(rbx+(wn-1)*(wInterval*w-1)+15,-50000,rbx+(wn-1)*(wInterval*w-1)+15,50000);

				textSize(500);
				fill(147);
				text(year,rbx+wn*(wInterval*w-1)+1500,0);
				year++;
				
			}
			
			dif = 0;
			sub = each.getValue();
		
			int nextWeek = 0;
			int nextPos = 0;
			Table before = subRecords.get(wn+1);
			
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
					
					float nrY = 0;
					float nNrY = 0;
			
					nrY = 960 - week/100;					
					
					nNrY = 960 - nextWeek/100;
					
					
					if(week > 200000)nrY = -2000;
					if(nextWeek > 200000)nNrY = -2000;
					
					nrY = nrY+ 2000 +pos*d;
					nNrY = nNrY+ 2000 +nextPos*d;
					
					int nrX = (int) (nrbx+(wn-1)*(wInterval*w-1)-1800);
					cur = new Rectangle(nrX-10,(int) nrY-10, w+20,h+20);
					
					if (mousePressed && cur.contains((mouseX+worldCamera.pos.x)/zoom, (mouseY+worldCamera.pos.y)/zoom)) {
						//���콺�� �� �� �簢��
						overedName = name;
						overedplatform = platform;
						overedWhen = when;
						overedWeek = week;	
						overedgenre = genre;
						overedesrb = esrb;
						overedpublisher = publisher;
								
						TableRow rgb1 = yearcolor.findRow(Integer.toString(when), "year");
						TableRow rgb2 = esrbColor.findRow((esrb), "esrb");
						TableRow rgb3 = genrecolor.findRow((genre), "genrename");
						TableRow rgb4 = platformColor.findRow((platform), "platform");
						TableRow rgb5 = publisherColor.findRow((publisher), "publisher");
					
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
						}else if(platformColorMap.get(overedplatform)==color){
							platformColorMap.put(overedplatform, color(rgb4.getInt("r"),rgb4.getInt("g"),rgb4.getInt("b"),255));
							color = platformColorMap.get(overedplatform);						
							platformColorMap.put(overedplatform,color(rgb4.getInt("r"),rgb4.getInt("g"),rgb4.getInt("b"),rgb4.getInt("a")));
						}else if(publisherColorMap.get(overedpublisher)==color){
							publisherColorMap.put(overedpublisher, color(rgb5.getInt("r"),rgb5.getInt("g"),rgb5.getInt("b"),255));
							color = publisherColorMap.get(overedpublisher);						
							publisherColorMap.put(overedpublisher,color(rgb5.getInt("r"),rgb5.getInt("g"),rgb5.getInt("b"),rgb5.getInt("a")));
						}		
						
					}
					else {
						if (overedName.equals(name) && overedplatform.equals(platform) && overedWhen == when) {
							//���� �̸�
							TableRow rgb = yearcolor.findRow(Integer.toString(when), "year");
							if (whenYearColorMap.get(overedWhen)==color) {
								whenYearColorMap.put(overedWhen, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = whenYearColorMap.get(overedWhen);
								whenYearColorMap.put(overedWhen,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}							
						if (overedName.equals(name) && overedplatform.equals(platform) && overedesrb.equals(esrb)){
							//���� �̸�
							TableRow rgb = esrbColor.findRow((esrb), "esrb");
							if (esrbColorMap.get(overedesrb)==color) {
								esrbColorMap.put(overedesrb, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = esrbColorMap.get(overedesrb);
								esrbColorMap.put(overedesrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}															
					    }						
						if (overedName.equals(name) && overedplatform.equals(platform) && overedgenre.equals(genre)){
							//���� �̸�
							TableRow rgb = genrecolor.findRow((genre), "genrename");
							if (genreColorMap.get(overedgenre)==color) {
								genreColorMap.put(overedgenre, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = genreColorMap.get(overedgenre);
								genreColorMap.put(overedgenre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
						if (overedName.equals(name) && overedplatform.equals(platform)){
							//���� �̸�
							TableRow rgb = platformColor.findRow((platform), "platform");
							if (platformColorMap.get(overedplatform)==color) {
								platformColorMap.put(overedplatform, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = platformColorMap.get(overedplatform);
								platformColorMap.put(overedplatform,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
						if (overedName.equals(name) && overedplatform.equals(platform) && overedpublisher.equals(publisher)){
							//���� �̸�
							TableRow rgb = publisherColor.findRow((publisher), "publisher");
							if (publisherColorMap.get(overedpublisher)==color) {
								publisherColorMap.put(overedpublisher, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = publisherColorMap.get(overedpublisher);
								publisherColorMap.put(overedpublisher,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
					}
					
					if (nextPos != -1) {
						strokeWeight(7);
						stroke(color);
						line(nrX+w,nrY+h/2,nrX-w/2+d*wInterval,nNrY+h/2);
					}
					
					
					noStroke();
					fill(color);

					rect(nrX,nrY,w,w);
					cur = new Rectangle(nrX,(int) nrY, w,h);

				}
				else {
					int rX = (int) (rbx+(wn-1)*(wInterval*w-1));
					int rY = rbx+pos*(w+d);
					cur = new Rectangle(rX, rY, w,w);

					if (mousePressed && cur.contains((mouseX+worldCamera.pos.x)/zoom, (mouseY+worldCamera.pos.y)/zoom)) {
						//���콺�� �� �� �簢��
						overedName = name;
						overedplatform = platform;
						overedWhen = when;
						overedWeek = week;	
						overedgenre = genre;
						overedesrb = esrb;
						overedpublisher = publisher;
								
						TableRow rgb1 = yearcolor.findRow(Integer.toString(when), "year");
						TableRow rgb2 = esrbColor.findRow((esrb), "esrb");
						TableRow rgb3 = genrecolor.findRow((genre), "genrename");
						TableRow rgb4 = platformColor.findRow((platform), "platform");
						TableRow rgb5 = publisherColor.findRow((publisher), "publisher");
					
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
						}else if(platformColorMap.get(overedplatform)==color){
							platformColorMap.put(overedplatform, color(rgb4.getInt("r"),rgb4.getInt("g"),rgb4.getInt("b"),255));
							color = platformColorMap.get(overedplatform);						
							platformColorMap.put(overedplatform,color(rgb4.getInt("r"),rgb4.getInt("g"),rgb4.getInt("b"),rgb4.getInt("a")));
						}else if(publisherColorMap.get(overedpublisher)==color){
							publisherColorMap.put(overedpublisher, color(rgb5.getInt("r"),rgb5.getInt("g"),rgb5.getInt("b"),255));
							color = publisherColorMap.get(overedpublisher);						
							publisherColorMap.put(overedpublisher,color(rgb5.getInt("r"),rgb5.getInt("g"),rgb5.getInt("b"),rgb5.getInt("a")));
						}		
						
					}
					else {
						if (overedName.equals(name) && overedplatform.equals(platform) && overedWhen == when) {
							//���� �̸�
							TableRow rgb = yearcolor.findRow(Integer.toString(when), "year");
							if (whenYearColorMap.get(overedWhen)==color) {
								whenYearColorMap.put(overedWhen, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = whenYearColorMap.get(overedWhen);
								whenYearColorMap.put(overedWhen,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}							
						if (overedName.equals(name) && overedplatform.equals(platform) && overedesrb.equals(esrb)){
							//���� �̸�
							TableRow rgb = esrbColor.findRow((esrb), "esrb");
							if (esrbColorMap.get(overedesrb)==color) {
								esrbColorMap.put(overedesrb, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = esrbColorMap.get(overedesrb);
								esrbColorMap.put(overedesrb,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}															
					    }						
						if (overedName.equals(name) && overedplatform.equals(platform) && overedgenre.equals(genre)){
							//���� �̸�
							TableRow rgb = genrecolor.findRow((genre), "genrename");
							if (genreColorMap.get(overedgenre)==color) {
								genreColorMap.put(overedgenre, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = genreColorMap.get(overedgenre);
								genreColorMap.put(overedgenre,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
						if (overedName.equals(name) && overedplatform.equals(platform)){
							//���� �̸�
							TableRow rgb = platformColor.findRow((platform), "platform");
							if (platformColorMap.get(overedplatform)==color) {
								platformColorMap.put(overedplatform, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = platformColorMap.get(overedplatform);
								platformColorMap.put(overedplatform,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
							}							
						}
						if (overedName.equals(name) && overedplatform.equals(platform) && overedpublisher.equals(publisher)){
							//���� �̸�
							TableRow rgb = publisherColor.findRow((publisher), "publisher");
							if (publisherColorMap.get(overedpublisher)==color) {
								publisherColorMap.put(overedpublisher, color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
								color = publisherColorMap.get(overedpublisher);
								publisherColorMap.put(overedpublisher,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));								
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
		if(isSales){
		fill(150,150,0,100);
		rect(-500,1000, 80000,20);
		}
		if(isSales){			

			image(CROWN,-700,700);
			CROWN.resize(0, 500);

		}
			
		
		// UI �κ����� ī�޶��� ������ ���� ����
		// ��� ��ǥ�� �׻� ī�޶��� ��ǥ�� dx,dy �� ���� ���������
		
		scale(1/zoom);
			fill(UiColor);
				//�� �簢��
				float upperRectX = worldCamera.pos.x+dx;
				float upperRectY = worldCamera.pos.y+dy;
				
				image(Header,upperRectX,upperRectY);
    			//rect(upperRectX,upperRectY,width,100);
    			// �Ʒ���
    			float footerX = worldCamera.pos.x+dx;
    			float footerY = worldCamera.pos.y+dy+height-204;
    			image(Footer,footerX,footerY);
    			
				
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
    			Rectangle stacked = new Rectangle((int) downRectX+1166,(int) downRectY+11,63,81);
    			stacked.x-=worldCamera.pos.x;
    			stacked.y-=worldCamera.pos.y;
    			
    			//stack change
    			if (mousePressed && stacked.contains(mouseX, mouseY)) {	
    				if(isStacked) {
    					isStacked =false;
    					forRecordStack.pop();
    					
    				}
    				else {
    					isStacked = true;
    					
    					Set<String> set = null;
    					Set<Integer> set2;
    					Table colorcsv = null;
    					
    					LinkedHashMap<String, Integer> map = null;
    					LinkedHashMap<Integer, Integer> map2;
    					
    					String type = new String();
    					
    					if(isPlatformChangeClicked) {
    						type = "platform";
    					
    					}
    					else if (isGenreChangeClicked) {
    						type = "genre";
    					
    					}
    					else if (isEsrbChangeClicked) {
    						type = "esrb";
    					
    					}
    					else if (isPublisherChangedClicked) {
    						type = "publisher";
    					
    					}
    					else if (iswhenChangeClicked) {
    						type = "whenyear";
    					
    					}
    					
    					if(type.equals("platform")) {
    						set = platformSet;
    						map = platformColorMap;
    						colorcsv = platformColor;
    					} else if (type.equals("genre")) {
    						set = genreSet;
    						map =  genreColorMap;
    						colorcsv = genrecolor;
    					} else if (type.equals("esrb")) {
    						set = esrbSet;
    						map = esrbColorMap;
    						colorcsv = esrbColor;
    					} else if (type.equals("publisher")) {
    						set = publisherSet;
    						map = publisherColorMap;
    						colorcsv = publisherColor;
    					} else if (type.equals("whenyear")) {
    						set2 = whenYearSet;
    						map2 = whenYearColorMap;
    						colorcsv = yearcolor;
    						
    						for (Integer each : set2) {
    							TableRow rgb = colorcsv.findRow(Integer.toString(each), type);
    							try {
    								map2.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
    							} catch (NullPointerException e) {
    								map2.put(each,color(192,192,192));
    							}
    							map2.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
    						}		
    					}
    					
    					if(set!=null && map != null) {
    						for (String each : set) {
    							TableRow rgb = colorcsv.findRow(each, type);
    							
    							try {
    								map.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),rgb.getInt("a")));
    							} catch (NullPointerException e) {
    								map.put(each,color(192,192,192));
    							}
    						}		
    					}
    					
    					
    				}
    				delay(200);
    			}
    			
    			if(isStacked) {
    				image(stackIcon,downRectX+1166,downRectY+11);
    				
    				if(isPlatformChangeClicked) {
						changeRecord("platform");
					
					}
					else if (isGenreChangeClicked) {
						changeRecord("genre");
					
					}
					else if (isEsrbChangeClicked) {
						changeRecord("esrb");
					
					}
					else if (isPublisherChangedClicked) {
						changeRecord("publisher");
					
					}
					else if (iswhenChangeClicked) {
						changeRecord("whenyear");
					
					}
    			}
    			
    			
    			fill(255,255,255,0);
    			rect(downRectX+1166,downRectY+11,63,81);
    			
    			//salesChange click
    			Rectangle salesChange = new Rectangle((int) downRectX+1407,(int) downRectY+17,120,69);
    			
    			salesChange.x-=worldCamera.pos.x;
    			salesChange.y-=worldCamera.pos.y;
    			
    			if (mousePressed && salesChange.contains(mouseX, mouseY)) {
    				if(isSales) {
    					isSales = false;
    					zoom = normal;
    				}
    				else { 
    					isSales = true;
    					zoom = sales;
    				}
    				delay(200);
    			}
    			
    			fill(255,255,255,0);
    			rect(downRectX+1407,downRectY+17,120,69);
    			if(isSales){
    				image(salesIcon,downRectX+1407,downRectY+17);
    				

    			}
    				

			fill(255);
			textSize(30);
			text(overedName+"   SALES: "+overedWeek,worldCamera.pos.x+dx+1000,worldCamera.pos.y+dy+65);
			
			
		}
	
	void changeRecord(String type) {
		LinkedHashMap<Integer,Table> splitedRecords = new LinkedHashMap<Integer,Table>(); // ���� �̸����� �ֺ��� ��ŷ�� ��� �ִ� Map 
		
		// Table �� 30���� �ɰ��� ����
		for (int week = 0 ; week < weekNum ; week++) {
			int end = rectNum+week*rectNum ;
			Table subTable = new Table();
			
			for (int col = 0 ; col < table.getColumnCount() ; col++)
				subTable.addColumn(table.getColumnTitle(col));
				
			for (int start = rectNum * week; start < end ; start++) {
				TableRow row = table.getRow(start);
				subTable.addRow(row);
			
			}
			
			subTable.sort(type);
			splitedRecords.put(week,subTable);
		}
		
			namePos.clear();
		
		// �̸� ���� ��ŷ �� ���� ����.
		for (String name : nameSet) {
			int[] positions = new int[weekNum];
			Arrays.fill(positions, -1);
			
			namePos.put(name, positions);
		}
		
				
		int idx = 0;
		// �� �ָ��� ���� �̸����� ��ŷ�� ��� �ִ� namePos�� ��� ����

		for (Entry<Integer,Table> records : splitedRecords.entrySet()) {
		
			for (int i = 0 ; i < records.getValue().getRowCount() ; i++)
				records.getValue().getRow(i).setInt("pos", i+1);
			

			for (TableRow row : records.getValue().rows()) {
				namePos.get(row.getString("name"))[idx] = row.getInt("pos");
				
			}
			
			idx++;
		}

		Set<String> set = null;
		Set<Integer> set2;
		Table colorcsv = null;
		
		LinkedHashMap<String, Integer> map = null;
		LinkedHashMap<Integer, Integer> map2;
		
		if(type.equals("platform")) {
			set = platformSet;
			map = platformColorMap;
			colorcsv = platformColor;
		} else if (type.equals("genre")) {
			set = genreSet;
			map =  genreColorMap;
			colorcsv = genrecolor;
		} else if (type.equals("esrb")) {
			set = esrbSet;
			map = esrbColorMap;
			colorcsv = esrbColor;
		} else if (type.equals("publisher")) {
			set = publisherSet;
			map = publisherColorMap;
			colorcsv = publisherColor;
		} else if (type.equals("whenyear")) {
			set2 = whenYearSet;
			map2 = whenYearColorMap;
			colorcsv = yearcolor;
			
			for (Integer each : set2) {
				TableRow rgb = colorcsv.findRow(Integer.toString(each), type);
				try {
					map2.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
				} catch (NullPointerException e) {
					map2.put(each,color(192,192,192));
				}
				map2.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
			}		
		}
		
		if(set!=null && map != null) {
			for (String each : set) {
				TableRow rgb = colorcsv.findRow(each, type);
				
				try {
					map.put(each,color(rgb.getInt("r"),rgb.getInt("g"),rgb.getInt("b"),255));
				} catch (NullPointerException e) {
					map.put(each,color(192,192,192));
				}
			}		
		}
				
		
		forRecordStack.push(splitedRecords);
		
		redraw();
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
	

	
	




