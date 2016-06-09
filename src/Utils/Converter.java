package Utils;

import Utils.API.MXToolbox_API_Loader;
import Utils.DNS.DNSExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by steve on 28/01/2016.
 */
public class Converter {
    public static String convertIPAddressIntoHostName(String IPAddress) {
        String hostName = "";
        try {
            InetAddress addr = InetAddress.getByName(IPAddress);
            hostName = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }

    public static String convertHostNameIntoIPAddress(String hostName) {
//        String IPAddress = "";
//        try {
//            InetAddress addr = InetAddress.getByName(hostName);
//            IPAddress = addr.getHostAddress();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        return IPAddress;
//        return MXToolbox_API_Loader.HostNameToIPAddress(hostName);

        // Delete path from hostname
        String pureHostName = hostName.split("/")[0];
        // Execute command ping for that domain
        Runtime rt = Runtime.getRuntime();
        String commandExec = "ping " + pureHostName;
        // Get raw string buffer result
        String ipAddress = "1.1.1.1";
        try {
            Process pr = rt.exec(commandExec);
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            StringBuffer outputCommand = new StringBuffer();
            String rawContent = "";
            int rowSize = 0;
            while ((rawContent = commandReader.readLine()) != null) {
                outputCommand.append(rawContent + "\n");
                rowSize++;
            }
            if (rowSize > 1) {
                StringTokenizer stringPerLine = new StringTokenizer(outputCommand.toString(),"\n");
                while (stringPerLine.hasMoreTokens()) {
                    String stringThisLine = stringPerLine.nextToken();
                    if (stringThisLine.contains("Pinging ")) {
                        StringTokenizer words = new StringTokenizer(stringThisLine," ");
                        int counter = 1;
                        while (words.hasMoreTokens()) {
                            String ipAddressWord = words.nextToken();
                            if (counter == 3) {
                                ipAddress = ipAddressWord.replace("[","").replace("]","");
                            }
                            counter++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ipAddress;
    }

    public static Boolean checkIfDomainResolved(String hostName) {
        Boolean isDomainResolved = true;
        try {
            InetAddress.getByName(hostName).isReachable(10000);
        } catch (IOException e) {
            isDomainResolved = false;
            e.printStackTrace();
        }
        return isDomainResolved;
    }

    public static List<String> convertPrefixIntoResolvedIPAddress(String IPPrefix) {
        List<String> IPAddressResolved = new ArrayList<String>();

        Runtime rt = Runtime.getRuntime();
        String commandExec = "nmap -sL " + IPPrefix;
        try {
            Process pr = rt.exec(commandExec);
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = commandReader.readLine()) != null) {
                if (line.contains("Nmap scan report for ")) {
                    String IPAddress = line.replace("Nmap scan report for ","");
                    IPAddressResolved.add(IPAddress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return IPAddressResolved;
    }

    public static int convertIPAddressIntoASN(String IPAddress) {
        int ASNNumber = 0;

        if (IPAddress != "") {
            Runtime rt = Runtime.getRuntime();
            String commandExec = "D:\\steve\\Library\\curl\\curl.exe ipinfo.io/" + IPAddress + "/org";
            try {
                Process pr = rt.exec(commandExec);
                BufferedReader commandReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                StringBuffer outputCommand = new StringBuffer();
                String line = "";
                while ((line = commandReader.readLine()) != null) {
                    outputCommand.append(line + "\n");
                }
                if (!outputCommand.toString().contains("undefined")) {
                    String[] token = outputCommand.toString().split(" ");
                    ASNNumber = Integer.parseInt(token[0].replace("AS", ""));
                }
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ASNNumber;
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
//        List<String> ipAddress = Converter.convertPrefixIntoResolvedIPAddress("217.16.179.17/32");
//        for (String ip : ipAddress) {
//            System.out.println(Converter.convertIPAddressIntoHostName(ip));
//        }
//        long before = System.currentTimeMillis();
//        Double[] HitRatioList = new Double[3];
//        for (int j=0;j<3;j++) {
//            HitRatioList[j] = DNSExtractor.getHitASRatio("0000love.net",j+1);
//        }
//        long after_1 = System.currentTimeMillis();
//
//        System.out.println((after_1-before));

//        long begin = System.currentTimeMillis();
//        System.out.println("Is Domain Name Available? " + Converter.checkIfDomainResolved("facebook.com"));
//        long end = System.currentTimeMillis();
//
//        System.out.println("Waktu eksekusi : " + (end-begin));

//        long begin = System.currentTimeMillis();
//        System.out.println("ASNumber : " + Converter.convertIPAddressIntoASN(Converter.convertHostNameIntoIPAddress("facebook.com")));
//        long end = System.currentTimeMillis();
//        System.out.println("EXECUTION TIME : " + (end-begin));

        for (int i=3;i<=3;i++) {
            DNSExtractor.saveASNSitesToExternalFile(i);
        }
    }
}
