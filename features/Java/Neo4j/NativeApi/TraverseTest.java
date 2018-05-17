package NativeApi;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.impl.OrderedByTypeExpander;
import org.neo4j.graphdb.impl.StandardExpander;
import org.neo4j.graphdb.traversal.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraverseTest {

    //扩展关系
    /*
    * 遍历数据集
    * create(john:PERSON{name : 'John'})-[:LIKES]->(topGun:FILM{title : 'Top Gun'}),
      (john)-[:IS_FRIEND_OF]->(kate:PERSON{name : 'KATE'}),
      (john)-[:WORK_WITH]->(emma:PERSON{name : 'EMMA'}),
      (kate)-[:LIKES]->(fargo:FILM{title : 'Fargo'}),
      (kate)-[:WORK_WITH]->(alex:PERSON{name : 'Alex'}),
      (kate)-[:WORK_WITH]->(jack:PERSON{name : 'JACK'}),
      (emma)-[:WORK_WITH]->(jack),
      (emma)-[:LIKES]->(alien:FILM{title : 'Alien'}),
      (alex)-[:LIKES]->(godfather:FILM{title : 'Godfather'}),
      (jack)-[:LIKES]->(godfather),
      (jack)-[:LIKES]->(great:FILM{title : 'Great'});
    * */

    //标准扩展器
    public static void standardExpandTest(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            Node userJohn = graphDB.getNodeById(10l);
            //构造StandardExpander，添加要扩展的关系类型
            PathExpander standardExpander = StandardExpander.DEFAULT
                    .add(MyRelationshipTypes.WORK_WITH)
                    .add(MyRelationshipTypes.IS_FRIEND_OF)
                    .add(MyRelationshipTypes.LIKES);
            //在最终结果中只考虑深度2上的节点，并且只保留电影节点
            TraversalDescription traversalDescription = graphDB.traversalDescription()
                    .expand(standardExpander)
                    .evaluator(Evaluators.atDepth(2))
                    .evaluator(path -> {
                        if(path.endNode().hasProperty("title")){
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        }
                        return Evaluation.EXCLUDE_AND_CONTINUE;
                    });
            //从节点john开始遍历
            Iterable<Node> nodes = traversalDescription.traverse(userJohn).nodes();
            for(Node n : nodes){
                System.out.print(n.getProperty("title") + " -> ");
            }
            tx.success();
        }
        graphDB.shutdown();
    }

    //顺序扩展器
    public static void orderedByTypeExpanderTest(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            Node userJohn = graphDB.getNodeById(10l);
            //OrderedByTypeExpander，添加要扩展的关系类型，总是先遍历WORK_WITH关系
            PathExpander orderedByTypeExpander = new OrderedByTypeExpander()
                    .add(MyRelationshipTypes.WORK_WITH)
                    .add(MyRelationshipTypes.IS_FRIEND_OF)
                    .add(MyRelationshipTypes.LIKES);

            //OrderedByTypeExpander，添加要扩展的关系类型，总是先遍历IS_FRIEND_OF关系，只需要把IS_FRIEND_OF放在WORK_WITH前面即可
            /*
            * PathExpander orderedByTypeExpander = new OrderedByTypeExpander()
                    .add(MyRelationshipTypes.IS_FRIEND_OF)
                    .add(MyRelationshipTypes.WORK_WITH)
                    .add(MyRelationshipTypes.LIKES);
            * */

            //在最终结果中只考虑深度2上的节点，并且只保留电影节点
            TraversalDescription traversalDescription = graphDB.traversalDescription()
                    .expand(orderedByTypeExpander)
                    .evaluator(Evaluators.atDepth(2))
                    .evaluator(path -> {
                        if(path.endNode().hasProperty("title")){
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        }
                        return Evaluation.EXCLUDE_AND_CONTINUE;
                    });
            //从节点john开始遍历
            Iterable<Node> nodes = traversalDescription.traverse(userJohn).nodes();
            for(Node n : nodes){
                System.out.print(n.getProperty("title") + " -> ");
            }
            tx.success();
        }
        graphDB.shutdown();
    }

    //自定义扩展器
    public static void customExpanderTest(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            //配置深度、关系映射
            Map<Integer, List<RelationshipType>> mappings = new HashMap<>();
            mappings.put(0,
                    Arrays.asList(new RelationshipType[]{
                            MyRelationshipTypes.IS_FRIEND_OF,
                            MyRelationshipTypes.WORK_WITH})
            );
            mappings.put(1, Arrays.asList(new RelationshipType[]{MyRelationshipTypes.LIKES}));
            Node userJohn = graphDB.getNodeById(10l);
            TraversalDescription traversalDescription = graphDB.traversalDescription()
                    .expand(new DepthAwareExpander(mappings))
                    .evaluator(Evaluators.atDepth(2));
            //从节点john开始遍历
            Iterable<Node> nodes = traversalDescription.traverse(userJohn).nodes();
            for(Node n : nodes){
                System.out.print(n.getProperty("title") + " -> ");
            }
            tx.success();
        }
        graphDB.shutdown();
    }


    //管理唯一性 & 双向遍历
    /*
    * 以下数据集
    * create(jane:HUMAN{name : '1.Jane'}),
      (john:HUMAN{name : '2.John'}),
      (kate:HUMAN{name : '3.Kate'}),
      (jack:HUMAN{name : '4.Jack'}),
      (leeo:HUMAN{name : '5.Leeo'}),
      (emma:HUMAN{name : '6.Emma'}),
      (jane)-[:KNOWS]->(john),
      (jane)-[:KNOWS]->(kate),
      (john)-[:KNOWS]->(kate),
      (john)-[:KNOWS]->(jack),
      (john)-[:KNOWS]->(leeo),
      (kate)-[:KNOWS]->(emma);
    * */

    /*
    * NODE_GLOBAL意味着每个节点只能访问一次并且在遍历期间只能访问一次
      NODE_PATH意味着同一个节点在不同路径中可以多次被访问，但在同一路径中只能被访问一次
      RELATIONSHIP_GLOBAL 图中每一个关系只能别访问一次
      RELATIONSHIP_PATH 与NODE_PATH类似
      NODE_RECENT 记忆访问过的节点有一个上线
      NODE_LEVEL 确保处于同一级的节点在遍历期间只能被访问一次
      RELATIONSHIP_LEVEL 确保处于统一级的关系在遍历期间只能被访问一次
    * */


    //找出将用户1（Jane）介绍给用户5（Leeo）的直接联系人
    public static void NODE_GLOBAL_Test(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            //获取目标起始节点和目标节点
            Node jane = graphDB.getNodeById(89l);
            Node leeo = graphDB.getNodeById(93l);
            //创建KNOWS关系遍历
            TraversalDescription traversalDescription = graphDB.traversalDescription()
                    .relationships(MyRelationshipTypes.KNOWS)
                    .evaluator(path -> {
                        Node currentNode = path.endNode();
                        //当到达目标节点Leeo时，停止遍历
                        if(currentNode.getId() == leeo.getId()){
                            return Evaluation.EXCLUDE_AND_PRUNE;
                        }
                        //在当前节点和目标节点（Leeo）之间查找直接路径
                        Path singlePath = GraphAlgoFactory
                                .shortestPath(PathExpanders.forType(MyRelationshipTypes.KNOWS), 1)
                                .findSinglePath(currentNode, leeo);
                        if(singlePath != null){
                            //当前节点能直接能到达目标节点，将该节点包含在结果中并继续遍历
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        }else{
                            //当前节点不能直接达到目标节点，丢弃该节点并继续遍历
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    })
                    .uniqueness(Uniqueness.NODE_GLOBAL);
            Iterable<Node> nodes = traversalDescription.traverse(jane).nodes();
            for(Node n : nodes){
                System.out.println(n.getProperty("name"));
            }
            tx.success();
        }
        graphDB.shutdown();
    }

    //双向遍历
    public static void doubleSidedTest(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);
        try(Transaction tx = graphDB.beginTx()){
            //获取目标起始节点和目标节点
            Node jane = graphDB.getNodeById(89l);
            Node leeo = graphDB.getNodeById(93l);
            //初始化双向遍历描述BidirectionalTraversalDescription
            BidirectionalTraversalDescription description = graphDB.bidirectionalTraversalDescription()
                    //设置遍历描述的起始侧遍历出
                    .startSide(
                            graphDB.traversalDescription()
                                    .relationships(MyRelationshipTypes.KNOWS)
                                    .uniqueness(Uniqueness.NODE_PATH)
                    )
                    //设置遍历描述的结束侧节点遍历进的方向
                    .endSide(
                            graphDB.traversalDescription()
                                    .relationships(MyRelationshipTypes.KNOWS)
                                    .uniqueness(Uniqueness.NODE_PATH)
                    )
                    //设置碰撞评估函数为包含找到的所有碰撞点
                    .collisionEvaluator(path -> Evaluation.INCLUDE_AND_CONTINUE)
                    //设置侧选择器为在两个遍历方向交替变换
                    .sideSelector(SideSelectorPolicies.ALTERNATING, 100);
            PathPrinter pathPrinter = new PathPrinter( "name" );
            String output = "";
            for ( Path path : description.traverse(jane, leeo) ) {
                output += Paths.pathToString( path, pathPrinter );
                output += "\n";
            }
            System.out.println(output);
            tx.success();
        }
        graphDB.shutdown();
    }

}


/*
* 自定义扩展
* 仅仅跟踪从起始节点（John）开始的IS_FRIEND_OF和WORK_WITH关系
* 对所有在深度1访问过的节点（代表John的朋友和同事）仅仅跟踪LIKES关系
* */
class DepthAwareExpander implements PathExpander{
    //使用Map存储要跟踪的遍历深度和关系类型之间的映射
    private final Map<Integer, List<RelationshipType>> relationshipToDepthMapping;
    public DepthAwareExpander(Map<Integer, List<RelationshipType>> relationshipToDepthMapping) {
        this.relationshipToDepthMapping = relationshipToDepthMapping;
    }
    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        //查找遍历的当前深度
        int depth= path.length();
        //在当前的深度查找要跟踪的关系
        List<RelationshipType> relationshipTypes = relationshipToDepthMapping.get(depth);
        //扩展当前节点配置过的类型的所有关系
        RelationshipType[] relationshipTypeArray =  new RelationshipType[0];
        RelationshipType[] relationshipTypesArray = relationshipTypes.toArray(relationshipTypeArray);
        return path.endNode().getRelationships(relationshipTypesArray);
    }
    @Override
    public PathExpander reverse() {
        return null;
    }
}
