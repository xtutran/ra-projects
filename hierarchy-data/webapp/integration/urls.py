from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^phone?$', views.index, name='index'),
    url(r'^phone/list/$', views.listing, name='list'),
    url(r'^phone/detail/$', views.detail, name='detail'),
    url(r'^freebase/matching/$', views.freebase, name='freebase')
]