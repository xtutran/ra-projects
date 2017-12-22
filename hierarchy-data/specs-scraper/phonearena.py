import re

import urllib
from multiprocessing import Pool

import bs4
import happybase
import pandas as pd
from bs4 import BeautifulSoup, NavigableString

import util

print bs4.__version__


cate_url = 'https://www.phonearena.com/phones/Apple-iPhone-X_id10414'
soup = BeautifulSoup(urllib.urlopen(cate_url).read(), 'html.parser')

spec_boxes = soup.find_all('div', class_='s_specs_box s_box_4')
for spec_box in spec_boxes:
    feature = spec_box.find('h2', class_='htitle')
    if not feature:
        continue

    lis = spec_box.find_all('li', class_=re.compile("^s_lv_1.*"))
    print feature.text, len(lis)

    for li in lis:
        sub_feature = li.find('strong')
        k = sub_feature.find('span', class_='s_tooltip_anchor')
        if k is None:
            k = sub_feature

        sub_sub_feature = li.find_all('li', class_=re.compile(".*clearfix$"))
        if sub_sub_feature:
            # print sub_sub_feature[0]
            prev = sub_sub_feature[0].find_previous_sibling()
            if prev is not None:
                print k.text, prev.text

            for fff in sub_sub_feature:
                k1 = fff.find('span', class_='s_tooltip_anchor')
                if k1 is None:
                    k1 = fff.find('strong')
                v1 = fff.find('li')
                print k1.text, v1.text
        else:
            next_sib = sub_feature.find_next_sibling()

            print k.text, next_sib.text

    print "##############\n"
