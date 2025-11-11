# Swisscom-varmenteen päivitys/vaihtaminen

## Vaihe 1: Luo/uusi client cert

Esim. Let's encryptillä

    % brew install letsencrypt
    % certbot certonly -d pdf-test.koski-luovutuspalvelu-test-certs.testiopintopolku.fi --manual --preferred-challenges dns --config-dir ~/.certbot/config --logs-dir ~/.certbot/logs --work-dir ~/.certbot/work --register-unsafely-without-email

Noudata ohjeita ja tee ACME-autentikointi AWS konsolilla.

## Vaihe 2: tee .p12 -tiedosto

Generoi salasana, jolla avain kryptataan, PASSWORD alla:

    openssl pkcs12 -export \
    -in cert.pem \
    -inkey cert.key \
    -out client.p12 \
    -name "swisscom-test-client-cert" \
    -password pass:PASSWORD


## Vaihe 3: .p12 => Java KeyStore (.jks)

    keytool -importkeystore \
    -srckeystore client.p12 \
    -srcstoretype pkcs12 \
    -srcstorepass PASSWORD \
    -destkeystore client.jks \
    -deststoretype jks \
    -deststorepass PASSWORD

## Vaihe 4: base64-enkoodaa

    base64 -i client.jks | pbcopy

### Vaihe 5: Vie AWS secrets manageriin

Pastea base64-enkoodattu keystore ja valittu salasana swisscom-secrets sisältöön.

## Vaihe 6: Poista lokaalit tiedostot!

   Niitä ei enää tarvita, eikä saa jäädä roikkumaan levylle.
