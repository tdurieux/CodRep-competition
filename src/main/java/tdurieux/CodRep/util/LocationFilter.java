package tdurieux.CodRep.util;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.io.IOException;
import java.nio.file.Files;

public abstract class LocationFilter<T extends CtElement> extends AbstractFilter<T> {

    public LocationFilter(Class<T> theClass, SourcePosition position) {
        super(theClass);
        this.position = position;
    }

    protected SourcePosition position() {
        return position;
    }

    public boolean onTheSameFile(SourcePosition otherPosition) {
        try {
            return Files.isSameFile(position().getFile().toPath(), otherPosition.getFile().toPath());
        } catch (IOException e) {
            return false;
        }
    }

    private SourcePosition position;
}
