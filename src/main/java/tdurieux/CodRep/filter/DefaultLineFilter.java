package tdurieux.CodRep.filter;

import java.util.List;

public class DefaultLineFilter implements LineFilter {
    @Override
    public boolean filter(List<Integer> line, String lineContent, String newLine, double similarity) {
        return !Double.isNaN(similarity);
    }
}
