# Compile from source to Scala
FROM hseeberger/scala-sbt:8u151-2.12.4-1.0.2 AS compile

WORKDIR /build

# Hack to fetch sbt first. Makes future compilation faster
COPY ./project/assembly.sbt /build/project/assembly.sbt
COPY ./project/plugins.sbt /build/project/plugins.sbt
COPY ./project/build.properties /build/project/build.properties
RUN touch /build/build.sbt
RUN sbt assembly

# Fetch the rest of our dependencies
COPY ./build.sbt /build/build.sbt
RUN sbt assembly

# COPY ./scalastyle-config.xml /build/
COPY ./src /build/src

RUN sbt assembly

# Run the assembly, generate the intermediate files (cache, maps)
FROM openjdk:jre-alpine AS one

WORKDIR /app

COPY ./onemap/bus-stops-headings.json /app/onemap/bus-stops-headings.json
COPY ./onemap/mrt-stations.json /app
COPY ./config.properties /app

COPY --from=compile /build/target/scala-2.11/beeline-routing-assembly*.jar /app/beeline-routing.jar
COPY ./SG.pbf /app/SG.pbf
RUN wget https://download.geofabrik.de/asia/malaysia-singapore-brunei-latest.osm.pbf -O /app/SG.pbf

RUN java -jar /app/beeline-routing.jar cache

# Final docker image
FROM openjdk:jre-alpine AS final

WORKDIR /app

COPY --from=one /app/SG-gh /app/SG-gh
COPY --from=one /app/distances_cache.dat.gz /app
COPY ./distances_cache.dat.gz /app

COPY ./onemap/bus-stops-headings.json /app/onemap/bus-stops-headings.json
COPY ./onemap/mrt-stations.json /app
COPY ./config.properties /app

COPY --from=compile ./target/scala-2.11/beeline-routing-assembly*.jar /app/beeline-routing.jar

ENV PORT 5000

CMD java -jar /app/beeline-routing.jar

