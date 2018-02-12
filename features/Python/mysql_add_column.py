import pymysql
import pandas as pd

data = pd.read_csv("E:/forum_message.csv")
filed_data = data.iloc[0:1, 1:]

# 打开数据库连接
db = pymysql.connect("localhost", "root", "******", "datacastlecom")
cursor = db.cursor()

for item in filed_data:
    item = str(item)
    item = item.replace('-', '')
    sql = "alter table forum_message add column " + item + " int not null"
    try:
        # 执行sql语句
        cursor.execute(sql)


        # 提交到数据库执行
        db.commit()

    except Exception as  e:
        # 如果发生错误则进行回滚
        print(e)
        db.rollback()

# 关闭数据库连接
db.close()
