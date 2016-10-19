import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Created by jude on 12/10/16.
 */
public class udpServer implements Runnable
{
    int fixedPieceSize = 60;
    ServerSocket serverSocket;
    String recieveData = "";
    byte[] sendData = new byte[fixedPieceSize];


    public udpServer() throws SocketException {
        try {
            serverSocket = new ServerSocket(5001);
        } catch (IOException e) {
            e.printStackTrace();
        }
        run();
    }

    @Override
    public void run() {
        System.out.println("TCP Server is about to start");
        while(true)
        {

            try {

                clientHandler clienthandler;
                clienthandler = new clientHandler(serverSocket.accept());
                Thread clientThread = new Thread(clienthandler);
                clientThread.start();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}