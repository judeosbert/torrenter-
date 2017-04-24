import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;


public class startTorrenting {
    ArrayList<String> seederListArray = new ArrayList<String>();
    int torrentID,torrentSize,torrentPartNo;
    int fixedPieceSize = 1048576;
    String torrentName="";
    private Connection c=null;
    String userHome = System.getProperty("user.home");
//    String tracker = "192.168.43.113";
String tracker = "localhost";
    MainWIndow mainWindow = new MainWIndow(2);

    startTorrenting()
    {


    }

    startTorrenting(int torrentID,String torrentName)
    {
        this.torrentName = torrentName;
        this.torrentID = torrentID;
        System.out.print("Starting torrent for "+torrentID);
        mainWindow.initiateDBConnection();
        //Starting server and client in UDP for downloading and uploading
        torrentPartNo = mainWindow.getTotalPieceCount(torrentID);




            while(notAllPartsDownloaded(torrentID)) {
                try {
                    getSwarmList(torrentID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        System.out.print("File is COmpletely Downloaded");
        int row = universalData.getRowNo(String.valueOf(torrentID));
        int column = 3;
        //table1.setValueAt("Completed",row,column);
        mainWindow.updateCompleteStatus(torrentID);
        showCompletedMessage();

    }
    private boolean notAllPartsDownloaded(int torrentID) {

        ArrayList<Integer> downloadedParts;
        int downloadedPartCount;
        System.out.print("Torrent ID:" +torrentID);
        downloadedParts = mainWindow.getDownloadedPartCount(torrentID);
        downloadedPartCount = downloadedParts.size();
        System.out.println("Downloaded Part Size"+downloadedPartCount);
        System.out.println("Complete Torrent Size"+torrentPartNo);
        System.out.print("Whats system says" +(downloadedPartCount==torrentPartNo));
        if(downloadedPartCount != torrentPartNo)
        {
            return true;
        }
            System.out.print("Returning False");
            return false;
    }


    private void getSwarmList(int torrentID) throws IOException {


        String urlS = "http://"+tracker+"/projects/torrent/getSwarmList/index.php";
        String parameters="torrentID="+torrentID;
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
        try {
            processOutput(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //After successful attempt of requesting peer list we proceed to request for file parts from the different peers.
        //If the file size is less than the the fixed piece size we request the file from the first seeder in the list and save it.
        //Otherwise we get the data from different peers one by one till the file size is reached.
        //On failure from any one peer we just move on to the next list. 
        //Also update the peer list every 2 seconds looking for any new peers. This can be later given to assign priority
        getTorrentData();

        for (String peerIP:seederListArray) {
            tcpClient tcpClient = new tcpClient(peerIP, torrentID, torrentName, torrentSize, torrentPartNo);
        }

    }


    private void getTorrentData() {
      ArrayList<Integer> torrentData = new ArrayList<Integer>();
        MainWIndow mainWindow = new MainWIndow(1);
        mainWindow.initiateDBConnection();
        torrentData = mainWindow.getTorrentBasicData(torrentID);
        this.torrentSize = torrentData.get(0);
        this.torrentPartNo = torrentData.get(1);
        System.out.println("Data from Main Window"+torrentSize+"Part"+torrentPartNo);






    }



    private void processOutput(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        int seederCount = jsonObject.getInt("seederCount");




        JSONObject seedersList = jsonObject.getJSONObject("seeders");

        for (int i = 1; i <= seederCount; i++) {
            String seederIP = seedersList.getString(String.valueOf(i));

            seederListArray.add(seederIP);

        }

    }
    void showCompletedMessage()
    {

            String message = "The file "+torrentName+" has been completely downloaded";
            JOptionPane.showMessageDialog(null, message);
        }


}


