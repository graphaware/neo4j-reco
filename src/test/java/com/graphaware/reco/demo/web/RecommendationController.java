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

package com.graphaware.reco.demo.web;

import com.graphaware.reco.demo.RecruitingRecoEngine;
import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.engine.TopLevelRecommendationEngine;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.web.ConfigParser;
import com.graphaware.reco.generic.web.KeyValueConfigParser;
import org.neo4j.graphdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

import static com.graphaware.common.util.IterableUtils.getSingle;

@Controller
public class RecommendationController {

    private final GraphDatabaseService database;
    private final TopLevelRecommendationEngine<Node, Node> engine = new RecruitingRecoEngine();
    private final ConfigParser<KeyValueConfig> parser = new KeyValueConfigParser(":");

    @Autowired
    public RecommendationController(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping("/recommendation/{name}")
    @ResponseBody
    public List<RecommendationVO> recommend(@PathVariable String name, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String config) {
        try (Transaction tx = database.beginTx()) {
            return convert(engine.recommend(findCompanyByName(name), parser.produceConfig(limit, config)));
        }
    }

    private Node findCompanyByName(String name) {
        return getSingle(database.findNodes(DynamicLabel.label("Company"), "name", name), "Company with name " + name + " does not exist.");
    }

    private List<RecommendationVO> convert(List<Recommendation<Node>> recommendations) {
        List<RecommendationVO> result = new LinkedList<>();

        for (Recommendation<Node> recommendation : recommendations) {
            result.add(new RecommendationVO(recommendation.getUuid(), recommendation.getItem().getProperty("name", "unknown").toString(), recommendation.getScore()));
        }

        return result;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String translateException(NotFoundException e) {
        return e.getMessage();
    }
}
