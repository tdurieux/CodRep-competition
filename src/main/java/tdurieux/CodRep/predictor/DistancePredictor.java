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
    private final Map<String, List<Integer>> lines;
    protected LineFilter lineFilter;

    public DistancePredictor(String fileContent, String line) {
        this(fileContent, line, 2);
    }

    public DistancePredictor(String fileContent, String line, int sizeSequence) {
        this.line = line;
        this.fileContent = fileContent;
        this.sizeSequence = Math.min(line.length(), sizeSequence);
        this.lineFilter = new DefaultLineFilter();
        lines = SpoonUtil.splitLineToMap(fileContent);
    }


    private Map<List<Integer>, Double> similarity(NormalizedStringSimilarity similarityImpl) {
        Map<List<Integer>, Double> result = new HashMap<>();

        String trimmedLine = line.trim();
        String lowerLine = trimmedLine;
        for (String s : lines.keySet()) {
            if (s.isEmpty()) {
                continue;
            }
            if (s.equals(trimmedLine)) {
                continue;
            }
            //s = s.toLowerCase();
            double similarity = similarityImpl.similarity(lowerLine, s);
            if (lineFilter.filter(lines.get(s), s, lowerLine, similarity)) {
                result.put(lines.get(s), similarity);
            }
        }
        return result;
    }

    public Map<List<Integer>, Double> sorensen() {
        return similarity(new SorensenDice());
    }
    public Map<List<Integer>, Double> jaccard() {
        return similarity(new Jaccard());
    }
    public Map<List<Integer>, Double> jaroWinkler() {
        return similarity(new JaroWinkler());
    }
    public Map<List<Integer>, Double> normalizedLevenshtein() {
        return similarity(new NormalizedLevenshtein());
    }
    public Map<List<Integer>, Double> cousin() {
        return similarity(new Cosine(sizeSequence));
    }

    public Map<List<Integer>, Double> getSimilarity() {
        //return normalizedLevenshtein();
        //return jaroWinkler();
        //return jaccard();
        //return cousin();
        return sorensen();
    }



    @Override
    public List<List<Integer>> predict() {
        Map<List<Integer>, Double> result = getSimilarity();
        List<List<Integer>> output = new ArrayList<>(result.keySet());

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
