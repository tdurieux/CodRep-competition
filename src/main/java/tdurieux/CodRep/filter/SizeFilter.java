package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public class SizeFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        int existingLength = existing.getLineContent().length();
        if (existingLength == 0) {
            return false;
        }
        return true;
    }
}
