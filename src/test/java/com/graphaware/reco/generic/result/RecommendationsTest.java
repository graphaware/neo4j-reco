/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.result;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit test for {@link com.graphaware.reco.generic.result.Recommendations}.
 */
public class RecommendationsTest {

    @Test
    public void newRecommendationsShouldBeEmpty() {
        Recommendations<String> r = new Recommendations<>();

        assertTrue(r.get(10).isEmpty());
        assertTrue(r.get().isEmpty());

        try {
            r.get("test");
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    @Test
    public void shouldAddSingleScore() {
        Recommendations<String> r = new Recommendations<>();

        r.add("Reco1", "Score1", 1);
        r.add("Reco2", "Score1", 2);
        r.add("Reco1", "Score2", 1);
        r.add("Reco1", "Score1", 1);

        List<Recommendation<String>> result = r.get(10);

        assertEquals(2, result.size());
        Recommendation<String> one = result.get(0);
        Recommendation<String> two = result.get(1);

        assertEquals("Reco1", one.getItem());
        assertEquals("Reco2", two.getItem());

        assertNotNull(one.getUuid());
        assertNotNull(two.getUuid());

        assertEquals(2, one.getScore().get("Score1"), 0);
        assertEquals(1, one.getScore().get("Score2"), 0);
        assertEquals(2, two.getScore().get("Score1"), 0);

        assertEquals(2, r.get().size());
        assertTrue(r.get().contains(new Recommendation<>("Reco1")));
        assertTrue(r.get().contains(new Recommendation<>("Reco2")));

        assertEquals(2, r.get("Reco1").getScore().get("Score1"), 0);
        assertEquals(1, r.get("Reco1").getScore().get("Score2"), 0);
        assertEquals(2, r.get("Reco2").getScore().get("Score1"), 0);

        try {
            r.get("Unknown");
            fail();
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    @Test
    public void shouldAddSingleScoreMultiThreaded() throws InterruptedException {
        final Recommendations<String> r = new Recommendations<>();

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 10000; i++) {
            final int j = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    r.add("Reco" + (j % 3), "Score" + (j % 2), (j % 10));
                    r.add("Reco" + (j % 2), "Score" + (j % 4), (j % 10));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        List<Recommendation<String>> result = r.get(10);

        assertEquals(3, result.size());
        Recommendation<String> one = result.get(0);
        Recommendation<String> two = result.get(1);
        Recommendation<String> three = result.get(2);

        assertEquals("Reco1", one.getItem());
        assertEquals("Reco0", two.getItem());
        assertEquals("Reco2", three.getItem());

        assertEquals(39997, one.getScore().getTotalScore(), 0);
        assertEquals(35003, two.getScore().getTotalScore(), 0);
        assertEquals(15000, three.getScore().getTotalScore(), 0);

        assertEquals(6664, one.getScore().get("Score0"), 0);
        assertEquals(20833, one.getScore().get("Score1"), 0);
        assertEquals(12500, one.getScore().get("Score3"), 0);

        assertEquals(16666, two.getScore().get("Score0"), 0);
        assertEquals(8337, two.getScore().get("Score1"), 0);
        assertEquals(10000, two.getScore().get("Score2"), 0);

        assertEquals(6670, three.getScore().get("Score0"), 0);
        assertEquals(8330, three.getScore().get("Score1"), 0);
    }

    @Test
    public void shouldAddFullScore() {
        Recommendations<String> r = new Recommendations<>();

        Score s1 = new Score();
        s1.add("Score1", 2);
        s1.add("Score2", 1);

        Score s2 = new Score();
        s2.add("Score1", 2);

        r.add("Reco1", s1);
        r.add("Reco2", s2);
        r.add("Reco1", s1);

        List<Recommendation<String>> result = r.get(10);

        assertEquals(2, result.size());
        Recommendation<String> one = result.get(0);
        Recommendation<String> two = result.get(1);

        assertEquals("Reco1", one.getItem());
        assertEquals("Reco2", two.getItem());

        assertEquals(4, one.getScore().get("Score1"), 0);
        assertEquals(2, one.getScore().get("Score2"), 0);
        assertEquals(2, two.getScore().get("Score1"), 0);

        assertEquals(2, r.get().size());
        assertTrue(r.get().contains(new Recommendation<>("Reco1")));
        assertTrue(r.get().contains(new Recommendation<>("Reco2")));

        assertEquals(4, r.get("Reco1").getScore().get("Score1"), 0);
        assertEquals(2, r.get("Reco1").getScore().get("Score2"), 0);
        assertEquals(2, r.get("Reco2").getScore().get("Score1"), 0);
    }

    @Test
    public void shouldAddFullScoreMultiThreaded() throws InterruptedException {
        final Recommendations<String> r = new Recommendations<>();

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 10000; i++) {
            final int j = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Score s1 = new Score();
                    s1.add("Score" + (j % 2), (j % 10));

                    Score s2 = new Score();
                    s2.add("Score" + (j % 4), (j % 10));

                    r.add("Reco" + (j % 3), s1);
                    r.add("Reco" + (j % 2), s2);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        List<Recommendation<String>> result = r.get(10);

        assertEquals(3, result.size());
        Recommendation<String> one = result.get(0);
        Recommendation<String> two = result.get(1);
        Recommendation<String> three = result.get(2);

        assertEquals("Reco1", one.getItem());
        assertEquals("Reco0", two.getItem());
        assertEquals("Reco2", three.getItem());

        assertEquals(39997, one.getScore().getTotalScore(), 0);
        assertEquals(35003, two.getScore().getTotalScore(), 0);
        assertEquals(15000, three.getScore().getTotalScore(), 0);

        assertEquals(6664, one.getScore().get("Score0"), 0);
        assertEquals(20833, one.getScore().get("Score1"), 0);
        assertEquals(12500, one.getScore().get("Score3"), 0);

        assertEquals(16666, two.getScore().get("Score0"), 0);
        assertEquals(8337, two.getScore().get("Score1"), 0);
        assertEquals(10000, two.getScore().get("Score2"), 0);

        assertEquals(6670, three.getScore().get("Score0"), 0);
        assertEquals(8330, three.getScore().get("Score1"), 0);
    }

    @Test
    public void shoulMergeMultiThreaded() throws InterruptedException {
        final Recommendations<String> r = new Recommendations<>();

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 10000; i++) {
            final int j = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Recommendations<String> toMerge = new Recommendations<>();

                    Score s1 = new Score();
                    s1.add("Score" + (j % 2), (j % 10));

                    Score s2 = new Score();
                    s2.add("Score" + (j % 4), (j % 10));

                    toMerge.add("Reco" + (j % 3), s1);
                    toMerge.add("Reco" + (j % 2), s2);

                    r.merge(toMerge);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        List<Recommendation<String>> result = r.get(10);

        assertEquals(3, result.size());
        Recommendation<String> one = result.get(0);
        Recommendation<String> two = result.get(1);
        Recommendation<String> three = result.get(2);

        assertEquals("Reco1", one.getItem());
        assertEquals("Reco0", two.getItem());
        assertEquals("Reco2", three.getItem());

        assertEquals(39997, one.getScore().getTotalScore(), 0);
        assertEquals(35003, two.getScore().getTotalScore(), 0);
        assertEquals(15000, three.getScore().getTotalScore(), 0);

        assertEquals(6664, one.getScore().get("Score0"), 0);
        assertEquals(20833, one.getScore().get("Score1"), 0);
        assertEquals(12500, one.getScore().get("Score3"), 0);

        assertEquals(16666, two.getScore().get("Score0"), 0);
        assertEquals(8337, two.getScore().get("Score1"), 0);
        assertEquals(10000, two.getScore().get("Score2"), 0);

        assertEquals(6670, three.getScore().get("Score0"), 0);
        assertEquals(8330, three.getScore().get("Score1"), 0);
    }

    @Test
    public void shouldRespectLimit() {
        Recommendations<String> r = new Recommendations<>();

        r.add("Reco1", "Score1", 1);
        r.add("Reco2", "Score1", 2);
        r.add("Reco1", "Score2", 1);
        r.add("Reco1", "Score1", 1);

        List<Recommendation<String>> result = r.get(1);

        assertEquals(1, result.size());
        Recommendation<String> one = result.get(0);

        assertEquals("Reco1", one.getItem());
    }

    @Test
    public void shouldCorrectlyJudgeEnoughItems() {
        Recommendations<String> r = new Recommendations<>();

        r.add("Reco1", "Score1", 1);
        r.add("Reco2", "Score1", 2);

        assertTrue(r.hasEnough(1));
        assertTrue(r.hasEnough(2));
        assertFalse(r.hasEnough(3));
    }
}
