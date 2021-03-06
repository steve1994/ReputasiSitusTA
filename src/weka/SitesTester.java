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
        int typeReputation = 3;
        SitesLabeler labeledSiteDangerous = new SitesLabeler(typeReputation);
        labeledSiteDangerous.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
        SitesLabeler labeledSiteNormal = new SitesLabeler(typeReputation);
        labeledSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
        SitesClusterer clusteredSiteNormal = new SitesClusterer(typeReputation);
        clusteredSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
        System.out.println("Config ARFF Done");

        // CREATE TESTING DATA 
//        for (int l=1;l<=7;l++) {
//            int type = l;
//            int numSitesEachType = 100;
//            for (int k = 0; k < 4; k++) {     // Phishing, Malware, Spamming, Normal
//                List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
//                int border = listSites.size() - numSitesEachType;
//                for (int i = (listSites.size() - 1); i >= border; i--) {
//                    // SET RECORD INSTANCE DATA STRUCTURE
//                    SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i),type);
//
//                    if (k < 3) {
//                        String classLabel = "";
//                        switch (k) {
//                            default:
//                            case 0:
//                                classLabel = "malware";
//                                break;
//                            case 1:
//                                classLabel = "phishing";
//                                break;
//                            case 2:
//                                classLabel = "spamming";
//                                break;
//                        }
//                        labeledSiteDangerous.fillDataIntoInstanceRecord(recordML, classLabel);
//                    }
//                    String classLabel2 = "";
//                    switch (k) {
//                        case 0:
//                        case 1:
//                        case 2:
//                            classLabel2 = "abnormal";
//                            break;
//                        default:
//                        case 3:
//                            classLabel2 = "normal";
//                            break;
//                    }
//                    labeledSiteNormal.fillDataIntoInstanceRecord(recordML, classLabel2);
//                    System.out.println("Situs ke-" + (i + 1));
//                }
//            }
//
//            Instances instancesNormalThisType = labeledSiteNormal.getSiteReputationRecord();
//            String fileNameStaticNormal = "numsites_" + numSitesEachType + ".type_" + type + ".normal.testdata.arff";
//            String pathNameStaticNormal = "database/weka/test/" + fileNameStaticNormal;
//            EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalThisType, pathNameStaticNormal);
//
//            Instances instancesDangerousThisType = labeledSiteDangerous.getSiteReputationRecord();
//            String fileNameStaticDangerous = "numsites_" + numSitesEachType + ".type_" + type + ".dangerous.testdata.arff";
//            String pathNameStaticDangerous = "database/weka/test/" + fileNameStaticDangerous;
//            EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousThisType, pathNameStaticDangerous);
//        }

        // TESTING VARIABLE CONTROL
//        int optimumNumTrainingNormalType2 = 100;
//        String pathInstancesNormalControl = "database/weka/test/numsites_100.type_" + typeReputation + ".normal.testdata.arff";
//        Instances instancesNormalControl = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesNormalControl);
//        instancesNormalControl.setClassIndex(instancesNormalControl.numAttributes()-1);
//        String pathClassifierNormalControl = "database/weka/model/num_" + optimumNumTrainingNormalType2 + ".type_" + typeReputation + ".normalitySVM.model";
//        Classifier supervisedClassifierNormalControl = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierNormalControl);
//
//        try {
//            Evaluation evalNormalType2 = new Evaluation(instancesNormalControl);
//            evalNormalType2.crossValidateModel(supervisedClassifierNormalControl,instancesNormalControl,10,new Random(1));
//            System.out.println("Correctly Classified Instances Type 2 Stage 1 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalNormalType2,instancesNormalControl));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        int optimumNumTrainingDangerousType2 = 400;
//        int optimumKNNType2 = 7;
//        String pathInstancesDangerousControl2 = "database/weka/test/numsites_100.type_" + typeReputation + ".dangerous.testdata.arff";
//        Instances instancesDangerousControl2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesDangerousControl2);
//        instancesDangerousControl2.setClassIndex(instancesDangerousControl2.numAttributes()-1);
//        String pathClassifierDangerousControl2 = "database/weka/model/num_" + optimumNumTrainingDangerousType2 + ".type_" + typeReputation + ".dangerousityKNN_" + optimumKNNType2 + ".model";
//        Classifier supervisedClassifierDangerousControl2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierDangerousControl2);
//
//        try {
//            Evaluation evalDangerousType2 = new Evaluation(instancesDangerousControl2);
//            evalDangerousType2.crossValidateModel(supervisedClassifierDangerousControl2,instancesDangerousControl2,10,new Random(1));
//            System.out.println("Correctly Classified Instances Type 2 Stage 2 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalDangerousType2,instancesDangerousControl2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        // TESTING VARIABLE BEBAS STAGE I
        for (int numTest=100;numTest<=500;numTest=numTest+100) {
            int optimumTrainingSVMType2 = 100;
            String pathInstancesNormalType2 = "database/weka/test/numsites_" + numTest + ".type_" + typeReputation + ".normal.testdata.arff";
//            String pathInstancesNormalType2 = "database/weka/test/numsites_" + numTest + ".type_" + typeReputation + ".dangerous.testdata.arff";
            Instances instancesNormalType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesNormalType2);
            instancesNormalType2.setClassIndex(instancesNormalType2.numAttributes() - 1);
            int numOptimumKNN = 3;
            String pathClassifierNormalType2 = "database/weka/model/num_" + optimumTrainingSVMType2 + ".type_" + typeReputation + ".normalitySVM.hybrid.model";
//            String pathClassifierNormalType2 = "database/weka/model/num_" + optimumTrainingSVMType2 + ".type_" + typeReputation + ".dangerousityKNN.hybrid.model";
//            String pathClassifierNormalType2 = "database/weka/model/num_" + optimumTrainingSVMType2 + ".type_" + typeReputation + ".dangerousityKNN_" + numOptimumKNN + ".model";
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
            Clusterer clusterNormalityEMStageI = clusteredSiteNormal.buildEMReputationModel(classifiedNormalityInstances, optimumClusterEMStageI);
            ClusterEvaluation clusterNormalityEval = clusteredSiteNormal.evaluateClusterReputationModel(instancesNormalType2, clusterNormalityEMStageI);
            System.out.println("AKURASI STAGE I WITH NUM TESTING " + numTest + " : " + SitesClusterer.getIncorrectlyClassifiedInstance(clusterNormalityEval, instancesNormalType2));
        }

//        // TESTING VARIABLE BEBAS STAGE II
//        int optimumTrainingKNN = 400;
//        int optimumNumKNN = 9;
//        String pathInstancesDangerousType2 = "database/weka/test/numsites_100.type_" + typeReputation + ".dangerous.testdata.arff";
//        Instances instancesDangerousType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesDangerousType2);
//        instancesDangerousType2.setClassIndex(instancesDangerousType2.numAttributes()-1);
//        String pathClassifierDangerousType2 = "database/weka/model/num_" + optimumTrainingKNN + ".type_" + typeReputation + ".dangerousityKNN_" + optimumNumKNN + ".model";
//        Classifier supervisedClassifierDangerousType2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierDangerousType2);
//
//        try {
//            Evaluation evalDangerousType2 = new Evaluation(instancesDangerousType2);
//            evalDangerousType2.crossValidateModel(supervisedClassifierDangerousType2,instancesDangerousType2,10,new Random(1));
//            System.out.println("Correctly Classified Instances Type 7 Stage 2 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalDangerousType2,instancesDangerousType2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // CREATE STATIC DATA BY REMOVING UNUSED ATTRIBUTES
//        Instances instancesNormalType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_7.normal.staticdata.arff");
        Instances instancesNormalType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/test/normal" + typeReputation + "_500.arff");
        instancesNormalType4.setClassIndex(instancesNormalType4.numAttributes()-1);
//        Instances instancesDangerousType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_7.dangerous.staticdata.arff");
        Instances instancesDangerousType4 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/test/dangerous" + typeReputation + "_500.arff");
        instancesDangerousType4.setClassIndex(instancesDangerousType4.numAttributes()-1);

//        for (int i=0;i<19;i++) {
////            instancesNormalType4.deleteAttributeAt(instancesNormalType4.numAttributes()-2);
////            instancesDangerousType4.deleteAttributeAt(instancesDangerousType4.numAttributes()-2);
//            instancesNormalType4.deleteAttributeAt(0);
//            instancesDangerousType4.deleteAttributeAt(0);
//        }

////        EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalType4,"database/weka/data_static/normal3.arff");
//        EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalType4,"database/weka/test/normal3_500.arff");
////        EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousType4,"database/weka/data_static/dangerous3.arff");
//        EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousType4,"database/weka/test/dangerous3_500.arff");

        // SPLIT TEST DATA ACCORDING TO NUM TRAINING 100-400 EACH TYPE
        // Extract attributes from allInstancesRecordSite (malware / phishing / spamming / normal)
        FastVector instancesAttributesNormality = clusteredSiteNormal.getAttributesVector(instancesNormalType4);
        FastVector instancesAttributesDangerousity = clusteredSiteNormal.getAttributesVector(instancesDangerousType4);

        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
        Instances normalInstances = new Instances("normal_instances", instancesAttributesNormality, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributesNormality, 0);
        for (int i = 0; i < instancesNormalType4.numInstances(); i++) {
            int indexClassThisInstance = (int) instancesNormalType4.instance(i).classValue();
            if (instancesNormalType4.classAttribute().value(indexClassThisInstance).equals("normal")) {
                normalInstances.add(instancesNormalType4.instance(i));
            } else {
                abnormalInstances.add(instancesNormalType4.instance(i));
            }
        }

        Instances malwareInstances = new Instances("malware_instances", instancesAttributesDangerousity, 0);
        Instances phishingInstances = new Instances("phishing_instances", instancesAttributesDangerousity, 0);
        Instances spammingInstances = new Instances("spamming_instances", instancesAttributesDangerousity, 0);
        for (int i = 0; i < instancesDangerousType4.numInstances(); i++) {
            int indexClassThisInstance = (int) instancesDangerousType4.instance(i).classValue();
            if (instancesDangerousType4.classAttribute().value(indexClassThisInstance).equals("malware")) {
                malwareInstances.add(instancesDangerousType4.instance(i));
            } else if (instancesDangerousType4.classAttribute().value(indexClassThisInstance).equals("phishing")) {
                phishingInstances.add(instancesDangerousType4.instance(i));
            } else if (instancesDangerousType4.classAttribute().value(indexClassThisInstance).equals("spamming")) {
                spammingInstances.add(instancesDangerousType4.instance(i));
            }
        }

        int interval = 100;
        int numSitesMaxAllocation = 500;
        for (int i=interval; i<=numSitesMaxAllocation; i=i+interval) {
            // Bentuk Training Record Secara Bertahap (normal, abnormal)
            Instances trainingRecordSitesNormality = new Instances("mixed_instances_normality", instancesAttributesNormality, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSitesNormality.add(normalInstances.instance(j));
                trainingRecordSitesNormality.add(abnormalInstances.instance(j));
            }
            trainingRecordSitesNormality.setClassIndex(trainingRecordSitesNormality.numAttributes() - 1);

            // Bentuk Training Record Secara Bertahap (normal, abnormal)
            Instances trainingRecordSitesDangerousity = new Instances("mixed_instances_dangerousity", instancesAttributesDangerousity, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSitesDangerousity.add(malwareInstances.instance(j));
                trainingRecordSitesDangerousity.add(phishingInstances.instance(j));
                trainingRecordSitesDangerousity.add(spammingInstances.instance(j));
            }
            trainingRecordSitesDangerousity.setClassIndex(trainingRecordSitesDangerousity.numAttributes() - 1);

            // Save both training data (normality and dangerousity)
            String fileNameNormal = "numsites_" + i + ".type_" + typeReputation + ".normal.testdata.arff";
            String pathNameNormal = "database/weka/test/" + fileNameNormal;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesNormality, pathNameNormal);
            String fileNameDangerous = "numsites_" + i + ".type_" + typeReputation + ".dangerous.testdata.arff";
            String pathNameDangerous = "database/weka/test/" + fileNameDangerous;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesDangerousity, pathNameDangerous);
        }
    }
}
