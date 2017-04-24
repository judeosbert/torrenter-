import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class tcpClient implements Runnable {
    int fixedPieceSize = 1048576;
//    String serverAddress="192.168.43.113";
    String peerIP;
    String tracker = "localhost";



    byte[] sendData = new byte[fixedPieceSize];
    ArrayList<String> seederListArray;
    ArrayList<Integer> downloadedParts = new ArrayList<Integer>();
    DataOutputStream toServer;
    DataInputStream fromServer;
    Socket socketClient;
    startTorrenting starttorrenting = new startTorrenting();
    Boolean shownCompletedStatus = false;

    int torrentID,torrentSize,torrentPartNo,downloadedPartCount;
    String torrentName;
    public MainWIndow mainWindow;
    tcpClient(String peerIP, int torrentID, String torrentName, int torrentSize, int torrentPartNo)
        {
            this.peerIP = peerIP;
            this.torrentID = torrentID;
            this.torrentName = torrentName;
            this.torrentSize = torrentSize;
            if(this.torrentSize < fixedPieceSize)
            {
                fixedPieceSize = this.torrentSize;
            }
            this.torrentPartNo = torrentPartNo;
             mainWindow = new MainWIndow(1);
            mainWindow.initiateDBConnection();


            run();
        }
    @Override
    public void run() {
        System.out.println("TCP Client is about to start");

        //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {

            String response="";
            socketClient = new Socket();
            socketClient.connect(new InetSocketAddress(peerIP,5002),20000);
            toServer = new DataOutputStream(socketClient.getOutputStream());
            fromServer = new DataInputStream(socketClient.getInputStream());

//            System.out.print("Has sent Message to Server");
//            fromServer.read(recieveData);
//            response = new String(recieveData);
//            System.out.print("Response from server"+response);
//
            startGettingfileParts();
            socketClient.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startGettingfileParts() throws IOException {
        //System.out.println("Start Getting file parts");
        int partNo;
        boolean partsDownloaded = getPartsDownloaded();
        if(!partsDownloaded)
        {
            return;
        }

        partNo = generatePartNo();
            if(partNo < 0)
            {
                System.out.print("Error in Index Generator");
            }
            //System.out.print("Generated partNumber"+partNo);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("torrentID",torrentID);
                jsonObject.put("partNo",partNo);
                jsonObject.put("fileSize",torrentSize);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            String partDetails = String.valueOf(torrentID)+"~"+String.valueOf(partNo);
            String partDetails = jsonObject.toString();
            try {
                toServer.write(partDetails.getBytes());
                if(torrentSize < fixedPieceSize)
                {
                    fixedPieceSize = torrentSize;
                }
                System.out.print("Torrent Size"+torrentSize);

                byte[] recieveData;
                byte[] buffer = new byte[1]; //fixedPieceSize/16
                //System.out.print("\nReceive Data Size"+recieveData.length);
                int n;int totalBytesRead=0;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((n = fromServer.read(buffer)) > 0)
                {
//                   System.out.print("\nRead Bytes " +n);
                    outputStream.write(buffer);
                    totalBytesRead+=n;


                }
                System.out.print("CLIENT: Bytes Read from server"+totalBytesRead);
                System.out.print("Reading While Completed");
                //recieveData = new byte[totalBytesRead];
                recieveData = outputStream.toByteArray();

                System.out.print("\nReceived Data "+recieveData);
                System.out.print("\nPart received from Server");
                String hash = mainWindow.getParthash(torrentID,partNo);
                if(hash == null)
                {
                    //Discard due to error;
                    System.out.print("Error in reading the hash");
                }
                else
                {
                    System.out.print("\nOLd Hash"+hash);

                    String obtainedPartHash = DigestUtils.shaHex(recieveData);

                    System.out.print("\nNew Hash"+obtainedPartHash);
                    if(hash.equals(obtainedPartHash))
                    {
                        System.out.print("Good Part");
                        //Part is good. Write to file
                       String filePath = System.getProperty("user.home")+"/Torrenter++/"+torrentName;
                        RandomAccessFile raf = new RandomAccessFile(filePath,"rw");
                        if(partNo == 1)
                        {
                            raf.seek(0);
                        }
                        else
                        {
                            int seekPos = ((partNo-1)*fixedPieceSize);//+1
                            raf.seek(seekPos); //seekPos
                        }
                        raf.write(recieveData,0,totalBytesRead);
                        raf.close();
                        //Adding completed Acknowledge to DB
                        mainWindow.addToCompleted(torrentID,partNo);
                        System.out.print("Part No "+partNo +" completed");

                    }
                    else
                    {
                        System.out.print("\nBAD Hash Obtained");
                    }
                    socketClient.close();

                }



            } catch (IOException e) {
                //e.printStackTrace();
                System.out.print("Socket Write Error");



            }








    }




    private int generatePartNo() {

        Random rand = new Random();
        int n=0;
        boolean found;
        do
        {
             n = rand.nextInt(torrentPartNo)+1;

            found = downloadedParts.contains(n);
            if(!found && n > 0)
                return n;

        }while(found);

    return -1;

    }

    private boolean getPartsDownloaded() {


        downloadedParts = mainWindow.getDownloadedPartCount(torrentID);
        downloadedPartCount = downloadedParts.size();
        System.out.println("Downloaded Part Size"+downloadedPartCount);
        System.out.println("Complete Torrent Size"+torrentPartNo);
        System.out.print("TCP CLient says");
        if(downloadedPartCount != torrentPartNo)
        {
            return true;
        }
        else
        {
            //mainWindow.updateCompleteStatus(torrentID);
            return false;
        }
    }

}