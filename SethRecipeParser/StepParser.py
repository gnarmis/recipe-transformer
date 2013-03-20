import nltk
import string
import re
import json
from cPickle import load


input = open('t0.pkl','rb') #load default tagger
t0 = load(input)
input.close()

def parseRecipeStepList(step_string):
	steps = []
	
	step_string = step_string.lower()
	step_tokens = nltk.word_tokenize(step_string)

	readyForNewStep = False
	step_string = ''
	inParen = 0

	for word,tag in t0.tag(step_tokens):
		print word, tag
		if word=='(':
			inParen += 1
			step_string += ' ' + word
		elif word==')':
			inParen -= 1
			step_string += word
		elif inParen > 0:
			step_string += addSpaceOrNot(word,step_string)
		elif not re.match('[0-9]\.',word):
			if (tag == 'VB' or tag == 'RB') and readyForNewStep:
				steps.append(step_string)
				step_string = word
				readyForNewStep = False
			elif not (tag == 'VB' or tag=='RB') and not (word == ',') and not (word == 'and') and not (word == 'or'):
				readyForNewStep = True
				step_string += addSpaceOrNot(word,step_string)
			else:
				step_string += addSpaceOrNot(word,step_string)

	steps.append(step_string)

	return json.dumps({'steps':steps})

def addSpaceOrNot(word, step_string):
	if word in string.punctuation or (len(step_string) > 0 and step_string[-1] == '('):
		return word
	else:
		return ' ' + word

