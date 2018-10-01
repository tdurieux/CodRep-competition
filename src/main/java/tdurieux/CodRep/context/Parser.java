package tdurieux.CodRep.context;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {

    private final Factory factory;

    Stack<CtElement> elements = new Stack<>();

    public Parser() {
        this.factory = new Launcher().createFactory();
        elements.push(factory.Package().getRootPackage());
    }

    enum ElementType {
        VARIABLE,
        COMMENT,
        TYPE,
        NUMBER,
        ANNOTATION,
        STRING,
        KEYWORD,
        TOKEN,
        METHOD
    }

    class LineElement {
        protected ElementType type;
        protected String value;
        protected int sourceBegin;
        protected int sourceEnd;

        public LineElement(ElementType type, String value, int sourceEnd) {
            this.type = type;
            this.value = value;
            this.sourceBegin = sourceEnd - value.length();
            this.sourceEnd = sourceEnd;
        }

        @Override
        public String toString() {
            return type + " " + value;
        }
    }

    class LineElementStack extends Stack<LineElement> {

        public List<LineElement> get(ElementType type, String value) {
            List<LineElement> output = new ArrayList<>();
            for (LineElement next : this) {
                if (next.type == type && value.equals(next.value)) {
                    output.add(next);
                }
            }
            return output;
        }

        public List<LineElement> get(ElementType type) {
            List<LineElement> output = new ArrayList<>();
            for (LineElement next : this) {
                if (next.type == type) {
                    output.add(next);
                }
            }
            return output;
        }

        public LineElementStack match(Object...types) {
            LineElementStack output = new LineElementStack();
            int currentIndex = 0;
            int indexStartMatch = -1;


            ArrayList<LineElement> elements = new ArrayList<>(this);
            for (int i = 0; i < elements.size(); i++) {
                LineElement next = elements.get(i);
                if ((types[currentIndex] instanceof String && next.value.equals(types[currentIndex])) || (next.type == types[currentIndex])) {
                    if (indexStartMatch == -1) {
                        indexStartMatch = i;
                    }
                    output.push(next);
                    currentIndex ++;
                    if (currentIndex == types.length) {
                        return output;
                    }
                } else {
                    currentIndex = 0;
                    output = new LineElementStack();
                    if (indexStartMatch != -1) {
                        i = indexStartMatch + 1;
                        indexStartMatch = -1;
                    }
                }
            }
            return null;
        }

        @Override
        public synchronized String toString() {
            StringBuilder sb = new StringBuilder();
            for (LineElement next : this) {
                sb.append(next.type + ", ");
            }
            return sb.toString();
        }
    }

    private boolean inComment = false;

    public LineContext parse(String line) {
        LineContext lineContext = new LineContext();
        lineContext.setLineContent(line);

        StringBuilder currentWords = new StringBuilder();

        LineElementStack stack = new LineElementStack();

        boolean inString = false;

        loop: for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inString || inComment) {
                if (inComment && c == '/' && i > 0 && line.charAt(i - 1) == '*') {
                    inComment = false;
                    lineContext.addComment(currentWords.toString());
                    stack.push(new LineElement(ElementType.COMMENT, currentWords.toString(), i));
                    currentWords = new StringBuilder();
                } else if (inString && c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                    inString = false;
                    lineContext.addString(currentWords.toString());
                    stack.push(new LineElement(ElementType.STRING, currentWords.toString(), i));
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
                            lineContext.addComment(line.substring(i + 2));
                            stack.push(new LineElement(ElementType.COMMENT, line.substring(i + 2), i));
                            break loop;
                        } else if (line.charAt(i + 1) == '*') {
                            inComment = true;
                            i++;
                        }
                    }
                    lineContext.increaseToken(c);
                    break;
                case '"':
                    if (i == 0 || line.charAt(i - 1) != '\\') {
                        inString = true;
                    }
                    break;
                case '*':
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
                case '!':
                case '?':

                    if (c == '{') {
                        factory.createBlock();
                    } else if (c == '}') {
                        
                    }
                    String word = currentWords.toString();
                    if (!word.isEmpty()) {
                        try {
                            if ((c == ' ' || c == ';' ) && word.toLowerCase().equals(word)) {
                                Keywords keyword = Keywords.valueOf(word.toUpperCase());

                                if (Keywords.primitives().contains(keyword)) {
                                    lineContext.addType(keyword.toString());
                                    stack.push(new LineElement(ElementType.TYPE, keyword.toString(), i));
                                } else {
                                    lineContext.addKeyword(keyword);
                                    stack.push(new LineElement(ElementType.KEYWORD, keyword.toString(), i));
                                }
                            } else if (detectFloat(lineContext, stack, currentWords, c, word, i)) break;
                        } catch (IllegalArgumentException e) {
                            if (detectFloat(lineContext, stack, currentWords, c, word, i)) break;
                        }
                        currentWords = new StringBuilder();
                    }

                    if (c != ' ') {
                        stack.push(new LineElement(ElementType.TOKEN, c + "", i));
                    }
                    lineContext.increaseToken(c);
                    break;
                default:
                    currentWords.append(c);
            }

        }
        if (inComment) {
            lineContext.addComment(currentWords.toString());
            stack.push(new LineElement(ElementType.COMMENT, currentWords.toString(), line.length()));
        } else {
            if (currentWords.length() != 0) {
                endOfWord(lineContext, stack, ' ', currentWords.toString(), line.length());
            }
        }

        CtElement element = createSpoonElement(lineContext, stack);
        lineContext.setElement(element);
        if (element != null) {
            if (inComment && element instanceof CtComment && this.elements.peek() instanceof CtComment) {
                CtComment comment = (CtComment) this.elements.peek();
                comment.setContent(comment.getContent() + "\n" + ((CtComment) element).getContent());
            } else if (element instanceof CtTypeMember  && this.elements.peek() instanceof CtType) {
                ((CtType) this.elements.peek()).addTypeMember((CtTypeMember) element);
                if (element instanceof CtBodyHolder || element instanceof CtComment){
                    this.elements.push(element);
                }
            } else if (element instanceof CtBodyHolder || element instanceof CtComment){
                this.elements.push(element);
            } else if (element instanceof CtStatement && this.elements.peek() instanceof CtBodyHolder) {
                ((CtStatementList)((CtBodyHolder)this.elements.peek()).getBody()).addStatement((CtStatement) element);
                if (element instanceof CtBodyHolder || element instanceof CtIf){
                    this.elements.push(element);
                }
            } else if (element instanceof CtType) {
                if (this.elements.peek() instanceof CtPackage) {
                    ((CtPackage) this.elements.peek()).addType((CtType<?>) element);
                } else if (this.elements.peek() instanceof CtType) {
                    ((CtType) this.elements.peek()).addTypeMember((CtTypeMember) element);
                }
                this.elements.push(element);
            } else if (element instanceof CtPackage  && this.elements.peek() instanceof CtPackage) {
                ((CtPackage) this.elements.peek()).addPackage((CtPackage) element);
                this.elements.push(element);
            }
        }
        return lineContext;
    }

    private CtElement createSpoonElement(LineContext lineContext, LineElementStack stack) {
        if (stack.empty()) {
            return null;
        }
        if (lineContext.hasKeyword(Keywords.PACKAGE)) {
            String qualifiedName = stack.get(ElementType.VARIABLE).stream().map(v -> v.value + ".").reduce((t, b) -> {
                return t + b;
            }).get();
            return factory.Package().getOrCreate(qualifiedName.substring(0, qualifiedName.length() - 1));
        }
        if (lineContext.hasKeyword(Keywords.CLASS)) {
            List<LineElement> type = stack.get(ElementType.TYPE);
            String className = "";
            if (type.isEmpty()) {
                className = stack.get(ElementType.VARIABLE).get(0).value;
            } else {
                className = type.get(0).value;
            }
            return factory.Class().create(className);
        }
        if (lineContext.hasKeyword(Keywords.IMPORT)) {
            CtTypeReference<Object> typeReference = factory.createTypeReference();
            List<LineElement> importType = stack.get(ElementType.TYPE);
            if (!importType.isEmpty()) {
                typeReference.setSimpleName(importType.get(0).value);
            } else {
                if (lineContext.hasToken('*')) {
                    typeReference.setSimpleName("*");
                } else {
                    List<LineElement> variables = stack.get(ElementType.VARIABLE);
                    typeReference.setSimpleName(variables.get(variables.size() - 1).value);
                }
            }
            CtImport anImport = factory.createImport(typeReference);
            return anImport;
        }
        if (lineContext.hasKeyword(Keywords.IF)) {
            CtIf anIf = factory.createIf();
            anIf.setThenStatement(factory.createBlock());
            return anIf;
        }
        if (stack.get(0).type == ElementType.KEYWORD && "RETURN".equals(stack.get(0).value)) {
            return factory.createReturn();
        }

        if (stack.size() == 1 && !stack.get(ElementType.COMMENT).isEmpty()) {
            return factory.createComment(stack.peek().value, CtComment.CommentType.BLOCK);
        }
        LineElementStack match = stack.match(ElementType.TYPE, ElementType.METHOD, "(");
        if (match != null) {
            CtMethod<Object> method = factory.createMethod();
            method.setSimpleName(match.get(ElementType.METHOD).get(0).value);
            CtTypeReference typeReference = factory.createTypeReference();
            typeReference.setSimpleName(match.get(ElementType.TYPE).get(0).value);
            method.setType(typeReference);
            method.setBody(factory.createBlock());
            return method;
        }

        match = stack.match(ElementType.TYPE, ElementType.VARIABLE);
        if (match != null) {
            CtLocalVariable variable = factory.createLocalVariable();
            CtTypeReference<?> typeReference = factory.createTypeReference();
            typeReference.setSimpleName(match.get(ElementType.TYPE).get(0).value);
            variable.setType(typeReference);
            variable.setSimpleName(match.get(ElementType.VARIABLE).get(0).value);
            return variable;
        }

        match = stack.match(ElementType.VARIABLE, "=");
        if (match != null) {
            LineElementStack declarationVariable = stack.match(ElementType.TYPE, ElementType.VARIABLE, "=");
            if (declarationVariable != null) {
                CtLocalVariable variable = factory.createLocalVariable();
                CtTypeReference<?> typeReference = factory.createTypeReference();
                typeReference.setSimpleName(match.get(ElementType.TYPE).get(0).value);
                variable.setType(typeReference);
                return variable;
            } else {
                CtAssignment assignment = factory.createAssignment();
                CtVariableRead variableRead = factory.createVariableRead();
                CtLocalVariableReference localVariableReference = factory.createLocalVariableReference();
                localVariableReference.setSimpleName(match.get(ElementType.VARIABLE).get(0).value);
                variableRead.setVariable(localVariableReference);
                assignment.setAssigned(variableRead);
                return assignment;
            }
        }

        match = stack.match(ElementType.METHOD, "(");
        if (match != null) {
            CtInvocation<Object> invocation = factory.createInvocation();
            CtExecutableReference<Object> executableReference = factory.createExecutableReference();
            executableReference.setSimpleName(match.get(ElementType.METHOD).get(0).value);
            invocation.setExecutable(executableReference);
            return invocation;
        }

        return null;
    }

    private boolean detectFloat(LineContext lineContext, Stack<LineElement> stack, StringBuilder currentWords, char c, String word, int sourceEnd) {
        try {
            if (c == '.') {
                Integer.parseInt(word);
                currentWords.append(c);
                return true;
            } else {
                endOfWord(lineContext, stack, c, word, sourceEnd);
            }
        } catch (NumberFormatException ex) {
            endOfWord(lineContext, stack, c, word, sourceEnd);
        }
        return false;
    }

    private void endOfWord(LineContext lineContext, Stack<LineElement> stack, char c, String word, int sourceEnd) {
        if (Character.isLowerCase(word.charAt(0)) || (word.toUpperCase().equals(word) && !word.toLowerCase().equals(word)) ) {
            if ('(' == c) {
                lineContext.addMethod(word);
                stack.push(new LineElement(ElementType.METHOD, word, sourceEnd));
            } else {
                lineContext.addVariable(word);
                stack.push(new LineElement(ElementType.VARIABLE, word, sourceEnd));
            }
        } else if (Character.isUpperCase(word.charAt(0))) {
            lineContext.addType(word);
            stack.push(new LineElement(ElementType.TYPE, word, sourceEnd));
        } else if (word.charAt(0) == '@') {
            lineContext.addAnnotation(word);
            stack.push(new LineElement(ElementType.ANNOTATION, word, sourceEnd));
        } else if (word.charAt(0) == '_') {
            if (word.length() > 1 && Character.isUpperCase(word.charAt(1))) {
                lineContext.addType(word);
                stack.push(new LineElement(ElementType.TYPE, word, sourceEnd));
            } else {
                lineContext.addVariable(word);
                stack.push(new LineElement(ElementType.VARIABLE, word, sourceEnd));
            }
        } else {
            lineContext.addNumber(word);
            stack.push(new LineElement(ElementType.NUMBER, word, sourceEnd));
        }
    }
}
