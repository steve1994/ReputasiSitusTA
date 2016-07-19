package weka;

import Utils.API.WOT_API_Loader;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import Utils.Statistics;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.clusterers.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.*;

/**
 * Created by steve on 26/03/2016.
 */
public class SitesClusterer extends SitesMLProcessor{

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     */
    public SitesClusterer(int typeReputation) {
        super(typeReputation);
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     */
    public void configARFFInstance(String[] classLabel) {
        List<Attribute> overallInstanceVector = super.getInstanceAttributes();
        FastVector siteLabel = new FastVector();
        for (String label : classLabel) {
            siteLabel.addElement(label);
        }
        Attribute siteLabelNominal = new Attribute("class", siteLabel);
        overallInstanceVector.add(siteLabelNominal);
        // Setting Attributes Vector Overall to Instance Record
        FastVector attributeInstanceRecord = new FastVector();
        for (Attribute attr : overallInstanceVector) {
            attributeInstanceRecord.addElement(attr);
        }
        siteReputationRecord = new Instances("Reputation Site Dataset", attributeInstanceRecord, 0);
        siteReputationRecord.setClassIndex(siteReputationRecord.numAttributes() - 1);
    }

    /**
     * Insert new reputation record into instances
     * Assumption : instances have been set properly
     * @param recordReputation
     * @param classLabel
     */
    public void fillDataIntoInstanceRecord(SiteRecordReputation recordReputation, String classLabel) {
        List<Object> instanceValues = super.getInstanceRecord(recordReputation);
        instanceValues.add(classLabel);

        double[] values = new double[instanceValues.size()];
        for (int i = 0; i < instanceValues.size(); i++) {
            if (i < instanceValues.size() - 1) {
                values[i] = new Double(instanceValues.get(i).toString());
            } else {
                if (i == instanceValues.size() - 1) {
                    Instances currentInstances = getSiteReputationRecord();
                    values[i] = currentInstances.attribute(currentInstances.numAttributes() - 1).indexOfValue(classLabel);
                }
            }
        }
        Instance instance = new Instance(1.0, values);
        siteReputationRecord.add(instance);
    }

    /**
     * Filtered Instances for Classes To Cluster Evaluation
     * @param instances
     * @return
     */
    private Instances filteredClassesToCluster(Instances instances) {
        weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
        filter.setAttributeIndices("" + (instances.classIndex() + 1));
        try {
            filter.setInputFormat(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Instances filteredInstances = null;
        try {
            filteredInstances = Filter.useFilter(instances, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filteredInstances;
    }

    /**
     * Build cluster from site record in instances weka (other settings is default)
     * using K-means clustering algorithm
     * @param instances
     * @param numCluster
     * @return
     */
    public SimpleKMeans buildKmeansReputationModel(Instances instances, int numCluster) {
        // Filter data before build
        Instances dataClusterer = filteredClassesToCluster(instances);
        // Build Cluster
        SimpleKMeans siteReputationCluster = new SimpleKMeans();
        siteReputationCluster.setPreserveInstancesOrder(true);
        try {
            siteReputationCluster.setNumClusters(numCluster);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            siteReputationCluster.buildClusterer(dataClusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return siteReputationCluster;
    }

    /**
     * Build cluster from site record in instances weka (other settings is default)
     * using EM (Expectation Maximization) Clustering Algorithm
     * @param instances
     * @param numCluster
     * @return
     */
    public EM buildEMReputationModel(Instances instances, int numCluster) {
        // Filter data before build
        Instances dataClusterer = filteredClassesToCluster(instances);
        // Build Cluster
        EM siteReputationCluster = new EM();
        try {
            siteReputationCluster.setNumClusters(numCluster);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            siteReputationCluster.buildClusterer(dataClusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return siteReputationCluster;
    }

    /**
     * Build cluster from site record in instances weka (other settings is default)
     * using HC (Hierarchical Clustering) Clustering Algorithm
     * @param instances
     * @param numCluster
     * @return
     */
    public HierarchicalClusterer buildHCReputationModel(Instances instances, int numCluster) {
        // Filter data before build
        Instances dataClusterer = filteredClassesToCluster(instances);
        // Build Cluster
        HierarchicalClusterer siteReputationCluster = new HierarchicalClusterer();
        siteReputationCluster.setNumClusters(numCluster);
        try {
            siteReputationCluster.buildClusterer(dataClusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return siteReputationCluster;
    }

    /**
     * Split instances by numFold folds (round robin method)
     * @param dataset
     * @param numFold
     * @return
     */
    private List<Instances> getSplitInstanceDataSet(Instances dataset, int numFold) {
        List<Instances> splitInstances = new ArrayList<Instances>();

        // Inisialisasi instances kosong per fold
        FastVector datasetAttributes = getAttributesVector(dataset);
        for (int i=0;i<numFold;i++) {
            Instances splitInstancesThisPart = new Instances("split_instance_fold_"+(i+1),datasetAttributes,0);
            splitInstances.add(i,splitInstancesThisPart);
        }
        // Secara round robin, isi instances dari dataset
        int sizeDatasetCounter = 0;
        int splitIndex = 0;
        do {
            Instance instanceFromDataset = dataset.instance(sizeDatasetCounter);
            splitInstances.get(splitIndex).add(instanceFromDataset);
            splitIndex++;
            if (splitIndex == numFold) {
                splitIndex = 0;
            }
            sizeDatasetCounter++;
        } while (sizeDatasetCounter < dataset.numInstances());

        return splitInstances;
    }

    /**
     * Calculated average test set error using v-fold cross validation method
     * @param dataset
     * @param algorithmType
     * @param clusterNumber
     * @param numFold
     * @return
     */
    public double getAverageTestErrorsCV(Instances dataset, int algorithmType, int clusterNumber, int numFold) {
        List<Double> listErrorsPerFold = new ArrayList<Double>();
        List<Instances> splitInstances = getSplitInstanceDataSet(dataset,numFold);
        for (int j=0;j<numFold;j++) {
            Instances testInstances = new Instances("test_instances",getAttributesVector(dataset),0);
            testInstances.setClassIndex(testInstances.numAttributes()-1);
            Instances trainingInstances = new Instances("training_instances",getAttributesVector(dataset),0);
            trainingInstances.setClassIndex(trainingInstances.numAttributes()-1);
            // Fill Test Set and Training Set
            Instances instancesThisFold = splitInstances.get(j);
            for (int k=0;k<instancesThisFold.numInstances();k++) {
                testInstances.add(instancesThisFold.instance(k));
            }
            for (int k=0;k<numFold;k++) {
                if (k != j) {
                    Instances instancesRemainingFold = splitInstances.get(k);
                    for (int l=0;l<instancesRemainingFold.numInstances();l++) {
                        trainingInstances.add(instancesRemainingFold.instance(l));
                    }
                }
            }
            // Build Cluster using Training, Evaluate Cluster using Test and get error
            Clusterer clusterer;
            if (algorithmType == 1) {
                clusterer = buildKmeansReputationModel(trainingInstances,clusterNumber);
            } else if (algorithmType == 2) {
                clusterer = buildEMReputationModel(trainingInstances,clusterNumber);
            } else {
                clusterer = buildHCReputationModel(trainingInstances,clusterNumber);
            }
            double errorCluster = getIncorrectlyClassifiedInstance(evaluateClusterReputationModel(testInstances, clusterer), testInstances);
            listErrorsPerFold.add(errorCluster);
        }

        return Statistics.getAverageListDouble(listErrorsPerFold);
    }

    /**
     * Calculate incorreclty classified instances manually (classes to cluster eval)
     * @param eval
     * @param testSet
     * @return
     */
    public static double getIncorrectlyClassifiedInstance(ClusterEvaluation eval, Instances testSet) {
        int[] classesToCluster = eval.getClassesToClusters();
        double[] clusterAssignment = eval.getClusterAssignments();

        int incorrectClusterInstance = 0;
        for (int i=0;i<testSet.numInstances();i++) {
            int classValueOriginal = (int) testSet.instance(i).classValue();
            int clusterThisInstance = (int) clusterAssignment[i];
            int classValueClustering = classesToCluster[clusterThisInstance];
            if (classValueOriginal != classValueClustering) {
                incorrectClusterInstance++;
            }
        }

        return ((double) incorrectClusterInstance / (double) testSet.numInstances()) * 100;
    }

    /**
     * Evaluate cluster build result with current instances
     * @param testInstances
     * @param clusterer
     * @return
     */
    public ClusterEvaluation evaluateClusterReputationModel(Instances testInstances, Clusterer clusterer) {
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        try {
            eval.evaluateClusterer(testInstances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eval;
    }


    public static void main(String[] args) {
        // Cluster sites dengan tipe reputasi 7 dan jumlah cluster 4
        int typeReputation = 7;
        SitesClusterer normalityClusterSite = new SitesClusterer(typeReputation);
        normalityClusterSite.configARFFInstance(new String[]{"normal","abnormal"});
        SitesClusterer dangerousityClusterSite = new SitesClusterer(typeReputation);
        dangerousityClusterSite.configARFFInstance(new String[]{"malware,phishing,spamming"});
        System.out.println("Config ARFF Done");

        // Time performance logger
//        List<Long> listTimeTLDRatioAS = new ArrayList<Long>();
//        List<Long> listTimeHitRatioAS = new ArrayList<Long>();
//        List<Long> listTimeNSDistAS = new ArrayList<Long>();
//        List<Long> listTimeNSCount = new ArrayList<Long>();
//        List<Long> listTimeTTLNS = new ArrayList<Long>();
//        List<Long> listTimeTTLIP = new ArrayList<Long>();
//        List<Long> listTimeTokenCount = new ArrayList<Long>();
//        List<Long> listTimeAvgToken = new ArrayList<Long>();
//        List<Long> listTimeSLDRatio = new ArrayList<Long>();
//        List<Long> listTimeInboundLink = new ArrayList<Long>();
//        List<Long> listTimeLookupTime = new ArrayList<Long>();
//        List<Long> listTimeTrust = new ArrayList<Long>();

        int numSitesMaxAllocation = 1000;
//        for (int k=0;k<4;k++) {
//            List<String> listSites = EksternalFile.loadSitesTrainingList(k+1).getKey();
//            for (int i = 0; i < numSitesMaxAllocation; i++) {
//
//                // TIME LOGGER SET
////                listTimeTLDRatioAS.add(afterTLDRatio - beforeDNS);
////                listTimeHitRatioAS.add(afterHitRatio - afterTLDRatio);
////                listTimeNSDistAS.add(afterNSDist - afterHitRatio);
////                listTimeNSCount.add(afterNSCount - afterNSDist);
////                listTimeTTLNS.add(afterTTLNS - afterNSCount);
////                listTimeTTLIP.add(afterTTLIP - afterTTLNS);
////                listTimeTokenCount.add(afterTokenCount - beforeSpesific);
////                listTimeAvgToken.add(afterAvgToken - afterTokenCount);
////                listTimeSLDRatio.add(afterSLDRatio - afterAvgToken);
////                listTimeInboundLink.add(afterInboundLink - afterSLDRatio);
////                listTimeLookupTime.add(afterLookupTime - afterInboundLink);
////                listTimeTrust.add(afterTrust - beforeTrust);
//
//                // SET RECORD INSTANCE DATA STRUCTURE
//                SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i),typeReputation);
//
//                // Instances for data static
//                if (k < 3) {
//                    String classLabelDangerous = "";
//                    switch (k) {
//                        default:
//                        case 0:
//                            classLabelDangerous = "malware";
//                            break;
//                        case 1:
//                            classLabelDangerous = "phishing";
//                            break;
//                        case 2:
//                            classLabelDangerous = "spamming";
//                            break;
//                    }
//                    dangerousityClusterSite.fillDataIntoInstanceRecord(recordML, classLabelDangerous);
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
//                normalityClusterSite.fillDataIntoInstanceRecord(recordML, classLabelNormality);
//
//                System.out.println("Situs ke-" + i);
//            }
//        }

//        System.out.println("FITUR DNS : ");
//        System.out.println("Avg Time TLD Ratio AS : " + getAverageListLong(listTimeTLDRatioAS) + " ms");
//        System.out.println("Avg Time Hit Ratio AS : " + getAverageListLong(listTimeHitRatioAS) + " ms");
//        System.out.println("Avg Time NS Distribution AS : " + getAverageListLong(listTimeNSDistAS) + " ms");
//        System.out.println("Avg Time NS Count : " + getAverageListLong(listTimeNSCount) + " ms");
//        System.out.println("Avg Time TTL NS : " + getAverageListLong(listTimeTTLNS) + " ms");
//        System.out.println("Avg Time TTL IP : " + getAverageListLong(listTimeTTLIP) + " ms");
//        System.out.println("FITUR SPESIFIK : ");
//        System.out.println("Avg Time Token Count : " + getAverageListLong(listTimeTokenCount) + " ms");
//        System.out.println("Avg Time Avg Token : " + getAverageListLong(listTimeAvgToken) + " ms");
//        System.out.println("Avg Time SLD Ratio : " + getAverageListLong(listTimeSLDRatio) + " ms");
//        System.out.println("Avg Time Inbound Link : " + getAverageListLong(listTimeInboundLink) + " ms");
//        System.out.println("Avg Time Lookup Time : " + getAverageListLong(listTimeLookupTime) + " ms");
//        System.out.println("FITUR TRUST : ");
//        System.out.println("Avg Time Trust : " + getAverageListLong(listTimeTrust) + " ms");

        // Save data static (cluster normality and dangerousity)
//        Instances allInstancesNormality = normalityClusterSite.getSiteReputationRecord();
//        allInstancesNormality.setClassIndex(allInstancesNormality.numAttributes()-1);
//        Instances allInstancesDangerousity = dangerousityClusterSite.getSiteReputationRecord();
//        allInstancesDangerousity.setClassIndex(allInstancesDangerousity.numAttributes()-1);

        Instances allInstancesNormality = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".normal.staticdata.arff");
        allInstancesNormality.setClassIndex(allInstancesNormality.numAttributes()-1);
        Instances allInstancesDangerousity = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".dangerous.staticdata.arff");
        allInstancesDangerousity.setClassIndex(allInstancesDangerousity.numAttributes()-1);

        // Extract attributes from allInstancesRecordSite (malware / phishing / spamming / normal)
        FastVector instancesAttributesNormality = normalityClusterSite.getAttributesVector(allInstancesNormality);
        FastVector instancesAttributesDangerousity = dangerousityClusterSite.getAttributesVector(allInstancesDangerousity);

        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
        Instances normalInstances = new Instances("normal_instances", instancesAttributesNormality, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributesNormality, 0);
        for (int i = 0; i < allInstancesNormality.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesNormality.instance(i).classValue();
            if (allInstancesNormality.classAttribute().value(indexClassThisInstance).equals("normal")) {
                normalInstances.add(allInstancesNormality.instance(i));
            } else {
                abnormalInstances.add(allInstancesNormality.instance(i));
            }
        }

        Instances malwareInstances = new Instances("malware_instances", instancesAttributesDangerousity, 0);
        Instances phishingInstances = new Instances("phishing_instances", instancesAttributesDangerousity, 0);
        Instances spammingInstances = new Instances("spamming_instances", instancesAttributesDangerousity, 0);
        for (int i = 0; i < allInstancesDangerousity.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesDangerousity.instance(i).classValue();
            if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance).equals("malware")) {
                malwareInstances.add(allInstancesDangerousity.instance(i));
            } else if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance).equals("phishing")) {
                phishingInstances.add(allInstancesDangerousity.instance(i));
            } else if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance).equals("spamming")) {
                spammingInstances.add(allInstancesDangerousity.instance(i));
            }
        }

        StringBuffer statisticEvaluationReport = new StringBuffer();

        // Secara bertahap dari jumlah training 1-100 (iterasi 10), ambil cluster optimum per iterasi
        int interval = 100;
        int maxCluster = 10;
        List<Integer> listOCKmeansNormalPerAlloc = new ArrayList<Integer>();
        List<Integer> listOCKmeansDangerousPerAlloc = new ArrayList<Integer>();
        List<Integer> listOCEMNormalPerAlloc = new ArrayList<Integer>();
        List<Integer> listOCEMDangerousPerAlloc = new ArrayList<Integer>();
        List<Integer> listOCHCNormalPerAlloc = new ArrayList<Integer>();
        List<Integer> listOCHCDangerousPerAlloc = new ArrayList<Integer>();

        statisticEvaluationReport.append("\n\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        statisticEvaluationReport.append("$$$$$$$$$$   NUMBER CLUSTER EACH TRAINING ALLOCATION   $$$$$$$$$$$$$$\n");
        statisticEvaluationReport.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");

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

            // Tulis instance di eksternal file
            String fileName = "num_" + i + ".type_" + typeReputation + ".normality_category.unsupervised.arff";
            String pathName = "database/weka/data/" + fileName;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesNormality, pathName);

            String fileName2 = "num_" + i + ".type_" + typeReputation + ".dangerous_category.unsupervised.arff";
            String pathName2 = "database/weka/data/" + fileName2;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesDangerousity, pathName2);

            // Find Optimum Cluster for KMeans Algorithm
            List<Double> listAvgPerClusterNormalKmeans = new ArrayList<Double>();
            List<Double> listAvgPerClusterDangerousKmeans = new ArrayList<Double>();
            for (int j=1;j<maxCluster;j++) {
                listAvgPerClusterNormalKmeans.add(normalityClusterSite.getAverageTestErrorsCV(trainingRecordSitesNormality, 1, (j+1), 10));
            }
            for (int j=2;j<maxCluster;j++) {
                listAvgPerClusterDangerousKmeans.add(dangerousityClusterSite.getAverageTestErrorsCV(trainingRecordSitesDangerousity, 1, (j+1), 10));
            }
            listOCKmeansNormalPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterNormalKmeans).getValue0() + 2);
            listOCKmeansDangerousPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterDangerousKmeans).getValue0() + 3);

            // Find optimum Cluster for EM Algorithm
            List<Double> listAvgPerClusterNormalEM = new ArrayList<Double>();
            List<Double> listAvgPerClusterDangerousEM = new ArrayList<Double>();
            for (int j=1;j<maxCluster;j++) {
                listAvgPerClusterNormalEM.add(normalityClusterSite.getAverageTestErrorsCV(trainingRecordSitesNormality, 2, (j+1), 10));
            }
            for (int j=2;j<maxCluster;j++) {
                listAvgPerClusterDangerousEM.add(dangerousityClusterSite.getAverageTestErrorsCV(trainingRecordSitesDangerousity, 2, (j+1), 10));
            }
            listOCEMNormalPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterNormalEM).getValue0() + 2);
            listOCEMDangerousPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterDangerousEM).getValue0() + 3);

            // Find optimum Cluster for HIerarchical Clustering Algorithm
            List<Double> listAvgPerClusterNormalHC = new ArrayList<Double>();
            List<Double> listAvgPerClusterDangerousHC = new ArrayList<Double>();
            for (int j=1;j<maxCluster;j++) {
                listAvgPerClusterNormalHC.add(normalityClusterSite.getAverageTestErrorsCV(trainingRecordSitesNormality, 3, (j+1), 10));
            }
            for (int j=2;j<maxCluster;j++) {
                listAvgPerClusterDangerousHC.add(dangerousityClusterSite.getAverageTestErrorsCV(trainingRecordSitesDangerousity, 3, (j+1), 10));
            }
            listOCHCNormalPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterNormalHC).getValue0() + 2);
            listOCHCDangerousPerAlloc.add(Statistics.getMinimumValueListDouble(listAvgPerClusterDangerousEM).getValue0() + 3);

            // Write Statistic num Cluster
            statisticEvaluationReport.append("Optimum Cluster KMeans Stage 1 : " + listOCKmeansNormalPerAlloc.get(listOCKmeansNormalPerAlloc.size()-1) + "\n");
            statisticEvaluationReport.append("Optimum Cluster KMeans Stage 2 : " + listOCKmeansDangerousPerAlloc.get(listOCKmeansDangerousPerAlloc.size()-1) + "\n");
            statisticEvaluationReport.append("Optimum Cluster EM Stage 1 : " + listOCEMNormalPerAlloc.get(listOCEMNormalPerAlloc.size()-1) + "\n");
            statisticEvaluationReport.append("Optimum Cluster EM Stage 2 : " + listOCEMDangerousPerAlloc.get(listOCEMDangerousPerAlloc.size()-1) + "\n");
            statisticEvaluationReport.append("Optimum Cluster Hierarchical Stage 1 : " + listOCHCNormalPerAlloc.get(listOCHCNormalPerAlloc.size()-1) + "\n");
            statisticEvaluationReport.append("Optimum Cluster Hierarchical Stage 2 : " + listOCHCDangerousPerAlloc.get(listOCHCDangerousPerAlloc.size()-1) + "\n");
            System.out.println("NUM SITES TRAINING : " + i);
        }

        // Determine final optimum cluster for each algorithm per type
        int aggOCKmeansNormal = Statistics.getMostFrequentValueListInteger(listOCKmeansNormalPerAlloc);
        int aggOCKmeansDangerous = Statistics.getMostFrequentValueListInteger(listOCKmeansDangerousPerAlloc);
        int aggOCEMNormal = Statistics.getMostFrequentValueListInteger(listOCEMNormalPerAlloc);
        int aggOCEMDangerous = Statistics.getMostFrequentValueListInteger(listOCEMDangerousPerAlloc);
        int aggOCHCNormal = Statistics.getMostFrequentValueListInteger(listOCHCNormalPerAlloc);
        int aggOCHCDangerous = Statistics.getMostFrequentValueListInteger(listOCHCDangerousPerAlloc);
        // Write statistic about final optimum Cluster
        statisticEvaluationReport.append("\n\n\naggOCKMeansNormal : " + aggOCKmeansNormal + "\n");
        statisticEvaluationReport.append("aggOCKmeansDangerous : " + aggOCKmeansDangerous + "\n");
        statisticEvaluationReport.append("aggOCEMNormal : " + aggOCEMNormal + "\n");
        statisticEvaluationReport.append("aggOCEMDangerous : " + aggOCEMDangerous + "\n");
        statisticEvaluationReport.append("aggOCHCNormal : " + aggOCHCNormal + "\n");
        statisticEvaluationReport.append("aggOCHCDangerous : " + aggOCHCDangerous + "\n\n\n");

        statisticEvaluationReport.append("\n\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        statisticEvaluationReport.append("$$$$$$$$   INCORRECTLY CLASSIFIED INSTANCE PER ALLOCATION   $$$$$$$$$\n");
        statisticEvaluationReport.append("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");

        // Secara bertahap dari jumlah training 1-100 (iterasi 100), build cluster
        // berdasarkan optimum cluster final per algoritma per tipe di atas
        for (int i=interval; i<=numSitesMaxAllocation; i=i+interval) {
            statisticEvaluationReport.append("\n\n===================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            // Bentuk Training Record Secara Bertahap (normal, abnormal)
            Instances trainingRecordSitesNormality = new Instances("mixed_instances_normality", instancesAttributesNormality, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSitesNormality.add(normalInstances.instance(j));
                trainingRecordSitesNormality.add(abnormalInstances.instance(j));
            }
            trainingRecordSitesNormality.setClassIndex(trainingRecordSitesNormality.numAttributes() - 1);
            // Bentuk Training Record Secara Bertahap (normal, abnormal)
            Instances trainingRecordSitesDangerousity = new Instances("mixed_instances_dangerousity", instancesAttributesDangerousity, 0);
            int numDangerousSites = i / 3;
            for (int j = 0; j < numDangerousSites; j++) {
                trainingRecordSitesDangerousity.add(malwareInstances.instance(j));
                trainingRecordSitesDangerousity.add(phishingInstances.instance(j));
                trainingRecordSitesDangerousity.add(spammingInstances.instance(j));
            }
            trainingRecordSitesDangerousity.setClassIndex(trainingRecordSitesDangerousity.numAttributes() - 1);

            // Save both training data (normality and dangerousity)
            String fileNameNormal = "num_" + i + ".type_" + typeReputation + ".normality_category.unsupervised.arff";
            String pathNameNormal = "database/weka/data/" + fileNameNormal;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesNormality, pathNameNormal);
            String fileNameDangerous = "num_" + i + ".type_" + typeReputation + ".dangerous_category.unsupervised.arff";
            String pathNameDangerous = "database/weka/data/" + fileNameDangerous;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesDangerousity, pathNameDangerous);

            // Build cluster KMeans
            Clusterer clusterKMeansNormal = normalityClusterSite.buildKmeansReputationModel(trainingRecordSitesNormality,aggOCKmeansNormal);
            ClusterEvaluation evalKMeans1 = normalityClusterSite.evaluateClusterReputationModel(trainingRecordSitesNormality,clusterKMeansNormal);
            Clusterer clusterKMeansDangerous = dangerousityClusterSite.buildKmeansReputationModel(trainingRecordSitesDangerousity,aggOCKmeansDangerous);
            ClusterEvaluation evalKMeans2 = dangerousityClusterSite.evaluateClusterReputationModel(trainingRecordSitesDangerousity, clusterKMeansDangerous);
            // Save clusterer KMeans
            String normalFileKmeansNormal = "num_" + i + ".type_" + typeReputation + ".normalityKmeans.model";
            String normalPathKmeansNormal = "database/weka/model/" + normalFileKmeansNormal;
            EksternalFile.saveClustererToExternalModel(clusterKMeansNormal, normalPathKmeansNormal);
            String normalFileKmeansDangerous = "num_" + i + ".type_" + typeReputation + ".dangerousityKmeans.model";
            String normalPathKmeansDangerous = "database/weka/model/" + normalFileKmeansDangerous;
            EksternalFile.saveClustererToExternalModel(clusterKMeansDangerous,normalPathKmeansDangerous);

            // Build cluster EM
            Clusterer clusterEMNormal = normalityClusterSite.buildEMReputationModel(trainingRecordSitesNormality,aggOCEMNormal);
            ClusterEvaluation evalEM1 = normalityClusterSite.evaluateClusterReputationModel(trainingRecordSitesNormality,clusterEMNormal);
            Clusterer clusterEMDangerous = dangerousityClusterSite.buildEMReputationModel(trainingRecordSitesDangerousity,aggOCEMDangerous);
            ClusterEvaluation evalEM2 = dangerousityClusterSite.evaluateClusterReputationModel(trainingRecordSitesDangerousity,clusterEMDangerous);
            // Save clusterer EM
            String normalFileEMNormal = "num_" + i + ".type_" + typeReputation + ".normalityEM.model";
            String normalPathEMNormal = "database/weka/model/" + normalFileEMNormal;
            EksternalFile.saveClustererToExternalModel(clusterEMNormal, normalPathEMNormal);
            String normalFileEMDangerous = "num_" + i + ".type_" + typeReputation + ".dangerousityEM.model";
            String normalPathEMDangerous = "database/weka/model/" + normalFileEMDangerous;
            EksternalFile.saveClustererToExternalModel(clusterEMDangerous,normalPathEMDangerous);

            // Build cluster Hierarchical Clustering
            Clusterer clusterHCNormal = normalityClusterSite.buildEMReputationModel(trainingRecordSitesNormality,aggOCHCNormal);
            ClusterEvaluation evalHC1 = normalityClusterSite.evaluateClusterReputationModel(trainingRecordSitesNormality,clusterHCNormal);
            Clusterer clusterHCDangerous = dangerousityClusterSite.buildEMReputationModel(trainingRecordSitesDangerousity,aggOCHCDangerous);
            ClusterEvaluation evalHC2 = dangerousityClusterSite.evaluateClusterReputationModel(trainingRecordSitesDangerousity,clusterHCDangerous);
            // Save clusterer Hierarchical Clustering
            String normalFileHCNormal = "num_" + i + ".type_" + typeReputation + ".normalityHC.model";
            String normalPathHCNormal = "database/weka/model/" + normalFileHCNormal;
            EksternalFile.saveClustererToExternalModel(clusterHCNormal, normalPathHCNormal);
            String normalFileHCDangerous = "num_" + i + ".type_" + typeReputation + ".dangerousityHC.model";
            String normalPathHCDangerous = "database/weka/model/" + normalFileHCDangerous;
            EksternalFile.saveClustererToExternalModel(clusterHCDangerous,normalPathHCDangerous);

            // Write Statistic About Error Statistic
            statisticEvaluationReport.append("Error KMeans Stage 1 : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalKMeans1, trainingRecordSitesNormality) + "\n");
            statisticEvaluationReport.append("Error KMeans Stage 2 : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalKMeans2, trainingRecordSitesDangerousity) + "\n");
            statisticEvaluationReport.append("Error EM Stage 1 : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalEM1, trainingRecordSitesNormality) + "\n");
            statisticEvaluationReport.append("Error EM Stage 2 : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalEM2, trainingRecordSitesDangerousity) + "\n");
            statisticEvaluationReport.append("Error Hierarchical Stage 1 : " + SitesClusterer.getIncorrectlyClassifiedInstance(evalHC1, trainingRecordSitesNormality) + "\n");
            statisticEvaluationReport.append("Error Hierarchical Stage 2: " + SitesClusterer.getIncorrectlyClassifiedInstance(evalHC2, trainingRecordSitesDangerousity) + "\n");
        }

        // Write evaluation statistic result
        String fileNameEvaluation = "evaluationStatisticUnsupervisedLearning.type_" + typeReputation + ".txt";
        String pathNameEvaluation = "database/weka/statistic/" + fileNameEvaluation;
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(), pathNameEvaluation);
    }
}
