import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jude on 11/9/16.
 */
public class processFileAndMakeTorrent extends JFrame {

    private JPanel panel1;
    private JButton closeButton;
    private JTextField textField1;

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

        closeButton.addActionListener((ActionEvent event) ->
        {
            dispose();
        });

    }


    public void startCreatingTorrent(String[] fileInfo) throws Exception {

//        String url = "http://localhost/projects/p2p/makeTorrent/index.php";
//        URL requestURL = new URL(url);
//        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
//        connection.setRequestMethod("POST");
        String parameters = "fileName="+fileInfo[0];
        parameters+="filePath="+fileInfo[1];
        parameters+="fileLength="+fileInfo[2];
        System.out.println("File Length"+fileInfo[2]);
        String fileName = fileInfo[0];
        String filePath = fileInfo[1];
        int fixedPieceSize = 1048576;//KB-Bytes 1 MB Fixed Piece Size
        int fileSize = Integer.parseInt(fileInfo[2]);
        if(fileSize<fixedPieceSize)
        {
            fixedPieceSize = fileSize;
        }
        System.out.println("Fixed Piece Size"+fixedPieceSize);
        parameters +="pieceLength="+String.valueOf(fixedPieceSize);
        int numberOfPieces = (int) Math.ceil(((fileSize)/fixedPieceSize));
        if (numberOfPieces == 0)
        {
            numberOfPieces = 1 ;
        }
        System.out.println("Number of Pieces ="+String.valueOf(numberOfPieces));
        int finalFixedPieceSize = fixedPieceSize;
        String sha1="";
        System.out.println("Started Thread");
        sha1+=computeHash(fileInfo[1],fixedPieceSize,fileSize);
        //Rewrite with async task for updating progress in the UI and not make UI stuck
        //Log Details
        System.out.println(sha1);
        System.out.println("Hash Length "+sha1.length());
        //Sending data to server
        int torrentNumber = registerTorrent(fileName,filePath,fileSize,sha1,numberOfPieces);









    }

    private int registerTorrent(String fileName, String filePath, int fileSize, String sha1, int numberOfPieces) throws Exception {

        String url = "http://localhost/projects/torrent/register/index.php";
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
        MainWIndow mainWindow = new MainWIndow(1);
        mainWindow.initiateDBConnection();
        mainWindow.addMyTorrent(response.toString(),filePath);


        return 0;


    }

    private String computeHash(String s, int finalFixedPieceSize, int fileSize) {
        String hashCode="";
        try {
            System.out.println("Reading file and computing Hash");
            FileInputStream fileIn = new FileInputStream(s);
            byte[] buffer = new byte[finalFixedPieceSize];
            int start=0;
            while(start<fileSize) {
                fileIn.read(buffer, 0, finalFixedPieceSize);
                //System.out.println(buffer);
                hashCode += DigestUtils.shaHex(buffer);
                start=start+finalFixedPieceSize;
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


