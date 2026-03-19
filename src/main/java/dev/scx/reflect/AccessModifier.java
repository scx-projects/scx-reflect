package dev.scx.reflect;

/// AccessModifier
///
/// @author scx567888
/// @version 0.0.1
public enum AccessModifier {

    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    PACKAGE_PRIVATE("package-private");

    private final String text;

    AccessModifier(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

}
