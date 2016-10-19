import java.io.*;
import java.net.Socket;

/**
 * Created by jude on 13/10/16.
 */
public class clientHandler implements Runnable {
    String recieveData;
    int fixedPieceSize = 60;
    byte[] sendData = new byte[fixedPieceSize];
    byte[] readData = new byte[fixedPieceSize];
    Socket clientSocket=null;

    clientHandler(Socket connectionSocket) {
        this.clientSocket = connectionSocket;

    }

    @Override
    public void run() {
        System.out.print("Client Handler is running");
        DataInputStream fromClient = null;
        try {
            fromClient = new DataInputStream(clientSocket.getInputStream());

            DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());

            fromClient.read(readData);
            recieveData = new String(readData);
            System.out.println("The client requests data for " + recieveData);
            /* Program Logic
            * the torrentNumber and partnumber is searched for and if the file is found then we see how many parts have been completed. If the
            * completed number of part is less than the received part number, then we select that bytes from the system and sent it back to the client as 1 mb.
            * This happens by looking at the localpath. If you are the owner of the file then we look at the remote path and do the same only if the file exists.
            */






            String sentDataS = "Hello man";
            sendData = sentDataS.getBytes();
            toClient.write(sendData);




        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
