import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jude on 13/9/16.
 */
public class getMetaData extends JFrame {
    private JPanel panel1;
    private JLabel torrentNumberLabel;


    public getMetaData()
    {
        initiateUI();
    }

    private void initiateUI() {
        add(panel1);
        setSize(400,200);
        setTitle("Obtaining Meta Data");
        setLocationRelativeTo(null);

    }
    public String getTorrentData(String torrentNumber) throws Exception
    {
        torrentNumberLabel.setText(torrentNumber);
        System.out.println("Torrent Number Obtained as "+torrentNumber);
        String urlS = "http://localhost/projects/torrent/getDetails/index.php";
        String parameters="torrentId="+torrentNumber;
        URL url = new URL(urlS);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(parameters);
        outputStream.flush();
        outputStream.close();

        int responseCode =connection.getResponseCode();
        System.out.println("Connectiong with"+urlS);
        System.out.println("Post Paramters"+parameters);
        System.out.println("Response Code"+responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while((inputLine = in.readLine()) != null)
        {
            response.append(inputLine);
        }
        in.close();
        System.out.println("Response :"+response.toString());

        dispose();
        return response.toString();


    }
    public static void  main(String args[])
    {
        EventQueue.invokeLater(()->{
            getMetaData o = new getMetaData();
            o.setVisible(true);

        });
    }
}
