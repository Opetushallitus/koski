import React from "react"
import { VisibleForKäyttöoikeusrooli } from "../../components/containers/VisibleForKäyttöoikeusrooli"
import {
  TabNavigation,
  TabNavigationItem,
} from "../../components/navigation/TabNavigation"
import { t } from "../../i18n/i18n"
import { kuntavalvontaAllowed } from "../../state/accessRights"
import { Oid } from "../../state/common"
import {
  kunnanHetuhakuPath,
  kuntailmoitusPath,
  kuntailmoitusPathWithOrg,
  kuntarouhintaPathWithOid,
  kuntarouhintaPathWithoutOid,
} from "../../state/paths"

export type KuntaNavigationProps = {
  selectedOrganisaatio?: Oid
}

export const KuntaNavigation = (props: KuntaNavigationProps) => {
  const organisaatioOid = props.selectedOrganisaatio
  const navOptions: TabNavigationItem[] = [
    {
      display: t("kuntailmoitus_nav__ilmoitetut"),
      linkTo: organisaatioOid
        ? kuntailmoitusPathWithOrg.href(null, organisaatioOid)
        : kuntailmoitusPath.href(),
    },
    {
      display: t("kuntailmoitus_nav__automaattinen_tarkistus"),
      linkTo: organisaatioOid
        ? kuntarouhintaPathWithOid.href(null, { organisaatioOid })
        : kuntarouhintaPathWithoutOid.href(),
    },
    {
      display: t("kuntailmoitus_nav__hae_hetulla"),
      linkTo: kunnanHetuhakuPath.href(),
    },
  ]

  return (
    <VisibleForKäyttöoikeusrooli rooli={kuntavalvontaAllowed}>
      <TabNavigation options={navOptions} />
    </VisibleForKäyttöoikeusrooli>
  )
}
