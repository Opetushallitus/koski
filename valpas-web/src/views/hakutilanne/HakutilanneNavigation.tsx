import React from "react"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"
import { Oid } from "../../state/common"
import {
  createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  createHakutilannePathWithOrg,
} from "../../state/paths"

export type HakutilanneNavigationProps = {
  selectedOrganisaatio: Oid
}

export const HakutilanneNavigation = (props: HakutilanneNavigationProps) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("hakeutumisvelvollisetnavi__hakutilanne"),
      linkTo: createHakutilannePathWithOrg(undefined, {
        organisaatioOid: props.selectedOrganisaatio,
      }),
    },
    {
      display: t("hakeutumisvelvollisetnavi__ilmoitettu_kunnalle"),
      linkTo: createHakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg(
        undefined,
        {
          organisaatioOid: props.selectedOrganisaatio,
        }
      ),
    },
  ]

  return <TabNavigation options={navOptions} />
}
