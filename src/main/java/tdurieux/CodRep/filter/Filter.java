package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.LineContext;

public interface Filter {
    boolean filter(LineContext existing, LineContext toPredict);
}
