package net.minecraftforge.accesstransformer;

public interface INameHandler {
    String translateClassName(String className);
    String translateFieldName(String fieldName);
    String translateMethodName(String methodName);
}
