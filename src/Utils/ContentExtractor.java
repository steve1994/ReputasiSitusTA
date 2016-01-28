package Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 28/01/2016.
 */
public class ContentExtractor {
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

    public static int getInboundLinkFromSearchResults(String domainNameURL) {
        int inboundLink = 0;
        String queryURL = "https://www.google.com/search?q=" + domainNameURL;
        try {
            Document doc = Jsoup
                    .connect(queryURL)
                    .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .get();
            Element numResults = doc.getElementById("resultStats");
            String[] tokenResults = numResults.text().split(" ");
            inboundLink = Integer.parseInt(tokenResults[1].replace(",", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inboundLink;
    }

    public static double getAverageDomainTokenLengthURL(String url) {
        int domainTokenCount = 0;
        int domainTokenLengthSum = 0;
        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_");
        while (tokenDomain.hasMoreTokens()) {
            String token = tokenDomain.nextToken();
            domainTokenLengthSum += token.length();
            domainTokenCount++;
        }
        return ((double) domainTokenLengthSum / (double) domainTokenCount);
    }

    public static int getDomainTokenCountURL(String url) {
        int domainTokenCount = 0;
        StringTokenizer tokenDomain = new StringTokenizer(url,"./?=-_");
        while (tokenDomain.hasMoreTokens()) {
            domainTokenCount++;
        }
        return domainTokenCount;
    }

    public static String getBaseHostURL(String url) {
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
       /* List<String> link = ContentExtractor.getListLinksContainInURL("ligaindonesia.co.id");
        for (String l : link) {
            System.out.println(l);
        }
        System.out.println(ContentExtractor.getInboundLinkFromSearchResults("www.owasp.org"));
        */
        try {
            String host = new URL("http://facebook.com/").getHost();
            System.out.println(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
