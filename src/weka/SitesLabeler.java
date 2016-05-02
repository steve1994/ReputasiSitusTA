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
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 29/03/2016.
 */
public class SitesLabeler extends SitesMLProcessor{

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     * @param typeReputation
     */
    public SitesLabeler(int typeReputation) {
        super(typeReputation);
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     * @param classLabel
     */
    public void configARFFInstance(String[] classLabel) {
        List<Attribute> overallInstanceVector = super.getInstanceAttributes();
        // Add class label into this Instance
        FastVector siteLabel = new FastVector();
        for (String label : classLabel) {
            siteLabel.addElement(label);
        }
        Attribute siteLabelNominal = new Attribute("class",siteLabel);
        overallInstanceVector.add(siteLabelNominal);
        // Setting Attributes Vector Overall to Instance Record
        FastVector attributeInstanceRecord = new FastVector();
        for (Attribute attr : overallInstanceVector) {
            attributeInstanceRecord.addElement(attr);
        }
        siteReputationRecord = new Instances("Reputation Site Dataset",attributeInstanceRecord,0);
        siteReputationRecord.setClassIndex(siteReputationRecord.numAttributes()-1);
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
        double[] values = new double[instanceValues.size()];
        for (int i=0;i<instanceValues.size();i++) {
            if (i < instanceValues.size()-1) {
                values[i] = new Double(instanceValues.get(i).toString());
            } else {
                if (i == instanceValues.size()-1) {
                    Instances currentInstances = getSiteReputationRecord();
                    values[i] =  currentInstances.attribute(currentInstances.numAttributes()-1).indexOfValue(classLabel);
                }
            }
        }
        Instance instance = new Instance(1.0,values);
        siteReputationRecord.add(instance);
    }

    /**
     * Build classifier from sitereputationrecord based on classifier type (1 : NaiveBayes, 2 : ID3, 3 : J48)
     * @param instances
     * @param classifierType
     * @return
     */
    public Classifier buildLabelReputationModel(Instances instances, int classifierType) {
        Classifier classifier;
        switch (classifierType) {
            default:
            case 1:
                classifier = new NaiveBayes();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                classifier = new Id3();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                classifier = new J48();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return classifier;
    }

    public static void main (String[] args){
        // Fill list sites type 1 (malware), 2 (phishing), 3 (spamming)
//        int numSitesEachType = 10;
//        List<String> listSites = new ArrayList<String>();
//        for (int i=0;i<3;i++) {
//            List<String> listSitesThisType = EksternalFile.loadSitesTrainingList(i+1).getKey();
//            for (int j=0; j<numSitesEachType; j++) {
//                listSites.add(listSitesThisType.get(j));
//            }
//        }

        // Labeled sites dengan tipe reputasi 7 4
        SitesLabeler labeledSite = new SitesLabeler(7);
        labeledSite.configARFFInstance(new String[] {"malware","phishing","spamming"});
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

        int numSitesEachType = 5;
        for (int k=0;k<3;k++) {
            List<String> listSites = EksternalFile.loadSitesTrainingList(k+1).getKey();
            for (int i = 0; i < numSitesEachType; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

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

                // TRUST FEATURES
                long beforeTrust = System.currentTimeMillis();

                Trust_Feature fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
                System.out.println("Trust WOT");

                long afterTrust = System.currentTimeMillis();

                // TIME LOGGER SET
                listTimeTLDRatioAS.add(afterTLDRatio - beforeDNS);
                listTimeHitRatioAS.add(afterHitRatio - afterTLDRatio);
                listTimeNSDistAS.add(afterNSDist - afterHitRatio);
                listTimeNSCount.add(afterNSCount - afterNSDist);
                listTimeTTLNS.add(afterTTLNS - afterNSCount);
                listTimeTTLIP.add(afterTTLIP - afterTTLNS);
                listTimeTokenCount.add(afterTokenCount - beforeSpesific);
                listTimeAvgToken.add(afterAvgToken - afterTokenCount);
                listTimeSLDRatio.add(afterSLDRatio - afterAvgToken);
                listTimeInboundLink.add(afterInboundLink - afterSLDRatio);
                listTimeLookupTime.add(afterLookupTime - afterInboundLink);
                listTimeTrust.add(afterTrust - beforeTrust);

                // SET RECORD INSTANCE DATA STRUCTURE
                SiteRecordReputation recordML = new SiteRecordReputation();
                recordML.setDNSRecordFeature(fiturDNS);
                recordML.setSpesificRecordFeature(fiturSpesific);
                recordML.setTrustRecordFeature(fiturTrust);
                String classLabel = "";
                switch (k) {
                    default :
                    case 0  :   classLabel = "malware";break;
                    case 1  :   classLabel = "phishing";break;
                    case 2  :   classLabel = "spamming";break;
                }
                labeledSite.fillDataIntoInstanceRecord(recordML,classLabel);

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

        // Tulis instance di eksternal file
        EksternalFile.saveInstanceWekaToExternalARFF(labeledSite.getSiteReputationRecord());

        // Tulis statistik non null data tiap attribute terlibat
//        List<Pair<String,Double>> percentageNotNullData = labeledSite.getPercentageNotNullData(labeledSite.getSiteReputationRecord());
//        for (Pair<String,Double> percent : percentageNotNullData) {
//            System.out.println("Attribute Name : " + percent.getValue0() + "  Percentage not null data : " + percent.getValue1());
//        }

        // Build and Evaluate Classifier (1 : NaiveBayes, 2 : ID3, 3 : J48)
        Classifier siteClassifier = labeledSite.buildLabelReputationModel(labeledSite.getSiteReputationRecord(), 1);
        try {
            Evaluation evalLabeledSite = new Evaluation(labeledSite.getSiteReputationRecord());
            evalLabeledSite.evaluateModel(siteClassifier,labeledSite.getSiteReputationRecord());
            System.out.println(evalLabeledSite.toSummaryString("\nResults Full-Training\n\n", false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
