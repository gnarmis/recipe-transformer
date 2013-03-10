import urllib2
import base64

#sentence = '3 1/2 cups peeled and diced potatoes';
sentence = '1. Lightly beat egg with a fork, then add a heaping tablespoon of the soup and mix lightly. Mix in rice, onion, parsley, onion salt and pepper. Stir in the ground beef and mix well with hands. Form mixture into 1 1/2 inch round meatballs. 2. Coat a large skillet over medium heat with cooking spray. Cook meatballs and brown on all sides. 3. Combine remaining soup with Worcestershire(you can increase or decrease Worcestershire to your liking), stir until smooth, then spoon over meatballs. Cover with lid and simmer for 20 to 30 minutes, stirring every few minutes.'
e = base64.b64encode(sentence)
content = urllib2.urlopen("http://localhost:5000/RecipeStepParser/"+e).read()

print content