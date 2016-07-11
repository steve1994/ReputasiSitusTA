package Utils.Database;

import Utils.Converter;
import Utils.Spesific.ContentExtractor;
import javafx.util.Pair;
import sample.StaticVars;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 28/01/2016.
 */
public class EksternalFile {
//    private static final String malwarePath = "database/malware_websites/hosts.txt";
//    private static final String phishingPath = "database/phishing_websites/phishing.txt";
//    private static final String spammingPath = "database/spamming_websites/spamming.txt";
//    private static final String popularPath = "database/top_popular_websites/top-1m.csv";
//    private static final String nonPopularPath = "database/DomainJanuary2016/2016-01-01.txt";

    private static final String malwarePath = "database/malware_websites/hosts.txt";
//    private static final String malwarePath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\malware_websites\\hosts.txt";
    private static final String phishingPath = "database/phishing_websites/phishing.txt";
//    private static final String phishingPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\phishing_websites\\phishing.txt";
    private static final String spammingPath = "database/spamming_websites/spamming.txt";
//    private static final String spammingPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\spamming_websites\\spamming.txt";
    private static final String popularPath = "database/top_popular_websites/top-1m.csv";
//    private static final String popularPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\top_popular_websites\\top-1m.csv";
    private static final String nonPopularPath = "database/DomainJanuary2016/2016-01-01.txt";
//    private static final String nonPopularPath = "D:\\steve\\TA_Project\\ReputasiSitusTA\\database\\DomainJanuary2016\\2016-01-01.txt";

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
     * Checking (before load list of URL) if domain can be resolved)
     * @param url
     * @return
     */
   /* private static Boolean checkDomainResolved(String url) {
        Boolean isDomainResolved = false;
        int timeOut = 3000;
        try {
            if (InetAddress.getByName(getBaseHostURL(url)).isReachable(timeOut)) {
                isDomainResolved = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isDomainResolved;
    }*/

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
     * Save weka instances to external file
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

        String trainingClassifier1 = "database/weka/data/num_200.type_3.normality_category.supervised.arff";

//        String pathClassifier1 = "database/weka/model/ffffff.model";
        String pathClassifier1 = "database/weka/model/num_100.type_3.normalitySVM.model";
        Classifier classifier = EksternalFile.loadClassifierWekaFromEksternalModel(pathClassifier1);
        Instances instances = EksternalFile.loadInstanceWekaFromExternalARFF(trainingClassifier1);

        try {
            classifier.classifyInstance(instances.instance(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (classifier != null) {
            System.out.println("SAY, HI!");
        } else {
            System.out.println("SAY, HELLO!");
        }
    }
}
