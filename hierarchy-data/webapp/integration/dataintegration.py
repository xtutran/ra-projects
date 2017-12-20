import happybase
import sys
import operator
import json
import re
from dateutil import parser
from date_extractor import extract_dates
import time
from pint import UnitRegistry, UndefinedUnitError, DimensionalityError

pool = happybase.ConnectionPool(size=3, host='172.17.240.52')

regex_2D = r"\b[0-9]+(\.[0-9]+)?\b[\s]*x[\s]*\b[0-9]+(\.[0-9]+)?\b[\s]*([\w]+)"
regex_3D = r"\b[0-9]+(\.[0-9]+)?\b[\s]*x[\s]*\b[0-9]+(\.[0-9]+)?\b[\s]*x[\s]*\b[0-9]+(\.[0-9]+)?\b[\s]*([\w]+)"
regex_number = r"\b[0-9]+(\.[0-9]+)?\b[\s]+[\w%]+[\/]*[\w]*"
skip_cols = ["cf:HTML", "cf:PhoneName", "cf:Phone_name", "cf:URL"]

type_2D = '2D'
type_3D = '3D'
type_number = 'number'
type_text = 'text'
type_date = 'date'

ureg = UnitRegistry()
ureg.load_definitions('custom_units')


def parse(regex, text, dtype):
    res = {dtype: []}
    for match in re.finditer(regex, text):
        res[dtype].append(match.group())
    return res


def extractor(text):
    date_text = extract_dates(text)

    if date_text:
        return {type_date: date_text}
    elif re.search(regex_3D, text):
        return parse(regex_3D, text, type_2D)
    elif re.search(regex_2D, text):
        return parse(regex_2D, text, type_3D)
    elif re.search(regex_number, text):

        numbers = []
        texts = []
        for number in parse(regex_number, text, type_number).get(type_number):
            try:
                numbers.append(ureg.parse_expression(number))
            except UndefinedUnitError:
                texts.append(number)
        if len(numbers) > 0:
            return {type_number: numbers}
        else:
            return {type_text: texts}
    else:
        return {type_text: re.split(r"[,;]", text)}


def get_data(conn, table, row_key):
    res = {}
    tb = conn.table(table)
    for key, value in tb.row(row_key).items():
        res[key.replace('cf:', '')] = extractor(value)
    return res


def compare(query, query_type, data):
    for key, values in data.items():
        if query_type in values:
            for value in values.get(query_type):
                if query_type == type_number:
                    try:
                        root1= query.to_root_units()
                        root2= value.to_root_units()

                        print root1, root2
                        # if root1.units == root2.units and root1.magnitude == root2.magnitude:
                        #     print query, value
                    #     elif round(abs((root1 - root2).magnitude), 1) == 0:
                    #         print query, value
                    # except AttributeError:
                    #     if type(query) == float and type(value) == float and query == value:
                    #         print query, value
                    except UndefinedUnitError:
                        if type(query) is type(value):
                            if type(query) is float and  query == value:
                                print query, value
                            elif query.units == value.units and query.magnitude == value.magnitude:
                                print query, value
                    except Exception:
                        print 'error'


def data_matching(data1, data2):
    for key, values in data1.items():
        query_type = values.keys()[0]
        for value in values.get(query_type):
            print value
            # compare(value, query_type, data2)
    return ''


if __name__ == '__main__':
    print ''

    row = "APPLE_IPHONE_6+201409"
    phonearena = "phonearena"
    gsmarena = "gsmarena"
    with pool.connection() as connection:
        gsm = get_data(connection, gsmarena, row)
        phonea = get_data(connection, phonearena, row)
        data_matching(gsm, phonea)