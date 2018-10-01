package tdurieux.CodRep.context;

import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LineContext {
    List<String> comments = new ArrayList<>();
    List<String> numbers = new ArrayList<>();
    List<String> annotations = new ArrayList<>();
    Set<String> strings = new HashSet<>();
    Set<String> variables = new HashSet<>();
    Set<String> methods = new HashSet<>();
    Set<String> types = new HashSet<>();
    Set<Keywords> keywords = new HashSet<>();
    Map<Character, Integer> tokens = new HashMap<>();
    private String lineContent;
    private List<Integer> lineNumbers = new ArrayList<>();
    private CtElement element;

    public void addComment(String content) {
        comments.add(content);
    }

    public void addKeyword(Keywords keyword) {
        keywords.add(keyword);
    }

    public void increaseToken(char c) {
        if (!tokens.containsKey(c)) {
            tokens.put(c, 0);
        }
        tokens.put(c, tokens.get(c) + 1);
    }

    public int token(char c) {
        if (tokens.containsKey(c)) {
            return tokens.get(c);
        }
        return 0;
    }


    public void addVariable(String word) {
        variables.add(word);
    }

    public Set<String> getVariables() {
        return variables;
    }

    public void addType(String word) {
        types.add(word);
    }

    public void addMethod(String word) {
        methods.add(word);
    }

    public void setLineContent(String line) {
        this.lineContent = line;
    }

    public void addString(String string) {
        strings.add(string);
    }

    public void addAnnotation(String word) {
        annotations.add(word);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void addNumber(String word) {
        numbers.add(word);
    }

    public List<String> getNumbers() {
        return numbers;
    }

    @Override
    public String toString() {
        return lineContent;
    }

    public boolean hasKeyword(Keywords keyword) {
        return keywords.contains(keyword);
    }

    public String getLineContent() {
        return this.lineContent;
    }

    public void setLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers = lineNumbers;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public boolean hasToken(char s) {
        return this.tokens.containsKey(s);
    }

    public CtElement getElement() {
        return element;
    }

    public void setElement(CtElement element) {
        this.element = element;
    }
}
