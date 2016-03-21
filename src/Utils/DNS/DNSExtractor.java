package Utils.DNS;


import Utils.API.RIPE_API_loader;
import Utils.Converter;
import Utils.Spesific.ContentExtractor;
import com.google.common.net.InternetDomainName;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.javatuples.Quintet;
import org.javatuples.Sextet;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * Created by steve on 11/03/2016.
 */
public class DNSExtractor {
    // CARI NAME SERVER HOST DARI BGP PREFIX
    // nmap -sL 167.205.3.0/24
    // nmap --script asn-query.nse 192.168.1.111/24
    // nmap --script targets-asn --script-args targets-asn.asn=32
    // https://stat.ripe.net/data/as-routing-consistency/data.json?resource=AS3333

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

    /**
     * Return distribution top level domain from Site's Autonomous System (.com, .org, .edu, .gov., .uk)
     * @param url
     * @return
     */
    public static Sextet<Float,Float,Float,Float,Float,Float> getTLDDistributionFromAS(String url) {
        HashSet<String> comTLDRetrieved = new HashSet<String>();
        HashSet<String> orgTLDRetrieved = new HashSet<String>();
        HashSet<String> eduTLDRetrieved = new HashSet<String>();
        HashSet<String> govTLDRetrieved = new HashSet<String>();
        HashSet<String> ukTLDRetrieved = new HashSet<String>();
        HashSet<String> nonPopularTLDRetrieved = new HashSet<String>();
        int numNameServersTotal = 0;

        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<String> listIPPrefixes = RIPE_API_loader.loadASNFromRIPEAPI(ASNumberThisURL);
        for (String IPPrefix : listIPPrefixes) {
            List<String> resolvedIPAddress = RIPE_API_loader.loadNameServersFromIPPrefix(IPPrefix);
            for (String ip : resolvedIPAddress) {
                String nameServerConverted = Converter.convertIPAddressIntoHostName(ip);
                // Cek apakah bisa dikonversi ke canonical name
                if (!InetAddressValidator.getInstance().isValidInet4Address(nameServerConverted)) {
                    InternetDomainName idn = InternetDomainName.from(nameServerConverted);
                    if (idn.hasPublicSuffix()) {
                        List<String> parts = idn.parts();
                        String TLD = parts.get(parts.size()-1);
                        if (TLD.equals("com")) {
                            comTLDRetrieved.add(nameServerConverted);
                        } else if (TLD.equals("org")) {
                            orgTLDRetrieved.add(nameServerConverted);
                        } else if (TLD.equals("edu")) {
                            eduTLDRetrieved.add(nameServerConverted);
                        } else if (TLD.equals("gov")) {
                            govTLDRetrieved.add(nameServerConverted);
                        } else if (TLD.equals("uk")) {
                            ukTLDRetrieved.add(nameServerConverted);
                        } else {
                            nonPopularTLDRetrieved.add(nameServerConverted);
                        }
                    }
                }
                numNameServersTotal++;
            }
        }
        // Hitung rasio keenam TLD
        float comRatio = (float) comTLDRetrieved.size() / (float) numNameServersTotal;
        float orgRatio = (float) orgTLDRetrieved.size() / (float) numNameServersTotal;
        float eduRatio = (float) eduTLDRetrieved.size() / (float) numNameServersTotal;
        float govRatio = (float) govTLDRetrieved.size() / (float) numNameServersTotal;
        float ukRatio = (float) ukTLDRetrieved.size() / (float) numNameServersTotal;
        float nonPopularRatio = (float) nonPopularTLDRetrieved.size() / (float) numNameServersTotal;

        Sextet<Float,Float,Float,Float,Float,Float> sixRatioRetrieved = new Sextet<Float, Float, Float, Float, Float, Float>(comRatio,orgRatio,eduRatio,govRatio,ukRatio,nonPopularRatio);
        return sixRatioRetrieved;
    }

    /**
     * Return distribution name server from Site's Autonomous System
     * @param url
     * @return
     */
    public static float getDistributionNSFromAS(String url) {
        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<String> listIPPrefixes = RIPE_API_loader.loadASNFromRIPEAPI(ASNumberThisURL);
        HashSet<String> uniqueHostNameRetrieved = new HashSet<String>();
        int numNameServersTotal = 0;
        for (String IPPrefix : listIPPrefixes) {
            List<String> resolvedIPAddress = RIPE_API_loader.loadNameServersFromIPPrefix(IPPrefix);
            for (String ip : resolvedIPAddress) {
                String nameServerConverted = Converter.convertIPAddressIntoHostName(ip);
                // Cek apakah bisa dikonversi ke canonical name
                if (!InetAddressValidator.getInstance().isValidInet4Address(nameServerConverted)) {
                    uniqueHostNameRetrieved.add(nameServerConverted);
                }
                numNameServersTotal++;
            }
        }
        return ((float) uniqueHostNameRetrieved.size() / (float) numNameServersTotal);
    }

    public static void main(String[] args) {
       /* List<String> ipAddressSitesList = DNSExtractor.getDNSRecords("cap2zen.com");
        System.out.println(ipAddressSitesList);
        for (int i=0;i<ipAddressSitesList.size();i++) {
            int ASNNumber = Converter.convertIPAddressIntoASN(ipAddressSitesList.get(i));
            System.out.println(ASNNumber);
        }*/
        // Rasio Distribusi NS dari AS
        //System.out.println(DNSExtractor.getDistributionNSFromAS("cutscenes.net"));
        // Rasio Distribusi TLD dari AS
        Sextet<Float,Float,Float,Float,Float,Float> sixRatio = DNSExtractor.getTLDDistributionFromAS("cutscenes.net");
        System.out.println("The six ratio : " + sixRatio.getValue0() + " ; " + sixRatio.getValue1() + " ; " + sixRatio.getValue2() + " ; " + sixRatio.getValue3() + " ; " + sixRatio.getValue4() + " ; " + sixRatio.getValue5());
    }
}
