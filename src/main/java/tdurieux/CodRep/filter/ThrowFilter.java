package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

public class ThrowFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (!toPredict.hasKeyword(Keywords.THROW)) {
            return true;
        }
        if (existing.hasKeyword(Keywords.BREAK) || existing.hasKeyword(Keywords.CONTINUE) || existing.hasKeyword(Keywords.RETURN) || existing.hasKeyword(Keywords.THROW)) {
            return true;
        }
        return existing.getLineContent().contains("LOG") || existing.getLineContent().contains("println");
    }
}
