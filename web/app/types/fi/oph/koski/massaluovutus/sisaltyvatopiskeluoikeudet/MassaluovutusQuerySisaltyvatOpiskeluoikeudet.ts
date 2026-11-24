/**
 * Palauttaa muiden oppilaitosten opiskeluoikeudet, jotka on merkitty sisältyviksi (kenttä sisältyyOpiskeluoikeuteen) organisaation tai sen alaorganisaatioiden opiskeluoikeuksiin. Täydentää organisaationOpiskeluoikeudet-kyselyä: sisältyvyys on merkitty vain sisältyvään opiskeluoikeuteen, joten sisältävän opiskeluoikeuden oppilaitos ei muuten näe, mitkä muualle kirjatut opiskeluoikeudet on liitetty sen omiin opiskeluoikeuksiin.
 * Hakuehdot kohdistuvat palautettaviin eli sisältyviin opiskeluoikeuksiin, eivät organisaation omiin sisältäviin opiskeluoikeuksiin. Opiskeluoikeus palautetaan vain, jos sen sisältävä opiskeluoikeus on olemassa eikä sitä ole mitätöity. Kunkin palautetun opiskeluoikeuden kenttä sisältyyOpiskeluoikeuteen kertoo, mihin organisaation opiskeluoikeuteen se on liitetty. Jos sisältyvyys poistetaan, sisältyvä opiskeluoikeus liitetään toiseen opiskeluoikeuteen tai sisältävä opiskeluoikeus mitätöidään, opiskeluoikeus jää pois kyselyn tuloksista. Poistumisesta ei tule erillistä tietoa, eikä se näy muuttunutJälkeen-ehdolla haettaessa.
 * Kysely on käytettävissä samoilla käyttöoikeuksilla kuin organisaationOpiskeluoikeudet: käyttäjällä on oltava lukuoikeus kyselyn organisaatioon ja LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-käyttöoikeus. Palautettavat koulutusmuodot rajautuvat käyttäjän koulutusmuotokohtaisiin käyttöoikeuksiin.
 * Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden. Vastauksen skeema on saatavana <a href="/koski/json-schema-viewer/?schema=massaluovutus-oppija-result.json">täältä.</a>
 *
 * @see `fi.oph.koski.massaluovutus.sisaltyvatopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet`
 */
export type MassaluovutusQuerySisaltyvatOpiskeluoikeudet = {
  $class: 'fi.oph.koski.massaluovutus.sisaltyvatopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet'
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type: 'sisaltyvatOpiskeluoikeudet'
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
  format: 'application/json'
}

export const MassaluovutusQuerySisaltyvatOpiskeluoikeudet = (o: {
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type?: 'sisaltyvatOpiskeluoikeudet'
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
  format?: 'application/json'
}): MassaluovutusQuerySisaltyvatOpiskeluoikeudet => ({
  type: 'sisaltyvatOpiskeluoikeudet',
  $class:
    'fi.oph.koski.massaluovutus.sisaltyvatopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet',
  format: 'application/json',
  ...o
})

MassaluovutusQuerySisaltyvatOpiskeluoikeudet.className =
  'fi.oph.koski.massaluovutus.sisaltyvatopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet' as const

export const isMassaluovutusQuerySisaltyvatOpiskeluoikeudet = (
  a: any
): a is MassaluovutusQuerySisaltyvatOpiskeluoikeudet =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.sisaltyvatopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet'
