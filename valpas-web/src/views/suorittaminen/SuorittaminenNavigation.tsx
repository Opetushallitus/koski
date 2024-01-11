import React from "react"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { Aikaleima } from "../../components/shared/Aikaleima"
import { t } from "../../i18n/i18n"
import { suorittamisenValvontaAllowed } from "../../state/accessRights"
import { Oid } from "../../state/common"
import {
  suorittaminenHetuhakuPath,
  suorittaminenPath,
  suorittaminenPathWithOrg,
} from "../../state/paths"

export type SuorittaminenNavigationProps = {
  selectedOrganisaatio?: Oid
}

export const SuorittaminenNavigation = (
  props: SuorittaminenNavigationProps,
) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("suorittaminen_nav__oppivelvolliset"),
      linkTo: props.selectedOrganisaatio
        ? suorittaminenPathWithOrg.href(null, props.selectedOrganisaatio)
        : suorittaminenPath.href(),
    },
    {
      display: t("suorittaminen_nav__hae_hetulla"),
      linkTo: suorittaminenHetuhakuPath.href(),
    },
  ]

  return (
    <VisibleForKäyttöoikeusrooli rooli={suorittamisenValvontaAllowed}>
      <TabNavigation options={navOptions} />
      <Aikaleima />
    </VisibleForKäyttöoikeusrooli>
  )
}
