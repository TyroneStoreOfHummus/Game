import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;

public class uServer{
	public static final int version = 1;
	public static uSender list;	
	public static void main(String args[]) throws Exception{		
		(new Thread(new uServerThread(8889))).start();
		System.out.println("HI");
	}
}
class uServerThread implements Runnable{
	ServerSocket serv;
	public uServerThread(int port) throws Exception{
		this.serv = new ServerSocket(port);
		this.serv.setSoTimeout(1000);
		
		uServer.list = new uSender();
		(new Thread(uServer.list)).start();
	}
	public void run(){
		while(true){
			try{
			Socket ss = serv.accept();
			System.out.println(ss.getRemoteSocketAddress());
			
			(new Thread(new uServerListener(ss))).start();
			}catch(Exception e){
			}
		}
	}
	
}
class uServerListener implements Runnable{
	Socket s;
	Scanner in;
	InputStream stream;
	public uServerListener(Socket s){
		this.s = s;
		try{
		stream = s.getInputStream();
		in = new Scanner(s.getInputStream());
		uServer.list.addSocket(s, in.nextInt());
		}catch(IOException e){
			System.out.println("Crap");
		}
	}
	
	public void run(){
		JFrame frame = new JFrame();
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(500,500));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		while(true){
			if(in.hasNext()){
				String type = in.next();
				if(type.equals("image")){
					try{
					System.out.println("IMAGE");
					ByteArrayOutputStream img = new ByteArrayOutputStream();
					uSender.copy(stream, img);
					BufferedImage render = ImageIO.read(new ByteArrayInputStream(img.toByteArray()));
					label.setIcon(new ImageIcon(render));
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(type.equals("info")){		//Yay Lets just DUMP all this random crap
					System.out.println(s.getRemoteSocketAddress() + " : " + in.nextLine());					
				}
				//System.out.println("WEJRIOWEJRIWE" + type);
				
			}
			
			try{
			Thread.sleep(10);
			}catch(Exception e){}
		}
	}
}
class uSender implements Runnable{
	private ArrayList<Socket> ss = new ArrayList<Socket>();
	private Scanner in = new Scanner(System.in);
	public uSender(){
	}
	public void addSocket(Socket s, int ver){
		try{
			PrintStream out = new PrintStream(s.getOutputStream());
			out.flush();
			out.println((ver != uServer.version));
			if(ver != uServer.version){
				System.out.println("Updating" + ver);
				copy(new FileInputStream(new File("updator.jar")), out);
			}
			
			try{
				Thread.sleep(1000);		//YA HORRID SOL
			}catch(InterruptedException e){
				System.out.println("Interrupted at Socket");
			}
			out.println("setup " + Integer.toString(ss.size()));
			System.out.println(ss.size());
			
			this.ss.add(s);
		}catch(IOException e){
			System.out.println("Could not construct output stream");
		}
	}
	public void delSocket(int id) throws Exception{
		ss.remove(id);
		
		for(int i = id; i < ss.size(); i++){	
			PrintStream out = new PrintStream(ss.get(i).getOutputStream());
			out.println("setup " + Integer.toString(i - 1));		//This is NOT the setup command!!!!!
			out.flush();
		}
	}
	public void run(){
		while(true){
			if(in.hasNextLine()){				
				if(in.hasNextInt()){
					try{
						PrintStream out = new PrintStream(ss.get(in.nextInt()).getOutputStream());
						out.println(in.nextLine());
						out.flush();
					}catch(IOException e){
						e.printStackTrace();
					}
				}else{
					sendAll(in.nextLine(),"");
					
					for(int i = 0; i < ss.size(); i++){
						//System.out.println(i + " " + ss.get(i).getRemoteSocketAddress());
					}
				}
			}
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
        byte[] buf = new byte[1024];
        int len = 1024;
        while ((len = in.read(buf)) == 1024) {
            out.write(buf, 0, len);
			//System.out.println(len);
        }
		out.write(buf, 0, len);
		System.out.println("DONE" + len);
    }
}