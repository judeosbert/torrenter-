import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class MainWIndow extends JFrame {

    private JPanel jPanel;
    private JTextField torrentNumber;
    private JButton startTorrenting;
    private JTable table1;
    private ArrayList<String> seederListArray;
    private DefaultTableModel table;
    String userHome = System.getProperty("user.home");
    public Connection c = null;
    public JProgressBar processingBar;


    Statement stmt = null;
    public MainWIndow(int dummy)
    {
        //This is for others to just access the public variables and functions. This will prevent from redoing the complete work again.

    }

    public MainWIndow()
    {

        try {
            createDirectories();
            initiateDBConnection();

            Thread serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        udpServer udpserver = new udpServer();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            });
            serverThread.start();
            intiateUI();


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void createDirectories() {

        File file  = new File(userHome+"/Torrenter++");
        if(!file.exists())
        {
            if(file.mkdir())
            {
                System.out.println("Directory Created successfully");
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Direcrtory Creation failed. Please check permissions");
            }
        }

    }

    public void initiateDBConnection() {
        System.out.print("------------------- Initiate DB Connection ---------------");
        try
        {
            c = DriverManager.getConnection("jdbc:sqlite:"+userHome+"/Torrenter++/torrents.db");
           // c.setAutoCommit(true);
            System.out.println("Database connection established");
            createDBTables();
        }  catch (SQLException e) {
            e.printStackTrace();
        }




    }

    private void createDBTables() {

        try {
            stmt = c.createStatement();
            String query ="";
            query = "create table if not exists myTorrents(torrentID int(6) primary key,filepath varchar(255))";
            stmt.execute(query);
            query = "create table if not exists completedParts(completedId integer primary key autoincrement,torrentID int(6),partID int(6))";
            stmt.execute(query);
            query = "create table if not exists activeTorrents(torrentID int(6) primary key,torrentStatus varchar(10))";
            stmt.execute(query);
            query = "create table if not exists metadata(torrentID int(6) primary key,torrentName varchar(255),torrentSize int(255),torrentPartNo int(255),remotePath varchar(255),torrentSavePath varchar(255) )";
            stmt.execute(query);
            query = "create table if  not exists hashes(hashID INTEGER  PRIMARY KEY AUTOINCREMENT,torrentID int(6),partID int(255) ,hashValue varchar(255))";
            stmt.execute(query);
            System.out.println("Database created if not exists");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void intiateUI() throws JSONException {
        createMenuBar();
        processingBar = new JProgressBar(0,100);
        processingBar.setStringPainted(true);
        add(jPanel);
        table = (DefaultTableModel) table1.getModel();
        table.addColumn("Name");
        table.addColumn("Size");
        table.addColumn("Progress");
        table1 = new JTable(table);
        getActiveTorrents();
        torrentNumber.setToolTipText("Enter the torrent number");
        setTitle("Torrenter ++ v1.0 ");
        setSize(1200,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        startTorrenting.addActionListener((ActionEvent event)->{
            String torrentNoString = torrentNumber.getText().toString();
            if(torrentNoString.length()!=6)
            {
                JOptionPane.showMessageDialog(null,"Please type in a proper torrent number(xxxxxx)");

            }
            else {
                getMetaData getData = new getMetaData();
                getData.setVisible(true);
                try {
                    final String responseFinal = getData.getTorrentData(torrentNoString);

                    processResponse(responseFinal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            });




        }

    private void getActiveTorrents() {



        String query = "SELECT metadata.torrentID,metadata.torrentName,metadata.torrentSize,activeTorrents.torrentStatus FROM activeTorrents,metadata WHERE activeTorrents.torrentID = metadata.torrentID";
        try {
            ResultSet resultSet = null;
            PreparedStatement preparedStatement = c.prepareStatement(query);
            resultSet=preparedStatement.executeQuery();
            while(resultSet.next())
            {
                int torrentID = resultSet.getInt("torrentID");
                String torrentName = resultSet.getString("torrentName");
                int torrentSize = resultSet.getInt("torrentSize");
                String torrentStatus = resultSet.getString("torrentStatus");
                System.out.println(torrentName);
                addToTorrentTable(String.valueOf(torrentID), torrentName,torrentSize,torrentStatus);
                /*
                The line following has been removed because the same system is trying to run the same class twice. Instead of this line the line in addToTorrentTable
                is retained bcoz that is common for new as well as for the ones taken from the database
                 */
                //startTorrenting startTorrenting = new startTorrenting(torrentID,torrentName);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }



    private void processResponse(String response) throws JSONException {
        if (response.length() != 0) {
            JSONObject jsonObject = new JSONObject(response);
            int status = Integer.parseInt(jsonObject.getString("status"));
            JSONObject data = jsonObject.getJSONObject("data");
            String torrentID = data.getString("torrentId");
            String torrentName = data.getString("name");
            System.out.print("Torrent Found:"+status);
            if (status==0)
            {
                JOptionPane.showMessageDialog(null,"Invalid Torrent.Exiting");
                return;
//                System.exit(0);

            }
//            String torrentPath = data.getString("path");
//            torrentPath.replace("\\", "");
            String torrentHash = data.getString("hash");
            String torrentSize = data.getString("size");
            String remotePath = data.getString("path");
            String torrentPieceCount = data.getString("noOfPieces");

            createMetaDataFile(torrentID,torrentName, torrentSize,torrentPieceCount,torrentHash,remotePath);
            try {
                addToActiveTorrents(torrentID,"Downloading");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,"No seeder found for this torrent");
                return;
            }

        }
    }

    private void addToActiveTorrents(String torrentID,String progress) throws IOException {
            String query = "INSERT INTO activeTorrents(torrentID,torrentStatus) VALUES("+Integer.parseInt(torrentID)+",'Downloading')";
        try {
            PreparedStatement preparedStatement=c.prepareStatement(query);
            preparedStatement.executeUpdate();
            System.out.println("Torrent Added to Active Torrents DB");
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }




    }

    private void createMetaDataFile(String torrentID, String torrentName, String torrentSize, String torrentPieceCount, String torrentHash, String remotePath) {
        final JDialog dialog = new JDialog();
        dialog.setTitle("Please wait while we process the metadata");
        dialog.setModal(true);
        dialog.setSize(1000,400);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel jPanel = new JPanel();
        jPanel.setSize(600,200);
        processingBar.setSize(400,100);
        jPanel.add(processingBar);
        processingBar.setVisible(true);
        JLabel jLabel = new JLabel("Please wait while we add the torrent");
        jPanel.add(jLabel);
        //final JOptionPane optionPane = new JOptionPane("Hello world",JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        jPanel.setVisible(true);

        dialog.setLocationRelativeTo(null);
        dialog.setContentPane(jPanel);
        dialog.pack();
        Thread showDialog = new Thread(() -> dialog.setVisible(true));

            try {
                //SQL LITE DB implementation for meta data
                showDialog.start();
                String query = "insert into metadata (torrentID,torrentName,torrentSize,torrentPartNo,remotePath,torrentSavePath) VALUES (?,?,?,?,?,?)";
                PreparedStatement preparedStatement = c.prepareStatement(query);
                preparedStatement.setInt(1,Integer.parseInt(torrentID));
                preparedStatement.setString(2,torrentName);
                preparedStatement.setInt(3,Integer.parseInt(torrentSize));
                preparedStatement.setInt(4,Integer.parseInt(torrentPieceCount));
                preparedStatement.setString(5,remotePath);
                preparedStatement.setString(6,userHome+"/Torrenter++/"+torrentName);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                splitByNumber(torrentHash,Integer.parseInt(torrentID),Integer.parseInt(torrentPieceCount));
                int count = 0;
                //Stupid thing. This has been changed to the splitByNumber Function.
//                query = "insert into hashes (torrentID,partID,hashValue) VALUES (?,?,?)";
//                preparedStatement = c.prepareStatement(query);
//                for (String hashPart : hashParts) {
//
//                    preparedStatement.setInt(1,Integer.parseInt(torrentID));
//                    preparedStatement.setInt(2,count++);
//                    preparedStatement.setString(3,hashPart);
//                    preparedStatement.executeUpdate();
//
//
//r
//                }
//                preparedStatement.close();
                //Create a dummyfile to which the data will be later appended.

//                File file = new File(userHome+"/Torrenter++/"+torrentName);
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                try {
                    RandomAccessFile file = new RandomAccessFile(userHome+"/Torrenter++/"+torrentName,"rw");
                    file.setLength(Integer.parseInt(torrentSize));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.setVisible(false);
                showDialog.stop();
                addToTorrentTable(torrentID,torrentName,Integer.parseInt(torrentSize),"Downloading");



            }
        catch (SQLException e) {
            System.out.print("The file is already added."); ////////Show error and the image
            JOptionPane.showMessageDialog(null,"The file has already been added. Please clear the file and retry again.");
            dialog.setVisible(false);
            showDialog.stop();
            //e.printStackTrace();

            }





    }

    private void addToTorrentTable(String torrentID, String torrentName, int torrentSize, String progress) {


        String size = bytesToMB(torrentSize);

        Vector row = new Vector();
        row.add(torrentName);
        row.add(size+" MB");
        row.add(progress);
        table.addRow(row);
        System.out.println("Torrent Added to Active Torrents Table");
        Thread callClient = new Thread(() -> {
            startTorrenting startTorrenting1 = new startTorrenting(Integer.parseInt(torrentID),torrentName);
        });
        callClient.start(); //The function is a bit problamatic




    }

    private String bytesToMB(int torrentSize) {

        double intSize = torrentSize;
        intSize =  (intSize/Math.pow(1024,2));
        intSize = Math.round(intSize*100D)/100D;

        return String.valueOf(intSize);
    }

    private void splitByNumber(String text, int torrentID, int totalPieces) {
        processingBar.setValue(0);
        int inLength = text.length();
        //Lol this function is huge and implementation takes lot of time. The following is better
//        int arLength = inLength / number;
//        int left=inLength%number;
//        if(left>0){++arLength;}
//        String ar[] = new String[arLength];
//        String tempText=text;
//        for (int x = 0; x < arLength; ++x) {
//
//            if(tempText.length()>number){
//                ar[x]=tempText.substring(0, number);
//                tempText=tempText.substring(number);
//            }else{
//                ar[x]=tempText;
//            }
//
//        }
//
//
//        return ar;
        int hashStringLength = 40,count = 0;
        PreparedStatement preparedStatement;
        try {
        String query = "insert into hashes (torrentID,partID,hashValue) VALUES (?,?,?)";
        preparedStatement = c.prepareStatement(query);
        for(int x=0;x<inLength;x+=hashStringLength) {
            String hashText = text.substring(x, (x + hashStringLength));

//            System.out.println(x+1+" hash: "+hashText);


            preparedStatement.setInt(1, torrentID);
            preparedStatement.setInt(2, count++);
            preparedStatement.setString(3, hashText);
            preparedStatement.executeUpdate();
            processingBar.setValue(count);


        }

            preparedStatement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Integer> getTorrentBasicData(int torrentID)
    {
        ArrayList<Integer> torrentData = new ArrayList<>();
        String query = "SELECT metadata.torrentSize,metadata.torrentPartNo FROM metadata WHERE metadata.torrentID="+torrentID;
        try {
            ResultSet resultSet = null;
            PreparedStatement preparedStatement = c.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            int torrentSize = resultSet.getInt("torrentSize");
            int torrentPartNo = resultSet.getInt("torrentPartNo");
            preparedStatement.close();
            torrentData.add(torrentSize);
            torrentData.add(torrentPartNo);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return torrentData;
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        JMenuItem makeNewTorrent = new JMenuItem("Create Your Torrent");
        makeNewTorrent.setToolTipText("Use this option for making new torrent to share");
        makeNewTorrent.addActionListener((ActionEvent event) -> {
           createNewTorrent createTorrent = new createNewTorrent();
            createTorrent.setVisible(true);

        });



        JMenuItem exitButton = new JMenuItem("Exit");
        exitButton.setToolTipText("This will exit the Application");
        exitButton.addActionListener((ActionEvent event) ->{
            System.exit(0);
        });
        file.add(makeNewTorrent);
        file.add(exitButton);
        menuBar.add(file);
        setJMenuBar(menuBar);

    }


    public static  void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
            MainWIndow mainWindow = new MainWIndow();
            mainWindow.setVisible(true);

        });


    }


    public void addMyTorrent(String s, String filePath) {
        String query = "INSERT INTO myTorrents(torrentID,filepath) VALUES (?,?)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(query);
            preparedStatement.setInt(1,Integer.parseInt(s));
            preparedStatement.setString(2,filePath);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.print("Added torrent to your ownership");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getDownloadedPartCount(int torrentID) {
        String query = "SELECT partID FROM completedParts WHERE torrentID = ?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(query);
            preparedStatement.setInt(1,torrentID);
            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<Integer> downloadedParts = new ArrayList<Integer>();
            while(resultSet.next())
            {
                downloadedParts.add(resultSet.getInt("partID"));
            }
            return downloadedParts;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }
}