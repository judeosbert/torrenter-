import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;



public class createNewTorrent extends JFrame {
    private JLabel selectFileNote;
    private JPanel panel1;
    private JButton chooseFileButton;
    private JTextArea fileInformationTextArea;
    private JButton closeButton;
    private JButton createTorrentButton;


    public createNewTorrent()
    {
        setLayout(new FlowLayout());
        intiateUI();
    }

    private void intiateUI() {
        setTitle("Create new Torrent");
        Dimension d = new Dimension(1000,800);
        setMinimumSize(d);
        setLayout( new GridLayout(5,1));
        setLocationRelativeTo(null);
        selectFileNote = new JLabel("Choose the file you need to share\n");
        selectFileNote.setHorizontalAlignment(SwingConstants.CENTER);
        add(panel1);
        createTorrentButton.setVisible(false);

        //File chooser code starts here
        final JFileChooser fileChooser = new JFileChooser();

        String[] fileInfo = new String[4];
        chooseFileButton.addActionListener(e -> {
            int permission = fileChooser.showOpenDialog(null);
            if (permission == JFileChooser.APPROVE_OPTION)
            {
             File file= fileChooser.getSelectedFile();
                fileInformationTextArea.setText("File Information");
                fileInformationTextArea.append("\nName:"+file.getName());
                fileInformationTextArea.append("\nPath:"+file.getAbsolutePath());
                fileInformationTextArea.append("\nSize:"+file.length()+" bytes");

                fileInformationTextArea.setEditable(false);
                fileInfo[0]=file.getName();
                fileInfo[1]=file.getAbsolutePath();
                fileInfo[2]=String.valueOf(file.length());

            }
            else
            {
                System.out.println("Error in Permission");
            }

            if(fileInformationTextArea.getText()!="File Information")
            {
                createTorrentButton.setVisible(true);
            }


        });


        createTorrentButton.addActionListener((ActionEvent event) -> {
            EventQueue.invokeLater(()->{
                processFileAndMakeTorrent newObject = new processFileAndMakeTorrent();
                newObject.setVisible(true);
                try {
                    createTorrentButton.setVisible(false);

                    newObject.startCreatingTorrent(fileInfo);
                }
                catch (Exception e)
                {
                    System.out.println("Exception in Connection to Network");
                }
            });
        });

        closeButton.addActionListener((ActionEvent event) ->{
            dispose(); // TO close current Active Window
        });



        

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
