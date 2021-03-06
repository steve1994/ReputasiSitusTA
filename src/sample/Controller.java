package sample;

import Utils.API.WOT_API_Loader;
import Utils.Converter;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import weka.SitesClusterer;
import weka.SitesHybrid;
import weka.SitesLabeler;
import weka.SitesMLProcessor;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
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
    private final ToggleGroup clusteringRadioButton;
    public TextField domainSitesTextField;
    public ChoiceBox numberTrainingChoiceBox;
    public Label keterangan;
//    public ChoiceBox numberTrainingChoiceBoxUnsupervised;
    public RadioButton KMeansRadioButton;
    public RadioButton HCRadioButton;
    public RadioButton EMRadioButton;
//    public ChoiceBox numKNNChoiceBox;
    public CheckBox updateDatabaseCheckBox;

    public Controller() {
        methodRadioButton = new ToggleGroup();
        featuresRadioButton = new ToggleGroup();
        numberTrainingChoiceBox = new ChoiceBox();
        clusteringRadioButton = new ToggleGroup();
//        numberTrainingChoiceBoxUnsupervised = new ChoiceBox();
//        numKNNChoiceBox = new ChoiceBox();
        updateDatabaseCheckBox = new CheckBox();
    }

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
        KMeansRadioButton.setToggleGroup(clusteringRadioButton);
        EMRadioButton.setToggleGroup(clusteringRadioButton);
        HCRadioButton.setToggleGroup(clusteringRadioButton);
        numberTrainingChoiceBox.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));
        numberTrainingChoiceBox.getSelectionModel().selectFirst();
//        numberTrainingChoiceBoxUnsupervised.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000));
//        numberTrainingChoiceBoxUnsupervised.getSelectionModel().selectFirst();
//        numKNNChoiceBox.setItems(FXCollections.observableArrayList(1, 3, 5, 7, 9, 11, 13, 15, 17, 19));
//        numKNNChoiceBox.getSelectionModel().selectFirst();
        keterangan.setVisible(false);
    }

    public void handleDiagnoseButton(ActionEvent actionEvent) {
        // Save static vars (method option and reputation option)
        if (dnsRadioButton.isSelected()) {
            StaticVars.reputationType = 1;
        } else if (spesificRadioButton.isSelected()) {
            StaticVars.reputationType = 2;
        } else if (trustRadioButton.isSelected()) {
            StaticVars.reputationType = 3;
        } else if (dnsSpesificRadioButton.isSelected()) {
            StaticVars.reputationType = 4;
        } else if (dnsTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 5;
        } else if (spesificTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 6;
        } else if (dnsSpesificTrustRadioButton.isSelected()) {
            StaticVars.reputationType = 7;
        }

        if (supervisedRadioButton.isSelected()) {
            StaticVars.methodType = 1;
        } else if (unsupervisedRadioButton.isSelected()) {
            StaticVars.methodType = 2;
        } else if (hybridRadioButton.isSelected()) {
            StaticVars.methodType = 3;
        }

        Boolean dataEmpty = false;
        keterangan.setVisible(false);
        // Extract Features From Domain Name and Process Based on Its method
        if (!domainSitesTextField.getText().isEmpty() && !numberTrainingChoiceBox.getSelectionModel().isEmpty() && (featuresRadioButton.getSelectedToggle() != null) && (methodRadioButton.getSelectedToggle() != null)
                && (clusteringRadioButton.getSelectedToggle() != null) /*&& !numberTrainingChoiceBoxUnsupervised.getSelectionModel().isEmpty() && !numKNNChoiceBox.getSelectionModel().isEmpty() */) {

            String domainName = Converter.getBaseHostURL(domainSitesTextField.getText());
            int numTrainingSites = (Integer) numberTrainingChoiceBox.getSelectionModel().getSelectedItem();
//            int numTrainingSitesUnsupervised = (Integer) numberTrainingChoiceBoxUnsupervised.getSelectionModel().getSelectedItem();
//            int optimumKNN = (Integer) numKNNChoiceBox.getSelectionModel().getSelectedItem();
            int optimumKNN = (int) Math.sqrt(numTrainingSites);

            long startResponseTime = System.currentTimeMillis();

            SiteRecordReputation thisDomainNameFeatures = SitesMLProcessor.extractFeaturesFromDomain(domainName, StaticVars.reputationType);
            String labelDomainNameResult = "";
            Triplet<Double, Double, Double> compositionDangerousity = null;
            if (supervisedRadioButton.isSelected()) {
                // Convert extracted feature into instance weka
                SitesLabeler sitesLabeler = new SitesLabeler(StaticVars.reputationType);
                sitesLabeler.configARFFInstance(new String[]{"normal", "abnormal"});
                sitesLabeler.fillDataIntoInstanceRecord(thisDomainNameFeatures, "normal");
                Instances convertedFeature = sitesLabeler.getSiteReputationRecord();

                // Classified site into two stages (SVM and kNN)
                String pathClassifier1 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalitySVM.model";
                Classifier optimumSupervisedClassifier1 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);
                if (optimumSupervisedClassifier1 != null) {
                    double classValue = 0;
                    try {
                        // Do classification stage I (SVM)
                        classValue = optimumSupervisedClassifier1.classifyInstance(convertedFeature.instance(0));
                        String normalityLabel = convertedFeature.classAttribute().value((int) classValue);
                        if (normalityLabel.equals("abnormal")) {
                            // Convert instance label first into dangerous label
                            Instances dangerousConvertedFeature = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);
                            // Do classification stage II (kNN)
                            String pathClassifier2 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKNN_" + optimumKNN + ".model";
                            Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);
                            if (optimumSupervisedClassifier2 != null) {
                                double classValueDangerousity = optimumSupervisedClassifier2.classifyInstance(dangerousConvertedFeature.instance(0));
                                labelDomainNameResult = dangerousConvertedFeature.classAttribute().value((int) classValueDangerousity);

//                                // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
//                                String pathTrainingClassifierKNN = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.supervised.arff";
//                                Instances trainingClassifierKNN = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingClassifierKNN);
//                                if (trainingClassifierKNN != null) {
//                                    trainingClassifierKNN.setClassIndex(trainingClassifierKNN.numAttributes() - 1);
//                                    convertedFeature.instance(0).setClassValue(classValueDangerousity);
//                                    trainingClassifierKNN.add(convertedFeature.instance(0));
//                                    EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierKNN, pathTrainingClassifierKNN);
//                                    // REBUILD CLASSIFIER KNN AGAIN AND SAVE AGAIN TO FILE
//                                    Classifier updatedClassifierKNN = new IBk(optimumKNN);
//                                    updatedClassifierKNN.buildClassifier(trainingClassifierKNN);
//                                    EksternalFile.saveClassifierToExternalModel(updatedClassifierKNN, pathClassifier2);
//                                }
                            } else {
                                dataEmpty = true;
                            }
                        } else {
                            labelDomainNameResult = "normal";
                        }
                        System.out.println("CURRENT LABEL RESULT SUPERVISED : " + labelDomainNameResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                    String pathTrainingClassifierSVM = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normality_category.supervised.arff";
                    Instances trainingClassifierSVM = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingClassifierSVM);
                    if (trainingClassifierSVM != null) {
//                        trainingClassifierSVM.setClassIndex(trainingClassifierSVM.numAttributes() - 1);
//                        convertedFeature.instance(0).setClassValue(classValue);
//                        trainingClassifierSVM.add(convertedFeature.instance(0));
//                        EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierSVM, pathTrainingClassifierSVM);
//                        // REBUILD CLASSIFIER SVM AND SAVE AGAIN TO FILE
//                        Classifier updatedClassifierSVM = new LibSVM();
//                        try {
//                            updatedClassifierSVM.buildClassifier(trainingClassifierSVM);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        EksternalFile.saveClassifierToExternalModel(updatedClassifierSVM, pathClassifier1);
                    } else {
                        dataEmpty = true;
                    }
                } else {
                    dataEmpty = true;
                }

            } else if (unsupervisedRadioButton.isSelected()) {
                // Convert extracted feature into instance weka
                SitesClusterer sitesClusterer = new SitesClusterer(StaticVars.reputationType);
                sitesClusterer.configARFFInstance(new String[]{"normal", "abnormal"});
                sitesClusterer.fillDataIntoInstanceRecord(thisDomainNameFeatures, "normal");
                Instances convertedFeature = sitesClusterer.getSiteReputationRecord();

                // Cluster sites stage I (normality sites) incrementally
                String pathTrainingNormalKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normality_category.unsupervised.arff";
                Instances trainingNormalKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingNormalKmeans);
                String pathClustererNormalKmeans = "";
                if (EMRadioButton.isSelected()) {
                    pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityEM.model";
                } else if (HCRadioButton.isSelected()) {
                    pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityHC.model";
                } else {
                    pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityKmeans.model";
                }
                Clusterer KmeansNormalClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererNormalKmeans);

                if (trainingNormalKmeans != null && KmeansNormalClusterer != null) {
                    trainingNormalKmeans.setClassIndex(trainingNormalKmeans.numAttributes() - 1);
                    trainingNormalKmeans.add(convertedFeature.instance(0));
                    String labelNormalityKmeans = "";
                    int classValueCluster = 0;
                    try {
                        ClusterEvaluation evalNormalKmeans = sitesClusterer.evaluateClusterReputationModel(trainingNormalKmeans, KmeansNormalClusterer);
                        double[] clusterAssigment1 = evalNormalKmeans.getClusterAssignments();
                        int[] classesToCluster1 = evalNormalKmeans.getClassesToClusters();
                        for (int i = 0; i < trainingNormalKmeans.numInstances(); i++) {
                            if (trainingNormalKmeans.instance(i).toString().equals(convertedFeature.instance(0).toString())) {
                                double clusterNumber = clusterAssigment1[i];
                                classValueCluster = classesToCluster1[(int) clusterNumber];
                                if (classValueCluster != -1) {
                                    labelNormalityKmeans = trainingNormalKmeans.classAttribute().value(classValueCluster);
                                } else {
                                    labelNormalityKmeans = "unknown";
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (labelNormalityKmeans.equals("normal")) {
                        labelDomainNameResult = "normal";
                    } else if (labelNormalityKmeans.equals("unknown")) {
                        labelDomainNameResult = "unknown";
                    } else {
                        // Cluster Site Stage II (dangerousity sites) incrementally
                        String pathTrainingDangerousKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.unsupervised.arff";
                        Instances trainingDangerousKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingDangerousKmeans);
                        String pathClustererDangerousKmeans = "";
                        if (EMRadioButton.isSelected()) {
                            pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityEM.model";
                        } else if (HCRadioButton.isSelected()) {
                            pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityHC.model";
                        } else {
                            pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKmeans.model";
                        }
                        Clusterer KmeansDangerousClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererDangerousKmeans);

                        if (trainingDangerousKmeans != null && KmeansDangerousClusterer != null) {
                            trainingDangerousKmeans.setClassIndex(trainingDangerousKmeans.numAttributes() - 1);
                            Instances convertedFeatureDangerous = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);
                            trainingDangerousKmeans.add(convertedFeatureDangerous.instance(0));
                            try {
                                ClusterEvaluation evalDangerousKmeans = sitesClusterer.evaluateClusterReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer);
                                double[] clusterAssignment2 = evalDangerousKmeans.getClusterAssignments();
                                int[] classesToCluster2 = evalDangerousKmeans.getClassesToClusters();
                                double clusterNumber = 0;
                                int classValueClusterDangerous = 0;
                                for (int i = 0; i < trainingDangerousKmeans.numInstances(); i++) {
                                    if (trainingDangerousKmeans.instance(i).toString().equals(convertedFeatureDangerous.instance(0).toString())) {
                                        clusterNumber = clusterAssignment2[i];
                                        classValueClusterDangerous = classesToCluster2[(int) clusterNumber];
                                        if (classValueClusterDangerous != -1) {
//                                            labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous);
                                            labelDomainNameResult = "malicious (" + trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous) + " detected)";
                                        } else {
                                            labelDomainNameResult = "malicious (unknown type)";
                                        }
                                    }
                                }
                                compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, trainingDangerousKmeans).get((int) clusterNumber);

                                if (!labelDomainNameResult.equals("malicious (unknown type)")) {
                                    if (updateDatabaseCheckBox.isSelected()) {
                                        // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                                        trainingDangerousKmeans.instance(trainingDangerousKmeans.numInstances() - 1).setClassValue(classValueClusterDangerous);
                                        EksternalFile.saveInstanceWekaToExternalARFF(trainingDangerousKmeans, pathTrainingDangerousKmeans);
                                        // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                                        try {
                                            Clusterer updatedClustererDangerousity = sitesClusterer.buildKmeansReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer.numberOfClusters());
                                            EksternalFile.saveClustererToExternalModel(updatedClustererDangerousity, pathClustererDangerousKmeans);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            dataEmpty = true;
                        }
                    }

                    if (!labelDomainNameResult.equals("unknown") && !labelDomainNameResult.equals("malicious (unknown type)")) {
                        if (updateDatabaseCheckBox.isSelected()) {
                            // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                            trainingNormalKmeans.instance(trainingNormalKmeans.numInstances() - 1).setClassValue(classValueCluster);
                            EksternalFile.saveInstanceWekaToExternalARFF(trainingNormalKmeans, pathTrainingNormalKmeans);
                            // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                            try {
                                Clusterer updatedClustererNormality = sitesClusterer.buildKmeansReputationModel(trainingNormalKmeans, KmeansNormalClusterer.numberOfClusters());
                                EksternalFile.saveClustererToExternalModel(updatedClustererNormality, pathClustererNormalKmeans);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    dataEmpty = true;
                }
            } else if (hybridRadioButton.isSelected()) {
                // Convert extracted feature into instance weka
                SitesClusterer sitesClusterer = new SitesClusterer(StaticVars.reputationType);
                sitesClusterer.configARFFInstance(new String[]{"normal", "abnormal"});
                sitesClusterer.fillDataIntoInstanceRecord(thisDomainNameFeatures, "normal");
                Instances convertedFeature = sitesClusterer.getSiteReputationRecord();

                // Load Classifier for STAGE I
                String pathTrainingNormalKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normality_category.hybrid.arff";
                Instances trainingNormalKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingNormalKmeans);
                String pathClassifier1 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalitySVM.hybrid.model";
                Classifier optimumSupervisedClassifier1 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);

                if (trainingNormalKmeans != null && optimumSupervisedClassifier1 != null) {
                    // Add diagnosed site into training data proceed
                    trainingNormalKmeans.setClassIndex(trainingNormalKmeans.numAttributes() - 1);
                    trainingNormalKmeans.add(convertedFeature.instance(0));

                    // STAGE 1 (Classify Cluster Member LibSVM)
                    Instance classifiedConvertedFeature = convertedFeature.instance(0);
                    Instances classifiedNormalityInstances = new Instances("normal_sites_classified_hybrid",sitesClusterer.getAttributesVector(trainingNormalKmeans),0);
                    classifiedNormalityInstances.setClassIndex(classifiedNormalityInstances.numAttributes()-1);
                    Enumeration normalityInstances = trainingNormalKmeans.enumerateInstances();
                    while (normalityInstances.hasMoreElements()) {
                        Instance thisInstanceNormality = (Instance) normalityInstances.nextElement();
                        try {
                            double classValue = optimumSupervisedClassifier1.classifyInstance(thisInstanceNormality);
                            if (thisInstanceNormality.toString().equals(convertedFeature.instance(0).toString())) {
                                classifiedConvertedFeature.setClassValue(classValue);
                            }
                            thisInstanceNormality.setClassValue(classValue);
                            classifiedNormalityInstances.add(thisInstanceNormality);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Load Clusterer for STAGE I
                    String pathClustererNormalKmeans = "";
                    if (EMRadioButton.isSelected()) {
                        pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityEMStageI.hybrid.model";
                    } else if (HCRadioButton.isSelected()) {
                        pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityHCStageI.hybrid.model";
                    } else {
                        pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityKmeansStageI.hybrid.model";
                    }
                    Clusterer KmeansNormalClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererNormalKmeans);

                    if (KmeansNormalClusterer != null) {
                        // STAGE 1 (Cluster based on classify member KMeans)
                        String labelNormality = "";
                        int classValueCluster = 0;
                        Clusterer normalClusterer = null;
                        try {
                            normalClusterer = sitesClusterer.buildKmeansReputationModel(classifiedNormalityInstances,KmeansNormalClusterer.numberOfClusters());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ClusterEvaluation evalNormalKmeans = sitesClusterer.evaluateClusterReputationModel(classifiedNormalityInstances, normalClusterer);
                        double[] clusterAssignment1 = evalNormalKmeans.getClusterAssignments();
                        int[] classesToCluster1 = evalNormalKmeans.getClassesToClusters();
                        for (int i=0;i<classifiedNormalityInstances.numInstances();i++) {
                            if (classifiedNormalityInstances.instance(i).toString().equals(classifiedConvertedFeature.toString())) {
                                double clusterNumber = clusterAssignment1[i];
                                classValueCluster = classesToCluster1[(int) clusterNumber];
                                if (classValueCluster != -1) {
                                    labelNormality = classifiedNormalityInstances.classAttribute().value(classValueCluster);
                                } else {
                                    labelNormality = "unknown";
                                }
                            }
                        }

                        if (labelNormality.equals("normal")) {
                            labelDomainNameResult = "normal";
                        } else if (labelNormality.equals("unknown")) {
                            labelDomainNameResult = "unknown";
                        } else {
                            // Convert diagnosed site into dangerousity label
                            Instances dangerousConvertedFeature = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);

                            // Load Classifier and Training STAGE 2
                            String pathTrainingDangerousKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.hybrid.arff";
                            Instances trainingDangerousKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingDangerousKmeans);
                            String pathClassifier2 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKNN.hybrid.model";
                            Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);

                            if (trainingDangerousKmeans != null && optimumSupervisedClassifier2 != null) {
                                // Add diagnosed site into training data proceed
                                trainingDangerousKmeans.setClassIndex(trainingDangerousKmeans.numAttributes()-1);
                                trainingDangerousKmeans.add(dangerousConvertedFeature.instance(0));

                                // STAGE 2 (Classify Cluster Member kNN)
                                Instance classifiedDangerousConvertedFeature = dangerousConvertedFeature.instance(0);
                                Instances classifiedDangerousityInstances = new Instances("dangerous_sites_classified_hybrid",sitesClusterer.getAttributesVector(trainingDangerousKmeans),0);
                                classifiedDangerousityInstances.setClassIndex(classifiedDangerousityInstances.numAttributes()-1);
                                Enumeration dangerousityInstances = trainingDangerousKmeans.enumerateInstances();
                                while (dangerousityInstances.hasMoreElements()) {
                                    Instance thisInstanceDangerousity = (Instance) dangerousityInstances.nextElement();
                                    try {
                                        double classValue = optimumSupervisedClassifier2.classifyInstance(thisInstanceDangerousity);
                                        if (thisInstanceDangerousity.toString().equals(dangerousConvertedFeature.instance(0).toString())) {
                                            classifiedDangerousConvertedFeature.setClassValue(classValue);
                                        }
                                        thisInstanceDangerousity.setClassValue(classValue);
                                        classifiedDangerousityInstances.add(thisInstanceDangerousity);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                // STAGE 2 (Cluster based on classified member KMeans)
                                String pathClustererDangerousKmeans = "";
                                if (EMRadioButton.isSelected()) {
                                    pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityEMStageII.hybrid.model";
                                } else if (HCRadioButton.isSelected()) {
                                    pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityHCStageII.hybrid.model";
                                } else {
                                    pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKmeansStageII.hybrid.model";
                                }
                                Clusterer KmeansDangerousClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererDangerousKmeans);

                                if (KmeansDangerousClusterer != null) {
                                    try {
                                        Clusterer dangerousClusterer = sitesClusterer.buildKmeansReputationModel(classifiedDangerousityInstances,KmeansDangerousClusterer.numberOfClusters());
                                        ClusterEvaluation evalDangerousKmeans = sitesClusterer.evaluateClusterReputationModel(classifiedDangerousityInstances, dangerousClusterer);
                                        double[] clusterAssignment2 = evalDangerousKmeans.getClusterAssignments();
                                        int[] classesToCluster2 = evalDangerousKmeans.getClassesToClusters();
                                        double clusterNumber = 0;
                                        int classValueClusterDangerous = 0;
                                        for (int i = 0; i < classifiedDangerousityInstances.numInstances(); i++) {
                                            if (classifiedDangerousityInstances.instance(i).toString().equals(classifiedDangerousConvertedFeature.toString())) {
                                                clusterNumber = clusterAssignment2[i];
                                                classValueClusterDangerous = classesToCluster2[(int) clusterNumber];
                                                if (classValueClusterDangerous != -1) {
//                                                    labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous);
                                                    labelDomainNameResult = "malicious (" + trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous) + " detected)";
                                                } else {
                                                    labelDomainNameResult = "malicious (unknown type)";
                                                }
                                            }
                                        }
                                        compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, classifiedDangerousityInstances).get((int) clusterNumber);

                                        if (!labelDomainNameResult.equals("malicious (unknown type)")) {
                                            if (updateDatabaseCheckBox.isSelected()) {
                                                // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                                                trainingDangerousKmeans.instance(trainingDangerousKmeans.numInstances() - 1).setClassValue(classValueClusterDangerous);
                                                EksternalFile.saveInstanceWekaToExternalARFF(trainingDangerousKmeans, pathTrainingDangerousKmeans);
                                                // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                                                try {
                                                    EksternalFile.saveClustererToExternalModel(dangerousClusterer, pathClustererDangerousKmeans);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    dataEmpty = true;
                                }
                            } else {
                                dataEmpty = true;
                            }
                        }

                        if (!labelDomainNameResult.equals("unknown") && !labelDomainNameResult.equals("malicious (unknown type)")) {
                            if (updateDatabaseCheckBox.isSelected()) {
                                // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                                trainingNormalKmeans.instance(trainingNormalKmeans.numInstances() - 1).setClassValue(classValueCluster);
                                EksternalFile.saveInstanceWekaToExternalARFF(trainingNormalKmeans, pathTrainingNormalKmeans);
                                // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                                try {
                                    EksternalFile.saveClustererToExternalModel(normalClusterer, pathClustererNormalKmeans);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        dataEmpty = true;
                    }
                } else {
                    dataEmpty = true;
                }
            }

            long endResponseTime = System.currentTimeMillis();

            if (!dataEmpty) {
                // Catat waktu response time dan tanggal sekarang
                Long responseTime = (endResponseTime - startResponseTime) / 1000;
                Date currentDate = new Date();
                // Simpan hasil pengukuran ke static variable
                StaticVars.currentDomainName = domainName;
                StaticVars.currentLabel = labelDomainNameResult;
                StaticVars.currentResponseTime = String.valueOf(responseTime);
                StaticVars.currentDate = currentDate;
                if (unsupervisedRadioButton.isSelected() || hybridRadioButton.isSelected()) {
                    if (StaticVars.currentLabel.equals("normal") || StaticVars.currentLabel.equals("unknown") || StaticVars.currentLabel.equals("malicious (unknown type)")) {
                        StaticVars.currentComposition = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);
                    } else {
                        StaticVars.currentComposition = compositionDangerousity;
                    }
                } else {
                    StaticVars.currentComposition = new Triplet<Double, Double, Double>(0.0, 0.0, 0.0);
                }

                // Pindah ke layar hasil pengukuran
                Parent root = null;
                try {
                    root = FXMLLoader.load(getClass().getResource("reputation_result.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("css/main_page.css").toExternalForm());
                stage.setScene(scene);
            } else {
                keterangan.setVisible(true);
                keterangan.setText("EMPTY DATA !");
            }
        } else {
            keterangan.setVisible(true);
            keterangan.setText("NOT COMPLETE INPUT !");
        }
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
