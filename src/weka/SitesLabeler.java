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
import weka.classifiers.functions.LibSVM;
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
public class SitesLabeler extends SitesMLProcessor {

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     *
     * @param typeReputation
     */
    public SitesLabeler(int typeReputation) {
        super(typeReputation);
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     *
     * @param classLabel
     */
    public void configARFFInstance(String[] classLabel) {
        List<Attribute> overallInstanceVector = super.getInstanceAttributes();
        // Add class label into this Instance
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
     *
     * @param recordReputation
     * @param classLabel
     */
    public void fillDataIntoInstanceRecord(SiteRecordReputation recordReputation, String classLabel) {
        List<Object> instanceValues = super.getInstanceRecord(recordReputation);
        instanceValues.add(classLabel);
        // Create new instance weka then insert it into siteReputationRecord
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
     * Build classifier from sitereputationrecord based on classifier type (1 : LibSVM, 2 : IBk)
     *
     * @param instances
     * @param classifierType
     * @param numNearestNeighbor
     * @return
     */
    public Classifier buildLabelReputationModel(Instances instances, int classifierType, int numNearestNeighbor) {
        Classifier classifier;
        switch (classifierType) {
            default:
            case 1:
                classifier = new LibSVM();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                classifier = new IBk(numNearestNeighbor);
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return classifier;
    }

    public static void main(String[] args) {
        // Labeled sites dengan tipe reputasi 3
        int typeReputation = 2;
        SitesLabeler labeledSite = new SitesLabeler(typeReputation);
        labeledSite.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
        SitesLabeler labeledSite2 = new SitesLabeler(typeReputation);
        labeledSite2.configARFFInstance(new String[]{"normal", "abnormal"});
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
        int numSitesEachType = 100;
        for (int k = 0; k < 4; k++) {     // Phishing, Malware, Spamming, Normal
            List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
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
                Trust_Feature fiturTrust = new Trust_Feature();
                if (labeledSite.getListCombinationRecordType()[2] == true) {
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

                if (k < 3) {
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
                    }
                    labeledSite.fillDataIntoInstanceRecord(recordML, classLabel);
                }
                String classLabel2 = "";
                switch (k) {
                    case 0:
                    case 1:
                    case 2:
                        classLabel2 = "abnormal";
                        break;
                    default:
                    case 3:
                        classLabel2 = "normal";
                        break;
                }
                labeledSite2.fillDataIntoInstanceRecord(recordML, classLabel2);

                System.out.println("Situs ke-" + i);
            }
        }

        // Pisahkan instances ke dalam golongan malware / phishing / spamming / normal
        Instances allInstancesRecordSite = labeledSite.getSiteReputationRecord();
        Instances allInstancesRecordSite2 = labeledSite2.getSiteReputationRecord();

        // Extract attributes from allInstancesRecordSite 1 (abnormal only (malware / phishing / spamming))
        FastVector instancesAttributes = new FastVector();
        Enumeration attributesRecordSite = allInstancesRecordSite.enumerateAttributes();
        while (attributesRecordSite.hasMoreElements()) {
            instancesAttributes.addElement((Attribute) attributesRecordSite.nextElement());
        }
        instancesAttributes.addElement(allInstancesRecordSite.classAttribute());
        // Extract attributes from allInstancesRecordSite 2 (normal and abnormal)
        FastVector instancesAttributes2 = new FastVector();
        Enumeration attributesRecordSite2 = allInstancesRecordSite2.enumerateAttributes();
        while (attributesRecordSite2.hasMoreElements()) {
            instancesAttributes2.addElement((Attribute) attributesRecordSite2.nextElement());
        }
        instancesAttributes2.addElement(allInstancesRecordSite2.classAttribute());

        // Divide allInstancesRecordSite based on site class (for allInstancesRecordSite 1)
        Instances malwareInstances = new Instances("malware_instances", instancesAttributes, 0);
        Instances phishingInstances = new Instances("phishing_instances", instancesAttributes, 0);
        Instances spammingInstances = new Instances("spamming_instances", instancesAttributes, 0);
        for (int i = 0; i < allInstancesRecordSite.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesRecordSite.instance(i).classValue();
            if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "malware") {
                malwareInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "phishing") {
                phishingInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance) == "spamming") {
                spammingInstances.add(allInstancesRecordSite.instance(i));
            }
        }
        // Divide allInstancesRecordSite based on site class (for allInstancesRecordSite 2)
        Instances normalInstances = new Instances("normal_instances", instancesAttributes2, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributes2, 0);
        for (int i = 0; i < allInstancesRecordSite2.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesRecordSite2.instance(i).classValue();
            if (allInstancesRecordSite2.classAttribute().value(indexClassThisInstance) == "normal") {
                normalInstances.add(allInstancesRecordSite2.instance(i));
            } else {
                abnormalInstances.add(allInstancesRecordSite2.instance(i));
            }
        }

        // Secara bertahap dari jumlah training 1-100 (iterasi 10), evaluasi hasil pembelajaran
        StringBuffer statisticEvaluationReport = new StringBuffer();
        int interval = 100;
        int numFoldCrossValidation = 10;
        for (int i = interval; i <= numSitesEachType; i = i + interval) {
            // Bentuk Training Record 1 Secara Bertahap (malware, phishing, dan spamming)
            Instances trainingRecordSites = new Instances("mixed_instances_1", instancesAttributes, 0);
            int numDangerousSites = i / 3;
            for (int j = 0; j < numDangerousSites; j++) {
                trainingRecordSites.add(malwareInstances.instance(j));
                trainingRecordSites.add(phishingInstances.instance(j));
                trainingRecordSites.add(spammingInstances.instance(j));
            }
            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes() - 1);
            // Bentuk Training Record 2 Secar Bertahap (normal, abnormal)
            Instances trainingRecordSites2 = new Instances("mixed_instances_2", instancesAttributes2, 0);
            for (int j = 0; j < i; j++) {
                trainingRecordSites2.add(normalInstances.instance(j));
                trainingRecordSites2.add(abnormalInstances.instance(j));
            }
            trainingRecordSites2.setClassIndex(trainingRecordSites2.numAttributes() - 1);

            // Tulis instance di eksternal file (training 1)
            String fileName = "num_" + i + ".type_" + typeReputation + ".dangerous_category.supervised.arff";
            String pathName = "database/weka/data/" + fileName;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites, pathName);
            // Tulis instance di eksternal file (training 2)
            String fileName2 = "num_" + i + ".type_" + typeReputation + ".normality_category.supervised.arff";
            String pathName2 = "database/weka/data/" + fileName2;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites2, pathName2);

            statisticEvaluationReport.append("\n\n=================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            // Evaluasi Hasil Pembelajaran Untuk Situs Normal / Tidak Normal
            Classifier normalityClassifier = labeledSite2.buildLabelReputationModel(trainingRecordSites2,1,0);
            try {
                // Cross Validation (Situs Normal / Tidak Normal)
                Evaluation evalLabeledSiteNormality = new Evaluation(trainingRecordSites2);
                evalLabeledSiteNormality.crossValidateModel(normalityClassifier, trainingRecordSites2, numFoldCrossValidation, new Random(1));
                statisticEvaluationReport.append(evalLabeledSiteNormality.toSummaryString("\nResults Cross-Validation for Normality\n\n", false));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Simpan hasil pembelajaran (classifier) untuk training data normal / tidak normal di eksternal file
            String normalFileName = "num_" + i + ".type_" + typeReputation + ".normalitySVM.model";
            String normalPathName = "database/weka/model/" + normalFileName;
            EksternalFile.saveClassifierToExternalModel(normalityClassifier,normalPathName);

            // Evaluasi Hasil Pembelajaran Untuk Kategori Situs Berbahaya (Malware / Phishing / Spamming)
            int numNearestNeighbor = 10;
            for (int j=1;j<=numNearestNeighbor;j++) {
                Classifier dangerousityClassifier = labeledSite.buildLabelReputationModel(trainingRecordSites,2,j);
                try {
                    // Cross Validation (Situs Malware / Phishing / Spamming)
                    Evaluation evalLabeledSiteDangerousity = new Evaluation(trainingRecordSites);
                    evalLabeledSiteDangerousity.crossValidateModel(dangerousityClassifier, trainingRecordSites, numFoldCrossValidation, new Random(1));
                    statisticEvaluationReport.append(evalLabeledSiteDangerousity.toSummaryString("\nResults Cross-Validation for Dangerousity with nearest neighbor value : " + j + "\n\n", false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Simpan hasil pembelajaran (classifier) untuk training data
                // situs berbahaya malware / phishing / spamming di eksternal file
                String dangerousFileName = "num_" + i + ".type_" + typeReputation + ".dangerousityKNN_" + j + ".model";
                String dangerousPathName = "database/weka/model/" + dangerousFileName;
                EksternalFile.saveClassifierToExternalModel(dangerousityClassifier,dangerousPathName);
            }
        }
        // Write evaluation statistic result
        String fileNameEvaluation = "evaluationStatisticSupervisedLearning.type_" + typeReputation + ".txt";
        String pathNameEvaluation = "database/weka/statistic/" + fileNameEvaluation;
        EksternalFile.saveRawContentToEksternalFile(statisticEvaluationReport.toString(), pathNameEvaluation);
    }
}

