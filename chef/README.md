# chef

Exploring various food data. Specifically USDA's FNDDS5 and SR25.

## Usage

- Install leiningen
- `lein deps`
- `lein ring server`

## API

- GET `/food/:q` -> JSON response containing foods and their food groups

## Documentation

Check out the `doc` directory.

## Notes

- chef.food and chef.db deal with the `food` API resource
- SQL dumps provided in `src/chef/resource`
- need to run an alter table SQL query to add full-text search; a function to
do that is in chef.db

## License

Distributed under the Eclipse Public License, the same as Clojure.
