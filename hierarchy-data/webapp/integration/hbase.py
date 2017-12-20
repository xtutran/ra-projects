# import happybase as hb
# import pickle
# from fuzzywuzzy import fuzz
# from fuzzywuzzy import process
# #import topicfinal
# #import matching
# #import pandas as pd
# import os
#
# def decode (data):
#     for i in data.keys():
#         data[i] = data[i].decode("utf8")
#         i = i.decode("utf8")
#     return data
#
# def cleardata(data,clearkey):
#     for key in data.keys():
#         if key == clearkey:
#             del data[key]
#     return data
#
#
# def run (table):
#     dic={}
#     for key,data in table.scan():
#         connection.open()
#         data = decode(data)
#         data = cleardata(data,'cf:HTML')
#         modelname = data['cf:PhoneName']
#         print modelname
#         fbdata,mid,typee = topicfinal.kbsearch(modelname)
#         if len(fbdata) != 0:
#             unstracture = {}
#             try:
#                 unstracture['description'] = fbdata['/common/topic/description']
#                 unstr_ratio = matching.matching(data,unstracture)
#             except:
#                 unstr_ratio = 0
#                 pass
#
#             try:
#                 del fbdata['/common/topic/description']
#             except KeyError:
#                 pass
#             str_ratio = matching.matching(data,fbdata)
#         else:
#             str_ratio = 0
#             unstr_ratio = 0
#         dic[modelname] = {}
#         dic[modelname]['str_ratio'] = str_ratio
#         dic[modelname]['unstr_ratio'] = unstr_ratio
#         dic[modelname]['mid'] = mid
#         dic[modelname]['type'] = typee
#         connection.close()
#     return dic
#
# connection = hb.Connection ('localhost')
# table = connection.table('gsmarena')
# b = run(table)
# i = pd.DataFrame.from_dict(b,orient='index')
# #print i
# i.to_csv('model_spec000.csv')
#
# ##
#
# ############
# ### edit code in Topic:
# ##when matching:
# ##    if it is a single word, look for the whole word only.
# ##    if not, use fuzzywuzzy
#
