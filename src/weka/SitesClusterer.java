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

    public Instances getSiteReputationRecord() {
        return siteReputationRecord;
    }

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

    public static void main(String[] args) {
        // Load tipe site ke-2 (phishing)
       //List<String> listSites = EksternalFile.loadSitesTrainingList(3).getKey();
        // Cluster sites dengan tipe reputasi 7 dan jumlah cluster 4
        SitesClusterer clusterSite = new SitesClusterer(7,4);

        clusterSite.configARFFInstance();
        System.out.println("Config ARFF Done");

        long before = System.currentTimeMillis();
        for (int i=0;i<1;i++) {
            // DNS FEATURES
            DNS_Feature fiturDNS = new DNS_Feature();
            // TLD ratio
            Sextet<Double,Double,Double,Double,Double,Double> TLDRatio = DNSExtractor.getTLDDistributionFromAS("0000love.net");
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
            for (int j=0;j<3;j++) {
                HitRatioList[j] = DNSExtractor.getHitASRatio("0000love.net",j+1);
            }
            fiturDNS.setHitASRatio(HitRatioList);
            System.out.println("Hit AS Ratio");
            // Name server distribution AS
            fiturDNS.setDistributionNSAS(DNSExtractor.getDistributionNSFromAS("0000love.net"));
            System.out.println("Name Server Distribution AS");
            // Name server count
            fiturDNS.setNumNameServer(DNSExtractor.getNumNameServers("0000love.net"));
            System.out.println("Name Server Count");
            // TTL Name Servers
            fiturDNS.setListNSTTL(DNSExtractor.getNameServerTimeToLive("0000love.net"));
            System.out.println("TTL Name Servers");
            // TTL DNS A Records
            fiturDNS.setListDNSRecordTTL(DNSExtractor.getDNSRecordTimeToLive("0000love.net"));
            System.out.println("TTL DNS Record");

            // SPESIFIC FEATURES
            Spesific_Feature fiturSpesific = new Spesific_Feature();
            // Token Count URL
            fiturSpesific.setTokenCountURL(ContentExtractor.getDomainTokenCountURL("0000love.net"));
            System.out.println("Token Count URL");
            // Average Token Length URL
            fiturSpesific.setAverageTokenLengthURL(ContentExtractor.getAverageDomainTokenLengthURL("0000love.net"));
            System.out.println("Average Token Length URL");
            // SLD ratio from URL (malware, phishing, spamming)
            double[] SLDRatioList = new double[3];
            for (int j=0;j<3;j++) {
                SLDRatioList[j] = ContentExtractor.getSLDHitRatio("0000love.net",j+1);
            }
            fiturSpesific.setSLDRatio(SLDRatioList);
            System.out.println("SLD Ratio List");
            // Inbound link Approximation (Google, Yahoo, Bing)
            int[] inboundLinkAppr = new int[3];
            for (int j=0;j<3;j++) {
                inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults("0000love.net",j+1);
            }
            fiturSpesific.setInboundLink(inboundLinkAppr);
            System.out.println("Inbound Link Approximation");
            // Lookup time to access site
            fiturSpesific.setLookupTime(ContentExtractor.getDomainLookupTimeSite("0000love.net"));
            System.out.println("Lookup Time");

            // TRUST FEATURES
            Trust_Feature fiturTrust = WOT_API_Loader.loadAPIWOTForSite("0000love.net");
            System.out.println("Trust WOT");

            // SET RECORD INSTANCE DATA STRUCTURE
            SiteRecordReputation recordML = new SiteRecordReputation();
            recordML.setDNSRecordFeature(fiturDNS);
            recordML.setSpesificRecordFeature(fiturSpesific);
            recordML.setTrustRecordFeature(fiturTrust);
            clusterSite.fillDataIntoInstanceRecord(recordML);

            System.out.println("Situs ke-" + i);
        }

        long after = System.currentTimeMillis();
        System.out.println("Instance Filling Payload : " + (after-before) + " ms");

        // Tulis instance di eksternal file
        EksternalFile.saveInstanceWekaToExternalARFF(clusterSite.getSiteReputationRecord());

        long after_again = System.currentTimeMillis();
        System.out.println("Write ARFF Payload : " + (after_again-after) + " ms");
        // Build Cluster
      //  clusterSite.buildKmeansReputationModel(clusterSite.getSiteReputationRecord(),5);
    }
}
