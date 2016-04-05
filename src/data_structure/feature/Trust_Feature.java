package data_structure.feature;

import javafx.util.Pair;

/**
 * Created by steve on 29/01/2016.
 */
public class Trust_Feature {
    private Pair<Integer,Integer> trustWorthinessPairValues;
    private Pair<Integer,Integer> childSafetyPairValues;
    private Integer[] categoryEstimateValues; // negative, questionable, neutral, positive
    private Integer[] blacklistIncluded; // malware, phishing, scam, spam

    public Trust_Feature() {
        trustWorthinessPairValues = new Pair<Integer,Integer>(0,0);
        childSafetyPairValues = new Pair<Integer,Integer>(0,0);
        categoryEstimateValues = new Integer[4];
        for (int i=0;i<4;i++) {
            categoryEstimateValues[i] = 0;
        }
        blacklistIncluded = new Integer[4];
        for (int i=0;i<4;i++) {
            blacklistIncluded[i] = 0;
        }
    }

    public Integer[] getBlacklistIncluded() {
        return blacklistIncluded;
    }

    public void setBlacklistIncluded(Integer[] blacklistIncluded) {
        this.blacklistIncluded = blacklistIncluded;
    }

    public Pair<Integer, Integer> getTrustWorthinessPairValues() {
        return trustWorthinessPairValues;
    }

    public Pair<Integer, Integer> getChildSafetyPairValues() {
        return childSafetyPairValues;
    }

    public Integer[] getCategoryEstimateValues() {
        return categoryEstimateValues;
    }

    public void setTrustWorthinessPairValues(Pair<Integer, Integer> trustWorthinessPairValues) {
        this.trustWorthinessPairValues = trustWorthinessPairValues;
    }

    public void setChildSafetyPairValues(Pair<Integer, Integer> childSafetyPairValues) {
        this.childSafetyPairValues = childSafetyPairValues;
    }

    public void setCategoryEstimateValues(Integer[] categoryEstimateValues) {
        this.categoryEstimateValues = categoryEstimateValues;
    }
}
