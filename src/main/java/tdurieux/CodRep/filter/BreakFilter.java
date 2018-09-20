package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

public class BreakFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (!toPredict.hasKeyword(Keywords.BREAK) && !toPredict.hasKeyword(Keywords.CONTINUE)) {
            return true;
        }
        if (existing.hasKeyword(Keywords.BREAK) || existing.hasKeyword(Keywords.CONTINUE) || existing.hasKeyword(Keywords.RETURN) || existing.hasKeyword(Keywords.THROW)) {
            return true;
        }
        return false;
    }
}
