# Kosken riippuvuuksien tietoturvaskannaukset

Kosken riippuvuuksia skannataan päivittäin Github Actionsissa OWASP ja Snyk työkaluilla. Alla ohjeet näiden paikalliseen
ajamiseen ja sellaisten virheiden hiljentämiseksi, jotka eivät aiheuta tietoturvaongelmia.

Virheiden hiljentämisessä käytetään seuraavia periaatteita:
- Korjataan kriittiset/korkean tason ongelmat heti
- Jos korjausta ei ole saatavilla, seurataan tilannetta ja tehdään korjaus heti kuin mahdollista. Hälyn voi
hiljentää kuukaudeksi kerrallaan, jotta saadaan helpommin tieto muista mahdollisista ongelmista.
- Jos ongelman taso on keskisuuri tai matala mutta siihen ei ole korjausta, voidaan se hiljentää kuukaudeksi kerrallaan.
- Jos ongelman taso on keskisuuri tai matala ja se ei ole tuotantokäytössä, voidaan se hiljentää toistaiseksi.


## OWASP
Voit ajaa OWASP:in seuraavasti:

`make owasp`

Raportoidut riippuvuudet voivat toisinaan olla aliriippuvuuksissa. Jos riippuvuutte ei löydy pom.xml:stä, saat
riippuvuushierarkian komennolla:

`mvn dependency:tree`

Virheet on helpoin hiljentää avaamalla target/dependency-check-report.html. Klikkaa suppress nappulaa, josta saat
valmiin XML elementin, jonka voit lisätä tiedostoon
[owasp-dependency-check-suppressions.xml](../owasp-dependency-check-suppressions.xml).

Toisinaan voi olla niin, että samasta haavoittuvuudesta voi olla tieto useammassa rekisterissä. Näiden osalta kannattaa
pyrkiä laittamaan suppress elementtiin useampi cve tai vulnerabilityName elementti.

## Snyk

Voit ajaa Snykiä seuraavasti, SNYK_TOKEN löytyy 1Passwordista:

```
export SNYK_TOKEN=XXXXX
make snyk TAI
./web/node/node web/node_modules/snyk/dist/cli/index.js test web valpas-web
```

Virheet on helpoin hiljentää Snykin wizard työkalulla, jonka voit ajaa web tai valpas-web hakemistossa seuraavasti:

web
`./node/node node_modules/snyk/dist/cli/index.js wizard`

valpas-web
`./node/node ../web/node_modules/snyk/dist/cli/index.js wizard`
