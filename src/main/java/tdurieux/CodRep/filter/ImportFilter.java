package tdurieux.CodRep.filter;

import tdurieux.CodRep.context.Keywords;
import tdurieux.CodRep.context.LineContext;

import java.util.Arrays;
import java.util.List;

public class ImportFilter implements Filter {

    @Override
    public boolean filter(LineContext existing, LineContext toPredict) {
        if (!existing.hasKeyword(Keywords.IMPORT)) {
            return true;
        }
        return checkImportLine(toPredict, existing);
    }

    private boolean checkImportLine(LineContext newLine, LineContext line) {
        String[] newLineQualifiedName = newLine.getLineContent().substring(7, newLine.getLineContent().length() - 1).split("\\.");
        String[] lineQualifiedName = line.getLineContent().substring(7, line.getLineContent().length() - 1).split("\\.");
        int lineLast = lineQualifiedName.length - 1;
        String className = lineQualifiedName[lineLast];
        int lastNew = newLineQualifiedName.length - 1;
        String newLineClassName = newLineQualifiedName[lastNew];
        if (className.equals("*") || newLineClassName.equals("*")) {
            className = lineQualifiedName[lineLast - 1];
            newLineClassName = newLineQualifiedName[lastNew - 1];
            return newLineClassName.contains(className) || className.contains(newLineClassName);
        }
        return newLineClassName.equals(className);
    }
}
