package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public class EqualityFilter implements Filter {
    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        return !existing.getLineContent().equals(toPredict.getLineContent());
    }
}
