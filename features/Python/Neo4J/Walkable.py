import py2neo

# Walkable 是增加了遍历信息的 Subgraph，我们通过 + 号便可以构建一个 Walkable 对象
a = py2neo.Node('Person', 'Admin', name='Mike')
b = py2neo.Node('Person', name='Bob')
c = py2neo.Node('Person', name='Alice')
ab = py2neo.Relationship(a, "KNOWS", b)
ac = py2neo.Relationship(a, "KNOWS", c)
w = ab + py2neo.Relationship(b, 'LIKE', c) + ac
print(w)

print("=====================")
# 通过walk()方法实现遍历
for item in py2neo.walk(w):
    print(item)


print("=====================")
# 利用 start_node()、end_node()、nodes()、relationships() 方法
# 来获取起始 Node、终止 Node、所有 Node 和 Relationship
print(w.start_node())
print(w.end_node())
print(w.nodes())
print(w.relationships())

