/*
 * Copyright (c) 2014 GraphAware
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

import com.graphaware.common.util.Pair;
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
        assertTrue(r.getItems().isEmpty());

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

        List<Pair<String, Score>> result = r.get(10);

        assertEquals(2, result.size());
        Pair<String, Score> one = result.get(0);
        Pair<String, Score> two = result.get(1);

        assertEquals("Reco1", one.first());
        assertEquals("Reco2", two.first());

        assertEquals(2, one.second().get("Score1"));
        assertEquals(1, one.second().get("Score2"));
        assertEquals(2, two.second().get("Score1"));

        assertEquals(2, r.getItems().size());
        assertTrue(r.getItems().contains("Reco1"));
        assertTrue(r.getItems().contains("Reco2"));

        assertEquals(2, r.get("Reco1").get("Score1"));
        assertEquals(1, r.get("Reco1").get("Score2"));
        assertEquals(2, r.get("Reco2").get("Score1"));

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

        List<Pair<String, Score>> result = r.get(10);

        assertEquals(3, result.size());
        Pair<String, Score> one = result.get(0);
        Pair<String, Score> two = result.get(1);
        Pair<String, Score> three = result.get(2);

        assertEquals("Reco1", one.first());
        assertEquals("Reco0", two.first());
        assertEquals("Reco2", three.first());

        assertEquals(39997, one.second().getTotalScore());
        assertEquals(35003, two.second().getTotalScore());
        assertEquals(15000, three.second().getTotalScore());

        assertEquals(6664, one.second().get("Score0"));
        assertEquals(20833, one.second().get("Score1"));
        assertEquals(12500, one.second().get("Score3"));

        assertEquals(16666, two.second().get("Score0"));
        assertEquals(8337, two.second().get("Score1"));
        assertEquals(10000, two.second().get("Score2"));

        assertEquals(6670, three.second().get("Score0"));
        assertEquals(8330, three.second().get("Score1"));
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

        List<Pair<String, Score>> result = r.get(10);

        assertEquals(2, result.size());
        Pair<String, Score> one = result.get(0);
        Pair<String, Score> two = result.get(1);

        assertEquals("Reco1", one.first());
        assertEquals("Reco2", two.first());

        assertEquals(4, one.second().get("Score1"));
        assertEquals(2, one.second().get("Score2"));
        assertEquals(2, two.second().get("Score1"));

        assertEquals(2, r.getItems().size());
        assertTrue(r.getItems().contains("Reco1"));
        assertTrue(r.getItems().contains("Reco2"));

        assertEquals(4, r.get("Reco1").get("Score1"));
        assertEquals(2, r.get("Reco1").get("Score2"));
        assertEquals(2, r.get("Reco2").get("Score1"));
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

        List<Pair<String, Score>> result = r.get(10);

        assertEquals(3, result.size());
        Pair<String, Score> one = result.get(0);
        Pair<String, Score> two = result.get(1);
        Pair<String, Score> three = result.get(2);

        assertEquals("Reco1", one.first());
        assertEquals("Reco0", two.first());
        assertEquals("Reco2", three.first());

        assertEquals(39997, one.second().getTotalScore());
        assertEquals(35003, two.second().getTotalScore());
        assertEquals(15000, three.second().getTotalScore());

        assertEquals(6664, one.second().get("Score0"));
        assertEquals(20833, one.second().get("Score1"));
        assertEquals(12500, one.second().get("Score3"));

        assertEquals(16666, two.second().get("Score0"));
        assertEquals(8337, two.second().get("Score1"));
        assertEquals(10000, two.second().get("Score2"));

        assertEquals(6670, three.second().get("Score0"));
        assertEquals(8330, three.second().get("Score1"));
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

        List<Pair<String, Score>> result = r.get(10);

        assertEquals(3, result.size());
        Pair<String, Score> one = result.get(0);
        Pair<String, Score> two = result.get(1);
        Pair<String, Score> three = result.get(2);

        assertEquals("Reco1", one.first());
        assertEquals("Reco0", two.first());
        assertEquals("Reco2", three.first());

        assertEquals(39997, one.second().getTotalScore());
        assertEquals(35003, two.second().getTotalScore());
        assertEquals(15000, three.second().getTotalScore());

        assertEquals(6664, one.second().get("Score0"));
        assertEquals(20833, one.second().get("Score1"));
        assertEquals(12500, one.second().get("Score3"));

        assertEquals(16666, two.second().get("Score0"));
        assertEquals(8337, two.second().get("Score1"));
        assertEquals(10000, two.second().get("Score2"));

        assertEquals(6670, three.second().get("Score0"));
        assertEquals(8330, three.second().get("Score1"));
    }

    @Test
    public void shouldRespectLimit() {
        Recommendations<String> r = new Recommendations<>();

        r.add("Reco1", "Score1", 1);
        r.add("Reco2", "Score1", 2);
        r.add("Reco1", "Score2", 1);
        r.add("Reco1", "Score1", 1);

        List<Pair<String, Score>> result = r.get(1);

        assertEquals(1, result.size());
        Pair<String, Score> one = result.get(0);

        assertEquals("Reco1", one.first());
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
