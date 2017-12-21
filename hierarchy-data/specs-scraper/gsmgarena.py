import urllib

import happybase
import pandas as pd
from bs4 import BeautifulSoup

import helper


def extract_specs(spec_url):
    soup = BeautifulSoup(urllib.urlopen(spec_url).read(), 'html.parser')
    model_name = soup.find('h1', class_='specs-phone-name-title')
    specs_html = soup.find('div', id='specs-list')

    specs = {}
    i = 1
    for table in specs_html.find_all('table'):
        feature = table.find('th')
        if not feature:
            continue

        if table.find('tr', class_='tr-hover'):
            del_rows = []
            for f in table.find_all('td', class_='ttl'):
                if f.text == u'\xa0':
                    curr_row = f.parent
                    prev_row = curr_row.find_previous_sibling()
                    while True:
                        prev_row_key = prev_row.find('td', class_='ttl')
                        prev_row_value = prev_row.find('td', class_='nfo')
                        if prev_row_key.text == u'\xa0':
                            prev_row = prev_row_key.parent.find_previous_sibling()
                        else:
                            prev_row_value.insert(1, '^^^')
                            prev_row_value.insert(2, f.find_next_sibling().text)
                            break
                    del_rows.append(curr_row)

            [del_row.decompose() for del_row in del_rows]

        feature_key = helper.normalize(feature.text)
        feature.decompose()
        if feature_key in specs:
            feature_key = '{0}_{1}'.format(feature_key, i)
            i += 1

        specs[feature_key] = table

    return helper.normalize(model_name.text), specs


def insert_to_hbase(row_key, column_family, specs, batch):
    for feature_key, data_html in specs.iteritems():
        data = pd.read_html(data_html.prettify())[0]

        data_row = {}
        for index, row in data.iterrows():
            key = b'{cf}:{feature}:{key}'.format(cf=column_family, feature=feature_key, key=helper.normalize(row[0]))
            value = b'{value}'.format(value=row[1])
            data_row[key] = value

        batch.put(bytes(row_key), data_row)


def main():
    # pool = happybase.ConnectionPool(size=3, host='192.168.1.240')
    pool = happybase.ConnectionPool(size=3, host='192.168.56.101')

    with pool.connection() as connection:
        table = connection.table('phone_specs')

    with table.batch(transaction=True) as b:
        base_url = 'https://www.gsmarena.com/acer_iconia_talk_s-8306.php'
        row_key, specs = extract_specs(base_url)
        insert_to_hbase(row_key, 'gsmgarena', specs, b)


if __name__ == '__main__':
    main()
