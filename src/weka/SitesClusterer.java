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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

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
     * Evaluate cluster build result with current instances
     * @param instances
     * @param clusterer
     * @return
     */
    public ClusterEvaluation evaluateClusterReputationModel(Instances instances, Clusterer clusterer) {
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        try {
            eval.evaluateClusterer(instances);
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
        SitesClusterer clusterSite = new SitesClusterer(typeReputation);
        clusterSite.configARFFInstance(new String[]{"malware", "phishing", "spamming","normal"});
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

        int numSitesMaxAllocation = 10;
        for (int k=0;k<4;k++) {
            List<String> listSites = EksternalFile.loadSitesTrainingList(k+1).getKey();
            for (int i = 0; i < numSitesMaxAllocation; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

                if (clusterSite.getListCombinationRecordType()[0] == true) {

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

                if (clusterSite.getListCombinationRecordType()[1] == true) {
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

                if (clusterSite.getListCombinationRecordType()[2] == true) {
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
                String classLabel = "";
                switch (k) {
                    default:
                    case 0:
                        classLabel = "malware";
                        break;
                    case 1:
                        classLabel = "phishing";
                        break;
                    case 2:
                        classLabel = "spamming";
                        break;
                    case 3:
                        classLabel = "normal";
                }
                clusterSite.fillDataIntoInstanceRecord(recordML,classLabel);

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
        Instances allInstancesRecordSite = clusterSite.getSiteReputationRecord();
        // Extract attributes from allInstancesRecordSite (malware / phishing / spamming / normal)
        FastVector instancesAttributes = new FastVector();
        Enumeration attributesRecordSite = allInstancesRecordSite.enumerateAttributes();
        while (attributesRecordSite.hasMoreElements()) {
            instancesAttributes.addElement((Attribute) attributesRecordSite.nextElement());
        }
        instancesAttributes.addElement(allInstancesRecordSite.classAttribute());
        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
        Instances malwareInstances = new Instances("malware_instances", instancesAttributes, 0);
        Instances phishingInstances = new Instances("phishing_instances", instancesAttributes, 0);
        Instances spammingInstances = new Instances("spamming_instances", instancesAttributes, 0);
        Instances normalInstances = new Instances("normal_instances", instancesAttributes, 0);
        for (int i = 0; i < allInstancesRecordSite.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesRecordSite.instance(i).classValue();
            if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "malware") {
                malwareInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "phishing") {
                phishingInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "spamming") {
                spammingInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "normal") {
                normalInstances.add(allInstancesRecordSite.instance(i));
            }
        }

        // Secara bertahap dari jumlah training 1-100 (iterasi 10), evaluasi hasil clustering
        StringBuffer statisticEvaluationReport = new StringBuffer();
        int interval = 5;
        int maxCluster = 10;
        for (int i=interval; i<=numSitesMaxAllocation; i=i+interval) {
            // Bentuk Training Record Secara Bertahap (malware, phishing, dan spamming)
            Instances trainingRecordSites = new Instances("mixed_instances_1", instancesAttributes, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSites.add(malwareInstances.instance(j));
                trainingRecordSites.add(phishingInstances.instance(j));
                trainingRecordSites.add(spammingInstances.instance(j));
                trainingRecordSites.add(normalInstances.instance(j));
            }
            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes() - 1);

            // Tulis instance di eksternal file
            String fileName = "num_" + i + ".type_" + typeReputation + ".unsupervised.arff";
            String pathName = "database/weka/data/" + fileName;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites, pathName);

            statisticEvaluationReport.append("\n\n===================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            // Build cluster berdasarkan mixed instances kemudian langsung evaluasi (K-means)
//            statisticEvaluationReport.append("\n\n-----------------------------------------------------------------\n\n SIMPLE K-means Algorithm \n\n");
            for (int j=1;j<=maxCluster;j++) {
                SimpleKMeans clusterKMeans = clusterSite.buildKmeansReputationModel(trainingRecordSites,j);
                // Classes to cluster evaluation
                ClusterEvaluation eval = clusterSite.evaluateClusterReputationModel(trainingRecordSites,clusterKMeans);
                statisticEvaluationReport.append("\n\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n\n\n");
                statisticEvaluationReport.append("\nResults Classes To Cluster Evaluation with cluster " + j + " : \n\n" + eval.clusterResultsToString());
            }

            // Build cluster berdasarkan mixed instances kemudian langsung evaluasi (EM)
//            statisticEvaluationReport.append("\n\n-----------------------------------------------------------------\n\n EM Algorithm \n\n");
            for (int j=1;j<=maxCluster;j++) {
                EM clusterEM = clusterSite.buildEMReputationModel(trainingRecordSites, j);
                // Classes to cluster evaluation
                ClusterEvaluation eval = clusterSite.evaluateClusterReputationModel(trainingRecordSites,clusterEM);
                statisticEvaluationReport.append("\n\n\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n\n\n");
                statisticEvaluationReport.append("\nResults Classes To Cluster Evaluation with cluster " + j + " : \n\n" + eval.clusterResultsToString());

            }
        }
        // Write evaluation statistic result
        String fileNameEvaluation = "evaluationStatisticUnsupervisedLearning.type_" + typeReputation + ".txt";
        String pathNameEvaluation = "database/weka/statistic/" + fileNameEvaluation;
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(), pathNameEvaluation);
    }
}
