/**
 * Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden opiskeluoikeudet.
 * Tulostiedostot sisältävät tiedot json-muodossa. Jokaista oppijaa kohden luodaan oma tiedostonsa, jonka alle opiskeluoikeudet on ryhmitelty.
 * Tiedostojen sisältö vastaa pääosin opintohallintojärjestelmille tarkoitettua rajapintaa GET /koski/api/oppija/{oid}. Tulokset ryhmitellään henkilön master-oidin mukaan ja sisältävät linkitetytOidit-kentän.
 *
 * @see `fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson`
 */
export type MassaluovutusQueryOrganisaationOpiskeluoikeudetJson = {
  $class: 'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson'
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type: 'organisaationOpiskeluoikeudet'
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

export const MassaluovutusQueryOrganisaationOpiskeluoikeudetJson = (o: {
  alkanutViimeistään?: string
  muuttunutJälkeen?: string
  mitätöidyt?: boolean
  päättynytViimeistään?: string
  alkanutAikaisintaan: string
  eiPäättymispäivää?: boolean
  päättynytAikaisintaan?: string
  type?: 'organisaationOpiskeluoikeudet'
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
}): MassaluovutusQueryOrganisaationOpiskeluoikeudetJson => ({
  type: 'organisaationOpiskeluoikeudet',
  $class:
    'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson',
  format: 'application/json',
  ...o
})

MassaluovutusQueryOrganisaationOpiskeluoikeudetJson.className =
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson' as const

export const isMassaluovutusQueryOrganisaationOpiskeluoikeudetJson = (
  a: any
): a is MassaluovutusQueryOrganisaationOpiskeluoikeudetJson =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQueryOrganisaationOpiskeluoikeudetJson'
