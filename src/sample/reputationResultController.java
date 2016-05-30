package sample;

import Utils.Database.EksternalFile;
import data_structure.instance_ML.historySitesReputation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by steve on 30/05/2016.
 */
public class reputationResultController implements Initializable {

    public Label domainName;
    public Label domainNameLabel;
    public Label domainNameResponseTime;
    public Label domainNameDateMeasured;
    public PieChart chartCompositionDangerousity;
    public Label labelSiteComposition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set measurement result into scene
        domainName.setText(StaticVars.currentDomainName);
        domainNameLabel.setText(StaticVars.currentLabel);
        domainNameResponseTime.setText(StaticVars.currentResponseTime + " second(s)");
        domainNameDateMeasured.setText(String.valueOf(StaticVars.currentDate));
        chartCompositionDangerousity = new PieChart();
        if (StaticVars.methodType == 1) {
            labelSiteComposition.setVisible(false);
            chartCompositionDangerousity.setVisible(false);
        } else {
            Triplet<Double,Double,Double> compositionDangerousity = StaticVars.currentComposition;
            ObservableList<PieChart.Data> pieChartElements = FXCollections.observableArrayList();
            pieChartElements.addAll(new PieChart.Data("Malware",compositionDangerousity.getValue0()),
                    new PieChart.Data("Phishing",compositionDangerousity.getValue1()),
                    new PieChart.Data("Spamming",compositionDangerousity.getValue2()));
        }
        // Save this reputation result into eksternal file
        historySitesReputation thisResultReputation = new historySitesReputation();
        thisResultReputation.setCompositionDangerousity(StaticVars.currentComposition);
        thisResultReputation.setLabelNormality(StaticVars.currentLabel);
        thisResultReputation.setResponseTime(Long.parseLong(StaticVars.currentResponseTime));
        thisResultReputation.setMeasureDate(StaticVars.currentDate);
        reputationResultController.saveHistoryReputation(StaticVars.currentDomainName,thisResultReputation);
    }

    /**
     * Method for save a site's reputation result into external file
     * @param domainName
     * @param siteReputation
     */
    public static void saveHistoryReputation(String domainName, historySitesReputation siteReputation) {
        List<Pair<String,historySitesReputation>> listHistoryReputation = reputationResultController.loadHistoryReputation();
        Pair<String,historySitesReputation> addedHistoryReputation = new Pair<String, historySitesReputation>(domainName,siteReputation);
        listHistoryReputation.add(addedHistoryReputation);
        // Write again into external file
        StringBuffer rawContent = new StringBuffer();
        for (int i=0;i<listHistoryReputation.size();i++) {
            Pair<String,historySitesReputation> thisHistoryReputation = listHistoryReputation.get(i);
            String sitesName = thisHistoryReputation.getValue0();
            historySitesReputation historyReputation = thisHistoryReputation.getValue1();
            String sitesLabel = historyReputation.getLabelNormality();
            String sitesResponseTime = String.valueOf(historyReputation.getResponseTime());
            String sitesDateMeasure = String.valueOf(historyReputation.getMeasureDate());
            Triplet<Double,Double,Double> sitesComposition = historyReputation.getCompositionDangerousity();
            Double malwareComposition = sitesComposition.getValue0();
            Double phishingComposition = sitesComposition.getValue0();
            Double spammingComposition = sitesComposition.getValue0();
            rawContent.append(sitesName + "*" + sitesLabel + "*" + sitesResponseTime + "*" + sitesDateMeasure
                    + "*" + malwareComposition + "*" + phishingComposition + "*" + spammingComposition + "\n");
        }
        EksternalFile.saveRawContentToEksternalFile(rawContent.toString(), "src/sample/history/historyReputation.txt");
    }

    /**
     * Method for load history reputation from external file
     * @return
     */
    public static List<Pair<String,historySitesReputation>> loadHistoryReputation() {
        List<Pair<String,historySitesReputation>> listHistoryReputation = new ArrayList<Pair<String, historySitesReputation>>();
        String rawContent = EksternalFile.getRawFileContent("src/sample/history/historyReputation.txt");
        if (!rawContent.isEmpty()) {
            StringTokenizer stringPerLine = new StringTokenizer(rawContent,"\n");
            while (stringPerLine.hasMoreTokens()) {
                String line = stringPerLine.nextToken();
                StringTokenizer elementEachLine = new StringTokenizer(line,"*");
                int counter = 1;
                String domainName = "";
                historySitesReputation thisHistoryReputation = new historySitesReputation();
                Double malwareComposition = 0.0, phishingComposition = 0.0, spammingComposition = 0.0;
                while (elementEachLine.hasMoreTokens()) {
                    String element = elementEachLine.nextToken();
                    switch (counter) {
                        case 1  :
                            domainName = element; break;
                        case 2  :
                            thisHistoryReputation.setLabelNormality(element); break;
                        case 3  :
                            thisHistoryReputation.setResponseTime(Long.parseLong(element)); break;
                        case 4  :
                            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.ENGLISH);
                            try {
                                Date measureDate = formatter.parse(element);
                                thisHistoryReputation.setMeasureDate(measureDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 5  :
                            malwareComposition = Double.parseDouble(element); break;
                        case 6  :
                            phishingComposition = Double.parseDouble(element); break;
                        case 7  :
                            spammingComposition = Double.parseDouble(element); break;
                    }
                    counter++;
                }
                Triplet<Double,Double,Double> composition =
                        new Triplet<Double, Double, Double>(malwareComposition,phishingComposition,spammingComposition);
                thisHistoryReputation.setCompositionDangerousity(composition);
                Pair<String,historySitesReputation> tupleHistoryReputation =
                        new Pair<String, historySitesReputation>(domainName,thisHistoryReputation);
                listHistoryReputation.add(tupleHistoryReputation);
            }
        }
        return listHistoryReputation;
    }

    public void handleBackButton(ActionEvent actionEvent) {
        // Back into main page
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/main_page.css").toExternalForm());
        stage.setScene(scene);
    }
}
