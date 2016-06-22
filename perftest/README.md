# Suorituskykytestit

[Testiympäristöä](http://koskidev.koski.oph.reaktor.fi/koski/) vasten ajettavat suorituskykytestit

### Testien ajaminen CI-palvelimella

Suorituskykytestit ajetaan automaattisesti CI-serverillä http://86.50.170.109:8080/job/PerformanceTest/

### Testien ajaminen omalta koneella

Buildaa ensin applikaatio projektin juurihakemistossa:

    mvn install -DskipTests

Testit ajetaan tässä hakemistossa (perftest) seuraavasti:

    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> [KOSKI_BASE_URL=<koski_url>] mvn gatling:execute -Dgatling.simulationClass=<simulationClass>

<!-- br -->

    KOSKI_USER        = Validi KOSKI-applikaation käyttäjätunnus
    KOSKI_PASS        = Käyttäjän salasana
    KOSKI_BASE_URL    = Testattavan KOSKI-applikaation osoite. Jos ei määritetty, niin oletuksena on http://koskidev.koski.oph.reaktor.fi/koski
    gatling.simulationClass = Ajettava simulaation (koski.OverloadSimulation | koski.NormalSimulation)
    

Esimerkiksi:

Suorituskykytestin ajaminen testiympäristöä vasten normaalilla kuormalla

    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=koski.NormalSimulation
    
Suorituskykytestin ajaminen testiympäristöä vasten ylikuormalla

    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> mvn gatling:execute -Dgatling.simulationClass=koski.OverloadSimulation
    
Suorituskykytestin ajaminen omaa lokaalia ympäristöä vasten normaalilla kuormalla
    
    KOSKI_USER=<käyttäjä> KOSKI_PASS=<salasana> KOSKI_BASE_URL=http://localhost:7021/koski mvn gatling:execute -Dgatling.simulationClass=koski.NormalSimulation