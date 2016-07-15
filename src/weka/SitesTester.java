package weka;

import Utils.Database.EksternalFile;
import data_structure.instance_ML.SiteRecordReputation;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Enumeration;
import java.util.List;
import java.util.Random;

/**
 * Created by Dukun GaIB on 6/9/2016.
 */
public class SitesTester {
    public static void main(String[] args) {
        int typeReputation = 5;
        SitesLabeler labeledSiteDangerous = new SitesLabeler(typeReputation);
        labeledSiteDangerous.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
        SitesLabeler labeledSiteNormal = new SitesLabeler(typeReputation);
        labeledSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
        SitesClusterer clusteredSiteNormal = new SitesClusterer(typeReputation);
        clusteredSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
        System.out.println("Config ARFF Done");

        // TESTING VARIABLE CONTROL
        int optimumNumTrainingNormalType2 = 100;
        String pathInstancesNormalControl = "database/weka/test/numsites_100.type_" + typeReputation + ".normal.testdata.arff";
        Instances instancesNormalControl = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesNormalControl);
        instancesNormalControl.setClassIndex(instancesNormalControl.numAttributes()-1);
        String pathClassifierNormalControl = "database/weka/model/num_" + optimumNumTrainingNormalType2 + ".type_" + typeReputation + ".normalitySVM.model";
        Classifier supervisedClassifierNormalControl = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierNormalControl);

        try {
            Evaluation evalNormalType2 = new Evaluation(instancesNormalControl);
            evalNormalType2.crossValidateModel(supervisedClassifierNormalControl,instancesNormalControl,10,new Random(1));
            System.out.println("Correctly Classified Instances Type 2 Stage 1 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalNormalType2,instancesNormalControl));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int optimumNumTrainingDangerousType2 = 400;
        int optimumKNNType2 = 7;
        String pathInstancesDangerousControl2 = "database/weka/test/numsites_100.type_" + typeReputation + ".dangerous.testdata.arff";
        Instances instancesDangerousControl2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesDangerousControl2);
        instancesDangerousControl2.setClassIndex(instancesDangerousControl2.numAttributes()-1);
        String pathClassifierDangerousControl2 = "database/weka/model/num_" + optimumNumTrainingDangerousType2 + ".type_" + typeReputation + ".dangerousityKNN_" + optimumKNNType2 + ".model";
        Classifier supervisedClassifierDangerousControl2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierDangerousControl2);

        try {
            Evaluation evalDangerousType2 = new Evaluation(instancesDangerousControl2);
            evalDangerousType2.crossValidateModel(supervisedClassifierDangerousControl2,instancesDangerousControl2,10,new Random(1));
            System.out.println("Correctly Classified Instances Type 2 Stage 2 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalDangerousType2,instancesDangerousControl2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TESTING VARIABLE BEBAS STAGE I
        int optimumTrainingSVMType2 = 300;
        String pathInstancesNormalType2 = "database/weka/test/numsites_100.type_" + typeReputation + ".normal.testdata.arff";
        Instances instancesNormalType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesNormalType2);
        instancesNormalType2.setClassIndex(instancesNormalType2.numAttributes()-1);
        String pathClassifierNormalType2 = "database/weka/model/num_" + optimumTrainingSVMType2 + ".type_" + typeReputation + ".normalitySVM.hybrid.model";
        Classifier supervisedClassifierNormalType2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierNormalType2);

        // Classification (SVM)
        FastVector instancesAttributesNormality = SitesMLProcessor.getAttributesVector(instancesNormalType2);
        Instances classifiedNormalityInstances = new Instances("normal_sites_supervised", instancesAttributesNormality, 0);
        classifiedNormalityInstances.setClassIndex(classifiedNormalityInstances.numAttributes() - 1);

        Enumeration normalityInstances = instancesNormalType2.enumerateInstances();
        while (normalityInstances.hasMoreElements()) {
            Instance thisInstanceNormality = (Instance) normalityInstances.nextElement();
            try {
                double classValue = supervisedClassifierNormalType2.classifyInstance(thisInstanceNormality);
                thisInstanceNormality.setClassValue(classValue);
                classifiedNormalityInstances.add(thisInstanceNormality);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clustering (EM)
        int optimumClusterEMStageI = 2;
        Clusterer clusterNormalityEMStageI = clusteredSiteNormal.buildEMReputationModel(classifiedNormalityInstances,optimumClusterEMStageI);
        ClusterEvaluation clusterNormalityEval = clusteredSiteNormal.evaluateClusterReputationModel(instancesNormalType2,clusterNormalityEMStageI);
        System.out.println("AKURASI STAGE I (EM) HYBRID TYPE 5 : " + SitesClusterer.getIncorrectlyClassifiedInstance(clusterNormalityEval,instancesNormalType2));

        // TESTING VARIABLE BEBAS STAGE II
        int optimumTrainingKNN = 400;
        int optimumNumKNN = 9;
        String pathInstancesDangerousType2 = "database/weka/test/numsites_100.type_" + typeReputation + ".dangerous.testdata.arff";
        Instances instancesDangerousType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesDangerousType2);
        instancesDangerousType2.setClassIndex(instancesDangerousType2.numAttributes()-1);
        String pathClassifierDangerousType2 = "database/weka/model/num_" + optimumTrainingKNN + ".type_" + typeReputation + ".dangerousityKNN_" + optimumNumKNN + ".model";
        Classifier supervisedClassifierDangerousType2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierDangerousType2);

        try {
            Evaluation evalDangerousType2 = new Evaluation(instancesDangerousType2);
            evalDangerousType2.crossValidateModel(supervisedClassifierDangerousType2,instancesDangerousType2,10,new Random(1));
            System.out.println("Correctly Classified Instances Type 7 Stage 2 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalDangerousType2,instancesDangerousType2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CREATE STATIC DATA BY REMOVING UNUSED ATTRIBUTES
//        Instances instancesNormalType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_7.normal.staticdata.arff");
//        instancesNormalType4.setClassIndex(instancesNormalType4.numAttributes()-1);
//        Instances instancesDangerousType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_7.dangerous.staticdata.arff");
//        instancesDangerousType4.setClassIndex(instancesDangerousType4.numAttributes()-1);
//
//        for (int i=0;i<19;i++) {
//            instancesNormalType4.deleteAttributeAt(instancesNormalType4.numAttributes()-2);
//            instancesDangerousType4.deleteAttributeAt(instancesDangerousType4.numAttributes()-2);
//        }
//
//        EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalType4,"database/weka/data_static/normal3.arff");
//        EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousType4,"database/weka/data_static/dangerous3.arff");
    }
}
