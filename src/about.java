/**
 * Created by jude on 8/11/16.
 */

import javax.swing.*;
import java.awt.*;


public class about extends JFrame {



    private JPanel jPanel;
    private JLabel jLabel,name;
    private JLabel head;
    private JLabel names;

    public about()
    {
        setLayout(new FlowLayout());

        intiateUI();
    }


    private void intiateUI() {

        setTitle("About Torrenter ++ v1.0");
        Dimension d = new Dimension(500,500);
        setMinimumSize(d);
        setLayout( new GridLayout(5,1));
        setLocationRelativeTo(null);
        name.setText("Torrenter ++ V1.0");
        jLabel.setText("About");
        head.setText("Created By");
        names.setText("Jude Osbert K  ,  Vishnuram   , Vishnu Remesh");
        add(jPanel);

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
