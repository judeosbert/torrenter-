import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jude on 3/11/16.
 */
public class onlinePoller implements Runnable{

//    String tracker = "192.168.43.113";
    String tracker = "localhost";

    onlinePoller()
    {
        this.run();
    }

    @Override
    public void run() {
        while(true){
        String urlS = "http://"+tracker+"/projects/torrent/setOnline";
        try {
            URL url = new URL(urlS);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            //Debug
//            if(responseCode == 200)
//                System.out.print("Online Status Updated");
//            else
//                System.out.print("Online Status Update Failed");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
            try {
                Thread.sleep(5500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
