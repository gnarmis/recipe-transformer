import urllib2
import base64
import StepParser
import IngredientParser

#content = urllib2.urlopen("http://localhost:5000/IngredientParser/"+base64.b64encode('one tablespoon worcestershire sauce')).read()
#print 'HALLLLO'
#print content
#print 'GOOOOODBYE'
#print ''

ingredients_string = '5 slices bacon\n1 onion, chopped\n2 cloves garlic, finely chopped, or more to taste\n1 pound lean ground beef\n1/2 cup barbeque sauce\n1/2 cup soft bread crumbs\n2 eggs, beaten\n1 tablespoon Worcestershire sauce\n1 teaspoon Dijon mustard\n1 teaspoon dried oregano\n1/2 teaspoon chili powder (optional)\n3 tablespoons barbeque sauce'

#print IngredientParser.classifyIngredient('5 slices bacon')

step_string = '1.	Preheat oven to 375 degrees F (190 degrees C). Grease a 9x11-inch baking dish. 2.	Place bacon in a large skillet and cook over medium-high heat until edges just start to curl, 1 to 2 minutes per side. Drain the bacon slices on paper towels. Reserve about 1 tablespoon bacon drippings in the skillet. 3.	Cook and stir onion and garlic in the bacon drippings until onion is softened, 6 to 7 minutes. Remove from heat and cool. 4.	Mix cooled onion and garlic, ground beef, 1/2 cup barbeque sauce, bread crumbs, eggs, Worcestershire sauce, Dijon mustard, oregano, and chili powder together with your hands in a large bowl. Form beef mixture into a log-shaped meatloaf; place in the prepared baking dish. 5.	Wrap partially-cooked bacon slices around the meatloaf, tucking the ends of bacon underneath the loaf. 6.	Bake in the preheated oven for 35 minutes. Brush bacon-wrapped meatloaf with 3 tablespoons barbeque sauce. Continue baking until no longer pink in the center, about 10 more minutes. An instant-read thermometer inserted into the center should read at least 160 degrees F (70 degrees C). Let stand for 10 minutes before serving.'


#print StepParser.parseRecipeStepList(step_string)


parsed_ingredients =  urllib2.urlopen("http://localhost:5000/RecipeParser/"+base64.b64encode(ingredients_string)+"/"+base64.b64encode(step_string)).read()

print parsed_ingredients

#print content