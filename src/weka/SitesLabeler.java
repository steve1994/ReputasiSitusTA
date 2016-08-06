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

import java.util.*;

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

    /**
     * Get incorrectly classified instances given the instances and classifier
     * @param allInstances
     * @param classifier
     * @return
     */
    public Pair<Instances,Instances> distinguishingCorrectIncorrectInstances(Instances allInstances, Classifier classifier) {
        Instances correctlyClassifyInstance = new Instances("correct_instances",SitesMLProcessor.getAttributesVector(allInstances),0);
        Instances incorrectlyClassifyInstance = new Instances("incorrect_instances",SitesMLProcessor.getAttributesVector(allInstances),0);
        allInstances.setClassIndex(allInstances.numAttributes()-1);

        for (int i=0;i<allInstances.numInstances();i++) {
            Instance thisInstance = allInstances.instance(i);
            int actualClassValue = (int) thisInstance.classValue();
            try {
                int predictedClassValue = (int) classifier.classifyInstance(thisInstance);
                if (actualClassValue == predictedClassValue) {
                    correctlyClassifyInstance.add(thisInstance);
                } else {
                    incorrectlyClassifyInstance.add(thisInstance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new Pair<Instances,Instances>(correctlyClassifyInstance,incorrectlyClassifyInstance);
    }

    /**
     * Get correctly classified instances from evalClassifier and testSet Instances
     * @param evalClassifier
     * @param testSet
     * @return
     */
    public static double getCorrectlyClassifiedInstances(Evaluation evalClassifier, Instances testSet) {
        return (evalClassifier.correct() / (double) testSet.numInstances()) * 100;
    }

    public static void main(String[] args) {
        // Labeled sites dengan tipe reputasi 3
        int typeReputation = 7;
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
        int numSitesEachType = 1000;
//        for (int k = 0; k < 4; k++) {     // Phishing, Malware, Spamming, Normal
//            List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
//            for (int i = 0; i < numSitesEachType; i++) {
//
//                // TIME LOGGER SET
////                listTimeTLDRatioAS.add(afterTLDRatio - beforeDNS);
////                listTimeHitRatioAS.add(afterHitRatio - afterTLDRatio);
////                listTimeNSDistAS.add(afterNSDist - afterHitRatio);
////                listTimeNSCount.add(afterNSCount - afterNSDist);
////                listTimeTTLNS.add(afterTTLNS - afterNSCount);
////                listTimeTTLIP.add(afterTTLIP - afterTTLNS);
////                listTimeTokenCount.add(afterTokenCount - beforeSpesific);
////                listTimeAvgToken.add(afterAvgToken - afterTokenCount);
////                listTimeSLDRatio.add(afterSLDRatio - afterAvgToken);
////                listTimeInboundLink.add(afterInboundLink - afterSLDRatio);
////                listTimeLookupTime.add(afterLookupTime - afterInboundLink);
////                listTimeTrust.add(afterTrust - beforeTrust);
//
//                // SET RECORD INSTANCE DATA STRUCTURE
//                SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i),typeReputation);
//
//                if (k < 3) {
//                    String classLabel = "";
//                    switch (k) {
//                        default:
//                        case 0:
//                            classLabel = "malware";
//                            break;
//                        case 1:
//                            classLabel = "phishing";
//                            break;
//                        case 2:
//                            classLabel = "spamming";
//                            break;
//                    }
//                    labeledSite.fillDataIntoInstanceRecord(recordML, classLabel);
//                }
//                String classLabel2 = "";
//                switch (k) {
//                    case 0:
//                    case 1:
//                    case 2:
//                        classLabel2 = "abnormal";
//                        break;
//                    default:
//                    case 3:
//                        classLabel2 = "normal";
//                        break;
//                }
//                labeledSite2.fillDataIntoInstanceRecord(recordML, classLabel2);
//
//                System.out.println("Situs ke-" + (i+1));
//            }
//        }
//
//        // Pisahkan instances ke dalam golongan malware / phishing / spamming / normal
//        Instances allInstancesRecordSite = labeledSite.getSiteReputationRecord();
//        String fileNameStatic = "numsites_" + numSitesEachType + ".ratio_3111.type_" + typeReputation + ".dangerous.staticdata.arff";
//        String pathNameStatic = "database/weka/data_static/" + fileNameStatic;
//        EksternalFile.saveInstanceWekaToExternalARFF(allInstancesRecordSite,pathNameStatic);
//
//        Instances allInstancesRecordSite2 = labeledSite2.getSiteReputationRecord();
//        String fileNameStatic2 = "numsites_" + numSitesEachType + ".ratio_3111.type_" + typeReputation + ".normal.staticdata.arff";
//        String pathNameStatic2 = "database/weka/data_static/" + fileNameStatic2;
//        EksternalFile.saveInstanceWekaToExternalARFF(allInstancesRecordSite2,pathNameStatic2);

        Instances allInstancesRecordSite2 = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".normal.staticdata.arff");
        allInstancesRecordSite2.setClassIndex(allInstancesRecordSite2.numAttributes()-1);
        Instances allInstancesRecordSite = EksternalFile.loadInstanceWekaFromExternalARFF("database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".dangerous.staticdata.arff");
        allInstancesRecordSite.setClassIndex(allInstancesRecordSite.numAttributes()-1);

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
            if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance).equals("malware")) {
                malwareInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance).equals("phishing")) {
                phishingInstances.add(allInstancesRecordSite.instance(i));
            } else if (allInstancesRecordSite.classAttribute().value(indexClassThisInstance).equals("spamming")) {
                spammingInstances.add(allInstancesRecordSite.instance(i));
            }
        }
        // Divide allInstancesRecordSite based on site class (for allInstancesRecordSite 2)
        Instances normalInstances = new Instances("normal_instances", instancesAttributes2, 0);
        Instances abnormalInstances = new Instances("abnormal_instances", instancesAttributes2, 0);
        for (int i = 0; i < allInstancesRecordSite2.numInstances(); i++) {
            int indexClassThisInstance = (int) allInstancesRecordSite2.instance(i).classValue();
            if (allInstancesRecordSite2.classAttribute().value(indexClassThisInstance).equals("normal")) {
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
            int numNearestNeighbor = (int) Math.sqrt(i);

            // Bentuk Training Record 1 Secara Bertahap (malware, phishing, dan spamming)
            Instances trainingRecordSites = new Instances("mixed_instances_1", instancesAttributes, 0);
            int numDangerousSites = i / 3;
            for (int j = 0; j < numDangerousSites; j++) {
                trainingRecordSites.add(malwareInstances.instance(j));
                trainingRecordSites.add(phishingInstances.instance(j));
                trainingRecordSites.add(spammingInstances.instance(j));
            }
            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes() - 1);
            // Bentuk Training Record 2 Secara Bertahap (normal, abnormal)
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
//            Instances trainingRecordSites = EksternalFile.loadInstanceWekaFromExternalARFF(pathName);
//            trainingRecordSites.setClassIndex(trainingRecordSites.numAttributes()-1);

            // Tulis instance di eksternal file (training 2)
            String fileName2 = "num_" + i + ".type_" + typeReputation + ".normality_category.supervised.arff";
            String pathName2 = "database/weka/data/" + fileName2;
            EksternalFile.saveInstanceWekaToExternalARFF(trainingRecordSites2, pathName2);
//            Instances trainingRecordSites2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathName2);
//            trainingRecordSites2.setClassIndex(trainingRecordSites2.numAttributes()-1);

            statisticEvaluationReport.append("\n\n=================================================================\n\n NUM SITES TRAINING : " + i + "\n\n");

            // Evaluasi Hasil Pembelajaran Untuk Situs Normal / Tidak Normal
            Classifier normalityClassifier = labeledSite2.buildLabelReputationModel(trainingRecordSites2,1,0);
            try {
                // Cross Validation (Situs Normal / Tidak Normal)
                Evaluation evalLabeledSiteNormality = new Evaluation(trainingRecordSites2);
                evalLabeledSiteNormality.crossValidateModel(normalityClassifier, trainingRecordSites2, numFoldCrossValidation, new Random(1));
                statisticEvaluationReport.append("\nResults Cross-Validation for Normality : " + SitesLabeler.getCorrectlyClassifiedInstances(evalLabeledSiteNormality,trainingRecordSites2) + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Simpan hasil pembelajaran (classifier) untuk training data normal / tidak normal di eksternal file
            String normalFileName = "num_" + i + ".type_" + typeReputation + ".normalitySVM.model";
            String normalPathName = "database/weka/model/" + normalFileName;
            EksternalFile.saveClassifierToExternalModel(normalityClassifier,normalPathName);

            // Evaluasi Hasil Pembelajaran Untuk Kategori Situs Berbahaya (Malware / Phishing / Spamming)
            for (int j=1;j<=numNearestNeighbor;j=j+2) {
                Classifier dangerousityClassifier = labeledSite.buildLabelReputationModel(trainingRecordSites,2,j);
                try {
                    // Cross Validation (Situs Malware / Phishing / Spamming)
                    Evaluation evalLabeledSiteDangerousity = new Evaluation(trainingRecordSites);
                    evalLabeledSiteDangerousity.crossValidateModel(dangerousityClassifier, trainingRecordSites, numFoldCrossValidation, new Random(1));
                    statisticEvaluationReport.append("\nResults Cross-Validation for Dangerousity with nearest neighbor " + j + " : " + SitesLabeler.getCorrectlyClassifiedInstances(evalLabeledSiteDangerousity,trainingRecordSites) + "\n");
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

