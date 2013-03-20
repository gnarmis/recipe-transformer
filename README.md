recipe-transformer
==================

An explorative look at recipe transformation

## Chef

### API Endpoints

- GET `http://localhost:3000/food/search/:q` -> JSON response containing results, which have both foods and their groups (such as dairy)

For more info, check out Chef/README.md

## SethRecipeParser

The parser is called RecipeParser. If you navigate to the `SethRecipeParser` folder in the repo you can start the Flask server by running 'python RecipeParser.py'.

### API Endpoints

- GET `http://localhost:5000/RecipeParser/<input>` -> JSON response

- `<input>` = a url-encoded JSON map that should look like:
    ```json
    {"ingredients": <ingredient-string>,
     "steps": <steps-string>}
    ```
  Example code in python: `urllib.encode(json.dumps(input_dict))`, `json.loads(urllib.decode(param))`

Note that for the ingredient and steps lists, the parser assumes that there is a new-line character in-between each ingredient (fairly common in such lists). 

- Output:
```json
{"steps":[
     {
      "step":"Place %INGR0% in the oven for 10 to 12 minutes",
       "timeString": {
           "seconds": "0", 
           "hours": "0", 
           "minutes": "10"
        }
      },
      ...more steps
              ],
"ingredients": [
    {
      "full_sentence": "5 slices bacon", 
      "name": "bacons", 
      "number": "5", 
      "measurement": "", 
      "adj": "", 
      "prep": "slices"
    }, 
    ..more ingredients
 ]
}
```