package CypherApi;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

public class CypherTest {
    public static void inquireTest(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            //通过Cypher查询获得结果
            StringBuilder sb = new StringBuilder();
            sb.append("start john = node(0) ");
            sb.append("match (john)-[:IS_FRIEND_OF]->(USER)-[:HAS_SEEN]->(movie) ");
            sb.append("return movie;");

            Result result = graphDB.execute(sb.toString());

            //遍历结果
            while(result.hasNext()){
                //get("movie")和查询语句的return movie相匹配
                Node movie = (Node) result.next().get("movie");
                System.out.println(movie.getId() + " : " + movie.getProperty("name"));
            }
            tx.success();
        }
        graphDB.shutdown();
    }
}
