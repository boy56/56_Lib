from pandas import DataFrame
from py2neo import Node, Relationship, Graph

# Graph代表了Neo4j图形数据库

graph = Graph(password='sjh123456')
#graph = Graph(host="localhost")
#graph = Graph("http://localhost:7474/db/data/")

# 利用creat函数可以完成图的创建, 也可以用于单个Node或Relation的添加

a = Node('Person', name='Alice')
b = Node('Person', name='Bob')
r = Relationship(a, 'KNOWS', b)
s = a | b | r
# graph.create(s)

'''
a = Node('Person', name='Alice')
graph.create(a)
b = Node('Person', name='Bob')
ab = Relationship(a, 'KNOWS', b)
graph.create(ab)
'''

# 利用 data() 方法来获取查询结果
# 通过 CQL 语句实现的查询, 输出结果即 CQL 语句的返回结果, 是列表形式

data = graph.data('MATCH (p:Person) return p')
print(data)

# 输出结果还可以直接转化为 DataFrame 对象
df = DataFrame(data)
print(df)

# 使用 find_one() 或 find() 方法进行 Node 的查找
# 利用 match() 或 match_one() 方法对 Relationship 进行查找
node = graph.find_one(label='Person')
print(node)
relationship = graph.match_one(rel_type='KNOWS')
print(relationship)

# 想要更新 Node 的某个属性可以使用 push() 方法
a['age'] = 21
graph.push(a)

# 想要删除某个节点可以使用delete()方法
# 在删除 Node 时必须先删除其对应的 Relationship, 否则无法删除 Node
node = graph.find_one(label='Employee')
relationship = graph.match_one(rel_type='WORK_FOR')

# 对于删除Relations操作
# 单独删除Relation而不涉及两边Node使用separate函数
# 同时删除Relation及其两边相连的Node使用delete函数
graph.separate(relationship)
graph.delete(node)

# 可以通过 run() 方法直接执行 CQL 语句, 返回结果为一个cursors实例
data = graph.run('MATCH (p:Person) RETURN p LIMIT 5')
print(list(data))