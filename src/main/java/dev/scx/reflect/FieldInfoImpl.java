package dev.scx.reflect;

import java.lang.reflect.Field;

import static dev.scx.reflect.ReflectSupport._findAccessModifier;
import static dev.scx.reflect.TypeFactory.typeOfAny;
import static java.lang.reflect.AccessFlag.FINAL;
import static java.lang.reflect.AccessFlag.STATIC;

/// FieldInfoImpl
///
/// @author scx567888
/// @version 0.0.1
final class FieldInfoImpl implements FieldInfo {

    private final Field rawField;
    private final ClassInfo declaringClass;
    private final String name;
    private final AccessModifier accessModifier;
    private final boolean isFinal;
    private final boolean isStatic;
    private final TypeInfo fieldType;
    private final int hashCode;

    FieldInfoImpl(Field field, ClassInfo declaringClass) {
        this.rawField = field;
        this.declaringClass = declaringClass;
        this.name = this.rawField.getName();
        var accessFlags = this.rawField.accessFlags();
        this.accessModifier = _findAccessModifier(accessFlags);
        this.isFinal = accessFlags.contains(FINAL);
        this.isStatic = accessFlags.contains(STATIC);
        this.fieldType = typeOfAny(this.rawField.getGenericType(), new TypeResolutionContext(this.declaringClass.bindings()));
        this.hashCode = this._hashCode();
    }

    @Override
    public Field rawField() {
        return rawField;
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
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public TypeInfo fieldType() {
        return fieldType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof FieldInfoImpl o) {
            return rawField.equals(o.rawField);
        }
        return false;
    }

    private int _hashCode() {
        int result = FieldInfoImpl.class.hashCode();
        result = 31 * result + rawField.hashCode();
        return result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();

        // 修饰符
        sb.append(accessModifier.name().toLowerCase());
        if (isStatic) {
            sb.append(" static");
        }
        if (isFinal) {
            sb.append(" final");
        }

        // 类型
        sb.append(" ");
        sb.append(fieldType.toString());

        // 名称
        sb.append(" ");
        sb.append(name);

        return sb.toString();
    }

}
