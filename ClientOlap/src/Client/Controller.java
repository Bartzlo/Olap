package Client;

import Common.QueryEntry;
import Common.ResultEntry;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.poi.hssf.usermodel.*;
import org.controlsfx.control.CheckListView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Controller {
    @FXML
    private StackPane categList;
    @FXML
    private StackPane coutryList;
    @FXML
    private ScrollPane scrollPanel;

    private CheckListView categListView;
    private CheckListView countryListView;


    public void fillQueryLists(ObservableList<String> categObsList, ObservableList<String> coutryObsList){
        categListView = new CheckListView(categObsList);
        countryListView = new CheckListView(coutryObsList);

        categList.getChildren().add(categListView);
        coutryList.getChildren().add(countryListView);
    }

    public String[][] refreshClick() throws IOException, ClassNotFoundException {

        String [][] resultArray = getResultArray();
        int xArr = resultArray.length;
        int yArr = resultArray[0].length;
        GridPane gridPane = new GridPane();

        // Создание и заполнение GridPanel
        for (int i = 0; i < xArr; i++) {
            gridPane.addRow(i);
        }

        for (int i = 0; i < yArr; i++) {
            gridPane.addColumn(i);
        }

        for (int i = 0; i < xArr; i++){
            for (int j = 0; j < yArr; j++){
                //System.out.print(resultArray[i][j] + " | ");
                Label label = new Label(resultArray[i][j]);
                label.setPadding(new Insets(5,5,5,5));
                gridPane.add(label, j, i);
            }
            //System.out.println();
        }
        //System.out.println("---------------");

        gridPane.setPadding(new Insets(5,5,5,5));
        gridPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        gridPane.setGridLinesVisible(true);
        scrollPanel.setContent(gridPane);

        return resultArray;
    }

    public void xlsExport() throws IOException, ClassNotFoundException {
        String [][] resultArray = refreshClick();
        int xArr = resultArray.length;
        int yArr = resultArray[0].length;

        String filename = "Export.xls" ;
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        HSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00"));

        for (int i = 0; i < xArr; i++) {
            HSSFRow row = sheet.createRow(i);
            sheet.setColumnWidth(i, 4000);
            for (int j = 0; j < yArr; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(style);
                try{
                    cell.setCellValue(Float.parseFloat(resultArray[i][j]));
                }
                catch (Exception e){
                    cell.setCellValue(resultArray[i][j]);
                }
            }
        }

        FileOutputStream fileOut = new FileOutputStream(filename);
        workbook.write(fileOut);
        fileOut.close();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Export is complete");
        alert.setContentText("file Export.xls in application directory");

        alert.showAndWait();
    }

    public void clearCoutryList(){
        countryListView.getCheckModel().clearChecks();
    }

    public void clearCategoryList(){
        categListView.getCheckModel().clearChecks();
    }

    public void selectAllCountryList(){
        countryListView.getCheckModel().checkAll();
    }

    public void selectAllCategoryList(){
        categListView.getCheckModel().checkAll();
    }

    private String[][] getResultArray() throws IOException, ClassNotFoundException {
        ArrayList<QueryEntry> queryList = new ArrayList<>();

        // Формирование запроса
        for (Object obj:  categListView.getCheckModel().getCheckedItems()){
            queryList.add(new QueryEntry("_DimProducts", "Category", obj.toString()));
        }

        for (Object obj:  countryListView.getCheckModel().getCheckedItems()){
            queryList.add(new QueryEntry("_DimShippers", "ShipCountry", obj.toString()));
        }

        // Создание и заполнение массива результатами
        int xArr = Main.netWork.getShippCount() + 2, yArr = Main.netWork.getCategCount() + 2;

        String[][] resultArray = new String[xArr][yArr];
        for (ResultEntry r: Main.netWork.getTable(queryList)){
            double newDouble = new BigDecimal(r.price).setScale(2, RoundingMode.UP).doubleValue();
            resultArray[Integer.parseInt(r.shipper)][Integer.parseInt(r.product)] = String.valueOf(newDouble);
        }

        // Заголовки таблицы
        for (int i = 0; i < xArr; i++) {
            resultArray[i][0] = Main.netWork.getShipp(String.valueOf(i));
        }

        for (int i = 0; i < yArr; i++) {
            resultArray[0][i] = Main.netWork.getCateg(String.valueOf(i));
        }

        // Итоги
        for (int x = 1; x < xArr; x++) {
            float res = 0;
            for (int y = 1; y < yArr; y++) {
                if (resultArray[x][y] != null)
                    res += Float.parseFloat(resultArray[x][y]);
                else resultArray[x][y] = "0.0";
            }
            resultArray[x][yArr-1] = String.valueOf(res);
        }

        for (int y = 1; y < yArr; y++) {
            float res = 0;
            for (int x = 1; x < xArr; x++) {
                if (resultArray[x][y] != null)
                    res += Float.parseFloat(resultArray[x][y]);
                else resultArray[x][y] = "0.0";
            }
            resultArray[xArr-1][y] = String.valueOf(res);
        }

        resultArray[xArr-1][0] = "RESULT";
        resultArray[0][yArr-1] = "RESULT";

        return resultArray;
    }
}
