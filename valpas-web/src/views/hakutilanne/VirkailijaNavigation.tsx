import React from "react"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"
import { Oid } from "../../state/common"
import { createHakutilannePathWithOrg } from "../../state/paths"

export type VirkailijaNavigationProps = {
  selectedOrganisaatio: Oid
}

export const VirkailijaNavigation = (props: VirkailijaNavigationProps) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("hakeutumisvelvollisetnavi__hakutilanne"),
      linkTo: createHakutilannePathWithOrg(undefined, {
        organisaatioOid: props.selectedOrganisaatio,
      }),
    },
  ]

  return <TabNavigation options={navOptions} />
}
