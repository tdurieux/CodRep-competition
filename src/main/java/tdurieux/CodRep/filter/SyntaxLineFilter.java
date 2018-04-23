package tdurieux.CodRep.filter;

import org.apache.commons.lang3.StringUtils;

public class SyntaxLineFilter extends DefaultLineFilter {

    @Override
    public boolean filter(int line, String lineContent, String newLine, double similarity) {
        if (!super.filter(line, lineContent, newLine, similarity)) {
            return false;
        }
        String newLineWithoutComment = removeComment(removeString(newLine)).trim();
        String lineWithoutComment = removeComment(removeString(lineContent)).trim();
        if (newLineWithoutComment.endsWith(";") && !lineWithoutComment.endsWith(";") && !newLineWithoutComment.startsWith("}")) {
            return false;
        }
        if (newLineWithoutComment.startsWith(",") && !lineWithoutComment.startsWith(",")) {
            return false;
        }
        if (newLineWithoutComment.startsWith("super(") && !lineWithoutComment.startsWith("super(")) {
            return false;
        }
        if (newLineWithoutComment.startsWith("this(") && !lineWithoutComment.startsWith("this(")) {
            return false;
        }
        if (newLine.trim().startsWith("+") && !lineContent.trim().startsWith("+")) {
            return false;
        }
        if (removeComment(newLine).trim().endsWith("+") && !removeComment(newLine).trim().endsWith("+")) {
            return false;
        }
        if ((newLineWithoutComment.startsWith("class ") || newLineWithoutComment.contains(" class ")) && !lineWithoutComment.contains("class ")) {
            return false;
        }
        if ((newLineWithoutComment.startsWith("enum ") || newLineWithoutComment.contains(" enum ")) && !lineWithoutComment.contains("enum ")) {
            return false;
        }
        if ((newLineWithoutComment.startsWith("interface ") || newLineWithoutComment.contains(" interface ")) && !lineWithoutComment.contains("interface ")) {
            return false;
        }
        int nbBraceNewLine = StringUtils.countMatches(newLineWithoutComment, "(") - StringUtils.countMatches(newLineWithoutComment, ")");
        int nbBrace = StringUtils.countMatches(removeString(lineWithoutComment), "(") - StringUtils.countMatches(removeString(lineWithoutComment), ")");
        if (nbBrace != nbBraceNewLine) {
            return false;
        }
        int nbBracketNewLine = StringUtils.countMatches(removeString(removeString(newLineWithoutComment)), "{") - StringUtils.countMatches(newLineWithoutComment, "}");
        int nbBracket = StringUtils.countMatches(lineWithoutComment, "{") - StringUtils.countMatches(lineWithoutComment, "}");
        if (nbBracket != nbBracketNewLine) {
            return false;
        }
        return true;
    }


    private int indexOfString(String line, char stringStart, int start) {
        int indexStringStart = line.indexOf(stringStart, start);
        if (indexStringStart == -1) {
            return -1;
        }
        if (indexStringStart > 0 && line.charAt(indexStringStart - 1) == '\\' && line.charAt(indexStringStart - 2) != '\\') {
            return indexOfString(line, stringStart, indexStringStart + 1);
        }
        return indexStringStart;
    }
    private String removeString(String line) {
        line = removeBlockComment(line);
        char stringStart = '"';
        int indexStringStart = indexOfString(line, stringStart, 0);
        if (indexStringStart == -1) {
            stringStart = '\'';
            indexStringStart = indexOfString(line, stringStart, 0);
        }
        if (indexStringStart == -1) {
            return line;
        }
        int indexStringEnd = indexOfString(line, stringStart, indexStringStart + 1);
        if (indexStringEnd == -1) {
            return line.substring(indexStringStart);
        }
        String start = line.substring(0, indexStringStart);
        String end = line.substring(indexStringEnd + 1);

        return removeString(start + end);
    }

    private String removeComment(String line) {
        int indexCommentStart = line.indexOf("//");
        if (indexCommentStart != -1) {
            return line.substring(0, indexCommentStart).trim();
        }
        return line;
    }

    private String removeBlockComment(String line) {
        int indexStart = line.indexOf("/*");
        if (indexStart != -1) {
            int indexEnd = line.indexOf("*/", indexStart);
            if (indexEnd == -1) {
                line = line.substring(0, indexStart);
            } else {
                line = line.substring(0, indexStart) + line.substring(indexEnd + 2);
            }
            return removeBlockComment(line);
        }
        return line;
    }
}
