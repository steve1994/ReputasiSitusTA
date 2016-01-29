package data_structure;

/**
 * Created by steve on 29/01/2016.
 */
public class WOTModel {
    private int TrustWorthinessEstimate;
    private int TrustWorthinessConfidence;
    private int ChildSafetyEstimate;
    private int ChildSafetyConfidence;
    private int NegativeCategoryConfidence;
    private int QuestionableCategoryConfidence;
    private int NeutralCategoryConfidence;
    private int PositiveCategoryConfidence;

    public WOTModel() {
        TrustWorthinessEstimate = 0;
        TrustWorthinessConfidence = 0;
        ChildSafetyEstimate = 0;
        ChildSafetyConfidence = 0;
        NegativeCategoryConfidence = 0;
        QuestionableCategoryConfidence = 0;
        NeutralCategoryConfidence = 0;
        PositiveCategoryConfidence = 0;
    }

    public int getTrustWorthinessEstimate() {
        return TrustWorthinessEstimate;
    }

    public void setTrustWorthinessEstimate(int trustWorthinessEstimate) {
        TrustWorthinessEstimate = trustWorthinessEstimate;
    }

    public int getTrustWorthinessConfidence() {
        return TrustWorthinessConfidence;
    }

    public void setTrustWorthinessConfidence(int trustWorthinessConfidence) {
        TrustWorthinessConfidence = trustWorthinessConfidence;
    }

    public int getChildSafetyEstimate() {
        return ChildSafetyEstimate;
    }

    public void setChildSafetyEstimate(int childSafetyEstimate) {
        ChildSafetyEstimate = childSafetyEstimate;
    }

    public int getChildSafetyConfidence() {
        return ChildSafetyConfidence;
    }

    public void setChildSafetyConfidence(int childSafetyConfidence) {
        ChildSafetyConfidence = childSafetyConfidence;
    }

    public int getNegativeCategoryConfidence() {
        return NegativeCategoryConfidence;
    }

    public void setNegativeCategoryConfidence(int negativeCategoryConfidence) {
        NegativeCategoryConfidence = negativeCategoryConfidence;
    }

    public int getQuestionableCategoryConfidence() {
        return QuestionableCategoryConfidence;
    }

    public void setQuestionableCategoryConfidence(int questionableCategoryConfidence) {
        QuestionableCategoryConfidence = questionableCategoryConfidence;
    }

    public int getNeutralCategoryConfidence() {
        return NeutralCategoryConfidence;
    }

    public void setNeutralCategoryConfidence(int neutralCategoryConfidence) {
        NeutralCategoryConfidence = neutralCategoryConfidence;
    }

    public int getPositiveCategoryConfidence() {
        return PositiveCategoryConfidence;
    }

    public void setPositiveCategoryConfidence(int positiveCategoryConfidence) {
        PositiveCategoryConfidence = positiveCategoryConfidence;
    }
}
