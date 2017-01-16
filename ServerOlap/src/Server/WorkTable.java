package Server;

import Common.QueryEntry;
import Common.ResultEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

class WorkTable {
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "net.ucanaccess.jdbc.UcanaccessDriver";
    private static final String DB_URL = "jdbc:ucanaccess://NorthWind.mdb";
    private Statement stmt = null;
    private Connection conn = null;

    void initBase() throws ClassNotFoundException, SQLException {
        double debugTime;

            System.out.println("---Start connect to db---");
            debugTime = System.currentTimeMillis();

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();

            System.out.println("---End connect to db--- done for " + (System.currentTimeMillis()-debugTime)/1000);



            System.out.println("---Start init---");
            debugTime = System.currentTimeMillis();

            // Удаляем старые таблицы если они есть
            try{
                stmt.executeUpdate("DROP TABLE [_DimProducts]");
            }
            catch (Exception e){}

            try{
                stmt.executeUpdate("DROP TABLE [_DimShippers]");
            }
            catch (Exception e){}

            try{
                stmt.executeUpdate("DROP TABLE [_FactPrice]");
            }
            catch (Exception e){}

            try{
                stmt.executeUpdate("DROP TABLE [_FactPriceTemp]");
            }
            catch (Exception e){}

            // Создаем новые таблицы для схемы звезда
            stmt.executeUpdate(
                    "CREATE TABLE [_DimShippers] " +
                            "(ShippersID AUTOINCREMENT, " +
                            "ShipCountry varchar(15))"
            );

            stmt.executeUpdate(
                    "CREATE TABLE [_DimProducts] " +
                            "(ProductID AUTOINCREMENT, " +
                            "Category varchar(15))"
            );

            stmt.executeUpdate(
                    "CREATE TABLE [_FactPrice] " +
                            "(Price float, " +
                            "ProductID int, " +
                            "ShippersID int)"
            );

            stmt.executeUpdate(
                    "CREATE TABLE [_FactPriceTemp] " +
                            "(Price float, " +
                            "ProductID int, " +
                            "ShippersID int, " +
                            "ShipCountry varchar(15), " +
                            "Category varchar(15))"
            );

            // Заполняем _DimOrders
            stmt.executeUpdate(
                    "INSERT INTO _DimShippers " +
                            "(ShipCountry) " +
                            "SELECT DISTINCT " +
                            "Orders.ShipCountry " +
                            "FROM Orders"
            );

            // Заполняем _DimProducts
            stmt.executeUpdate(
                    "INSERT INTO _DimProducts " +
                            "(Category) " +
                            "SELECT DISTINCT " +
                            "_Categ.CategoryName " +
                            "FROM Products " +
                            "LEFT JOIN (SELECT CategoryID, CategoryName FROM Categories) " +
                            "AS _Categ ON Products.CategoryID=_Categ.CategoryID"
            );

            // Заполняем временную таблицу
            stmt.executeUpdate(
                    "INSERT INTO _FactPriceTemp  \n" +
                            "\t(Price, \n" +
                            "\tProductID, \n" +
                            "\tShippersID,\n" +
                            "\tShipCountry, \n" +
                            "\tCategory)\n" +
                            "SELECT\n" +
                            "\t(OrderDetails.UnitPrice * OrderDetails.Quantity),\n" +
                            "\t0,\n" +
                            "\t0,\n" +
                            "\t_ShipTemp.ShipCountry,\n" +
                            "\t_CategTemp.CategoryName\n" +
                            "FROM \n" +
                            "\tOrderDetails,\n" +
                            "\t\n" +
                            "\t(SELECT Orders.OrderID, Shippers.CompanyName, Orders.ShipCountry\n" +
                            "\tFROM Orders, Shippers\n" +
                            "\tWHERE Orders.ShipVia=Shippers.ShipperID) _ShipTemp,\n" +
                            "\t\n" +
                            "\t(SELECT Products.ProductID, Categories.CategoryName\n" +
                            "\tFROM Products, Categories\n" +
                            "\tWHERE Products.CategoryID=Categories.CategoryID) _CategTemp\n" +
                            "\t\n" +
                            "WHERE OrderDetails.OrderID = _ShipTemp.OrderID\n" +
                            "AND OrderDetails.ProductID = _CategTemp.ProductID\n"
            );

            stmt.executeUpdate(
                    "INSERT INTO _FactPrice\n" +
                            "\t(Price,\n" +
                            "\tProductID,\n" +
                            "\tShippersID)\n" +
                            "SELECT \n" +
                            "\tSUM(_FactPriceTemp.Price) AS sumPrice,\n" +
                            "\t_DimProducts.ProductID,\n" +
                            "\t_DimShippers.ShippersID\n" +
                            "FROM _FactPriceTemp, _DimProducts, _DimShippers\n" +
                            "WHERE _FactPriceTemp.Category = _DimProducts.Category\n" +
                            "\tAND _FactPriceTemp.ShipCountry = _DimShippers.ShipCountry\n" +
                            "GROUP BY _DimProducts.ProductID, _DimShippers.ShippersID\n"
            );
            System.out.println("---End init--- done for " + (System.currentTimeMillis()-debugTime)/1000);
            debugTime = 0;

            ResultSet rSet = stmt.executeQuery(
                    "SELECT * FROM _FactPrice"
            );

            //Удалим временную таблицу
            try{
                stmt.executeUpdate("DROP TABLE [_FactPriceTemp]");
            }
            catch (Exception e){}
    }

    ArrayList<ResultEntry> makeSelection(ArrayList<QueryEntry> quarryList){
        ResultSet rSet;
        ArrayList<ResultEntry> resultList = new ArrayList<>();

        try {
            String selectQuarryProd = "AND (_FactPrice.Price IS NULL ";
            String selectQuarryShip = "AND (_FactPrice.Price IS NULL ";

            for (QueryEntry q: quarryList){
                if (q.table != null && q.column != null && q.arg != null){
                    if (q.table.contains("_DimProducts")){
                        selectQuarryProd+= " OR " + q.table + "." + q.column + "=\"" + q.arg + "\"";
                    }
                    if (q.table.contains("_DimShippers")){
                        selectQuarryShip+= " OR " + q.table + "." + q.column + "=\"" + q.arg + "\"";
                    }
                }
            }

            if (Objects.equals(selectQuarryProd, "AND (_FactPrice.Price IS NULL ")) {
                selectQuarryProd = "";
            } else selectQuarryProd+=")";

            if (Objects.equals(selectQuarryShip, "AND (_FactPrice.Price IS NULL ")) {
                selectQuarryShip = "";
            } else selectQuarryShip+=")";

            System.out.println("selectQuarryProd: " + selectQuarryProd);
            System.out.println("selectQuarryShip: " + selectQuarryShip);

            rSet = stmt.executeQuery(
                    "SELECT \n" +
                            "\t_FactPrice.Price, \n" +
                            "\t_FactPrice.ProductID, \n" +
                            "\t_FactPrice.ShippersID\n" +
                            "FROM \n" +
                            "\t_FactPrice, \n" +
                            "\t_DimProducts, \n" +
                            "\t_DimShippers\n" +
                            "WHERE \n" +
                            "\t_FactPrice.ProductID = _DimProducts.ProductID\n" +
                            "\tAND _FactPrice.ShippersID = _DimShippers.ShippersID\n" +
                            selectQuarryProd + selectQuarryShip
                );

            while (rSet.next()){
                resultList.add(new ResultEntry(rSet.getString(1), rSet.getString(2), rSet.getString(3)));
            }

            return resultList;
        }

        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    void closeBase(){
        //finally block used to close resources
        try{
            if(stmt!=null)
                conn.close();
        }catch(SQLException se){
        }// do nothing
        try{
            if(conn!=null)
                conn.close();
        }catch(SQLException se){
            se.printStackTrace();
        }//end finally try
    }

    LinkedHashMap<String, String> getDimMap(String nameTable, String nameColumn){
        try {
            ResultSet rSet = stmt.executeQuery("SELECT * FROM " + nameTable);
            LinkedHashMap<String, String> dimMap = new LinkedHashMap<>();

            while (rSet.next()){
                dimMap.put(rSet.getString(1), rSet.getString(2));
            }
            return dimMap;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
