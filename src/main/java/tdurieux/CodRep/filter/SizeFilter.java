package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public class SizeFilter implements Filter {
    private final static double threshold = 10000000;

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        int existingLength = existing.getLineContent().length();
        int toPredictLength = toPredict.getLineContent().length();
        if (existingLength == 0) {
            return false;
        }
        return Math.max(existingLength, toPredictLength) * 1.0 / Math.min(existingLength, toPredictLength) <= threshold;
    }
}
