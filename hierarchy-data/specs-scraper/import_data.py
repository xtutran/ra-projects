#!/usr/bin/python2.7

import datetime
import json
import uuid
import xml.etree.ElementTree as ET
from collections import OrderedDict
import os

import pandas as pd
import argparse
import xmltodict
from cassandra.cluster import Cluster, BatchStatement


def unix_time(dt):
    epoch = datetime.datetime.utcfromtimestamp(0)
    delta = dt - epoch
    return delta.total_seconds()


def unix_time_millis(dt):
    return int(unix_time(dt) * 1000.0)


def generate_stmt(session, table, columns):
    col_exp = ', '.join(columns)
    val_exp = ', '.join(['?' for _ in range(len(columns))])
    stmt = """INSERT INTO {table} ({columns}) VALUES({values})""".format(table=table, columns=col_exp, values=val_exp)
    return session.prepare(stmt)


def parse_meta_data(raw_meta_data):
    tree = ET.parse(raw_meta_data)  # element tree
    root = tree.getroot()
    meta_data = OrderedDict()
    mess_cols = []
    json_data = []
    for i, child in enumerate(root):
        tag = child.tag
        if tag == 'KOPF' or tag == 'ZEIT':
            for subchild in child:
                value = subchild.text
                if subchild.tag in ['ANFANG', 'ENDE']:
                    value = unix_time_millis(datetime.datetime.strptime(value, '%Y-%m-%d %H:%M:%S'))
                meta_data[subchild.tag] = value
        elif tag == 'DATEN':
            for subchild in child:
                if subchild.tag == 'SPALTE':
                    col = subchild.text
                    index = subchild.attrib.get('messstelle', '0')
                    mess_cols.append('.'.join([col, index]))
        else:
            json_data.append(xmltodict.parse(ET.tostring(child, encoding='utf-8', method='xml')))
    meta_data['json'] = json.dumps(json_data, ensure_ascii=False, separators=(';', ':')).replace(',', '.').replace(';', ',')
    return meta_data, mess_cols


def column_to_rows(df, column, sep):
    
    def _duplicate(new_row, value):
        copy = new_row.copy()
        copy[column] = value
        return copy
    
    def _cell_to_rows(row, new_rows):
        split_row = row[column].split(sep)
        new_row = row.to_dict()
        new_rows += [_duplicate(new_row, value) for value in split_row]
    new_rows = []
    df.apply(lambda row: _cell_to_rows(row, new_rows), axis=1)
    new_df = pd.DataFrame(new_rows)
    return new_df


def parse_mess_data(mess_file, mess_cols, meta_data, sep='\t'):
    data = pd.read_csv(mess_file, sep=sep, header=0, names=mess_cols)
    data = data.rename(columns={'LAENGE.1': 'LAENGE'})
    data = data.melt(id_vars=['LAENGE'], var_name='VARIABLE', value_name='VAL')
    data.LAENGE = data.LAENGE.str.replace(',', '.')
    data.VAL = data.VAL.str.replace(',', '.')

    data = column_to_rows(data, 'VAL', ';')
    data.LAENGE = pd.to_numeric(data.LAENGE, errors='coerce')
    data.VAL = pd.to_numeric(data.VAL, errors='coerce')

    split = data.VARIABLE.str.rsplit('.')
    data['VARIABLE'] = split.str.get(0)
    data['MESSSTELLE'] = split.str.get(1).astype(np.int8)
    data['PRNR'] = meta_data['PRNR']
    return data


def valid_path(s):
    if not os.path.isfile(s):
        raise argparse.ArgumentTypeError("Not exist path: {0}.".format(s))
    return s


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--meta",
                        type=valid_path,
                        help="Path to meta XML data, main.xml")
    parser.add_argument("--mess",
                        type=valid_path,
                        help="Path to measurement data, mess.txt")
    parser.add_argument("--db",
                        type=str,
                        help="Casandra Database",
                        default="tran_upwork")
    parser.add_argument("--meta-table",
                        type=str,
                        help="Casandra Meta Table",
                        default='meta')
    parser.add_argument("--mess-table",
                        type=str,
                        help="Casandra Mess Table",
                        default="mess")

    arguments, _ = parser.parse_known_args()
    print(arguments)

    meta_data_path = arguments.meta
    mess_path = arguments.mess

    cluster = Cluster()
    session = cluster.connect(arguments.db)

    meta_data, mess_cols = parse_meta_data(meta_data_path)

    meta_insert_stmt = generate_stmt(session, table=arguments.meta_table, columns=meta_data.keys())
    session.execute_async(meta_insert_stmt, meta_data.values())

    mess_data = parse_mess_data(mess_path, mess_cols, meta_data)
    mess_insert_stmt = generate_stmt(session, table=arguments.mess_table, columns=mess_data.columns.tolist())
    batch = BatchStatement(consistency_level=ConsistencyLevel.QUORUM)
    def _insert(row):
        batch.add(mess_insert_stmt, tuple(row.values))

    mess_data.apply(_insert, axis=1)
    session.execute(batch)


if __name__ == '__main__':
    main()
