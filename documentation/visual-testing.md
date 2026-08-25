# Visuaaliset regressiotestit (Playwright screenshot-vertailu)

Koski-frontendin uusille (v2) käyttöliittymille on Playwright-pohjaiset
visuaaliset regressiotestit, jotka ottavat koko sivun kuvakaappauksia ja
vertaavat niitä talletettuihin baseline-kuviin. Kohteina ammatillisen
tutkinnon v2-näkymä ja vapaan sivistystyön (VST) näkymä.

## Työnkulku: kun muutit ulkoasua

```
1. Tee muutokset (ja make front, jos web/app muuttui)
2. make visual-test      -> mitkä näkymät muuttuivat?
3. make visual-update    -> nauhoita muuttuneet kuvat uudelleen
4. git status --short -- web/test/e2e/__screenshots__
5. Committaa ja pushaa
```

Koko kierros on nopea: mitattuna 7 kuvaa ~10 s.

Edellyttää käynnissä olevaa sovellusta (`make run`) ja ajantasaista
frontend-käännöstä (`make front`, jos `web/app`-koodia on muutettu – muuten
kuvat vastaisivat vanhaa käännöstä).

Kohta 4 kannattaa tehdä huolella, ja se on erilainen muuttuneille ja uusille
kuville:

- **Muuttunut kuva (`M`)**: muuttuivatko vain ne näkymät, joihin kosketit?
  Jos jokin muu muuttui, kyseessä on todennäköisesti aito regressio – älä
  hyväksy sitä. Epäonnistuneen ajon diff-kuvat ovat `web/test-results/`.
- **Uusi kuva (`??`)**: diff-kuvaa **ei ole**, koska vertailukohtaa ei ollut.
  Avaa kuva ja katso silmillä: onko näkymä se jonka halusit, ja onko se
  kokonaan renderöitynyt?

Huom: pelkkä `git diff` **ei näytä uusia kuvia**, koska ne eivät ole vielä
gitin seurannassa. Käytä `git status`.

## Miksi kaikki ajetaan Linux-kontissa

Kuvakaappaus riippuu ympäristöstä enemmän kuin äkkiseltään uskoisi.
Playwright niputtaa oman Chromiuminsa, mutta tekstin rasterointi ja
vierityspalkkien leveys tulevat käyttöjärjestelmältä. Mitattuna:

| Vertailu | Ero |
| --- | --- |
| macOS vs. Linux | 6/7 kuvaa eroaa: rasterointi (FreeType vs. Core Text) 2–3 % pikseleistä, ja vierityspalkit (macOS 0 px, Linux 15 px) muuttivat yhden näkymän korkeutta 75 px |
| Linux-kontti vs. GitHubin ajuri | 4 000–10 000 px, eri fonttivalikoiman takia – **vaikka arkkitehtuuri on sama** |
| Sama kontti, arm64 vs. amd64 | **≤ 1 px** – arkkitehtuurilla ei ole väliä |

Toinen rivi on se ratkaiseva: pelkkä "aja Linuxilla" ei riitä, koska *mikä*
Linux-ympäristö vaikuttaa. Siksi sekä kehittäjän kone että CI ajavat testit
samassa pinnatussa kontissa (`mcr.microsoft.com/playwright:vX-jammy`, tagi
luetaan asennetusta Playwright-versiosta). Silloin kuvat syntyvät samassa
ympäristössä riippumatta koneesta, eikä baseline-kuvien alkuperällä ole
väliä.

Docker on joka tapauksessa jo vaatimus Kosken ajamiseen
(`docker-compose.yaml`), joten ylimääräistä riippuvuutta ei tule.

Mittaerot eivät muuten olisi kierrettävissä lainkaan: Playwright tulkitsee
kuvien **mittaeron ehdottomaksi virheeksi** eikä katso toleranssia.

### Imagen pinnaus ja Playwrightin nostaminen

Image on pinnattu digestillä samoin kuin repon muut imaget. Tagi yksin ei
riitä: se on mutatoituva, ja koska image määrää fontit ja fontit määräävät
baseline-kuvat, tagin takana vaihtuva fonttipaketti tekisi kaikista
committoiduista kuvista kelvottomia ilman yhtään koodimuutosta.

Playwrightia nostettaessa `scripts/koski-visual.sh` kaatuu tarkoituksella,
kunnes `PINNED_PW_VERSION` ja `PINNED_PW_DIGEST` on päivitetty. Uusi digest:

```
docker buildx imagetools inspect mcr.microsoft.com/playwright:vX.Y.Z-jammy
```

Ota ylin **manifest list** -digest, älä arkkitehtuurikohtaista – sama arvo
toimii sekä CI:n amd64:llä että Apple Siliconin arm64:llä.

### Kontti ajaa rootina – tietoinen valinta

Linux-koneella kontin työhakemistoon luomat tiedostot (esim.
`web/test-results/`) jäävät rootin omistamiksi, jolloin esim. `git clean` ei
saa niitä poistettua ilman sudoa. macOS:llä Docker Desktopin uid-mappaus
piilottaa tämän.

**Tämä on hyväksytty haitta, älä "korjaa" sitä.** Suoraviivainen
`--user $(id -u):$(id -g)` on kokeiltu ja se rikkoo ajon kokonaan: nimetty
volume (`koski-visual-node-modules`) syntyy rootin omistamana, joten
ei-root-käyttäjä ei voi kirjoittaa siihen eikä `corepack`/`pnpm install`
käynnisty lainkaan.

Toimiva versio vaatisi volumen chownaamisen erillisessä root-kontissa ennen
jokaista ajoa. Se lisäisi kontin käynnistyksen joka kertaan ja muuttaisi myös
CI:n polkua, joka toimii nyt – kosmeettisen kiusan hinnaksi liikaa. Jos tämä
joskus muuttuu oikeaksi ongelmaksi, lähde liikkeelle siitä että ongelma on
volumen omistajuus, ei `--user` sinänsä.

## Tiedostot

- `web/test/e2e/*.visual.spec.ts` – testit. Käyttävät
  `fragments/visualScreenshot.ts`:n `otaVakaaKuvakaappaus`-apuria.
- `web/test/e2e/fragments/visualScreenshot.ts` – odottaa fonttien latautumisen
  ja sivun korkeuden vakiintumisen ennen kaappausta (ks. alla).
- `web/test/e2e/__screenshots__/` – baseline-kuvat, versioidaan gitiin.
- `web/playwright.visual.config.ts` – ajaa vain `*.visual.spec.ts`-tiedostot.
  Oletuskonfiguraatio jättää ne pois (`testIgnore`), koska ne ajetaan omana
  suitenaan kontissa.
- `scripts/koski-visual.sh` (`make visual-test` / `make visual-update`) –
  ajaa testit kontissa. Sama skripti sekä paikallisesti että CI:ssä.
  `make visual-clean` poistaa kontin riippuvuusvolumen.
- `src/test/scala/fi/oph/koski/e2e/KoskiVisualFrontSpec.scala` – käynnistää
  backendin ja kutsuu skriptiä. CI ajaa tämän omassa jobissaan
  (`.github/actions/koski_visual_test`).

### Verkotus kontista backendiin

Linuxilla (myös CI) käytetään `--network host`ia. Docker Desktopilla
(macOS/Windows) se ei toimi, joten siellä käytetään
`host.docker.internal`-nimeä yhdessä `--add-host=…:host-gateway`-lipun
kanssa.

## Kierre: nauhoitus voi tallentaa asettumattoman ruudun

Osa näkymistä renderöityy loppuun vasta hetken kuluttua: mitattuna VST:n
JOTPA-näkymä on välillä 1495 px korkea ennen kuin asettuu 1776 px:iin, ja
kansanopisto 2174 px ennen 3259 px:ää.

`toHaveScreenshot` odottaa vertailuajossa kahden peräkkäisen kaappauksen
täsmäävän, mutta `--update-snapshots` tallentaa **ensimmäisen** kaappauksen
ilman odotusta. Siksi baseline saattoi jäädä puolivalmiiseen tilaan.

`otaVakaaKuvakaappaus` odottaa asettumisen itse, joten nauhoitus ja vertailu
näkevät saman valmiin näkymän. **Käytä sitä uusissakin testeissä** suoran
`toHaveScreenshot`-kutsun sijaan – muuten virhe ei näy testin kaatumisena
vaan hiljaa väärin nauhoitettuna baselinena.

## Kuvakaappausten asetukset

Yhteiset `toHaveScreenshot`-oletukset ovat `web/playwright.config.ts`:ssä:

- `maxDiffPixels: 500` – absoluuttinen pikselibudjetti, sama paikallisesti
  ja CI:ssä.
- `animations: 'disabled'`, `caret: 'hide'` – vakauttaa kuvat.

**Miksi sama arvo molemmissa?** Koska molemmat renderöivät samassa kontissa,
jolloin `make visual-test` ennustaa CI:n tuloksen.

Älä siis löysennä arvoa paikallista ajoa varten, vaikka houkuttaisi:
`make visual-update` käyttää Playwrightin `changed`-presetiä, joka arvioi
muuttuneisuuden **voimassa olevalla toleranssilla**. Löysempi paikallinen
arvo jättäisi hiljaa nauhoittamatta juuri ne kuvat, jotka CI hylkää – ja
virhe näkyisi vasta CI:ssä, punaisena ajona.

Tästä seuraa yleisempi varoitus: `--update-snapshots`-ajon `N passed` ei
kerro mitään, koska testi menee läpi *sen jälkeen* kun baseline on
kirjoitettu uudelleen. Ainoa kelvollinen signaali on se, mitkä tiedostot
`git status` näyttää muuttuneiksi – ja varsinainen varmistus on erillinen
vertailuajo (`make visual-test`).

**Miksi absoluuttinen eikä suhteellinen?** Aiempi `maxDiffPixelRatio: 0.01`
antoi budjetin sivun koon mukaan: 4700 px korkealla sivulla 60 000 pikseliä.
Mitattuna se päästi läpi aidon regression – osasuoritusrivien välistyksen
muutos 6px → 8px tuotti yhdessä näkymässä 17 751 eroavaa pikseliä, mikä
mahtui budjettiin. Pitkä sivu ei ansaitse isompaa budjettia.

Nolla ei kelpaa budjetiksi: se rikkoo Playwrightin vakiintumisodotuksen,
joka vaatii kaksi tavulleen samaa peräkkäistä kaappausta.

**Mistä 500 tulee?** Mitattu, ei arvattu. Ajamalla CI:ssä arvolla
`maxDiffPixels: 1` jokainen kuva kaatuu ja tulostaa todellisen eron:

- kohinan katto **203 px** (14/15 kuvaa 138–203 px, yksi ≤ 1 px)
- pienin mitattu aito muutos **950 px**
- mittaeroja ei yhtään

500 asettuu näiden väliin: 2,5× kohina, puolet pienimmästä aidosta muutoksesta.

**Kohina ei ole renderöintiä vaan dataa.** Ero on kokonaan opiskeluoikeuden
oidissa, joka generoituu ajossa ja on siksi eri kehittäjän koneella ja CI:ssä.
Diff-kuvissa punaista on vain oidin numeroissa; ero ryhmittyy oppijan mukaan
eikä riipu sivun korkeudesta, ja ainoa ≤ 1 px:iin täsmännyt kuva oli se, jossa
topbar sattuu peittämään oid-rivin.

Kaksi seurausta:

- **Arkkitehtuurilla ei ole väliä** (arm64 ≡ emuloitu amd64, ≤ 1 px), joten
  `--platform`-virityksistä ei ole hyötyä.
- Oidin voisi **maskata** ja budjetin tiukentaa. Sitä ei ole tehty: yhtään
  aitoa 50–500 px:n regressiota ei ole havaittu, joten tiukennus toisi lähinnä
  vääriä hälytyksiä ja veisi pelivaraa tuntemattomalta epädeterminismiltä.
  Maski myös lakkaisi huomaamatta toimimasta, jos oidin test-id vaihtuu. Jos
  joskus tiukennat, mittaa kohina ensin uudelleen samalla tavalla.

Jos jokin näkymä sisältää oikeasti dynaamista sisältöä (esim. päivämääriä),
käytä `toHaveScreenshot`in `mask`-optiota kyseisille elementeille.

> **Huom. NixOS-kehityskoneet:** testit ajetaan kontissa, joten Playwrightin
> esikäännetyn chromiumin käynnistysongelmat NixOS:lla eivät koske näitä.
