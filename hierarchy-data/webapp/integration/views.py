from django.shortcuts import render
from django.http import HttpResponse, HttpResponseBadRequest
from django.template import loader
import happybase
import sys,os
# import extract
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

pool = happybase.ConnectionPool(size=3, host='192.168.1.240')
# pool = happybase.ConnectionPool(size=3, host='127.0.0.1')
# global_schema = extract.read_global_schema("integration/rules/global_schema.txt")


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


def _get_specs(columns, row_prefix, limit):
    with pool.connection() as connection:
        table = connection.table('phone_specs')
        try:
            for key, data in table.scan(row_prefix=bytes(row_prefix), limit=limit, columns=columns):
                print(key, data)
        except IOError:
            pass


def listing(request):
    parameters = request.GET
    print parameters
    tables = parameters.getlist('table') if 'table' in parameters else ['phonearena', 'gsmarena']
    row_prefix = parameters['prefix'] if 'prefix' in parameters else ''
    try:
        limit = int(parameters.get('limit'))
        limit = limit if (limit <= 300) else 300
    except ValueError:
        limit = 100
    except TypeError:
        limit = 100
    page = request.GET.get('page')
    print row_prefix, tables, limit

    _get_specs(tables, row_prefix, limit)

    # with pool.connection() as connection:
    #     phone_data = {}
    #     for table in tables:
    #         phones, exit_code = extract.get_phone_models(connection, table, row_prefix=row_prefix, limit=limit)
    #         if exit_code == 0:
    #             # print str(phones)
    #             phone_data[table] = paginate(tuple(phones.items()), page)
                # phone_data[table] = phones

    return HttpResponse('Hello Freebase')
    # return render(request, 'list.html', {'phones': phone_data,
    #                                      'prefix': row_prefix, 'limit': limit})


# def freebase(request):
#     return HttpResponse('Hello Freebase')
#
#
# def detail(request):
#     print request.POST
#     if 'phone' in request.POST:
#         params = request.POST.getlist('phone')
#         with pool.connection() as connection:
#             phone_data = {}
#             for param in params:
#                 tokens = param.split('###')
#                 table = tokens[0]
#                 row_key = tokens[1]
#                 data, exit_code = extract.get_phone(connection, table, row_key)
#                 phone_data[param] = sorted(data.items(), key=sortfn)
#         return render(request, 'detail.html', {'phone_data': phone_data})
#     else:
#         return HttpResponseBadRequest('Bad request')
#
#
# def sortfn(item):
#     if len(global_schema) == 0:
#         return item[0]
#     else:
#         return global_schema[item[0].lower()]