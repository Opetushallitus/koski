/**
 * Palauttaa annettujen, oppilaitoksen nykyisten opiskelijoiden esi-, perus-, ammatillisen ja lukiokoulutuksen opiskeluoikeudet, jotka ovat alkaneet ennen opiskelijan opintoja kyselyn oppilaitoksessa. Mukana ovat sekä muiden organisaatioiden että oppilaitoksen omat aiemmat opiskeluoikeudet.
 * Oppijat annetaan henkilötunnuksina tai oppijanumeroina. Jokaisen annetun oppijan on oltava oppilaitoksen nykyinen opiskelija ja alle 18-vuotias tai kyselyn tekovuonna 18 vuotta täyttävä, muuten koko kysely hylätään ja vastaus kertoo, mitkä oppijat eivät täytä ehtoja. Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa päättymätön, myös vasta tulevaisuudessa alkava, esi-, perus-, ammatillisen, lukio-, TUVA- tai IB-koulutuksen opiskeluoikeus. Kysely tehdään aina yhdelle oppilaitokselle.
 * Kysely edellyttää AIEMMAT_OPISKELUOIKEUDET-käyttöoikeutta kyselyn oppilaitoksessa. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan vain, jos käyttäjällä on niihin oikeus. Skeemassa HiddenFromAiemmatOpiskeluoikeudet-merkinnällä varustetut kentät (esim. vankilaopetuksessa ja käyttäytymisenArvio) jätetään pois käyttöoikeuksista riippumatta.
 * Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.
 *
 * @see `fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet`
 */
export type MassaluovutusQueryAiemmatOpiskeluoikeudet = {
  $class: 'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet'
  hetut?: Array<string>
  oppijaOidit?: Array<string>
  type: 'aiemmatOpiskeluoikeudet'
  organisaatioOid: string
  koulutusmuoto?: Array<string>
  format: 'application/json'
}

export const MassaluovutusQueryAiemmatOpiskeluoikeudet = (o: {
  hetut?: Array<string>
  oppijaOidit?: Array<string>
  type?: 'aiemmatOpiskeluoikeudet'
  organisaatioOid: string
  koulutusmuoto?: Array<string>
  format?: 'application/json'
}): MassaluovutusQueryAiemmatOpiskeluoikeudet => ({
  type: 'aiemmatOpiskeluoikeudet',
  $class:
    'fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet',
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
