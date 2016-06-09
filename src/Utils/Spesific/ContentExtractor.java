package Utils.Spesific;

import Utils.API.RIPE_API_Loader;
import Utils.Converter;
import Utils.Database.EksternalFile;
import com.google.common.net.InternetDomainName;
import data_structure.feature.Spesific_Feature;
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
            case 3  :   queryURL = "http://www.bing.com/search?q=" + domainNameURL; break;
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
                    try {
                        if (doc.toString().contains("<div id=\"resultStats\">")) {
                            Element numResults1 = doc.getElementById("resultStats");
                            String[] tokenResults1 = numResults1.text().split(" ");
                            inboundLink = Integer.parseInt(tokenResults1[1].replace(",", ""));
                            System.out.println("GOOGLE");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        if (doc.toString().contains("compPagination")) {
                            Element numResults2 = doc.getElementsByClass("compPagination").tagName("span").last();
                            inboundLink = Integer.parseInt(numResults2.text().replace("12345Berikutnya", "").replace(" hasil", "").replace(",", ""));
                            System.out.println("YAHOO");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        if (doc.toString().contains("<span class=\"sb_count\">")) {
                            Elements numResults4 = doc.getElementsByClass("sb_count");
                            String[] tokenResults4 = numResults4.text().split(" ");
                            inboundLink = Integer.parseInt(tokenResults4[0].replace(".", ""));
                            System.out.println("BING");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
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
    public static double getAverageDomainTokenLengthURL(String url) {
        int domainTokenCount = 0;
        int domainTokenLengthSum = 0;
//        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_,");
        StringTokenizer tokenDomain = new StringTokenizer(url,"%&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (tokenDomain.hasMoreTokens()) {
            String token = tokenDomain.nextToken();
            domainTokenLengthSum += token.length();
            domainTokenCount++;
        }
        return ((double) domainTokenLengthSum / (double) domainTokenCount);
    }

    /**
     * Calculate the amount of token in URL sites
     * @param url
     * @return
     */
    public static int getDomainTokenCountURL(String url) {
        int domainTokenCount = 0;
//        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_,");
        StringTokenizer tokenDomain = new StringTokenizer(url,"%&\"*#@$^_<>|`+=-1234567890'(){}[]/.:;?!,\n");
        while (tokenDomain.hasMoreTokens()) {
            String token = tokenDomain.nextToken();
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
            InetAddress addr = InetAddress.getByName(Converter.getBaseHostURL(url));
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
        url = Converter.getBaseHostURL(url);

//        if (InternetDomainName.isValid(url)) {
//            InternetDomainName idn = InternetDomainName.from(url);
//            if (idn.hasPublicSuffix()) {
//                List<String> parts = idn.parts();
//                String pureHost = parts.get(0);
//                SLD = url.replace(pureHost + ".", "");
//            }
//        }

        List<String> SLDToken = new ArrayList<String>();
        StringTokenizer levelSLDSplit = new StringTokenizer(url,".");
        while (levelSLDSplit.hasMoreTokens()) {
            String token = levelSLDSplit.nextToken();
            SLDToken.add(token);
        }
        if (SLDToken.size() > 1) {
            SLD = SLDToken.get(SLDToken.size()-2) + "." + SLDToken.get(SLDToken.size()-1);
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
            try {
                String SLDCheckList = getSLDFromURL(listSites.get(i));
                String SLDThisURL = getSLDFromURL(url);
                if (SLDThisURL.equals(SLDCheckList) && (SLDCheckList != null) && (SLDThisURL != null)) {
                    totalSitesSLDMatch++;
                }
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }

        return totalSitesSLDMatch / totalSites;
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

        // SPESIFIC FEATURES
        List<Object> fiturs = new ArrayList<Object>();
        String hostName = "facebook.com";

        long before = System.currentTimeMillis();
        // Token Count URL
        int tokenCount = ContentExtractor.getDomainTokenCountURL(hostName);
        fiturs.add(tokenCount);
        System.out.println("Token Count : " + tokenCount);

        long afterTokenCount = System.currentTimeMillis();

        // Average Token Length URL
        double avgTokenCount = ContentExtractor.getAverageDomainTokenLengthURL(hostName);
        fiturs.add(avgTokenCount);
        System.out.println("Average Token Count : " + avgTokenCount);

        long afterAvgToken = System.currentTimeMillis();

        // SLD ratio from URL (malware, phishing, spamming)
        double[] SLDRatioList = new double[3];
        System.out.println("SLD Ratio List : ");
        for (int j=0;j<3;j++) {
            SLDRatioList[j] = ContentExtractor.getSLDHitRatio(hostName,j+1);
            System.out.println(SLDRatioList[j]);
            fiturs.add(SLDRatioList[j]);
        }

        long afterSLDRatio = System.currentTimeMillis();

        // Inbound link Approximation (Google, Yahoo, Bing)
        int[] inboundLinkAppr = new int[3];
        System.out.println("Inbound Link Approximation : ");
        for (int j=0;j<3;j++) {
            inboundLinkAppr[j] = ContentExtractor.getInboundLinkFromSearchResults(hostName,j+1);
            System.out.println(inboundLinkAppr[j]);
            fiturs.add(inboundLinkAppr[j]);
        }

        long afterInboundLink = System.currentTimeMillis();

        // Lookup time to access site
        long lookupTime = ContentExtractor.getDomainLookupTimeSite(hostName);
        fiturs.add(lookupTime);
        System.out.println("Lookup Time : " + lookupTime);

        long afterLookup = System.currentTimeMillis();

        System.out.println("==========================================================================");

        // TES ISI FITUR KE INSTANCE WEKA
        double[] values = new double[fiturs.size()];
        for (int i=0;i<fiturs.size();i++) {
            values[i] = new Double(fiturs.get(i).toString());
        }
        for (double d : values) {
            System.out.println(d);
        }
        long after = System.currentTimeMillis();

        System.out.println("Waktu eksekusi : " + (after-before));
        System.out.println("Waktu eksekusi token count : " + (afterTokenCount-before));
        System.out.println("Waktu eksekusi average token count : " + (afterAvgToken-afterTokenCount));
        System.out.println("Waktu eksekusi SLD ratio : " + (afterSLDRatio-afterAvgToken));
        System.out.println("Waktu eksekusi Inbound Link : " + (afterInboundLink-afterSLDRatio));
        System.out.println("Waktu eksekusi Lookup Time : " + (afterLookup-afterInboundLink));
    }
}
