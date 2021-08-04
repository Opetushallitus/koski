import React from "react"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"
import { suorittamisenValvontaAllowed } from "../../state/accessRights"
import { Oid } from "../../state/common"
import {
  createSuorittaminenHetuhakuPath,
  createSuorittaminenPath,
  createSuorittaminenPathWithOrg,
} from "../../state/paths"

export type SuorittaminenNavigationProps = {
  selectedOrganisaatio?: Oid
}

export const SuorittaminenNavigation = (
  props: SuorittaminenNavigationProps
) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("suorittaminen_nav__oppivelvolliset"),
      linkTo: props.selectedOrganisaatio
        ? createSuorittaminenPathWithOrg("", props.selectedOrganisaatio)
        : createSuorittaminenPath(),
    },
    {
      display: t("suorittaminen_nav__hae_hetulla"),
      linkTo: createSuorittaminenHetuhakuPath(),
    },
  ]

  return (
    <VisibleForKäyttöoikeusrooli rooli={suorittamisenValvontaAllowed}>
      <TabNavigation options={navOptions} />
    </VisibleForKäyttöoikeusrooli>
  )
}
