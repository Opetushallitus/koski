import { RouteComponentProps } from "react-router-dom"
import { nonNull } from "../utils/arrays"
import { fromEntries, isEmptyObject, isEntry } from "../utils/objects"
import { Oid } from "./common"

export type QueryParams = Record<
  string,
  string | number | boolean | undefined | null
>

export const queryPath = (path: string, query: QueryParams) =>
  [path, queryString(query)].filter((s) => s.length > 0).join("?")

export const queryString = (query: QueryParams) =>
  Object.entries(query)
    .filter(([_key, value]) => nonNull(value))
    .map(
      ([key, value]) =>
        encodeURIComponent(key) + "=" + encodeURIComponent(value!!)
    )
    .join("&")

export const parseQueryFromProps = (
  props: RouteComponentProps
): Record<string, string> => {
  const query = props.location.search.split("?")[1]
  return query
    ? fromEntries(
        query
          .split("&")
          .map((entry) => entry.split("=").map(decodeURIComponent))
          .filter(isEntry)
      )
    : {}
}

export type PathDeclaration<A extends any[]> = {
  route(basePath?: string): string
  href(basePath?: string | null, ...args: A): string
}

export const declarePath = <A extends any[] = never[]>(
  route: string,
  mapParams: (...args: A) => object = () => ({})
): PathDeclaration<A> => {
  const getRoute = (basePath: string = "") =>
    `${basePath}/${route}`.replace(/\/\//g, "/")
  return {
    route: getRoute,
    href: (basePath?: string | null, ...args: A) => {
      const params = mapParams(...args)
      const [url, query] = Object.entries(params).reduce(
        ([url, query], [key, value]) => {
          const paramPlace = `:${key}`
          return url.includes(paramPlace)
            ? [url.replace(paramPlace, value), query]
            : [url, { ...query, [key]: value }]
        },
        [getRoute(basePath || ""), {} as QueryParams]
      )
      return isEmptyObject(query) ? url : queryPath(url, query)
    },
  }
}

const passParamsThru =
  <T extends object>() =>
  (params: T): T =>
    params

export type OrganisaatioOidProps = { organisaatioOid: Oid }
export type OrganisaatioOidRouteProps = RouteComponentProps<
  Partial<OrganisaatioOidProps>
>

// Etusivu

export const rootPath = declarePath("")

// Hakutilannenäkymä

export const hakutilannePathWithoutOrg = declarePath("hakutilanne")

export const hakutilannePathWithOrg = declarePath(
  "hakutilanne/:organisaatioOid",
  passParamsThru<OrganisaatioOidProps>()
)

// Oppijakohtainen näkymä

export type OppijaPathProps = {
  oppijaOid: Oid
} & OppijaPathBackRefs

export type OppijaPathBackRefs = {
  hakutilanneRef?: string
  hakutilanneNivelvaiheRef?: string
  hakutilanneIlmoitetutRef?: string
  kuntailmoitusRef?: string
  suorittaminenRef?: string
  suorittaminenIlmoitetutRef?: string
  kuntaRef?: string
  prev?: string
}

export type OppijaViewRouteProps = RouteComponentProps<Partial<OppijaPathProps>>

export const oppijaPath = declarePath(
  "oppija/:oppijaOid",
  passParamsThru<OppijaPathProps>()
)

// Suorittamisnäkymä

export const suorittaminenPath = declarePath("suorittaminen")

export const suorittaminenPathWithOrg = declarePath(
  "suorittaminen/organisaatio/:organisaatioOid",
  (organisaatioOid: Oid) => ({ organisaatioOid })
)

// Suorittamisen hakunäkymä

export const suorittaminenHetuhakuPath = declarePath("suorittaminen/haku")

// Maksuttomuusnäkymä

export const maksuttomuusPath = declarePath("maksuttomuus")

// Käyttöoikeusnäkymä

export const käyttöoikeusPath = declarePath("kayttooikeudet")

// Kuntailmoitukset

export const kuntailmoitusPath = declarePath("kuntailmoitukset")

export const kuntailmoitusPathWithOrg = declarePath(
  "kuntailmoitukset/organisaatio/:organisaatioOid",
  (organisaatioOid: Oid) => ({ organisaatioOid })
)

// Kunnan automaattinen tarkastus eli rouhinta

export const kuntarouhintaPathWithoutOid = declarePath(
  "kuntailmoitukset/automaattinen-tarkastus"
)

export const kuntarouhintaPathWithOid = declarePath(
  "kuntailmoitukset/automaattinen-tarkastus/:organisaatioOid",
  passParamsThru<OrganisaatioOidProps>()
)

// Kuntakäyttäjän hakunäkymä

export const kunnanHetuhakuPath = declarePath("kuntailmoitukset/haku")

// Hakeutumisvalvonnan 'kunnalle tehdyt ilmoitukset' -näkymä

export const hakeutumisvalvonnanKunnalleIlmoitetutPathWithoutOrg = declarePath(
  "hakutilanne/ilmoitetut"
)

export const hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg = declarePath(
  "hakutilanne/:organisaatioOid/ilmoitetut",
  passParamsThru<OrganisaatioOidProps>()
)

// Suorittamisvalvonnan 'kunnalle tehdyt ilmoitukset' -näkymä

export const suorittamisvalvonnanKunnalleIlmoitetutPathWithoutOrg = declarePath(
  "suorittaminen/ilmoitukset"
)

export const suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg = declarePath(
  "suorittaminen/ilmoitukset/:organisaatioOid",
  passParamsThru<OrganisaatioOidProps>()
)

// Nivelvaiheen hakeutumisvelvollisten seuranta

export const nivelvaiheenHakutilannePathWithoutOrg = declarePath(
  "hakutilanne/nivelvaihe"
)

export const nivelvaiheenHakutilannePathWithOrg = declarePath(
  "hakutilanne/:organisaatioOid/nivelvaihe",
  passParamsThru<OrganisaatioOidProps>()
)
