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
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.Enumeration;
import java.util.List;

/**
 * Created by steve on 17/05/2016.
 */
public class SitesHybrid {
    public static void main(String[] args) {
        // Cluster sites dengan tipe reputasi 4
        int typeReputation = 4;
        SitesClusterer clusterSite = new SitesClusterer(typeReputation);
        clusterSite.configARFFInstance(new String[]{"malware", "phishing", "spamming","normal"});
        System.out.println("Config ARFF Done");

        int numSitesMaxAllocation = 1000;
        for (int k=0;k<4;k++) {     // malware, phishing, spamming, normal
            List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
            for (int i = 0; i < numSitesMaxAllocation; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

                if (clusterSite.getListCombinationRecordType()[0] == true) {

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

                if (clusterSite.getListCombinationRecordType()[1] == true) {
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

                if (clusterSite.getListCombinationRecordType()[2] == true) {
                    fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
                    System.out.println("Trust WOT");
                }

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

                System.out.println("Situs ke-" + (i+1));
            }
        }

        // Pisahkan instances berdasarkan tipenya (malware / phishing / spamming / normal)
        Instances allInstancesRecordSite = clusterSite.getSiteReputationRecord();
        String fileNameInstancesStatic = "ratio_1111.num_" + numSitesMaxAllocation + ".type_" + typeReputation + ".staticdata.arff";
        String pathNameInstancesStatic = "database/weka/data_static/" + fileNameInstancesStatic;
        EksternalFile.saveInstanceWekaToExternalARFF(allInstancesRecordSite, pathNameInstancesStatic);

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
        int interval = 100;
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
            for (int j=1;j<=maxCluster;j++) {
                SimpleKMeans clusterKMeans = clusterSite.buildKmeansReputationModel(trainingRecordSites,j);
                // Classes to cluster evaluation
                ClusterEvaluation evalKMeans = clusterSite.evaluateClusterReputationModel(trainingRecordSites,clusterKMeans);
            }

            // Build cluster berdasarkan mixed instances kemudian langsung evaluasi (EM)
            for (int j=1;j<=maxCluster;j++) {
                EM clusterEM = clusterSite.buildEMReputationModel(trainingRecordSites, j);
                // Classes to cluster evaluation
                ClusterEvaluation evalEM = clusterSite.evaluateClusterReputationModel(trainingRecordSites,clusterEM);
            }
        }
    }
}
