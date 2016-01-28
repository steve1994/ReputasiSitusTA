package Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static void main(String[] args) {
        List<String> link = ContentExtractor.getListLinksContainInURL("ligaindonesia.co.id");
        for (String l : link) {
            System.out.println(l);
        }
    }
}
