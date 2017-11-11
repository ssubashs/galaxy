FROM java:8-jre

ENV VERTICLE_FILE target/clustermanager-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 5701

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/conf/cluster.xml $VERTICLE_HOME/


WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -Dvertx.hazelcast.config=cluster.xml -jar clustermanager-fat.jar -cluster"]
