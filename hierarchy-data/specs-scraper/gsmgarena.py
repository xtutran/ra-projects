import os
import urllib
from multiprocessing import Pool

import bs4
import happybase
import pandas as pd
from bs4 import BeautifulSoup

import helper

print bs4.__version__


pool = happybase.ConnectionPool(size=5, host='192.168.56.101')
with pool.connection() as connection:
    table = connection.table('phone_specs')


def _extract_spec_url(cate_url, spec_urls, first=True):
    soup = BeautifulSoup(urllib.urlopen(cate_url).read(), 'html.parser')
    makers = soup.find('div', class_='makers')
    a_tags = makers.find_all('a')
    for a_tag in a_tags:
        spec_urls.append('https://www.gsmarena.com/{}'.format(a_tag.attrs['href']))

    if first:
        nav_pages = soup.find('div', class_='nav-pages')
        if nav_pages is None:
            return
        a_tags = nav_pages.find_all('a')
        for a_tag in a_tags:
            nav_page = 'https://www.gsmarena.com/{}'.format(a_tag.attrs['href'])
            _extract_spec_url(nav_page, spec_urls, False)


def extract_all_spec_url(base_url):
    soup = BeautifulSoup(urllib.urlopen(base_url).read(), 'html.parser')
    category = soup.find('div', class_='st-text')
    i = 0
    a_tags = category.find_all('a')

    for a_tag in a_tags:
        cate_name = a_tag.contents[0].upper()
        url_db_path = os.path.join('data', cate_name)

        if os.path.isfile(url_db_path):
            print('{} might be scraped already'.format(url_db_path))
            continue

        cate_url = 'https://www.gsmarena.com/{}'.format(a_tag.attrs['href'])
        spec_urls = []
        print('Scraping url of {}'.format(cate_url))

        _extract_spec_url(cate_url, spec_urls)
        url_db = [[cate_url, spec_url] for spec_url in spec_urls]
        url_db = pd.DataFrame(url_db, columns=['cate_url', 'spec_url'])
        url_db.to_csv(url_db_path, index=False)
        i += 1


def extract_specs(spec_url):
    soup = BeautifulSoup(urllib.urlopen(spec_url).read(), 'html.parser')
    model_name = soup.find('h1', class_='specs-phone-name-title')
    # specs_html = soup.find('div', id='specs-list')

    specs = {}
    i = 1

    for table in soup.findAll("table", {"cellspacing": "0"}):
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
        else:
            for f in table.find_all('td', class_='ttl'):
                if f.text == u'\xa0':
                    f.string = 'Other'
                    next_f = f.find_next_sibling()
                    next_f_text = '^^^'.join(filter(lambda x: isinstance(x, (str, unicode)), next_f.contents)).\
                        replace('\r\n', '').replace('\n', '')
                    next_f.string = next_f_text

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
            value = b'{value}'.format(value=row[1].encode('utf-8') if isinstance(row[1], (str, unicode)) else str(row[1]))
            data_row[key] = value

        batch.put(bytes(row_key), data_row)


def _scrape_specs(spec_url):
    print('Scraping: ...' + spec_url)
    try:
        row_key, specs = extract_specs(spec_url)
        insert_to_hbase(row_key, 'gsmgarena', specs, table)
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
    for dir_path, _, f_name in os.walk('data'):
        f_path = os.path.join(dir_path, f_name)
        try:
            scrape_specs(f_path)
        except ValueError:
            continue


if __name__ == '__main__':

    # just run one time to get all of specs url
    # extract_all_spec_url('https://www.gsmarena.com/makers.php3')

    # scrape_single_specs("https://www.gsmarena.com/acer_liquid_gallant_e350-4924.php")
    main()
