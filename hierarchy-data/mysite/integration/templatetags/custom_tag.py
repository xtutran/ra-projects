from django import template

register = template.Library()


@register.simple_tag
def split(value):
    return value.split('^^^')


@register.simple_tag
def isstring(value):
    if isinstance(value, (str, unicode)):
        return 1
    else:
        return 0
# register.tag('split', split)
