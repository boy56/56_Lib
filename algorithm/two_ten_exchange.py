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
        return eval('0b'+temp)
    else:
        tempList[0] = '0'
        lenth = len(tempList) - 1
        s = ''.join(tempList)
        t = int(eval('0b' + s))
        t -= 1
        tempList = list(bin(t).replace('0b',''))
        while len(tempList) < lenth:
            tempList.insert(0, '0')
        for i in range(len(tempList)):
            if tempList[i] == '1':
                tempList[i] = '0'
            else:
                tempList[i] = '1'
        s = ''.join(tempList)
        return 0 - eval('0b' + s)
