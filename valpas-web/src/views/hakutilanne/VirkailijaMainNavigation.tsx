import * as A from "fp-ts/Array"
import React, { useMemo } from "react"
import {
  MainNavigation,
  MainNavigationItem,
} from "../../components/navigation/MainNavigation"
import { t } from "../../i18n/i18n"
import {
  AccessGuard,
  hakeutumisenValvontaAllowed,
  kuntavalvontaAllowed,
  maksuttomuudenValvontaAllowed,
  suorittamisenValvontaAllowed,
  useKäyttöoikeusroolit,
} from "../../state/accessRights"
import {
  hakutilannePathWithoutOrg,
  kuntailmoitusPath,
  maksuttomuusPath,
  suorittaminenPath,
} from "../../state/paths"

type NavOption = MainNavigationItem & { visibleToRoles: AccessGuard }

export const VirkailijaMainNavigation = () => {
  const roles = useKäyttöoikeusroolit()

  const allNavOptions: NavOption[] = useMemo(
    () => [
      {
        display: t("ylänavi__kuntailmoitukset"),
        linkTo: kuntailmoitusPath.href(),
        visibleToRoles: kuntavalvontaAllowed,
      },
      {
        display: t("ylänavi__hakeutumisvelvolliset"),
        linkTo: hakutilannePathWithoutOrg.href(),
        visibleToRoles: hakeutumisenValvontaAllowed,
      },
      {
        display: t("ylänavi__oppivelvollisuuden_suorittaminen"),
        linkTo: suorittaminenPath.href(),
        visibleToRoles: suorittamisenValvontaAllowed,
      },
      {
        display: t("ylänavi__maksuttomuusoikeuden_arviointi"),
        linkTo: maksuttomuusPath.href(),
        visibleToRoles: maksuttomuudenValvontaAllowed,
      },
    ],
    [],
  )

  const navOptions: MainNavigationItem[] = useMemo(() => {
    return allNavOptions.filter((item) => item.visibleToRoles(roles))
  }, [roles, allNavOptions])

  return A.isNonEmpty(navOptions) ? (
    <MainNavigation title={t("ylänavi__otsikko")} options={navOptions} />
  ) : null
}
