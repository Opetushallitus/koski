Perusteen rakenteen saa haettua esimerkiksi näin:
```
curl -v -X GET -H "Content-Type: application/json" -H "Caller-id: ihansama" -o "vst.json" "https://eperusteet.opintopolku.fi/eperusteet-service/api/external/peruste/7675672"
```

Jos perusteesta ei ole tiedossa sen ID:tä, voi perusteen diaarinumeron perusteella hakea metatiedot perusteesta näin:
```
curl -v -X GET -H "Content-Type: application/json" -H "Caller-id: ihansama" -o "diaari.json" "https://eperusteet.opintopolku.fi/eperusteet-service/api/external/perusteet?poistuneet=true&diaarinumero=OPH-123-2021"
```

ID löytyy json-vastauksessta.
