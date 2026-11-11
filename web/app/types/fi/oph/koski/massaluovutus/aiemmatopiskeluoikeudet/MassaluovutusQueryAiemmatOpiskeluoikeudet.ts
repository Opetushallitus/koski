/**
 * Palauttaa annettujen, oppilaitoksen nykyisten opiskelijoiden kaikki esi-, perus-, ammatillisen, lukio-, TUVA-, IB-, EB-, DIA-, International School- ja European School of Helsinki -koulutuksen opiskeluoikeudet: aiemmat, nykyiset ja tulevat, sekä muiden organisaatioiden että oppilaitoksen omat.
 * Oppijat annetaan henkilötunnuksina tai oppijanumeroina. Jokaisen annetun oppijan on oltava oppilaitoksen nykyinen opiskelija ja alle 18-vuotias tai kyselyn tekovuonna 18 vuotta täyttävä, muuten koko kysely hylätään ja vastaus kertoo, mitkä oppijat eivät täytä ehtoja. Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa päättymätön, myös vasta tulevaisuudessa alkava, jonkin näistä koulutusmuodoista opiskeluoikeus. Kysely tehdään aina yhdelle oppilaitokselle.
 * Esiopetustasoiset tiedot eli esiopetuksen opiskeluoikeudet sekä International Schoolin explorer-vuosiluokka ja Helsingin eurooppalaisen koulun nursery-vuosiluokat palautetaan vain, jos oppija on kyselyn oppilaitoksessa esi- tai perusopetustasoinen opiskelija. International Schoolissa ja eurooppalaisessa koulussa taso päätellään nykyisen opiskeluoikeuden uusimmasta vuosiluokasta: PYP ja MYP 6–9 sekä nursery, primary ja S1–S4 ovat perusopetustasoa, MYP 10, Diploma ja S5–S7 toista astetta. Oppilaitoksen omaa nykyistä opiskeluoikeutta ei karsita.
 * Kysely on käytettävissä palvelukäyttäjälle, jolla on kyselyn oppilaitoksessa lukuoikeus ja AIEMMAT_OPISKELUOIKEUDET-käyttöoikeus, sekä Opetushallituksen pääkäyttäjälle. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan aina. Skeemassa HiddenFromAiemmatOpiskeluoikeudet-merkinnällä varustetut kentät (esim. vankilaopetuksessa ja käyttäytymisenArvio) jätetään pois.
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
