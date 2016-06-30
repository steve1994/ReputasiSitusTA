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
import org.javatuples.Sextet;
import weka.SitesClusterer;

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
            // DNS FEATURES
            DNS_Feature fiturDNS = new DNS_Feature();

            if ((StaticVars.reputationType == 1) || (StaticVars.reputationType == 4) || (StaticVars.reputationType == 5) || (StaticVars.reputationType == 7)) {
                long beforeDNS = System.currentTimeMillis();

                // TLD ratio
                Pair<Double,Sextet<Double, Double, Double, Double, Double, Double>> TLDRatio = DNSExtractor.getTLDDistributionFromAS(StaticVars.listSitesTraining.get(i));
                Double[] TLDRatioList = new Double[6];
                TLDRatioList[0] = TLDRatio.getValue1().getValue0();
                TLDRatioList[1] = TLDRatio.getValue1().getValue1();
                TLDRatioList[2] = TLDRatio.getValue1().getValue2();
                TLDRatioList[3] = TLDRatio.getValue1().getValue3();
                TLDRatioList[4] = TLDRatio.getValue1().getValue4();
                TLDRatioList[5] = TLDRatio.getValue1().getValue5();
                fiturDNS.setPopularTLDRatio(TLDRatioList);
                System.out.println("TLD Ratio");

                long afterTLDRatio = System.currentTimeMillis();

                // Hit AS Ratio (malware, phishing, spamming)
                Double[] HitRatioList = new Double[3];
                int thisSiteASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(StaticVars.listSitesTraining.get(i)));
                for (int j = 0; j < 3; j++) {
                    HitRatioList[j] = DNSExtractor.getHitASRatio(thisSiteASN, j + 1);
                }
                fiturDNS.setHitASRatio(HitRatioList);
                System.out.println("Hit AS Ratio");

                long afterHitRatio = System.currentTimeMillis();

                // Name server distribution AS
//                fiturDNS.setDistributionNSAS(DNSExtractor.getDistributionNSFromAS(StaticVars.listSitesTraining.get(i)));
                fiturDNS.setDistributionNSAS(TLDRatio.getValue0());
                System.out.println("Name Server Distribution AS");

                long afterNSDist = System.currentTimeMillis();

                // Name server count
                fiturDNS.setNumNameServer(DNSExtractor.getNumNameServers(StaticVars.listSitesTraining.get(i)));
                System.out.println("Name Server Count");

                long afterNSCount = System.currentTimeMillis();

                // TTL Name Servers
                fiturDNS.setListNSTTL(DNSExtractor.getNameServerTimeToLive(StaticVars.listSitesTraining.get(i)));
                System.out.println("TTL Name Servers");

                long afterTTLNS = System.currentTimeMillis();

                // TTL DNS A Records
                fiturDNS.setListDNSRecordTTL(DNSExtractor.getDNSRecordTimeToLive(StaticVars.listSitesTraining.get(i)));
                System.out.println("TTL DNS Record");

                long afterTTLIP = System.currentTimeMillis();

                // TIME LOGGER SET
                listTimeTLDRatioAS.add(afterTLDRatio-beforeDNS);
                listTimeHitRatioAS.add(afterHitRatio-afterTLDRatio);
                listTimeNSDistAS.add(afterNSDist-afterHitRatio);
                listTimeNSCount.add(afterNSCount-afterNSDist);
                listTimeTTLNS.add(afterTTLNS-afterNSCount);
                listTimeTTLIP.add(afterTTLIP-afterTTLNS);
            }

            // SPESIFIC FEATURES
            Spesific_Feature fiturSpesific = new Spesific_Feature();

            if ((StaticVars.reputationType == 2) || (StaticVars.reputationType == 4) || (StaticVars.reputationType == 6) || (StaticVars.reputationType == 7)) {
                long beforeSpesific = System.currentTimeMillis();

                // Token Count URL
                fiturSpesific.setTokenCountURL(ContentExtractor.getDomainTokenCountURL(StaticVars.listSitesTraining.get(i)));
                System.out.println("Token Count URL");

                long afterTokenCount = System.currentTimeMillis();

                // Average Token Length URL
                fiturSpesific.setAverageTokenLengthURL(ContentExtractor.getAverageDomainTokenLengthURL(StaticVars.listSitesTraining.get(i)));
                System.out.println("Average Token Length URL");

                long afterAvgToken = System.currentTimeMillis();

                // SLD ratio from URL (malware, phishing, spamming)
                double[] SLDRatioList = new double[3];
                for (int j = 0; j < 3; j++) {
                    SLDRatioList[j] = ContentExtractor.getSLDHitRatio(StaticVars.listSitesTraining.get(i), j + 1);
                }
                fiturSpesific.setSLDRatio(SLDRatioList);
                System.out.println("SLD Ratio List");

                long afterSLDRatio = System.currentTimeMillis();

                // Inbound link Approximation (Google, Yahoo, Bing)
                double[] inboundLinkAppr = new double[3];
                for (int j = 0; j < 3; j++) {
                    inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults(StaticVars.listSitesTraining.get(i), j + 1);
                }
                fiturSpesific.setInboundLink(inboundLinkAppr);
                System.out.println("Inbound Link Approximation");

                long afterInboundLink = System.currentTimeMillis();

                // Lookup time to access site
                fiturSpesific.setLookupTime(ContentExtractor.getDomainLookupTimeSite(StaticVars.listSitesTraining.get(i)));
                System.out.println("Lookup Time");

                long afterLookupTime = System.currentTimeMillis();

                // SET TIME LOGGER
                listTimeTokenCount.add(afterTokenCount-beforeSpesific);
                listTimeAvgToken.add(afterAvgToken-afterTokenCount);
                listTimeSLDRatio.add(afterSLDRatio-afterAvgToken);
                listTimeInboundLink.add(afterInboundLink-afterSLDRatio);
                listTimeLookupTime.add(afterLookupTime-afterInboundLink);
            }

            // TRUST FEATURES
            Trust_Feature fiturTrust = new Trust_Feature();
            if ((StaticVars.reputationType == 3) || (StaticVars.reputationType == 5) || (StaticVars.reputationType == 6) || (StaticVars.reputationType == 7)) {
                long beforeTrust = System.currentTimeMillis();

                fiturTrust = WOT_API_Loader.loadAPIWOTForSite(StaticVars.listSitesTraining.get(i));
                System.out.println("Trust WOT");

                long afterTrust = System.currentTimeMillis();

                // SET TIME LOGGER
                listTimeTrust.add(afterTrust - beforeTrust);
            }

            // SET RECORD INSTANCE DATA STRUCTURE
            SiteRecordReputation recordML = new SiteRecordReputation();
            recordML.setDNSRecordFeature(fiturDNS);
            recordML.setSpesificRecordFeature(fiturSpesific);
            recordML.setTrustRecordFeature(fiturTrust);
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
