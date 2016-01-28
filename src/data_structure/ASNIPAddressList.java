package data_structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by steve on 28/01/2016.
 */
public class ASNIPAddressList {
    private HashMap<Integer,List<String>> mappingASNIntoIPAddress;

    public HashMap<Integer, List<String>> getMappingASNIntoIPAddress() {
        return mappingASNIntoIPAddress;
    }

    public ASNIPAddressList() {
        mappingASNIntoIPAddress = new HashMap<Integer, List<String>>();
    }

    public void insertASNIPAddressMapping(int ASNNumber, String IPAddress) {
        if (mappingASNIntoIPAddress.containsKey(ASNNumber)) {
            List<String> currentIPAddressList = mappingASNIntoIPAddress.get(ASNNumber);
            currentIPAddressList.add(IPAddress);
        } else {
            List<String> newIPAddressList = new ArrayList<String>();
            newIPAddressList.add(IPAddress);
            mappingASNIntoIPAddress.put(ASNNumber,newIPAddressList);
        }
    }
}
