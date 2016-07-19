package Utils.DNS;


import Utils.API.MXToolbox_API_Loader;
import Utils.API.RIPE_API_Loader;
import Utils.Converter;
import Utils.Database.EksternalFile;
import com.google.common.net.InternetDomainName;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.javatuples.*;
import org.javatuples.Octet;
import org.xbill.DNS.*;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

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
     *
     * @param url
     * @return
     */
    public static List<String> getDNSRecords(String url) {
        List<String> servers = new ArrayList<String>();
        Attributes attrs = null;
        try {
            attrs = DNSExtractor.getInitialDirContext().getAttributes(Converter.getBaseHostURL(url), A_ATTRIBS);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        Attribute attr = attrs.get(A_ATTRIB);
        if (attr != null) {
            for (int i = 0; i < attr.size(); i++) {
                try {
                    String dnsARecord = (String) attr.get(i);
                    String[] dnsARecordAttr = dnsARecord.split(" ");
                    servers.add(dnsARecordAttr[dnsARecordAttr.length - 1]);
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
        return servers;
    }

    /**
     * Return number of name servers used by domain
     *
     * @param url
     * @return
     */
    public static int getNumNameServers(String url) {
        return RIPE_API_Loader.loadNameServersFromHost(url).size();
    }

    /**
     * Return hit AS ratio from certain malicious type (1 : malware, 2 : Phishing, 3 : Spamming)
     * Assumption : AS ratio is measured up to only first 100 certain sites list
     *
     * @param type
     * @return
     */
    public static double getHitASRatio(int numberASNURL, int type) {
        int hitASCounter = 0;
//        int thisURLASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        List<Integer> listASNSitesThisType = loadASNSitesFromExternalFile(type);
        if (numberASNURL > 0) {  // Sites not detected or null
            for (int i = 0; i < 1000; i++) {
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
     *
     * @param typeSites
     */
    public static void saveASNSitesToExternalFile(int typeSites) {
        List<String> listSites = EksternalFile.loadSitesTrainingList(typeSites).getKey();
        StringBuffer stringBuffer = new StringBuffer();
        System.out.println("TYPE SITES : " + typeSites);
        for (int i = 0; i < 1000; i++) {
            int ASNumberThisSite = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(listSites.get(i)));
            stringBuffer.append(ASNumberThisSite + "\n");
            System.out.println(ASNumberThisSite);
        }
        switch (typeSites) {
            default:
            case 1:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(), "src/Utils/DNS/database/ASNumberListMalware.txt");
                break;
            case 2:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(), "src/Utils/DNS/database/ASNumberListPhishing.txt");
                break;
            case 3:
                EksternalFile.saveRawContentToEksternalFile(stringBuffer.toString(), "src/Utils/DNS/database/ASNumberListSpamming.txt");
                break;
        }
    }

    /**
     * Load
     *
     * @param typeSites
     * @return
     */
    private static List<Integer> loadASNSitesFromExternalFile(int typeSites) {
        String rawContent;
        switch (typeSites) {
            default:
            case 1:
                rawContent = EksternalFile.getRawFileContent("src/Utils/DNS/database/ASNumberListMalware.txt");
//                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListMalware.txt");
                break;
            case 2:
                rawContent = EksternalFile.getRawFileContent("src/Utils/DNS/database/ASNumberListPhishing.txt");
//                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListPhishing.txt");
                break;
            case 3:
                rawContent = EksternalFile.getRawFileContent("src/Utils/DNS/database/ASNumberListSpamming.txt");
//                rawContent = EksternalFile.getRawFileContent("D:\\steve\\TA_Project\\ReputasiSitusTA\\src\\Utils\\DNS\\database\\ASNumberListSpamming.txt");
                break;
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
     *
     * @param url
     * @return
     */
    public static Pair<Double, Quartet<Double, Double, Double, Double>> getTLDDistributionFromAS(String url) {
        HashSet<String> comTLDRetrieved = new HashSet<String>();
        HashSet<String> orgTLDRetrieved = new HashSet<String>();
//        HashSet<String> eduTLDRetrieved = new HashSet<String>();
//        HashSet<String> govTLDRetrieved = new HashSet<String>();
        HashSet<String> ukTLDRetrieved = new HashSet<String>();
        HashSet<String> nonPopularTLDRetrieved = new HashSet<String>();
        HashSet<String> uniqueNameServers = new HashSet<String>();
        int numNameServersTotal = 0;

        int ASNumberThisURL = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(url));
        System.out.println("AS NUMBER : " + ASNumberThisURL);
        if (ASNumberThisURL > 0) {
            // Load IP Prefix and determine partition
            List<String> listIPPrefixes = RIPE_API_Loader.loadASNFromRIPEAPI(ASNumberThisURL);
            int numPartitionIPPrefixes = 50;
            if (numPartitionIPPrefixes > listIPPrefixes.size()) {
                numPartitionIPPrefixes = listIPPrefixes.size();
            }

            // Split list ip prefixes into ten parts
            List<List<String>> listIPPrefixesPartition = new ArrayList<List<String>>();
            for (int j=0;j<numPartitionIPPrefixes;j++) {
                listIPPrefixesPartition.add(new ArrayList<String>());
            }
            int offSetPartition = 0;
            for (int j=0;j<listIPPrefixes.size();j++) {
                listIPPrefixesPartition.get(offSetPartition).add(listIPPrefixes.get(j));
                offSetPartition++;
                if (offSetPartition == numPartitionIPPrefixes) {
                    offSetPartition = 0;
                }
            }

            // Thread TLD Execution (using Callable)
            ExecutorService executorService = Executors.newFixedThreadPool(numPartitionIPPrefixes);
            List<Callable<Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer>>> listCallable =
                    new ArrayList<Callable<Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer>>>();
            for (int j = 0; j < listIPPrefixesPartition.size(); j++) {
                listCallable.add(new TLDDistributionASThread(listIPPrefixesPartition.get(j)));
            }
            try {
                List<Future<Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer>>> tasksCallable = executorService.invokeAll(listCallable);
                for (Future<Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer>> task : tasksCallable) {
                    Sextet<HashSet<String>, HashSet<String>, HashSet<String>, HashSet<String>, HashSet<String>, Integer> thread = null;
                    try {
                        thread = task.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    // .com Top Level Domain
                    HashSet<String> comTLDThisThread = thread.getValue0();
                    Iterator iteratorCom = comTLDThisThread.iterator();
                    while (iteratorCom.hasNext()) {
                        String TLD = (String) iteratorCom.next();
                        comTLDRetrieved.add(TLD);
                    }
                    // .org Top Level Domain
                    HashSet<String> orgTLDThisThread = thread.getValue1();
                    Iterator iteratorOrg = orgTLDThisThread.iterator();
                    while (iteratorOrg.hasNext()) {
                        String TLD = (String) iteratorOrg.next();
                        orgTLDRetrieved.add(TLD);
                    }
                    // .uk Top Level Domain
                    HashSet<String> ukTLDThisThread = thread.getValue2();
                    Iterator iteratorUk = ukTLDThisThread.iterator();
                    while (iteratorUk.hasNext()) {
                        String TLD = (String) iteratorUk.next();
                        ukTLDRetrieved.add(TLD);
                    }
                    // non popular Top Level Domain
                    HashSet<String> npTLDThisThread = thread.getValue3();
                    Iterator iteratorNp = npTLDThisThread.iterator();
                    while (iteratorNp.hasNext()) {
                        String TLD = (String) iteratorNp.next();
                        nonPopularTLDRetrieved.add(TLD);
                    }
                    // Num Name Server Unique
                    HashSet<String> uniqueNSThisThread = thread.getValue4();
                    Iterator iteratorUnique = uniqueNSThisThread.iterator();
                    while (iteratorUnique.hasNext()) {
                        String TLD = (String) iteratorUnique.next();
                        uniqueNameServers.add(TLD);
                    }
                    // Update num name server total
                    numNameServersTotal += thread.getValue5();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executorService.shutdown();

//            for (String IPPrefix : listIPPrefixes) {
//                System.out.println("IPPrefix : " + IPPrefix);
//                List<String> resolvedIPAddress = RIPE_API_Loader.loadNameServersFromIPPrefix(IPPrefix);
//                for (String ip : resolvedIPAddress) {
//                    String nameServerConverted = Converter.convertIPAddressIntoHostName(ip);
//                    System.out.println("Name Server Converted : " + nameServerConverted);
//
//                    if (nameServerConverted != "") {
//                        // Cek apakah bisa dikonversi ke canonical name
//                        if ((!InetAddressValidator.getInstance().isValidInet4Address(nameServerConverted)) && (!nameServerConverted.isEmpty())) {
//                            InternetDomainName idn = InternetDomainName.from(nameServerConverted);
//                            if (idn.hasPublicSuffix()) {
//                                List<String> parts = idn.parts();
//                                String TLD = parts.get(parts.size() - 1);
//                                if (TLD.equals("com")) {
//                                    comTLDRetrieved.add(nameServerConverted);
//                                } else if (TLD.equals("org")) {
//                                    orgTLDRetrieved.add(nameServerConverted);
//                                } else if (TLD.equals("uk")) {
//                                    ukTLDRetrieved.add(nameServerConverted);
//                                } else {
//                                    nonPopularTLDRetrieved.add(nameServerConverted);
//                                }
//                            }
//                        }
//                        String[] nameServerConvertedToken = nameServerConverted.split("\\.");
//                        String nameServerConvertedUnique = "";
//                        for (int l=1;l<nameServerConvertedToken.length;l++) {
//                            nameServerConvertedUnique += nameServerConvertedToken[l];
//                            if (l < (nameServerConvertedToken.length-1)) {
//                                nameServerConvertedUnique += ".";
//                            }
//                        }
//                        System.out.println("Name Server Unique : " + nameServerConvertedUnique);
//                        uniqueNameServers.add(nameServerConvertedUnique);
//
//                        numNameServersTotal++;
//                    }
//                }
//            }
        }
        System.out.println("TOTAL COM : " + comTLDRetrieved.size());
        System.out.println("TOTAL ORG : " + orgTLDRetrieved.size());
        System.out.println("TOTAL UK : " + ukTLDRetrieved.size());
        System.out.println("TOTAL Non popular : " + nonPopularTLDRetrieved.size());
        System.out.println("TOTAL Unique : " + uniqueNameServers.size());
        System.out.println("TOTAL NUM SERVER : " + numNameServersTotal);

        // Hitung rasio keenam TLD
        double comRatio, orgRatio, ukRatio, nonPopularRatio;
        if (numNameServersTotal > 0) {
            comRatio = (double) comTLDRetrieved.size() / (double) numNameServersTotal;
            orgRatio = (double) orgTLDRetrieved.size() / (double) numNameServersTotal;
//            eduRatio = (double) eduTLDRetrieved.size() / (double) numNameServersTotal;
//            govRatio = (double) govTLDRetrieved.size() / (double) numNameServersTotal;
            ukRatio = (double) ukTLDRetrieved.size() / (double) numNameServersTotal;
            nonPopularRatio = (double) nonPopularTLDRetrieved.size() / (double) numNameServersTotal;
        } else {
            comRatio = orgRatio = ukRatio = nonPopularRatio = 0.0;
        }
        // Hitung distribusi name server unik AS
        double uniqueNSDistributionAS;
        if (numNameServersTotal > 0) {
            uniqueNSDistributionAS = (double) uniqueNameServers.size() / (double) numNameServersTotal;
        } else {
            uniqueNSDistributionAS = 0.0;
        }

        Quartet<Double, Double, Double, Double> sixRatioRetrieved = new Quartet<Double, Double, Double, Double>(comRatio, orgRatio, ukRatio, nonPopularRatio);
        return new Pair<Double, Quartet<Double, Double, Double, Double>>(uniqueNSDistributionAS, sixRatioRetrieved);
    }

    /**
     * Thread to improve TLD Distribution Performance
     */
    private static class TLDDistributionASThread implements Callable {
        private List<String> listIPPrefixes;
        private HashSet<String> comTLDRetrieved;
        private HashSet<String> orgTLDRetrieved;
        private HashSet<String> ukTLDRetrieved;
        private HashSet<String> nonPopularTLDRetrieved;
        private HashSet<String> uniqueNameServers;
        private int numNameServerTotal;

        public TLDDistributionASThread(List<String> listIPPrefixes) {
            this.listIPPrefixes = listIPPrefixes;
            comTLDRetrieved = new HashSet<String>();
            orgTLDRetrieved = new HashSet<String>();
            ukTLDRetrieved = new HashSet<String>();
            nonPopularTLDRetrieved = new HashSet<String>();
            uniqueNameServers = new HashSet<String>();
            numNameServerTotal = 0;
        }

        public Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer> getTLDDistAS() {
            for (String ipPrefix : listIPPrefixes) {
                List<String> listResolvedIPAddress = RIPE_API_Loader.loadNameServersFromIPPrefix(ipPrefix);
                for (String ipAddress : listResolvedIPAddress) {
                    String nameServerConverted = Converter.convertIPAddressIntoHostName(ipAddress);
                    if (nameServerConverted != "") {
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
                                } else if (TLD.equals("uk")) {
                                    ukTLDRetrieved.add(nameServerConverted);
                                } else {
                                    nonPopularTLDRetrieved.add(nameServerConverted);
                                }
                            }
                        }
                        String[] nameServerConvertedToken = nameServerConverted.split("\\.");
                        String nameServerConvertedUnique = "";
                        for (int l=1;l<nameServerConvertedToken.length;l++) {
                            nameServerConvertedUnique += nameServerConvertedToken[l];
                            if (l < (nameServerConvertedToken.length-1)) {
                                nameServerConvertedUnique += ".";
                            }
                        }
                        System.out.println("Name Server Unique : " + nameServerConvertedUnique);
                        uniqueNameServers.add(nameServerConvertedUnique);

                        numNameServerTotal++;
                    }
                }
            }

            return new Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer>
                    (comTLDRetrieved,orgTLDRetrieved,ukTLDRetrieved,nonPopularTLDRetrieved,uniqueNameServers,numNameServerTotal);
        }

        public Sextet<HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,HashSet<String>,Integer> call() throws Exception {
            return getTLDDistAS();
        }
    }

    /**
     * Return distribution name server from Site's Autonomous System
     *
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
        for (int i = 1; i < listInteger.size(); i++) {
            if (listInteger.get(i) > maxInteger) {
                maxInteger = listInteger.get(i);
            }
        }
        return maxInteger;
    }

    /**
     * Return list time to live for each name server owned by url
     *
     * @param url
     * @return
     */
    public static Integer getNameServerTimeToLive(String url) {
//        List<Integer> NSTTLList = MXToolbox_API_Loader.listNameServerTimeToLive(url);
//        Integer timeToLive = 0;
//        if (NSTTLList.size() > 0) {
//            timeToLive = getMaxIntegerList(NSTTLList);
//        }

        Integer timeToLive = 1000;
        try {
            Record[] records = new Lookup(url,Type.SOA).run();
            if (records != null) {
                timeToLive = (int) records[0].getTTL();
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
        return timeToLive;
    }

    /**
     * Return list time to live for DNS A record retrieved from its corresponding name server
     *
     * @param url
     * @return
     */
    public static Integer getDNSRecordTimeToLive(String url) {
//        List<Integer> IPTTLList = MXToolbox_API_Loader.listIPAddressTimeToLive(url);
//        Integer timeToLive = 0;
//        if (IPTTLList.size() > 0) {
//            timeToLive = getMaxIntegerList(IPTTLList);
//        }

        Integer timeToLive = 1000;
        try {
            Record[] records = new Lookup(url,Type.A).run();
            if (records != null) {
                timeToLive = (int) records[0].getTTL();
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
        return timeToLive;
    }

    public static void main(String[] args) {
        List<String> listSites = EksternalFile.loadSitesTrainingList(1).getKey();
        for (int k=0;k<1;k++) {
            String hostName = listSites.get(3);

            List<Object> fiturs = new ArrayList<Object>();

            long before = System.currentTimeMillis();

            // TLD ratio
            Pair<Double, Quartet<Double, Double, Double, Double>> pairDistAndTLDRatio = DNSExtractor.getTLDDistributionFromAS(hostName);
            Double[] TLDRatioList = new Double[6];
            TLDRatioList[0] = pairDistAndTLDRatio.getValue1().getValue0();
            fiturs.add(TLDRatioList[0]);
            TLDRatioList[1] = pairDistAndTLDRatio.getValue1().getValue1();
            fiturs.add(TLDRatioList[1]);
            TLDRatioList[2] = pairDistAndTLDRatio.getValue1().getValue2();
            fiturs.add(TLDRatioList[2]);
            TLDRatioList[3] = pairDistAndTLDRatio.getValue1().getValue3();
            fiturs.add(TLDRatioList[3]);
            System.out.println("TLD Ratio");

            long afterTLD = System.currentTimeMillis();

            // Hit AS Ratio (malware, phishing, spamming)
            Double[] HitRatioList = new Double[3];
            int thisSiteASN = Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress(hostName));
            for (int j = 0; j < 3; j++) {
                HitRatioList[j] = DNSExtractor.getHitASRatio(thisSiteASN, j + 1);
                fiturs.add(HitRatioList[j]);
            }
            System.out.println("Hit AS Ratio");

            long afterHitRatio = System.currentTimeMillis();

            // Name server distribution AS
            double distributionNS = pairDistAndTLDRatio.getValue0();
            fiturs.add(distributionNS);
            System.out.println("Name Server Distribution AS");

            long afterNSDist = System.currentTimeMillis();

            // Name server count
            int numNameServer = DNSExtractor.getNumNameServers(hostName);
            fiturs.add(numNameServer);
            System.out.println("Name Server Count");

            long afterNSCount = System.currentTimeMillis();

            // TTL Name Servers
            int NSTTL = DNSExtractor.getNameServerTimeToLive(hostName);
            fiturs.add(NSTTL);
            System.out.println("TTL Name Servers");

            long afterTTLNS = System.currentTimeMillis();

            // TTL DNS A Records
            int IPTTL = DNSExtractor.getDNSRecordTimeToLive(hostName);
            fiturs.add(IPTTL);
            System.out.println("TTL DNS Record");

            long afterTTLIP = System.currentTimeMillis();

            double[] values = new double[fiturs.size()];
            for (int i = 0; i < fiturs.size(); i++) {
                values[i] = new Double(fiturs.get(i).toString());
            }
//            System.out.println("LIST VALUES SITE-" + (k+1));
            for (double d : values) {
                System.out.println(d);
            }
            long after = System.currentTimeMillis();

            System.out.println("Waktu eksekusi : " + (after - before));
            System.out.println("Waktu eksekusi TLD ratio : " + (afterTLD - before));
            System.out.println("Waktu eksekusi Hit ratio : " + (afterHitRatio - afterTLD));
            System.out.println("Waktu eksekusi NS distribution : " + (afterNSDist - afterHitRatio));
            System.out.println("Waktu eksekusi NS count : " + (afterNSCount - afterNSDist));
            System.out.println("Waktu eksekusi TTL NS : " + (afterTTLNS - afterNSCount));
            System.out.println("Waktu eksekusi TTL IP : " + (afterTTLIP - afterTTLNS));
            System.out.println("==================================================================================");
        }
    }
}
