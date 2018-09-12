package net.minecraftforge.accesstransformer.testJar;

public class ATTestClass {
    private final String finalPrivateField = "EMPTY";
    private String privateField = "EMPTY";

    private void privateMethod() {
    }

    public void otherMethod() {
        privateMethod();
    }
}
