import { t } from '../i18n/i18n'
import { LocalizedString } from '../types/fi/oph/koski/schema/LocalizedString'

export type RaportinOrganisaatio = {
  oid: string
  nimi: LocalizedString
  raportit: string[]
  children: RaportinOrganisaatio[]
}

export type NäkyväOrganisaatio = Omit<RaportinOrganisaatio, 'children'> & {
  children: NäkyväOrganisaatio[]
  selectable: boolean
  visible: boolean
}

export type KategorianRaportti = {
  id: string
  visibleForAllOrgs?: boolean
}

export type Raporttikategoria<R extends KategorianRaportti> = {
  id: string
  tab: string
  heading: string
  raportit: R[]
}

export type NäkyväRaportti<R extends KategorianRaportti> = R & {
  visible: boolean
  organisaatiot: NäkyväOrganisaatio[]
}

export type NäkyväKategoria<R extends KategorianRaportti> = Omit<
  Raporttikategoria<R>,
  'raportit'
> & {
  raportit: NäkyväRaportti<R>[]
  visible: boolean
}

export type Raporttivalinta = {
  kategoriaIdx: number
  raporttiIdx: number
  organisaatio: NäkyväOrganisaatio | undefined
}

export const raportinOrganisaatiot = (
  raporttityyppi: string,
  organisaatiot: RaportinOrganisaatio[]
): NäkyväOrganisaatio[] =>
  organisaatiot
    .map((organisaatio) => {
      const children = raportinOrganisaatiot(
        raporttityyppi,
        organisaatio.children
      )
      const selectable = organisaatio.raportit.includes(raporttityyppi)
      return {
        ...organisaatio,
        children,
        selectable,
        visible: selectable || children.some((child) => child.visible)
      }
    })
    .filter((organisaatio) => organisaatio.visible)

export const kaikkiOrganisaatiot = (
  organisaatiot: RaportinOrganisaatio[]
): NäkyväOrganisaatio[] =>
  organisaatiot.map((organisaatio) => ({
    ...organisaatio,
    children: kaikkiOrganisaatiot(organisaatio.children),
    selectable: true,
    visible: true
  }))

const raporttityypinOrganisaatiot = (
  raporttityyppi: string,
  organisaatiot: NäkyväOrganisaatio[]
): NäkyväOrganisaatio[] =>
  organisaatiot.flatMap((organisaatio) => {
    const children = raporttityypinOrganisaatiot(
      raporttityyppi,
      organisaatio.children
    )
    return organisaatio.raportit.includes(raporttityyppi)
      ? [{ ...organisaatio, children }]
      : children
  })

const sisältääOrganisaation = (
  organisaatiot: NäkyväOrganisaatio[],
  oid: string
): boolean =>
  organisaatiot.some(
    (organisaatio) =>
      organisaatio.oid === oid ||
      sisältääOrganisaation(organisaatio.children, oid)
  )

export const esivalitseOrganisaatio = (
  raportti: KategorianRaportti & { organisaatiot: NäkyväOrganisaatio[] },
  valittu: NäkyväOrganisaatio | undefined
): NäkyväOrganisaatio | undefined => {
  if (raportti.visibleForAllOrgs) {
    return valittu
  }
  const valittavat = raporttityypinOrganisaatiot(
    raportti.id,
    raportti.organisaatiot
  )
  return valittu && sisältääOrganisaation(valittavat, valittu.oid)
    ? valittu
    : valittavat.find((organisaatio) => organisaatio.selectable)
}

export const rajaaHakusanalla = (
  hakusana: string,
  organisaatiot: NäkyväOrganisaatio[]
): NäkyväOrganisaatio[] => {
  const haku = hakusana.toLowerCase()
  const rajaa = (orgs: NäkyväOrganisaatio[]): NäkyväOrganisaatio[] =>
    orgs.flatMap((organisaatio) => {
      const osuu = t(organisaatio.nimi).toLowerCase().includes(haku)
      const children = rajaa(organisaatio.children)
      return osuu || children.length > 0
        ? [
            {
              ...organisaatio,
              children: osuu ? organisaatio.children : children
            }
          ]
        : []
    })
  return rajaa(organisaatiot)
}

export const litistäOrganisaatiot = (
  organisaatiot: NäkyväOrganisaatio[]
): NäkyväOrganisaatio[] =>
  organisaatiot.flatMap((organisaatio) => [
    organisaatio,
    ...litistäOrganisaatiot(organisaatio.children)
  ])

export const organisaatioidenNimet = (
  organisaatiot: RaportinOrganisaatio[]
): Record<string, string> =>
  Object.fromEntries(
    litistäRaportinOrganisaatiot(organisaatiot).map((organisaatio) => [
      organisaatio.oid,
      t(organisaatio.nimi)
    ])
  )

const litistäRaportinOrganisaatiot = (
  organisaatiot: RaportinOrganisaatio[]
): RaportinOrganisaatio[] =>
  organisaatiot.flatMap((organisaatio) => [
    organisaatio,
    ...litistäRaportinOrganisaatiot(organisaatio.children)
  ])

export const näkyvätKategoriat = <R extends KategorianRaportti>(
  kategoriat: Raporttikategoria<R>[],
  organisaatiot: RaportinOrganisaatio[]
): NäkyväKategoria<R>[] =>
  kategoriat.map((kategoria) => {
    const raportit = kategoria.raportit.map((raportti) => {
      const raportinNäkyvätOrganisaatiot = raportti.visibleForAllOrgs
        ? kaikkiOrganisaatiot(organisaatiot)
        : raportinOrganisaatiot(raportti.id, organisaatiot)
      return {
        ...raportti,
        visible:
          !!raportti.visibleForAllOrgs ||
          raportinNäkyvätOrganisaatiot.length > 0,
        organisaatiot: raportinNäkyvätOrganisaatiot
      }
    })
    return {
      ...kategoria,
      raportit,
      visible: raportit.some((raportti) => raportti.visible)
    }
  })

const indeksiTunnisteella = <T extends { id: string }>(
  lista: T[],
  id: string | undefined
): number | null => {
  const indeksi = lista.findIndex((alkio) => alkio.id === id)
  return indeksi >= 0 ? indeksi : null
}

export const alkuvalinta = <R extends KategorianRaportti>(
  kategoriat: NäkyväKategoria<R>[],
  polku: string[]
): Raporttivalinta => {
  const [polunKategoria, polunRaportti] = polku
  const polunKategoriaIdx = indeksiTunnisteella(kategoriat, polunKategoria)
  const polunRaporttiIdx =
    polunKategoriaIdx === null
      ? null
      : indeksiTunnisteella(
          kategoriat[polunKategoriaIdx].raportit,
          polunRaportti
        )
  const kategoriaIdx =
    polunKategoriaIdx !== null && kategoriat[polunKategoriaIdx].visible
      ? polunKategoriaIdx
      : kategoriat.findIndex((kategoria) => kategoria.visible)
  const raporttiIdx =
    kategoriaIdx === polunKategoriaIdx &&
    polunRaporttiIdx !== null &&
    kategoriat[kategoriaIdx].raportit[polunRaporttiIdx].visible
      ? polunRaporttiIdx
      : 0

  return {
    kategoriaIdx,
    raporttiIdx,
    organisaatio:
      kategoriaIdx >= 0
        ? esivalitseOrganisaatio(
            kategoriat[kategoriaIdx].raportit[raporttiIdx],
            undefined
          )
        : undefined
  }
}

export const kategorianValinta = <R extends KategorianRaportti>(
  kategoriat: NäkyväKategoria<R>[],
  valinta: Raporttivalinta,
  kategoriaIdx: number
): Raporttivalinta => {
  const raportit = kategoriat[kategoriaIdx].raportit
  const raporttiIdx = raportit.findIndex((raportti) => raportti.visible)
  return {
    kategoriaIdx,
    raporttiIdx,
    organisaatio: esivalitseOrganisaatio(
      raportit[raporttiIdx],
      valinta.organisaatio
    )
  }
}

export const raportinValinta = <R extends KategorianRaportti>(
  kategoriat: NäkyväKategoria<R>[],
  valinta: Raporttivalinta,
  raporttiIdx: number
): Raporttivalinta => ({
  ...valinta,
  raporttiIdx,
  organisaatio: esivalitseOrganisaatio(
    kategoriat[valinta.kategoriaIdx].raportit[raporttiIdx],
    valinta.organisaatio
  )
})
