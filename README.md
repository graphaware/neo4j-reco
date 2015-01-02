GraphAware Neo4j Recommendation Engine
======================================

[![Build Status](https://travis-ci.org/graphaware/neo4j-reco.png)](https://travis-ci.org/graphaware/neo4j-reco) | <a href="http://graphaware.com/products/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/reco/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 2.1.6.26.1

GraphAware Neo4j Recommendation Engine is a library for building high-performance complex recommendation engines atop Neo4j.
It is in production at a number of GraphAware's clients producing real-time recommendations on graphs with hundreds of millions of nodes.

The library imposes a specific recommendation engine architecture, which has emerged from years of experience building recommendation
engines on top of Neo4j. In return, it offers high performance and handles most of the plumbing so that you only write
the recommendation business logic specific to your use case.

Besides computing recommendations in real-time, it also allows for pre-computing recommendations that are perhaps too complex
to compute in real-time. The pre-computing happens on best-effort basis during quiet periods, so that it does not interfere
with regular transaction processing that your Neo4j database is performing.

Getting the Software
--------------------

### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
you will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j Recommendation Engine .jar files (both of which you can <a href="http://graphaware.com/products/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation.

Unlike with other GraphAware Framework Modules, you will need to write at least a few lines of your own Java code (read on).

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
GraphAware Runtime Modules, or Spring MVC Controllers can include use the module as a dependency for their Java project.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22reco%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml.

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>reco</artifactId>
            <version>2.1.6.26.1</version>
        </dependency>
        ...
    </dependencies>

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to 2.1.6.26.2-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the Recommendation Engine library. For example, version 2.1.6.26.1 is version 1 of the Recommendation Engine
 compatible with GraphAware Neo4j Framework 2.1.6.26.

Introduction to GraphAware Recommendation Engine
------------------------------------------------

The purpose of a recommendation engine is (unsurprisingly) to recommend something to users. This could be products they
should buy, users they should connect with, artists they should follow, etc. It turns out graph is a really good data
structure for representing users' interests, behaviours, and other characteristics that might be useful for finding
recommendations. More importantly, graph databases, and Neo4j especially, provide a natural way of expressing queries on
this data in order to find relevant recommendations, and executing these queries very fast.

There are three main challenges when building a recommendation engine. The first is to discover the items to recommend.
The second is to choose the most relevant ones to present to the user. Finally, the third challenge is to find relevant
recommendations as quickly as possible. Preferably, this should happen in real-time, i.e. using the most up to date information
we have. The last thing we want to do is to recommend something the user has already purchased, or a person we know she
isn't interested in.

The first two points above are business rather than technical challenges. Typically, when you start building a recommendation
engine, you have some idea about how the recommended items will be discovered. For instance, you might want to recommend
items that other people with similar interests have bought. You also know, which items you absolutely do not want to recommend,
for example, items the user has already purchased, or people that we know are married as a potential match for a date.

The issue with recommendation relevance is usually something that needs to be experimented with. When building the first
recommendation engine, or perhaps even a proof of concept, one feature that shouldn't be missing is the ability to configure
how the recommendation relevance is computed and, perhaps more importantly, measure how users react to recommendations
produced by different relevance-computing configurations.

Finally, let's address the speed issue, which is of a technical nature. When serving real-time recommendations, users
shouldn't need to wait for more than, let's say, a couple hundred milliseconds. With Neo4j, we will be able to build many
different recommendation queries that take milliseconds to execute. However, there are situations (large graphs with some
very dense nodes) where we will need to take extra care in order not to slow the recommendation process down. Finally,
in situations where the recommendation logic and the size of the graph simply don't allow real-time computation, we will
need to look at pre-computing some recommendations, whilst avoiding the dangers of serving out of date recommendations.

Recommendation Engine Architecture
----------------------------------

The architecture of GraphAware Neo4j Recommendation Engine has been designed to address, or easily allow you to address,
all of the above challenges. The library works with the following concepts:

#### Recommendation Engines and Recommendations

A **Recommendation Engine** is a component that produces **Recommendations**, given an **Input**. Whilst the architecture is
generic enough to support other persistence mechanisms, we focus on Neo4j and so the input will typically be a Neo4j _Node_
representing a user for whom we want to find recommendations, a product for which we want to find buyers, etc.

A _RecommendationEngine_, as in the case of _DelegatingRecommendationEngine_ can be composed of other _RecommendationEngines_
that it delegates to. Usually, however, a _RecommendationEngine_ will encapsulate the querying and relevance-computing
logic for discovering recommendations based on a single logical criterion. Such engine typically extends
_SingleScoreRecommendationEngine_. For example, we would have one engine that discovers items a user may want to buy based
on what other users with similar tastes have bought. Another engine would discover items based on user's expressed
preferences. Yet another one could discover items to be recommended based on what is currently trending.

For performance reasons as well as to achieve good encapsulation, _RecommendationEngines_ are only concerned with discovering
and scoring all potential recommendations, without caring about the fact that some recommendations discovered this way may
not be suitable, perhaps because the user has already purchased the discovered item. Removing irrelevant recommendations
will be discussed shortly.

#### Scores and Score Transformers

_Recommendations_ is a collections of tuples/pairs, where each pair is composed of a recommended **Item** (again, typically a _Node_)
and associated relevance **Score**. The _Score_ is composed of **Partial Scores**. Each _Partial Score_ has a name and an integer
value. Typically, a single _SingleScoreRecommendationEngine_, as the name suggest, is responsible for a single _Partial Score_.

When an _Item_ has been discovered as a potential recommendation by multiple _SingleScoreRecommendationEngines_, its _Parial Scores_
will be tallied by the _Score_ object. For example, an item that is currently trending and matches the user's preferred
tastes will have a total relevance _Score_ composed of two _Partial Scores_, one due to the fact that it is trending, and
another one because it is a preferred item.

In some cases, an _Item_ might be discovered multiple times by the same _SingleScoreRecommendationEngine_. For example,
we may have an engine that suggests people a user should be friends with based on the fact that they have some friends in
common. Assuming an easy-to-imagine graph traversal that discovers these recommendations, a potential friend will be discovered
three times if he has three friends in common with the user we're computing recommendations for. However, each additional
friend in common might not bear the same relevance for the recommendation. Thus, each _Partial Score_ can have a
**Score Transformer** applied to it. A _ScoreTransformer_ can apply an arbitrary mathematical function to the _Partial Score_
computed by a _SingleScoreRecommendationEngine_.

#### Contexts, Context Factories, and Mode

_Recommendations_ are always computed within a **Context**. Whist each recommendation-computing process for a single _Input_
might involve multiple _RecommendationEngines_ and other components, there is usually a single _Context_ per computation
that encapsulates information relevant to the process. For example, the _Context_ provides information about the **Mode**
of computation, i.e. whether it is _REAL\_TIME_ or _BATCH_ (pre-computing). It also knows, how many recommendations should
be produced, and is able to decide, whether a potential recommendation discovered by a _RecommendationEngine_ is allowed
to be served to the user. For each computation, a new _Context_ is produced and this is typically achieved using a
singleton **ContextFactory**.

#### Blacklist Builders and Filters

TBD (in progress)

#### Post Processors

#### Pre-Computation


First of, these things need to
be discovered.

Using GraphAware Neo4j Recommendation Engine
--------------------------------------------



License
-------

Copyright (c) 2015 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.