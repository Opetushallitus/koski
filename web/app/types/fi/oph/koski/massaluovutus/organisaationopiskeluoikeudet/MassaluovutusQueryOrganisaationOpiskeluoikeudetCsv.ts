/**
 * Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden opiskeluoikeudet.
 * Tulostiedostot sisältävät tiedot csv-muodossa. Tiedostoa ei luoda, jos se jää sisällöltään tyhjäksi.
 * Tiedostojen skeema vastaa KOSKI-raportointikannan skeemaa, joten integraatiossa on hyvä huomioida sen mahdollinen muuttuminen.
 * Skeema erittäin harvoin muuttuu niin, että kenttiä poistetaan tai niiden muoto muuttuu, mutta uusia kenttiä voi tulla mukaan.
 * Huom! Taulujen väliset relaatiot eivät ole stabiileja kyselyiden välillä, vaan id-kentät ovat kyselykohtaisia.
 * Jos kyselyn tuloksena syntyvä tulostiedosto on liian iso (n. 5 gigatavua), kysely epäonnistuu.
 * Tällaisessa tilanteessa tee kysely lyhyemmälle aikavälille tai käytä formaattinen text/x-csv-partition,
 * jolloin tiedostot jaetaan useampaan palaseen ja tiedostonimissä on mukana palasen numero.
 * Käyttäjän vastuulle jää tiedostojen yhdistäminen.
 * <ul><li>opiskeluoikeus.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_opiskeluoikeus.html">Opiskeluoikeudet</a>)</li>
<li>paatason_suoritus.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_paatason_suoritus.html">Päätason suoritukset</a>)</li>
<li>osasuoritus.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_osasuoritus.html">Osasuoritukset</a>)</li>
<li>opiskeluoikeus_aikajakso.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_opiskeluoikeus_aikajakso.html">Opiskeluoikeuksien aikajaksot</a>)</li>
<li>esiopetus_opiskeluoik_aikajakso.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/esiopetus_opiskeluoik_aikajakso.html">Esiopetuksen opiskeluoikeuksien aikajaksot</a>)</li>
<li>mitatoity_opiskeluoikeus.csv (<a href="https://db-documentation.testiopintopolku.fi/koski-raportointikanta/tables/r_mitatoitu_opiskeluoikeus.html">Mitätöidyt opiskeluoikeudet</a>)</li></ul>
 * 
 * @see `fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv`
 */
export type MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv = {
  $class: 'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv'
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type: 'organisaationOpiskeluoikeudet'
  eiOsasuorituksia?: boolean
  eiAikajaksoja?: boolean
  organisaatioOid?: string
  koulutusmuoto?:
    | 'ibtutkinto'
    | 'europeanschoolofhelsinki'
    | 'ammatillinenkoulutus'
    | 'perusopetuksenlisaopetus'
    | 'lukiokoulutus'
    | 'perusopetukseenvalmistavaopetus'
    | 'internationalschool'
    | 'luva'
    | 'tuva'
    | 'esiopetus'
    | 'perusopetus'
    | 'muukuinsaanneltykoulutus'
    | 'ebtutkinto'
    | 'taiteenperusopetus'
    | 'diatutkinto'
    | 'korkeakoulutus'
    | 'vapaansivistystyonkoulutus'
    | 'aikuistenperusopetus'
  format: 'text/csv' | 'text/x-csv-partition'
}

export const MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv = (o: {
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type?: 'organisaationOpiskeluoikeudet'
  eiOsasuorituksia?: boolean
  eiAikajaksoja?: boolean
  organisaatioOid?: string
  koulutusmuoto?:
    | 'ibtutkinto'
    | 'europeanschoolofhelsinki'
    | 'ammatillinenkoulutus'
    | 'perusopetuksenlisaopetus'
    | 'lukiokoulutus'
    | 'perusopetukseenvalmistavaopetus'
    | 'internationalschool'
    | 'luva'
    | 'tuva'
    | 'esiopetus'
    | 'perusopetus'
    | 'muukuinsaanneltykoulutus'
    | 'ebtutkinto'
    | 'taiteenperusopetus'
    | 'diatutkinto'
    | 'korkeakoulutus'
    | 'vapaansivistystyonkoulutus'
    | 'aikuistenperusopetus'
  format: 'text/csv' | 'text/x-csv-partition'
}): MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv => ({
  type: 'organisaationOpiskeluoikeudet',
  $class:
    'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv',
  ...o
})

MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv.className =
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv' as const

export const isMassaluovutusQueryOrganisaationOpiskeluoikeudetCsv = (
  a: any
): a is MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetCsv'
