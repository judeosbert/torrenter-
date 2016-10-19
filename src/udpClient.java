import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class udpClient implements Runnable {
    int fixedPieceSize = 60;
    String serverAddress="localhost";//Replace by ip address of permanent server

    byte[] recieveData = new byte[fixedPieceSize];
    byte[] sendData = new byte[fixedPieceSize];
    ArrayList<String> seederListArray;
    ArrayList<Integer> downloadedParts = new ArrayList<Integer>();
    int torrentID,torrentSize,torrentPartNo,downloadedPartCount;
    String torrentName;
    udpClient(ArrayList<String> seederListArray, int torrentID, String torrentName, int torrentSize, int torrentPartNo)
        {
            this.seederListArray = seederListArray;
            this.torrentID = torrentID;
            this.torrentName = torrentName;
            this.torrentSize = torrentSize;
            this.torrentPartNo = torrentPartNo;
            run();
        }
    @Override
    public void run() {
        System.out.println("TCP Client is about to start");

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {
            String sendData = "Hello Server";
            String response="";
            Socket socketClient = new Socket(serverAddress,5001);
            DataOutputStream toServer = new DataOutputStream(socketClient.getOutputStream());
            DataInputStream fromServer = new DataInputStream(socketClient.getInputStream());
            toServer.write(sendData.getBytes());
            System.out.print("Has sent Message to Server");
            fromServer.read(recieveData);
            response = new String(recieveData);
            System.out.print("Response from server"+response);
            socketClient.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startGettingfileParts() {

        while(notAllPartsDownloaded())
        {
            //Generate partno such that it is not in the completed array list.
            // Take the first ip from the array and request the part no in increasing order. Set time out.

                //Send Packet Frame ---torrentID -- partNo
                //Response data as byte,identify part no by looking at the request history.
                //ON success or on time out remove the the entry from request history.
        }


    }

    private boolean notAllPartsDownloaded() {

        MainWIndow mainWindow = new MainWIndow(1);
        mainWindow.initiateDBConnection();
        downloadedParts = mainWindow.getDownloadedPartCount(torrentID);
        downloadedPartCount = downloadedParts.size();
        System.out.println("Downloaded Part Size"+downloadedPartCount);
        return downloadedPartCount != torrentPartNo;
    }

}