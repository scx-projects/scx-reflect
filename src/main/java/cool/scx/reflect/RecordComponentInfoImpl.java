package cool.scx.reflect;

import java.lang.reflect.RecordComponent;

import static cool.scx.reflect.TypeFactory.typeOfFromAny;

///  RecordComponentInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class RecordComponentInfoImpl implements RecordComponentInfo {

    private final RecordComponent rawRecordComponent;
    private final ClassInfo declaringClass;
    private final String name;
    private final TypeInfo recordComponentType;

    RecordComponentInfoImpl(RecordComponent recordComponent, ClassInfo declaringClass) {
        this.rawRecordComponent = recordComponent;
        this.declaringClass = declaringClass;
        this.name = this.rawRecordComponent.getName();
        this.recordComponentType = typeOfFromAny(this.rawRecordComponent.getGenericType(), new TypeResolutionContext(this.declaringClass.bindings()));
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
    public String toString() {
        return recordComponentType.toString() + " " + name;
    }

}
