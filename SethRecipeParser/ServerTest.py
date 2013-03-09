import urllib2
import base64

sentence = '3 1/2 cups peeled and diced potatoes';
e = base64.b64encode(sentence)
content = urllib2.urlopen("http://localhost:5000/IngredientParser/"+e).read()

print content