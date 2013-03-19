import nltk
import string
import base64
import re
from flask import Flask
from flask import jsonify
from cPickle import load
from nltk.tag import brill
import urllib2
import json
import Pluralize
import IngredientParser
import StepParser

RecipeParser = Flask(__name__)

def findTime(step_sentence):
	
	step_sentence = step_sentence.lower()
	for c in string.punctuation:
		step_sentence = step_sentence.replace(c,'')
	step_sentence = step_sentence.replace('more','')
	step_sentence = step_sentence.replace('or so','')
	
	tokens = nltk.word_tokenize(step_sentence)
	seconds = ''
	minutes = ''
	hours = ''
	last_tcd = ''
	
	for word,tag in t2.tag(tokens):
		#print word,tag
		if(tag == 'TCD'):
			last_tcd += word+" "
		elif(word == 'to' and len(last_tcd) > 0):
			last_tcd += 'to '
		elif(tag == 'TIME'):
			if last_tcd == '':
				last_tcd = '1 '
			if(word == 'second' or word == 'seconds'):
				seconds = last_tcd[:-1]
			elif(word == 'minute' or word == 'minutes'):
				minutes = last_tcd[:-1]
			elif(word == 'hour' or word == 'hours'):
				hours = last_tcd[:-1]
			last_tcd = ''

	if len(seconds) == 0:
		seconds = '0'
	if len(minutes) == 0:
		minutes = '0'
	if len(hours) == 0:
		hours = '0'
				
	return {'seconds':seconds,'minutes':minutes,'hours':hours}

def placeSymbol(step_sentence,ingredients):
	#print step_sentence
	i = 0
	for ingredient_string in ingredients:
		step_sentence = step_sentence.replace(ingredient_string,'%INGR'+str(i)+'%')
		step_sentence = step_sentence.replace(Pluralize.pluralize(ingredient_string),'%INGR'+str(i)+'%')
		i += 1
	return step_sentence


@RecipeParser.route('/RecipeParser/<ingredients>/<steps>')
def parseCompiler(ingredients,steps):
	
	ingredients = base64.b64decode(ingredients).decode('utf-8')
	ingredient_list = ingredients.split('\n')

	parsed_ingredients = []
	for line in ingredient_list:
		#print line
		content = IngredientParser.classifyIngredient(line)
		#print content
		parsed_ingredients.append(content)
	
	steps = base64.b64decode(steps).decode('utf-8')
	parsed_steps = StepParser.parseRecipeStepList(steps)
	#at this point, our inputs have been parsed by their respective parsers
	
	#task: replace step strings with symbols
	#task: identify cook time, if a string contains it
	
	#compile a list of the ingredients, in order
	ingr_name_list = []
	ingr_output_list = []
	for ingredient_json in parsed_ingredients:
		data = json.loads(ingredient_json)
		if(len(data['name']) > 0):
			ingr_name_list.append(str(data['name']))
			data['name'] = Pluralize.pluralize(str(data['name']))
			#while we're looping, pluralize to help future database
		ingr_output_list.append(data)

	step_data = json.loads(parsed_steps)
	step_data =  step_data['steps']

	json_steps = []
	
	for step in step_data:
		s = findTime(str(step))
		r = placeSymbol(step,ingr_name_list)
		data = {'timeString': s, 'step': r}
		json_steps.append(data)
	print json_steps

	return jsonify(ingredients=ingr_output_list,steps=json_steps)

	#pluralize all ingredient names so that the database can match them more easily



input = open('t0.pkl','rb') #load default tagger
t0 = load(input)
input.close()

###Create specialized tags for time units
file = open('data/time_units.txt')
time_unit_tag_pairs = []
for line in file:
	if(line[-1] == '\n'):
		line = line[:-1]
	time_unit_tag_pairs.append( (line,'TIME') )

t1 = nltk.UnigramTagger([time_unit_tag_pairs],backoff=t0)
#######

######train a brill tagger for numbers before time units

number_training_set = [[('3','TCD'),('seconds','TIME')],[('1/2','TCD'),('second','TIME')],[('2.5','TCD'),('minutes','TIME')],[('1','TCD'),('minute','TIME')],[('1','TCD'),('hour','TIME')],[('2','TCD'),('hours','TIME')],
							  [('2','TCD'),('3','TCD')],[('1/2','TCD'),('3','TCD')],[('8','TCD'),('3','TCD')],[('9/4','TCD'),('3','TCD')],[('8.5','TCD'),('3','TCD')],
							  [('3','TCD'),('to','TO'),('5','TCD'),('minutes','TIME')],
							   [('2','TCD'),('to','TO'),('4','TCD'),('seconds','TIME')],
							   [('12','TCD'),('to','TO'),('19','TCD'),('hours','TIME')],
							   [('8','TCD'),('to','TO'),('12','TCD'),('minutes','TIME')],
							   [('1','TCD'),('to','TO'),('2','TCD'),('hours','TIME')]]
							  

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
t2 = trainer.train(number_training_set, max_rules=5, min_score=3)

#######
							 
#########
if __name__ == '__main__':
	RecipeParser.run(debug=True,port=5000)
