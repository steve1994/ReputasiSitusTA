package sample;

import data_structure.instance_ML.SiteRecordReputation;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by steve on 08/04/2016.
 */
public class StaticVars {
    public static List<String> listSitesTraining;
    public static int numSitesTraining;
    public static int reputationType;
    public static int methodType;
    public static String currentDomainName;
    public static String currentLabel;
    public static String currentResponseTime;
    public static Date currentDate;
    public static Triplet<Double,Double,Double> currentComposition;
}
