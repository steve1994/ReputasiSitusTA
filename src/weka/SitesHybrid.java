package weka;

import Utils.API.WOT_API_Loader;
import Utils.DNS.DNSExtractor;
import Utils.Database.EksternalFile;
import Utils.Spesific.ContentExtractor;
import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;
import data_structure.instance_ML.SiteRecordReputation;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import weka.classifiers.Classifier;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by steve on 17/05/2016.
 */
public class SitesHybrid {
    public static void main(String[] args) {
        int typeReputation = 3;

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

        int numSitesMaxAllocation = 5;
        for (int k=0;k<4;k++) {     // malware, phishing, spamming, normal
            List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
            for (int i = 0; i < numSitesMaxAllocation; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

                if (Arrays.asList(1,4,5,7).contains(typeReputation)) {

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

                    // Hit AS Ratio (malware, phishing, spamming)
                    Double[] HitRatioList = new Double[3];
                    for (int j = 0; j < 3; j++) {
                        HitRatioList[j] = DNSExtractor.getHitASRatio(listSites.get(i), j + 1);
                    }
                    fiturDNS.setHitASRatio(HitRatioList);
                    System.out.println("Hit AS Ratio");

                    // Name server distribution AS
                    fiturDNS.setDistributionNSAS(DNSExtractor.getDistributionNSFromAS(listSites.get(i)));
                    System.out.println("Name Server Distribution AS");

                    // Name server count
                    fiturDNS.setNumNameServer(DNSExtractor.getNumNameServers(listSites.get(i)));
                    System.out.println("Name Server Count");

                    // TTL Name Servers
                    fiturDNS.setListNSTTL(DNSExtractor.getNameServerTimeToLive(listSites.get(i)));
                    System.out.println("TTL Name Servers");

                    // TTL DNS A Records
                    fiturDNS.setListDNSRecordTTL(DNSExtractor.getDNSRecordTimeToLive(listSites.get(i)));
                    System.out.println("TTL DNS Record");
                }

                // SPESIFIC FEATURES
                Spesific_Feature fiturSpesific = new Spesific_Feature();

                if (Arrays.asList(2,4,6,7).contains(typeReputation)) {
                    // Token Count URL
                    fiturSpesific.setTokenCountURL(ContentExtractor.getDomainTokenCountURL(listSites.get(i)));
                    System.out.println("Token Count URL");

                    // Average Token Length URL
                    fiturSpesific.setAverageTokenLengthURL(ContentExtractor.getAverageDomainTokenLengthURL(listSites.get(i)));
                    System.out.println("Average Token Length URL");

                    // SLD ratio from URL (malware, phishing, spamming)
                    double[] SLDRatioList = new double[3];
                    for (int j = 0; j < 3; j++) {
                        SLDRatioList[j] = ContentExtractor.getSLDHitRatio(listSites.get(i), j + 1);
                    }
                    fiturSpesific.setSLDRatio(SLDRatioList);
                    System.out.println("SLD Ratio List");

                    // Inbound link Approximation (Google, Yahoo, Bing)
                    int[] inboundLinkAppr = new int[3];
                    for (int j = 0; j < 3; j++) {
                        inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults(listSites.get(i), j + 1);
                    }
                    fiturSpesific.setInboundLink(inboundLinkAppr);
                    System.out.println("Inbound Link Approximation");

                    // Lookup time to access site
                    fiturSpesific.setLookupTime(ContentExtractor.getDomainLookupTimeSite(listSites.get(i)));
                    System.out.println("Lookup Time");
                }

                // TRUST FEATURES
                Trust_Feature fiturTrust = new Trust_Feature();

                if (Arrays.asList(3,5,6,7).contains(typeReputation)) {
                    fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
                    System.out.println("Trust WOT");
                }

                // SET RECORD INSTANCE DATA STRUCTURE
                SiteRecordReputation recordML = new SiteRecordReputation();
                recordML.setDNSRecordFeature(fiturDNS);
                recordML.setSpesificRecordFeature(fiturSpesific);
                recordML.setTrustRecordFeature(fiturTrust);

                // FILL INSTANCES INTO SITE CLUSTERER / LABELER ABOVE
                if (k < 3) {
                    String classLabelDangerousity = "";
                    switch (k) {
                        default:
                        case 0:
                            classLabelDangerousity = "malware";
                            break;
                        case 1:
                            classLabelDangerousity = "phishing";
                            break;
                        case 2:
                            classLabelDangerousity = "spamming";
                            break;
                    }
                    clusterSiteDangerousity.fillDataIntoInstanceRecord(recordML,classLabelDangerousity);
                    labelSiteDangerousity.fillDataIntoInstanceRecord(recordML,classLabelDangerousity);
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
                clusterSiteNormality.fillDataIntoInstanceRecord(recordML,classLabelNormality);
                labelSiteNormality.fillDataIntoInstanceRecord(recordML,classLabelNormality);

                System.out.println("Situs ke-" + (i+1));
            }
        }

        // Get extracted instances result from labeler / clusterer
        Instances allInstancesLabelNormality = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data/num_1000.type_3.normality_category.supervised.arff");
        allInstancesLabelNormality.setClassIndex(allInstancesLabelNormality.numAttributes()-1);
        Instances allInstancesLabelDangerousity = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data/num_1000.type_3.dangerous_category.supervised.arff");
        allInstancesLabelDangerousity.setClassIndex(allInstancesLabelDangerousity.numAttributes()-1);
//        Instances allInstancesLabelDangerousity = labelSiteDangerousity.getSiteReputationRecord();
//        Instances allInstancesLabelNormality = labelSiteNormality.getSiteReputationRecord();
//        Instances allInstancesClusterDangerousity = clusterSiteDangerousity.getSiteReputationRecord();
//        Instances allInstancesClusterNormality = clusterSiteNormality.getSiteReputationRecord();
//
        // Extracted vector attributes for Normal / Abnormal Instances
        FastVector instancesAttributesNormality = new FastVector();
        Enumeration attributesRecordSiteNormality = allInstancesLabelNormality.enumerateAttributes();
        while (attributesRecordSiteNormality.hasMoreElements()) {
            instancesAttributesNormality.addElement((Attribute) attributesRecordSiteNormality.nextElement());
        }
        instancesAttributesNormality.addElement(allInstancesLabelNormality.classAttribute());
        // Extracted vector attributes for Dangerous Instances (Malware / Phishing / Spamming)
        FastVector instancesAttributesDangerousity = new FastVector();
        Enumeration attributesRecordSiteDangerousity = allInstancesLabelDangerousity.enumerateAttributes();
        while (attributesRecordSiteDangerousity.hasMoreElements()) {
            instancesAttributesDangerousity.addElement((Attribute) attributesRecordSiteDangerousity.nextElement());
        }
        instancesAttributesDangerousity.addElement(allInstancesLabelDangerousity.classAttribute());

        // STAGE 1

        // Classify Normality Sites First and Split Normal / Abnormal
        Instances classifiedNormalityInstances = new Instances("normal_sites_supervised",instancesAttributesNormality,0);
        classifiedNormalityInstances.setClassIndex(classifiedNormalityInstances.numAttributes()-1);
        Classifier normalClassifier = labelSiteNormality.buildLabelReputationModel(allInstancesLabelNormality,1,0);

        Enumeration normalityInstances = allInstancesLabelNormality.enumerateInstances();
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

        // Build Normal / Abnormal Cluster
        Instances hybridInstancesAbnormal = new Instances("hybrid_normality_cluster",instancesAttributesNormality,0);
        int optimumNumCluster = 10;

        Clusterer clusterNormality = clusterSiteNormality.buildKmeansReputationModel(classifiedNormalityInstances, optimumNumCluster);
        ClusterEvaluation evalClusterNormality = clusterSiteNormality.evaluateClusterReputationModel(classifiedNormalityInstances,clusterNormality);
        try {
            // Find cluster with label / class Abnormal
            int numClusterAbnormal = 0;
            for (int j=0;j<evalClusterNormality.getClassesToClusters().length;j++) {
                int classValue = evalClusterNormality.getClassesToClusters()[j];
                if (classValue >= 0) {
                    if (classifiedNormalityInstances.classAttribute().value(classValue) == "abnormal") {
                        numClusterAbnormal = j;
                    }
                }
            }
            // Collect instances with cluster label Abnormal
            for (int k=0;k<evalClusterNormality.getClusterAssignments().length;k++) {
                int clusterNumber = (int) evalClusterNormality.getClusterAssignments()[k];
                if (clusterNumber == numClusterAbnormal) {
                    hybridInstancesAbnormal.add(classifiedNormalityInstances.instance(k));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(evalClusterNormality.clusterResultsToString());

        // STAGE 2

        // Classify Abnormal Instances from Stage 1 Using kNN Algorithm
        Instances classifiedDangerousityInstances = new Instances("dangerous_type_sites_supervised",instancesAttributesDangerousity,0);
        classifiedDangerousityInstances.setClassIndex(classifiedDangerousityInstances.numAttributes()-1);
        Classifier dangerousClassifier = labelSiteDangerousity.buildLabelReputationModel(allInstancesLabelDangerousity,1,0);

        Enumeration dangerousityInstances = allInstancesLabelDangerousity.enumerateInstances();
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

        // Build Malware / Phishing / Spamming Cluster (Abnormal Type Composition)
        Clusterer clusterDangerousity = clusterSiteDangerousity.buildKmeansReputationModel(classifiedDangerousityInstances, optimumNumCluster);
        ClusterEvaluation evalClusterDangerousity = clusterSiteDangerousity.evaluateClusterReputationModel(classifiedDangerousityInstances,clusterDangerousity);
        // Write Composition for Each Cluster
        List<Triplet<Double,Double,Double>> compositionEachCluster = new ArrayList<Triplet<Double, Double, Double>>();
        try {
            // Create data structure containing instances each cluster
            List<Instances> listInstancesPerCluster = new ArrayList<Instances>();
            for (int j=0;j<evalClusterDangerousity.getNumClusters();j++) {
                Instances thisClusterInstances = new Instances("dangerous_type_sites_supervised",instancesAttributesDangerousity,0);
                thisClusterInstances.setClassIndex(thisClusterInstances.numAttributes()-1);
                listInstancesPerCluster.add(j,thisClusterInstances);
            }
            // Fill data structure above with corresponding instance for each cluster
            for (int j=0;j<evalClusterDangerousity.getClusterAssignments().length;j++) {
                int clusterNumber = (int) evalClusterDangerousity.getClusterAssignments()[j];
                listInstancesPerCluster.get(clusterNumber).add(classifiedDangerousityInstances.instance(j));
            }
            // Calculate malware / phishing / spamming composition for each cluster
            for (int j=0;j<evalClusterDangerousity.getNumClusters();j++) {
                int counterMalwareThisCluster = 0, counterPhishingThisCluster = 0, counterSpammingThisCluster = 0;
                Instances instancesThisCluster = listInstancesPerCluster.get(j);
                for (int k=0;k<instancesThisCluster.numInstances();k++) {
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
                Triplet<Double,Double,Double> compositionThisCluster = new Triplet<Double, Double, Double>(malwareComposition,phishingComposition,spammingComposition);
                compositionEachCluster.add(j,compositionThisCluster);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0;i<compositionEachCluster.size();i++) {
            System.out.println("CLUSTER : " + (i+1));
            Triplet<Double,Double,Double> malPhisSpam = compositionEachCluster.get(i);
            System.out.println("Malware Percentage : " + malPhisSpam.getValue0());
            System.out.println("Phishing Percentage : " + malPhisSpam.getValue1());
            System.out.println("Spamming Percentage : " + malPhisSpam.getValue2());
        }
        System.out.println(evalClusterDangerousity.clusterResultsToString());
    }
}
