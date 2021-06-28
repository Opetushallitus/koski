import React from "react"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"
import { Oid } from "../../state/common"
import {
  createKunnanHetuhakuPath,
  createKuntailmoitusPath,
  createKuntailmoitusPathWithOrg,
} from "../../state/paths"

export type KuntaNavigationProps = {
  selectedOrganisaatio?: Oid
}

export const KuntaNavigation = (props: KuntaNavigationProps) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("kuntailmoitus_nav__ilmoitetut"),
      linkTo: props.selectedOrganisaatio
        ? createKuntailmoitusPathWithOrg("", props.selectedOrganisaatio)
        : createKuntailmoitusPath(),
    },
    {
      display: t("kuntailmoitus_nav__hae_hetulla"),
      linkTo: createKunnanHetuhakuPath(),
    },
  ]

  return <TabNavigation options={navOptions} />
}
