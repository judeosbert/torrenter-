import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class clientHandler implements Runnable {
    String recieveData;
    int fixedPieceSize = 1048576;
    int partNumber = 0,torrentSize=0;
    byte[] sendData = new byte[fixedPieceSize];
    byte[] readData = new byte[fixedPieceSize];
    Socket clientSocket=null;
    MainWIndow mainWIndow = new MainWIndow(1);
    DataOutputStream toClient;
    DataInputStream fromClient;
    clientHandler(Socket connectionSocket) {
        this.clientSocket = connectionSocket;

    }

    @Override
    public void run() {
        System.out.print("Client Handler is running");
        try {
            clientSocket.setTcpNoDelay(true);

        } catch (SocketException e) {
            e.printStackTrace();
        }
        DataInputStream fromClient = null;
        try {
            fromClient = new DataInputStream(clientSocket.getInputStream());

            toClient = new DataOutputStream(clientSocket.getOutputStream());

            fromClient.read(readData);
            recieveData = new String(readData);
            System.out.println("The client requests data for " + recieveData);
            /* Program Logic
            * the torrentNumber and partnumber is searched for and if the file is found then we see how many parts have been completed. If the
            * completed number of part is less than the received part number, then we select that bytes from the system and sent it back to the client as 1 mb.
            * This happens by looking at the localpath. If you are the owner of the file then we look at the remote path and do the same only if the file exists.
            */
            int torrentNumber = 0;
            try {
                JSONObject jsonObject = new JSONObject(recieveData);
                torrentNumber = jsonObject.getInt("torrentID");
                partNumber = jsonObject.getInt("partNo");
                torrentSize = jsonObject.getInt("fileSize");
                if(torrentSize<fixedPieceSize)
                {
                    fixedPieceSize = torrentSize;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            //Checking if the file Path belongs to this owner
            checkIfIamOwner(torrentNumber);


        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkIfIamOwner(int torrentNumber) {
        mainWIndow.initiateDBConnection();
        String filepath = mainWIndow.checkIfOwner(torrentNumber);
        if(filepath.equals("fail"))
        {
            System.out.print("I AM not the owner");
            //Go looking in the meta data and completedParts
        }
        else
        {
            getFilePart(partNumber,filepath);
        }
    }

    private void getFilePart(int partNumber, String filepath) {
            System.out.print("Looking for part Number "+partNumber+"For file in "+filepath);
        try {
            RandomAccessFile file = new RandomAccessFile(filepath,"rw");
            System.out.print("Opening File");
            int offset = (partNumber-1)*fixedPieceSize;
            file.seek(offset);
            byte[] buffer = new byte[fixedPieceSize];
            System.out.print("Buffer Data Size"+buffer.length);
            int n = file.read(buffer,0,fixedPieceSize);
            System.out.print("\nBytes Read from File "+n);
            file.close();
            System.out.print("File has been read.");
            System.out.print("\nBuffer Data"+buffer);
            String sha="";
            sha+=DigestUtils.shaHex(buffer);
            System.out.print("\n Sha "+ sha);
            //toClient.flush();
            toClient.write(buffer,0,buffer.length);

            toClient.flush();
            toClient.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
