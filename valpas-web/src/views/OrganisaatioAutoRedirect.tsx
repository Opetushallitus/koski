import React, { useMemo } from "react"
import { Redirect } from "react-router-dom"
import {
  getOrganisaatiot,
  useStoredOrgState,
} from "../components/shared/OrganisaatioValitsin"
import { useOrganisaatiotJaKäyttöoikeusroolit } from "../state/accessRights"
import { useBasePath } from "../state/basePath"
import { Kayttooikeusrooli, Oid } from "../state/common"

export type OrganisatioAutoRedirectProps = {
  organisaatioHakuRooli: Kayttooikeusrooli | Kayttooikeusrooli[]
  organisaatioTyyppi: string
  redirectTo: (basePath: string, org: Oid) => string
  renderError: () => JSX.Element
}

export const OrganisaatioAutoRedirect = (
  props: OrganisatioAutoRedirectProps,
) => {
  const basePath = useBasePath()
  const organisaatiotJaKäyttöoikeusroolit =
    useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        props.organisaatioHakuRooli,
        props.organisaatioTyyppi,
      ),
    [
      organisaatiotJaKäyttöoikeusroolit,
      props.organisaatioHakuRooli,
      props.organisaatioTyyppi,
    ],
  )
  const [storedOrFallbackOrg] = useStoredOrgState(
    props.organisaatioTyyppi,
    organisaatiot,
  )
  return storedOrFallbackOrg ? (
    <Redirect to={props.redirectTo(basePath, storedOrFallbackOrg)} />
  ) : (
    props.renderError()
  )
}
