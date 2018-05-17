package NativeApi;


import org.apache.lucene.index.ReaderSlice;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.io.File;

/**
 * 索引笔记
 * */
public class IndexTest {
    public static void creatIndex(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        try(Transaction tx = graphDB.beginTx()){
            String johnEmail = "john@example.org";
            String kateEmail = "kage@example.org";
            String jackEmail = "jack@example.org";

            //获取用户节点，并且设置email属性
            Node userJohn = graphDB.getNodeById(2l);
            userJohn.setProperty("email", johnEmail);
            System.out.println(userJohn.getProperties("name"));

            Node userKate = graphDB.getNodeById(3l);
            userKate.setProperty("email", kateEmail);
            System.out.println(userKate.getProperties("name"));

            Node userJack = graphDB.getNodeById(4l);
            userJack.setProperty("email", jackEmail);
            System.out.println(userJack.getProperties("name"));

            //获取索引管理器
            IndexManager indexManager = graphDB.index();

            //查找名称为users的索引，若不存在则创建一个
            Index<Node> userIndex =indexManager.forNodes("users");

            //以email为key，为users索引添加具体的索引项目(email)
            userIndex.add(userJohn, "email", johnEmail);
            userIndex.add(userKate, "email", kateEmail);
            userIndex.add(userJack, "email", jackEmail);

            //获取索引命中的结果集
            IndexHits<Node> indexHits = userIndex.get("email", "john@example.org");

            /**
             * 获取命中的节点，且要求命中节点只有一个，如果有多个则抛出NoSuchElementException("More than one element in...")
             * 若索引命中的结果集中不只一条时，遍历indexHits即可
             * for(Node user : indexHits){
             *     System.out.println(user.getProperty("name"));
             * }
             */
            Node loggedOnUserNode = indexHits.getSingle();
            if(loggedOnUserNode != null){
                System.out.println(loggedOnUserNode.getProperty("name"));
            }

            tx.success();
        }
        graphDB.shutdown();
    }

    public static void updateIndex(String s){
        /**
         * 如果此时用户需要修改email，原索引就失效了。同时Neo4j并不会自动地修改索引，
         * 而且Index<Node>接口也没有提供修改索引的方法，所以解决的方法就是将原索引先删除，然后再添加新的索引。
         */
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        try(Transaction tx = graphDB.beginTx()) {
            String johnEmail = "john@example.org";
            String updateJohnEmail = "john@new.example.org";

            //获取索引管理器
            IndexManager indexManager = graphDB.index();

            //查找名称为users的索引
            Index<Node> userIndex = indexManager.forNodes("users");

            //获取索引命中的结果集
            IndexHits<Node> indexHits = userIndex.get("email",johnEmail);
            Node loggedOnUserNode = indexHits.getSingle();
            if(loggedOnUserNode != null){
                //删除索引
                userIndex.remove(loggedOnUserNode,"email",johnEmail);
                //更新用户邮件地址
                loggedOnUserNode.setProperty("email",updateJohnEmail);
                //新增索引
                userIndex.add(loggedOnUserNode,"email",updateJohnEmail);
            }

            tx.success();
        }
        graphDB.shutdown();
    }

    public static void schemaIndex(String s){
        File file = new File(s);
        GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(file);

        //声明将要使用的标签
        Label movieLabel = MyLabels.MOVIES;
        Label userLabel = MyLabels.USERS;

        Node movie, user;
        try(Transaction tx = graphDB.beginTx()){
            //创建电影名字属性索引
            graphDB.schema().indexFor(movieLabel).on("name").create();

            //创建用户名字属性索引
            graphDB.schema().indexFor(userLabel).on("name").create();
            tx.success();
        }

        try(Transaction tx = graphDB.beginTx()){
            //创建新的MOVIES节点,并设置name属性值
            movie = graphDB.createNode(movieLabel);
            movie.setProperty("name","Michael Collins");

            //创建新的USERS节点,并设置name属性值
            user = graphDB.createNode(userLabel);
            user.setProperty("name","Michael Collins");

            tx.success();
        }

        //验证结果
        try(Transaction tx = graphDB.beginTx()){
            //通过名字索引查找电影
            ResourceIterator<Node> result = graphDB.findNodes(movieLabel,"name","Michael Collins");
            result.forEachRemaining(node->
                System.out.println(node.getId() + "->" + node.getLabels()));
            tx.success();
        }


        //创建管理员索引

        Label adminLabel = MyLabels.ADMIN;
        try(Transaction tx = graphDB.beginTx()){
            //创建管理员属性索引
            graphDB.schema().indexFor(adminLabel).on("name").create();
            tx.success();
        }

        try(Transaction tx = graphDB.beginTx()){
            //创建同时为USERS和ADMIN标签类型的节点
            Node adminUser = graphDB.createNode(userLabel,adminLabel);
            adminUser.setProperty("name", "Peter Smith");
            tx.success();
        }

        //验证结果
        try(Transaction tx = graphDB.beginTx()){
            ResourceIterator<Node> adminSearch = graphDB.findNodes(adminLabel, "name", "Peter Smith");
            adminSearch.forEachRemaining(node -> System.out.println(node.getId()+ " -> " + node.getLabels()));

            ResourceIterator<Node> userSearch = graphDB.findNodes(userLabel, "name", "Peter Smith");
            userSearch.forEachRemaining(node -> System.out.println(node.getId()+ " -> " + node.getLabels()));
            tx.success();
        }

        graphDB.shutdown();

    }
}
