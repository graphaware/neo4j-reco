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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link com.graphaware.reco.generic.result.Score}.
 */
public class ScoreTest {

    @Test
    public void newScoreShouldBeEmpty() {
        Score score = new Score();

        assertEquals(0, score.getTotalScore(),0);
        assertTrue(score.getScoreParts().isEmpty());
        assertEquals(0, score.get("someScore"),0);
    }

    @Test
    public void shouldCorrectlyAddToScore() {
        Score score = new Score();

        score.add("score1", 3);
        score.add("score2", 2);
        score.add("score1", 2);

        assertEquals(7, score.getTotalScore(),0);
        assertEquals(2, score.getScoreParts().size());
        assertTrue(score.getScoreParts().keySet().contains("score1"));
        assertTrue(score.getScoreParts().keySet().contains("score2"));
        assertEquals(2, score.get("score2"),0);
        assertEquals(5, score.get("score1"),0);
    }

    @Test
    public void shouldCorrectlyAddToScoreMultiThreaded() throws InterruptedException {
        final Score score = new Score();

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 1000; i++) {
            final int j =i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    score.add("score" + (j % 5), 1);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(1000, score.getTotalScore(),0);
        assertEquals(5, score.getScoreParts().size());
        assertTrue(score.getScoreParts().keySet().contains("score0"));
        assertTrue(score.getScoreParts().keySet().contains("score1"));
        assertTrue(score.getScoreParts().keySet().contains("score2"));
        assertTrue(score.getScoreParts().keySet().contains("score3"));
        assertTrue(score.getScoreParts().keySet().contains("score4"));
        assertEquals(200, score.get("score0"),0);
        assertEquals(200, score.get("score1"),0);
        assertEquals(200, score.get("score2"),0);
        assertEquals(200, score.get("score3"),0);
        assertEquals(200, score.get("score4"),0);
    }

    @Test
    public void shouldCorrectlyMergeScores() {
        Score score1 = new Score();

        score1.add("score1", 3);
        score1.add("score2", 2);
        score1.add("score1", 2);

        Score score2 = new Score();

        score2.add("score1", 3);
        score2.add("score2", 2);
        score2.add("score3", 2);

        Score merged = score1.merge(score2);

        assertEquals(14, merged.getTotalScore(),0);
        assertEquals(3, merged.getScoreParts().size());
        assertTrue(merged.getScoreParts().keySet().contains("score1"));
        assertTrue(merged.getScoreParts().keySet().contains("score2"));
        assertTrue(merged.getScoreParts().keySet().contains("score3"));
        assertEquals(2, merged.get("score3"),0);
        assertEquals(4, merged.get("score2"),0);
        assertEquals(8, merged.get("score1"),0);
    }

    @Test
    public void shouldCorrectlyMergeScoresMultiThreaded() throws InterruptedException {
        final Score score = new Score();

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 1000; i++) {
            final int j =i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Score newScore = new Score();
                    newScore.add("score" + (j % 5), 1);
                    score.merge(newScore);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        assertEquals(1000, score.getTotalScore(),0);
        assertEquals(5, score.getScoreParts().size());
        assertTrue(score.getScoreParts().keySet().contains("score0"));
        assertTrue(score.getScoreParts().keySet().contains("score1"));
        assertTrue(score.getScoreParts().keySet().contains("score2"));
        assertTrue(score.getScoreParts().keySet().contains("score3"));
        assertTrue(score.getScoreParts().keySet().contains("score4"));
        assertEquals(200, score.get("score0"),0);
        assertEquals(200, score.get("score1"),0);
        assertEquals(200, score.get("score2"),0);
        assertEquals(200, score.get("score3"),0);
        assertEquals(200, score.get("score4"),0);
    }

    @Test
    public void shouldCorrectlyOrderScores() {
        Score score1 = new Score();

        score1.add("score1", 3);
        score1.add("score2", 3);
        score1.add("score1", 2);

        Score score2 = new Score();

        score2.add("score1", 3);
        score2.add("score2", 2);
        score2.add("score3", 2);

        assertTrue(score1.compareTo(score2) > 0);

        List<Score> scores = Arrays.asList(score1, score2);
        Collections.sort(scores);

        assertEquals(7, scores.get(0).getTotalScore(),0);
        assertEquals(8, scores.get(1).getTotalScore(),0);
    }
}
