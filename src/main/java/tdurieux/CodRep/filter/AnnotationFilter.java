package tdurieux.CodRep.filter;

import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableWrite;
import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

public class AnnotationFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (toPredict.getAnnotations().isEmpty()) {
            return true;
        }
        if (existing.getLineContent().startsWith("*")) {
            return false;
        }
        if (existing.getElement() instanceof CtVariableWrite) {
            return false;
        }
        return true;
    }
}
