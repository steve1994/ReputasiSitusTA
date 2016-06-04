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
    public Label keterangan;

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
        // Extract Features From Domain Name and Process Based on Its method
        if (!domainSitesTextField.getText().isEmpty() && !numberTrainingChoiceBox.getSelectionModel().isEmpty() && featuresRadioButton.getSelectedToggle().isSelected() && methodRadioButton.getSelectedToggle().isSelected()) {
            String domainName = Converter.getBaseHostURL(domainSitesTextField.getText());
            int numTrainingSites = (Integer) numberTrainingChoiceBox.getSelectionModel().getSelectedItem();
            int optimumKNN = 10;

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

                                // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                                String pathTrainingClassifierKNN = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.supervised.arff";
                                Instances trainingClassifierKNN = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingClassifierKNN);
                                if (trainingClassifierKNN != null) {
                                    trainingClassifierKNN.setClassIndex(trainingClassifierKNN.numAttributes() - 1);
                                    convertedFeature.instance(0).setClassValue(classValueDangerousity);
                                    trainingClassifierKNN.add(convertedFeature.instance(0));
                                    EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierKNN, pathTrainingClassifierKNN);
                                    // REBUILD CLASSIFIER KNN AGAIN AND SAVE AGAIN TO FILE
                                    Classifier updatedClassifierKNN = new IBk(optimumKNN);
                                    updatedClassifierKNN.buildClassifier(trainingClassifierKNN);
                                    EksternalFile.saveClassifierToExternalModel(updatedClassifierKNN, pathClassifier2);
                                }
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
                        trainingClassifierSVM.setClassIndex(trainingClassifierSVM.numAttributes() - 1);
                        convertedFeature.instance(0).setClassValue(classValue);
                        trainingClassifierSVM.add(convertedFeature.instance(0));
                        EksternalFile.saveInstanceWekaToExternalARFF(trainingClassifierSVM, pathTrainingClassifierSVM);
                        // REBUILD CLASSIFIER SVM AND SAVE AGAIN TO FILE
                        Classifier updatedClassifierSVM = new LibSVM();
                        try {
                            updatedClassifierSVM.buildClassifier(trainingClassifierSVM);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        EksternalFile.saveClassifierToExternalModel(updatedClassifierSVM, pathClassifier1);
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
                String pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityKmeans.model";
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
                        String pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKmeans.model";
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
                                            labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous);
                                        } else {
                                            labelDomainNameResult = "malicious (unknown type)";
                                        }
                                    }
                                }
                                compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, trainingDangerousKmeans).get((int) clusterNumber);

                                if (!labelDomainNameResult.equals("malicious (unknown type)")) {
                                    // UPDATE CURRENT TRAINING INSTANCES (DANGEROUSITY)
                                    trainingDangerousKmeans.delete(trainingDangerousKmeans.numInstances() - 1);
                                    convertedFeatureDangerous.instance(0).setClassValue(classValueClusterDangerous);
                                    trainingDangerousKmeans.add(convertedFeatureDangerous.instance(0));
                                    EksternalFile.saveInstanceWekaToExternalARFF(trainingDangerousKmeans, pathTrainingDangerousKmeans);
                                    // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                                    try {
                                        Clusterer updatedClustererDangerousity = sitesClusterer.buildKmeansReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer.numberOfClusters());
                                        EksternalFile.saveClustererToExternalModel(updatedClustererDangerousity, pathClustererDangerousKmeans);
                                    } catch (Exception e) {
                                        e.printStackTrace();
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
                        // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                        trainingNormalKmeans.delete(trainingNormalKmeans.numInstances() - 1);
                        convertedFeature.instance(0).setClassValue(classValueCluster);
                        trainingNormalKmeans.add(convertedFeature.instance(0));
                        EksternalFile.saveInstanceWekaToExternalARFF(trainingNormalKmeans, pathTrainingNormalKmeans);
                        // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                        try {
                            Clusterer updatedClustererNormality = sitesClusterer.buildKmeansReputationModel(trainingNormalKmeans, KmeansNormalClusterer.numberOfClusters());
                            EksternalFile.saveClustererToExternalModel(updatedClustererNormality, pathClustererNormalKmeans);
                        } catch (Exception e) {
                            e.printStackTrace();
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

                // STAGE 1 (Classification SVM)
                String pathClassifier1 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalitySVM.hybrid.model";
                Classifier optimumSupervisedClassifier1 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);

                if (optimumSupervisedClassifier1 != null) {
                    double classValueClassified = 0;
                    try {
                        classValueClassified = optimumSupervisedClassifier1.classifyInstance(convertedFeature.instance(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    convertedFeature.instance(0).setClassValue(classValueClassified);

                    // STAGE 1 (Clustering KMeans Normality)
                    String pathTrainingNormalKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normality_category.hybrid.arff";
                    Instances trainingNormalKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingNormalKmeans);
                    String pathClustererNormalKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".normalityKmeansStageI.hybrid.model";
                    Clusterer KmeansNormalClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererNormalKmeans);

                    if (trainingNormalKmeans != null && KmeansNormalClusterer != null) {
                        trainingNormalKmeans.setClassIndex(trainingNormalKmeans.numAttributes() - 1);
                        trainingNormalKmeans.add(convertedFeature.instance(0));
                        ClusterEvaluation evalNormalKmeans = sitesClusterer.evaluateClusterReputationModel(trainingNormalKmeans, KmeansNormalClusterer);
                        double[] clusterAssigment1 = evalNormalKmeans.getClusterAssignments();
                        int[] classesToCluster1 = evalNormalKmeans.getClassesToClusters();
                        String labelNormality = "";
                        int classValueCluster = 0;
                        for (int i = 0; i < trainingNormalKmeans.numInstances(); i++) {
                            if (trainingNormalKmeans.instance(i).toString().equals(convertedFeature.instance(0).toString())) {
                                double clusterNumber = clusterAssigment1[i];
                                classValueCluster = classesToCluster1[(int) clusterNumber];
                                if (classValueCluster != -1) {
                                    labelNormality = trainingNormalKmeans.classAttribute().value(classValueCluster);
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
                            // STAGE II (Classification kNN)
                            Instances dangerousConvertedFeature = SitesMLProcessor.convertNormalityToDangerousityLabel(convertedFeature);
                            String pathClassifier2 = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKNN.hybrid.model";
                            Classifier optimumSupervisedClassifier2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier2);

                            if (optimumSupervisedClassifier2 != null) {
                                double classValueDangerousity = 0;
                                try {
                                    classValueDangerousity = optimumSupervisedClassifier2.classifyInstance(dangerousConvertedFeature.instance(0));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                dangerousConvertedFeature.instance(0).setClassValue(classValueDangerousity);

                                // STAGE II (Clustering Kmeans Dangerousity)
                                String pathTrainingDangerousKmeans = "database/weka/data/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerous_category.hybrid.arff";
                                Instances trainingDangerousKmeans = EksternalFile.loadInstanceWekaFromExternalARFF(pathTrainingDangerousKmeans);
                                String pathClustererDangerousKmeans = "database/weka/model/num_" + numTrainingSites + ".type_" + StaticVars.reputationType + ".dangerousityKmeansStageII.hybrid.model";
                                Clusterer KmeansDangerousClusterer = EksternalFile.loadClustererWekaFromEksternalModel(pathClustererDangerousKmeans);

                                if (trainingDangerousKmeans != null && KmeansDangerousClusterer != null) {
                                    trainingDangerousKmeans.setClassIndex(trainingDangerousKmeans.numAttributes() - 1);
                                    trainingDangerousKmeans.add(dangerousConvertedFeature.instance(0));
                                    try {
                                        ClusterEvaluation evalDangerousKmeans = sitesClusterer.evaluateClusterReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer);
                                        double[] clusterAssignment2 = evalDangerousKmeans.getClusterAssignments();
                                        int[] classesToCluster2 = evalDangerousKmeans.getClassesToClusters();
                                        double clusterNumber = 0;
                                        int classValueClusterDangerous = 0;
                                        for (int i = 0; i < trainingDangerousKmeans.numInstances(); i++) {
                                            if (trainingDangerousKmeans.instance(i).toString().equals(dangerousConvertedFeature.instance(0).toString())) {
                                                clusterNumber = clusterAssignment2[i];
                                                classValueClusterDangerous = classesToCluster2[(int) clusterNumber];
                                                if (classValueClusterDangerous != -1) {
                                                    labelDomainNameResult = trainingDangerousKmeans.classAttribute().value(classValueClusterDangerous);
                                                } else {
                                                    labelDomainNameResult = "malicious (unknown type)";
                                                }
                                            }
                                        }
                                        compositionDangerousity = SitesHybrid.getClusterPercentageDangerousity(evalDangerousKmeans, trainingDangerousKmeans).get((int) clusterNumber);

                                        if (!labelDomainNameResult.equals("malicious (unknown type)")) {
                                            // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                                            trainingDangerousKmeans.delete(trainingDangerousKmeans.numInstances() - 1);
                                            dangerousConvertedFeature.instance(0).setClassValue(classValueClusterDangerous);
                                            trainingDangerousKmeans.add(dangerousConvertedFeature.instance(0));
                                            EksternalFile.saveInstanceWekaToExternalARFF(trainingDangerousKmeans, pathTrainingDangerousKmeans);
                                            // REBUILD CLASSIFIER AND SAVE TO EKSTERNAL FILE
                                            Classifier updatedClassifierKNN = new IBk(optimumKNN);
                                            try {
                                                updatedClassifierKNN.buildClassifier(trainingDangerousKmeans);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            EksternalFile.saveClassifierToExternalModel(updatedClassifierKNN, pathClassifier2);
                                            // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                                            try {
                                                Clusterer updatedClustererDangerousity = sitesClusterer.buildKmeansReputationModel(trainingDangerousKmeans, KmeansDangerousClusterer.numberOfClusters());
                                                EksternalFile.saveClustererToExternalModel(updatedClustererDangerousity, pathClustererDangerousKmeans);
                                            } catch (Exception e) {
                                                e.printStackTrace();
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
                            // UPDATE CURRENT TRAINING INSTANCES (NORMALITY)
                            trainingNormalKmeans.delete(trainingNormalKmeans.numInstances() - 1);
                            convertedFeature.instance(0).setClassValue(classValueCluster);
                            trainingNormalKmeans.add(convertedFeature.instance(0));
                            EksternalFile.saveInstanceWekaToExternalARFF(trainingNormalKmeans, pathTrainingNormalKmeans);
                            // REBUILD CLASSIFIER AND SAVE TO EKSTERNAL FILE
                            Classifier updatedClassifierSVM = new LibSVM();
                            try {
                                updatedClassifierSVM.buildClassifier(trainingNormalKmeans);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            EksternalFile.saveClassifierToExternalModel(updatedClassifierSVM, pathClassifier1);
                            // REBUILD CLUSTERER AND SAVE TO EKSTERNAL FILE
                            try {
                                Clusterer updatedClustererNormality = sitesClusterer.buildKmeansReputationModel(trainingNormalKmeans, KmeansNormalClusterer.numberOfClusters());
                                EksternalFile.saveClustererToExternalModel(updatedClustererNormality, pathClustererNormalKmeans);
                            } catch (Exception e) {
                                e.printStackTrace();
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
