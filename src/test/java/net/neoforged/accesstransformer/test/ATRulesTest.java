package net.neoforged.accesstransformer.test;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import net.neoforged.accesstransformer.AccessTransformer;

import static net.neoforged.accesstransformer.parser.Transformation.FinalState.*;
import static net.neoforged.accesstransformer.parser.Transformation.Modifier.*;
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
            ()->assertEquals(deflt, AccessTransformer.mergeWith(privte, DEFAULT, LEAVE), "Upgrade private to default"),
            ()->assertEquals(prot, AccessTransformer.mergeWith(privte, PROTECTED, LEAVE), "Upgrade private to protected"),
            ()->assertEquals(pub, AccessTransformer.mergeWith(privte, PUBLIC, LEAVE), "Upgrade private to public"),
            ()->assertEquals(pub, AccessTransformer.mergeWith(pub, PRIVATE, LEAVE), "No downgrade public to private"),
            ()->assertEquals(pub, AccessTransformer.mergeWith(pub, DEFAULT, LEAVE), "No downgrade public to default"),
            ()->assertEquals(pub, AccessTransformer.mergeWith(pub, PROTECTED, LEAVE), "No downgrade public to protected"),
            ()->assertEquals(prot, AccessTransformer.mergeWith(prot, PRIVATE, LEAVE), "No downgrade prot to private"),
            ()->assertEquals(prot, AccessTransformer.mergeWith(prot, DEFAULT, LEAVE), "No downgrade prot to default"),
            ()->assertEquals(deflt, AccessTransformer.mergeWith(deflt, PRIVATE, LEAVE), "No downgrade default to private")
        );
    }
    @Test
    void testFinalUpgrade() {
        int fnl = Opcodes.ACC_FINAL; // 0x10
        int notfnl = 0;

        assertAll(
                ()->assertEquals(fnl, AccessTransformer.mergeWith(fnl, DEFAULT, MAKEFINAL), "makefinal+final = final"),
                ()->assertEquals(fnl, AccessTransformer.mergeWith(notfnl, DEFAULT, MAKEFINAL), "makefinal+notfinal = final"),
                ()->assertEquals(notfnl, AccessTransformer.mergeWith(fnl, DEFAULT, REMOVEFINAL), "removefinal+final = notfinal"),
                ()->assertEquals(notfnl, AccessTransformer.mergeWith(notfnl, DEFAULT, REMOVEFINAL), "removefinal+notfinal = notfinal"),
                ()->assertEquals(fnl, AccessTransformer.mergeWith(fnl, DEFAULT, LEAVE), "leave+final = final"),
                ()->assertEquals(notfnl, AccessTransformer.mergeWith(notfnl, DEFAULT, LEAVE), "leave+notfinal = notfinal")
        );
    }
}
