/**
 * Palauttaa oppilaitoksen nykyisten opiskelijoiden muissa organisaatioissa suoritetut esi-, perus-, ammatillisen ja lukiokoulutuksen opiskeluoikeudet, jotka ovat alkaneet ennen opiskelijan opintoja kyselyn oppilaitoksessa.
 * Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa voimassa oleva esi-, perus-, ammatillisen tai lukiokoulutuksen opiskeluoikeus. Oppilaitoksen omia opiskeluoikeuksia ei palauteta. Kysely tehdään aina yhdelle oppilaitokselle.
 * Kysely edellyttää AIEMMAT_OPISKELUOIKEUDET-käyttöoikeutta kyselyn oppilaitoksessa. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan vain, jos käyttäjällä on niihin oikeus.
 * Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.
 *
 * @see `fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet`
 */
export type MassaluovutusQueryAiemmatOpiskeluoikeudet = {
  $class: 'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet'
  type: 'aiemmatOpiskeluoikeudet'
  format: 'application/json'
  organisaatioOid: string
  koulutusmuoto?: Array<string>
}

export const MassaluovutusQueryAiemmatOpiskeluoikeudet = (o: {
  type?: 'aiemmatOpiskeluoikeudet'
  format?: 'application/json'
  organisaatioOid: string
  koulutusmuoto?: Array<string>
}): MassaluovutusQueryAiemmatOpiskeluoikeudet => ({
  $class:
    'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet',
  type: 'aiemmatOpiskeluoikeudet',
  format: 'application/json',
  ...o
})

MassaluovutusQueryAiemmatOpiskeluoikeudet.className =
  'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet' as const

export const isMassaluovutusQueryAiemmatOpiskeluoikeudet = (
  a: any
): a is MassaluovutusQueryAiemmatOpiskeluoikeudet =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet'
