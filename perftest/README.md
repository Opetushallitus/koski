# Suorituskykytestit

[Testiympäristöä](http://tordev.tor.oph.reaktor.fi/koski/) vasten ajettavat suorituskykytestit

### Testien ajaminen CI-palvelimella

Suorituskykytestit ajetaan automaattisesti CI-serverillä http://86.50.170.109:8080/job/PerformanceTest/

### Testien ajaminen omalta koneella


    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> [KOSKI_BASE_URL=<tor_url>] mvn gatling:execute -Dgatling.simulationClass=<simulationClass>

<!-- br -->

    KOSKI_USER        = Validi KOSKI-applikaation käyttäjätunnus
    KOSKI_PASS        = Käyttäjän salasana
    KOSKI_BASE_URL    = Testattavan KOSKI-applikaation osoite. Jos ei määritetty, niin oletuksena on http://tordev.tor.oph.reaktor.fi/koski
    gatling.simulationClass = Ajettava simulaation (tor.OverloadSimulation | tor.NormalSimulation)
    

Esimerkiksi:

Suorituskykytestin ajaminen testiympäristöä vasten normaalilla kuormalla

    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=tor.NormalSimulation
    
Suorituskykytestin ajaminen testiympäristöä vasten ylikuormalla

    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=tor.OverloadSimulation
    
Suorituskykytestin ajaminen omaa lokaalia ympäristöä vasten normaalilla kuormalla
    
    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> KOSKI_BASE_URL=http://localhost:7021/koski mvn gatling:execute -Dgatling.simulationClass=tor.NormalSimulation

Wed Jan 20 16:33:23 EET 2016
