/**
 * Author: Yuxi Zhang
 * Purpose: Java GUI that connects to a server to get prime number
 */

import org.omg.PortableInterceptor.SUCCESSFUL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends JPanel implements ActionListener {

    //GUI components

    //Labels for text fields
    private JLabel ipLabel;
    private JLabel bitlenLabel;
    private JLabel primeLabel;
    private JLabel msgLabel;

    //Strings for the labels
    private static String ipString = "IP Address: ";
    private static String bitlenString = "Bit Length: ";
    private static String primeString  = "Prime Number: ";
    //other strings
    private static String primeAreaString = "Result not yet available";
    private static String ipFormatError = "Ip address is not appropriately formatted";
    private static String bitLenError = "Please enter an integer greater than 1";

    //Text fields for entering data
    private JTextField ipField = new JTextField();
    private JTextField bitlenField = new JTextField();
    private TextArea primeField = new TextArea("",1,20, TextArea.SCROLLBARS_BOTH);


    //Buttons "GenPrime", "Stop", and "Test"
    JButton genBt = new JButton("GenPrime");
    JButton stopBt = new JButton("Stop");
    JButton testBt = new JButton("Test");


    //two booleans needed to enable buttons
    boolean ipCorrect;
    boolean bitlenCorrect;

    //custome verifier to check inputs in text fields
    private myVerifier v = new myVerifier();


    //variables for network
    Thread clientThread;
    //IP address of machinf (server)
    protected String ip_address = null;
    //Server socket
    protected Socket client_sock = null;
    public static int PORT = 12345;
    BufferedReader from = null;
    PrintWriter to = null;



    public Client(){
        super(new GridLayout(0,1));

        //create the labels
        ipLabel = new JLabel(ipString);
        bitlenLabel = new JLabel(bitlenString);
        primeLabel = new JLabel(primeString);
        msgLabel = new JLabel();
        msgLabel.setPreferredSize(new Dimension(200, 0));
        msgLabel.setForeground(Color.RED);

        //create the text fields

        ipField.setInputVerifier(v);
        ipField.setPreferredSize(new Dimension(150,0));
        bitlenField.setInputVerifier(v);

        //label\text field pairs
        ipLabel.setLabelFor(ipField);
        bitlenLabel.setLabelFor(bitlenField);
        primeLabel.setLabelFor(primeField);

        //initially disable buttons
//        genBt.setEnabled(false);
        stopBt.setEnabled(false);
        genBt.addActionListener(this);
        stopBt.addActionListener(this);

        ipCorrect = false;
        bitlenCorrect = false;

        //layout

        //labels "ip", "bitlen" in a panel
        JPanel topLabelPane = new JPanel(new GridLayout(0,1));
        topLabelPane.add(ipLabel);
        topLabelPane.add(bitlenLabel);

        //text fields "ip", "bitlen" in a panel
        JPanel topFieldPane = new JPanel(new GridLayout(0,1));
        topFieldPane.add(ipField);
        topFieldPane.add(bitlenField);

        JPanel topPane = new JPanel();
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPane.add(topLabelPane, BorderLayout.CENTER);
        topPane.add(topFieldPane, BorderLayout.WEST);

        //buttons panel
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(genBt);
        buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPane.add(stopBt);
        buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPane.add(testBt);

        //bottom panel
        JPanel bottomPane = new JPanel();
        bottomPane.add(primeLabel, BorderLayout.WEST);
        bottomPane.add(primeField, BorderLayout.EAST);

        // three top level panes stack together
        super.add(topPane);
        super.add(msgLabel);
        super.add(buttonPane);
        super.add(bottomPane);

    }

    private class myVerifier extends InputVerifier{

        @Override
        public boolean verify(JComponent input) {
            //checks input
            if (input == ipField){

                return checkIpField();
            }
            if(input == bitlenField){
                return checkBitlenField();
            }
            else {
                //which should not happen
                return true;
            }

        }

        private void showIpError(){
            ipField.setBackground(new Color(255,0,0));

            msgLabel.setText(ipFormatError);
            Client.this.add(msgLabel);
            // paint immediately, although may not be thread safe
//            msgLabel.paintImmediately(msgLabel.getVisibleRect());
            System.out.println("should immediately print"+ msgLabel.getText());

        };


        protected boolean checkIpField(){
            //assume we only use IPv4 dot-decimal notation
            //consisting of four decimal numbers each ranging from 0 to 255
            //separated by dot. eg. ---.---.---.---

            //clear potential error msg from previous error checking
            msgLabel.setText("");
            String IpStr = ipField.getText();
            boolean wellFormatted = IpStr.matches("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
            if (!wellFormatted){
                showIpError();
                ipCorrect = false;
                genBt.setEnabled(false);
                return false;
            }
            String[] nums = IpStr.split("\\.");
            for (String numlit:nums){
                try {
                    int num = Integer.parseInt(numlit);
                    if (num<0 || num >255) {
                        showIpError();
                        genBt.setEnabled(false);
                        ipCorrect = false;
                        return false;
                    }
                }catch (NumberFormatException e){
                    showIpError();
                    genBt.setEnabled(false);
                    ipCorrect = false;
                    return false;
                }
            }
            //het here means all integers are within right range, no format error
            //set background color back to white
            ipField.setBackground(Color.white);
            ipCorrect = true;
            updateGenBt();
            return true;

        }
        private void showLenError(){
            bitlenField.setBackground(new Color(255,0,0));
            msgLabel.setText(bitLenError);
        }

        protected boolean checkBitlenField(){
            //when check this, means value for ip address is valid
            //clear previous error msg if any
            msgLabel.setText("");
            String bitlenStr = bitlenField.getText();
            try{
                int lenValue = Integer.parseInt(bitlenStr);
                if (lenValue<2){
                  showLenError();
                  genBt.setEnabled(false);
                  bitlenCorrect=false;
                  return false;
                }
            }catch (NumberFormatException e){
                showLenError();
                genBt.setEnabled(false);
                bitlenCorrect = false;
                return false;
            }
            bitlenField.setBackground(Color.white);
            bitlenCorrect = true;
            updateGenBt();
            return true;
        }

        private void updateGenBt(){
                if (ipCorrect && bitlenCorrect){
                    genBt.setEnabled(true);
                }
        }

    };

    Runnable client_runnable = ()->{
        try {
            ip_address = ipField.getText(); //verified: correct format

            client_sock = new Socket(ip_address, PORT);

            from = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));

            to = new PrintWriter(client_sock.getOutputStream(), true);

            stopBt.setEnabled(true);

        }catch (IOException e){

            msgLabel.setText("Cannot connect to "+ip_address);
            System.out.println("Cannot connect to " + ip_address);

            return;

        }

        //now we are connected
        System.out.println("Connected: "+ client_sock.getInetAddress());

        try {
            String bitlength = bitlenField.getText();
            to.println(bitlength);

            String primeLit= from.readLine();
            System.out.println("prime received: " + primeLit);
//            primeField.setEditable(true);
            primeField.setText(primeLit);
            //enable genPrimtBt again
            genBt.setEnabled(true);
            //disable stopBt again
            stopBt.setEnabled(false);
            //then this thread terminates
            //TODO
            //client_sock.close();

        } catch (IOException e) {
        }

    };



    @Override
    public void actionPerformed(ActionEvent e) {
        //listen for button click events

        //listen for click on "genBt"
        if (e.getSource()==genBt){

            if (ipCorrect && bitlenCorrect){
                //try to connect to server, this part in separate thread

                clientThread = new Thread(client_runnable);
                clientThread.start();
                //clientThread kicks off

                //not enable stopBt here but after get a connection

                //disable egnBt while receiving prime
                genBt.setEnabled(false);
                //set textArea text
                primeField.setText(primeAreaString);

            }
        }

        //listen for lick on "stopBt"
        if(e.getSource()==stopBt){
            //can only be clicked after genBt clicked
            try {
                client_sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            stopBt.setEnabled(false);
            //enable the genPrime again
            genBt.setEnabled(true);
        }

    }

    public static void main(String[] args){
        JFrame primeFrame = new JFrame("Prime Generator");
//        primeFrame.setPreferredSize(new Dimension(250, 80));
        primeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Client clientPanel = new Client();
        primeFrame.setContentPane(clientPanel);
        primeFrame.getRootPane().setDefaultButton(clientPanel.genBt);
        primeFrame.pack();
        primeFrame.setVisible(true);
    }
}
