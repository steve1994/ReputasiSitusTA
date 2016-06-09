package weka;

import Utils.Database.EksternalFile;
import data_structure.instance_ML.SiteRecordReputation;
import weka.core.Instances;

import java.util.List;

/**
 * Created by Dukun GaIB on 6/9/2016.
 */
public class SitesTester {
    public static void main(String[] args) {
        for (int l=1;l<=7;l++) {
            int typeReputation = l;
            SitesLabeler labeledSiteDangerous = new SitesLabeler(typeReputation);
            labeledSiteDangerous.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
            SitesLabeler labeledSiteNormal = new SitesLabeler(typeReputation);
            labeledSiteNormal.configARFFInstance(new String[]{"normal", "abnormal"});
            System.out.println("Config ARFF Done");

            // Iterate for malware, phishing, and spamming sites list
            int numSitesEachType = 100;
            for (int k = 0; k < 4; k++) {     // Phishing, Malware, Spamming, Normal
                List<String> listSites = EksternalFile.loadSitesTrainingList(k + 1).getKey();
                int border = listSites.size() - numSitesEachType;
                for (int i = (listSites.size() - 1); i >= border; i--) {
                    // SET RECORD INSTANCE DATA STRUCTURE
                    SiteRecordReputation recordML = SitesMLProcessor.extractFeaturesFromDomain(listSites.get(i), typeReputation);

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
                        labeledSiteDangerous.fillDataIntoInstanceRecord(recordML, classLabel);
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
                    labeledSiteNormal.fillDataIntoInstanceRecord(recordML, classLabel2);
                    System.out.println("Situs ke-" + (i + 1));
                }
            }

            Instances instancesNormalThisType = labeledSiteNormal.getSiteReputationRecord();
            String fileNameStaticNormal = "numsites_" + numSitesEachType + ".type_" + typeReputation + ".normal.testdata.arff";
            String pathNameStaticNormal = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\" + fileNameStaticNormal;
            EksternalFile.saveInstanceWekaToExternalARFF(instancesNormalThisType,pathNameStaticNormal);

            Instances instancesDangerousThisType = labeledSiteDangerous.getSiteReputationRecord();
            String fileNameStaticDangerous = "numsites_" + numSitesEachType + ".type_" + typeReputation + ".dangerous.testdata.arff";
            String pathNameStaticDangerous = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\weka\\test\\" + fileNameStaticDangerous;
            EksternalFile.saveInstanceWekaToExternalARFF(instancesDangerousThisType,pathNameStaticDangerous);
        }
    }
}
