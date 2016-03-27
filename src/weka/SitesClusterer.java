package weka;

import data_structure.instance_ML.SiteRecordReputation;
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
        List<Attribute> overallInstanceVector = new ArrayList<Attribute>();
        // DNS
        if (recordSiteData.getListCombinationRecordType()[0] == true) {
            // Popular TLD ratio in AS
            Attribute ratioTLDAsCom = new Attribute("TLD_AS_com"); overallInstanceVector.add(ratioTLDAsCom);
            Attribute ratioTLDAsOrg = new Attribute("TLD_AS_org"); overallInstanceVector.add(ratioTLDAsOrg);
            Attribute ratioTLDAsEdu = new Attribute("TLD_AS_edu"); overallInstanceVector.add(ratioTLDAsEdu);
            Attribute ratioTLDAsGov = new Attribute("TLD_AS_gov"); overallInstanceVector.add(ratioTLDAsGov);
            Attribute ratioTLDAsUk = new Attribute("TLD_AS_uk"); overallInstanceVector.add(ratioTLDAsUk);
            Attribute ratioTLDAsRest = new Attribute("TLD_AS_rest"); overallInstanceVector.add(ratioTLDAsRest);
            // Hit AS ratio in malware / phishing / spamming
            Attribute ratioHitAsMalware = new Attribute("Hit_AS_mal"); overallInstanceVector.add(ratioHitAsMalware);
            Attribute ratioHitAsPhishing = new Attribute("Hit_AS_phis"); overallInstanceVector.add(ratioHitAsPhishing);
            Attribute ratioHitAsSpamming = new Attribute("Hit_AS_spam"); overallInstanceVector.add(ratioHitAsSpamming);
            // Distribution name server ratio in AS
            Attribute distributionNSAs = new Attribute("NS_Dist_AS"); overallInstanceVector.add(distributionNSAs);
            // Name server count
            Attribute NSCount = new Attribute("NS_Count"); overallInstanceVector.add(NSCount);
            // Name server time to live (array)
            Attribute TTLNS = new Attribute("TTL_NS"); overallInstanceVector.add(TTLNS);
            // DNS record time to live (array)
            Attribute TTLDNSRecord = new Attribute("TTL_DNS_Record"); overallInstanceVector.add(TTLDNSRecord);
        }
        // Spesific
        if (recordSiteData.getListCombinationRecordType()[1] == true) {
            // Token count in site URL (path excluded)
            Attribute tokenCount = new Attribute("token_count"); overallInstanceVector.add(tokenCount);
            // Average token length in site URL (path excluded)
            Attribute averageTokenCount = new Attribute("avg_token_count"); overallInstanceVector.add(averageTokenCount);
            // SLD ratio from site URL (malware, phsihing, spamming)
            Attribute ratioSLDMalware = new Attribute("SLD_URL_mal"); overallInstanceVector.add(ratioSLDMalware);
            Attribute ratioSLDPhishing = new Attribute("SLD_URL_phis"); overallInstanceVector.add(ratioSLDPhishing);
            Attribute ratioSLDSpamming = new Attribute("SLD_URL_spam"); overallInstanceVector.add(ratioSLDSpamming);
            // Inbound link approximation from 3 search engine (Google, Yahoo, Bing)
            Attribute inboundLinkGoogle = new Attribute("inbound_link_google"); overallInstanceVector.add(inboundLinkGoogle);
            Attribute inboundLinkYahoo = new Attribute("inbound_link_yahoo"); overallInstanceVector.add(inboundLinkYahoo);
            Attribute inboundLinkBing = new Attribute("inbound_link_bing"); overallInstanceVector.add(inboundLinkBing);
            // Lookup time to access site
            Attribute lookupTime = new Attribute("lookup_time"); overallInstanceVector.add(lookupTime);
        }
        // Trust
        if (recordSiteData.getListCombinationRecordType()[2] == true) {
            // Trustworthy Score
            Attribute trustWorthy1 = new Attribute("trust_score_1"); overallInstanceVector.add(trustWorthy1);
            Attribute trustWorthy2 = new Attribute("trust_score_2"); overallInstanceVector.add(trustWorthy2);
            // Child Safety Score
            Attribute childSafety1 = new Attribute("safety_score_1"); overallInstanceVector.add(childSafety1);
            Attribute childSafety2 = new Attribute("safety_score_2"); overallInstanceVector.add(childSafety2);
            // Category Score (negative, questionable, neutral, positive)
            Attribute categoryEstimateNegative = new Attribute("category_negative"); overallInstanceVector.add(categoryEstimateNegative);
            Attribute categoryEstimateQuestionable = new Attribute("category_questionable"); overallInstanceVector.add(categoryEstimateQuestionable);
            Attribute categoryEstimateNeutral = new Attribute("category_neutral"); overallInstanceVector.add(categoryEstimateNeutral);
            Attribute categoryEstimatePositive = new Attribute("category_positive"); overallInstanceVector.add(categoryEstimatePositive);
            // Blacklisting detected (malware, phishing, scam, spam)
            Attribute blacklistTypeMalware = new Attribute("blacklist_mal"); overallInstanceVector.add(blacklistTypeMalware);
            Attribute blacklistTypePhishing = new Attribute("blacklist_phis"); overallInstanceVector.add(blacklistTypePhishing);
            Attribute blacklistTypeScam = new Attribute("blacklist_scam"); overallInstanceVector.add(blacklistTypeScam);
            Attribute blacklistTypeSpam = new Attribute("blacklist_spam"); overallInstanceVector.add(blacklistTypeSpam);
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
//                Instances ratioTLDFromAS = new Instances(siteReputationRecord.attribute(0).relation(),0);
//                double[] valuesRatioTLD = new double[ratioTLDFromAS.numAttributes()];
//                for (int i=0;i<valuesRatioTLD.length;i++) {
//                    valuesRatioTLD[i] = (Double) DNSRecord.get(i);
//                }

            }
        }
    }
}
