package weka;

import Utils.Database.EksternalFile;
import data_structure.instance_ML.SiteRecordReputation;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.List;
import java.util.Random;

/**
 * Created by Dukun GaIB on 6/9/2016.
 */
public class SitesTester {
    public static void main(String[] args) {
        for (int l=1;l<=7;l++) {
            int typeReputation = 3;
            SitesLabeler labeledSiteDangerous = new SitesLabeler(typeReputation);
            labeledSiteDangerous.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
            SitesLabeler labeledSiteNormal = new SitesLabeler(typeReputation);
            labeledSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
            System.out.println("Config ARFF Done");

            // Iterate for malware, phishing, and spamming sites list
//            int numSitesEachType = 100;
//            for (int k = 0; k < 4; k++) {     // Phishing, Malware, Spamming, Normal
//                List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
//                int border = listSites.size() - numSitesEachType;
//                for (int i = (listSites.size() - 1); i >= border; i--) {
//                    // SET RECORD INSTANCE DATA STRUCTURE
//                    SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i), typeReputation);
//
//                    if (k < 3) {
//                        String classLabel = "";
//                        switch (k) {
//                            default:
//                            case 0:
//                                classLabel = "malware";
//                                break;
//                            case 1:
//                                classLabel = "phishing";
//                                break;
//                            case 2:
//                                classLabel = "spamming";
//                                break;
//                        }
//                        labeledSiteDangerous.fillDataIntoInstanceRecord(recordML, classLabel);
//                    }
//                    String classLabel2 = "";
//                    switch (k) {
//                        case 0:
//                        case 1:
//                        case 2:
//                            classLabel2 = "abnormal";
//                            break;
//                        default:
//                        case 3:
//                            classLabel2 = "normal";
//                            break;
//                    }
//                    labeledSiteNormal.fillDataIntoInstanceRecord(recordML, classLabel2);
//                    System.out.println("Situs ke-" + (i + 1));
//                }
//            }

//            Instances instancesNormalThisType = labeledSiteNormal.getSiteReputationRecord();
//            String fileNameStaticNormal = "numsites_" + numSitesEachType + ".type_" + typeReputation + ".normal.testdata.arff";
//            String pathNameStaticNormal = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\" + fileNameStaticNormal;
//            EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalThisType,pathNameStaticNormal);
//
//            Instances instancesDangerousThisType = labeledSiteDangerous.getSiteReputationRecord();
//            String fileNameStaticDangerous = "numsites_" + numSitesEachType + ".type_" + typeReputation + ".dangerous.testdata.arff";
//            String pathNameStaticDangerous = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\" + fileNameStaticDangerous;
//            EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousThisType,pathNameStaticDangerous);

            int optimumNumTrainingNormalType2 = 1000;
            String pathInstancesNormalType2 = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\numsites_100.type_" + typeReputation + ".normal.testdata.arff";
            Instances instancesNormalType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesNormalType2);
            instancesNormalType2.setClassIndex(instancesNormalType2.numAttributes()-1);
            String pathClassifierNormalType2 = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\model\\num_" + optimumNumTrainingNormalType2 + ".type_" + typeReputation + ".normalitySVM.model";
            Classifier supervisedClassifierNormalType2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierNormalType2);

            try {
                Evaluation evalNormalType2 = new Evaluation(instancesNormalType2);
                evalNormalType2.crossValidateModel(supervisedClassifierNormalType2,instancesNormalType2,10,new Random(1));
                System.out.println("Correctly Classified Instances Type 2 Stage 1 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalNormalType2,instancesNormalType2));
            } catch (Exception e) {
                e.printStackTrace();
            }

            int optimumNumTrainingDangerousType2 = 500;
            int optimumKNNType2 = 15;
            String pathInstancesDangerousType2 = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\numsites_100.type_" + typeReputation + ".dangerous.testdata.arff";
            Instances instancesDangerousType2 = EksternalFile.loadInstanceWekaFromExternalARFF(pathInstancesDangerousType2);
            instancesDangerousType2.setClassIndex(instancesDangerousType2.numAttributes()-1);
            String pathClassifierDangerousType2 = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\model\\num_" + optimumNumTrainingDangerousType2 + ".type_" + typeReputation + ".dangerousityKNN_" + optimumKNNType2 + ".model";
            Classifier supervisedClassifierDangerousType2 = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifierDangerousType2);

            try {
                Evaluation evalDangerousType2 = new Evaluation(instancesDangerousType2);
                evalDangerousType2.crossValidateModel(supervisedClassifierDangerousType2,instancesDangerousType2,10,new Random(1));
                System.out.println("Correctly Classified Instances Type 2 Stage 2 : " + SitesLabeler.getCorrectlyClassifiedInstances(evalDangerousType2,instancesDangerousType2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
