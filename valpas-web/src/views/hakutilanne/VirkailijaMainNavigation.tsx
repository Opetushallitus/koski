import React, { useMemo } from "react"
import {
  MainNavigation,
  MainNavigationItem,
} from "../../components/navigation/MainNavigation"
import { t } from "../../i18n/i18n"
import { isFeatureFlagEnabled } from "../../state/featureFlags"

export const VirkailijaMainNavigation = () => {
  const navOptions: MainNavigationItem[] = useMemo(
    () => [
      {
        display: t("ylänavi__hakeutumisvelvolliset"),
        linkTo: "/hakutilanne",
      },
      {
        display: t("ylänavi__maksuttomuusoikeuden_arviointi"),
        linkTo: "/maksuttomuus",
      },
    ],
    []
  )

  return isFeatureFlagEnabled("maksuttomuus") ? (
    <MainNavigation
      title={t("ylänavi__otsikko_oppilaitos")}
      options={navOptions}
    />
  ) : null
}
