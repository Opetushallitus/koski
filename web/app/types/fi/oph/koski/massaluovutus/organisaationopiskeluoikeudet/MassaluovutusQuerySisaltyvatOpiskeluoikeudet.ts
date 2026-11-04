/**
 * Palauttaa muiden oppilaitosten opiskeluoikeudet, jotka on merkitty sisältyviksi (kenttä sisältyyOpiskeluoikeuteen) organisaation tai sen alaorganisaatioiden opiskeluoikeuksiin. Täydentää organisaationOpiskeluoikeudet-kyselyä: sisältyvyys on merkitty vain sisältyvään opiskeluoikeuteen, joten sisältävän opiskeluoikeuden oppilaitos ei muuten näe, mitkä muualle kirjatut opiskeluoikeudet on liitetty sen omiin opiskeluoikeuksiin.
 * Hakuehdot kohdistuvat palautettaviin eli sisältyviin opiskeluoikeuksiin, eivät organisaation omiin sisältäviin opiskeluoikeuksiin. Opiskeluoikeus palautetaan vain, jos sen sisältävä opiskeluoikeus on olemassa eikä sitä ole mitätöity. Kunkin palautetun opiskeluoikeuden kenttä sisältyyOpiskeluoikeuteen kertoo, mihin organisaation opiskeluoikeuteen se on liitetty.
 * Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.
 *
 * @see `fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet`
 */
export type MassaluovutusQuerySisaltyvatOpiskeluoikeudet = {
  $class: 'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet'
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
    'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet',
  format: 'application/json',
  ...o
})

MassaluovutusQuerySisaltyvatOpiskeluoikeudet.className =
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet' as const

export const isMassaluovutusQuerySisaltyvatOpiskeluoikeudet = (
  a: any
): a is MassaluovutusQuerySisaltyvatOpiskeluoikeudet =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet'
