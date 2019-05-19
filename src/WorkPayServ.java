import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class WorkPayServ {
    private JFrame frame;
    private JTextField text;
    private JFileChooser fileChooser;
    private File file;
    private HashMap<String,Integer> list;
    private Worker worker;
    public static void main(String[] args) {
        new WorkPayServ().go();
    }

    public class ServerStart implements Runnable {
        private JFrame frameServer = new JFrame("Сервер");
        Thread t;
        Socket clientSocket;

        ServerStart() {
            frameServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JButton stopServer = new JButton("STOP");
            stopServer.addActionListener(new ButtonStopListener());
            frameServer.getContentPane().add(BorderLayout.CENTER, stopServer);
            frameServer.setSize(100, 100);
            frameServer.setVisible(true);
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSock = new ServerSocket(4242);
                while (true) {
                    JOptionPane.showMessageDialog(frame.getContentPane(), new String[]{"The server has started successfully"}, "Server started", JOptionPane.INFORMATION_MESSAGE, null);
                    clientSocket = serverSock.accept();
                    t = new Thread(new ClientHandler(clientSocket));
                    t.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class ButtonStopListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                try {
                    t.interrupt();
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    frameServer.dispatchEvent(new WindowEvent(frameServer, WindowEvent.WINDOW_CLOSING));
                }
            }
        }
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket clientSocket;

        ClientHandler(Socket socket) {
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
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void go() {
        buildGui();
    }

    private void startServer() {
        list = ExcelParser.parse(file);
        Thread serverThread = new Thread(new ServerStart());
        serverThread.start();

    }

    private void sendResult(Object obj, ObjectOutputStream out) {
        getResult(obj);
        try {
            out.writeObject(worker);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void getResult(Object obj) {
        worker = (Worker) obj;
        for (Map.Entry<String, Integer> pair : list.entrySet()) {
            if (pair.getKey().contains(worker.getName())) {
                worker.setSells(pair.getValue());
                break;
            }
        }
    }



    private void buildGui() {
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
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel","xlsx");
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        panel.add(labelDefault);
        panel.add(text);
        panel.add(buttonchoose);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.getContentPane().add(BorderLayout.SOUTH,button);

        frame.setSize(350,300);
        frame.setVisible(true);
    }

    class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                file.exists();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame.getContentPane(), new String[]{"Файл не выбран, либо его не существует!"}, "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            startServer();
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
