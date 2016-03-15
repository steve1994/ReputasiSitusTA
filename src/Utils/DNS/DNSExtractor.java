package Utils.DNS;


import Utils.Converter;
import Utils.Spesific.ContentExtractor;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by steve on 11/03/2016.
 */
public class DNSExtractor {
    // CARI NAME SERVER HOST DARI BGP PREFIX
    // nmap -sL 167.205.3.0/24
    // nmap --script asn-query.nse 192.168.1.111/24

    private static final String A_ATTRIB = "A";
    private static final String[] A_ATTRIBS = {A_ATTRIB};

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

    private static InitialDirContext getInitialDirContext() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        InitialDirContext jdniNaming = null;
        try {
            jdniNaming = new InitialDirContext(env);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return jdniNaming;
    }

    /**
     * Return list of DNS a records from URL site
     * @param url
     * @return
     */
    public static List<String> getDNSRecords(String url) {
        List<String> servers = new ArrayList<String>();
        Attributes attrs = null;
        try {
            attrs = DNSExtractor.getInitialDirContext().getAttributes(DNSExtractor.getBaseHostURL(url),A_ATTRIBS);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        Attribute attr = attrs.get(A_ATTRIB);
        if (attr != null) {
            for (int i=0;i<attr.size();i++) {
                try {
                    String dnsARecord = (String) attr.get(i);
                    String[] dnsARecordAttr = dnsARecord.split(" ");
                    servers.add(dnsARecordAttr[dnsARecordAttr.length-1]);
                } catch (NamingException e) {
                    e.printStackTrace();
                }

            }
        }
        return servers;
    }

    public static void main(String[] args) {
        List<String> ipAddressSitesList = DNSExtractor.getDNSRecords("facebook.com");
        System.out.println(ipAddressSitesList);
        for (int i=0;i<ipAddressSitesList.size();i++) {
            int ASNNumber = Converter.convertIPAddressIntoASN(ipAddressSitesList.get(i));
            System.out.println(ASNNumber);
        }
    }
}
