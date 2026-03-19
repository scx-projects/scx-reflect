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

    private final AccessModifier accessModifier;

    private final boolean isStatic;
    private final boolean isFinal;

    private final TypeInfo fieldType;

    private final int hashCode;

    FieldInfoImpl(Field field, ClassInfo declaringClass) {
        this.rawField = field;
        this.declaringClass = declaringClass;

        var accessFlags = this.rawField.accessFlags();

        this.accessModifier = _findAccessModifier(accessFlags);
        this.isStatic = accessFlags.contains(STATIC);
        this.isFinal = accessFlags.contains(FINAL);
        this.fieldType = typeOfAny(this.rawField.getGenericType(), new TypeResolutionContext(this.declaringClass.allBindings()));
        // 缓存 hashCode
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
    public AccessModifier accessModifier() {
        return accessModifier;
    }

    @Override
    public String name() {
        return rawField.getName();
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public TypeInfo fieldType() {
        return fieldType;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
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
        sb.append(accessModifier.text());
        if (isStatic) {
            sb.append(" static");
        }
        if (isFinal) {
            sb.append(" final");
        }

        // 类型
        sb.append(" ").append(fieldType.toString());

        // 名称
        sb.append(" ").append(rawField.getName());

        return sb.toString();
    }

}
