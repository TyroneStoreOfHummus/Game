import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class server{
	public static final int version = 10;
	public static listener list;
	public static terrain ter;
	public static void main(String args[]) throws Exception{
		server.ter = new terrain();
		JFrame frame = new JFrame("Testo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JLabel("TESTO", JLabel.CENTER), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		(new Thread(new serverThread(8888, frame))).start();
		System.out.println("HI");
	}
	
	
	
	
}
class serverThread implements Runnable{
	ServerSocket serv;
	JFrame frame;
	public serverThread(int port, JFrame frame) throws Exception{
		this.serv = new ServerSocket(port);
		this.serv.setSoTimeout(1000);
		this.frame = frame;
		
		server.list = new listener();
		frame.addKeyListener(server.list);	
		
	}
	public void run(){
		while(true){
			try{
			Socket ss = serv.accept();
			System.out.println(ss.getRemoteSocketAddress());
			
			(new Thread(new serverListener(ss))).start();
			}catch(Exception e){
			}
		}
	}
	
}
class serverListener implements Runnable{
	Socket s;
	Scanner in;
	public serverListener(Socket s){
		this.s = s;
		try{
		in = new Scanner(s.getInputStream());
		
		
		server.list.addSocket(s, in.nextInt());
		}catch(IOException e){
			System.out.println("Crap");
		}
	}
	
	public void run(){
		while(true){
			if(in.hasNext()){
				String type = in.next();
				if(type.equals("text")){
					String p = in.nextLine();
					System.out.println(p);
					server.list.sendAll("text", p);
				}else if(type.equals("pos")){
					server.list.sendAll("pos", in.nextLine());
					System.out.println("sent");
				}else if(type.equals("ter")){
					String lines = in.nextLine();
					server.ter.decode(lines);
					server.list.sendAll("ter",lines);
				}
				
			}
			try{
			Thread.sleep(10);
			}catch(Exception e){}
		}
	}
}
class listener implements KeyListener{
	private ArrayList<Socket> ss = new ArrayList<Socket>();
	
	private String curr = "";
	public listener(){
	}
	public void addSocket(Socket s, int ver){
		try{
			PrintStream out = new PrintStream(s.getOutputStream());
			out.println(server.ter.toString());
			out.println((ver != server.version));
			
			if(ver != server.version){
				this.wait(500);
				copy(new FileInputStream(new File("client.java")), out);
			}
			
			this.wait(500);
			out.println("setup " + Integer.toString(ss.size()));
			System.out.println(ss.size());
			this.ss.add(s);
		}catch(IOException e){
			System.out.println("Could not construct output stream");
		}
	}
	public void wait(int mili){
		try{
			Thread.sleep(mili);
		}catch(InterruptedException e){System.out.println("Uh oh");}
	}
	public void delSocket(int id) throws Exception{
		ss.remove(id);
		
		for(int i = id; i < ss.size(); i++){	
			PrintStream out = new PrintStream(ss.get(i).getOutputStream());
			out.println("setup " + Integer.toString(i - 1));		//This is NOT the setup command!!!!!
			out.flush();
		}
	}
	public void keyPressed(KeyEvent e){
		char c = e.getKeyChar();
		if((int)c == 10){
			sendAll("text", curr);
			curr = "";
		}else if((int) c != 65535){
			curr += e.getKeyChar();
		}
	}
	public void sendAll(String header, String message) {
		try{
			for(int i = 0; i < ss.size(); i++){
				PrintStream out = new PrintStream(ss.get(i).getOutputStream());
				out.println(header + " " + message);
				out.flush();
			}
		}catch(Exception e){}
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
			System.out.println(len);
        }
    }
}