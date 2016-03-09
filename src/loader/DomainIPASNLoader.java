package loader;

import Utils.Converter;
import Utils.Database.EksternalFile;
import data_structure.DomainNameIPAddressMapping;
import data_structure.IPAddressASNMapping;

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

    private void loadIPAddressASNMapping() {
        // Store list IP Address from DomainIPMapping
        List<String> listIPAddress = new ArrayList<String>();
        for (Map.Entry m : mappingDomainIP.getMappingDomainNameIntoIPAddress().entrySet()) {
            String IPAddress = (String) m.getValue();
            listIPAddress.add(IPAddress);
        }

        // For each IP Address, execute curl ipinfo.io/[ip address]/org to get asn number
        for (String ip : listIPAddress) {
            int ASN = Converter.convertIPAddressIntoASN(ip);
            mappingIPASN.insertASNNumberRelated(ip,ASN);
        }
    }

    private void loadDomainNameIPAddressMapping() {
        // Get list of domain from raw content
        StringTokenizer domainName = new StringTokenizer(EksternalFile.getRawFileContent(pathListURL),"\n");
        while (domainName.hasMoreTokens()) {
            String domain = domainName.nextToken();
            String IPAddress = Converter.convertHostNameIntoIPAddress(domain);
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
