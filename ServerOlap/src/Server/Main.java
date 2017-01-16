package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;

public class Main {
    static private int PORT=7777;

    public static void main(String[] args) throws IOException {
        WorkTable workTable = new WorkTable();

        Properties config = new Properties();
        File configFile = new File("configServer.ini");
        if (!configFile.isFile()){
            try {
                configFile.createNewFile();
                FileWriter fv = new FileWriter(configFile);
                fv.write("PORT=7777");
                fv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config.load(new FileInputStream((new File("configServer.ini"))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PORT = Integer.valueOf(config.getProperty("PORT"));
        ServerSocket serverSocket = new ServerSocket(PORT);

        try {
            workTable.initBase();

            while (true){
                System.out.println("Waiting for connect...");
                Socket socket = serverSocket.accept();
                System.out.println("Connect is accepted");
                Connect connect = new Connect(socket, workTable);
                connect.start();
            }
        }
        catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }
        catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        finally {
            workTable.closeBase();
        }
    }
}
