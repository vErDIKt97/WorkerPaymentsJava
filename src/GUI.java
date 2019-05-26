import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;
import java.io.IOException;

class GUI {
    private WorkPayServ workPayServ;
    private JFrame frame;
    private JTextField text;
    private JLabel labelFile;
    private JFileChooser fileChooser;
    private JButton buttonStart;
    private JButton buttonStopServer;
    private JButton buttonLaunchServer;
    private JButton buttonReloadFile;
    private JPopupMenu popupMenu;
    private Image icon = new ImageIcon(getClass().getResource("wksrv.png")).getImage();
    private SystemTray tray = SystemTray.getSystemTray();

    GUI () {
        workPayServ = WorkPayServ.workPayServ;
        buildTray();
        if (workPayServ.getProp().size()==0)  startGui();
        else launchGui();
    }

    private void buildTray () {
        TrayIcon trayIcon = new TrayIcon(icon, "WKserver");
        popupMenu = new JPopupMenu();

        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(new ExitMenuListener());

        JMenuItem showProg = new JMenuItem("Свернуть\\Развернуть");
        showProg.addActionListener(new ShowMenuListener());

        popupMenu.add(showProg);
        popupMenu.add(exit);

        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new TrayIconMouseReleased());
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    class ShowMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.setVisible(true);
        }
    }

    class ExitMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class TrayIconMouseReleased implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == 3) {
                popupMenu.setInvoker(popupMenu);
                popupMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
                popupMenu.setVisible(true);
            }
            else if (e.getClickCount()==2 && e.getButton()==1)
                if (frame.isVisible()) frame.setVisible(false);
                else frame.setVisible(true);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }
    }

    private void startGui() {
        frame = new JFrame("Настройки");
        frame.setIconImage(icon);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JLabel label = new JLabel("Выберете порт:");

        text = new JTextField(20);
        text.setText(workPayServ.getProp().getProperty("port"));

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
        frame.setIconImage(icon);
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
        text.setText(workPayServ.getProp().getProperty("file"));
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
                    workPayServ.setFile(fileChooser.getSelectedFile());
                    if (text.getText().length() > 0) {
                        buttonStart.setEnabled(true);
                        workPayServ.setPort(Integer.parseInt(text.getText()));
                    } else buttonStart.setEnabled(false);
                } else
                    JOptionPane.showMessageDialog(frame.getContentPane(), "Файл не существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public class ButtonSaveSettingsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (workPayServ.getProp().size()>0)
                save();
            else{
                save();
                launchGui();
            }
        }

        private void save() {
            try {
                if (!text.getText().equals(workPayServ.getProp().getProperty("port"))) {
                    workPayServ.getProp().setProperty("port", workPayServ.getPort().toString());
                    JOptionPane.showMessageDialog(frame.getContentPane(),"Для изменения порта нужен перезапуск программы!","Информация",JOptionPane.INFORMATION_MESSAGE);
                }
                workPayServ.getProp().setProperty("file",workPayServ.getFile().toString());
                FileOutputStream outputStream = new FileOutputStream(workPayServ.getPath());
                workPayServ.getProp().store(outputStream, "settings");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            frame.setVisible(false);
        }
    }

    public class ButtonStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            workPayServ.setServerOn(true);
            buttonStopServer.setEnabled(true);
            buttonReloadFile.setEnabled(false);
            buttonLaunchServer.setEnabled(false);
            if (workPayServ.getThreadCount() == 1) workPayServ.startServer();
            else synchronized (workPayServ.getMonitor()) {workPayServ.getMonitor().notifyAll();}
        }
    }

    public class ButtonReloadFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            workPayServ.setList(ExcelParser.excelParse(workPayServ.getFile()));
        }
    }

    public class ButtonStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonStopServer.setEnabled(false);
            buttonLaunchServer.setEnabled(true);
            buttonReloadFile.setEnabled(true);
            workPayServ.setServerOn(false);
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
            if (!workPayServ.isServerOn())
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
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            text = text.replaceAll("\\D", "");
            super.replace(fb, offset, length, text, attrs);
        }
    }

}
