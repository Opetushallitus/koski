import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import React, { useContext, useMemo } from "react"
import { Redirect } from "react-router-dom"
import {
  Kayttooikeusrooli,
  käyttöoikeusrooliEq,
  OrganisaatioJaKayttooikeusrooli,
} from "../state/common"

export const hakeutumisenValvontaAllowed = (roles: Kayttooikeusrooli[]) =>
  roles.includes("OPPILAITOS_HAKEUTUMINEN")

export const maksuttomuudenValvontaAllowed = (roles: Kayttooikeusrooli[]) =>
  roles.includes("OPPILAITOS_MAKSUTTOMUUS")

export type WithRequiresAccessRightsProps = {
  redirectUserWithoutAccessTo: string
}

const accessRightGuardHoc = (
  hasAccess: (roles: Kayttooikeusrooli[]) => boolean
) => <P extends object>(
  Component: React.ComponentType<P>
): React.FC<P & WithRequiresAccessRightsProps> => (
  props: WithRequiresAccessRightsProps
) => {
  const roles = useKäyttöoikeusroolit()
  return hasAccess(roles) ? (
    <Component {...(props as P)} />
  ) : (
    <Redirect to={props.redirectUserWithoutAccessTo} />
  )
}

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
