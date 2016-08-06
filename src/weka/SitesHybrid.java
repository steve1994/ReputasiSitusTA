package weka;

import Utils.API.WOT_API_Loader;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by steve on 17/05/2016.
 */
public class SitesHybrid {
    /**
     * Get composition of each cluster for dangerousity
     * @param evalCluster
     * @return
     */
    public static List<Triplet<Double,Double,Double>> getClusterPercentageDangerousity(ClusterEvaluation evalCluster, Instances testSetDangerous) {
        List<Triplet<Double, Double, Double>> compositionEachCluster = new ArrayList<Triplet<Double, Double, Double>>();
        try {
            // Create data structure containing instances each cluster
            List<Instances> listInstancesPerCluster = new ArrayList<Instances>();
            for (int j = 0; j < evalCluster.getNumClusters(); j++) {
                Instances thisClusterInstances = new Instances("dangerous_type_sites_supervised", SitesClusterer.getAttributesVector(testSetDangerous), 0);
                thisClusterInstances.setClassIndex(thisClusterInstances.numAttributes() - 1);
                listInstancesPerCluster.add(j, thisClusterInstances);
            }
            // Fill data structure above with corresponding instance for each cluster
            for (int j = 0; j < evalCluster.getClusterAssignments().length; j++) {
                int clusterNumber = (int) evalCluster.getClusterAssignments()[j];
                listInstancesPerCluster.get(clusterNumber).add(testSetDangerous.instance(j));
            }
            // Calculate malware / phishing / spamming composition for each cluster
            for (int j = 0; j < evalCluster.getNumClusters(); j++) {
                int counterMalwareThisCluster = 0, counterPhishingThisCluster = 0, counterSpammingThisCluster = 0;
                Instances instancesThisCluster = listInstancesPerCluster.get(j);
                for (int k = 0; k < instancesThisCluster.numInstances(); k++) {
                    int classValueThisInstance = (int) instancesThisCluster.instance(k).classValue();
                    if (classValueThisInstance == 0) {          // Malware
                        counterMalwareThisCluster++;
                    } else if (classValueThisInstance == 1) {   // Phishing
                        counterPhishingThisCluster++;
                    } else if (classValueThisInstance == 2) {   // Spamming
                        counterSpammingThisCluster++;
                    }
                }
                Double malwareComposition = (double) counterMalwareThisCluster / (double) instancesThisCluster.numInstances();
                Double phishingComposition = (double) counterPhishingThisCluster / (double) instancesThisCluster.numInstances();
                Double spammingComposition = (double) counterSpammingThisCluster / (double) instancesThisCluster.numInstances();
                Triplet<Double, Double, Double> compositionThisCluster = new Triplet<Double, Double, Double>(malwareComposition, phishingComposition, spammingComposition);
                compositionEachCluster.add(j, compositionThisCluster);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return compositionEachCluster;
    }

    /**
     * Get incorrectly classified instances given the instances and clusterer
     * @param allInstances
     * @param clusterer (build with classifiedAllInstances outside this function)
     * @return
     */
    public static Pair<Instances,Instances> distinguishingCorrectIncorrectInstances(Instances allInstances, Clusterer clusterer) {
        Instances correctlyClassifyInstance = new Instances("correct_instances",SitesMLProcessor.getAttributesVector(allInstances),0);
        Instances incorrectlyClassifyInstance = new Instances("incorrect_instances",SitesMLProcessor.getAttributesVector(allInstances),0);
        allInstances.setClassIndex(allInstances.numAttributes()-1);

        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        try {
            clusterEvaluation.evaluateClusterer(allInstances);
            double[] clusterEachInstance = clusterEvaluation.getClusterAssignments();
            int[] classesToCluster = clusterEvaluation.getClassesToClusters();
            for (int i=0;i<allInstances.numInstances();i++) {
                Instance instance = allInstances.instance(i);
                int actualClassValue = (int) instance.classValue();
                int predictedClassValue = classesToCluster[(int) clusterEachInstance[i]];
                if (predictedClassValue == actualClassValue) {
                    correctlyClassifyInstance.add(instance);
                } else {
                    incorrectlyClassifyInstance.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Pair<Instances,Instances>(correctlyClassifyInstance,incorrectlyClassifyInstance);
    }

    public static void main(String[] args) {
        int typeReputation = 7;

        // Sites Clusterer Consist Of 2 stages (normality and then dangerousity)
        SitesClusterer clusterSiteNormality = new SitesClusterer(typeReputation);
        clusterSiteNormality.configARFFInstance(new String[]{"normal","abnormal"});
        SitesClusterer clusterSiteDangerousity = new SitesClusterer(typeReputation);
        clusterSiteDangerousity.configARFFInstance(new String[]{"malware","phishing","spamming"});

        // Sites Labeler Consist Of 2 stages (normality and (then) dangerousity (if abnormal))
        SitesLabeler labelSiteNormality = new SitesLabeler(typeReputation);
        labelSiteNormality.configARFFInstance(new String[]{"normal", "abnormal"});
        SitesLabeler labelSiteDangerousity = new SitesLabeler(typeReputation);
        labelSiteDangerousity.configARFFInstance(new String[]{"malware", "phishing", "spamming"});

        System.out.println("Config ARFF Done");

        // TESTING DISTINGUISH INSTANCE RECORD
//        Instances instances = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data/num_800.type_" + typeReputation + ".normality_category.hybrid.arff");
//        Classifier classifier = EksternalFile.loadClassifierWekaFromEksternalModel("database/weka/model/num_100.type_" + typeReputation + ".normalitySVM.hybrid.model");
//
//        // Classification
//        Instances classifiedAllInstances = new Instances("classified_all_instances",SitesMLProcessor.getAttributesVector(instances),0);
//        classifiedAllInstances.setClassIndex(classifiedAllInstances.numAttributes()-1);
//        for (int i=0;i<instances.numInstances();i++) {
//            Instance instance = instances.instance(i);
//            try {
//                double predictedClassValue = classifier.classifyInstance(instance);
//                instance.setClassValue(predictedClassValue);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            classifiedAllInstances.add(instance);
//        }
//
//        // Clustering
//        int typeClusterAlgorithm = 1;
//        int optimumNumberCluster = 2;
//        Clusterer clusterer;
//        if (typeClusterAlgorithm == 1) {            // KMeans
//            clusterer = clusterSiteNormality.buildKmeansReputationModel(classifiedAllInstances,optimumNumberCluster);
//        } else if (typeClusterAlgorithm == 2) {     // EM
//            clusterer = clusterSiteNormality.buildEMReputationModel(classifiedAllInstances,optimumNumberCluster);
//        } else {                                    // HC
//            clusterer = clusterSiteNormality.buildHCReputationModel(classifiedAllInstances,optimumNumberCluster);
//        }
//
//        System.out.println("TOTAL: " + instances.numInstances());
//        Pair<Instances,Instances> distinguishResult = clusterSiteNormality.distinguishingCorrectIncorrectInstances(instances,clusterer);
//        System.out.println("Correct: " + distinguishResult.getValue0().numInstances());
//        System.out.println("Incorrect: " + distinguishResult.getValue1().numInstances());

        int numSitesMaxAllocation = 1000;
//        for (int k=0;k<4;k++) {     // malware, phishing, spamming, normal
//            List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
//            for (int i = 0; i < numSitesMaxAllocation; i++) {
//                // SET RECORD INSTANCE DATA STRUCTURE
//                SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i),typeReputation);
//
//                // FILL INSTANCES INTO SITE CLUSTERER / LABELER ABOVE
//                if (k < 3) {
//                    String classLabelDangerousity = "";
//                    switch (k) {
//                        default:
//                        case 0:
//                            classLabelDangerousity = "malware";
//                            break;
//                        case 1:
//                            classLabelDangerousity = "phishing";
//                            break;
//                        case 2:
//                            classLabelDangerousity = "spamming";
//                            break;
//                    }
//                    clusterSiteDangerousity.fillDataIntoInstanceRecord(recordML,classLabelDangerousity);
//                    labelSiteDangerousity.fillDataIntoInstanceRecord(recordML,classLabelDangerousity);
//                }
//                String classLabelNormality = "";
//                switch (k) {
//                    case 0:
//                    case 1:
//                    case 2:
//                        classLabelNormality = "abnormal";
//                        break;
//                    default:
//                    case 3:
//                        classLabelNormality = "normal";
//                        break;
//                }
//                clusterSiteNormality.fillDataIntoInstanceRecord(recordML,classLabelNormality);
//                labelSiteNormality.fillDataIntoInstanceRecord(recordML,classLabelNormality);
//
//                System.out.println("Situs ke-" + (i+1));
//            }
//        }

        // Get extracted instances result from labeler / clusterer
        Instances allInstancesLabelNormality = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".normal.staticdata.arff");
        allInstancesLabelNormality.setClassIndex(allInstancesLabelNormality.numAttributes()-1);
        Instances allInstancesLabelDangerousity = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".dangerous.staticdata.arff");
        allInstancesLabelDangerousity.setClassIndex(allInstancesLabelDangerousity.numAttributes()-1);
//        Instances allInstancesLabelDangerousity = labelSiteDangerousity.getSiteReputationRecord();
//        Instances allInstancesLabelNormality = labelSiteNormality.getSiteReputationRecord();
//
        // Extracted vector attributes for Normal / Abnormal Instances
        FastVector instancesAttributesNormality = clusterSiteNormality.getAttributesVector(allInstancesLabelNormality);
        // Extracted vector attributes for Dangerous Instances (Malware / Phishing / Spamming)
        FastVector instancesAttributesDangerousity = clusterSiteDangerousity.getAttributesVector(allInstancesLabelDangerousity);

        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
        Instances normalInstances = new Instances("normal_instances", instancesAttributesNormality, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributesNormality, 0);
        for (int i = 0; i < allInstancesLabelNormality.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesLabelNormality.instance(i).classValue();
            if (allInstancesLabelNormality.classAttribute().value(indexClassThisInstance).equals("normal")) {
                normalInstances.add(allInstancesLabelNormality.instance(i));
            } else {
                abnormalInstances.add(allInstancesLabelNormality.instance(i));
            }
        }

        Instances malwareInstances = new Instances("malware_instances", instancesAttributesDangerousity, 0);
        Instances phishingInstances = new Instances("phishing_instances", instancesAttributesDangerousity, 0);
        Instances spammingInstances = new Instances("spamming_instances", instancesAttributesDangerousity, 0);
        for (int i = 0; i < allInstancesLabelDangerousity.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesLabelDangerousity.instance(i).classValue();
            if (allInstancesLabelDangerousity.classAttribute().value(indexClassThisInstance).equals("malware")) {
                malwareInstances.add(allInstancesLabelDangerousity.instance(i));
            } else if (allInstancesLabelDangerousity.classAttribute().value(indexClassThisInstance).equals("phishing")) {
                phishingInstances.add(allInstancesLabelDangerousity.instance(i));
            } else if (allInstancesLabelDangerousity.classAttribute().value(indexClassThisInstance).equals("spamming")) {
                spammingInstances.add(allInstancesLabelDangerousity.instance(i));
            }
        }

        StringBuffer statisticEvaluationReport = new StringBuffer();

        // DO EKSPERIMENT FROM 100 TO 1000 HERE !!!

        int interval = 100;
        int optimumNumClusterKMeans1 = 2;
        int optimumNumClusterKMeans2 = 4;
        int optimumNumClusterEM1 = 2;
        int optimumNumClusterEM2 = 3;
        int optimumNumClusterHC1 = 2;
        int optimumNumClusterHC2 = 3;
        int numNearestNeigbor = 1;
        for (int i=interval; i<=numSitesMaxAllocation; i=i+interval) {
            statisticEvaluationReport.append("\n\n===================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            // Bentuk Training Record Secara Bertahap (normal, abnormal)
            Instances trainingRecordSitesNormality = new Instances("mixed_instances_normality", instancesAttributesNormality, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSitesNormality.add(normalInstances.instance(j));
                trainingRecordSitesNormality.add(abnormalInstances.instance(j));
            }
            trainingRecordSitesNormality.setClassIndex(trainingRecordSitesNormality.numAttributes() - 1);

            // Bentuk Training Record Secara Bertahap (malware, phishing, spamming)
            Instances trainingRecordSitesDangerousity = new Instances("mixed_instances_dangerousity", instancesAttributesDangerousity, 0);
            int numDangerousSites = i / 3;
            for (int j = 0; j < numDangerousSites; j++) {
                trainingRecordSitesDangerousity.add(malwareInstances.instance(j));
                trainingRecordSitesDangerousity.add(phishingInstances.instance(j));
                trainingRecordSitesDangerousity.add(spammingInstances.instance(j));
            }
            trainingRecordSitesDangerousity.setClassIndex(trainingRecordSitesDangerousity.numAttributes() - 1);

            // Save both mixed instances training data (normality and dangerousity)
            String fileNameNormal = "num_" + i + ".type_" + typeReputation + ".normality_category.hybrid.arff";
            String pathNameNormal = "database/weka/data/" + fileNameNormal;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesNormality, pathNameNormal);
            String fileNameDangerous = "num_" + i + ".type_" + typeReputation + ".dangerous_category.hybrid.arff";
            String pathNameDangerous = "database/weka/data/" + fileNameDangerous;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesDangerousity, pathNameDangerous);

            // STAGE 1
            statisticEvaluationReport.append("\nSTAGE 1 : \n");

            // Classify Normality Sites First and Split Normal / Abnormal (SVM Algorithm)
            Instances classifiedNormalityInstances = new Instances("normal_sites_supervised", instancesAttributesNormality, 0);
            classifiedNormalityInstances.setClassIndex(classifiedNormalityInstances.numAttributes() - 1);
            Classifier normalClassifier = labelSiteNormality.buildLabelReputationModel(trainingRecordSitesNormality, 1, 0);

            Enumeration normalityInstances = trainingRecordSitesNormality.enumerateInstances();
            while (normalityInstances.hasMoreElements()) {
                Instance thisInstanceNormality = (Instance) normalityInstances.nextElement();
                try {
                    double classValue = normalClassifier.classifyInstance(thisInstanceNormality);
                    thisInstanceNormality.setClassValue(classValue);
                    classifiedNormalityInstances.add(thisInstanceNormality);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Save Classifier SVM (Normality) Stage I
            String nameClassifierSVM = "num_" + i + ".type_" + typeReputation + ".normalitySVM.hybrid.model";
            String pathClassifierSVM = "database/weka/model/" + nameClassifierSVM;
            EksternalFile.saveClassifierToExternalModel(normalClassifier,pathClassifierSVM);

            // Build Normal / Abnormal Cluster (Algoritma KMeans)
//            Instances hybridInstancesAbnormal = new Instances("hybrid_normality_cluster", instancesAttributesNormality, 0);
            Clusterer clusterNormality = clusterSiteNormality.buildKmeansReputationModel(classifiedNormalityInstances, optimumNumClusterKMeans1);
            ClusterEvaluation evalClusterNormality = clusterSiteNormality.evaluateClusterReputationModel(trainingRecordSitesNormality, clusterNormality);
//            try {
//                // Find cluster with label / class Abnormal
//                int numClusterAbnormal = 0;
//                for (int j = 0; j < evalClusterNormality.getClassesToClusters().length; j++) {
//                    int classValue = evalClusterNormality.getClassesToClusters()[j];
//                    if (classValue >= 0) {
//                        if (classifiedNormalityInstances.classAttribute().value(classValue).equals("abnormal")) {
//                            numClusterAbnormal = j;
//                        }
//                    }
//                }
//                // Collect instances with cluster label Abnormal
//                for (int k = 0; k < evalClusterNormality.getClusterAssignments().length; k++) {
//                    int clusterNumber = (int) evalClusterNormality.getClusterAssignments()[k];
//                    if (clusterNumber == numClusterAbnormal) {
//                        hybridInstancesAbnormal.add(classifiedNormalityInstances.instance(k));
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (KMeans) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterNormality, trainingRecordSitesNormality) + "\n");
            // Save Clusterer (Algoritma KMeans) Stage I
            String normalFileKmeansStageI = "num_" + i + ".type_" + typeReputation + ".normalityKmeansStageI.hybrid.model";
            String normalPathKmeansStageI = "database/weka/model/" + normalFileKmeansStageI;
            EksternalFile.saveClustererToExternalModel(clusterNormality, normalPathKmeansStageI);

            // Build Normal / Abnormal Cluster (Algoritma EM)
            Clusterer clusterNormality2 = clusterSiteNormality.buildEMReputationModel(classifiedNormalityInstances, optimumNumClusterEM1);
            ClusterEvaluation evalClusterNormality2 = clusterSiteNormality.evaluateClusterReputationModel(trainingRecordSitesNormality, clusterNormality2);
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (EM) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterNormality2, trainingRecordSitesNormality) + "\n");
            // Save Clusterer (Algoritma EM) Stage I
            String normalFileEMStageI = "num_" + i + ".type_" + typeReputation + ".normalityEMStageI.hybrid.model";
            String normalPathEMStageI = "database/weka/model/" + normalFileEMStageI;
            EksternalFile.saveClustererToExternalModel(clusterNormality2, normalPathEMStageI);

            // Build Normal / Abnormal Cluster (Algoritma Hierarchical)
            Clusterer clusterNormality3 = clusterSiteNormality.buildHCReputationModel(classifiedNormalityInstances, optimumNumClusterHC1);
            ClusterEvaluation evalClusterNormality3 = clusterSiteNormality.evaluateClusterReputationModel(trainingRecordSitesNormality, clusterNormality3);
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (HC) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterNormality3,trainingRecordSitesNormality) + "\n");
            // Save Clusterer (Algoritma KMeans) Stage I
            String normalFileHCStageI = "num_" + i + ".type_" + typeReputation + ".normalityHCStageI.hybrid.model";
            String normalPathHCStageI = "database/weka/model/" + normalFileHCStageI;
            EksternalFile.saveClustererToExternalModel(clusterNormality3, normalPathHCStageI);

            // STAGE 2
            statisticEvaluationReport.append("\nSTAGE 2 : \n");

            // Classify Abnormal Instances from Stage 1 Using kNN Algorithm
            Instances classifiedDangerousityInstances = new Instances("dangerous_type_sites_supervised", instancesAttributesDangerousity, 0);
            classifiedDangerousityInstances.setClassIndex(classifiedDangerousityInstances.numAttributes() - 1);
            Classifier dangerousClassifier = labelSiteDangerousity.buildLabelReputationModel(trainingRecordSitesDangerousity, 2, numNearestNeigbor);

            Enumeration dangerousityInstances = trainingRecordSitesDangerousity.enumerateInstances();
            while (dangerousityInstances.hasMoreElements()) {
                Instance thisInstanceDangerousity = (Instance) dangerousityInstances.nextElement();
                try {
                    double classValue = dangerousClassifier.classifyInstance(thisInstanceDangerousity);
                    thisInstanceDangerousity.setClassValue(classValue);
                    classifiedDangerousityInstances.add(thisInstanceDangerousity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Save Classifier SVM (Normality) Stage I
            String nameClassifierKNN = "num_" + i + ".type_" + typeReputation + ".dangerousityKNN.hybrid.model";
            String pathClassifierKNN = "database/weka/model/" + nameClassifierKNN;
            EksternalFile.saveClassifierToExternalModel(dangerousClassifier,pathClassifierKNN);

            // Build Malware / Phishing / Spamming Cluster (Abnormal Type Composition) --> Algoritma KMeans
            Clusterer clusterDangerousity = clusterSiteDangerousity.buildKmeansReputationModel(classifiedDangerousityInstances, optimumNumClusterKMeans2);
            ClusterEvaluation evalClusterDangerousity = clusterSiteDangerousity.evaluateClusterReputationModel(trainingRecordSitesDangerousity, clusterDangerousity);
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (KMeans) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterDangerousity, trainingRecordSitesDangerousity) + "\n");
            // Save Clusterer (Algoritma KMeans) Stage II
            String normalFileKmeansStageII = "num_" + i + ".type_" + typeReputation + ".dangerousityKmeansStageII.hybrid.model";
            String normalPathKmeansStageII = "database/weka/model/" + normalFileKmeansStageII;
            EksternalFile.saveClustererToExternalModel(clusterDangerousity, normalPathKmeansStageII);

            // Build Malware / Phishing / Spamming Cluster (Abnormal Type Composition) --> Algoritma EM
            Clusterer clusterDangerousity2 = clusterSiteDangerousity.buildEMReputationModel(classifiedDangerousityInstances, optimumNumClusterEM2);
            ClusterEvaluation evalClusterDangerousity2 = clusterSiteDangerousity.evaluateClusterReputationModel(trainingRecordSitesDangerousity, clusterDangerousity2);
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (EM) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterDangerousity2, trainingRecordSitesDangerousity) + "\n");
            // Save Clusterer (Algoritma EM) Stage II
            String normalFileEMStageII = "num_" + i + ".type_" + typeReputation + ".dangerousityEMStageII.hybrid.model";
            String normalPathEMStageII = "database/weka/model/" + normalFileEMStageII;
            EksternalFile.saveClustererToExternalModel(clusterDangerousity2, normalPathEMStageII);

            // Build Malware / Phishing / Spamming Cluster (Abnormal Type Composition) --> Algoritma Hierarchical
            Clusterer clusterDangerousity3 = clusterSiteDangerousity.buildHCReputationModel(classifiedDangerousityInstances, optimumNumClusterHC2);
            ClusterEvaluation evalClusterDangerousity3 = clusterSiteDangerousity.evaluateClusterReputationModel(trainingRecordSitesDangerousity, clusterDangerousity3);
            statisticEvaluationReport.append("\nIncorrectly Clustered Instance (HC) : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalClusterDangerousity3, trainingRecordSitesDangerousity) + "\n");
            // Save Clusterer (Algoritma Hierarchical) Stage II
            String normalFileHCStageII = "num_" + i + ".type_" + typeReputation + ".dangerousityHCStageII.hybrid.model";
            String normalPathHCStageII = "database/weka/model/" + normalFileHCStageII;
            EksternalFile.saveClustererToExternalModel(clusterDangerousity3, normalPathHCStageII);
        }

        // Write evaluation statistic result
        String fileNameEvaluation = "evaluationStatisticHybrid.type_" + typeReputation + ".txt";
        String pathNameEvaluation = "database/weka/statistic/" + fileNameEvaluation;
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(), pathNameEvaluation);
    }
}
