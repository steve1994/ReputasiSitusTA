package Utils.API;

import Utils.Converter;
import Utils.Database.EksternalFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 05/08/2016.
 */
public class SiteAdvisor_API_Loader {
    private static final String SITEADVISOR_ENDPOINT = "https://www.siteadvisor.com/sites/";

    public static String getWebsiteCategory(String url) {
        String request = SITEADVISOR_ENDPOINT + Converter.getBaseHostURL(url);
        String category = "";
        try {
            Document doc = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)").get();
            Element categoryTable = doc.getElementById("siteMeta");
            if (categoryTable != null) {
                category = categoryTable.select("table").select("tbody").select("tr").get(1).select("td").get(0).text();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return category;
    }

    public static void main(String[] args) {
//        List<String> listSites = EksternalFile.loadSitesTrainingList(4).getKey();
//        StringBuffer sb = new StringBuffer();
//        int belowIndex=9119;
//        int upperIndex=10000;
//        for (int i=belowIndex;i<upperIndex;i++) {
//            String categoryThisSite = SiteAdvisor_API_Loader.getWebsiteCategory(listSites.get(i));
//            sb.append(categoryThisSite + "\n");
//            System.out.println("Situs ke-" + (i+1));
//            System.out.println(categoryThisSite);
//        }
//        EksternalFile.saveRawContentToEksternalFile(sb.toString(),"database/top_popular_websites/top_popular_categories_2.txt");

        List<String> listTopNewsAlexa = EksternalFile.loadSitesTrainingList(6).getKey();
        List<String> listTopOverallAlexa = EksternalFile.loadSitesTrainingList(7).getKey();
        List<String> intersectedSite = EksternalFile.listIntersectedSites(listTopNewsAlexa,listTopOverallAlexa);
        for (String site : intersectedSite) {
            System.out.println(site);
        }

//        String rawListCategoriesTopWebsites = EksternalFile.getRawFileContent("database/top_popular_websites/top_popular_categories_2.txt");
//        StringTokenizer token = new StringTokenizer(rawListCategoriesTopWebsites,"\n");
//        List<String> listNewsWebsites = new ArrayList<String>();
//        List<String> listTopWebsites = EksternalFile.loadSitesTrainingList(4).getKey();
//        int listCounter = 0;
//        while (token.hasMoreTokens()) {
//            String oneRow = token.nextToken();
//            if (oneRow.contains("Portal") || oneRow.contains("News")) {
//                listNewsWebsites.add(listTopWebsites.get(listCounter));
//            }
//            listCounter++;
//        }
//        StringBuffer sb = new StringBuffer();
//        for (int i=0;i<listNewsWebsites.size();i++) {
//            sb.append(listNewsWebsites.get(i) + "\n");
//        }
//        EksternalFile.saveRawContentToEksternalFile(sb.toString(),"database/top_popular_websites/top-1m-news.txt");
    }
}
