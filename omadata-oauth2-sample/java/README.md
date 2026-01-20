# OmaData OAuth2 Java-esimerkki

Spring Boot -pohjainen esimerkki OAuth2-integraatiosta Koski-palveluun.

## Esiehdot

- Java 21 tai uudempi
- Koski käynnissä portissa 7021 (ks. [README.md](../../README.md#koski-sovelluksen-ajaminen-paikallisesti))
- Testisertifikaatit generoitu ja ALB-proxy käynnissä:

```bash
cd omadata-oauth2-sample/server
./testca/generate-certs.sh
pnpm run start:alb-proxy
```

## Käynnistys

```bash
cd omadata-oauth2-sample/java
KOSKI_BACKEND_HOST=http://localhost:7021 ./mvnw spring-boot:run
```

Java-esimerkki löytyy osoitteesta http://localhost:7052

## Arkkitehtuuri

Lokaalisti ALB:n korvaa yksinkertainen, Node.js-pohjainen mock-toteutus.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  (1) Java-esimerkki (Spring Boot), http://localhost:7052                    │
│    │                                                                        │
│    │  mTLS (client cert)                                                    │
│    ▼                                                                        │
│  (2) ALB-proxy, https://localhost:7022                                      │
│      - Emuloi AWS ALB:n mTLS-toimintaa                                      │
│      - Lisää x-amzn-mtls-clientcert-* headerit                              │
│    │                                                                        │
│    ▼                                                                        │
│  (3) Koski-backend, http://localhost:7021                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

Sertifikaatit ladataan oletuksena hakemistosta `../server/testca/`.

## Lisätietoja

- Päädokumentaatio: [README.md](../README.md)
- Rajapintadokumentaatio: https://testiopintopolku.fi/koski/dokumentaatio/rajapinnat/oauth2/omadata
