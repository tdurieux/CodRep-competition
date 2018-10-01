package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public class ConstructorCallFilter implements Filter {
    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (toPredict.getLineContent().contains("super(") || toPredict.getLineContent().contains("this(")) {
            return existing.getLineContent().contains("super(") || existing.getLineContent().contains("this(") || existing.getLineContent().contains("empty constructor");
        }
        return true;
    }
}
