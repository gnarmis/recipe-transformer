import urllib
import json

# create functions to encode arguments in JSON and make it URL-safe

# expects a dictionary!
def safe_encode(x):
    return urllib.pathname2url(json.dumps(x))

def safe_decode(x):
    return json.loads(urllib.url2pathname(x))
