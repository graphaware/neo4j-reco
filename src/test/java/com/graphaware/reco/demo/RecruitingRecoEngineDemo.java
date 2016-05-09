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

package com.graphaware.reco.demo;

import com.graphaware.reco.demo.web.RecommendationController;
import com.graphaware.reco.demo.web.RecommendationVO;
import com.graphaware.test.data.DatabasePopulator;
import com.graphaware.test.data.GraphgenPopulator;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A demonstration of an end-to-end recommendation engine. Mostly for showcasing and documenting functionality rather
 * than functional testing.
 */
public class RecruitingRecoEngineDemo extends GraphAwareIntegrationTest {

    @Override
    protected DatabasePopulator databasePopulator() {
        return new GraphgenPopulator() {
            @Override
            protected String file() throws IOException {
                return new ClassPathResource("demo-data.cyp").getFile().getAbsolutePath();
            }
        };
    }

    @Test
    public void shouldRecommendSomethingUsingApi() throws IOException {
        String result1 = httpClient.get(baseUrl() + "/recommendation/Durgan%20LLC?limit=2", HttpStatus.OK_200);
        System.out.println("=== RESULT 1 ===");
        System.out.println(result1);
        System.out.println("=== END ===");

        String result2 = httpClient.get(baseUrl() + "/recommendation/Durgan%20LLC?limit=2&config=legalAge:15:pointsPerSkill:2", HttpStatus.OK_200);
        System.out.println("=== RESULT 2 ===");
        System.out.println(result2);
        System.out.println("=== END ===");

        assertTrue(result1.length() > 10);
        assertTrue(result2.length() > 10);
    }

    @Test
    public void shouldRecommendDifferentThingsWithDifferentConfig() throws IOException {
        RecommendationController controller = new RecommendationController(getDatabase());

        List<RecommendationVO> reco1 = controller.recommend("Durgan LLC", 2, "");
        List<RecommendationVO> reco2 = controller.recommend("Durgan LLC", 2, "legalAge:15:pointsPerSkill:2");

        RecommendationVO reco1r1 = reco1.get(0);
        RecommendationVO reco1r2 = reco1.get(1);
        RecommendationVO reco2r1 = reco2.get(0);
        RecommendationVO reco2r2 = reco2.get(1);

        assertEquals("Zoila McDermott", reco1r1.getItem());
        assertEquals(18, reco1r1.getScore().getTotalScore(), 0.01);
        assertEquals("Garry Terry", reco1r2.getItem());
        assertEquals(18, reco1r2.getScore().getTotalScore(), 0.01);
        assertEquals("Lilyan Beer", reco2r1.getItem());
        assertEquals(29, reco2r1.getScore().getTotalScore(), 0.01);
        assertEquals("Zoila McDermott", reco2r2.getItem());
        assertEquals(26, reco2r2.getScore().getTotalScore(), 0.01);
    }
}
