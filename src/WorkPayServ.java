import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class WorkPayServ {
    private Properties prop = new Properties();
    private JFrame frame;
    private JTextField text;
    private JLabel labelFile;
    private JFileChooser fileChooser;
    private JButton buttonStart;
    private JButton buttonStopServer;
    private JButton buttonLaunchServer;
    private JButton buttonReloadFile;

    private int threadCount = 1;
    private Integer port;
    private File file = null;
    private HashMap<String, Integer> list;

    private Worker worker;

    private final AtomicReference<Object> monitor = new AtomicReference<>();
    private boolean serverOn = false;

    public static void main(String[] args) {
        new WorkPayServ().go();
    }

    public class ServerStart implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSock = new ServerSocket(Integer.parseInt(prop.getProperty("port")));
                while (true) {
                    if (serverOn) {
                        Socket clientSocket = serverSock.accept();
                        Thread t = new Thread(new ClientHandler(clientSocket));
                        threadCount++;
                        t.start();
                    }
                    else synchronized (monitor) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                    sendResult(obj, out);
                    if (!serverOn) synchronized (monitor) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void go() {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("config.properties");
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (prop.size()==0)
        startGui();
        else {
            port = Integer.parseInt(prop.getProperty("port"));
            file = new File(prop.getProperty("file"));
            launchGui();
        }
    }

    private void startServer() {
        Thread serverThread = new Thread(new ServerStart());
        threadCount++;
        serverThread.start();

    }

    private void sendResult(Object obj, @NotNull ObjectOutputStream out) {
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

    @Contract(pure = true)
    private synchronized boolean isServerOn() {
        return serverOn;
    }

    private void startGui() {
        frame = new JFrame("Настройки");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JLabel label = new JLabel("Выберете порт:");

        text = new JTextField(20);

        JLabel labelSettings = new JLabel("Настройки:");

        JLabel labelChoose = new JLabel("Выберете файл");

        labelFile = new JLabel("Ваш файл: ");

        JButton buttonChoose = new JButton("Выбрать");
        buttonChoose.addActionListener(new ButtonChooseListener());
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выбор файла");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Файлы Excel", "xlsx");
        fileChooser.setFileFilter(filter);

        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(text);
        panel.add(labelChoose);
        panel.add(labelFile);
        panel.add(buttonChoose);


        buttonStart = new JButton("Сохранить настройки");
        buttonStart.addActionListener(new ButtonSaveSettingsListener());
        buttonStart.setEnabled(false);
        frame.getContentPane().add(labelSettings,BorderLayout.NORTH);
        frame.getContentPane().add(panel);
        frame.getContentPane().add(buttonStart, BorderLayout.SOUTH);
        frame.setSize(300, 200);
        frame.setVisible(true);

        ((AbstractDocument) text.getDocument()).setDocumentFilter(new MyDocumentFilter());

    }

    private void launchGui() {
        frame = new JFrame("WK-Server");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu menuProgram = new JMenu("Программа");
        JMenuItem settings = new JMenuItem("Настройки");
        JMenuItem support = new JMenuItem("Поддержка");
        JMenuItem about = new JMenuItem("О программе");
        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(new ExitListener());
        settings.addActionListener(new SettingsListener());
        JPanel centrPanel = new JPanel();

        JLabel labelInWork =new JLabel("Файл в работе:");

        text = new JTextField();
        text.setText(prop.getProperty("file"));
        text.setEditable(false);


        buttonStopServer = new JButton("Остановить сервер");
        buttonStopServer.setEnabled(false);
        buttonStopServer.addActionListener(new ButtonStopListener());


        buttonReloadFile = new JButton("Обновить файл");
        buttonReloadFile.addActionListener(new ButtonReloadFileListener());


        buttonLaunchServer = new JButton("Запустить сервер");
        buttonLaunchServer.addActionListener(new ButtonStartListener());

        centrPanel.add(labelInWork);
        centrPanel.add(text);
        centrPanel.add(buttonStopServer);
        centrPanel.add(buttonReloadFile);
        centrPanel.add(buttonLaunchServer);

        menuProgram.add(settings);
        menuProgram.add(new JSeparator());
        menuProgram.add(support);
        menuProgram.add(about);
        menuProgram.add(exit);
        menuBar.add(menuProgram);


        frame.getContentPane().add(menuBar,BorderLayout.NORTH);
        frame.getContentPane().add(centrPanel,BorderLayout.CENTER);
        frame.setSize(300,200);
        frame.setVisible(true);
    }

    public class ButtonChooseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = fileChooser.showOpenDialog(frame.getContentPane());
            if (result == JFileChooser.APPROVE_OPTION)
                if (fileChooser.getSelectedFile().exists()) {
                    labelFile.setText(fileChooser.getSelectedFile().getName());
                    file = fileChooser.getSelectedFile();
                    if (text.getText().length() > 0) {
                        buttonStart.setEnabled(true);
                        port = Integer.parseInt(text.getText());
                    } else buttonStart.setEnabled(false);
                } else
                    JOptionPane.showMessageDialog(frame.getContentPane(), "Файл не существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    public class ButtonSaveSettingsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (prop.size() > 0) {
                save();
            } else {
                save();
                launchGui();
            }
        }

        private void save() {
            try {
                prop.setProperty("port",port.toString());
                prop.setProperty("file",file.toString());
                FileOutputStream outputStream = new FileOutputStream(".\\properties\\config.properties");
                prop.store(outputStream, "settings");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            frame.setVisible(false);
        }
    }

    public class ButtonStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            serverOn = true;
            buttonStopServer.setEnabled(true);
            buttonReloadFile.setEnabled(false);
            buttonLaunchServer.setEnabled(false);
            if (threadCount == 1) startServer();
            else synchronized (monitor) {monitor.notifyAll();}
        }
    }

    public class ButtonReloadFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            list = ExcelParser.excelParse(file);
        }
    }

    public class ButtonStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonStopServer.setEnabled(false);
            buttonLaunchServer.setEnabled(true);
            buttonReloadFile.setEnabled(true);
            serverOn = false;
        }
    }

    public class ExitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class SettingsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isServerOn())
            startGui();
            else JOptionPane.showMessageDialog(frame.getContentPane(),"Сначала остановите работу сервера","Ошибка",JOptionPane.ERROR_MESSAGE);
        }
    }

    class MyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            string = string.replaceAll("\\D", "");
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            text = text.replaceAll("\\D", "");
            super.replace(fb, offset, length, text, attrs);
        }
    }


}
