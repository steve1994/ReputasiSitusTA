package sample;

import Utils.API.WOT_API_Loader;
import Utils.Converter;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import Utils.Statistics;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Sextet;
import weka.SitesClusterer;
import weka.SitesMLProcessor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by steve on 08/04/2016.
 */
public class StatistikController implements Initializable{
    @FXML private TextArea responseTimeStat;
    @FXML private TextArea percentageNullStat;

    public void initialize(URL location, ResourceBundle resources) {
        // Cluster sites dengan tipe reputasi 7 dan jumlah cluster 4
        SitesClusterer clusterSite = new SitesClusterer(StaticVars.reputationType);
        clusterSite.configARFFInstance(new String[] {"malware","phishing","spamming","normal"});
        System.out.println("Config ARFF Done");

        // Time performance logger
        List<Long> listTimeTLDRatioAS = new ArrayList<Long>();
        List<Long> listTimeHitRatioAS = new ArrayList<Long>();
        List<Long> listTimeNSDistAS = new ArrayList<Long>();
        List<Long> listTimeNSCount = new ArrayList<Long>();
        List<Long> listTimeTTLNS = new ArrayList<Long>();
        List<Long> listTimeTTLIP = new ArrayList<Long>();
        List<Long> listTimeTokenCount = new ArrayList<Long>();
        List<Long> listTimeAvgToken = new ArrayList<Long>();
        List<Long> listTimeSLDRatio = new ArrayList<Long>();
        List<Long> listTimeInboundLink = new ArrayList<Long>();
        List<Long> listTimeLookupTime = new ArrayList<Long>();
        List<Long> listTimeTrust = new ArrayList<Long>();

        for (int i=0;i<StaticVars.numSitesTraining;i++) {
            // SET RECORD INSTANCE DATA STRUCTURE
            SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(StaticVars.listSitesTraining.get(i),StaticVars.reputationType);
            clusterSite.fillDataIntoInstanceRecord(recordML,"normal");

            System.out.println("Situs ke-" + (i+1));
        }

        StringBuffer responseTimePerAttributes = new StringBuffer();
        if ((StaticVars.reputationType == 1) || (StaticVars.reputationType == 4) || (StaticVars.reputationType == 5) || (StaticVars.reputationType == 7)) {
            responseTimePerAttributes.append("FITUR DNS : \n");
            responseTimePerAttributes.append("Avg Time TLD Ratio AS : " + Statistics.getAverageListLong(listTimeTLDRatioAS) + " ms \n");
            responseTimePerAttributes.append("Avg Time Hit Ratio AS : " + Statistics.getAverageListLong(listTimeHitRatioAS) + " ms \n");
            responseTimePerAttributes.append("Avg Time NS Distribution AS : " + Statistics.getAverageListLong(listTimeNSDistAS) + " ms \n");
            responseTimePerAttributes.append("Avg Time NS Count : " + Statistics.getAverageListLong(listTimeNSCount) + " ms \n");
            responseTimePerAttributes.append("Avg Time TTL NS : " + Statistics.getAverageListLong(listTimeTTLNS) + " ms \n");
            responseTimePerAttributes.append("Avg Time TTL IP : " + Statistics.getAverageListLong(listTimeTTLIP) + " ms \n");
        }
        if ((StaticVars.reputationType == 2) || (StaticVars.reputationType == 4) || (StaticVars.reputationType == 6) || (StaticVars.reputationType == 7)) {
            responseTimePerAttributes.append("FITUR SPESIFIK :  \n");
            responseTimePerAttributes.append("Avg Time Token Count : " + Statistics.getAverageListLong(listTimeTokenCount) + " ms \n");
            responseTimePerAttributes.append("Avg Time Avg Token : " + Statistics.getAverageListLong(listTimeAvgToken) + " ms \n");
            responseTimePerAttributes.append("Avg Time SLD Ratio : " + Statistics.getAverageListLong(listTimeSLDRatio) + " ms \n");
            responseTimePerAttributes.append("Avg Time Inbound Link : " + Statistics.getAverageListLong(listTimeInboundLink) + " ms \n");
            responseTimePerAttributes.append("Avg Time Lookup Time : " + Statistics.getAverageListLong(listTimeLookupTime) + " ms \n");
        }
        if ((StaticVars.reputationType == 3) || (StaticVars.reputationType == 5) || (StaticVars.reputationType == 6) || (StaticVars.reputationType == 7)) {
            responseTimePerAttributes.append("FITUR TRUST :  \n");
            responseTimePerAttributes.append("Avg Time Trust : " + Statistics.getAverageListLong(listTimeTrust) + " ms \n");
        }
        responseTimeStat.setText(responseTimePerAttributes.toString());

        // Tulis statistik non null data tiap attribute terlibat
        StringBuffer percentageNotNullPerAttribute = new StringBuffer();
        List<Pair<String,Double>> percentageNotNullData = clusterSite.getPercentageNotNullData(clusterSite.getSiteReputationRecord());
        for (Pair<String,Double> percent : percentageNotNullData) {
            percentageNotNullPerAttribute.append("Attribute Name : " + percent.getValue0() + "  Percentage not null data : " + percent.getValue1() + "\n");
        }
        percentageNullStat.setText(percentageNotNullPerAttribute.toString());

        // Tulis instance di eksternal file
        String fileName = "num_" + StaticVars.numSitesTraining + ".type_" + StaticVars.reputationType + ".unsupervised.txt";
        String pathName = "database/weka/" + fileName;
        EksternalFile.saveInstanceWekaToExternalARFF(clusterSite.getSiteReputationRecord(),pathName);
    }

    public void handleBackButton(ActionEvent actionEvent) {
        // Go into Statistic screens
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
