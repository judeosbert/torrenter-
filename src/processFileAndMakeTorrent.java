import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class processFileAndMakeTorrent extends JFrame {

    private JPanel panel1;
    private JButton closeButton;
    private JTextField textField1;
    private JProgressBar progressBar1;
    int numberOfPieces;
    int fixedPieceSize = 1024*1024;
    //String tracker = "192.168.43.113";
   String tracker = "localhost";
    String sha1="";
    processFileAndMakeTorrent()
    {
        setLayout(new FlowLayout());
        intiateUI();
    }

    private void intiateUI() {
        setTitle("Creating torrent");
        setSize(400,200);
        setLocationRelativeTo(null);
        add(panel1);
        textField1.setText("Please Wait");
        textField1.setEditable(false);
        textField1.setHorizontalAlignment(SwingConstants.CENTER);
        progressBar1 = new JProgressBar(0,100);
        progressBar1.setValue(0);
        progressBar1.setStringPainted(true);

        add(progressBar1);
        closeButton.addActionListener((ActionEvent event) ->
        {
            dispose();
        });

    }


    public void startCreatingTorrent(String[] fileInfo) throws Exception {

//        String url = "http://192.168.1.3/projects/p2p/makeTorrent/index.php";
//        URL requestURL = new URL(url);
//        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
//        connection.setRequestMethod("POST");
        String parameters = "fileName="+fileInfo[0];
        parameters+="filePath="+fileInfo[1];
        parameters+="fileLength="+fileInfo[2];
        System.out.println("File Length"+fileInfo[2]);
        String fileName = fileInfo[0];
        String filePath = fileInfo[1];
        //KB-Bytes 1 MB Fixed Piece Size
        int fileSize = Integer.parseInt(fileInfo[2]);
        double fileSizeDouble = bytesToMB(fileSize);
        System.out.print("File Size in MB "+fileSizeDouble);
        if(fileSize<fixedPieceSize)
        {
            fixedPieceSize = fileSize;
        }
        System.out.println("Fixed Piece Size"+fixedPieceSize);
        parameters +="pieceLength="+String.valueOf(fixedPieceSize);
        numberOfPieces = (int) Math.ceil((fileSizeDouble));
        if (numberOfPieces == 0)
        {
            numberOfPieces = 1 ;
        }
        System.out.println("Number of Pieces ="+String.valueOf(numberOfPieces));
        int finalFixedPieceSize = fixedPieceSize;

        System.out.println("Started Thread");
        Thread computeHash = new Thread(new Runnable() {
            @Override
            public void run() {
                sha1+=computeHash(fileInfo[1],fixedPieceSize,fileSize);

        //Rewrite with async task for updating progress in the UI and not make UI stuck
        //Log Details
        System.out.println(sha1);
        System.out.println("Hash Length "+sha1.length());
        //Sending data to server
                try {
                    int torrentNumber = registerTorrent(fileName,filePath,fileSize,sha1,numberOfPieces);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });


        computeHash.start();




    }
    private double bytesToMB(long torrentSize) {

        double intSize = torrentSize;
        intSize =  (intSize/Math.pow(1024,2));
        intSize = Math.round(intSize*100D)/100D;

        return intSize;
    }



    private int registerTorrent(String fileName, String filePath, int fileSize, String sha1, int numberOfPieces) throws Exception {

        String url = "http://"+tracker+"/projects/torrent/register/index.php";
        URL registerURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) registerURL.openConnection();

        conn.setRequestMethod("POST");

        String urlParameters="fileName="+fileName+"&filePath="+filePath+"&fileSize="+String.valueOf(fileSize)+"&sha1="+sha1+"&pieceNumbers="+String.valueOf(numberOfPieces);
        conn.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending Request to "+url);
        System.out.println("\nParameters are "+urlParameters);
        System.out.println("\nResponse Code "+responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null )
        {
            response.append(inputLine);
        }
        in.close();
        System.out.println("Torrent Number "+response.toString());
        textField1.setText(response.toString());
        MainWIndow mainWindow1 = new MainWIndow(1);
        mainWindow1.initiateDBConnection();
        mainWindow1.addMyTorrent(response.toString(),fileName,fileSize,filePath);


        return 0;


    }

    private String computeHash(String s, int finalFixedPieceSize, int fileSize) {
        String hashCode="";
        int hashedPieceCount=0;
        try {
            System.out.println("Reading file and computing Hash");
            RandomAccessFile fileIn = new RandomAccessFile(s,"rw");
            fileIn.seek(0);
            byte[] buffer = new byte[finalFixedPieceSize];
            int start=0;
            while(start<fileSize) {
                int n = fileIn.read(buffer, 0, finalFixedPieceSize);
                byte[] filePart;
                //System.out.println("\nBytes read "+n);
                filePart = Arrays.copyOfRange(buffer,0,n);
                hashCode += DigestUtils.shaHex(filePart);
                start=start+finalFixedPieceSize;
                hashedPieceCount++;
                System.out.println("Hash Count" + hashedPieceCount);
                double dPercent = (double)hashedPieceCount/(double)numberOfPieces;
                int percent = (int) ((dPercent)*100);
                System.out.println("PERCENT"+percent);
                progressBar1.setValue(percent);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        //Read specific bytes from the input file and compute the sha hash for it and return it by concatenating.





        return hashCode;
    }




}


