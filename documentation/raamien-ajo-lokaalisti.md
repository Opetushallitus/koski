# Raamien ajo lokaalisti

Tuotantoympäristössä kosken kanssa käytetään virkailijoiden ja oppijoiden raameja, jotka löytyvät seuraavista repoista:

- https://github.com/Opetushallitus/virkailija-raamit
- https://github.com/Opetushallitus/oppija-raamit

Raameja voi tulla joskus tarve ajaa lokaalisti, esimerkiksi testattaessa raamien linkkejä. Tässä tapauksessa raamit on mahdollista käynnistää lokaalisti käyttämällä seuraavia ohjeita:

## Virkailija-raamien käynnistys lokaalisti

1. Kloonaa https://github.com/Opetushallitus/virkailija-raamit
2. Jos käytössäsi on `nvm`, aja komento `nvm use`. Tällöin käytetään Noden versiota `10.10.0`.
3. Aja komento `./run-local-virkailija-raamit.sh`
4. Lisää Kosken VM-argumentteihin `-DvirkailijaRaamitProxy=http://localhost:8080`

## Oppija-raamien käynnistys lokaalisti

1. Kloonaa https://github.com/Opetushallitus/oppija-raamit
2. Aja komento `./run-local-virkailija-raamit.sh`
3. Lisää Kosken VM-argumentteihin `-DoppijaRaamitProxy=http://localhost:8079`
