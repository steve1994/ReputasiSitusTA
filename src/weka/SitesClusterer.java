package weka;

import data_structure.SiteRecordReputation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 26/03/2016.
 */
public class SitesClusterer {
    private int numCluster = 0;
    private Instances siteReputationRecord;
    SiteRecordReputation recordSiteData;

    public SitesClusterer(int numCluster, SiteRecordReputation recordSiteData) {
        this.numCluster = numCluster;
        this.recordSiteData = recordSiteData;
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     */
    public void configARFFInstance() {
        // Filling Attribute Vector Used based on combination type
        List<Attribute> overallInstanceVector = new ArrayList<Attribute>();
        // DNS
        if (recordSiteData.getListCombinationRecordType()[0] == true) {
            Attribute ratioTLDAs = new Attribute("TLD_AS"); overallInstanceVector.add(ratioTLDAs);
            Attribute ratioHitAs = new Attribute("Hit_AS"); overallInstanceVector.add(ratioHitAs);
            Attribute distributionNSAs = new Attribute("NS_Dist_AS"); overallInstanceVector.add(distributionNSAs);
            Attribute NSCount = new Attribute("NS_Count"); overallInstanceVector.add(NSCount);
            Attribute TTLNS = new Attribute("TTL_NS"); overallInstanceVector.add(TTLNS);
            Attribute TTLDNSRecord = new Attribute("TTL_DNS_Record"); overallInstanceVector.add(TTLDNSRecord);
        }
        // Spesific
        if (recordSiteData.getListCombinationRecordType()[1] == true) {
            Attribute tokenCount = new Attribute("token_count"); overallInstanceVector.add(tokenCount);
            Attribute averageTokenCount = new Attribute("avg_token_count"); overallInstanceVector.add(averageTokenCount);
            Attribute ratioSLD = new Attribute("SLD_URL"); overallInstanceVector.add(ratioSLD);
            Attribute inboundLink = new Attribute("inbound_link"); overallInstanceVector.add(inboundLink);
            Attribute lookupTime = new Attribute("lookup_time"); overallInstanceVector.add(lookupTime);
        }
        // Trust
        if (recordSiteData.getListCombinationRecordType()[2] == true) {
            Attribute trustWorthy1 = new Attribute("trust_score_1"); overallInstanceVector.add(trustWorthy1);
            Attribute trustWorthy2 = new Attribute("trust_score_2"); overallInstanceVector.add(trustWorthy2);
            Attribute childSafety1 = new Attribute("safety_score_1"); overallInstanceVector.add(childSafety1);
            Attribute childSafety2 = new Attribute("safety_score_2"); overallInstanceVector.add(childSafety2);
            Attribute categoryEstimate = new Attribute("category"); overallInstanceVector.add(categoryEstimate);
            Attribute blacklistType = new Attribute("blacklist"); overallInstanceVector.add(blacklistType);
        }
        // Setting Attributes Vector Overall to Instance Record
        FastVector attributeInstanceRecord = new FastVector();
        for (Attribute attr : overallInstanceVector) {
            attributeInstanceRecord.addElement(attr);
        }
        siteReputationRecord = new Instances("Reputation Site Dataset",attributeInstanceRecord,0);
    }

    public void fillDataIntoInstanceRecord() {
        List<List<List<Object>>> rawRecordsSiteReputation = recordSiteData.getListRecordDataCombination();
        for (List<List<Object>> oneTuple : rawRecordsSiteReputation) {
            List<Object> DNSRecord = oneTuple.get(0);
            List<Object> SpesificRecord = oneTuple.get(1);
            List<Object> TrustRecord = oneTuple.get(2);
            if (recordSiteData.getListCombinationRecordType()[0] == true) {
//                Instances dataRel = new Instances(data.attribute(4).relation(),0);
//                valuesRel = new double[dataRel.numAttributes()];
//                valuesRel[0] = 2.34;
//                valuesRel[1] = dataRel.attribute(1).indexOf("val_C");
//                dataRel.add(new Instance(1.0, valuesRel));
//                values[4] = data.attribute(4).addRelation(dataRel);
                // Rasio 5 tld terpopuler
                Instances ratioTLDFromAS = new Instances(siteReputationRecord.attribute(0).relation(),0);
                double[] valuesRatioTLD = new double[ratioTLDFromAS.numAttributes()];
                for (int i=0;i<valuesRatioTLD.length;i++) {
                    valuesRatioTLD[i] = (Double) DNSRecord.get(i);
                } 
            }
        }
    }
}
