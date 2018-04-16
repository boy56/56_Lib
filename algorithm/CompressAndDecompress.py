import random
import numpy as np
import pandas as  pd


def ten_to_two(num):
    """
    十进制转二进制
    :param num: int类型
    :return: string类型,第一位为符号位
    """

    if num > 0:
        return '0' + bin(num).replace('0b', '')
    elif num == 0:
        return '0'
    else:
        temp = bin(num).replace('-0b', '')
        tempList = list(temp)
        lenth = len(tempList)
        for i in range(lenth):
            if tempList[i] == '1':
                tempList[i] = '0'
            else:
                tempList[i] = '1'
        s = ''.join(tempList)
        t = int(eval('0b' + s))
        t += 1
        tempList = list(bin(t).replace('0b', ''))
        while len(tempList) < lenth:
            tempList.insert(0, '0')
        return '1' + ''.join(tempList)


def two_to_ten(num):
    """
    二进制转十进制,支持负数，第一位为符号位
    :param num: string类型, 第一位为符号位
    :return: int 类型
    """

    temp = num
    tempList = list(temp)
    if tempList[0] == '0':
        return eval('0b' + temp)
    else:
        tempList[0] = '0'
        lenth = len(tempList) - 1
        s = ''.join(tempList)
        t = int(eval('0b' + s))
        t -= 1
        tempList = list(bin(t).replace('0b', ''))
        while len(tempList) < lenth:
            tempList.insert(0, '0')
        for i in range(len(tempList)):
            if tempList[i] == '1':
                tempList[i] = '0'
            else:
                tempList[i] = '1'
        s = ''.join(tempList)
        return 0 - eval('0b' + s)


def change_data_to_tuple(tupleSize, data):
    """
    处理转换成二进制的字符串,向其中增加标志位以及补足对其(本实验为3为一组，一个数据包4位,首位为1代表一个数的第一个包,直到下一个首位为1的包，
    取两者之间数据包的后三位拼接起来则为补齐的二进制补码形式)
    :param tupleSize: int类型，为元组tuple的大小
    :param data: 字符串类型，为二进制
    :return: 字符串类型，处理好的一串元组
    """
    # 需要补齐的位数
    lenth = (tupleSize - 1) - len(data) % (tupleSize - 1)

    dataList = list(data)
    # data的首位, 0或者1
    firstEle = dataList[0]
    # 标志位插入的位置下标
    insertLoc = 0
    for i in range(lenth):
        dataList.insert(0, firstEle)
    if len(dataList) % (tupleSize - 1) != 0:
        print("error in change_data_to_tuple: the dataList is not the multi of (tupleSize-1)")

    tupleNums = int(len(dataList) / (tupleSize - 1))

    for i in range(tupleNums):
        if i == 0:
            dataList.insert(insertLoc, '1')
        else:
            dataList.insert(insertLoc, '0')
        insertLoc += tupleSize

    if len(dataList) % tupleSize != 0:
        print("error in change_data_to_tuple: the insert flag may be wrong")

    return ''.join(dataList)


def compress_data(data, type, tupleSize):
    """
    压缩算法
    :param data: list类型， 目标数据
    :param type: int 类型, 表明为一次差值还是两次差值(0代表一次差值,1代表两次差值)
    :param tupleSize: int 类型, 为压缩时有标志位的小数据包的大小
    :return: 字符串类型，为已经压缩完的二进制序列
    """
    if type == 0:
        # Data1 = pd.read_csv("time_deal1.csv")
        # time_data1 = Data1.iloc[:, 0]
        data1_sum = 64
        data1TwoList = []
        for item in data:
            temp = ten_to_two(item)
            temp = change_data_to_tuple(tupleSize, temp)
            data1TwoList.append(temp)
            data1_sum += len(temp)
        print(data1_sum)
        return ''.join(data1TwoList)
    elif type == 1:
        # 对第二种差值的压缩
        # Data2 = pd.read_csv("time_deal2.csv")
        # time_data2 = Data2.iloc[:, 0]
        data2_sum = 64 + 10
        data2TwoList = []
        for item in data:
            temp = ten_to_two(item)
            temp = change_data_to_tuple(tupleSize, temp)
            data2TwoList.append(temp)
            data2_sum += len(temp)
        print(data2_sum)
        return ''.join(data2TwoList)
    else:
        print("error in compress_data, the type is not 0 or 1")


def change_tuple_to_data(tupleSize, str):
    """
    :param tupleSize: int类型，为元组tuple的大小
    :param str: 字符串类型，为二进制
    :return: 字符串类型，处理好的一串元组
    """


def de_compress_data(data, type, tupleSize):
    """
    :param data: 字符串类型， 目标数据
    :param type: int 类型, 表明为一次差值还是两次差值(0代表一次差值,1代表两次差值)
    :param tupleSize: int 类型, 为压缩时有标志位的小数据包的大小
    :return: list类型， 十进制的列表
    """
    dataList = list(data)
    tempList = []
    resultList = []
    i = 0
    while i < len(dataList):
        if i != 0 and dataList[i] == '1':
            temp = ''.join(tempList)
            resultList.append(two_to_ten(temp))
            tempList[:] = []
            for j in range(1, tupleSize):  # 已经清楚了标志位的
                tempList.append(dataList[i + j])
        else:
            for j in range(1, tupleSize):   # 已经清楚了标志位的
                tempList.append(dataList[i+j])
        i += tupleSize
    # 最后一个数据
    temp = ''.join(tempList)
    resultList.append(two_to_ten(temp))
    return resultList


def init_environment():
    """
    生成实验数据
    :return:
    """
    init = 1388505600000
    init_temp = init
    timeList = []
    diff = []

    for i in range(90000):
        diff.append(random.randint(1, 7))

    for i in range(7000):
        diff.append(random.randint(8, 20))

    for i in range(2000):
        diff.append(random.randint(21, 100))

    for i in range(1000):
        diff.append(random.randint(101, 1000))

    random.shuffle(diff)

    for i in diff:
        init_temp = init_temp + i
        timeList.append(init_temp)

    np.savetxt('time1.csv', timeList,
               delimiter=',', header='time', comments='', fmt='%ld')


def cal_diff(average):
    """
    用来计算差值和差值的差值,并分别记录在time_deal1.csv 和 time_deal2.csv中
    :param average:int 类型, 用来设置第二张表(差值的差值)的基准值  例如: average 为3 , 代表两者相差基准为3，表中+1代表
                差值为4
    :return:NONE
    """
    data = pd.read_csv("time1.csv")
    time_data = data.iloc[:, 0]
    disList1 = []
    disList2 = []
    for i in range(1, 100000):
        disList1.append(time_data[i] - time_data[i - 1])

    # 二次差值部分
    for item in disList1:
        disList2.append(item - average)

    np.savetxt('time_deal1.csv', disList1,
               delimiter=',', header='time', comments='', fmt='%d')
    np.savetxt('time_deal2.csv', disList2,
               delimiter=',', header='time', comments='', fmt='%d')


# init_environment()
# cal_diff(4)


# 一次差值的实验
Data1 = pd.read_csv("time_deal1.csv")
time_data1 = Data1.iloc[:, 0]
result1 = compress_data(time_data1, 0, 4)
deResult1 = de_compress_data(result1, 0, 4)
np.savetxt('time_deal1_deCompress.csv',deResult1 ,
           delimiter=',', header='time', comments='', fmt='%d')


# 二次差值的实验
Data2 = pd.read_csv("time_deal2.csv")
time_data2 = Data2.iloc[:, 0]
result2 = compress_data(time_data2, 1, 4)
deResult2 = de_compress_data(result2, 0, 4)
np.savetxt('time_deal2_deCompress.csv',deResult2,
           delimiter=',', header='time', comments='', fmt='%d')

