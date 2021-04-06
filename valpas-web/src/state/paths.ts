import { RouteComponentProps } from "react-router"
import { nonNull } from "../utils/arrays"
import { fromEntries, isEntry } from "../utils/objects"
import { Oid } from "./types"

export type QueryParams = Record<string, string | undefined | null>

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

// Etusivu

export const rootPath = (basePath: string = "") => `${basePath}/`

// Hakutilannenäkymä

export const perusopetusPathWithoutOrg = (basePath: string = "") =>
  `${basePath}/hakutilanne`

export const createPerusopetusPathWithoutOrg = perusopetusPathWithoutOrg

export const perusopetusPathWithOrg = (basePath: string = "") =>
  `${basePath}/hakutilanne/:organisaatioOid`

export const createPerusopetusPathWithOrg = (
  basePath: string = "",
  params: { organisaatioOid: Oid }
) => `${basePath}/hakutilanne/${params.organisaatioOid}`

export type PerusopetusViewRouteProps = RouteComponentProps<{
  organisaatioOid?: string
}>

// Oppijakohtainen näkymä

export const oppijaPath = (basePath: string = "") =>
  `${basePath}/oppijat/:oppijaOid`

export const createOppijaPath = (
  basePath: string = "",
  params: { oppijaOid: Oid; organisaatioOid?: Oid }
) =>
  queryPath(`${basePath}/oppijat/${params.oppijaOid}`, {
    organisaatioRef: params.organisaatioOid,
  })

export type OppijaViewRouteProps = RouteComponentProps<{
  oppijaOid: string
}>
