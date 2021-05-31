import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import React, { useContext, useMemo } from "react"
import { Redirect } from "react-router-dom"
import {
  Kayttooikeusrooli,
  käyttöoikeusrooliEq,
  OrganisaatioJaKayttooikeusrooli,
} from "../state/common"

export type WithRequiresHakeutumisenValvontaProps = {
  redirectJosKäyttäjälläEiOleOikeuksiaTo: string
}

const accessRightGuardHoc = (
  hasAccess: (roles: Kayttooikeusrooli[]) => boolean
) => <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P & WithRequiresHakeutumisenValvontaProps> => (
  props: WithRequiresHakeutumisenValvontaProps
) => {
  const roles = useKäyttöoikeusroolit()
  return hasAccess(roles) ? (
    <Component {...(props as P)} />
  ) : (
    <Redirect to={props.redirectJosKäyttäjälläEiOleOikeuksiaTo} />
  )
}

export const hakeutumisenValvontaAllowed = (roles: Kayttooikeusrooli[]) =>
  roles.includes("OPPILAITOS_HAKEUTUMINEN")

export const withRequiresHakeutumisenValvonta = accessRightGuardHoc(
  hakeutumisenValvontaAllowed
)

const käyttöoikeusroolitContext = React.createContext<
  OrganisaatioJaKayttooikeusrooli[]
>([])

export const KäyttöoikeusroolitProvider = käyttöoikeusroolitContext.Provider

export const useOrganisaatiotJaKäyttöoikeusroolit = () =>
  useContext(käyttöoikeusroolitContext)

export const useKäyttöoikeusroolit = (): Kayttooikeusrooli[] => {
  const data = useOrganisaatiotJaKäyttöoikeusroolit()
  return useMemo(
    () =>
      pipe(
        data,
        A.map((käyttöoikeus) => käyttöoikeus.kayttooikeusrooli),
        A.uniq(käyttöoikeusrooliEq)
      ),
    [data]
  )
}
