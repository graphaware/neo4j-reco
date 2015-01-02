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

Recommendation Engine Architecture
----------------------------------



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