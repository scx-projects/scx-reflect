package dev.scx.reflect;

import java.lang.reflect.RecordComponent;

import static dev.scx.reflect.TypeFactory.typeOfAny;

///  RecordComponentInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class RecordComponentInfoImpl implements RecordComponentInfo {

    private final RecordComponent rawRecordComponent;
    private final ClassInfo declaringClass;
    private final String name;
    private final TypeInfo recordComponentType;
    private final int hashCode;

    RecordComponentInfoImpl(RecordComponent recordComponent, ClassInfo declaringClass) {
        this.rawRecordComponent = recordComponent;
        this.declaringClass = declaringClass;
        this.name = this.rawRecordComponent.getName();
        this.recordComponentType = typeOfAny(this.rawRecordComponent.getGenericType(), new TypeResolutionContext(this.declaringClass.bindings()));
        this.hashCode = this._hashCode();
    }

    @Override
    public RecordComponent rawRecordComponent() {
        return rawRecordComponent;
    }

    @Override
    public ClassInfo declaringClass() {
        return declaringClass;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TypeInfo recordComponentType() {
        return recordComponentType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof RecordComponentInfoImpl o) {
            return rawRecordComponent.equals(o.rawRecordComponent);
        }
        return false;
    }

    private int _hashCode() {
        int result = RecordComponentInfoImpl.class.hashCode();
        result = 31 * result + rawRecordComponent.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return recordComponentType.toString() + " " + name;
    }

}
