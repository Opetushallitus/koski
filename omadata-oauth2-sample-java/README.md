## OmaData OAuth2 esimerkki-applikaatio

# Kehitys

Vaatii, että Koski on käynnissä portissa 7021. ks. [README.md](../README.md#koski-sovelluksen-ajaminen-paikallisesti) ja
että [koski-luovutuspalvelu](https://github.com/Opetushallitus/koski-luovutuspalvelu) -repositoryn sisältö on koski-hakemiston juuressa alihakemistossa

    /koski-luovutuspalvelu

Tämän jälkeen luovutuspalvelun, voi käynnistää:

    /omadata-oauth2-sample/client % npm run build-and-start-luovutuspalvelu

Tämän varsinen esimerkki-applikaatio frontti & backend käynnistetään terminaalissa:

    ./mvnw spring-boot:run

Koko putken rakenne:

    (1) Esimerkki-applikaation frontti & backend, http://localhost:7051

       kutsuu:
    (2) Docker-kontissa ajettu koski-luovutuspalvelu, https://localhost:7022
          Huom! Tämä on mutual TLS -putken testaamiseksi https, lokaalilla root-CA:lla.

      kutsuu:
    (3) Koski-backend, http://<lokaali-ip>:<port>
          Luovutuspalvelun docker-kontille näkyvä <lokaali-ip> on Github Actions CI:llä ajettaessa
          172.17.0.1 ja <port> arvottu.
          Lokaalisti <lokaali-ip> haetaan client/scripts/getmyip.js -skriptillä ja <port> on aina 7021.
