GraphAware Neo4j Recommendation Engine
======================================

[![Build Status](https://travis-ci.org/graphaware/neo4j-reco.png)](https://travis-ci.org/graphaware/neo4j-reco) | <a href="http://graphaware.com/products/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/reco/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 2.1.6.26.1

GraphAware Neo4j Recommendation Engine is a library for building high-performance complex recommendation engines atop Neo4j.
It is in production at a number of <a href="http://graphaware.com" target="_blank">GraphAware</a>'s clients producing real-time recommendations on graphs with hundreds of millions of nodes.

The library imposes a specific recommendation engine architecture, which has emerged from our experience building recommendation
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
            <artifactId>recommendation-engine</artifactId>
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

There are three main challenges when building a recommendation engine. The first is to **discover** the items to recommend.
The second is to **choose the most relevant ones** to present to the user. Finally, the third challenge is to find relevant
recommendations **as quickly as possible**. Preferably, this should happen in real-time, i.e. using the most up to date information
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

Finally, let's address the issue of speed, which is of a technical nature. When serving real-time recommendations, users
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
generic enough to support other persistence mechanisms, we focus on Neo4j and so the input will typically be a Neo4j `Node`
representing a user for whom we want to find recommendations, a product for which we want to find buyers, etc.

A `RecommendationEngine`, as in the case of `DelegatingRecommendationEngine` can be composed of other `RecommendationEngine`s
that it delegates to. Usually, however, a `RecommendationEngine` will encapsulate the querying and relevance-computing
logic for discovering recommendations based on a single logical criterion. Such engine typically extends
`SingleScoreRecommendationEngine`. For example, we could have one engine that discovers items a user may want to buy based
on what other users with similar tastes have bought. Another engine would discover items based on user's expressed
preferences. Yet another one could discover items to be recommended based on what is currently trending.

For performance reasons as well as to achieve good encapsulation, `RecommendationEngines` are only concerned with discovering
and scoring all potential recommendations, without caring about the fact that some recommendations discovered this way may
not be suitable, perhaps because the user has already purchased the discovered item. Removing irrelevant recommendations
will be discussed shortly.

#### Scores and Score Transformers

`Recommendations` are a collection of tuples/pairs, where each pair is composed of a recommended item (again, typically a `Node`)
and associated relevance **Score**. The `Score` is composed of **Partial Scores**. Each _Partial Score_ has a name and an integer
value. Typically, a single `SingleScoreRecommendationEngine`, as the name suggest, is responsible for a single _Partial Score_.

When an item has been discovered as a potential recommendation by multiple `SingleScoreRecommendationEngine`s, its _Parial Scores_
will be tallied by the `Score` object. For example, an item that is currently trending and matches the user's preferred
tastes will have a total relevance `Score` composed of two _Partial Scores_, one due to the fact that it is trending, and
another one because it is a preferred item.

In some cases, an item might be discovered multiple times by the same `SingleScoreRecommendationEngine`. For example,
we may have an engine that suggests people a user should be friends with based on the fact that they have some friends in
common. Assuming an easy-to-imagine graph traversal that discovers these recommendations, a potential friend will be discovered
three times if he has three friends in common with the user we're computing recommendations for. However, each additional
friend in common might not bear the same relevance for the recommendation. Thus, each _Partial Score_ can have a
**Score Transformer** applied to it. A ScoreTransformer can apply an arbitrary mathematical function to the _Partial Score_
computed by a `SingleScoreRecommendationEngine`.

#### Contexts, Context Factories, and Mode

`Recommendations` are always computed within a **Context**. Whist each recommendation-computing process for a single input
might involve multiple `RecommendationEngine`s and other components, there is usually a single `Context` per computation
that encapsulates information relevant to the process. For example, the `Context` provides information about the **Mode**
of computation, i.e. whether it is `REAL_TIME` or `BATCH` (pre-computing). It also knows, how many recommendations should
be produced, and is able to decide, whether a potential recommendation discovered by a `RecommendationEngine` is allowed
to be served to the user. For each computation, a new `Context` is produced and this is typically achieved using a
singleton **ContextFactory**.

#### Blacklist Builders and Filters

Rather than requiring all `RecommendationEngine`s to know how to detect irrelevant recommendations (thus slowing the computation
down and scattering a single concern), the logic is centralised into **Blacklist Builders** and **Filters**. `BlacklistBuilder`s,
as the name suggests, are responsible for building "blacklists" of items that must not be recommended for a given input.

Assuming that the input is a `Node` representing a person, an example of a `BlacklistBuilder` could be `AlreadyPurchasedItems`
which builds a blacklist of items that the person has already purchased. `BlacklistBuilder`s are most efficient in situations
where a small number of irrelevant recommendations (let's say up to 100) can be discovered with a single query before the
recommendation process begins.

`Filter`s, on the other hand, can tell whether a recommendation is relevant or not by looking at the recommendation
itself once it has been discovered. An obvious example of a `Filter` could be a class called `ExcludeSelf`, which would
make sure that (for example) a recommended friend isn't the same `Node` that the recommendations are being computed for.
Another example of a `Filter` could be `ExcludeItemsOutOfStock`, or `ExcludeMarriedPeople`.

Blacklists produced by `BlacklistBuilder`s and `Filter`s are typically passed to an instance of `Context` (usually
`FilteringContext`), which uses them to exclude irrelevant recommendations.

#### Post Processors

In the presence of "supernodes", i.e. nodes with disproportionately many relationships, it would too expensive to compute
recommendations using dedicated `RecommendationEngine`s. Imagine, for example, that we would like to boost the score of
people living in the same city as the person we're computing recommendations for. Rather than implementing a `RecommendationEngine`
that discovers all people living in the same city (which could be millions!), we can implement a **PostProcessor** which
modifies the score of already computed recommendations. In the example above, a `PostProcessor` called `RewardSameCity`
could add 50 points to each recommendation if the person we're recommending to and the recommended person live in the same city.
It is much quicker to perform this check for each recommendation than discovering all people living in the same city.

Other examples of a `PostProcessor` could include `RewardSameGender`, `PenalizeAgeDifference`, etc.

#### Pre-Computation

Once we've built a `RecommendationEngine`, we could use it to continuously
pre-compute recommendations when the database isn't busy, using <a href="https://github.com/graphaware/neo4j-framework/tree/master/runtime#building-a-timer-driven-graphaware-runtime-module" target="_blank">GraphAware Timer-Driven Module</a>. For each potential
input, we could pre-compute a number of recommendations and link them to the input using a _RECOMMEND_ relationship.
When serving recommendations, we could read them directly from the database, rather than computing them in real-time.
Blacklists and `Filter`s are still consulted in case the situation has changed since the time recommendations were pre-computed.

Using GraphAware Neo4j Recommendation Engine
--------------------------------------------

The best place to start is by having a look at the `ModuleIntegrationTest` class and the other classes it uses.
Also, the classes in this library have a decent <a href="http://graphaware.com/site/reco/latest/apidocs/" target="_blank">Javadoc</a>,
which should help you get building your first recommendation engine. Feel free to get in touch for support (info@graphaware.com).

We will illustrate how easy it is to build a recommendation using an example. Let's say we have a graph of people, i.e.
`Node`s with label `:Person`. Moreover, each `:Person` also has a `:Male` or a `:Female` label, and two properties: a `name` (String) and
an `age` (integer). We will also have `Node`s with label `:City` and a `name` property.

The only two relationship types in our simple graph will be `FRIEND_OF` and `LIVES_IN` and we will assume friendships are mutual,
thus ignore the direction of the `FRIEND_OF` relationship. A sample graph, expressed in Cypher, could look like this:

```
    CREATE
    (m:Person:Male {name:'Michal', age:30}),
    (d:Person:Female {name:'Daniela', age:20}),
    (v:Person:Male {name:'Vince', age:40}),
    (a:Person:Male {name:'Adam', age:30}),
    (l:Person:Female {name:'Luanne', age:25}),
    (c:Person:Male {name:'Christophe', age:60}),

    (lon:City {name:'London'}),
    (mum:City {name:'Mumbai'}),

    (m)-[:FRIEND_OF]->(d),
    (m)-[:FRIEND_OF]->(l),
    (m)-[:FRIEND_OF]->(a),
    (m)-[:FRIEND_OF]->(v),
    (d)-[:FRIEND_OF]->(v),
    (c)-[:FRIEND_OF]->(v),
    (d)-[:LIVES_IN]->(lon),
    (v)-[:LIVES_IN]->(lon),
    (m)-[:LIVES_IN]->(lon),
    (l)-[:LIVES_IN]->(mum));
```

Our intention will be recommending people a person should be friends with, based on the following requirements:
(1) The more friends in common two people have, the more likely it is they should become friends
(2) The difference between zero and one friends in common should be significant and each additional friend in common should
 increase the recommendation relevance by a smaller magnitude.
(3) If people live in the same city, the chance of them becoming friends increases
(4) If people are of the same gender, the chance of them becoming friends is greater than if they are of opposite genders
(5) The bigger the age difference between two people, the lower the chance they will become friends
(6) People should not be friends with themselves
(7) People who are already friends should not be recommended as potential friends
(8) If we don't have enough recommendations, we will recommend some random people

Let's start tackling the requirements one by one.

### Real-Time Recommendations

#### FriendsInCommon

First, we will build a `RecommendationEngine` that finds recommendations based on friends in common. For each friend in
common, the relevance score will increase by 1. Since this is a single-criterion `RecommendationEngine`, we will extend
`SingleScoreRecommendationEngine` as follows:

```java
/**
 * {@link com.graphaware.reco.generic.engine.RecommendationEngine} that finds recommendation based on friends in common.
 */
public class FriendsInCommon extends SomethingInCommon {

    @Override
    protected RelationshipType getType() {
        return Relationships.FRIEND_OF;
    }

    @Override
    protected Direction getDirection() {
        return Direction.BOTH;
    }

    @Override
    protected String scoreName() {
        return "friendsInCommon";
    }
}
```

The code above tackles requirement (1). Let's modify the code to account for requirement (2) as well by providing an
exponential `ScoreTransformer`, called the `ParetoScoreTransformer`. Please read the Javadoc of the class to find out
exactly how it works. For now, it is sufficient to say that it will transform the number of friends in common to a score
with a theoretical upper value of 100, with 80% of the total score being achieved by 10 friends in common.

```java
/**
 * {@link com.graphaware.reco.generic.engine.RecommendationEngine} that finds recommendation based on friends in common.
 * <p/>
 * The score is increasing by Pareto function, achieving 80% score with 10 friends in common. The maximum score is 100.
 */
public class FriendsInCommon extends SomethingInCommon {

    @Override
    protected ScoreTransformer scoreTransformer() {
        return new ParetoScoreTransformer(100, 10);
    }

    @Override
    protected RelationshipType getType() {
        return Relationships.FRIEND_OF;
    }

    @Override
    protected Direction getDirection() {
        return BOTH;
    }

    @Override
    protected String scoreName() {
        return "friendsInCommon";
    }
}
```

#### FriendsInCommon

Whilst we're at it, we will also build the other `SingleScoreRecommendationEngine` that we'll need to satisfy requirement (8):

```java
 /**
  * {@link com.graphaware.reco.neo4j.engine.RandomRecommendations} selecting random nodes with "Person" label.
  */
 public class RandomPeople extends RandomRecommendations {

     @Override
     protected NodeInclusionPolicy getPolicy() {
         return new NodeInclusionPolicy() {
             @Override
             public boolean include(Node node) {
                 return node.hasLabel(DynamicLabel.label("Person"));
             }
         };
     }

     @Override
     protected String scoreName() {
         return "random";
     }
 }
```

Note that this engine will (automatically) only be used if there aren't enough genuine recommendations.

#### RewardSameLocation and RewardSameLabels

We will tackle requirements (3) and (4) by implementing some `PostProcessors` rather than separate `RecommendationEngine`s.
The reason is mainly performance; we do not want to suggest everyone who lives in the same city or who is of the same gender.
Instead, we will reward already discovered recommendations for living in the same city or being of the same gender, by
the following two classes:

```java
/**
 * Rewards same location by 10 points.
 */
public class RewardSameLocation extends RewardSomethingShared {

    @Override
    protected RelationshipType type() {
        return LIVES_IN;
    }

    @Override
    protected Direction direction() {
        return OUTGOING;
    }

    @Override
    protected int scoreValue(Node recommendation, Node input, Node sharedThing) {
        return 10;
    }

    @Override
    protected String scoreName() {
        return "sameLocation";
    }
}
```

```java
/**
 * Rewards same gender (exactly the same labels) by 10 points.
 */
public class RewardSameLabels implements PostProcessor<Node, Node> {

    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        Label[] inputLabels = toArray(Label.class, input.getLabels());

        for (Node recommendation : recommendations.getItems()) {
            if (Arrays.equals(inputLabels, toArray(Label.class, recommendation.getLabels()))) {
                recommendations.add(recommendation, "sameGender", 10);
            }
        }
    }
}
```

#### PenalizeAgeDifference

Another `PostProcessor` will take care of requirement (5). We will subtract 1 point from the relevance score for each
year of difference in age.

```java
/**
 * Subtracts a point of each year of difference in age.
 */
public class PenalizeAgeDifference implements PostProcessor<Node, Node> {

    private final ParetoScoreTransformer transformer = new ParetoScoreTransformer(10, 20, 0);

    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        int age = getInt(input, "age", 40);

        for (Node reco : recommendations.getItems()) {
            int diff = Math.abs(getInt(reco, "age", 40) - age);
            recommendations.add(reco, "ageDifference", -transformer.transform(reco, diff));
        }
    }
}
```

#### Blacklist Builders and Filters

We could build custom `BlacklistBuilder`s and `Filter`s as well to satisfy requirements (6) and (7), but we will just use classes
already provided by the library, as we will see shortly.

#### Putting it all together

Now that we have all the components that satisfy all 8 requirements, we just need to combine them into a `ContextFactory` and
a top-level `RecommendationEngine`. The following `ContextFactory` will produce `Context`s that do not allow existing
friends, or the person we are computing recommendations for, to be recommended as potential friends:

```java
/**
 * {@link com.graphaware.reco.neo4j.context.Neo4jContextFactory} for recommending friends.
 */
public final class FriendsContextFactory extends Neo4jContextFactory {

    @Override
    protected List<BlacklistBuilder<Node, Node>> blacklistBuilders() {
        return Arrays.asList(
                new ExistingRelationshipBlacklistBuilder(FRIEND_OF, BOTH)
        );
    }

    @Override
    protected List<Filter<Node, Node>> filters() {
        return Arrays.<Filter<Node, Node>>asList(
                new ExcludeSelf()
        );
    }
}
```

Finally, we will combine everything into a top-level engine:

```java
/**
 * {@link com.graphaware.reco.neo4j.engine.Neo4jTopLevelDelegatingEngine} that computes friend recommendations.
 */
public final class FriendsComputingEngine extends Neo4jTopLevelDelegatingEngine {

    public FriendsComputingEngine() {
        super(new FriendsContextFactory());
    }

    @Override
    protected List<RecommendationEngine<Node, Node>> engines() {
        return Arrays.<RecommendationEngine<Node, Node>>asList(
                new FriendsInCommon(),
                new RandomPeople()
        );
    }

    @Override
    protected List<PostProcessor<Node, Node>> postProcessors() {
        return Arrays.asList(
                new RewardSameLabels(),
                new RewardSameLocation(),
                new PenalizeAgeDifference()
        );
    }
}

```

#### A quick integration test

In this example, we have neglected unit testing altogether, which, of course, you shouldn't do. We will build a simple
integration test though in order to smoke-test our brand new recommendation engine.

```java
public class ModuleIntegrationTest extends WrappingServerIntegrationTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Neo4jTopLevelDelegatingEngine recommendationEngine;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        recommendationEngine = new FriendsComputingEngine();
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        new ExecutionEngine(database).execute(
                "CREATE " +
                        "(m:Person:Male {name:'Michal', age:30})," +
                        "(d:Person:Female {name:'Daniela', age:20})," +
                        "(v:Person:Male {name:'Vince', age:40})," +
                        "(a:Person:Male {name:'Adam', age:30})," +
                        "(l:Person:Female {name:'Luanne', age:25})," +
                        "(b:Person:Male {name:'Bob', age:60})," +

                        "(lon:City {name:'London'})," +
                        "(mum:City {name:'Mumbai'})," +

                        "(m)-[:FRIEND_OF]->(d)," +
                        "(m)-[:FRIEND_OF]->(l)," +
                        "(m)-[:FRIEND_OF]->(a)," +
                        "(m)-[:FRIEND_OF]->(v)," +
                        "(d)-[:FRIEND_OF]->(v)," +
                        "(b)-[:FRIEND_OF]->(v)," +
                        "(d)-[:LIVES_IN]->(lon)," +
                        "(v)-[:LIVES_IN]->(lon)," +
                        "(m)-[:LIVES_IN]->(lon)," +
                        "(l)-[:LIVES_IN]->(mum)");
    }

    @Test
    public void shouldRecommendRealTime() {
        try (Transaction tx = getDatabase().beginTx()) {
            List<Pair<Node, Score>> result;

            result = recommendationEngine.recommend(getPersonByName("Vince"), Mode.REAL_TIME, 2);
            assertEquals("" +
                            "(:Male:Person {age: 30, name: Adam}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:8, ageDifference:-7, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Adam"), Mode.REAL_TIME, 2);

            assertEquals("" +
                            "(:Male:Person {age: 40, name: Vince}): total:19, ageDifference:-6, friendsInCommon:15, sameGender:10" + LINE_SEPARATOR +
                            "(:Female:Person {age: 25, name: Luanne}): total:12, ageDifference:-3, friendsInCommon:15",
                    toString(result));

            result = recommendationEngine.recommend(getPersonByName("Luanne"), Mode.REAL_TIME, 4);

            assertEquals("Daniela", result.get(0).first().getProperty("name"));
            assertEquals(22, result.get(0).second().getTotalScore());

            assertEquals("Adam", result.get(1).first().getProperty("name"));
            assertEquals(12, result.get(1).second().getTotalScore());

            assertEquals("Vince", result.get(2).first().getProperty("name"));
            assertEquals(8, result.get(2).second().getTotalScore());

            assertEquals("Bob", result.get(3).first().getProperty("name"));
            assertEquals(-9, result.get(3).second().getTotalScore());

            tx.success();
        }
    }

    private Node getPersonByName(String name) {
        return IterableUtils.getSingle(getDatabase().findNodesByLabelAndProperty(DynamicLabel.label("Person"), "name", name));
    }

    private String toString(List<Pair<Node, Score>> recommendations) {
        StringBuilder s = new StringBuilder();
        for (Pair<Node, Score> pair : recommendations) {
            Node node = pair.first();
            Score score = pair.second();
            s.append(PropertyContainerUtils.nodeToString(node)).append(": ");
            s.append("total:").append(score.getTotalScore());
            for (Map.Entry<String, Integer> part : score.getScoreParts().entrySet()) {
                s.append(", ");
                s.append(part.getKey()).append(":").append(part.getValue());
            }
            s.append(LINE_SEPARATOR);
        }

        String result = s.toString();
        if (result.isEmpty()) {
            return result;
        }
        return result.substring(0, result.length() - LINE_SEPARATOR.length());
    }
}
```

### Pre-Computed Recommendations

TBD


License
-------

Copyright (c) 2015 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.