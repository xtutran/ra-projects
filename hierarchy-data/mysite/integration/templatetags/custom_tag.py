from django import template

register = template.Library()


@register.simple_tag
def split(value):
    return value.split('^^^')


# register.tag('split', split)
