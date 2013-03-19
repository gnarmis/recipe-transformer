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

- GET `http://localhost:5000/RecipeParser/<ingredients>/<steps>/` -> JSON response

You can call the parser by sending a GET request to `http://localhost:5000/RecipeParser/<ingredients>/<steps>/` where `<ingredients>` is the base-64 encoded ingredient list, and `<steps>` is the steps, base-64 encoded. Note that for the ingredient list, the parser assumes that there is a new-line character in-between each ingredient (fairly common in an ingredient list). 

The output of this parser is in JSON. The format is as follows:

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