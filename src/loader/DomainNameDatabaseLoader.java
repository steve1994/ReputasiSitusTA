package loader;

import com.sun.java.browser.plugin2.DOM;
import data_structure.DomainNameIPAddressMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by steve on 27/01/2016.
 */
public class DomainNameDatabaseLoader {
    private static final String pathListURL = "database\\DomainJanuary2016\\tes.txt";
    private DomainNameIPAddressMapping mappingDomainIP;

    public DomainNameIPAddressMapping getMappingDomainIP() {
        return mappingDomainIP;
    }

    public DomainNameDatabaseLoader() {
        mappingDomainIP = new DomainNameIPAddressMapping();
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

    public void loadListDomainName() {
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

    public static void main(String[] args) {
        DomainNameDatabaseLoader domainNameList = new DomainNameDatabaseLoader();
        domainNameList.loadListDomainName();
        for (Map.Entry m : domainNameList.getMappingDomainIP().getMappingDomainNameIntoIPAddress().entrySet()) {
            String domainName = (String) m.getKey();
            String IPAddress = (String) m.getValue();
            System.out.println("Domain Name : " + domainName);
            System.out.println("IP Address Terkait : " + IPAddress);
        }
    }
}
