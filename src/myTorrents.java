/**
 * Created by jude on 8/11/16.
 */

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;


public class myTorrents extends JFrame {


    private JTable table1;
    private JLabel jLabel;
    private JPanel jPanel;
    private DefaultTableModel table;
    private MainWIndow mainWindow = new MainWIndow(1);
    public myTorrents()
    {
        setLayout(new FlowLayout());
        mainWindow.initiateDBConnection();
        intiateUI();
    }


    private void intiateUI() {
        add(jPanel);
        setTitle("My Torrents");
        Dimension d = new Dimension(1000,500);
        setMinimumSize(d);
        setLayout( new GridLayout(5,1));
        setLocationRelativeTo(null);
        JLabel jlabel = new JLabel();
        jLabel.setText("My Torrents");
        table = (DefaultTableModel) table1.getModel();
        table.addColumn("ID");
        table.addColumn("Name");
        table.addColumn("Size");
        table.addColumn("Path");

        table1 = new JTable(table);
        table1.setVisible(true);
        getMyTorrents();



        }

    private void getMyTorrents() {
        String response = mainWindow.getMyTorrents();
        JSONObject result = null;
        try {
            result = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int i=1;
        try {
        int count= result.getInt("count");

            while(i<count)
            {

                int torrentID = result.getInt("result"+String.valueOf(i)+"ID");
                String name = result.getString("result"+String.valueOf(i)+"name");
                String filePath = result.getString("result"+String.valueOf(i)+"filePath");
                int size = result.getInt("result"+String.valueOf(i)+"size");
                addToTorrentTable(String.valueOf(torrentID),name,size,filePath);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void addToTorrentTable(String torrentID, String torrentName, int torrentSize,String filePath) {


        String size = bytesToMB(torrentSize);

        Vector row = new Vector();
        row.add(torrentID);
        row.add(torrentName);
        row.add(size+" MB");
        row.add(filePath);

        table.addRow(row);
        System.out.println("Torrent Added to Active Torrents Table");




    }

    private String bytesToMB(long torrentSize) {

        double intSize = torrentSize;
        intSize =  (intSize/Math.pow(1024,2));
        intSize = Math.round(intSize*100D)/100D;

        return String.valueOf(intSize);
    }

    public static void main(String[] args)
    {
        EventQueue.invokeLater(()->
        {
            createNewTorrent create = new createNewTorrent();
            create.setVisible(true);
        });
    }


}
