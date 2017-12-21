import re


def normalize(text):
    strip = re.sub(r'[^A-Za-z0-9\s]+', '', text.strip())
    return re.sub(r"[\s]+", "_", strip).upper()
