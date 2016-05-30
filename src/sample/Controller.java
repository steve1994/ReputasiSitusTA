package sample;

import Utils.API.WOT_API_Loader;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import data_structure.instance_ML.historySitesReputation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import weka.SitesClusterer;
import weka.SitesLabeler;
import weka.SitesMLProcessor;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public RadioButton supervisedRadioButton;
    public RadioButton unsupervisedRadioButton;
    public RadioButton hybridRadioButton;
    public RadioButton trustRadioButton;
    public RadioButton spesificRadioButton;
    public RadioButton dnsRadioButton;
    public RadioButton dnsSpesificRadioButton;
    public RadioButton dnsTrustRadioButton;
    public RadioButton spesificTrustRadioButton;
    public RadioButton dnsSpesificTrustRadioButton;
    private final ToggleGroup methodRadioButton;
    private final ToggleGroup featuresRadioButton;
    public TextField domainSitesTextField;

    public Controller() {
        methodRadioButton = new ToggleGroup();
        featuresRadioButton = new ToggleGroup();
    }


//    @FXML private RadioButton malwareRadioButton;
//    @FXML private RadioButton phishingRadioButton;
//    @FXML private RadioButton spammingRadioButton;
//    @FXML private RadioButton popularRadioButton;
//    @FXML private TextField numSitesTextField;
//    @FXML private CheckBox optionDNSCheckBox;
//    @FXML private CheckBox optionSpesificCheckBox;
//    @FXML private CheckBox optionTrustCheckBox;
//    private final ToggleGroup siteListRadioButton;

//    public void handleTrainingDataFeatures(ActionEvent actionEvent) {
//        // Setting static vars
//        StaticVars.numSitesTraining = Integer.parseInt(numSitesTextField.getText());
//        if (phishingRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(2).getKey();
//        } else if (spammingRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(3).getKey();
//        } else if (popularRadioButton.isSelected()) {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(4).getKey();
//        } else {
//            StaticVars.listSitesTraining = EksternalFile.loadSitesTrainingList(1).getKey();
//        }
//        if (optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 1;
//        } else if (!optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 2;
//        } else if (!optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 3;
//        } else if (optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 4;
//        } else if (optionDNSCheckBox.isSelected() && (!optionSpesificCheckBox.isSelected()) && (optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 5;
//        } else if (optionDNSCheckBox.isSelected() && (optionSpesificCheckBox.isSelected()) && (!optionTrustCheckBox.isSelected())) {
//            StaticVars.reputationType = 6;
//        } else {
//            StaticVars.reputationType = 7;
//        }
//
//        // Go into Statistic screens
//        Parent root = null;
//        try {
//            root = FXMLLoader.load(getClass().getResource("statistik.fxml"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root));
//    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supervisedRadioButton.setToggleGroup(methodRadioButton);
        unsupervisedRadioButton.setToggleGroup(methodRadioButton);
        hybridRadioButton.setToggleGroup(methodRadioButton);
        dnsRadioButton.setToggleGroup(featuresRadioButton);
        spesificRadioButton.setToggleGroup(featuresRadioButton);
        trustRadioButton.setToggleGroup(featuresRadioButton);
        dnsSpesificRadioButton.setToggleGroup(featuresRadioButton);
        dnsTrustRadioButton.setToggleGroup(featuresRadioButton);
        spesificTrustRadioButton.setToggleGroup(featuresRadioButton);
        dnsSpesificTrustRadioButton.setToggleGroup(featuresRadioButton);
    }

    public void handleDiagnoseButton(ActionEvent actionEvent) {
        // Save static vars (method option and reputation option)
        if (dnsRadioButton.isSelected()) {
            StaticVars.reputationType = 1;
        }
        else if (spesificRadioButton.isSelected()) {
            StaticVars.reputationType = 2;
        }
        else if (trustRadioButton.isSelected()) {
            StaticVars.reputationType = 3;
        }
        else if (dnsSpesificRadioButton.isSelected()) {
            StaticVars.reputationType = 4;
        }
        else if (dnsTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 5;
        }
        else if (spesificTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 6;
        }
        else if (dnsSpesificTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 7;
        }

        if (supervisedRadioButton.isSelected()) {
            StaticVars.methodType = 1;
        }
        else if (unsupervisedRadioButton.isSelected()) {
            StaticVars.methodType = 2;
        }
        else if (hybridRadioButton.isSelected()) {
            StaticVars.methodType = 3;
        }

        // Extract Features From Domain Name and Process Based on Its method
        String domainName = domainSitesTextField.getText();

        long startResponseTime = System.currentTimeMillis();

        SiteRecordReputation thisDomainNameFeatures = SitesMLProcessor.extractFeaturesFromDomain(domainName,StaticVars.reputationType);
        String labelDomainNameResult = "";
        Triplet<Double,Double,Double> compositionDangerousity = null;
        if (supervisedRadioButton.isSelected()) {
            // Convert extracted feature into instance weka
            SitesLabeler sitesLabeler = new SitesLabeler(StaticVars.reputationType);
            sitesLabeler.configARFFInstance(new String[]{"normal","abnormal"});
            sitesLabeler.fillDataIntoInstanceRecord(thisDomainNameFeatures,"normal");
            Instances convertedFeature = sitesLabeler.getSiteReputationRecord();

            // Classified site into two stages (SVM and kNN)
            String pathClassifier1 = "database/weka/model/num_1000.type_" + StaticVars.reputationType + ".normalitySVM.model";
            Classifier optimumSupervisedClassifier1 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);
            try {
                // Do classification stage I (SVM)
                double classValue = optimumSupervisedClassifier1.classifyInstance(convertedFeature.instance(0));
                String normalityLabel = convertedFeature.classAttribute().value((int) classValue);
                if (normalityLabel.equals("abnormal")) {
                    // Convert instance label first into dangerous label
                    Instances dangerousConvertedFeature = sitesLabeler.convertNormalityToDangerousityLabel(convertedFeature);
                    // Do classification stage II (kNN)
                    String pathClassifier2 = "database/weka/model/num_1000.type_" + StaticVars.reputationType + ".dangerousityKNN_10.model";
                    Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);
                    double classValueDangerousity = optimumSupervisedClassifier2.classifyInstance(dangerousConvertedFeature.instance(0));
                    labelDomainNameResult = dangerousConvertedFeature.classAttribute().value((int) classValueDangerousity);
                } else {
                    labelDomainNameResult = "normal";
                }
                System.out.println("CURRENT LABEL RESULT SUPERVISED : " + labelDomainNameResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (unsupervisedRadioButton.isSelected()) {
            // Convert extracted feature into instance weka
            SitesClusterer sitesClusterer = new SitesClusterer(StaticVars.reputationType);
            sitesClusterer.configARFFInstance(new String[]{"normal","abnormal"});
            sitesClusterer.fillDataIntoInstanceRecord(thisDomainNameFeatures,"normal");
            Instances convertedFeature = sitesClusterer.getSiteReputationRecord();


        }
        else if (hybridRadioButton.isSelected()) {

        }

        long endResponseTime = System.currentTimeMillis();

        // Catat waktu response time dan tanggal sekarang
        Long responseTime = (endResponseTime - startResponseTime) / 1000;
        Date currentDate = new Date();
        // Simpan hasil pengukuran ke static variable
        StaticVars.currentDomainName = domainName;
        StaticVars.currentLabel = labelDomainNameResult;
        StaticVars.currentResponseTime = String.valueOf(responseTime);
        StaticVars.currentDate = currentDate;
        if (unsupervisedRadioButton.isSelected() || hybridRadioButton.isSelected()) {
            StaticVars.currentComposition = compositionDangerousity;
        } else {
            StaticVars.currentComposition = new Triplet<Double,Double,Double>(0.0,0.0,0.0);
        }
        System.out.println(StaticVars.currentDomainName);
        System.out.println(StaticVars.currentLabel);
        System.out.println(StaticVars.currentResponseTime);
        System.out.println(StaticVars.currentDate);

        // Pindah ke layar hasil pengukuran
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("reputation_result.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/main_page.css").toExternalForm());
        stage.setScene(scene);
    }

    public void handleHistoryButton(ActionEvent actionEvent) {
        // Pindah ke layar hasil pengukuran
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("reputation_history.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("css/main_page.css").toExternalForm());
        stage.setScene(scene);
    }
}
