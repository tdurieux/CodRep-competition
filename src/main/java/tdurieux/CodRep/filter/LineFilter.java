package tdurieux.CodRep.filter;

public interface LineFilter {

    boolean filter(int line, String lineContent, String newLine, double similarity);
}
