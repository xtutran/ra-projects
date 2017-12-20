import happybase
import sys
import operator
import json
from fuzzywuzzy import fuzz, process
import re
from dateutil import parser
import time
from neo4jrestclient.client import GraphDatabase, Q



pool = happybase.ConnectionPool(size=3, host='172.17.240.52')
# pool = happybase.ConnectionPool(size=3, host='127.0.0.1')
name_sep = '_'
date_sep = '+'
col_sep = '>'
reobj = re.compile(r"Release.*", re.IGNORECASE)
db = GraphDatabase("http://172.17.240.52:7474", username="neo4j", password="abc123")
# db = GraphDatabase("http://localhost:7474", username="neo4j", password="abc123")


def read_global_schema(schema_file):
    schema = {}
    try:
        with open(schema_file, "r") as f:
            i = 1
            for line in f.readlines():
                for attr in line.rstrip('\n').split(","):
                    schema[attr.lower()] = i
                i += 1
    except (IOError, OSError) as e:
        print "File not found!"
    return schema


def date_extract(val):
    val = re.sub(reobj, "", val)
    try:
        struct_time = time.strptime(val, "%d %b %Y")
        return parser.parse(val).strftime("%m-%Y")
    except ValueError:
        try:
            struct_time = time.strptime(val, "%Y, %B")
            return parser.parse(val).strftime("%m-%Y")
        except ValueError:
            return val.strip()


def get_phone(connection, table_name, row_key):
    res = {}
    exit_code = 0
    try:
        table = connection.table(table_name)
        for key, data in table.row(row_key).items():
            tokens = key.decode('utf-8').split(col_sep)
            if len(tokens) >= 2:
                level1 = tokens[0].replace('cf:', '').replace(name_sep, ' ')
                level2 = tokens[1].replace(name_sep, ' ')
                level3 = ''
                if level1 not in res:
                    res[level1] = {}
                if level2 not in res[level1]:
                    res[level1][level2] = {}
                if len(tokens) != 2:
                    level3 = tokens[2].replace(name_sep, ' ')

                res[level1][level2][level3] = date_extract(data).split("<br />")
    except:
        exit_code = 1
    return res, exit_code
    # return sorted(res.items(), key=operator.itemgetter(0)), exit_code


def get_phone_models(connection, table_name, row_prefix='', limit=10):
    exit_code = 0
    phones = {}
    try:
        table = connection.table(table_name)
        if len(row_prefix) == 0:
            rows = table.scan(limit=limit)
        else:
            rows = table.scan(row_prefix=row_prefix, limit=limit)
        for key, value in rows:
            phones[key.decode('utf-8')] = value['cf:PhoneName']
    except:
        exit_code = 1

    return phones, exit_code


def schema_extract(connection, table_name):

    column_stats = {}
    schema = {}
    rows = []

    table = connection.table(table_name)
    for key, data in table.scan():
        rows.append(key.decode('utf-8').replace(date_sep, ' '))
        for column in data.keys():
            if column not in column_stats:
                column_stats[column] = 1
            else:
                column_stats[column] += 1

    for k in column_stats.keys():
        tokens = k.split(col_sep)
        if len(tokens) < 2:
            continue

        if tokens[0] not in schema:
            schema[tokens[0]] = {}
            schema[tokens[0]][tokens[1]] = 1
        else:
            schema[tokens[0]][tokens[1]] = 1

    return schema, rows


def schema_matching(schema1, schema2):
    global_schema = {}
    choices = attribute_extract(schema2)
    print str(choices)

    for k, v in schema1.items():
        global_schema[k] = {}
        for a in v.keys():
            global_schema[k][a] = process.extract(a, choices, limit=2)

    return global_schema


def combine(phone):
    result = []
    for k, v in phone:
        result.append(k + ": " + v)
    return result


def matching(key, choices):
    return process.extractOne(key, choices)


def attribute_extract(schema):
    result = []
    for k, v in schema.items():
        result += v.keys()
    return result


def main():
    reload(sys)
    sys.setdefaultencoding("utf-8")

    sourcedb = db.labels.create("Sources")
    phonedb = db.labels.create("Phones")
    featuredb1 = db.labels.create("Features_lv1")
    featuredb2 = db.labels.create("Features_lv2")
    featuredb3 = db.labels.create("Features_lv3")
    valuedb = db.labels.create("Values")
    with pool.connection() as connection:
        for tb in ["gsmarena", "phonearena"]:
            source = db.nodes.create(name=tb)
            sourcedb.add(source)

            models, exit_code = get_phone_models(connection, tb, row_prefix="APPLE_IPHONE")
            print models
            for rowkey, name in models.items():
                phones, exit_code = get_phone(connection, tb, rowkey)

                phone = db.nodes.create(name=name)
                phonedb.add(phone)
                source.relationships.create("has", phone)

                # phone = db.labels.create(name)
                # featuredb = db.labels.create("Features_" + name)
                # subfeaturedb = db.labels.create("SubFeatures_" + name)
                # subfeaturedb = db.labels.create("SubSubFeatures_" + name)
                # valuedb = db.labels.create("Value_" + name)
                for cols, data in phones.items():
                    feature = db.nodes.create(name=cols)
                    featuredb1.add(feature)
                    phone.relationships.create("has", feature)

                    for k, v in data.items():
                        if len(k) == 0:
                            for v1 in v:
                                if len(v1) > 0:
                                    value = db.nodes.create(name=v1)
                                    valuedb.add(value)
                                    feature.relationships.create("is", value)
                        else:
                            sfeature = db.nodes.create(name=k)
                            featuredb2.add(sfeature)
                            feature.relationships.create("has", sfeature)
                            for k1, v1 in v.items():
                                # value = db.nodes.create(name=v1)
                                # valuedb.add(value)
                                if len(k1) == 0:
                                    for v2 in v1:
                                        if len(v2) > 0:
                                            value = db.nodes.create(name=v2)
                                            valuedb.add(value)
                                            sfeature.relationships.create("is", value)
                                else:
                                    ssfeature = db.nodes.create(name=k1)
                                    featuredb3.add(ssfeature)
                                    sfeature.relationships.create("has", ssfeature)
                                    for v2 in v1:
                                        if len(v2) > 0:
                                            value = db.nodes.create(name=v2)
                                            valuedb.add(value)
                                            ssfeature.relationships.create("is", value)
            # print phones


import quantities as pq
import pint
from pint import UnitRegistry, unit

if __name__ == '__main__':
    t = pq.Quantity(10, 'P')
    ureg = UnitRegistry()
    a = ureg.parse_expression('12 GHz')
    b = ureg.parse_expression('12000 MHz')

    print type(b)
    if type(b) is type(a):
        print True

    root1= a.to_root_units()
    root2= b.to_root_units()

    print root1.units

    # print (a - b).magnitude
    #
    # for u,v in ureg.Quantity._dimensionality:
    #     print u, v
    #
    # print a.ito_base_units()
    # with pool.connection() as connection:
    #     phones, exit_code = get_phone(connection, "phonearena", row_key="APPLE_IPHONE_5+201209")
    #     print json.dumps(phones)

    # global_schema = read_global_schema("rules/global_schema.txt")
    # print global_schema
    # main()
    # lookup = Q("name", istartswith="william")
    # res = db.nodes.filter(lookup)
    # for i in res:
    #     print i.