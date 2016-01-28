package data_structure;

import java.util.HashMap;

/**
 * Created by steve on 28/01/2016.
 */
public class DomainNameIPAddressMapping {
    private HashMap<String,String> mappingDomainNameIntoIPAddress;

    public HashMap<String, String> getMappingDomainNameIntoIPAddress() {
        return mappingDomainNameIntoIPAddress;
    }

    public DomainNameIPAddressMapping() {
        mappingDomainNameIntoIPAddress = new HashMap<String, String>();
    }

    public void insertIPAddressRelatedDomainName(String IPAddress, String domainName) {
        mappingDomainNameIntoIPAddress.put(domainName,IPAddress);
    }
}
