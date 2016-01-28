package loader;

import data_structure.DomainNameIPAddressMapping;
import data_structure.IPAddressASNMapping;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by steve on 27/01/2016.
 */
public class DomainIPASNLoader {
    private static final String pathListURL = "database\\DomainJanuary2016\\tes.txt";
    private DomainNameIPAddressMapping mappingDomainIP;
    private IPAddressASNMapping mappingIPASN;

    public IPAddressASNMapping getMappingIPASN() {
        return mappingIPASN;
    }

    public DomainNameIPAddressMapping getMappingDomainIP() {
        return mappingDomainIP;
    }

    public DomainIPASNLoader() {
        mappingDomainIP = new DomainNameIPAddressMapping();
        mappingIPASN = new IPAddressASNMapping();
    }

    private String convertDomainNameIntoIPAddress(String domainName) {
        String IPAddressResult = "";
        try {
            InetAddress domain = InetAddress.getByName(domainName);
            IPAddressResult = domain.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return IPAddressResult;
    }

    private int getASNNumberFromCurlExec(String execOutput) {
        int ASNNumber;
        String[] token = execOutput.split(" ");
        ASNNumber = Integer.parseInt(token[0].replace("AS",""));
        return ASNNumber;
    }

    private void loadIPAddressASNMapping() {
        // Store list IP Address from DomainIPMapping
        List<String> listIPAddress = new ArrayList<String>();
        for (Map.Entry m : mappingDomainIP.getMappingDomainNameIntoIPAddress().entrySet()) {
            String IPAddress = (String) m.getValue();
            listIPAddress.add(IPAddress);
        }

        // For each IP Address, execute curl ipinfo.io/[ip address]/org to get asn number
        for (String ip : listIPAddress) {
            Runtime rt = Runtime.getRuntime();
            String commandExec = "curl ipinfo.io/" + ip + "/org";
            try {
                Process pr = rt.exec(commandExec);
                BufferedReader commandReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                StringBuffer outputCommand = new StringBuffer();
                String line = "";
                while ((line = commandReader.readLine()) != null) {
                    outputCommand.append(line + "\n");
                }
                int ASNNumber = getASNNumberFromCurlExec(outputCommand.toString());
                // Put result in IP-ASN mapping
                mappingIPASN.insertASNNumberRelated(ip,ASNNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadDomainNameIPAddressMapping() {
        // Get raw file content
        StringBuffer rawFileContent = new StringBuffer();
        String  thisLine;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathListURL));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((thisLine = br.readLine()) != null) {
                rawFileContent.append(thisLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get list of domain from raw content
        StringTokenizer domainName = new StringTokenizer(rawFileContent.toString(),"\n");
        while (domainName.hasMoreTokens()) {
            String domain = domainName.nextToken();
            String IPAddress = convertDomainNameIntoIPAddress(domain);
            mappingDomainIP.insertIPAddressRelatedDomainName(IPAddress,domain);
        }
    }

    public void loadDomainIPASNController() {
        loadDomainNameIPAddressMapping();
        if (!mappingDomainIP.getMappingDomainNameIntoIPAddress().isEmpty()) {
            loadIPAddressASNMapping();
        }
    }

    public static void main(String[] args) {
        DomainIPASNLoader domainNameList = new DomainIPASNLoader();
        domainNameList.loadDomainIPASNController();
        for (Map.Entry m : domainNameList.getMappingDomainIP().getMappingDomainNameIntoIPAddress().entrySet()) {
            String domainName = (String) m.getKey();
            String IPAddress = (String) m.getValue();
            System.out.println("Domain Name : " + domainName);
            System.out.println("IP Address Terkait : " + IPAddress);
        }
        System.out.println("======================================================");
        for (Map.Entry n : domainNameList.getMappingIPASN().getMappingIPAddressIntoASN().entrySet()) {
            String IPAddress = (String) n.getKey();
            int ASNNumber = (Integer) n.getValue();
            System.out.println("IP Address : " + IPAddress);
            System.out.println("ASN Number Terkait : " + ASNNumber);
        }
    }
}
