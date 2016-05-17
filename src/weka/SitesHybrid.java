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
import weka.classifiers.Classifier;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

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
        Instances allInstancesLabelNormality = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data/num_100.type_3.normality_category.supervised.arff");
        allInstancesLabelNormality.setClassIndex(allInstancesLabelNormality.numAttributes()-1);
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
//        // Extracted vector attributes for Dangerous Instances (Malware / Phishing / Spamming)
//        FastVector instancesAttributesDangerousity = new FastVector();
//        Enumeration attributesRecordSiteDangerousity = allInstancesLabelDangerousity.enumerateAttributes();
//        while (attributesRecordSiteDangerousity.hasMoreElements()) {
//            instancesAttributesDangerousity.addElement((Attribute) attributesRecordSiteDangerousity.nextElement());
//        }
//        instancesAttributesDangerousity.addElement(allInstancesLabelDangerousity.classAttribute());
//
        // Classify Normality Sites First and Split Normal / Abnormal
        Instances classifiedNormalityInstances = new Instances("normal_sites_supervised",instancesAttributesNormality,0);
        classifiedNormalityInstances.setClassIndex(classifiedNormalityInstances.numAttributes()-1);
        Classifier normalClassifier = labelSiteNormality.buildLabelReputationModel(allInstancesLabelNormality,1,0);
        Enumeration normalityInstances = allInstancesLabelNormality.enumerateInstances();
        while (normalityInstances.hasMoreElements()) {
            Instance thisInstanceNormality = (Instance) normalityInstances.nextElement();
            double oldClassValue = thisInstanceNormality.classValue();
            System.out.println("OLD CLASS LABEL : " + allInstancesLabelNormality.classAttribute().value((int) oldClassValue));
            try {
                double classValue = normalClassifier.classifyInstance(thisInstanceNormality);
                thisInstanceNormality.setClassValue(classValue);
                classifiedNormalityInstances.add(thisInstanceNormality);
                System.out.println("INSTANCE : " + thisInstanceNormality);
                System.out.println("NEW CLASS LABEL : " + classifiedNormalityInstances.classAttribute().value((int) classValue));
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


        // Divide allInstancesRecordSite based on site class (malware / phishing / spamming / normal)
//        Instances malwareInstances = new Instances("malware_instances", instancesAttributes, 0);
//        Instances phishingInstances = new Instances("phishing_instances", instancesAttributes, 0);
//        Instances spammingInstances = new Instances("spamming_instances", instancesAttributes, 0);
//        Instances normalInstances = new Instances("normal_instances", instancesAttributes, 0);
//        for (int i = 0; i < allInstancesRecordSite.numInstances(); i++) {
//            int indexClassThisInstance = (int) allInstancesRecordSite.instance(i).classValue();
//            if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "malware") {
//                malwareInstances.add(allInstancesRecordSite.instance(i));
//            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "phishing") {
//                phishingInstances.add(allInstancesRecordSite.instance(i));
//            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "spamming") {
//                spammingInstances.add(allInstancesRecordSite.instance(i));
//            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "normal") {
//                normalInstances.add(allInstancesRecordSite.instance(i));
//            }
//        }



        // Secara bertahap dari jumlah training 1-100 (iterasi 10), evaluasi hasil clustering
//        StringBuffer statisticEvaluationReport = new StringBuffer();
//        int interval = 100;
//        int maxCluster = 10;
//        for (int i=interval; i<=numSitesMaxAllocation; i=i+interval) {
            // Bentuk Training Record Secara Bertahap (malware, phishing, dan spamming)
//            Instances trainingRecordSites = new Instances("mixed_instances_1", instancesAttributes, 0);
//            for (int j = 0; j < i; j++) {
//                trainingRecordSites.add(malwareInstances.instance(j));
//                trainingRecordSites.add(phishingInstances.instance(j));
//                trainingRecordSites.add(spammingInstances.instance(j));
//                trainingRecordSites.add(normalInstances.instance(j));
//            }
//            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes() - 1);
//
//            // Tulis instance di eksternal file
//            String fileName = "num_" + i + ".type_" + typeReputation + ".unsupervised.arff";
//            String pathName = "database/weka/data/" + fileName;
//            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites, pathName);

            // Build cluster berdasarkan mixed instances kemudian langsung evaluasi (K-means)
//            for (int j=1;j<=maxCluster;j++) {
//                SimpleKMeans clusterKMeans = clusterSite.buildKmeansReputationModel(trainingRecordSites, j);
//            }
//
//            // Build cluster berdasarkan mixed instances kemudian langsung evaluasi (EM)
//            for (int j=1;j<=maxCluster;j++) {
//                EM clusterEM = clusterSite.buildEMReputationModel(trainingRecordSites, j);
//            }
//        }
    }
}
