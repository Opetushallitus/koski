/**
 * Massaluovutusrajapinnan kysely valintalaskentaan.
 * Palauttaa valintalaskentaa varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista.
 * Vastauksen skeema on saatavana <a href="/koski/json-schema-viewer/?schema=valintalaskenta-result.json">täältä.</a>
 *
 * @see `fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery`
 */
export type ValintalaskentaQuery = {
  $class: 'fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery'
  oppijaOids: Array<string>
  type: 'valintalaskenta'
  rajapäivä: string
  suoritustyypit?: Array<string>
  koulutusmuoto?: string
  format: 'application/json'
}

export const ValintalaskentaQuery = (o: {
  oppijaOids?: Array<string>
  type?: 'valintalaskenta'
  rajapäivä: string
  suoritustyypit?: Array<string>
  koulutusmuoto?: string
  format?: 'application/json'
}): ValintalaskentaQuery => ({
  oppijaOids: [],
  type: 'valintalaskenta',
  $class: 'fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery',
  format: 'application/json',
  ...o
})

ValintalaskentaQuery.className =
  'fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery' as const

export const isValintalaskentaQuery = (a: any): a is ValintalaskentaQuery =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.valintalaskenta.ValintalaskentaQuery'
