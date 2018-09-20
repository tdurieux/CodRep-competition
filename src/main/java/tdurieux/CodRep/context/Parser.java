package tdurieux.CodRep.context;

public class Parser {
    public static LineContext parse(String line) {
        LineContext lineContext = new LineContext();
        lineContext.setLineContent(line);

        StringBuilder currentWords = new StringBuilder();

        boolean inString = false;
        boolean inComment = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inString || inComment) {
                if (c == '/' && line.charAt(i - 1) == '*') {
                    inComment = false;
                    lineContext.addComment(currentWords.toString());
                    currentWords = new StringBuilder();
                } else if (c == '"' && line.charAt(i - 1) != '\\') {
                    inString = false;
                    lineContext.addString(currentWords.toString());
                    currentWords = new StringBuilder();
                } else {
                    currentWords.append(c);
                }
                continue;
            }
            switch (c) {
                case '/':
                    if (line.length() > i + 1) {
                        if (line.charAt(i + 1) == '/') {
                            lineContext.addComment(line.substring(i));
                            return lineContext;
                        } else if (line.charAt(i + 1) == '*') {
                            inComment = true;
                        }
                    }
                    lineContext.increaseToken(c);
                    break;
                case '"':
                    if (i == 0 || line.charAt(i - 1) != '\\') {
                        inString = true;
                    }
                    break;
                case '.':
                case ',':
                case ';':
                case ':':
                case ' ':
                case '{':
                case '[':
                case '(':
                case '}':
                case ']':
                case ')':

                case '=':
                case '<':
                case '>':
                case '&':
                case '|':
                case '+':
                case '-':
                case '*':
                case '!':
                case '?':

                    String word = currentWords.toString();
                    if (!word.isEmpty()) {
                        try {
                            if ((c == ' ' || c == ';' ) && word.toLowerCase().equals(word)) {
                                Keywords keyword = Keywords.valueOf(word.toUpperCase());
                                lineContext.addKeyword(keyword);
                            } else if (detectFloat(lineContext, currentWords, c, word)) break;
                        } catch (IllegalArgumentException e) {
                            if (detectFloat(lineContext, currentWords, c, word)) break;
                        }
                        currentWords = new StringBuilder();
                    }

                    lineContext.increaseToken(c);
                    break;
                default:
                    currentWords.append(c);
            }

        }
        if (inComment) {
            lineContext.addComment(currentWords.toString());
        }

        return lineContext;
    }

    private static boolean detectFloat(LineContext lineContext, StringBuilder currentWords, char c, String word) {
        try {
            if (c == '.') {
                Integer.parseInt(word);
                currentWords.append(c);
                return true;
            } else {
                endOfWord(lineContext, c, word);
            }
        } catch (NumberFormatException ex) {
            endOfWord(lineContext, c, word);
        }
        return false;
    }

    private static void endOfWord(LineContext lineContext, char c, String word) {
        if (Character.isLowerCase(word.charAt(0))) {
            if ('(' == c) {
                lineContext.addMethod(word);
            } else {
                lineContext.addVariable(word);
            }
        } else if (Character.isUpperCase(word.charAt(0))) {
            lineContext.addType(word);
        } else if (word.charAt(0) == '@') {
            lineContext.addAnnotation(word);
        } else if (word.charAt(0) == '_') {
            lineContext.addVariable(word);
        } else {
            lineContext.addNumber(word);
        }
    }
}
