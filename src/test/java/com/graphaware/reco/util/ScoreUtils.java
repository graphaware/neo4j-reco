/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.util;

import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Score;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ScoreUtils {

	public static void assertScoresEqual(Recommendation<Node> expected, Recommendation<Node> recommendation) {
		Score recommendationScore = recommendation.getScore();
		Score expectedScore = expected.getScore();
		assertEquals(expectedScore.getTotalScore(), recommendationScore.getTotalScore(), 0);
		assertEquals(expectedScore.getScoreParts().size(), recommendationScore.getScoreParts().size());
		for (String partialScoreName : expectedScore.getScoreParts().keySet()) {
			PartialScore recommendedPartialScore = recommendationScore.getScoreParts().get(partialScoreName);
			PartialScore expectedPartialScore = expectedScore.getScoreParts().get(partialScoreName);
			assertNotNull(recommendedPartialScore);
			assertEquals(expectedPartialScore.getValue(), recommendedPartialScore.getValue(),0);
			assertEquals(expectedPartialScore.getReasons(), recommendedPartialScore.getReasons());
		}
	}

}
