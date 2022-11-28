# PART 1 - Build jar via gradle
FROM gradle:7.5.1-jdk11 AS gradle

WORKDIR /usr/src/app
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN gradle clean installShadowDist


# Part 2 - Create flink container with jar
FROM flink:1.16.0-java11

ARG kotlinVersion=1.7.21
ARG flinkVersion=1.16.0

RUN apt update
RUN apt -y upgrade

USER flink

## Provide Kotlin jar
RUN wget -P $FLINK_HOME/lib https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/${kotlinVersion}/kotlin-stdlib-${kotlinVersion}.jar

## Provide Job jar
RUN mkdir -p $FLINK_HOME/usrlib
COPY --from=gradle /usr/src/app/build/libs/flink-parquet-batch-demo-1.0-all.jar $FLINK_HOME/usrlib/flink-parquet-batch-demo.jar

## Provide S3 plugin
RUN mkdir -p $FLINK_HOME/plugins/s3-fs-hadoop
RUN cp $FLINK_HOME/opt/flink-s3-fs-hadoop-${flinkVersion}.jar $FLINK_HOME/plugins/s3-fs-hadoop/
