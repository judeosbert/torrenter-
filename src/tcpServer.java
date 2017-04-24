import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;


public class tcpServer implements Runnable
{
    int fixedPieceSize = 1048576;
    ServerSocket serverSocket;
    String recieveData = "";
    byte[] sendData = new byte[fixedPieceSize];


    public tcpServer() throws SocketException {
        try {
            serverSocket = new ServerSocket(5002);
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