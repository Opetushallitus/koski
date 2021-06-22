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
  createHakutilannePathWithoutOrg,
  createKuntailmoitusPath,
  createMaksuttomuusPath,
  createSuorittaminenPath,
} from "../../state/paths"

type NavOption = MainNavigationItem & { visibleToRoles: AccessGuard }

export const VirkailijaMainNavigation = () => {
  const roles = useKäyttöoikeusroolit()

  const allNavOptions: NavOption[] = useMemo(
    () => [
      {
        display: t("ylänavi__kuntailmoitukset"),
        linkTo: createKuntailmoitusPath(),
        visibleToRoles: kuntavalvontaAllowed,
      },
      {
        display: t("ylänavi__hakeutumisvelvolliset"),
        linkTo: createHakutilannePathWithoutOrg(),
        visibleToRoles: hakeutumisenValvontaAllowed,
      },
      {
        display: t("ylänavi__oppivelvollisuuden_suorittaminen"),
        linkTo: createSuorittaminenPath(),
        visibleToRoles: suorittamisenValvontaAllowed,
      },
      {
        display: t("ylänavi__maksuttomuusoikeuden_arviointi"),
        linkTo: createMaksuttomuusPath(),
        visibleToRoles: maksuttomuudenValvontaAllowed,
      },
    ],
    []
  )

  const navOptions: MainNavigationItem[] = useMemo(() => {
    return allNavOptions.filter((item) => item.visibleToRoles(roles))
  }, [roles, allNavOptions])

  return A.isNonEmpty(navOptions) ? (
    <MainNavigation title={t("ylänavi__otsikko")} options={navOptions} />
  ) : null
}
