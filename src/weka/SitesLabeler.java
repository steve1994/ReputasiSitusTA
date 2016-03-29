package weka;

import data_structure.instance_ML.SiteRecordReputation;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 29/03/2016.
 */
public class SitesLabeler extends SitesMLProcessor{

    /**
     * Konstruktor struktur data record reputasi situs terdiri dari 7 kombinasi :
     * 1 (T,F,F) 2 (F,T,F) 3 (F,F,T) 4 (T,T,F) 5 (T,F,T) 6 (F,T,T) 7 (T,T,T)
     * @param typeReputation
     */
    public SitesLabeler(int typeReputation) {
        super(typeReputation);
    }

    /**
     * Setting instances weka config for this site reputation record (type)
     * @param classLabel
     */
    public void configARFFInstance(String[] classLabel) {
        List<Attribute> overallInstanceVector = super.getInstanceAttributes();
        // Add class label into this Instance
        FastVector siteLabel = new FastVector();
        for (String label : classLabel) {
            siteLabel.addElement(label);
        }
        Attribute siteLabelNominal = new Attribute("class",siteLabel); overallInstanceVector.add(siteLabelNominal);
        // Setting Attributes Vector Overall to Instance Record
        FastVector attributeInstanceRecord = new FastVector();
        for (Attribute attr : overallInstanceVector) {
            attributeInstanceRecord.addElement(attr);
        }
        siteReputationRecord = new Instances("Reputation Site Dataset",attributeInstanceRecord,0);
    }

    /**
     * Insert new reputation record into instances
     * Assumption : instances have been set properly
     * @param recordReputation
     * @param classLabel
     */
    public void fillDataIntoInstanceRecord(SiteRecordReputation recordReputation, String classLabel) {
        List<Object> instanceValues = super.getInstanceRecord(recordReputation);
        // Create new instance weka then insert it into siteReputationRecord
        double[] values = new double[instanceValues.size()];
        for (int i=0;i<instanceValues.size();i++) {
            values[i] = (Double) instanceValues.get(i);
        }
        Instance instance = new Instance(1.0,values);
        instance.attribute(siteReputationRecord.numAttributes()-1).indexOfValue(classLabel);
        siteReputationRecord.add(instance);
    }

    /**
     * Build classifier from sitereputationrecord based on classifier type (1 : NaiveBayes, 2 : ID3, 3 : J48)
     * @param instances
     * @param classifierType
     * @return
     */
    public Classifier buildLabelReputationModel(Instances instances, int classifierType) {
        siteReputationRecord.setClassIndex(siteReputationRecord.numAttributes()-1);
        Classifier classifier;
        switch (classifierType) {
            default:
            case 1:
                classifier = new NaiveBayes();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                classifier = new Id3();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                classifier = new J48();
                try {
                    classifier.buildClassifier(instances);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return classifier;
    }
}
