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
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 26/03/2016.
 */
public class SitesClusterer extends SitesMLProcessor{
    private int numCluster = 0;

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     * @param numCluster
     */
    public SitesClusterer(int typeReputation, int numCluster) {
        super(typeReputation);
        this.numCluster = numCluster;
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     */
    public void configARFFInstance() {
        List<Attribute> overallInstanceVector = super.getInstanceAttributes();
        // Setting Attributes Vector Overall to Instance Record
        FastVector attributeInstanceRecord = new FastVector();
        for (Attribute attr : overallInstanceVector) {
            attributeInstanceRecord.addElement(attr);
        }
        siteReputationRecord = new Instances("Reputation Site Dataset",attributeInstanceRecord,0);
    }

    /**
     * Insert new reputation record into instances
     * Assumption : instances have been set properly
     * @param recordReputation
     */
    public void fillDataIntoInstanceRecord(SiteRecordReputation recordReputation) {
        List<Object> instanceValues = super.getInstanceRecord(recordReputation);
        // Create new instance weka then insert it into siteReputationRecord
        double[] values = new double[instanceValues.size()];
        for (int i=0;i<instanceValues.size();i++) {
            values[i] = new Double(instanceValues.get(i).toString());
        }
        Instance instance = new Instance(1.0,values);

        siteReputationRecord.add(instance);
    }

    /**
     * Build cluster from site record in instances weka
     * @param instances
     * @param maxIteration
     * @return
     */
    public SimpleKMeans buildKmeansReputationModel(Instances instances, int maxIteration) {
        SimpleKMeans siteReputationCluster = new SimpleKMeans();
        siteReputationCluster.setPreserveInstancesOrder(true);
        try {
            siteReputationCluster.setMaxIterations(maxIteration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            siteReputationCluster.setNumClusters(numCluster);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            siteReputationCluster.buildClusterer(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return siteReputationCluster;
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
        // Load tipe site ke-2 (phishing)
        int typeList = 1;
        List<String> listSites = EksternalFile.loadSitesTrainingList(typeList).getKey();

        // Cluster sites dengan tipe reputasi 7 dan jumlah cluster 4
        int typeReputation = 7;
        int numCluster = 4;
        SitesClusterer clusterSite = new SitesClusterer(typeReputation,numCluster);
        clusterSite.configARFFInstance();
        System.out.println("Config ARFF Done");

        // Time performance logger
        List<Long> listTimeTLDRatioAS = new ArrayList<Long>();
        List<Long> listTimeHitRatioAS = new ArrayList<Long>();
        List<Long> listTimeNSDistAS = new ArrayList<Long>();
        List<Long> listTimeNSCount = new ArrayList<Long>();
        List<Long> listTimeTTLNS = new ArrayList<Long>();
        List<Long> listTimeTTLIP = new ArrayList<Long>();
        List<Long> listTimeTokenCount = new ArrayList<Long>();
        List<Long> listTimeAvgToken = new ArrayList<Long>();
        List<Long> listTimeSLDRatio = new ArrayList<Long>();
        List<Long> listTimeInboundLink = new ArrayList<Long>();
        List<Long> listTimeLookupTime = new ArrayList<Long>();
        List<Long> listTimeTrust = new ArrayList<Long>();

        for (int i=0;i<10;i++) {
            // DNS FEATURES
            DNS_Feature fiturDNS = new DNS_Feature();

            long beforeDNS = System.currentTimeMillis();

            // TLD ratio
            Sextet<Double,Double,Double,Double,Double,Double> TLDRatio = DNSExtractor.getTLDDistributionFromAS(listSites.get(i));
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
            for (int j=0;j<3;j++) {
                HitRatioList[j] = DNSExtractor.getHitASRatio(listSites.get(i),j+1);
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

            // SPESIFIC FEATURES
            Spesific_Feature fiturSpesific = new Spesific_Feature();

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
            for (int j=0;j<3;j++) {
                SLDRatioList[j] = ContentExtractor.getSLDHitRatio(listSites.get(i),j+1);
            }
            fiturSpesific.setSLDRatio(SLDRatioList);
            System.out.println("SLD Ratio List");

            long afterSLDRatio = System.currentTimeMillis();

            // Inbound link Approximation (Google, Yahoo, Bing)
            int[] inboundLinkAppr = new int[3];
            for (int j=0;j<3;j++) {
                inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults(listSites.get(i),j+1);
            }
            fiturSpesific.setInboundLink(inboundLinkAppr);
            System.out.println("Inbound Link Approximation");

            long afterInboundLink = System.currentTimeMillis();

            // Lookup time to access site
            fiturSpesific.setLookupTime(ContentExtractor.getDomainLookupTimeSite(listSites.get(i)));
            System.out.println("Lookup Time");

            long afterLookupTime = System.currentTimeMillis();

            // TRUST FEATURES
            long beforeTrust = System.currentTimeMillis();

            Trust_Feature fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
            System.out.println("Trust WOT");

            long afterTrust = System.currentTimeMillis();

            // TIME LOGGER SET
            listTimeTLDRatioAS.add(afterTLDRatio-beforeDNS);
            listTimeHitRatioAS.add(afterHitRatio-afterTLDRatio);
            listTimeNSDistAS.add(afterNSDist-afterHitRatio);
            listTimeNSCount.add(afterNSCount-afterNSDist);
            listTimeTTLNS.add(afterTTLNS-afterNSCount);
            listTimeTTLIP.add(afterTTLIP-afterTTLNS);
            listTimeTokenCount.add(afterTokenCount-beforeSpesific);
            listTimeAvgToken.add(afterAvgToken-afterTokenCount);
            listTimeSLDRatio.add(afterSLDRatio-afterAvgToken);
            listTimeInboundLink.add(afterInboundLink-afterSLDRatio);
            listTimeLookupTime.add(afterLookupTime-afterInboundLink);
            listTimeTrust.add(afterTrust-beforeTrust);

            // SET RECORD INSTANCE DATA STRUCTURE
            SiteRecordReputation recordML = new SiteRecordReputation();
            recordML.setDNSRecordFeature(fiturDNS);
            recordML.setSpesificRecordFeature(fiturSpesific);
            recordML.setTrustRecordFeature(fiturTrust);
            clusterSite.fillDataIntoInstanceRecord(recordML);

            System.out.println("Situs ke-" + i);
        }

        System.out.println("FITUR DNS : ");
        System.out.println("Avg Time TLD Ratio AS : " + getAverageListLong(listTimeTLDRatioAS) + " ms");
        System.out.println("Avg Time Hit Ratio AS : " + getAverageListLong(listTimeHitRatioAS) + " ms");
        System.out.println("Avg Time NS Distribution AS : " + getAverageListLong(listTimeNSDistAS) + " ms");
        System.out.println("Avg Time NS Count : " + getAverageListLong(listTimeNSCount) + " ms");
        System.out.println("Avg Time TTL NS : " + getAverageListLong(listTimeTTLNS) + " ms");
        System.out.println("Avg Time TTL IP : " + getAverageListLong(listTimeTTLIP) + " ms");
        System.out.println("FITUR SPESIFIK : ");
        System.out.println("Avg Time Token Count : " + getAverageListLong(listTimeTokenCount) + " ms");
        System.out.println("Avg Time Avg Token : " + getAverageListLong(listTimeAvgToken) + " ms");
        System.out.println("Avg Time SLD Ratio : " + getAverageListLong(listTimeSLDRatio) + " ms");
        System.out.println("Avg Time Inbound Link : " + getAverageListLong(listTimeInboundLink) + " ms");
        System.out.println("Avg Time Lookup Time : " + getAverageListLong(listTimeLookupTime) + " ms");
        System.out.println("FITUR TRUST : ");
        System.out.println("Avg Time Trust : " + getAverageListLong(listTimeTrust) + " ms");

        // Tulis instance di eksternal file
        String fileName = "type_" + typeReputation + ".numcluster_" + numCluster + ".unsupervised.txt";
        String pathName = "database/weka/" + fileName;
        EksternalFile.saveInstanceWekaToExternalARFF(clusterSite.getSiteReputationRecord(),pathName);
        // Tulis statistik non null data tiap attribute terlibat
        List<Pair<String,Double>> percentageNotNullData = clusterSite.getPercentageNotNullData(clusterSite.getSiteReputationRecord());
        for (Pair<String,Double> percent : percentageNotNullData) {
            System.out.println("Attribute Name : " + percent.getValue0() + "  Percentage not null data : " + percent.getValue1());
        }

        // Build Cluster
      //  clusterSite.buildKmeansReputationModel(clusterSite.getSiteReputationRecord(),5);
    }
}
