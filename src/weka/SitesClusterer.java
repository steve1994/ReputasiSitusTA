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
        // Setting Attributes Vector Overall to Instance Record
//        FastVector attributeInstanceRecord = new FastVector();
//        for (Attribute attr : overallInstanceVector) {
//            attributeInstanceRecord.addElement(attr);
//        }
//        Attribute siteLabelNominal = new Attribute("class", siteLabel);
//        overallInstanceVector.add(siteLabelNominal);
//        // Setting Attributes Vector Overall to Instance Record
//        FastVector attributeInstanceRecord = new FastVector();
//        for (Attribute attr : overallInstanceVector) {
//            attributeInstanceRecord.addElement(attr);
//        }
//        siteReputationRecord = new Instances("Reputation Site Dataset",attributeInstanceRecord,0);
//        siteReputationRecord.setClassIndex(siteReputationRecord.numAttributes() - 1);
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
        // Create new instance weka then insert it into siteReputationRecord
//        double[] values = new double[instanceValues.size()];
//        for (int i=0;i<instanceValues.size();i++) {
//            values[i] = new Double(instanceValues.get(i).toString());
//        }
//        Instance instance = new Instance(1.0,values);
//
//        siteReputationRecord.add(instance);

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
     * Split instances by numFold folds (round robin method)
     * @param dataset
     * @param numFold
     * @return
     */
    public List<Instances> getSplitInstanceDataSet(Instances dataset, int numFold) {
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
     * Calculate list double average
     * @param listDouble
     * @return
     */
    private double getAverageFromListDouble(List<Double> listDouble) {
        int size = listDouble.size();
        double sumElement = 0;
        for (Double element : listDouble) {
            sumElement += element;
        }
        return sumElement / (double) size;
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
            } else {
                clusterer = buildEMReputationModel(trainingInstances,clusterNumber);
            }
            double errorCluster = getIncorreclyClassifiedInstance(evaluateClusterReputationModel(testInstances, clusterer), testInstances);
            listErrorsPerFold.add(errorCluster);
        }

        return getAverageFromListDouble(listErrorsPerFold);
    }

    /**
     * Calculate incorreclty classified instances manually (classes to cluster eval)
     * @param eval
     * @param testSet
     * @return
     */
    public double getIncorreclyClassifiedInstance(ClusterEvaluation eval, Instances testSet) {
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

    public static long getAverageListLong(List<Long> listLong) {
        long sumListLong = 0;
        for (Long l : listLong) {
            sumListLong += l;
        }
        long average = 0;
        if (listLong.size() > 0) {
            average = sumListLong / (long) listLong.size();
        }
        return average;
    }

    public static void main(String[] args) {
        // Cluster sites dengan tipe reputasi 7 dan jumlah cluster 4
        int typeReputation = 3;
        SitesClusterer normalityClusterSite = new SitesClusterer(typeReputation);
        normalityClusterSite.configARFFInstance(new String[]{"normal","abnormal"});
        SitesClusterer dangerousityClusterSite = new SitesClusterer(typeReputation);
        dangerousityClusterSite.configARFFInstance(new String[]{"malware,phishing,spamming"});
        System.out.println("Config ARFF Done");

        Instances cobacoba = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data/num_100.type_3.dangerous_category.supervised.arff");
        cobacoba.setClassIndex(cobacoba.numAttributes()-1);
        // Test AVG Log likelihood
        for (int i=0;i<10;i++) {
            System.out.println("Average error cluster-" + (i+1) + " : " + dangerousityClusterSite.getAverageTestErrorsCV(cobacoba,1,(i+1),10));
        }

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
        for (int k=0;k<4;k++) {
            List<String> listSites = EksternalFile.loadSitesTrainingList(k+1).getKey();
            for (int i = 0; i < numSitesMaxAllocation; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

                if (Arrays.asList(1,4,5,7).contains(typeReputation)) {

                    long beforeDNS = System.currentTimeMillis();

                    // TLD ratio
                    Sextet<Double, Double, Double, Double, Double, Double> TLDRatio = DNSExtractor.getTLDDistributionFromAS(listSites.get(i));
                    Double[] TLDRatioList = new Double[6];
                    TLDRatioList[0] = TLDRatio.getValue0();
                    TLDRatioList[1] = TLDRatio.getValue1();
                    TLDRatioList[2] = TLDRatio.getValue2();
                    TLDRatioList[3] = TLDRatio.getValue3();
                    TLDRatioList[4] = TLDRatio.getValue4();
                    TLDRatioList[5] = TLDRatio.getValue5();
                    fiturDNS.setPopularTLDRatio(TLDRatioList);
                    System.out.println("TLD Ratio");

                    long afterTLDRatio = System.currentTimeMillis();

                    // Hit AS Ratio (malware, phishing, spamming)
                    Double[] HitRatioList = new Double[3];
                    for (int j = 0; j < 3; j++) {
                        HitRatioList[j] = DNSExtractor.getHitASRatio(listSites.get(i), j + 1);
                    }
                    fiturDNS.setHitASRatio(HitRatioList);
                    System.out.println("Hit AS Ratio");

                    long afterHitRatio = System.currentTimeMillis();

                    // Name server distribution AS
                    fiturDNS.setDistributionNSAS(DNSExtractor.getDistributionNSFromAS(listSites.get(i)));
                    System.out.println("Name Server Distribution AS");

                    long afterNSDist = System.currentTimeMillis();

                    // Name server count
                    fiturDNS.setNumNameServer(DNSExtractor.getNumNameServers(listSites.get(i)));
                    System.out.println("Name Server Count");

                    long afterNSCount = System.currentTimeMillis();

                    // TTL Name Servers
                    fiturDNS.setListNSTTL(DNSExtractor.getNameServerTimeToLive(listSites.get(i)));
                    System.out.println("TTL Name Servers");

                    long afterTTLNS = System.currentTimeMillis();

                    // TTL DNS A Records
                    fiturDNS.setListDNSRecordTTL(DNSExtractor.getDNSRecordTimeToLive(listSites.get(i)));
                    System.out.println("TTL DNS Record");

                    long afterTTLIP = System.currentTimeMillis();
                }

                // SPESIFIC FEATURES
                Spesific_Feature fiturSpesific = new Spesific_Feature();

                if (Arrays.asList(2,4,6,7).contains(typeReputation)) {
                    long beforeSpesific = System.currentTimeMillis();

                    // Token Count URL
                    fiturSpesific.setTokenCountURL(ContentExtractor.getDomainTokenCountURL(listSites.get(i)));
                    System.out.println("Token Count URL");

                    long afterTokenCount = System.currentTimeMillis();

                    // Average Token Length URL
                    fiturSpesific.setAverageTokenLengthURL(ContentExtractor.getAverageDomainTokenLengthURL(listSites.get(i)));
                    System.out.println("Average Token Length URL");

                    long afterAvgToken = System.currentTimeMillis();

                    // SLD ratio from URL (malware, phishing, spamming)
                    double[] SLDRatioList = new double[3];
                    for (int j = 0; j < 3; j++) {
                        SLDRatioList[j] = ContentExtractor.getSLDHitRatio(listSites.get(i), j + 1);
                    }
                    fiturSpesific.setSLDRatio(SLDRatioList);
                    System.out.println("SLD Ratio List");

                    long afterSLDRatio = System.currentTimeMillis();

                    // Inbound link Approximation (Google, Yahoo, Bing)
                    int[] inboundLinkAppr = new int[3];
                    for (int j = 0; j < 3; j++) {
                        inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults(listSites.get(i), j + 1);
                    }
                    fiturSpesific.setInboundLink(inboundLinkAppr);
                    System.out.println("Inbound Link Approximation");

                    long afterInboundLink = System.currentTimeMillis();

                    // Lookup time to access site
                    fiturSpesific.setLookupTime(ContentExtractor.getDomainLookupTimeSite(listSites.get(i)));
                    System.out.println("Lookup Time");

                    long afterLookupTime = System.currentTimeMillis();
                }

                // TRUST FEATURES
                Trust_Feature fiturTrust = new Trust_Feature();

                if (Arrays.asList(3, 5, 6, 7).contains(typeReputation)) {
                    long beforeTrust = System.currentTimeMillis();

                    fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
                    System.out.println("Trust WOT");

                    long afterTrust = System.currentTimeMillis();
                }

                // TIME LOGGER SET
//                listTimeTLDRatioAS.add(afterTLDRatio - beforeDNS);
//                listTimeHitRatioAS.add(afterHitRatio - afterTLDRatio);
//                listTimeNSDistAS.add(afterNSDist - afterHitRatio);
//                listTimeNSCount.add(afterNSCount - afterNSDist);
//                listTimeTTLNS.add(afterTTLNS - afterNSCount);
//                listTimeTTLIP.add(afterTTLIP - afterTTLNS);
//                listTimeTokenCount.add(afterTokenCount - beforeSpesific);
//                listTimeAvgToken.add(afterAvgToken - afterTokenCount);
//                listTimeSLDRatio.add(afterSLDRatio - afterAvgToken);
//                listTimeInboundLink.add(afterInboundLink - afterSLDRatio);
//                listTimeLookupTime.add(afterLookupTime - afterInboundLink);
//                listTimeTrust.add(afterTrust - beforeTrust);

                // SET RECORD INSTANCE DATA STRUCTURE
                SiteRecordReputation recordML = new SiteRecordReputation();
                recordML.setDNSRecordFeature(fiturDNS);
                recordML.setSpesificRecordFeature(fiturSpesific);
                recordML.setTrustRecordFeature(fiturTrust);

                // Instances for data static
                if (k < 3) {
                    String classLabelDangerous = "";
                    switch (k) {
                        default:
                        case 0:
                            classLabelDangerous = "malware";
                            break;
                        case 1:
                            classLabelDangerous = "phishing";
                            break;
                        case 2:
                            classLabelDangerous = "spamming";
                            break;
                    }
                    dangerousityClusterSite.fillDataIntoInstanceRecord(recordML, classLabelDangerous);
                }
                String classLabelNormality = "";
                switch (k) {
                    case 0:
                    case 1:
                    case 2:
                        classLabelNormality = "abnormal";
                        break;
                    default:
                    case 3:
                        classLabelNormality = "normal";
                        break;
                }
                normalityClusterSite.fillDataIntoInstanceRecord(recordML, classLabelNormality);

                System.out.println("Situs ke-" + i);
            }
        }

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

        // Pisahkan instances berdasarkan tipenya (malware / phishing / spamming / normal)
//        Instances allInstancesRecordSite = normalityClusterSite.getSiteReputationRecord();
//        Instances allInstancesRecordSite2 = dangerousityClusterSite.getSiteReputationRecord();

        // Save data static (cluster normality and dangerousity)
        Instances allInstancesNormality = normalityClusterSite.getSiteReputationRecord();
        Instances allInstancesDangerousity = dangerousityClusterSite.getSiteReputationRecord();

        // Extract attributes from allInstancesRecordSite (malware / phishing / spamming / normal)
        FastVector instancesAttributesNormality = new FastVector();
        Enumeration attributesRecordSiteNormality = allInstancesNormality.enumerateAttributes();
        while (attributesRecordSiteNormality.hasMoreElements()) {
            instancesAttributesNormality.addElement((Attribute) attributesRecordSiteNormality.nextElement());
        }
        instancesAttributesNormality.addElement(allInstancesNormality.classAttribute());

        FastVector instancesAttributesDangerousity = new FastVector();
        Enumeration attributesRecordSiteDangerousity = allInstancesDangerousity.enumerateAttributes();
        while (attributesRecordSiteDangerousity.hasMoreElements()) {
            instancesAttributesDangerousity.addElement((Attribute) attributesRecordSiteDangerousity.nextElement());
        }
        instancesAttributesDangerousity.addElement(allInstancesDangerousity.classAttribute());

        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
        Instances normalInstances = new Instances("normal_instances", instancesAttributesNormality, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributesNormality, 0);
        for (int i = 0; i < allInstancesNormality.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesNormality.instance(i).classValue();
            if (allInstancesNormality.classAttribute().value(indexClassThisInstance) == "normal") {
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
            if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance) == "malware") {
                malwareInstances.add(allInstancesDangerousity.instance(i));
            } else if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance) == "phishing") {
                phishingInstances.add(allInstancesDangerousity.instance(i));
            } else if (allInstancesDangerousity.classAttribute().value(indexClassThisInstance) == "spamming") {
                spammingInstances.add(allInstancesDangerousity.instance(i));
            }
        }

        // Secara bertahap dari jumlah training 1-100 (iterasi 10), evaluasi hasil clustering
        StringBuffer statisticEvaluationReport = new StringBuffer();
        int interval = 100;
        int maxCluster = 10;
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

            // Tulis instance di eksternal file
            String fileName = "num_" + i + ".type_" + typeReputation + ".normality_category.unsupervised.arff";
            String pathName = "database/weka/data/" + fileName;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesNormality, pathName);

            String fileName2 = "num_" + i + ".type_" + typeReputation + ".dangerous_category.unsupervised.arff";
            String pathName2 = "database/weka/data/" + fileName2;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSitesDangerousity, pathName2);

            statisticEvaluationReport.append("\n\n===================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            int optimumClusterEMNormal = 0, optimumClusterEMDangerous = 0, optimumClusterKmeansNormal = 0, optimumClusterKmeansDangerous = 0;

            // Find optimum Cluster for EM Algorithm
            for (int j=1;j<=maxCluster;j++) {

            }
            // Find Optimum Cluster for KMeans Algorithm
            for (int j=1;j<=maxCluster;j++) {

            }

            // Build cluster (normality type)
            Clusterer clusterKMeansNormal = normalityClusterSite.buildKmeansReputationModel(trainingRecordSitesNormality,optimumClusterKmeansNormal);
            normalityClusterSite.evaluateClusterReputationModel(trainingRecordSitesNormality,clusterKMeansNormal);
            Clusterer clusterEMNormal = normalityClusterSite.buildEMReputationModel(trainingRecordSitesNormality,optimumClusterEMNormal);
            normalityClusterSite.evaluateClusterReputationModel(trainingRecordSitesNormality,clusterEMNormal);
            // Build cluster (dangerousity type)
            Clusterer clusterKMeansDangerous = dangerousityClusterSite.buildKmeansReputationModel(trainingRecordSitesDangerousity,optimumClusterKmeansDangerous);
            dangerousityClusterSite.evaluateClusterReputationModel(trainingRecordSitesDangerousity,clusterKMeansDangerous);
            Clusterer clusterEMDangerous = dangerousityClusterSite.buildEMReputationModel(trainingRecordSitesDangerousity,optimumClusterEMDangerous);
            dangerousityClusterSite.evaluateClusterReputationModel(trainingRecordSitesDangerousity,clusterEMDangerous);
        }
        // Write evaluation statistic result
        String fileNameEvaluation = "evaluationStatisticUnsupervisedLearning.type_" + typeReputation + ".txt";
        String pathNameEvaluation = "database/weka/statistic/" + fileNameEvaluation;
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(), pathNameEvaluation);
    }
}
