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
     * Load List of Training Websites Based on Type (1 : Malware, 2 : Phishing, 3 : Spamming, 4 : Populer)
     * @param type
     * @return
     */
    public static List<String> loadSitesTrainingList(int type) {
        List<String> SitesTrainingList = new ArrayList<String>();
        String rawTrainingList = null;
        switch (type) {
            case 1  :   rawTrainingList = getRawFileContent("database/malware_websites/hosts.txt"); break;
            case 2  :   rawTrainingList = getRawFileContent("database/phishing_websites/phishing.txt"); break;
            case 3  :   rawTrainingList = getRawFileContent("database/spamming_websites/spamming.txt"); break;
            case 4  :   rawTrainingList = getRawFileContent("database/top_popular_websites/top-1m.csv"); break;
        }
        StringTokenizer token = new StringTokenizer(rawTrainingList.toString(),"\n");
        while (token.hasMoreTokens()) {
            String oneRow = (String) token.nextToken();
            SitesTrainingList.add(oneRow);
        }
        return SitesTrainingList;
    }

    public static void main(String[] args) {
        List<String> sites = EksternalFile.loadSitesTrainingList(3);
        for (String site : sites) {
            System.out.println(site);
        }
    }
}
