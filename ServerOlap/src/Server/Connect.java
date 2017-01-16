package Server;

import Common.QueryEntry;
import Common.ResultEntry;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Connect extends Thread {

    private Socket socket;
    private ObjectInputStream objInput;
    private ObjectOutputStream objOut;
    private WorkTable workTable;

    Connect(Socket socket, WorkTable workTable) throws IOException {
        this.socket = socket;
        this.workTable = workTable;
        this.objInput = new ObjectInputStream(this.socket.getInputStream());
        this.objOut = new ObjectOutputStream(this.socket.getOutputStream());
    }

    @Override
    public void run(){
        try {
            ArrayList<ResultEntry> resultList;
            ArrayList<QueryEntry> queryList;

            objOut.writeObject(workTable.getDimMap("_DimProducts", "Category"));
            objOut.writeObject(workTable.getDimMap("_DimShippers", "ShipCountry"));

            while (true) {
                queryList = (ArrayList<QueryEntry>) objInput.readObject();
                resultList = workTable.makeSelection(queryList);
                objOut.writeObject(resultList);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
