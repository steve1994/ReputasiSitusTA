package Utils.DNS;


import Utils.API.MXToolbox_API_Loader;
import Utils.API.RIPE_API_Loader;
import Utils.Converter;
import Utils.Database.EksternalFile;
import com.google.common.net.InternetDomainName;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.javatuples.Sextet;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
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
     * Return number of name servers used by domain
     * @param url
     * @return
     */
    public static int getNumNameServers(String url) {
        return RIPE_API_Loader.loadNameServersFromHost(url).size();
    }

    /**
     * Return hit AS ratio from certain malicious type (1 : malware, 2 : Phishing, 3 : Spamming)
     * Assumption : AS ratio is measured up to only first 100 certain sites list
     * @param url
     * @param type
     * @return
     */
    public static double getHitASRatio(String url, int type) {
        int thisURLASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<String> listSites = EksternalFile.loadSitesTrainingList(type).getKey();
        int hitASCounter = 0;
        for (int i=0;i<100;i++) {
            int siteASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(listSites.get(i)));
            if (siteASN == thisURLASN) {
                hitASCounter++;
            }
        }
        return (double) hitASCounter / (double) 100;
    }

    /**
     * Return distribution top level domain from Site's Autonomous System (.com, .org, .edu, .gov., .uk)
     * @param url
     * @return
     */
    public static Sextet<Double,Double,Double,Double,Double,Double> getTLDDistributionFromAS(String url) {
        HashSet<String> comTLDRetrieved = new HashSet<String>();
        HashSet<String> orgTLDRetrieved = new HashSet<String>();
        HashSet<String> eduTLDRetrieved = new HashSet<String>();
        HashSet<String> govTLDRetrieved = new HashSet<String>();
        HashSet<String> ukTLDRetrieved = new HashSet<String>();
        HashSet<String> nonPopularTLDRetrieved = new HashSet<String>();
        int numNameServersTotal = 0;

        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<String> listIPPrefixes = RIPE_API_Loader.loadASNFromRIPEAPI(ASNumberThisURL);
        for (String IPPrefix : listIPPrefixes) {
            List<String> resolvedIPAddress = RIPE_API_Loader.loadNameServersFromIPPrefix(IPPrefix);
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
        double comRatio = (double) comTLDRetrieved.size() / (double) numNameServersTotal;
        double orgRatio = (double) orgTLDRetrieved.size() / (double) numNameServersTotal;
        double eduRatio = (double) eduTLDRetrieved.size() / (double) numNameServersTotal;
        double govRatio = (double) govTLDRetrieved.size() / (double) numNameServersTotal;
        double ukRatio = (double) ukTLDRetrieved.size() / (double) numNameServersTotal;
        double nonPopularRatio = (double) nonPopularTLDRetrieved.size() / (double) numNameServersTotal;

        Sextet<Double,Double,Double,Double,Double,Double> sixRatioRetrieved = new Sextet<Double, Double, Double, Double, Double, Double>(comRatio,orgRatio,eduRatio,govRatio,ukRatio,nonPopularRatio);
        return sixRatioRetrieved;
    }

    /**
     * Return distribution name server from Site's Autonomous System
     * @param url
     * @return
     */
    public static double getDistributionNSFromAS(String url) {
        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<String> listIPPrefixes = RIPE_API_Loader.loadASNFromRIPEAPI(ASNumberThisURL);
        HashSet<String> uniqueHostNameRetrieved = new HashSet<String>();
        int numNameServersTotal = 0;
        for (String IPPrefix : listIPPrefixes) {
            List<String> resolvedIPAddress = RIPE_API_Loader.loadNameServersFromIPPrefix(IPPrefix);
            for (String ip : resolvedIPAddress) {
                String nameServerConverted = Converter.convertIPAddressIntoHostName(ip);
                // Cek apakah bisa dikonversi ke canonical name
                if (!InetAddressValidator.getInstance().isValidInet4Address(nameServerConverted)) {
                    uniqueHostNameRetrieved.add(nameServerConverted);
                }
                numNameServersTotal++;
            }
        }
        return ((double) uniqueHostNameRetrieved.size() / (double) numNameServersTotal);
    }

    /**
     * Return list time to live for each name server owned by url
     * @param url
     * @return
     */
    public static Integer getNameServerTimeToLive(String url) {
        List<Integer> NSTTLList = MXToolbox_API_Loader.listNameServerTimeToLive(url);
        Integer timeToLive = 0;
        if (NSTTLList.size() > 0) {
            timeToLive = NSTTLList.get(0);
        }
        return timeToLive;
    }

    /**
     * Return list time to live for DNS A record retrieved from its corresponding name server
     * @param url
     * @return
     */
    public static Integer getDNSRecordTimeToLive(String url) {
        List<Integer> IPTTLList = MXToolbox_API_Loader.listIPAddressTimeToLive(url);
        Integer timeToLive = 0;
        if (IPTTLList.size() > 0) {
            timeToLive = IPTTLList.get(0);
        }
        return timeToLive;
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
       /* Sextet<Float,Float,Float,Float,Float,Float> sixRatio = DNSExtractor.getTLDDistributionFromAS("cutscenes.net");
        System.out.println("The six ratio : " + sixRatio.getValue0() + " ; " + sixRatio.getValue1() + " ; " + sixRatio.getValue2() + " ; " + sixRatio.getValue3() + " ; " + sixRatio.getValue4() + " ; " + sixRatio.getValue5());*/
        // Rasio Hit AS certain list
      //  System.out.println("AS hit ratio : " + DNSExtractor.getHitASRatio("facebook.com",2));
        // Rasio 5 top populer TLD AS
        Sextet<Double,Double,Double,Double,Double,Double> TLDRatioAS = DNSExtractor.getTLDDistributionFromAS("google.com");
        System.out.println(TLDRatioAS);
    }
}
