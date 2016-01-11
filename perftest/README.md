# Suorituskykytestit

[Testiympäristöä](http://tordev.tor.oph.reaktor.fi/tor/) vasten ajettavat suorituskykytestit

### Testien ajaminen CI-palvelimella

Suorituskykytestit ajetaan automaattisesti CI-serverillä http://86.50.170.109:8080/job/PerformanceTest/

### Testien ajaminen omalta koneella


    TOR_USER=<käyttäjä> TOR_PASS=<salasana> [TOR_BASE_URL=<tor_url>] mvn gatling:execute -Dgatling.simulationClass=<simulationClass>

<!-- br -->

    TOR_USER        = Validi TOR-applikaation käyttäjätunnus
    TOR_PASS        = Käyttäjän salasana
    TOR_BASE_URL    = Testattavan TOR-applikaation osoite. Jos ei määritetty, niin oletuksena on http://tordev.tor.oph.reaktor.fi/tor
    gatling.simulationClass = Ajettava simulaation (tor.OverloadSimulation | tor.NormalSimulation)
    

Esimerkiksi:

Suorituskykytestin ajaminen testiympäristöä vasten normaalilla kuormalla

    TOR_USER=<käyttäjä> TOR_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=tor.NormalSimulation
    
Suorituskykytestin ajaminen testiympäristöä vasten ylikuormalla

    TOR_USER=<käyttäjä> TOR_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=tor.OverloadSimulation
    
Suorituskykytestin ajaminen omaa lokaalia ympäristöä vasten normaalilla kuormalla
    
    TOR_USER=<käyttäjä> TOR_PASS=<salasana> TOR_BASE_URL=http://localhost:7021/tor mvn gatling:execute -Dgatling.simulationClass=tor.NormalSimulation

