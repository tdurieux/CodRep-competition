package tdurieux.CodRep.filter;

import spoon.reflect.code.CtComment;
import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

public class CommentFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (toPredict.getLineContent().startsWith("/*") && toPredict.getLineContent().endsWith("*/")) {
            if (existing.getLineContent().startsWith("/*") || (existing.getElement() instanceof CtComment && !existing.getLineContent().startsWith("//"))) {
                return  existing.getLineContent().endsWith("*/");
            }
        }
        return true;
    }
}
