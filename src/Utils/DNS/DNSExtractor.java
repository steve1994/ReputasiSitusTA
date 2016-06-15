package Utils.DNS;


import Utils.API.MXToolbox_API_Loader;
import Utils.API.RIPE_API_Loader;
import Utils.Converter;
import Utils.Database.EksternalFile;
import com.google.common.net.InternetDomainName;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.javatuples.Pair;
import org.javatuples.Sextet;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by steve on 11/03/2016.
 */
public class DNSExtractor {
    private static final String A_ATTRIB = "A";
    private static final String[] A_ATTRIBS = {A_ATTRIB};

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
            attrs = DNSExtractor.getInitialDirContext().getAttributes(Converter.getBaseHostURL(url),A_ATTRIBS);
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
     * @param type
     * @return
     */
    public static double getHitASRatio(int numberASNURL, int type) {
        int hitASCounter = 0;
//        int thisURLASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<Integer> listASNSitesThisType = loadASNSitesFromExternalFile(type);
        if (numberASNURL > 0) {  // Sites not detected or null
            for (int i=0;i<1000;i++) {
                if (listASNSitesThisType.get(i) == numberASNURL) {
                    hitASCounter++;
                }
            }
        }
        if (listASNSitesThisType.size() > 0) {
            return (double) hitASCounter / (double) listASNSitesThisType.size();
        } else {
            return 0.0;
        }
    }

    /**
     * Update AS Number List of this typeSites into external file
     * @param typeSites
     */
    public static void saveASNSitesToExternalFile(int typeSites) {
        List<String> listSites = EksternalFile.loadSitesTrainingList(typeSites).getKey();
        StringBuffer stringBuffer = new StringBuffer();
        System.out.println("TYPE SITES : " + typeSites);
        for (int i=0;i<1000;i++) {
            int ASNumberThisSite = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(listSites.get(i)));
            stringBuffer.append(ASNumberThisSite + "\n");
            System.out.println(ASNumberThisSite);
        }
        switch (typeSites) {
            default:
            case 1:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(),"src/Utils/DNS/database/ASNumberListMalware.txt"); break;
            case 2:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(),"src/Utils/DNS/database/ASNumberListPhishing.txt"); break;
            case 3:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(),"src/Utils/DNS/database/ASNumberListSpamming.txt"); break;
        }
    }

    /**
     * Load
     * @param typeSites
     * @return
     */
    private static List<Integer> loadASNSitesFromExternalFile(int typeSites) {
        String rawContent;
        switch (typeSites) {
            default:
            case 1:
                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListMalware.txt"); break;
            case 2:
                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListPhishing.txt"); break;
            case 3:
                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListSpamming.txt"); break;
        }
        List<Integer> listASNThisSites = new ArrayList<Integer>();
        if (!rawContent.isEmpty()) {
            StringTokenizer rawContentPerRow = new StringTokenizer(rawContent, "\n");
            while (rawContentPerRow.hasMoreTokens()) {
                String row = rawContentPerRow.nextToken();
                listASNThisSites.add(Integer.parseInt(row));
            }
        }
        return listASNThisSites;
    }

    /**
     * Return distribution top level domain from Site's Autonomous System (.com, .org, .edu, .gov., .uk)
     * @param url
     * @return
     */
    public static Pair<Double,Sextet<Double,Double,Double,Double,Double,Double>> getTLDDistributionFromAS(String url) {
        HashSet<String> comTLDRetrieved = new HashSet<String>();
        HashSet<String> orgTLDRetrieved = new HashSet<String>();
        HashSet<String> eduTLDRetrieved = new HashSet<String>();
        HashSet<String> govTLDRetrieved = new HashSet<String>();
        HashSet<String> ukTLDRetrieved = new HashSet<String>();
        HashSet<String> nonPopularTLDRetrieved = new HashSet<String>();
        HashSet<String> uniqueNameServers = new HashSet<String>();
        int numNameServersTotal = 0;

        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        if (ASNumberThisURL > 0) {
            List<String> listIPPrefixes = RIPE_API_Loader.loadASNFromRIPEAPI(ASNumberThisURL);
            for (String IPPrefix : listIPPrefixes) {
                System.out.println("IPPrefix : " + IPPrefix);
                List<String> resolvedIPAddress = RIPE_API_Loader.loadNameServersFromIPPrefix(IPPrefix);
                for (String ip : resolvedIPAddress) {
                    String nameServerConverted = Converter.convertIPAddressIntoHostName(ip);
                    uniqueNameServers.add(nameServerConverted);
                    System.out.println("Name Server : " + nameServerConverted);
                    // Cek apakah bisa dikonversi ke canonical name
                    if ((!InetAddressValidator.getInstance().isValidInet4Address(nameServerConverted)) && (!nameServerConverted.isEmpty())) {
                        InternetDomainName idn = InternetDomainName.from(nameServerConverted);
                        if (idn.hasPublicSuffix()) {
                            List<String> parts = idn.parts();
                            String TLD = parts.get(parts.size() - 1);
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
//                    String[] nameServerConvertedToken = nameServerConverted.split("\\.");
//                    String nameServerConvertedUnique = nameServerConvertedToken[nameServerConvertedToken.length-2] + "." + nameServerConvertedToken[nameServerConvertedToken.length-1];
//                    System.out.println("Name Server Unique : " + nameServerConvertedUnique);
//                    uniqueNameServers.add(nameServerConvertedUnique);

                    numNameServersTotal++;
                }
            }
        }
        
        // Hitung rasio keenam TLD
        double comRatio, orgRatio, eduRatio, govRatio, ukRatio, nonPopularRatio;
        if (numNameServersTotal > 0) {
            comRatio = (double) comTLDRetrieved.size() / (double) numNameServersTotal;
            orgRatio = (double) orgTLDRetrieved.size() / (double) numNameServersTotal;
            eduRatio = (double) eduTLDRetrieved.size() / (double) numNameServersTotal;
            govRatio = (double) govTLDRetrieved.size() / (double) numNameServersTotal;
            ukRatio = (double) ukTLDRetrieved.size() / (double) numNameServersTotal;
            nonPopularRatio = (double) nonPopularTLDRetrieved.size() / (double) numNameServersTotal;
        } else {
            comRatio = orgRatio = eduRatio = govRatio = ukRatio = nonPopularRatio = 0.0;
        }
        // Hitung distribusi name server unik AS
        double uniqueNSDistributionAS;
        if (numNameServersTotal > 0) {
            uniqueNSDistributionAS = (double) uniqueNameServers.size() / (double) numNameServersTotal;
        } else {
            uniqueNSDistributionAS = 0.0;
        }

        Sextet<Double,Double,Double,Double,Double,Double> sixRatioRetrieved = new Sextet<Double, Double, Double, Double, Double, Double>(comRatio,orgRatio,eduRatio,govRatio,ukRatio,nonPopularRatio);
        return new Pair<Double,Sextet<Double,Double,Double,Double,Double,Double>>(uniqueNSDistributionAS,sixRatioRetrieved);
    }

    /**
     * Return distribution name server from Site's Autonomous System
     * @param url
     * @return
     */
    public static double getDistributionNSFromAS(String url) {
        int numNameServersTotal = 0;
        HashSet<String> uniqueHostNameRetrieved = new HashSet<String>();
        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        if (ASNumberThisURL > 0) {  // Sites not detected or null
            List<String> listIPPrefixes = RIPE_API_Loader.loadASNFromRIPEAPI(ASNumberThisURL);
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
        }
        double distributionNSinAS;
        if (numNameServersTotal > 0) {
            distributionNSinAS = (double) uniqueHostNameRetrieved.size() / (double) numNameServersTotal;
        } else {
            distributionNSinAS = 0.0;
        }
        return distributionNSinAS;
    }

    private static Integer getMaxIntegerList(List<Integer> listInteger) {
        Integer maxInteger = listInteger.get(0);
        for (int i=1;i<listInteger.size();i++) {
            if (listInteger.get(i) > maxInteger) {
                maxInteger = listInteger.get(i);
            }
        }
        return maxInteger;
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
            timeToLive = getMaxIntegerList(NSTTLList);
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
            timeToLive = getMaxIntegerList(IPTTLList);
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

        String hostName = "facebook.com";
        List<Object> fiturs = new ArrayList<Object>();

        long before = System.currentTimeMillis();

        // TLD ratio
        Pair<Double,Sextet<Double,Double,Double,Double,Double,Double>> pairDistAndTLDRatio = DNSExtractor.getTLDDistributionFromAS(hostName);
        Double[] TLDRatioList = new Double[6];
        TLDRatioList[0] = pairDistAndTLDRatio.getValue1().getValue0(); fiturs.add(TLDRatioList[0]);
        TLDRatioList[1] = pairDistAndTLDRatio.getValue1().getValue1(); fiturs.add(TLDRatioList[1]);
        TLDRatioList[2] = pairDistAndTLDRatio.getValue1().getValue2(); fiturs.add(TLDRatioList[2]);
        TLDRatioList[3] = pairDistAndTLDRatio.getValue1().getValue3(); fiturs.add(TLDRatioList[3]);
        TLDRatioList[4] = pairDistAndTLDRatio.getValue1().getValue4(); fiturs.add(TLDRatioList[4]);
        TLDRatioList[5] = pairDistAndTLDRatio.getValue1().getValue5(); fiturs.add(TLDRatioList[5]);
        System.out.println("TLD Ratio : ");
        for (Double d : TLDRatioList) {
            System.out.println(d);
        }

        long afterTLD = System.currentTimeMillis();

        // Hit AS Ratio (malware, phishing, spamming)
        Double[] HitRatioList = new Double[3];
        int thisSiteASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(hostName));
        for (int j=0;j<3;j++) {
            HitRatioList[j] = DNSExtractor.getHitASRatio(thisSiteASN,j+1);
            fiturs.add(HitRatioList[j]);
        }
        System.out.println("Hit AS Ratio : ");
        for (Double d : HitRatioList) {
            System.out.println(d);
        }

        long afterHitRatio = System.currentTimeMillis();

        // Name server distribution AS
//        double distributionNS = DNSExtractor.getDistributionNSFromAS(hostName);
        double distributionNS = pairDistAndTLDRatio.getValue0();
        fiturs.add(distributionNS);
        System.out.println("Name Server Distribution AS : " + distributionNS);

        long afterNSDist = System.currentTimeMillis();

        // Name server count
        int numNameServer = DNSExtractor.getNumNameServers(hostName); fiturs.add(numNameServer);
        System.out.println("Name Server Count : " + numNameServer);

        long afterNSCount = System.currentTimeMillis();

        // TTL Name Servers
        int NSTTL = DNSExtractor.getNameServerTimeToLive(hostName); fiturs.add(NSTTL);
        System.out.println("TTL Name Servers : " + NSTTL);

        long afterTTLNS = System.currentTimeMillis();

        // TTL DNS A Records
        int IPTTL = DNSExtractor.getDNSRecordTimeToLive(hostName); fiturs.add(IPTTL);
        System.out.println("TTL DNS Record : " + IPTTL);

        long afterTTLIP = System.currentTimeMillis();

        System.out.println("==================================================================================");

        double[] values = new double[fiturs.size()];
        for (int i=0;i<fiturs.size();i++) {
            values[i] = new Double(fiturs.get(i).toString());
        }
        for (double d : values) {
            System.out.println(d);
        }
        long after = System.currentTimeMillis();

        System.out.println("Waktu eksekusi : " + (after-before));
        System.out.println("Waktu eksekusi TLD ratio : " + (afterTLD-before));
        System.out.println("Waktu eksekusi Hit ratio : " + (afterHitRatio-afterTLD));
        System.out.println("Waktu eksekusi NS distribution : " + (afterNSDist-afterHitRatio));
        System.out.println("Waktu eksekusi NS count : " + (afterNSCount-afterNSDist));
        System.out.println("Waktu eksekusi TTL NS : " + (afterTTLNS-afterNSCount));
        System.out.println("Waktu eksekusi TTL IP : " + (afterTTLIP-afterTTLNS));

//        long start = System.currentTimeMillis();
//        int thisASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress("mista.eu"));
//        for (int i=1;i<=3;i++) {
//            DNSExtractor.getHitASRatio(thisASN, i);
//        }
//        long finish = System.currentTimeMillis();
//        System.out.println("TIME : " + (finish-start));
    }
}
