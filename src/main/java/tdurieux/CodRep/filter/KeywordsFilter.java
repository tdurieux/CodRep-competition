package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

import java.util.Arrays;
import java.util.List;

public class KeywordsFilter implements Filter {
    private List<Keywords> hasToHave = Arrays.asList(
            Keywords.IMPORT,
            Keywords.PACKAGE,
            Keywords.CLASS,
            Keywords.INTERFACE,
            Keywords.SWITCH,
            Keywords.CATCH);

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        for (int i = 0; i < hasToHave.size(); i++) {
            Keywords keywords =  hasToHave.get(i);
            if (toPredict.hasKeyword(keywords) != existing.hasKeyword(keywords) ||
                    !toPredict.hasKeyword(keywords) != !existing.hasKeyword(keywords)) {
                return false;
            }
        }
        return true;
    }
}
