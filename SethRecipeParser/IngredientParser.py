import nltk
import string
import Pluralize
import base64
from nltk.corpus import brown
from cPickle import load
from nltk.tag import brill
from flask import Flask
from flask import jsonify
app = Flask(__name__)


class Ingredient:
	def __init__(self,number,measurement,food,prep,full_sentence):
		self.number = number
		self.measurement = measurement
		self.food = food
		self.prep = prep
		self.full_sentence = full_sentence

@app.route('/')
def index():
    return food_dict[0]


@app.route('/IngredientParser/<ingredient_sentence>')
def classifyIngredient(ingredient_sentence):
	ingredient_sentence = base64.b64decode(ingredient_sentence)
	ingredient_sentence = ingredient_sentence.decode('utf-8')
	
	tokens = nltk.word_tokenize(ingredient_sentence)

	if(ingredient_sentence[-1] == '\n'):
		ingredient_sentence = ingredient_sentence[:-1]
	ingredient_sentence += ' '
	
	spec_number = ''
	number = ''
	measurement = ''
	prep = ''
	food = ''
	foods = []
	food_adj = ''
		
	for line in food_dict:
		line = line.decode('utf-8').lower()
		if (ingredient_sentence).find(' '+Pluralize.pluralize(line)+' ') > -1:
			foods.append(line)
		elif (ingredient_sentence).find(' '+line+' ') > -1:
			foods.append(line)
	try:
		food = max(foods, key=len)
	except:
		pass
			
	for word,tag in t2.tag(tokens):
		print word, tag
		if(tag == 'SCD'):
			spec_number += word+" "
		elif(tag == 'CD'):
			number += word+" "
		elif(tag == 'MEA'):
			measurement += word+" "
		elif(tag == 'PREP' or tag == 'PREP_ADV'):
			prep += word+" "
		elif(tag == 'JJ' and not word in food):
				food_adj += word+" "
				
	#special case: '3 oranges' has no SCD's because there is no true measurement - take CD's instead
	if(measurement == '' and len(number) > 0):
		numbers = number
	else:
		numbers = spec_number

	#return Ingredient(numbers[:-1],measurement[:-1],(food_adj+food),prep[:-1],ingredient_sentence[:-1])
	return jsonify(number=numbers[:-1],measurement=measurement[:-1],name=(food_adj+food),prep=prep[:-1],full_sentence=ingredient_sentence[:-1])

input = open('t0.pkl','rb') #load default tagger
t0 = load(input)
input.close()


###Create specialized tags for measurements and knifework####
file = open('data/measurements.txt')
measurement_tag_pairs = []
for line in file:
	if(line[-1] == '\n'):
		line = line[:-1]
	measurement_tag_pairs.append( (line,'MEA') )

file = open('data/prep.txt')
prep_tag_pairs = []
for line in file:
	if(line[-1] == '\n'):
		line = line[:-1]
	prep_tag_pairs.append( (line,'PREP') )

misc_tag_pairs = [('boneless','JJ')]

t1 = nltk.UnigramTagger([measurement_tag_pairs,prep_tag_pairs, misc_tag_pairs], backoff=t0)

#######

######brill tagging#####

number_bigram_training_set = [[('3','SCD'),('cups','MEA')],[('1/2','SCD'),('oz','MEA')],[('2.5','SCD'),('gram','MEA')],[('2','SCD'),('tsp','MEA')],[('3','SCD'),('tbsp','MEA')],
							  [('2','SCD'),('3','SCD')],[('1/2','SCD'),('3','SCD')],[('8','SCD'),('3','SCD')],[('9/4','SCD'),('3','SCD')],[('8.5','SCD'),('3','SCD')],
							  [('thinly','PREP_ADV'),('chopped','PREP')],[('finely','PREP_ADV'),('cut','PREP')],[('carefully','PREP_ADV'),('sliced','PREP')],[('gingerly','PREP_ADV'),('diced','PREP')],[('gently','PREP_ADV'),('peeled','PREP')]];

templates = [
			 brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,1)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (2,2)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,2)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,3)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,1)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (2,2)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,2)),
			 brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,3)),
			 brill.ProximateTokensTemplate(brill.ProximateTagsRule, (-1, -1), (1,1)),
			 brill.ProximateTokensTemplate(brill.ProximateWordsRule, (-1, -1), (1,1))
			 ]

trainer = brill.FastBrillTaggerTrainer(t1, templates)
t2 = trainer.train(number_bigram_training_set, max_rules=5, min_score=3)

#######



###load food dictionary

file = open('data/food_dictionary.txt')
food_dict=[]
for line in file:
	if(line[-1] == '\n'):
		line = line[:-1]
	food_dict.append(line)

#######

#load test cases#
file =open('data/test_cases.txt')
test_ingredients=[]
for line in file:
	test_ingredients.append(line)
########


if __name__ == '__main__':
	app.run(debug=True)

'''
sentence = '3 1/2 cups peeled and diced potatoes';	
for c in ['.',',',';']:
		sentence = sentence.replace(c,'')
classified_ingr = classifyIngredient(sentence, food_dict,t2)

print 'NUM',classified_ingr.number,'MEA',classified_ingr.measurement,'FOOD',classified_ingr.food,'PREP',classified_ingr.prep

for ingredient in test_ingredients:
	print "WORKING ON: " + ingredient
	for c in ['.',',',';']:
		ingredient = ingredient.replace(c,'')

	classified_ingr = classifyIngredient(ingredient, food_dict,t2)
	print 'NUM',classified_ingr.number,'MEA',classified_ingr.measurement,'FOOD',classified_ingr.food,'PREP',classified_ingr.prep
	print

'''


###how do i classify parts of food? i.e. chicken breasts, broccoli florets?


