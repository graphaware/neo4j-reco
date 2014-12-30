package com.graphaware.reco.generic.transform;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link ParetoScoreTransformer}.
 */
public class ParetoScoreTransformerTest {

    @Test
    public void verifyTransform() {
        ParetoScoreTransformer transformer = new ParetoScoreTransformer(100, 10);

        assertEquals(0, transformer.transform(null, 0));
        assertEquals(15, transformer.transform(null, 1));
        assertEquals(28, transformer.transform(null, 2));
        assertEquals(38, transformer.transform(null, 3));
        assertEquals(55, transformer.transform(null, 5));
        assertEquals(80, transformer.transform(null, 10));
        assertEquals(96, transformer.transform(null, 20));
        assertEquals(100, transformer.transform(null, 50));
        assertEquals(100, transformer.transform(null, 100));
        assertEquals(100, transformer.transform(null, 10000));
    }
}
