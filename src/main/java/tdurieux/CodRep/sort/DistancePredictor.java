package tdurieux.CodRep.sort;

import info.debatty.java.stringsimilarity.*;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;
import tdurieux.CodRep.context.LineContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistancePredictor implements SortLine {

    private int sizeSequence = 3;

    private double similarity(LineContext existing, LineContext toPredict) {
        return getSimilarityImpl().similarity(toPredict.getLineContent(), existing.getLineContent());
    }

    private Map<LineContext, Double> getSimilarity(LineContext context, List<LineContext> lines) {
        Map<LineContext, Double> result = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            LineContext lineContext = lines.get(i);
            double similarity = similarity(lineContext, context);
            result.put(lineContext, similarity);
        }
        return result;
    }

    public NormalizedStringSimilarity sorensen() {
        return new SorensenDice(sizeSequence);
    }
    public NormalizedStringSimilarity jaccard() {
        return new Jaccard();
    }
    public NormalizedStringSimilarity jaroWinkler() {
        return new JaroWinkler();
    }
    public NormalizedStringSimilarity normalizedLevenshtein() {
        return new NormalizedLevenshtein();
    }
    public NormalizedStringSimilarity cousin() {
        return new Cosine(sizeSequence);
    }

    public NormalizedStringSimilarity getSimilarityImpl() {
        //return normalizedLevenshtein();
        //return jaroWinkler();
        //return jaccard();
        //return cousin();
        return sorensen();
    }



    public List<LineContext> sort(LineContext context, List<LineContext> lines) {
        Map<LineContext, Double> result = getSimilarity(context, lines);
        List<LineContext> output = new ArrayList<>(result.keySet());

        output.sort((s1, s2) -> {
            if (result.get(s1) > result.get(s2)) {
                return -1;
            } else if (result.get(s1) < result.get(s2)) {
                return 1;
            }
            return 0;
        });


        return output.subList(0, Math.min(output.size(), 10));
    }
}
