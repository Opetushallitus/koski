import React from "react"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { Aikaleima } from "../../components/shared/Aikaleima"
import { t } from "../../i18n/i18n"
import { Oid } from "../../state/common"
import {
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  hakutilannePathWithOrg,
  nivelvaiheenHakutilannePathWithOrg,
} from "../../state/paths"

export type HakutilanneNavigationProps = {
  selectedOrganisaatio: Oid
}

export const HakutilanneNavigation = (props: HakutilanneNavigationProps) => {
  const navOptions: TabNavigationItem[] = [
    {
      display: t("hakeutumisvelvollisetnavi__hakutilanne"),
      linkTo: hakutilannePathWithOrg.href(null, {
        organisaatioOid: props.selectedOrganisaatio,
      }),
    },
    {
      display: t("hakeutumisvelvollisetnavi__nivelvaihe"),
      linkTo: nivelvaiheenHakutilannePathWithOrg.href(null, {
        organisaatioOid: props.selectedOrganisaatio,
      }),
    },
    {
      display: t("hakeutumisvelvollisetnavi__kunnalle_tehdyt_ilmoitukset"),
      linkTo: hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg.href(null, {
        organisaatioOid: props.selectedOrganisaatio,
      }),
    },
  ]

  return (
    <>
      <TabNavigation options={navOptions} />
      <Aikaleima />
    </>
  )
}
