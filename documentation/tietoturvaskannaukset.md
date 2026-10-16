# Kosken riippuvuuksien tietoturvaskannaukset

Kosken riippuvuuksia skannataan automaattisesti Renovatella (riippuvuuspäivitykset ja haavoittuvuudet) ja
Trivyllä (tiedostojärjestelmätason haavoittuvuusskannaus Github Actionsissa). Alla ohjeet virheiden
hiljentämiseksi.

Virheiden hiljentämisessä käytetään seuraavia periaatteita:
- Korjataan kriittiset/korkean tason ongelmat heti
- Jos korjausta ei ole saatavilla, seurataan tilannetta ja tehdään korjaus heti kuin mahdollista. Hälyn voi
hiljentää kuukaudeksi kerrallaan, jotta saadaan helpommin tieto muista mahdollisista ongelmista.
- Jos ongelman taso on keskisuuri tai matala mutta siihen ei ole korjausta, voidaan se hiljentää kuukaudeksi kerrallaan.
- Jos ongelman taso on keskisuuri tai matala ja se ei ole tuotantokäytössä, voidaan se hiljentää toistaiseksi.

## Renovate

Renovate on konfiguroitu tiedostossa `renovate.json`. Se hallinnoi riippuvuuspäivityksiä (npm, Maven) ja
haavoittuvuusilmoituksia OSV-tietokannan kautta. Renovate on asennettu GitHub-organisaatiotasolla GitHub Appina.

Renovaten luomat PR:t näkyvät GitHubissa automaattisesti. Patch-tason päivitykset mergetään automaattisesti.
Tietoturvapäivitykset merkitään `security`-labelilla.

## Trivy

Trivy skannaa tiedostojärjestelmätason haavoittuvuudet (CRITICAL/HIGH) Github Actionsissa.
Tunnetut väärät hälytykset voi hiljentää tiedostossa `.trivyignore`.
