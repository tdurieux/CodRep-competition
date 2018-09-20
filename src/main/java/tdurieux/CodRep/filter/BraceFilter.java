package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public class BraceFilter implements Filter {
    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        return !(toPredict.token('(') - toPredict.token(')') != existing.token('(') - existing.token(')') ||
                toPredict.token('{') - toPredict.token('}') != existing.token('{') - existing.token('}') ||
                toPredict.token('[') - toPredict.token(']') != existing.token('[') - existing.token(']'));
    }
}
