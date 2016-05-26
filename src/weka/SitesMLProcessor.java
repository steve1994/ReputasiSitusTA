package weka;

import data_structure.instance_ML.SiteRecordReputation;
import org.javatuples.Pair;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by steve on 29/03/2016.
 */
public class SitesMLProcessor {
    protected Instances siteReputationRecord;
    protected Boolean[] listCombinationRecordType;

    public Instances getSiteReputationRecord() {
        return siteReputationRecord;
    }

    protected Boolean[] getListCombinationRecordType() {
        return listCombinationRecordType;
    }

    protected FastVector getAttributesVector(Instances dataset) {
        dataset.setClassIndex(dataset.numAttributes()-1);

        FastVector attributesVectorDataset = new FastVector();
        Enumeration attributesDataset = dataset.enumerateAttributes();
        while (attributesDataset.hasMoreElements()) {
            attributesVectorDataset.addElement((Attribute) attributesDataset.nextElement());
        }
        attributesVectorDataset.addElement(dataset.classAttribute());

        return attributesVectorDataset;
    }

    protected SitesMLProcessor(int typeReputation) {
        listCombinationRecordType = new Boolean[3];         // DNS, Spesific, Trust
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

    protected List<Attribute> getInstanceAttributes() {
        List<Attribute> overallInstanceVector = new ArrayList<Attribute>();
        // DNS
        if (listCombinationRecordType[0] == true) {
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
        if (listCombinationRecordType[1] == true) {
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
        if (listCombinationRecordType[2] == true) {
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
        return overallInstanceVector;
    }

    protected List<Object> getInstanceRecord(SiteRecordReputation recordReputation) {
        List<Object> instanceValues = new ArrayList<Object>();
        if (listCombinationRecordType[0] == true) {
            // Popular TLD ratio in AS
            Double[] tldRatio = recordReputation.getDNSRecordFeature().getPopularTLDRatio();
            for (Double tld : tldRatio) {
                instanceValues.add(tld);
            }
            // Hit AS Ratio in malware / phishing / spamming
            Double[] hitASRatio = recordReputation.getDNSRecordFeature().getHitASRatio();
            for (Double hit : hitASRatio) {
                instanceValues.add(hit);
            }
            // Distribution name server in AS
            instanceValues.add(recordReputation.getDNSRecordFeature().getDistributionNSAS());
            // Name server count
            instanceValues.add(recordReputation.getDNSRecordFeature().getNumNameServer());
            // Time To Live Name server
            instanceValues.add(recordReputation.getDNSRecordFeature().getListNSTTL());
            // Time To Live DNS A Record
            instanceValues.add(recordReputation.getDNSRecordFeature().getListDNSRecordTTL());
        }
        if (listCombinationRecordType[1] == true) {
            // Token Count URL
            instanceValues.add(recordReputation.getSpesificRecordFeature().getTokenCountURL());
            // Average Token Length URL
            instanceValues.add(recordReputation.getSpesificRecordFeature().getAverageTokenLengthURL());
            // SLD ratio from URL (malware, phishing, spamming)
            double[] SLDRatio = recordReputation.getSpesificRecordFeature().getSLDRatio();
            for (double sld : SLDRatio) {
                instanceValues.add(sld);
            }
            // Inbound link Approximation (Google, Yahoo, Bing)
            int[] inboundLink = recordReputation.getSpesificRecordFeature().getInboundLink();
            for (int link : inboundLink) {
                instanceValues.add(link);
            }
            // Lookup time to access site
            instanceValues.add(recordReputation.getSpesificRecordFeature().getLookupTime());
        }
        if (listCombinationRecordType[2] == true) {
            // Trustworthy Score
            instanceValues.add(recordReputation.getTrustRecordFeature().getTrustWorthinessPairValues().getKey());
            instanceValues.add(recordReputation.getTrustRecordFeature().getTrustWorthinessPairValues().getValue());
            // Child Safety Score
            instanceValues.add(recordReputation.getTrustRecordFeature().getChildSafetyPairValues().getKey());
            instanceValues.add(recordReputation.getTrustRecordFeature().getChildSafetyPairValues().getValue());
            // Category Estimation Score
            Integer[] estimationCategories = recordReputation.getTrustRecordFeature().getCategoryEstimateValues();
            for (Integer category : estimationCategories) {
                instanceValues.add(category);
            }
            // Blacklist Detection Trust
            Integer[] blacklistTrust = recordReputation.getTrustRecordFeature().getBlacklistIncluded();
            for (Integer blacklist : blacklistTrust) {
                instanceValues.add(blacklist);
            }
        }
        return instanceValues;
    }

    /**
     * Get statistic about not null data percentage each attribute per instance weka
     * @param instances
     * @return
     */
    public List<Pair<String,Double>> getPercentageNotNullData(Instances instances) {
        List<Pair<String,Double>> vectorPercentageEachAttribute = new ArrayList<Pair<String, Double>>();

        // Initialize counter not null data for each attribute
        int[] counterNotNullData = new int[instances.numAttributes()];
        for (int i=0;i<instances.numAttributes();i++) {
            counterNotNullData[i] = 0;
        }
        // Enumerate instance to get not null data for each attribute
        for (int i=0;i<instances.numInstances();i++) {
            Instance thisInstance = instances.instance(i);
            for (int j=0;j<instances.numAttributes();j++) {
                double valueThisAttribute = thisInstance.value(j);
                String attrName = thisInstance.attribute(j).name();
                if (attrName.equals("token_count") || attrName.equals("avg_token_count") || attrName.equals("lookup_time")) {
                    counterNotNullData[j]++;
                } else {
                    if (valueThisAttribute > 0) {
                        counterNotNullData[j]++;
                    }
                }
            }
        }
        // Calculate not null data percentage based on counter
        for (int i=0;i<instances.numAttributes();i++) {
            double percentageThisAttr = (double) counterNotNullData[i] / (double) instances.numInstances();
            String nameAttr = instances.attribute(i).name();
            vectorPercentageEachAttribute.add(new Pair<String, Double>(nameAttr,percentageThisAttr));
        }

        return vectorPercentageEachAttribute;
    }
}
