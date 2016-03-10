package Utils.Database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
     * Load List of Training Websites Based on Type (1 : Malware, 2 : Phishing, 3 : Spamming, 4 : Populer, 5 : Tidak Populer)
     * @param type
     * @return
     */
    public static List<String> loadSitesTrainingList(int type) {
        List<String> SitesTrainingList = new ArrayList<String>();
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
            SitesTrainingList.add(oneRow);
        }
        return SitesTrainingList;
    }

    public static void main(String[] args) {
        List<String> sites = EksternalFile.loadSitesTrainingList(4);
        for (String site : sites) {
            System.out.println(site);
        }
    }
}
