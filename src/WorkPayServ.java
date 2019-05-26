import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class WorkPayServ {
    private Properties prop = new Properties();
    static WorkPayServ workPayServ;
    private String path;

    {
        try {
            path = WorkPayServ.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "cfg.txt";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private int threadCount = 1;
    private Integer port;
    private File file = null;
    private HashMap<String, Integer> list;

    private Worker worker;

    private final AtomicReference<Object> monitor = new AtomicReference<>();
    private boolean serverOn = false;

    public static void main(String[] args) {
        workPayServ = new WorkPayServ();
        workPayServ.go();
    }

    void setList(HashMap<String, Integer> list) {
        this.list = list;
    }

    AtomicReference<Object> getMonitor() {
        return monitor;
    }

    int getThreadCount() {
        return threadCount;
    }

    void setServerOn(boolean serverOn) {
        this.serverOn = serverOn;
    }

    String getPath() {
        return path;
    }

    File getFile() {
        return file;
    }

    void setFile(File file) {
        this.file = file;
    }

    Integer getPort() {
        return port;
    }

    void setPort(Integer port) {
        this.port = port;
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
                    } else synchronized (monitor) {
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
            File cfgFile = new File(path);
            if (cfgFile.exists()) {
                prop.load(new FileInputStream(cfgFile));
            } else {
                cfgFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        GUI gui = new GUI();
    }

    void startServer() {
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
    synchronized boolean isServerOn() {
        return serverOn;
    }

    Properties getProp() {
        return prop;
    }
}
