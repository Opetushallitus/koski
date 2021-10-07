Curlilla kaiken saa haettua näin:
```
curl -X GET -H "Content-Type: application/json" 'localhost:9200/perustiedot-v3/_search?size=10000&pretty' -d '
{
    "query" : {
        "match_all" : {}
    }
}'
```
