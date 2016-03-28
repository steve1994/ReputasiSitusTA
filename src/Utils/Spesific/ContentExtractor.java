package Utils.Spesific;

import Utils.Converter;
import Utils.Database.EksternalFile;
import com.google.common.net.InternetDomainName;
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 28/01/2016.
 */
public class ContentExtractor {
    private static HashSet<String> linkInWebsite = new HashSet<String>();
    private static String domainNameWebsite;
    private static int outboundLink = 0;

    public static List<String> getListLinksContainInURL(String domainNameURL) {
        List<String> listLinks = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect("http://www." + domainNameURL).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                listLinks.add(link.attr("abs:href"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listLinks;
    }

    public static int getOutboundLinkFromJSOUP(String domainNameURL) {
        if (linkInWebsite.isEmpty()) {
            domainNameWebsite = domainNameURL;
        }
        if (!linkInWebsite.contains(domainNameURL)) {
            linkInWebsite.add(domainNameURL);
            List<String> linkInThisPage = getListLinksContainInURL(domainNameURL);
            for (String link : linkInThisPage) {
                System.out.println(link);
                if (!link.contains(domainNameWebsite)) {
                    outboundLink++;
                } else {
                    getOutboundLinkFromJSOUP(link);
                }
            }
        }
        return outboundLink;
    }

    /**
     * Get inbound link approximation from 5 search engine (1 : Google, 2 : Yahoo, 3 : Ask!, 4 : Bing, 5 : Duckduckgo)
     * @param domainNameURL
     * @param type
     * @return
     */
    public static int getInboundLinkFromSearchResults(String domainNameURL, int type) {
        String queryURL = null;
        switch (type) {
            default :
            case 1  :   queryURL = "https://www.google.com/search?q=" + domainNameURL; break;
            case 2  :   queryURL = "https://id.search.yahoo.com/search?p=" + domainNameURL; break;
          //  case 3  :   queryURL = "http://id.ask.com/web?q=" + domainNameURL; break;
            case 3  :   queryURL = "http://www.bing.com/search?q=" + domainNameURL; break;
          //  case 5  :   queryURL = "https://duckduckgo.com/?q=" + domainNameURL; break;
        }

        int inboundLink = 0;
        try {
            Document doc = Jsoup
                    .connect(queryURL)
                    .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .get();

            switch (type) {
                default:
                case 1:
                    Element numResults1 = doc.getElementById("resultStats");
                    String[] tokenResults1 = numResults1.text().split(" ");
                    inboundLink = Integer.parseInt(tokenResults1[1].replace(",", ""));
                    break;
                case 2:
                    Element numResults2 = doc.getElementsByClass("compPagination").tagName("span").last();
                    inboundLink = Integer.parseInt(numResults2.text().replace("12345Berikutnya","").replace(" hasil","").replace(",",""));
                    break;
                case 3:
                    Elements numResults4 = doc.getElementsByClass("sb_count");
                    String[] tokenResults4 = numResults4.text().split(" ");
                    inboundLink = Integer.parseInt(tokenResults4[0].replace(".", ""));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inboundLink;
    }

    /**
     * Calculate average token length from URL sites
     * @param url
     * @return
     */
    public static float getAverageDomainTokenLengthURL(String url) {
        int domainTokenCount = 0;
        int domainTokenLengthSum = 0;
        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_,");
        while (tokenDomain.hasMoreTokens()) {
            String token = tokenDomain.nextToken();
            domainTokenLengthSum += token.length();
            domainTokenCount++;
        }
        return ((float) domainTokenLengthSum / (float) domainTokenCount);
    }

    /**
     * Calculate the amount of token in URL sites
     * @param url
     * @return
     */
    public static int getDomainTokenCountURL(String url) {
        int domainTokenCount = 0;
        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_,");
        while (tokenDomain.hasMoreTokens()) {
            domainTokenCount++;
        }
        return domainTokenCount;
    }

    /**
     * Get domain lookup time from URL sites (five consecutive trials)
     * @param url
     * @return
     */
    public static long getDomainLookupTimeSite(String url) {
        long before = System.currentTimeMillis();
        try {
            InetAddress addr = InetAddress.getByName(getBaseHostURL(url));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        long after = System.currentTimeMillis();
        long lookupTime = after - before;

        return lookupTime;
    }

    /**
     * Parsing URL to get SLD domain of it
     * @param url
     * @return
     */
    public static String getSLDFromURL(String url) {
        String SLD = null;
        url = getBaseHostURL(url);

        if (InternetDomainName.isValid(url)) {
            InternetDomainName idn = InternetDomainName.from(url);
            if (idn.hasPublicSuffix()) {
                List<String> parts = idn.parts();
                String pureHost = parts.get(0);
                SLD = url.replace(pureHost + ".","");
            }
        }
        return SLD;
    }

    /**
     * Calculate SLD Hit Ratio by Checking Malware (1), Phishing (2), Spamming (3), Popular (4) List
     * @param url
     * @param type
     * @return
     */
    public static double getSLDHitRatio(String url, int type) {
        Pair<List<String>,Integer> listSitesAndTotal;
        switch (type) {
            default :
            case 1  :   listSitesAndTotal = EksternalFile.loadSitesTrainingList(1); break;
            case 2  :   listSitesAndTotal = EksternalFile.loadSitesTrainingList(2); break;
            case 3  :   listSitesAndTotal = EksternalFile.loadSitesTrainingList(3); break;
            case 4  :   listSitesAndTotal = EksternalFile.loadSitesTrainingList(4); break;
        }
        List<String> listSites = listSitesAndTotal.getKey();
        double totalSites = listSitesAndTotal.getValue();

        double totalSitesSLDMatch = 0.0;
        for (int i=0;i<listSites.size();i++) {
            String SLDCheckList = getSLDFromURL(listSites.get(i));
            String SLDThisURL = getSLDFromURL(url);
            if (SLDThisURL.equals(SLDCheckList)) {
                totalSitesSLDMatch++;
            }
        }

        return totalSitesSLDMatch / totalSites;
    }

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

    public static void main(String[] args) {
      /*  List<String> link = ContentExtractor.getListLinksContainInURL("http://www.ligaindonesia.co.id/assets/collections/pojok_media/files/551e0c0185557.pdf");
        for (String l : link) {
            System.out.println(l);
        }*/
       // System.out.println(ContentExtractor.getInboundLinkFromSearchResults("nasibungkus",4));
       // System.out.println(ContentExtractor.getOutboundLinkFromJSOUP("ligaindonesia.co.id"));
       /* try {
            String host = new URL("http://facebook.com/").getHost();
            System.out.println(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        /*for (int i=0;i<20;i++) {
            System.out.println(ContentExtractor.getDomainLookupTimeSite("pornhub.com"));
        }*/

        System.out.println(ContentExtractor.getSLDFromURL("facebook.com"));
    }
}
