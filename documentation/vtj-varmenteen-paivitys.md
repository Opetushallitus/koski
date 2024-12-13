# VTJkysely-rajapinnan varmenteet

Koski hakee mTLS:ään tarvittavat varmenteet AWS Secrets Managerista base64-enkoodattuina merkkijonoina.

Keystore sisältää avaimen, DVV:ltä saadun varmenteen sekä julkiset väli- ja juurivarmenteen.

Truststore sisältää julkiset väli- ja juurivarmenteen.

Testausta varten huomaa, että rajapinnan osoitteet ovat
* https://vtjkysely.2016.vrk.fi/sosowebsite/soso.asmx (tuotanto)
* https://vtjkyselykoulutus.2016.vrk.fi/sosowebsite/soso.asmx (testi)

## Varmenteen hakeminen
Uutta varmennetta haetaan DVV:n portaalissa https://asiointi.dvv.fi/ asiointiluvan omaavan OPH-asiantuntijan toimesta.

Kehittäjää tarvitaan avaimen ja CSR-tiedoston luomista varten.

```
openssl genrsa -aes256 -out varmenne_key.pem 4096 # Laita avain talteen 1Passwordiin
openssl rsa -in varmenne_key.pem -pubout -out varmenne_pubkey.pem
openssl req -new -sha256 -key varmenne_key.pem -out varmenne.csr
```
Varmista että avain on tallessa 1Passwordissa mutta ei jää työasemalle lojumaan!

Kun CSR-tiedosto on olemassa, siirrytään asiointipalveluun.

Valitse asiointipalvelussa
    Julkishallinto →
    Haluan tilata toimikortteja tai palveluvarmenteita →
    Palveluvarmennehakemukset →
    Aloita →
    Käytä aikaisempaa lomaketta pohjana →
    valitse listalta uusin palveluvarmenne

Tilaamista varten tarvitaan seuraavat tiedot:

* Palveluvarmenteen tyyppi: Palvelinvarmenne
* Palveluvarmenteen ominaisuudet: "client authentication" ja "server authentication"
* Voimaantulopäivämäärä: noin 3 viikkoa ennen edellisen sertifikaatin vanhenemista
* Äsken luotu CSR-tiedosto

## Keystore

Kun varmenne on saapunut sähköpostiin, selvitä missä muodossa se tuli. Jos kyse on .der-muodosta, muunna varmenne .pem-muotoon:

```openssl x509 -inform der -in varmenne.der.crt -out varmenne.pem```

Nyt sen pitäisi olla tekstitiedosto ja alkaa merkkijonolla `-----BEGIN CERTIFICATE-----`.

Tarkista sitten, millaisella varmenneketjulla allekirjoitus on tehty:

```openssl x509 -in varmenne.pem -noout -text```

Lataa vastaavat juuri- ja välivarmenteet internetistä https://dvv.fi/ca-varmenteet.

Rakenna sitten keystore:
```
# Yhdistä PEM-tiedostot
cat varmenne_key.pem varmenne.pem dvvsp5rc.pem dvvroot3rc.pem > chain.pem

# Rakenna PKCS12-paketti (kiertääksesi keystore-formaatin rajoituksia)
openssl pkcs12 -export \
    -in varmenne.pem \
    -inkey varmenne_key.pem \
    -out varmenne.pkcs12 \
    -name koski.opintopolku.fi \
    -chain -CAfile chain.pem \
    -caname root

# Luo keystore PKCS12-paketista
keytool \
    -importkeystore \
    -destkeystore dvvkeystore.jks \
    -srckeystore varmenne.pkcs12 \
    -srcstoretype PKCS12 \
    -alias koski.opintopolku.fi
```

Voit tarkistaa keystoren sisällön seuraavasti:
```
keytool -list -keystore dvvkeystore.jks -rfc | openssl crl2pkcs7 -nocrl -certfile /dev/stdin | openssl pkcs7 -noout -print_certs
```

Lopuksi enkoodaa keystore base64-merkkijonoksi ja vie AWS Secrets Manageriin.

```base64 -i dvvkeystore.jks | pbcopy```

## Truststore
VTJkysely-rajapinnan palvelinvarmenteet ovat DVV:n itse allekirjoittamat ja siksi java ei luota palvelimeen ilman erillistä truststorea.

Selvitä palvelinvarmenteet:
```
openssl s_client -showcerts -connect vtjkysely.2016.vrk.fi:443
```
Lataa vastaavat juuri- ja välivarmenne internetistä https://dvv.fi/ca-varmenteet.

Vuonna 2024 tarvittavat varmenteet olivat esim. dvvroot3rc ja dvvsp5rc sekä testi- että tuotantoympäristössä.

Kokeile varmenteiden toimivuutta seuraavasti:

```
# Varmista että varmenteet ovat pem-muotoisia ja yhdistä ne
cat dvvroot3rc.pem dvvsp5rc.pem > combined_trust.pem

# Tarkista että SSL Handshake on OK
openssl s_client -showcerts -connect vtjkysely.2016.vrk.fi:443 -CAfile combined_trust.pem
```

Kun varmenteet on todettu toimiviksi, rakenna truststore:
```
keytool -importcert -keystore truststore.jks -alias dvv-root-ca -noprompt -file dvvroot3rc.pem
keytool -importcert -keystore truststore.jks -alias dvv-intermediate-ca -noprompt -file dvvsp5rc.pem
```

Voit tarkistaa sisällön komennolla

```keytool -list -keystore truststore.jks```


Lopuksi enkoodaa truststore base64-merkkijonoksi ja vie AWS Secrets Manageriin.

```base64 -i truststore.jks | pbcopy```

## Saapuneen varmenteen testaaminen curlilla

Saapunutta varmennetta voi testata rajapintaa vasten curl:illa:

```
curl -X POST -d "moi" \
    --cacert dvvroot3rc.pem \
    --cert varmenne.pem \
    --key varmenne_key.pem \
    https://vtjkysely.2016.vrk.fi/sosowebsite/soso.asmx
```

Ylläolevassa curliloitsussa määritellään.
* `--cacert vivulla` https://vtjkysely.2016.vrk.fi/ - käyttämä juuri sertifikaatti luotetuksi (koska vastapäässäkin käytetään ns. self-signed sertifikaatteja jotka eivät ole missään käyttöjärjestelmässä defaulttina luotettuina
* `--cert` vivulla määritellään client certificate, jota käyttäen halutaan autentikoitua vastapäähän
* `--key` vivulla määritellään salainen avain, jolla yllä oleva sertti on generoitu (avaimen salasana on sama jonka määrittelit private keyn generoinnin yhteydessä, mutta löytyy myös tuotannon parameter storesta)
* `-X POST -d "moi"` vivuilla määritellään http - requestin tyypiksi POST ja annetaan vähän jotain sisältöä POST - requestille

Havainnoidut curl:in antamat vastaukset riippuen sertifikaattien tilasta:

* Toimiva certti: valittaa POST:in sisällöstä
* Voimassa oleva certti, johon vastapää ei luota: 302 (jos tämä tulee certillä jonka pitäisi olla voimassa ja toimia → tupla tarkista, että certti on kunnossa ja sitten voi harkita, olisiko vastapäässä tarvetta asettaa certti luotetuksi)
* Vanhentunut certti: 403
* Ilman certtiä: 403
