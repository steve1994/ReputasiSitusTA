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
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

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
                classifier = new IBk();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                classifier = new NaiveBayes();
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
        // Labeled sites dengan tipe reputasi 2
        int typeReputation = 2;
        SitesLabeler labeledSite = new SitesLabeler(typeReputation);
        labeledSite.configARFFInstance(new String[] {"malware","phishing","spamming"});
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

        // Iterate for malware, phishing, and spamming sites list
        int numSitesEachType = 50;
        for (int k=0;k<3;k++) {     // Phishing, Malware, Spamming
            List<String> listSites = EksternalFile.loadSitesTrainingList(k+1).getKey();
            for (int i = 0; i < numSitesEachType; i++) {
                // DNS FEATURES
                DNS_Feature fiturDNS = new DNS_Feature();

                if (labeledSite.getListCombinationRecordType()[0] == true) {
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

                if (labeledSite.getListCombinationRecordType()[1] == true) {
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
                long beforeTrust = System.currentTimeMillis();

                Trust_Feature fiturTrust = WOT_API_Loader.loadAPIWOTForSite(listSites.get(i));
                System.out.println("Trust WOT");

                long afterTrust = System.currentTimeMillis();

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
                    default :
                    case 0  :   classLabel = "malware";break;
                    case 1  :   classLabel = "phishing";break;
                    case 2  :   classLabel = "spamming";break;
                }
                labeledSite.fillDataIntoInstanceRecord(recordML,classLabel);

                System.out.println("Situs ke-" + i);
            }
        }

        // Pisahkan instances ke dalam golongan malware / phishing / spamming
        Instances allInstancesRecordSite = labeledSite.getSiteReputationRecord();
        // Extract attributes from allInstancesRecordSite
        Enumeration attributesRecordSite = allInstancesRecordSite.enumerateAttributes();
        FastVector instancesAttributes = new FastVector();
        while (attributesRecordSite.hasMoreElements()) {
            instancesAttributes.addElement((Attribute) attributesRecordSite.nextElement());
        }
        instancesAttributes.addElement(allInstancesRecordSite.classAttribute());
        // Divide allInstancesRecordSite based on site class
        Instances malwareInstances = new Instances("malware_instances",instancesAttributes,0);
        Instances phishingInstances = new Instances("phishing_instances",instancesAttributes,0);
        Instances spammingInstances = new Instances("spamming_instances",instancesAttributes,0);
        for (int i=0;i<allInstancesRecordSite.numInstances();i++) {
            int indexClassThisInstance = (int) allInstancesRecordSite.instance(i).classValue();
            if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "malware") {
                malwareInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "phishing") {
                phishingInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "spamming") {
                spammingInstances.add(allInstancesRecordSite.instance(i));
            }
        }

        // Secara bertahap dari jumlah training 1-100 (iterasi 10), evaluasi hasil pembelajaran
        StringBuffer statisticEvaluationReport = new StringBuffer();
        for (int i = 5; i <= numSitesEachType; i=i+5) {
            // Bentuk Training Record Secara Bertahap
            Instances trainingRecordSites = new Instances("mixed_instances", instancesAttributes, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSites.add(malwareInstances.instance(j));
                trainingRecordSites.add(phishingInstances.instance(j));
                trainingRecordSites.add(spammingInstances.instance(j));
            }
            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes() - 1);
            System.out.println("TRAINING RECORD SITES ATTR NUM : " + trainingRecordSites.numAttributes());
            System.out.println("NUMBER INSTANCES : " + trainingRecordSites.numInstances());
            // Tulis instance di eksternal file
            String fileName = "num_" + i + ".type_" + typeReputation + ".supervised.arff";
            String pathName = "database/weka/" + fileName;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites, pathName);
            // Evaluasi Hasil Pembelajaran
            statisticEvaluationReport.append("===========================================================\n\nNUM SITES : " + i + "\n\n");
            for (int k = 0; k < 3; k++) {     // Algoritma IBk, Naive Bayes, J48
                statisticEvaluationReport.append("\nAlgoritma Pembelajaran Supervised Learning ke-" + (k + 1) + " : \n\n");
                Classifier siteClassifier = labeledSite.buildLabelReputationModel(trainingRecordSites, (k + 1));
                try {
                    // Full Training
                    Evaluation evalLabeledSite1 = new Evaluation(trainingRecordSites);
                    evalLabeledSite1.evaluateModel(siteClassifier, trainingRecordSites);
                    statisticEvaluationReport.append(evalLabeledSite1.toSummaryString("\nResults Full-Training\n\n", false));
                    // Cross Validation
                    int numFold = i / 2;
                    Evaluation evalLabeledSite2 = new Evaluation(trainingRecordSites);
                    evalLabeledSite2.crossValidateModel(siteClassifier, trainingRecordSites, numFold, new Random(1));
                    statisticEvaluationReport.append(evalLabeledSite2.toSummaryString("\nResults Cross-Validation\n\n", false));
                    // Test Set Validation (ambil 10 situs terakhir di daftar phishing / spamming / malware)
//                List<String> listSitesTest = new ArrayList<String>();
//                for (int j=0;j<3;j++) {
//                    List<String> listSitesThisType = EksternalFile.loadSitesTrainingList(i+1).getKey();
//                    for (int k=listSitesThisType.size()-numSitesEachType; k<listSitesThisType.size(); k++) {
//                        listSitesTest.add(listSitesThisType.get(k));
//                    }
//                }
//                Evaluation evalLabeledSite3 = new Evaluation(labeledSite.getSiteReputationRecord());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // Write evaluation statistic result
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(),"database/weka/evaluationStatisticSupervisedLearning.txt");
    }
}
