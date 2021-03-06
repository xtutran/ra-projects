import re


def normalize(text):
    strip = re.sub(r'[^A-Za-z0-9\s]+', '', str(text).strip())
    return re.sub(r"[\s]+", "_", strip).upper()
