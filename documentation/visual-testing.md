# Visuaaliset regressiotestit

Koski-frontendin v2-käyttöliittymien Playwright-testit vertaavat koko sivun
kuvakaappauksia versionhallinnassa oleviin baseline-kuviin.

## Ulkoasumuutoksen tarkistaminen

Testit edellyttävät käynnissä olevaa sovellusta (`make run`). Jos `web/app` on
muuttunut, päivitä frontend-käännös ensin komennolla `make front`.

```sh
make visual-test
```

Tarkista epäonnistuneen ajon kuvat hakemistosta `web/test-results/`. Jos muutos
on tarkoituksellinen, päivitä baseline-kuvat ja aja vertailu uudelleen:

```sh
make visual-update
git status --short -- web/test/e2e/__screenshots__
make visual-test
```

Käy kaikki muuttuneet ja uudet kuvat läpi ennen committia. `git diff` ei näytä
uusia PNG-tiedostoja, joten tarkista ne `git status` -komennolla. Älä hyväksy
testin kohteeseen liittymättömiä muutoksia.

## Ympäristö

Kuvakaappaukset ajetaan pinnatussa Playwright Linux -kontissa sekä paikallisesti
että CI:ssä, jotta fontit, tekstin rasterointi ja vierityspalkit pysyvät
yhdenmukaisina. Käytä aina `make visual-test`- ja `make visual-update`-komentoja
äläkä aja Playwrightin visual-skriptiä suoraan.

Playwrightia päivitettäessä korjaa `scripts/koski-visual.sh`-tiedoston
`PINNED_PW_VERSION` ja `PINNED_PW_DIGEST`. Manifest list -digestin saa komennolla:

```sh
docker buildx imagetools inspect mcr.microsoft.com/playwright:vX.Y.Z-jammy
```

Kontti ajaa tarkoituksella root-käyttäjänä. `--user` rikkoo nimetyn
`koski-visual-node-modules`-volumen kirjoitusoikeudet.

## Ongelmatilanteet

- Mitta- tai fonttierot: varmista, että ajo tehtiin `make visual-test`-komennolla
  pinnatussa kontissa.
- Puuttuva tai vanha sisältö: varmista, että backend on käynnissä ja suorita
  `make front`, jos frontend-koodi muuttui.
- Yllättävä PNG-muutos: tarkista `web/test-results/` ja selvitä ero ennen
  baseline-kuvan päivittämistä.
- Riippuvuusvolumeen liittyvä ongelma: `make visual-clean` poistaa visual-testien
  node_modules-volumen.
- Kontin yhteys backendiin: Linux käyttää host-verkkoa, Docker Desktop
  `host.docker.internal`-osoitetta.
