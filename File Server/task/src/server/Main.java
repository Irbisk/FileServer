package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {

    public static final String ADRESS = "127.0.0.1";
    public static final int PORT = 23456;

    public static void main(String[] args) {
        System.out.println("Server started!");
        Session.isWorking = true;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ADRESS));
            while (Session.isWorking) {
                Session session = new Session(serverSocket.accept());
                session.start();
                session.join();
            }
        }  catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


static class Session extends Thread {
    private final Socket socket;
    private Map<Integer, String> mapID = new HashMap<>();
    private static Boolean isWorking;

    public Session(Socket socketForClient) {
        this.socket = socketForClient;
    }

    private synchronized void server() {

        File saveFile = new File("C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\saved\\SavedData.txt");
        if (saveFile.exists()) {
            Main.SavedFiles savedFiles = null;
            try {
                savedFiles = (Main.SavedFiles) Main.SerializationUtils.deserialize();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mapID = savedFiles.getMapID();
        }

        try (
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
        ) {
            String input = inputStream.readUTF();
            String request = null;

            if (input.contains(" ")) {
                request = input.substring(0, input.indexOf(" "));
            } else {
                request = input;
            }
            switch (request) {
                case "PUT":
                    String fileName = input.substring(input.indexOf(" ") + 1);


                    int length = inputStream.readInt();
                    byte[] message = new byte[length];
                    inputStream.readFully(message, 0, message.length);

                    outputStream.writeUTF(String.valueOf(createFile(fileName, message)));
                    break;
                case "GET":
                    fileName = input.substring(input.indexOf(" ") + 1);
                    getFile(fileName, outputStream);
                    break;
                case "DELETE":
                    fileName = input.substring(input.indexOf(" ") + 1);
                    outputStream.writeUTF(String.valueOf(delete(fileName)));
                    break;
                default:
                    isWorking = false;
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Main.SavedFiles savedFiles = new Main.SavedFiles(mapID);
            try {
                SerializationUtils.serialize(savedFiles);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
            server();
    }

    private int delete(String fileName) {
        String path = "C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";
        int id = 0;
        if (!fileName.contains(".")) {
            if (mapID.containsKey(Integer.valueOf(fileName))) {
                id = Integer.parseInt(fileName);
                fileName = mapID.get(id);
            }
        }

        File file = new File(path + fileName);
        int code;
        if (file.delete()) {
            mapID.remove(id);
            code = 200;
        } else {
            code = 404;
        }
        return code;
    }

    private void getFile(String fileName, DataOutputStream outputStream) {
        String path = "C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";
        byte[] message = null;
        if (!fileName.contains(".")) {
            if (mapID.containsKey(Integer.valueOf(fileName))) {
                fileName = mapID.get(Integer.valueOf(fileName));
            }
        }
        File file = new File(path + fileName);
        String code = null;
        if (file.exists()) {
            try {
                message = Files.readAllBytes(Paths.get(path + fileName));
                code = "200";
                outputStream.writeUTF(code);
                outputStream.writeInt(message.length);
                outputStream.write(message);
                outputStream.close();
            } catch (IOException e) {
                code = "404";
                try {
                    outputStream.writeUTF(code);
                    outputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                e.printStackTrace();
            }
        } else {
            code = "404";
            try {
                outputStream.writeUTF(code);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createFile(String fileName, byte[] message) {
        String path = "C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";

        File file = new File(path + fileName);
        String code;
        FileOutputStream outputStream = null;

        try {
            boolean createdNew = file.createNewFile();
            if (createdNew) {
                outputStream = new FileOutputStream(file);
                outputStream.write(message);
                outputStream.close();
                String ID = String.valueOf(createID(fileName));

                code = "200 " + ID;

            } else {
                code = "403";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            code = "403";
        } catch (IOException e) {
            e.printStackTrace();
            code = "403";
        }

        return code;
    }

    private int createID(String fileName) {
        int id = 1;
        while (true) {
            if (mapID.containsKey(id)) {
                id++;
            } else {
                break;
            }
        }
        mapID.put(id, fileName);
        return id;
    }
}

    public static class SavedFiles implements Serializable{
        private Map<Integer, String> mapID;

        public SavedFiles(Map<Integer, String> mapID) {
            this.mapID = mapID;
        }

        public Map<Integer, String> getMapID() {
            return mapID;
        }

        public void setMapID(Map<Integer, String> mapID) {
            this.mapID = mapID;
        }

        @Override
        public String toString() {
            return mapID.toString();
        }
    }

    public static class SerializationUtils {
        public static void serialize(Object object) throws IOException {
            FileOutputStream fos = new FileOutputStream("C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\saved\\SavedData.txt");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.close();
        }

        public static Object deserialize() throws IOException, ClassNotFoundException {
            FileInputStream fis = null;
            fis = new FileInputStream("C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\saved\\SavedData.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object object = ois.readObject();
            ois.close();

            return object;
        }
    }
}