package tdurieux.CodRep.filter;

import spoon.reflect.code.CtLocalVariable;
import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

public class CatchFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (!toPredict.hasKeyword(Keywords.CATCH)) {
            return true;
        }
        if (!existing.hasKeyword(Keywords.CATCH)) {
            return false;
        }
        String simpleName = ((CtLocalVariable) toPredict.getElement()).getSimpleName();
        String simpleNameExisting = ((CtLocalVariable) existing.getElement()).getSimpleName();
        return simpleNameExisting.equals(simpleName);
    }
}
