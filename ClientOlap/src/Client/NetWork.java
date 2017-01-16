package Client;

import Common.QueryEntry;
import Common.ResultEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.util.*;


class NetWork {
    private Socket socket;
    private ObjectOutputStream objOut;
    private ObjectInputStream objInp;
    private LinkedHashMap<String, String> prodCategory;
    private LinkedHashMap<String, String> shipCountry;
    static public String IP_ADRES="127.0.0.1";
    static public int PORT=7777;

    ObservableList<String> getProdCategory() {
        ObservableList<String> list = FXCollections.observableArrayList();

        Iterator it = prodCategory.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            list.add((String)entry.getValue());
        }

        return list;
    }

    ObservableList<String> getShipCountry() {
        ObservableList<String> list = FXCollections.observableArrayList();

        Iterator it = shipCountry.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            list.add((String)entry.getValue());
        }

        return list;
    }

    NetWork() throws IOException, ClassNotFoundException {
        Properties config = new Properties();
        File configFile = new File("configClient.ini");
        if (!configFile.isFile()){
            try {
                configFile.createNewFile();
                FileWriter fv = new FileWriter(configFile);
                fv.write("IP_ADRES=127.0.0.1" + "\r\n");
                fv.write("PORT=7777");
                fv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config.load(new FileInputStream((new File("configClient.ini"))));
        } catch (FileNotFoundException e){
            new File("configClient.ini");
        } catch (IOException e) {
            e.printStackTrace();
        }

        IP_ADRES = config.getProperty("IP_ADRES");
        PORT = Integer.valueOf(config.getProperty("PORT"));

        socket = new Socket(IP_ADRES, PORT);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objInp = new ObjectInputStream(socket.getInputStream());

        prodCategory = (LinkedHashMap<String, String>) objInp.readObject();
        shipCountry = (LinkedHashMap<String, String>) objInp.readObject();
    }

    ArrayList<ResultEntry> getTable(ArrayList<QueryEntry> queryList) throws IOException, ClassNotFoundException {
        objOut.writeObject(queryList);
        objOut.flush();
        return  (ArrayList<ResultEntry>) objInp.readObject();
    }

    public String getCateg(String key){
        return prodCategory.get(key);
    }
    public int getCategCount(){
        return prodCategory.size();
    }

    public String getShipp(String key){
        return shipCountry.get(key);
    }
    public int getShippCount(){
        return shipCountry.size();
    }
}
