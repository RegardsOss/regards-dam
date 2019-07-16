# CustomDataObjectsAccessPlugin

This plugin allows access only to new data objects. New data objects are thoses created at most X days ago. X is a parameter to configure.

Link to the doc : https://regardsoss.github.io/development/regards/catalog/api/search-api/#open-search-api

You need to write a request like this : 

```q=(Lucene format opensearch)```

https://regardsoss.github.io/development/regards/catalog/api/search-api/#using-lucene-standard-query-parser

For example,

```
q=(title:sampleTitle)
```
