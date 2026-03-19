package dev.scx.reflect;

import java.lang.reflect.RecordComponent;

import static dev.scx.reflect.TypeFactory.typeOfAny;

/// RecordComponentInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class RecordComponentInfoImpl implements RecordComponentInfo {

    private final RecordComponent rawRecordComponent;
    private final ClassInfo declaringClass;

    private final TypeInfo recordComponentType;

    private final int hashCode;

    RecordComponentInfoImpl(RecordComponent recordComponent, ClassInfo declaringClass) {
        this.rawRecordComponent = recordComponent;
        this.declaringClass = declaringClass;

        this.recordComponentType = typeOfAny(this.rawRecordComponent.getGenericType(), new TypeResolutionContext(this.declaringClass.allBindings()));
        // 缓存 hashCode
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
        return rawRecordComponent.getName();
    }

    @Override
    public TypeInfo recordComponentType() {
        return recordComponentType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
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
        return recordComponentType.toString() + " " + rawRecordComponent.getName();
    }

}
