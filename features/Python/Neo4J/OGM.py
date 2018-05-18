# 实现一个对象和Node的关联
from py2neo.ogm import GraphObject, Property, RelatedTo, RelatedFrom
from py2neo import Graph


class Movie(GraphObject):
    __primarykey__ = 'title'

    title = Property()
    released = Property()
    actors = RelatedFrom('Person', 'ACTED_IN')
    directors = RelatedFrom('Person', 'DIRECTED')
    producers = RelatedFrom('Person', 'PRODUCED')


class Person(GraphObject):
    __primarykey__ = 'name'

    name = Property()
    age = Property()
    location = Property()
    born = Property()
    acted_in = RelatedTo('Movie')
    directed = RelatedTo('Movie')
    produced = RelatedTo('Movie')
    knows  =RelatedTo('Person', 'KNOWS')


if __name__ == '__main__':
    graph = Graph(password='sjh123456')
    # 完成Node到类对象的映射
    person = Person.select(graph).where(age=21).first()
    print(person.__ogm__.node)
    # 修改node属性
    person.age = 22
    print(person.__ogm__.node)
    # 提交更改
    graph.push(person)

    # 通过映射关系进行Relationship的调整
    print(list(person.knows))
    # 添加一个关联Node
    new_person = Person()
    new_person.name = 'Durant'
    new_person.age = 28
    person.knows.add(new_person)
    print(list(person.knows))

    # 提交更改
    graph.push(person)

    # 通过remove()方法移除某个关联Node
    target = Person.select(graph).where(name='Durant').first()
    person.knows.remove(target)
    graph.push(person)