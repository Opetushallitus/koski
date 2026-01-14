# OmaData OAuth2 esimerkki-applikaatio

Tässä hakemistossa on 2 esimerkkiapplikaatiota OAuth2-integraatiosta Koski-palveluun:

- **Node.js -versio** - Lähdekoodi hakemistossa [server](server/). Sisältää myös staattisen HTML-frontin.
- **Java-versio** - Lähdekoodi hakemistossa [java](java/). Ks. [java/README.md](java/README.md).

Molemmat esimerkit käyttävät samaa mTLS (mutual TLS) -autentikointia, joka on käytössä tuotantoympäristössä AWS ALB:n kautta.

## Arkkitehtuuri

Lokaalisti ALB:n korvaa yksinkertainen, Node.js-pohjainen mock-toteutus.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Selain                                                                     │
│    │                                                                        │
│    ▼                                                                        │
│  (1) Esimerkki-applikaatio                                                  │
│      - Node.js: http://localhost:7051 (API + staattinen frontti)            │
│      - Java:    http://localhost:7052                                       │
│    │                                                                        │
│    │  mTLS (client cert)                                                    │
│    ▼                                                                        │
│  (2) ALB-proxy (emuloi AWS ALB:n mTLS-toimintaa), https://localhost:7022    │
│      - Käsittelee client certin                                             │
│      - Lisää x-amzn-mtls-clientcert-* headerit                              │
│    │                                                                        │
│    ▼                                                                        │
│  (3) Koski-backend, http://localhost:7021                                   │
│      - Validoi client certin headerien perusteella                          │
│      - Käsittelee OAuth2-pyynnöt                                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

Sertifikaatit ladataan oletuksena hakemistosta `../server/testca/`.

## Kehitys

### Esiehdot

1. Koski käynnissä portissa 7021 (ks. [README.md](../README.md#koski-sovelluksen-ajaminen-paikallisesti))
2. Testisertifikaatit generoitu:
   ```bash
   cd omadata-oauth2-sample/server
   ./testca/generate-certs.sh
   ```

### Node.js -esimerkki

Asenna riippuvuudet ja käynnistä:

```bash
cd omadata-oauth2-sample/server
pnpm install --frozen-lockfile

# Käynnistä ALB-proxy ja Node.js -palvelin (kahdessa terminaalissa tai taustalle):
pnpm run start:alb-proxy
pnpm run start:start:node-sample
```

Node.js -esimerkki löytyy osoitteesta http://localhost:7051

### Java-esimerkki

Ks. [java/README.md](java/README.md)

## E2E-testit

Playwright-testit testaavat koko OAuth2-flown kummankin backendin osalta:

```bash
cd omadata-oauth2-sample/server
pnpm install --frozen-lockfile
pnpm run test:e2e
```

Testit käynnistävät automaattisesti ALB-proxyn, Node.js-palvelimen ja Java-esimerkin.

## Tuotantoympäristö

Tuotannossa mTLS-käsittely tapahtuu AWS ALB:ssä, joka:
- Vastaanottaa client certin
- Validoi sen AWS Certificate Managerin avulla
- Lisää `x-amzn-mtls-clientcert-subject` ja `x-amzn-mtls-clientcert-serial-number` headerit

Lokaali ALB-proxy (`server/scripts/alb-proxy.mjs`) emuloi tätä kehitysympäristössä.

## Dokumentaatio

- Rajapintadokumentaatio: https://testiopintopolku.fi/koski/dokumentaatio/rajapinnat/oauth2/omadata
- Arkkitehtuuridokumentaatio: [documentation/oauth2.md](../documentation/oauth2.md)
