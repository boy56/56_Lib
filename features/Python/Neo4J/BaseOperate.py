from py2neo import Node, Relationship, order, size


# 创建实体和关系
a = Node('Person', name='Alice')
b = Node('Person', name='Bob')
r = Relationship(a, 'KNOWS', b)
print(a, b, r)

# 利用字典对属性赋值
a['age'] = 20
b['age'] = 21
r['time'] = '2018/05/16'
print(a, b, r)

# 利用字典对属性进行批量更新
a_data = {
    'name': 'Amy',
    'age': 21
}
a.update(a_data)
print(a)

# 构建子图(subgraph)
s = a | b | r
print(s)

# 获取所有的Node和Relations
print(s.nodes())    # 返回结果为frozenset类型
print(s.relationships())

# 利用 & 取subgraph的交集
s2 = a | b
print(s & s2)


# 利用keys(), labels(), nodes(), relations(), types 分别获取Subgraph
# 的Key, Label, Node, Relationship, Relationship Type

print(s.keys())
print(s.labels())
print(s.keys())
print(s.relationships())
print(s.types())

# 利用order() 或 size() 方法来获取Subgraph 的Node数量和Relation数量
print(order(s))  # Node数量
print(size(s))  # Relation数量


