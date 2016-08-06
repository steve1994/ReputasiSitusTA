package Utils.Database;

import Utils.Converter;
import Utils.Spesific.ContentExtractor;
import javafx.util.Pair;
import sample.StaticVars;
import weka.SitesLabeler;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by steve on 28/01/2016.
 */
public class EksternalFile {

    private static final String malwarePath = "database/malware_websites/hosts.txt";
//    private static final String malwarePath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\malware_websites\\hosts.txt";
    private static final String phishingPath = "database/phishing_websites/phishing.txt";
//    private static final String phishingPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\phishing_websites\\phishing.txt";
    private static final String spammingPath = "database/spamming_websites/spamming.txt";
//    private static final String spammingPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\spamming_websites\\spamming.txt";
//    private static final String popularPath = "database/top_popular_websites/top-1m.csv";
    private static final String popularPath = "database/top_popular_websites/top-1m-news.txt";
//    private static final String popularPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\top_popular_websites\\top-1m.csv";
    private static final String nonPopularPath = "database/DomainJanuary2016/2016-01-01.txt";
//    private static final String nonPopularPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\DomainJanuary2016\\2016-01-01.txt";
    private static final String topNewsPathAlexa = "database/top_news_websites_alexa/top_500_news_websites.txt";
//    private static final String topNewsPathAlexa = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\top_news_websites_alexa\\top_500_news_websites.txt";
    private static final String topNewsPathSimilarWebID = "database/top_news_websites_similarweb_id/top_100_news_id.txt";
//    private static final String nonPopularPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\top_news_websites_similarweb_id\\top_100_news_id.txt";
//    private static final String topNewsFromTopMillionAlexa = "database/top_popular_websites/top-1m-news.txt";

    /**
     * Get raw content of string from external file
     * @param path
     * @return
     */
    public static String getRawFileContent(String path) {
        StringBuffer rawFileContent = new StringBuffer();
        String  thisLine;
        BufferedReader br = null;
        Boolean isFileExist = true;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            isFileExist = false;
            e.printStackTrace();
        }
        if (isFileExist) {
            try {
                while ((thisLine = br.readLine()) != null) {
                    rawFileContent.append(thisLine + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rawFileContent.toString();
    }

    /**
     * Load List of Training Websites Based on Type and its amount(1 : Malware, 2 : Phishing, 3 : Spamming, 4 : Populer, 5 : Tidak Populer)
     * @param type
     * @return
     */
    public static Pair<List<String>,Integer> loadSitesTrainingList(int type) {
        List<String> SitesTrainingList = new ArrayList<String>();
        int numSitesReturn = 0;

        String rawTrainingList = null;
        switch (type) {
            case 1  :   rawTrainingList = getRawFileContent(malwarePath); break;
            case 2  :   rawTrainingList = getRawFileContent(phishingPath); break;
            case 3  :   rawTrainingList = getRawFileContent(spammingPath); break;
            case 4  :   rawTrainingList = getRawFileContent(popularPath); break;
            case 5  :   rawTrainingList = getRawFileContent(nonPopularPath); break;
            case 6  :   rawTrainingList = getRawFileContent(topNewsPathAlexa); break;
            case 7  :   rawTrainingList = getRawFileContent(topNewsPathSimilarWebID); break;
        }
        StringTokenizer token = new StringTokenizer(rawTrainingList.toString(),"\n");
        while (token.hasMoreTokens()) {
            String oneRow = (String) token.nextToken();
            SitesTrainingList.add(Converter.getBaseHostURL(oneRow));
            numSitesReturn++;
        }

        Pair<List<String>,Integer> SitesListandAmount = new Pair<List<String>, Integer>(SitesTrainingList,numSitesReturn);
        return SitesListandAmount;
    }

    /**
     * Looking for intersection between two list sites
     * @param listSites1
     * @param listSites2
     * @return
     */
    public static List<String> listIntersectedSites(List<String> listSites1, List<String> listSites2) {
        List<String> intersectedSites = new ArrayList<String>();
        for (int i=0;i<listSites1.size();i++) {
            for (int j=0;j<listSites2.size();j++) {
                String site1 = Converter.getBaseHostURL(listSites1.get(i));
                String site2 = Converter.getBaseHostURL(listSites2.get(j));
                if (site1.equals(site2)) {
                    intersectedSites.add(site1);
                }
            }
        }
        return intersectedSites;
    }

    /**
     * Generate random list sites of certain types with amount numRandom
     * Assumption : random list sites generate maximum from 1000 sites
     * @param type
     * @param numRandom
     * @return
     */
    public static List<Integer> loadSitesTrainingListIndexRandom(int type, int numRandom) {
        List<Integer> indexListSites = new ArrayList<Integer>();
        int maxIndexListSites = 1000;
        for (int i=0;i<maxIndexListSites;i++) {
            indexListSites.add(i);
        }
        Collections.shuffle(indexListSites);

        return indexListSites;
    }

    /**
     * Save weka instances to external file (ARFF Format)
     * @param instances
     * @param path
     */
    public static void saveInstanceWekaToExternalARFF(Instances instances, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter writer1 = null;
        try {
            writer1 = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter writer = new BufferedWriter(writer1);
        try {
            writer.write(instances.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save weka instances to external file (CSV Format)
     * @param instances
     * @param path
     */
    public static void saveInstanceWekaToExternalCSV(Instances instances, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        CSVSaver csvSaver = new CSVSaver();
        csvSaver.setInstances(instances);
        try {
            csvSaver.setFile(file);
            csvSaver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save classifier model to external file
     * @param classifier
     * @param path
     */
    public static void saveClassifierToExternalModel(Classifier classifier, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            SerializationHelper.write(path,classifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save clusterer model to external file
     * @param clusterer
     * @param path
     */
    public static void saveClustererToExternalModel(Clusterer clusterer, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            SerializationHelper.write(path,clusterer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save raw content (anything) into eksternal file in spesific path
     * @param rawContent
     * @param path
     */
    public static void saveRawContentToEksternalFile(String rawContent, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.write(rawContent);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load instances weka from external file (arff)
     * @param path
     * @return
     */
    public static Instances loadInstanceWekaFromExternalARFF(String path) {
        Instances instances = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                instances = ConverterUtils.DataSource.read(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instances;
    }

    /**
     * Load classifier from eksternal model
     * @param path
     * @return
     */
    public static Classifier loadClassifierWekaFromEksternalModel(String path) {
        Classifier classifier = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                classifier = (Classifier) SerializationHelper.read(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return classifier;
    }

    /**
     * Load clusterer from eksternal model
     * @param path
     * @return
     */
    public static Clusterer loadClustererWekaFromEksternalModel(String path) {
        Clusterer clusterer = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                clusterer = (Clusterer) SerializationHelper.read(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return clusterer;
    }

    public static void main(String[] args) {
//        Pair<List<String>,Integer> sites = EksternalFile.loadSitesTrainingList(4);
//        List<String> listSites = sites.getKey();
//        int numSites = sites.getValue();
//
//        for (int i=0;i<listSites.size();i++) {
//            System.out.println(listSites.get(i));
//        }
//        System.out.println("Total Sites : " + numSites);

//        String trainingClassifier1 = "database/weka/data/num_200.type_3.normality_category.supervised.arff";
//
////        String pathClassifier1 = "database/weka/model/ffffff.model";
//        String pathClassifier1 = "database/weka/model/num_100.type_3.normalitySVM.model";
//        Classifier classifier = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);
//        Instances instances = EksternalFile.loadInstanceWekaFromExternalARFF(trainingClassifier1);
//
//        try {
//            classifier.classifyInstance(instances.instance(0));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (classifier != null) {
//            System.out.println("SAY, HI!");
//        } else {
//            System.out.println("SAY, HELLO!");
//        }

        // CREATE ANOTHER STATIC DATA IN CSV FORMAT
//        for (int typeReputation=1;typeReputation<=7;typeReputation++) {
//            String pathNormal = "database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".normal.staticdata.arff";
//            Instances normalInstances = EksternalFile.loadInstanceWekaFromExternalARFF(pathNormal);
//            String pathDangerous = "database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".dangerous.staticdata.arff";
//            Instances dangerousInstances = EksternalFile.loadInstanceWekaFromExternalARFF(pathDangerous);
//
//            String pathNormalCSV = "database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".normal.staticdata.csv";
//            EksternalFile.saveInstanceWekaToExternalCSV(normalInstances, pathNormalCSV);
//            String pathDangerousCSV = "database/weka/data_static/numsites_1000.ratio_3111.type_" + typeReputation + ".dangerous.staticdata.csv";
//            EksternalFile.saveInstanceWekaToExternalCSV(dangerousInstances, pathDangerousCSV);
//        }

//        int typeReputation = 6;
//        for (int numTraining = 100; numTraining <= 1000; numTraining = numTraining + 100) {
//            // STAGE 1 (SUPERVISED)
//            String pathNormal = "database/weka/data/num_" + numTraining + ".type_" + typeReputation + ".dangerous_category.supervised.arff";
//            Instances normalInstances = EksternalFile.loadInstanceWekaFromExternalARFF(pathNormal);
//
//            // Delete Particular Trust Score
////            normalInstances.deleteAttributeAt(normalInstances.numAttributes()-11);
////            normalInstances.deleteAttributeAt(normalInstances.numAttributes()-9);
//            for (int i=0;i<4;i++) {
//                normalInstances.deleteAttributeAt(normalInstances.numAttributes()-9);
//            }
//
//            // Build Classifier using SVM
//            SitesLabeler labeledSite2 = new SitesLabeler(typeReputation);
//            labeledSite2.configARFFInstance(new String[]{"malware", "phishing", "spamming"});
//            normalInstances.setClassIndex(normalInstances.numAttributes()-1);
//
//            int maxNearestNeighbor = (int) Math.sqrt(numTraining);
//            StringBuffer arrayOfAccuration = new StringBuffer();
//            for (int j=1;j<=maxNearestNeighbor;j++) {
//                Classifier normalityClassifier = labeledSite2.buildLabelReputationModel(normalInstances, 2, j);
//                try {
//                    // Cross Validation (Situs Normal / Tidak Normal)
//                    Evaluation evalLabeledSiteNormality = new Evaluation(normalInstances);
//                    evalLabeledSiteNormality.crossValidateModel(normalityClassifier, normalInstances, 10, new Random(1));
//                    arrayOfAccuration.append("\n" + SitesLabeler.getCorrectlyClassifiedInstances(evalLabeledSiteNormality, normalInstances) + "\n");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("\nAccuration with type " + typeReputation + " and number training " + numTraining + " : " + arrayOfAccuration.toString() + "\n");
//        }
    }
}
