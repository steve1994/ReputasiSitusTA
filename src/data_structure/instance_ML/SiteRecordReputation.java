package data_structure.instance_ML;

import data_structure.feature.DNS_Feature;
import data_structure.feature.Spesific_Feature;
import data_structure.feature.Trust_Feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by steve on 26/03/2016.
 */
public class SiteRecordReputation {
    private Boolean[]  listCombinationRecordType;
    private List<List<List<Object>>> listRecordDataCombination;

    // GETTER
    public Boolean[] getListCombinationRecordType() {
        return listCombinationRecordType;
    }

    public List<List<List<Object>>> getListRecordDataCombination() {
        return listRecordDataCombination;
    }

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     * @param typeReputation
     */
    public SiteRecordReputation(int typeReputation) {
        listCombinationRecordType = new Boolean[3]; // 3 aspek reputasi situs (trust, DNS, Spesific)
        listRecordDataCombination = new ArrayList<List<List<Object>>>();

        for (int i=0;i<3;i++) {
            listCombinationRecordType[i] = false;
        }
        switch (typeReputation) {
            default:
            case 1:                 // DNS
                listCombinationRecordType[0] = true;
                break;
            case 2:                 // Spesific
                listCombinationRecordType[1] = true;
                break;
            case 3:                 // Trust
                listCombinationRecordType[2] = true;
                break;
            case 4:                 // DNS + Spesific
                listCombinationRecordType[0] = true;
                listCombinationRecordType[1] = true;
                break;
            case 5:                 // DNS + Trust
                listCombinationRecordType[0] = true;
                listCombinationRecordType[2] = true;
                break;
            case 6:                 // Spesific + Trust
                listCombinationRecordType[1] = true;
                listCombinationRecordType[2] = true;
                break;
            case 7:                 // DNS + Spesific + Trust
                listCombinationRecordType[0] = true;
                listCombinationRecordType[1] = true;
                listCombinationRecordType[2] = true;
                break;
        }
    }

    /**
     * Insert new tuple data regarding site reputation if known recordElementDNS,
     * recordElementSpesific, and recordElementTrust from that site
     * @param recordElementDNS : may null
     * @param recordElementSpesific : may null
     * @param recordElementTrust : may null
     */
    public void insertSiteRecordReputation(DNS_Feature recordElementDNS, Spesific_Feature recordElementSpesific, Trust_Feature recordElementTrust) {
        List<List<Object>> tupleRecord = new ArrayList<List<Object>>();
        if (listCombinationRecordType[0] == true) {
            List<Object> DNSFeatureCollection = new ArrayList<Object>();

            // Five popular TLD ratio in AS
            Float[] TLDratio = recordElementDNS.getPopularTLDRatio();
            for (Float TLD : TLDratio) {
                DNSFeatureCollection.add(TLD);
            }
            // Hit AS ratio in AS
            Integer[] hitASRatio = recordElementDNS.getHitASRatio();
            for (Integer hit : hitASRatio) {
                DNSFeatureCollection.add(hit);
            }
            // Distribution Name Server AS
            DNSFeatureCollection.add(recordElementDNS.getDistributionNSAS());
            // Name server count
            DNSFeatureCollection.add(recordElementDNS.getNumNameServer());
            // List TTL name servers
            List<Integer> listTTLNS = recordElementDNS.getListNSTTL();
            for (Integer ttl : listTTLNS) {
                DNSFeatureCollection.add(ttl);
            }
            // List TTL DNS A Records
            List<Integer> listTTLDNSRecord = recordElementDNS.getListDNSRecordTTL();
            for (Integer ttl : listTTLDNSRecord) {
                DNSFeatureCollection.add(ttl);
            }

            tupleRecord.add(0,DNSFeatureCollection);
        }
        if (listCombinationRecordType[1] == true) {
            List<Object> SpesificFeatureCollection = new ArrayList<Object>();

            // Token count URL
            SpesificFeatureCollection.add(recordElementSpesific.getTokenCountURL());
            // Average Token Length URL
            SpesificFeatureCollection.add(recordElementSpesific.getAverageTokenLengthURL());
            // SLD ratio from URL (malware, phishing, spamming)
            SpesificFeatureCollection.add(recordElementSpesific.getSLDRatio());

            tupleRecord.add(1,SpesificFeatureCollection);
        }
        if (listCombinationRecordType[2] == true) {
            //tupleRecord.add(2,recordElementTrust);
        }
        listRecordDataCombination.add(tupleRecord);
    }
}
