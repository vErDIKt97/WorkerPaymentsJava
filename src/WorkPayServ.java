import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorkPayServ {
    private ArrayList<ObjectOutputStream> clientOutputStreams;
    private JFrame frame;
    private JLabel labelMoney;
    private JTextField text;
    private JFileChooser fileChooser;
    private File file;
    private HashMap<String,Integer> list;
    private Worker worker;
    public static void main(String[] args) {
        new WorkPayServ().go();
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket clientSocket;

        public ClientHandler (Socket socket) {
            try {
                clientSocket = socket;
                in = new ObjectInputStream(clientSocket.getInputStream());
                out = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Object obj;
            try {
                while ((obj = in.readObject()) != null) {
                    sendResult(obj,out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void go() {
        buildGui();
    }

    public void startServ () {
        list = ExelParser.parse(file);
        try {
            ServerSocket serverSock = new ServerSocket(4242);
            JOptionPane.showMessageDialog(frame.getContentPane(),JOptionPane.INFORMATION_MESSAGE,"Server started",JOptionPane.INFORMATION_MESSAGE);

            while (true) {
                Socket clientSocket = serverSock.accept();
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void sendResult (Object obj, ObjectOutputStream out) {
        getResult(obj);
        try {
            out.writeObject(worker);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void getResult (Object obj) {
        worker = (Worker) obj;
        Iterator<Map.Entry<String ,Integer>> itterator = list.entrySet().iterator();
        while (itterator.hasNext()) {
            Map.Entry<String ,Integer> pair =  itterator.next();
             if (pair.getKey().contains(worker.getName())) {
                 worker.setSells(pair.getValue());
                 break;
             }
        }
    }



    public void buildGui() {
        frame = new JFrame("WorkerPay Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        JButton button = new JButton("Запустить сервер");
        button.addActionListener(new ButtonListener());
        JButton buttonchoose = new JButton("Выбрать файл");
        buttonchoose.addActionListener(new ChooseListener());
        text = new JTextField(20);
        text.setMaximumSize(new Dimension(20,20));
        JLabel labelDefault = new JLabel("Путь к файлу выручки:");
        labelMoney = new JLabel();
        fileChooser = new JFileChooser();
        panel.add(labelDefault);
        panel.add(text);
        panel.add(labelMoney);
        panel.add(buttonchoose);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.getContentPane().add(BorderLayout.SOUTH,button);

        frame.setSize(350,300);
        frame.setVisible(true);
    }

    class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (text.getText().equals(null)) {
                JOptionPane pane = new JOptionPane(frame,JOptionPane.ERROR_MESSAGE,JOptionPane.DEFAULT_OPTION,null, new String[] {"Сначала выбери файл!"});
            }
            else {
                startServ();
            }
        }
    }

    class ChooseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            fileChooser.setDialogTitle("Выберете файл");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                text.setText(fileChooser.getSelectedFile().toString());
                file = fileChooser.getSelectedFile();
            }
        }
    }
}
