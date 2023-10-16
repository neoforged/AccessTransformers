package net.neoforged.accesstransformer.test;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static net.neoforged.accesstransformer.AccessTransformer.FinalState.*;
import static net.neoforged.accesstransformer.AccessTransformer.Modifier.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ATRulesTest {
    @Test
    void testATUpgrade() {
        // Does AT upgrade private->default->protected->public?
        int privte = Opcodes.ACC_PRIVATE; // 0x4
        int deflt = 0; // 0x0
        int prot = Opcodes.ACC_PROTECTED; // 0x2
        int pub = Opcodes.ACC_PUBLIC; // 0x1
        assertAll(
            ()->assertEquals(deflt, DEFAULT.mergeWith(privte), "Upgrade private to default"),
            ()->assertEquals(prot, PROTECTED.mergeWith(privte), "Upgrade private to protected"),
            ()->assertEquals(pub, PUBLIC.mergeWith(privte), "Upgrade private to public"),
            ()->assertEquals(pub, PRIVATE.mergeWith(pub), "No downgrade public to private"),
            ()->assertEquals(pub, DEFAULT.mergeWith(pub), "No downgrade public to default"),
            ()->assertEquals(pub, PROTECTED.mergeWith(pub), "No downgrade public to protected"),
            ()->assertEquals(prot, PRIVATE.mergeWith(prot), "No downgrade prot to private"),
            ()->assertEquals(prot, DEFAULT.mergeWith(prot), "No downgrade prot to default"),
            ()->assertEquals(deflt, PRIVATE.mergeWith(deflt), "No downgrade default to private")
        );
    }
    @Test
    void testFinalUpgrade() {
        int fnl = Opcodes.ACC_FINAL; // 0x10
        int notfnl = 0;

        assertAll(
                ()->assertEquals(fnl, MAKEFINAL.mergeWith(fnl), "makefinal+final = final"),
                ()->assertEquals(fnl, MAKEFINAL.mergeWith(notfnl), "makefinal+notfinal = final"),
                ()->assertEquals(notfnl, REMOVEFINAL.mergeWith(fnl), "removefinal+final = notfinal"),
                ()->assertEquals(notfnl, REMOVEFINAL.mergeWith(notfnl), "removefinal+notfinal = notfinal"),
                ()->assertEquals(fnl, LEAVE.mergeWith(fnl), "leave+final = final"),
                ()->assertEquals(notfnl, LEAVE.mergeWith(notfnl), "leave+notfinal = notfinal")
        );
    }
}
