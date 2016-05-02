package Utils.Database;

import Utils.Spesific.ContentExtractor;
import javafx.util.Pair;
import weka.core.Instances;
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
    private static final String malwarePath = "database/malware_websites/hosts.txt";
    private static final String phishingPath = "database/phishing_websites/phishing.txt";
    private static final String spammingPath = "database/spamming_websites/spamming.txt";
    private static final String popularPath = "database/top_popular_websites/top-1m.csv";
    private static final String nonPopularPath = "database/DomainJanuary2016/2016-01-01.txt";

    /**
     * Convert URL sites into its base host name
     * @param url
     * @return
     */
    private static String getBaseHostURL(String url) {
        if (!url.contains("http://") && !url.contains("https://")) {
            url = "http://" + url;
        }
        String host = "";
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return host;
    }

    /**
     * Get raw content of string from external file
     * @param path
     * @return
     */
    public static String getRawFileContent(String path) {
        StringBuffer rawFileContent = new StringBuffer();
        String  thisLine;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((thisLine = br.readLine()) != null) {
                rawFileContent.append(thisLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            SitesTrainingList.add(getBaseHostURL(oneRow));
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
        try {
//            ConverterUtils.DataSink.write("database/weka/siteReputation.arff",instances);
            ConverterUtils.DataSink.write(path,instances);
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
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new PrintStream(fout).print(rawContent);
    }

    /**
     * Load instances weka from external file (arff)
     * @param path
     * @return
     */
    public static Instances loadInstanceWekaFromExternalARFF(String path) {
        Instances instances = null;
        try {
            instances = ConverterUtils.DataSource.read(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }

    public static void main(String[] args) {
        Pair<List<String>,Integer> sites = EksternalFile.loadSitesTrainingList(4);
        List<String> listSites = sites.getKey();
        int numSites = sites.getValue();

        for (int i=0;i<listSites.size();i++) {
            System.out.println(listSites.get(i));
        }
        System.out.println("Total Sites : " + numSites);
    }
}
