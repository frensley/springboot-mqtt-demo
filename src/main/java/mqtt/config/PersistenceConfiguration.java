package mqtt.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by sfrensley on 3/11/15.
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "mqtt.repository")
@EnableTransactionManagement
public class PersistenceConfiguration extends Neo4jConfiguration {

    public PersistenceConfiguration() {
        setBasePackage("mqtt.domain");
    }

    /**
     * Build graph database in current working directory.
     * @return
     */
    @Bean
    public GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder("./mqtt.db")
                .newGraphDatabase();
    }
}
