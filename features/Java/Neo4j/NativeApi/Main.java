package NativeApi;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.impl.StandardExpander;
import org.neo4j.graphdb.traversal.*;


public class Main {
    public static void main(String[] args){
        String path = "D:/Neo4j/neo4j-community-3.3.5/data/databases/TMP1.db";
        //Main.createDB(path);
        //Main.traversalTestJavaApi(path);
        //Main.travelTestNeo4jApi(path);
        //IndexTest.creatIndex(path);
        //IndexTest.updateIndex(path);
        IndexTest.schemaIndex(path);
    }

    public static void createDB(String s){
        /**
        * para s 为要打开的数据库的路径
        * */
        /**
         * 指定Neo4j存储路径
         */
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        try(Transaction tx = graphDB.beginTx()){
            /**
             * 新增User节点
             * 每个节点设置name属性
             * 添加Lable以区分节点类型
             */
            Node user1 = graphDB.createNode();
            user1.setProperty("name", "John Johnson");
            user1.addLabel(MyLabels.USERS);

            Node user2 = graphDB.createNode();
            user2.setProperty("name", "Kate Smith");
            user2.addLabel(MyLabels.USERS);

            Node user3 = graphDB.createNode();
            user3.setProperty("name", "Jack Jeffries");
            user3.addLabel(MyLabels.USERS);

            /**
             * 为user1添加Friend关系
             * 注：Neo4j的关系是有向的箭头，正常来讲Friend关系应该是双向的，
             *    此处为了简单起见，将关系定义成单向的，不会影响后期的查询
             */
            user1.createRelationshipTo(user2,MyRelationshipTypes.IS_FRIEND_OF);
            user1.createRelationshipTo(user3,MyRelationshipTypes.IS_FRIEND_OF);

            /**
             * 新增Movie节点
             * 每个节点设置name属性
             * 添加Lable以区分节点类型
             */
            Node movie1 = graphDB.createNode();
            movie1.setProperty("name", "Fargo");
            movie1.addLabel(MyLabels.MOVIES);

            Node movie2 = graphDB.createNode();
            movie2.setProperty("name", "Alien");
            movie2.addLabel(MyLabels.MOVIES);

            Node movie3 = graphDB.createNode();
            movie3.setProperty("name", "Heat");
            movie3.addLabel(MyLabels.MOVIES);

            /**
             * 为User节点和Movie节点之间添加HAS_SEEN关系
             * HAS_SEEN关系设置stars属性
             */
            Relationship relationship1 = user1.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            relationship1.setProperty("stars", 5);

            Relationship relationship2 = user2.createRelationshipTo(movie3, MyRelationshipTypes.HAS_SEEN);
            relationship2.setProperty("stars", 3);

            Relationship relationship6 = user2.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);
            relationship6.setProperty("stars", 6);

            Relationship relationship3 = user3.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            relationship3.setProperty("stars", 4);

            Relationship relationship4 = user3.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);
            relationship4.setProperty("stars", 5);

            System.out.println("成功");
            tx.success();
        }

        //关闭数据库
        graphDB.shutdown();

    }


    /**
     * 使用java提供的API进行遍历
     * Node.getRelationships(…)返回的时一个Iterable对象，在对结果迭代之前实际上还没有访问结果中包含的元素。
     * 这样会产生一个问题，Iterable中有可能返回非常大的数据集，当转成Java的集合类时会使用比较多的内存
     * */
    public static void traversalTestJavaApi(String s){
        /**
         * 指定Neo4j存储路径
         */
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        /**
         * 遍历John看过的电影
         */
        try(Transaction tx = graphDB.beginTx()){

            //在管理界面可以查处John节点的id为0
            Node userJohn =graphDB.getNodeById(2);
            // System.out.println(userJohn.getProperties("name"));

            //获取从user John节点出发的HAS_SEEN关系
            Iterable<Relationship> allRelationShips =
                    userJohn.getRelationships(Direction.OUTGOING, MyRelationshipTypes.HAS_SEEN);

            allRelationShips.forEach(relationship ->
                    System.out.println("User has seen movie: " + relationship.getEndNode().getProperty("name"))
            );

            System.out.println(" ");
            tx.success();
        }

        /**
         * 遍历John的朋友喜欢而John还没有看过的电影
         */
        try(Transaction tx = graphDB.beginTx()){
            Node userJohn =graphDB.getNodeById(2);

            //遍历出John的朋友看过的电影
            Set<Node> moviesFriendsLike = new HashSet<>();

            userJohn.getRelationships(MyRelationshipTypes.IS_FRIEND_OF).forEach(friendRelation -> {
                //获取该关系上的除指定节点外的其他节点
                Node friend = friendRelation.getOtherNode(userJohn);
                friend.getRelationships(Direction.OUTGOING, MyRelationshipTypes.HAS_SEEN).forEach(seenMovie ->
                        moviesFriendsLike.add(seenMovie.getEndNode()));
            });

            //遍历出John看过的电影
            Set<Node> moviesJohnLike = new HashSet<>();
            userJohn.getRelationships(Direction.OUTGOING, MyRelationshipTypes.HAS_SEEN)
                    .forEach(movieJohnLike -> moviesJohnLike.add(movieJohnLike.getEndNode()));
            moviesJohnLike.forEach(movie ->
                    System.out.println("John like movie: " + movie.getProperty("name"))
            );
            System.out.println("");

            //过滤John看过的电影
            moviesFriendsLike.removeAll(moviesJohnLike);
            moviesFriendsLike.forEach(movie ->
                    System.out.println("Recommend movie to John: " + movie.getProperty("name"))
            );
            System.out.println("");

            tx.success();
        }
        graphDB.shutdown();
    }


    /**
     * 使用Neo4j提供的API遍历
     * */
    public static void travelTestNeo4jApi(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        /**
         * Evaluators.atDepth(2)列出深度为2的节点，userJohn节点的深度为0
         * NODE_GLOBAL，全局相同的节点将只遍历一次
         * NODE_PATH，同一路径下，相同的节点只遍历一次
         * NODE_LEVEL，同一层级下，相同的节点只遍历一次
         */
        try(Transaction tx = graphDB.beginTx()){
            Node userJohn = graphDB.getNodeById(2);

            //可以使用扩展器来限制关系的遍历

            //构造StandardExpander，添加要扩展的关系类型
            PathExpander standardExpander = StandardExpander.DEFAULT
                    .add(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING)
                    .add(MyRelationshipTypes.IS_FRIEND_OF);

            //OrderedByTypeExpander， 特定的顺序扩展关系
            /*
            * 先输出同事看过的电影
            * PathExpander orderedByTypeExpander = new OrderedByTypeExpander()
				.add(MyRelationshipTypes.WORK_WITH)
				.add(MyRelationshipTypes.IS_FRIEND_OF)
				.add(MyRelationshipTypes.LIKES);

            * 先输出朋友看过的电影
            * PathExpander orderedByTypeExpander = new OrderedByTypeExpander()
	            .add(MyRelationshipTypes.IS_FRIEND_OF)
	            .add(MyRelationshipTypes.WORK_WITH)
	            .add(MyRelationshipTypes.LIKES);
            *
            * */


            TraversalDescription traversalMoviesFriendsLike = graphDB.traversalDescription()
                    .expand(standardExpander)       //与下面两种写法等价
                    //.relationships(MyRelationshipTypes.IS_FRIEND_OF)
                    //.relationships(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING)
                    .uniqueness(Uniqueness.NODE_PATH)
                    .evaluator(Evaluators.atDepth(2))
                    .evaluator(new CustomNodeFilteringEvaluator(userJohn))
                    //.depthFirst() //深度优先搜索
                    .breadthFirst();    //广度优先搜索

            Traverser traverser = traversalMoviesFriendsLike.traverse(userJohn);
            Iterable<Node> moviesFriendsLike = traverser.nodes();
            moviesFriendsLike.forEach(movie -> System.out.println(movie.getProperty("name")));

            //PathPrinter类在下面给出
            PathPrinter pathPrinter = new PathPrinter( "name" );
            String output = "";
            for(Path path : traverser){
                output += Paths.pathToString( path, pathPrinter );
                output += "\n";
            }
            System.out.println(output);

            tx.success();
        }
        graphDB.shutdown();
    }

}

/**
 * Label类型枚举类
 */
enum MyLabels implements Label {
    MOVIES,USERS,ADMIN
}
/**
 * 关系类型枚举类
 */
enum MyRelationshipTypes implements RelationshipType {
    IS_FRIEND_OF,
    HAS_SEEN,
    WORK_WITH,
    LIKES,
    KNOWS
}

class PathPrinter implements Paths.PathDescriptor<Path>{
    private final String nodePropertyKey;
    public PathPrinter(String nodePropertyKey){
        this.nodePropertyKey = nodePropertyKey;
    }
    @Override
    public String nodeRepresentation(Path path, Node node){
        return "(" + node.getProperty( nodePropertyKey, "" ) + ")";
    }
    @Override
    public String relationshipRepresentation(Path path, Node from, Relationship relationship){
        String prefix = "--", suffix = "--";
        if (from.equals( relationship.getEndNode())){
            prefix = "<--";
        } else {
            suffix = "-->";
        }
        return prefix + "[" + relationship.getType().name() + "]" + suffix;
    }
}


/**
 * 自定义遍历评估函数
 * */
class CustomNodeFilteringEvaluator implements Evaluator{
    private final Node userNode;

    public CustomNodeFilteringEvaluator(Node userNode) {
        this.userNode = userNode;
    }

    @Override
    public Evaluation evaluate(Path path) {
        //遍历路径中的最后一个节点，当前例子中是所有的USERS、MOVIES节点
        Node currentNode = path.endNode();
        //判断是否是MOVIES节点，如果不是，则丢弃并且继续遍历
        if(!currentNode.hasLabel(MyLabels.MOVIES)){
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        //遍历指向当前节点的HAS_SEEN关系
        for(Relationship r :
                currentNode.getRelationships(Direction.INCOMING, MyRelationshipTypes.HAS_SEEN)){
            //获取HAS_SEEN关系的源头，即USERS节点，如果节点是给定的目标节点(John)，则丢弃
            if(r.getStartNode().equals(userNode)){
                return Evaluation.EXCLUDE_AND_CONTINUE;
            }
        }
        return Evaluation.INCLUDE_AND_CONTINUE;
    }
}