package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static final String ADRESS = "127.0.0.1";
    public static final int PORT = 23456;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {


        System.out.println("Client started!");
        System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file):");
        String action = scanner.nextLine();
        runClient(action);
    }

    private static void runClient(String action) {
        String fileName = null;


        try (
                Socket socket = new Socket(InetAddress.getByName(ADRESS), PORT);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())
        ) {

            switch (action) {
                case "1":
                    System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id):");
                    String wayToGet = scanner.nextLine();

                    switch (wayToGet) {
                        case "1":
                            System.out.println("Enter name of the file\":");
                            fileName = scanner.nextLine();
                            break;
                        case "2":
                            System.out.println("Enter id");
                            fileName = scanner.nextLine();
                            break;
                        default:
                    }


                    outputStream.writeUTF(String.format("GET %s", fileName));
                    System.out.println("The request was sent.");

                    String respond = inputStream.readUTF();
                    int code = Integer.valueOf(respond);
                    switch (code) {
                        case 200:
                            System.out.println("The file was downloaded! Specify a name for it:");
                            String clientFileName = scanner.nextLine();
                            if (clientFileName.isEmpty()) {
                                clientFileName = fileName;
                            }
                            int length = inputStream.readInt();
                            byte[] message = new byte[length];
                            inputStream.readFully(message, 0, message.length);

                            createAFile(clientFileName, message);

                            break;
                        case 404:
                            System.out.println("The response says that this file is not found!");
                            break; default:
                    }
                    break;
                case "2":
                    String pathToFile = "C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data\\";
                    System.out.println("Enter name of the file:");
                    fileName = scanner.nextLine();
                    System.out.println("Enter name of the file to be saved on server:");
                    String serverFileName = scanner.nextLine();
                    if (serverFileName.isEmpty()) {
                        serverFileName = fileName;
                    }

                    File file = new File(pathToFile + fileName);
                    if (!file.exists()) {
                        System.out.println("There's no such file! Try again");
                        return;
                    }
                    byte[] message = Files.readAllBytes(Paths.get(pathToFile + fileName));
                    outputStream.writeUTF(String.format("PUT %s", serverFileName));
                    outputStream.writeInt(message.length);
                    outputStream.write(message);


                    System.out.println("The request was sent.");

                    respond = inputStream.readUTF();
                    code = Integer.valueOf(respond.substring(0, 3));
                    switch (code) {
                        case 200:
                            String ID = respond.substring(4);
                            System.out.println("Response says that file is saved! ID = " + ID);
                            break;
                        case 403:
                            System.out.println("The response says that creating the file was forbidden!");
                            break;
                        default:
                    }
                    break;
                case "3":
                    System.out.println("Do you want to delete the file by name or by id (1 - name, 2 - id):");
                    String wayToDelete = scanner.nextLine();

                    switch (wayToDelete) {
                        case "1":
                            System.out.println("Enter name of the file:");
                            fileName = scanner.nextLine();
                            break;
                        case "2":
                            System.out.println("Enter id");
                            fileName = scanner.nextLine();
                            break;
                        default:
                    }

                    outputStream.writeUTF(String.format("DELETE %s", fileName));
                    System.out.println("The request was sent.");

                    respond = inputStream.readUTF();
                    code = Integer.parseInt(respond);
                    switch (code) {
                        case 200:
                            System.out.println("The response says that the file was successfully deleted!");
                            break;
                        case 404:
                            System.out.println("The response says that this file is not found!");
                            break;
                        default:
                    }
                case "exit" :
                    outputStream.writeUTF("EXIT");
                    System.out.println("The request was sent.");
                default:
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createAFile(String clientFileName, byte[] message) {
        String pathToFile = "C:\\Users\\irbis\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data\\";
        File file = new File(pathToFile + clientFileName);
        try {
            boolean createdNew = file.createNewFile();
            if (createdNew) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(message);
                fileOutputStream.close();
                System.out.println("File saved on the hard drive!");
            } else {
                System.out.println("Unable to save the file!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
