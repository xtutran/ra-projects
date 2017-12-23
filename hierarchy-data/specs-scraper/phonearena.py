import re
import sys
import os
import time
import urllib2
import urllib
import httplib
from multiprocessing import Pool

import bs4
import happybase
import pandas as pd
from bs4 import BeautifulSoup, NavigableString

import util

print bs4.__version__

source = 'phonearena'
# pool = happybase.ConnectionPool(size=5, host='192.168.56.101')
pool = happybase.ConnectionPool(size=3, host='192.168.1.240')
with pool.connection() as connection:
    table = connection.table('phone_specs')


def append_log(message):
    print message


def get_web_page(address):
    try:
        user_agent = 'Mozilla/4.0 (compatible; MSIE 5.5; Windows NT)'
        headers = {'User-Agent' : user_agent}
        request = urllib2.Request(address, None, headers)
        response = urllib2.urlopen(request, timeout=20)
        try:
            return response.read()
        finally:
            response.close()
    except urllib2.HTTPError as e:
        error_desc = httplib.responses.get(e.code, '')
        append_log('HTTP Error: ' + str(e.code) + ': ' + error_desc + ': ' + address)
    except urllib2.URLError as e:
        append_log('URL Error: {}: {}'.format(e.reason, address))
    except Exception as e:
        append_log('Unknown Error: ' + str(e) + address)
    return ''


def _extract_spec_url(page_url):
    # time.sleep(1)
    soup = BeautifulSoup(get_web_page(page_url), 'html.parser')

    phones = soup.find('div', id='phones')
    if phones is None:
        return []

    a_tags = phones.find_all('a', class_='s_thumb')
    if a_tags is None or not a_tags:
        return []

    return [[page_url, 'https://www.phonearena.com' + a_tag.attrs['href']] for a_tag in a_tags]


def extract_all_spec_url(base_url):
    soup = BeautifulSoup(urllib2.urlopen(base_url).read(), 'html.parser')
    s_listing = soup.find('div', class_='s_listing')
    brands = s_listing.find_all('a', class_='ahover')
    for brand in brands:
        brand_name = brand.get_text(strip=True)
        brand_url = 'https://www.phonearena.com' + brand.attrs['href']
        print brand_name, brand_url

        url_db = []
        url_db_path = os.path.join('data', source, util.normalize(brand_name))

        if os.path.isfile(url_db_path):
            continue

        paging_url = '{url}/page/{index}'
        j = 0
        for i in range(1, 1000):
            page_url = paging_url.format(url=brand_url, index=i)
            print page_url

            if j != 0 and j % 5 == 0:
                print('Print: {}'.format(j))
                time.sleep(j/5)
            # elif j != 0 and j % 30 == 0:
            #     time.sleep(5)
            #     j = 5
            spec_urls = _extract_spec_url(page_url)
            if len(spec_urls) == 0:
                break
            url_db.extend(spec_urls)
            j += 1
        url_db = pd.DataFrame(url_db, columns=['cate_url', 'spec_url'])
        url_db.to_csv(url_db_path, index=False)


def _get_real_text(strong_tag):
    tooltip = strong_tag.find('span', class_='s_tooltip_anchor')
    if tooltip is None:
        return strong_tag.get_text(strip=True)
    else:
        return tooltip.get_text(strip=True)


def extract_specs(spec_url):
    soup = BeautifulSoup(urllib.urlopen(spec_url).read(), 'html.parser')

    if soup is None:
        print spec_url
        return

    model_name = soup.find('div', id='phone').find('h1').find('span')
    # print model_name
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
                    try:
                        feature_level2 = util.normalize(_get_real_text(fff.find('strong')))
                        key = ':'.join([feature_cate, feature_level1, feature_level2])
                        value = fff.find('li').get_text(strip=True)
                        specs[key] = value
                    except AttributeError:
                        continue

            else:
                next_sib = sub_feature.find_next_sibling()
                key = ':'.join([feature_cate, feature_level1])
                value = next_sib.get_text(strip=True)
                specs[key] = value

    return model_name, specs


def insert_to_hbase(row_key, column_family, specs, table_):
    data = {}
    for key, value in specs.iteritems():
        data[b'{0}:{1}'.format(column_family, key)] = \
            bytes(value.encode('utf-8') if isinstance(value, (str, unicode)) else str(value))
    table_.put(bytes(row_key), data)


def _scrape_specs(spec_url):
    print('Scraping: ...' + spec_url)
    try:
        row_key, specs = extract_specs(spec_url)
        insert_to_hbase(row_key, source, specs, table)
        return spec_url
    except UnicodeError and ValueError and IOError:
        print('Error of {}'.format(spec_url))
        return None


def scrape_specs(cate_url_file):
    try:
        m_pool = Pool(4)
        tracking = pd.read_csv(cate_url_file)
        if 'scraped' not in tracking:
            tracking['scraped'] = 0

        spec_url_to_scrape = tracking[tracking.scraped == 0]['spec_url'].values
        # for spec_url in spec_url_to_scrape:
        #     _scrape_specs(spec_url)

        multiple_results = \
            [m_pool.apply_async(_scrape_specs, (spec_url,)) for spec_url in spec_url_to_scrape]

        processed_urls = [result.get() for result in multiple_results]

        # print processed_urls
        succeed_urls = filter(lambda x: x is not None, processed_urls)

        m_pool.close()
        # print succeed_urls
        tracking.loc[tracking['spec_url'].isin(succeed_urls), 'scraped'] = 1

        # # update
        tracking.to_csv(cate_url_file, index=False)
    except ValueError:
        return


def main():
    data_path = os.path.join('data', source)
    for data_file in os.listdir(os.path.join('data', source)):
        cate_url_file = os.path.join(data_path, data_file)
        if not os.path.isfile(cate_url_file):
            continue

        try:
            scrape_specs(cate_url_file)
        except ValueError:
            continue


if __name__ == '__main__':
    # just run one time to get all of specs url
    # extract_all_spec_url('https://www.phonearena.com/phones/manufacturers')

    # _scrape_specs('https://www.phonearena.com/phones/Acer-Liquid-Zest-Plus_id10036')

    scrape_specs(os.path.join('data', source, 'ACER'))

    # print _extract_spec_url('https://www.phonearena.com/phones/manufacturers/Samsung/page/33')
    # main()
