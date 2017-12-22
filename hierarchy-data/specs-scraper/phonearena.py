import re

import urllib
from multiprocessing import Pool

import bs4
import happybase
import pandas as pd
from bs4 import BeautifulSoup, NavigableString

import util

print bs4.__version__

# pool = happybase.ConnectionPool(size=5, host='192.168.56.101')
pool = happybase.ConnectionPool(size=3, host='192.168.1.240')
with pool.connection() as connection:
    table = connection.table('phone_specs')


def _get_real_text(strong_tag):
    tooltip = strong_tag.find('span', class_='s_tooltip_anchor')
    if tooltip is None:
        return strong_tag.get_text(strip=True)
    else:
        return tooltip.get_text(strip=True)


def scape_spec(spec_url):
    soup = BeautifulSoup(urllib.urlopen(spec_url).read(), 'html.parser')

    model_name = soup.find('div', id='phone').find('h1').find('span')
    print model_name
    if model_name is None:
        return
    model_name = util.normalize(model_name.get_text(strip=True))

    spec_boxes = soup.find_all('div', class_='s_specs_box s_box_4')
    specs = {}
    for spec_box in spec_boxes:
        feature = spec_box.find('h2', class_='htitle')
        if feature is None:
            continue

        feature_cate = util.normalize(feature.get_text(strip=True))
        spec_list = spec_box.find_all('li', class_=re.compile("^s_lv_1.*"))

        for spec in spec_list:
            sub_feature = spec.find('strong')
            feature_level1 = util.normalize(_get_real_text(sub_feature))

            sub_sub_feature = spec.find_all('li', class_=re.compile(".*clearfix$"))
            if sub_sub_feature:
                # print sub_sub_feature[0]
                prev = sub_sub_feature[0].find_previous_sibling()
                if prev is not None:
                    key = ':'.join([feature_cate, feature_level1])
                    value = prev.get_text(strip=True)
                    specs[key] = value
                for fff in sub_sub_feature:
                    feature_level2 = util.normalize(_get_real_text(fff.find('strong')))
                    key = ':'.join([feature_cate, feature_level1, feature_level2])
                    value = fff.find('li').get_text(strip=True)
                    specs[key] = value
            else:
                next_sib = sub_feature.find_next_sibling()
                key = ':'.join([feature_cate, feature_level1])
                value = next_sib.get_text(strip=True)
                specs[key] = value

    print model_name, specs
    return model_name, specs


def insert_to_hbase(row_key, column_family, specs, table_):
    data = {}
    for key, value in specs.iteritems():
        print key, value
        data[b'{0}:{1}'.format(column_family, key)] = \
            bytes(value.encode('utf-8') if isinstance(value, (str, unicode)) else str(value))
    table_.put(bytes(row_key), data)


def main():
    cate_url = 'https://www.phonearena.com/phones/Apple-iPhone-X_id10414'
    row_key, specs = scape_spec(cate_url)
    insert_to_hbase(row_key, 'phonearena', specs, table)


if __name__ == '__main__':
    main()
