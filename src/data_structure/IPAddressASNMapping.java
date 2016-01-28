package data_structure;

import java.util.HashMap;

/**
 * Created by steve on 28/01/2016.
 */
public class IPAddressASNMapping {
    private HashMap<String,Integer> mappingIPAddressIntoASN;

    public HashMap<String, Integer> getMappingIPAddressIntoASN() {
        return mappingIPAddressIntoASN;
    }

    public IPAddressASNMapping() {
        mappingIPAddressIntoASN = new HashMap<String, Integer>();
    }

    public void insertASNNumberRelated(String IPAddress, int ASNNumber) {
        mappingIPAddressIntoASN.put(IPAddress,ASNNumber);
    }
}
