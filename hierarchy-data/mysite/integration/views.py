from django.shortcuts import render
from django.http import HttpResponse, HttpResponseBadRequest
from django.template import loader
import happybase
import pandas as pd
import sys,os
# import extract
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

# pool = happybase.ConnectionPool(size=3, host='192.168.1.240')
global_schema = {}# extract.read_global_schema("integration/rules/global_schema.txt")


# Create your views here.
def index(request):
    return HttpResponse("Welcome to Data Integration site!")


def paginate(phones, page):
    paginator = Paginator(phones, 25)  # Show 25 contacts per page
    try:
        phones = paginator.page(page)
    except PageNotAnInteger:
        # If page is not an integer, deliver first page.
        phones = paginator.page(1)
    except EmptyPage:
        # If page is out of range (e.g. 9999), deliver last page of results.
        phones = paginator.page(paginator.num_pages)
    return phones


def _get_specs(table, column, row_prefix, limit):
    # with pool.connection() as connection:
    phones = []
    exit_code = 0
    if column not in table.families():
        return phones, exit_code

    try:
        for key, data in table.scan(filter=b'FirstKeyOnlyFilter()',
                                    row_prefix=bytes(row_prefix), limit=limit, columns=[column]):
            phones.append((key.decode('utf-8'), key.decode('utf-8')))
    except IOError and AssertionError:
        exit_code = 1
    return phones, exit_code


def _get_spec(table, column, row_key):
    try:
        row = table.row(bytes(row_key), columns=[column])
    except IOError and AssertionError:
        return {}, 1

    spec = pd.DataFrame(row, index=[0]).T.reset_index()
    # spec.columns = ['feature', 'value']
    features = spec['index'].apply(lambda x: pd.Series(x.replace('{}:'.format(column.lower()), '').split(':')))
    feature_cols = map(lambda x: 'level_{}'.format(x), features.columns)
    features.columns = feature_cols
    features['value'] = spec[0]

    while len(feature_cols) != 1:
        features = features.groupby(feature_cols[:-1]).\
            apply(lambda x: dict(zip(x[feature_cols[-1]], x.value))).\
            reset_index().\
            rename(columns={0: 'value'})
        feature_cols = features.columns[:-1]

    return map(lambda x: tuple(x), features.values), 0


def listing(request):
    parameters = request.GET
    tables = parameters.getlist('table') if 'table' in parameters else ['phonearena', 'gsmgarena']
    row_prefix = parameters['prefix'] if 'prefix' in parameters else ''
    try:
        limit = int(parameters.get('limit'))
        limit = limit if (limit <= 300) and (limit > 0) else 300
    except ValueError:
        limit = 100
    except TypeError:
        limit = 100
    page = request.GET.get('page')

    pool = happybase.ConnectionPool(size=3, host='192.168.56.101')
    with pool.connection() as connection:
        table = connection.table('phone_specs')
        phone_data = {}
        for column in tables:
            phones, exit_code = _get_specs(table, column.lower(), row_prefix=row_prefix.upper(), limit=limit)
            if exit_code == 0:
                phone_data[column] = paginate(phones, page)
                phone_data[column] = phones

        return render(request, 'list.html', {'phones': phone_data,
                                         'prefix': row_prefix, 'limit': limit})


def detail(request):
    print request.POST
    if 'phone' in request.POST:
        params = request.POST.getlist('phone')
        pool = happybase.ConnectionPool(size=3, host='192.168.56.101')
        with pool.connection() as connection:
            table = connection.table('phone_specs')
            phone_data = {}
            for param in params:
                tokens = param.split('###')
                column = tokens[0]
                row_key = tokens[1]
                data, exit_code = _get_spec(table, column, row_key)
                phone_data[param] = sorted(data, key=sortfn)

            return render(request, 'detail.html', {'phone_data': phone_data})
    else:
        return HttpResponseBadRequest('Bad request')


def sortfn(item):
    if len(global_schema) == 0:
        return item[0]
    else:
        return global_schema[item[0].lower()]
