Curlilla kaiken saa haettua näin:
```
curl -X GET -H "Content-Type: application/json" 'localhost:9200/perustiedot-v3/_search?size=10000&pretty' -d '
{
    "query" : {
        "match_all" : {}
    }
}'
```

Muutaman eri henkilön haku kerralla, ilman curlia (Dev Tools Console):
```
GET perustiedot-v3/_search
{
  "query": {
    "bool": {
      "must": [
        {"terms": {"henkilö.oid": ["1.2.246.562.24.43853603938", "1.2.246.562.24.93535162831"]}}
      ]
    }
  }
}
```

Vastaavasti nuo henkilöt voisi sittn poistaa vaihtamalla `_search`-endpointin endpointtiin `_delete_by_query` ja `GET`-metodin `POST`-iin
