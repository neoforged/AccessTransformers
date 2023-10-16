package net.neoforged.accesstransformer.testJar;

@SuppressWarnings("unused")
public class ATTestClass {
    private final String finalPrivateField = "EMPTY";
    private String privateField = "EMPTY";

    private void privateMethod() {
    }

    public void otherMethod() {
        privateMethod();
    }
}
