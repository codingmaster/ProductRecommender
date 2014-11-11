mvn install:install-file -Dfile="./lib/virt_jena.jar" -DgroupId=virtuoso.jena -DartifactId=virtuoso-jena -Dversion=1.10 -Dpackaging=jar
mvn install:install-file -Dfile="./lib/virtjdbc3.jar" -DgroupId=virtuoso.jdbc3 -DartifactId=virtuoso-jdbc3 -Dversion=3.62 -Dpackaging=jar
mvn clean compile assembly:single
cd target
cp ProductRecommender-1.0-SNAPSHOT-jar-with-dependencies.jar ../recommender.jar
