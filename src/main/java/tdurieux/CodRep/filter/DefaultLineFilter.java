package tdurieux.CodRep.filter;

public class DefaultLineFilter implements LineFilter {
    @Override
    public boolean filter(int line, String lineContent, String newLine, double similarity) {
        return !Double.isNaN(similarity);
    }
}
