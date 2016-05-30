package data_structure.instance_ML;

import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by steve on 30/05/2016.
 */
public class historySitesReputation {
    private String labelNormality;
    private Long responseTime;
    private Date measureDate;
    private Triplet<Double,Double,Double> compositionDangerousity;
    private String methodType;
    private String reputationType;

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getReputationType() {
        return reputationType;
    }

    public void setReputationType(String reputationType) {
        this.reputationType = reputationType;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Date getMeasureDate() {
        return measureDate;
    }

    public void setMeasureDate(Date measureDate) {
        this.measureDate = measureDate;
    }

    public Triplet<Double, Double, Double> getCompositionDangerousity() {
        return compositionDangerousity;
    }

    public void setCompositionDangerousity(Triplet<Double, Double, Double> compositionDangerousity) {
        this.compositionDangerousity = compositionDangerousity;
    }

    public String getLabelNormality() {
        return labelNormality;
    }

    public void setLabelNormality(String labelNormality) {
        this.labelNormality = labelNormality;
    }
}
