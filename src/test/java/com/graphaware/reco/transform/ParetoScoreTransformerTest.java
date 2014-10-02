package com.graphaware.reco.transform;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link ParetoScoreTransformer}.
 */
public class ParetoScoreTransformerTest {

    @Test
    public void verifyTransform() {
        ParetoScoreTransformer transformer = new ParetoScoreTransformer(100, 10);

        assertEquals(0, transformer.transform(0));
        assertEquals(15, transformer.transform(1));
        assertEquals(28, transformer.transform(2));
        assertEquals(38, transformer.transform(3));
        assertEquals(55, transformer.transform(5));
        assertEquals(80, transformer.transform(10));
        assertEquals(96, transformer.transform(20));
        assertEquals(100, transformer.transform(50));
        assertEquals(100, transformer.transform(100));
        assertEquals(100, transformer.transform(10000));
    }
}
