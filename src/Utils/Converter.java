package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
        String IPAddress = "";
        try {
            InetAddress addr = InetAddress.getByName(hostName);
            IPAddress = addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return IPAddress;
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
        int ASNNumber = -9999;
        Runtime rt = Runtime.getRuntime();
        String commandExec = "curl ipinfo.io/" + IPAddress + "/org";
        try {
            Process pr = rt.exec(commandExec);
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            StringBuffer outputCommand = new StringBuffer();
            String line = "";
            while ((line = commandReader.readLine()) != null) {
                outputCommand.append(line + "\n");
            }
            String[] token = outputCommand.toString().split(" ");
            ASNNumber = Integer.parseInt(token[0].replace("AS",""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ASNNumber;
    }

    public static void main(String[] args) {
        List<String> ipAddress = Converter.convertPrefixIntoResolvedIPAddress("217.16.179.17/32");
        for (String ip : ipAddress) {
            System.out.println(Converter.convertIPAddressIntoHostName(ip));
        }
    }
}
