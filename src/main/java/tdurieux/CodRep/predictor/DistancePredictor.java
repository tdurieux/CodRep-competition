package tdurieux.CodRep.predictor;

import info.debatty.java.stringsimilarity.*;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;
import tdurieux.CodRep.filter.DefaultLineFilter;
import tdurieux.CodRep.filter.LineFilter;
import tdurieux.CodRep.util.SpoonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistancePredictor implements LinePredictor {

    private final String line;
    private final String fileContent;
    private final int sizeSequence;
    private final String[] lines;
    protected LineFilter lineFilter;

    public DistancePredictor(String fileContent, String line) {
        this(fileContent, line, 2);
    }

    public DistancePredictor(String fileContent, String line, int sizeSequence) {
        this.line = line;
        this.fileContent = fileContent;
        this.sizeSequence = Math.min(line.length(), sizeSequence);
        this.lineFilter = new DefaultLineFilter();
        lines = SpoonUtil.splitLine(fileContent);
    }


    private Map<Integer, Double> similarity(NormalizedStringSimilarity similarityImpl) {
        Map<Integer, Double> result = new HashMap<>();
        
        for (int i = 0; i < lines.length; i++) {
            String s = lines[i].trim();
            if (s.equals(line.trim())) {
                continue;
            }
            String line = this.line.trim().toLowerCase();
            s = s.toLowerCase();
            double similarity = similarityImpl.similarity(line, s);
            if (lineFilter.filter(i, s, line, similarity)) {
                result.put(i, similarity);
            }
        }
        return result;
    }

    public Map<Integer, Double> sorensen() {
        return similarity(new SorensenDice());
    }
    public Map<Integer, Double> jaccard() {
        return similarity(new Jaccard());
    }
    public Map<Integer, Double> jaroWinkler() {
        return similarity(new JaroWinkler());
    }
    public Map<Integer, Double> normalizedLevenshtein() {
        return similarity(new NormalizedLevenshtein());
    }
    public Map<Integer, Double> cousin() {
        return similarity(new Cosine(sizeSequence));
    }

    public Map<Integer, Double> getSimilarity() {
        //return normalizedLevenshtein();
        //return jaroWinkler();
        //return jaccard();
        //return cousin();
        return sorensen();
    }



    @Override
    public List<Integer> predict() {
        Map<Integer, Double> result = getSimilarity();
        List<Integer> output = new ArrayList<>(result.keySet());

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

    @Override
    public String getLine() {
        return line;
    }

    @Override
    public String getFileContent() {
        return fileContent;
    }
}
