# Graph 有时候用起来不太方便,比如如果要根据多个条件进行 Node 的查询是做不到的,
# 在这里更方便的查询方法是利用 NodeSelector

from py2neo import Graph, Node, Relationship, NodeSelector

graph = Graph(password='sjh123456')

# 新建节点

'''
a = Node('Person', name='Alice', age=21, location='广州')
b = Node('Person', name='Bob', age=22, location='上海')
c = Node('Person', name='Mike', age=21, location='北京')
r1 = Relationship(a, 'KNOWS', b)
r2 = Relationship(b, 'KNOWS', c)
graph.create(a)
graph.create(r1)
graph.create(r2)
'''
selector = NodeSelector(graph)
persons = selector.select('Person', age=21)
print(list(persons))

# 也可以用where()进行更复杂的查询
persons = selector.select('Person').where('_.name =~ "A.*"')    # 使用了正则匹配
print(list(persons))

# 使用order_by()进行排序
persons = selector.select('Person').order_by('_.age')
print(list(persons))

# 查询单个节点可以用 first()方法
person = selector.select('Person').where('_.name =~ "A.*"').first()
print(person)