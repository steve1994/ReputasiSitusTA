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
    public ChoiceBox numberTrainingChoiceBox;

    public Controller() {
        methodRadioButton = new ToggleGroup();
        featuresRadioButton = new ToggleGroup();
        numberTrainingChoiceBox = new ChoiceBox();
    }

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
        numberTrainingChoiceBox.setItems(FXCollections.observableArrayList(100,200,300,400,500,600,700,800,900,1000));
        numberTrainingChoiceBox.getSelectionModel().selectFirst();
    }

    public void handleDiagnoseButton(ActionEvent actionEvent) {
        if (!domainSitesTextField.getText().isEmpty() && (featuresRadioButton.getSelectedToggle().isSelected()) && (methodRadioButton.getSelectedToggle().isSelected())) {
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

            // Extract Features From Domain Name and Process Based on Its method
            String domainName = Converter.getBaseHostURL(domainSitesTextField.getText());
            int numTrainingSites = (Integer) numberTrainingChoiceBox.getSelectionModel().getSelectedItem();

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
                double classValue = 0;
                try {
                    // Do classification stage I (SVM)
                    classValue = optimumSupervisedClassifier1.classifyInstance(convertedFeature.instance(0));
                    String normalityLabel = convertedFeature.classAttribute().value((int) classValue);
                    if (normalityLabel.equals("abnormal")) {
                        // Convert instance label first into dangerous label
                        Instances dangerousConvertedFeature = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);
                        // Do classification stage II (kNN)
                        int optimumKNN = 10;
                        String pathClassifier2 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKNN_" + optimumKNN + ".model";
                        Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);
                        double classValueDangerousity = optimumSupervisedClassifier2.classifyInstance(dangerousConvertedFeature.instance(0));
                        labelDomainNameResult = dangerousConvertedFeature.classAttribute().value((int) classValueDangerousity);

                        // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                        String pathTrainingClassifierKNN = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.supervised.arff";
                        Instances trainingClassifierKNN = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingClassifierKNN);
                        trainingClassifierKNN.setClassIndex(trainingClassifierKNN.numAttributes()-1);
                        convertedFeature.instance(0).setClassValue(classValueDangerousity);
                        trainingClassifierKNN.add(convertedFeature.instance(0));
                        EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierKNN,pathTrainingClassifierKNN);
                        // REBUILD CLASSIFIER KNN AGAIN AND SAVE AGAIN TO FILE
                        Classifier updatedClassifierKNN = new IBk(optimumKNN);
                        updatedClassifierKNN.buildClassifier(trainingClassifierKNN);
                        EksternalFile.saveClassifierToExternalModel(updatedClassifierKNN,pathClassifier2);

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
                trainingClassifierSVM.setClassIndex(trainingClassifierSVM.numAttributes()-1);
                convertedFeature.instance(0).setClassValue(classValue);
                trainingClassifierSVM.add(convertedFeature.instance(0));
                EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierSVM,pathTrainingClassifierSVM);
                // REBUILD CLASSIFIER SVM AND SAVE AGAIN TO FILE
                Classifier updatedClassifierSVM = new LibSVM();
                try {
                    updatedClassifierSVM.buildClassifier(trainingClassifierSVM);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EksternalFile.saveClassifierToExternalModel(updatedClassifierSVM,pathClassifier1);

            } else if (unsupervisedRadioButton.isSelected()) {
                // Convert extracted feature into instance weka
                SitesClusterer sitesClusterer = new SitesClusterer(StaticVars.reputationType);
                sitesClusterer.configARFFInstance(new String[]{"normal", "abnormal"});
                sitesClusterer.fillDataIntoInstanceRecord(thisDomainNameFeatures, "normal");
                Instances convertedFeature = sitesClusterer.getSiteReputationRecord();

                // Cluster sites stage I (normality sites) incrementally
                String pathTrainingNormalKmeans = "database/weka/data/num_100.type_" + StaticVars.reputationType + ".normality_category.unsupervised.arff";
                Instances trainingNormalKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingNormalKmeans);
                trainingNormalKmeans.setClassIndex(trainingNormalKmeans.numAttributes() - 1);
                String pathClustererNormalKmeans = "database/weka/model/num_100.type_" + StaticVars.reputationType + ".normalityKmeans.model";
                Clusterer KmeansNormalClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererNormalKmeans);

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
                            labelNormalityKmeans = trainingNormalKmeans.classAttribute().value(classValueCluster);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (labelNormalityKmeans.equals("normal")) {
                    labelDomainNameResult = "normal";
                } else {
                    // Cluster Site Stage II (dangerousity sites) incrementally
                    String pathTrainingDangerousKmeans = "database/weka/data/num_100.type_" + StaticVars.reputationType + ".dangerous_category.unsupervised.arff";
                    Instances trainingDangerousKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingDangerousKmeans);
                    trainingDangerousKmeans.setClassIndex(trainingDangerousKmeans.numAttributes() - 1);
                    String pathClustererDangerousKmeans = "database/weka/model/num_100.type_" + StaticVars.reputationType + ".dangerousityKmeans.model";
                    Clusterer KmeansDangerousClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererDangerousKmeans);

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
                                labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous);
                            }
                        }
                        compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, trainingDangerousKmeans).get((int) clusterNumber);

                        // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                        convertedFeatureDangerous.instance(0).setClassValue(classValueClusterDangerous);
                        trainingDangerousKmeans.add(convertedFeatureDangerous.instance(0));
                        EksternalFile.saveInstanceWekaToExternalARFF(trainingDangerousKmeans,pathTrainingDangerousKmeans);
                        // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                        try {
                            Clusterer updatedClustererDangerousity = sitesClusterer.buildKmeansReputationModel(trainingDangerousKmeans,KmeansDangerousClusterer.numberOfClusters());
                            EksternalFile.saveClustererToExternalModel(updatedClustererDangerousity,pathClustererDangerousKmeans);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                convertedFeature.instance(0).setClassValue(classValueCluster);
                trainingNormalKmeans.add(convertedFeature.instance(0));
                EksternalFile.saveInstanceWekaToExternalARFF(trainingNormalKmeans,pathTrainingNormalKmeans);
                // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                try {
                    Clusterer updatedClustererNormality = sitesClusterer.buildKmeansReputationModel(trainingNormalKmeans,KmeansNormalClusterer.numberOfClusters());
                    EksternalFile.saveClustererToExternalModel(updatedClustererNormality,pathClustererNormalKmeans);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (hybridRadioButton.isSelected()) {
                // Convert extracted feature into instance weka
                SitesClusterer sitesClusterer = new SitesClusterer(StaticVars.reputationType);
                sitesClusterer.configARFFInstance(new String[]{"normal", "abnormal"});
                sitesClusterer.fillDataIntoInstanceRecord(thisDomainNameFeatures, "normal");
                Instances convertedFeature = sitesClusterer.getSiteReputationRecord();

                // STAGE 1 (Classification SVM)
                String pathClassifier1 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalitySVM.model";
                Classifier optimumSupervisedClassifier1 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);
                double classValueClassified = 0;
                try {
                    classValueClassified = optimumSupervisedClassifier1.classifyInstance(convertedFeature.instance(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                convertedFeature.instance(0).setClassValue(classValueClassified);

                // STAGE 1 (Clustering KMeans Normality)
                String pathTrainingNormalKmeans = "database/weka/data/num_100.type_" + StaticVars.reputationType + ".normality_category.unsupervised.arff";
                Instances trainingNormalKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingNormalKmeans);
                trainingNormalKmeans.setClassIndex(trainingNormalKmeans.numAttributes() - 1);
                String pathClustererNormalKmeans = "database/weka/model/num_100.type_" + StaticVars.reputationType + ".normalityKmeans.model";
                Clusterer KmeansNormalClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererNormalKmeans);

                trainingNormalKmeans.add(convertedFeature.instance(0));
                ClusterEvaluation evalNormalKmeans = sitesClusterer.evaluateClusterReputationModel(trainingNormalKmeans, KmeansNormalClusterer);
                double[] clusterAssigment1 = evalNormalKmeans.getClusterAssignments();
                int[] classesToCluster1 = evalNormalKmeans.getClassesToClusters();
                String labelNormality = "";
                for (int i = 0; i < trainingNormalKmeans.numInstances(); i++) {
                    if (trainingNormalKmeans.instance(i).toString().equals(convertedFeature.instance(0).toString())) {
                        double clusterNumber = clusterAssigment1[i];
                        int classValueCluster = classesToCluster1[(int) clusterNumber];
                        labelNormality = trainingNormalKmeans.classAttribute().value(classValueCluster);
                    }
                }

                if (labelNormality.equals("normal")) {
                    labelDomainNameResult = "normal";
                } else {
                    // STAGE II (Classification kNN)
                    Instances dangerousConvertedFeature = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);
                    String pathClassifier2 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKNN_10.model";
                    Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);
                    double classValueDangerousity = 0;
                    try {
                        classValueDangerousity = optimumSupervisedClassifier2.classifyInstance(dangerousConvertedFeature.instance(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dangerousConvertedFeature.instance(0).setClassValue(classValueDangerousity);

                    // STAGE II (Clustering Kmeans Dangerousity)
                    String pathTrainingDangerousKmeans = "database/weka/data/num_100.type_" + StaticVars.reputationType + ".dangerous_category.unsupervised.arff";
                    Instances trainingDangerousKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingDangerousKmeans);
                    trainingDangerousKmeans.setClassIndex(trainingDangerousKmeans.numAttributes() - 1);
                    String pathClustererDangerousKmeans = "database/weka/model/num_100.type_" + StaticVars.reputationType + ".dangerousityKmeans.model";
                    Clusterer KmeansDangerousClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererDangerousKmeans);

                    trainingDangerousKmeans.add(dangerousConvertedFeature.instance(0));
                    try {
                        ClusterEvaluation evalDangerousKmeans = sitesClusterer.evaluateClusterReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer);
                        double[] clusterAssignment2 = evalDangerousKmeans.getClusterAssignments();
                        int[] classesToCluster2 = evalDangerousKmeans.getClassesToClusters();
                        double clusterNumber = 0;
                        for (int i = 0; i < trainingDangerousKmeans.numInstances(); i++) {
                            if (trainingDangerousKmeans.instance(i).toString().equals(dangerousConvertedFeature.instance(0).toString())) {
                                clusterNumber = clusterAssignment2[i];
                                int classValueCluster = classesToCluster2[(int) clusterNumber];
                                labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueCluster);
                            }
                        }
                        compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, trainingDangerousKmeans).get((int) clusterNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                if (StaticVars.currentLabel.equals("normal")) {
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
