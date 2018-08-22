package tdurieux.CodRep.filter;

import java.util.List;

public interface LineFilter {

    boolean filter(List<Integer> line, String lineContent, String newLine, double similarity);
}
