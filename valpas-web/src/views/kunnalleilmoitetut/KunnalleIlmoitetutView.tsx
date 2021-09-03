import React, { useMemo } from "react"
import { useHistory } from "react-router-dom"
import { Card, CardBody, CardHeader } from "../../components/containers/cards"
import { Page } from "../../components/containers/Page"
import {
  getOrganisaatiot,
  OrganisaatioValitsin,
} from "../../components/shared/OrganisaatioValitsin"
import { Counter } from "../../components/typography/Counter"
import { t, T } from "../../i18n/i18n"
import { useOrganisaatiotJaKäyttöoikeusroolit } from "../../state/accessRights"
import { OppijaHakutilanteillaSuppeatTiedot } from "../../state/apitypes/oppija"
import { Kayttooikeusrooli, Oid } from "../../state/common"
import { HakutilanneNavigation } from "../hakutilanne/HakutilanneNavigation"
import { KunnalleIlmoitetutTable } from "./KunnalleIlmoitetutTable"

export type KunnalleIlmoitetutViewProps = {
  organisaatioOid: Oid
  organisaatioTyyppi: string
  organisaatioHakuRooli: Kayttooikeusrooli
}

export const KunnalleIlmoitetutView = (props: KunnalleIlmoitetutViewProps) => {
  const organisaatiotJaKäyttöoikeusroolit = useOrganisaatiotJaKäyttöoikeusroolit()
  const organisaatiot = useMemo(
    () =>
      getOrganisaatiot(
        organisaatiotJaKäyttöoikeusroolit,
        props.organisaatioHakuRooli,
        props.organisaatioTyyppi
      ),
    [
      organisaatiotJaKäyttöoikeusroolit,
      props.organisaatioHakuRooli,
      props.organisaatioTyyppi,
    ]
  )

  const history = useHistory()
  const changeOrganisaatio = (oid?: Oid) => {
    if (oid) {
      history.push(oid)
    }
  }

  const data: OppijaHakutilanteillaSuppeatTiedot[] = []

  return (
    <Page>
      <OrganisaatioValitsin
        organisaatioTyyppi={props.organisaatioTyyppi}
        organisaatioHierarkia={organisaatiot}
        valittuOrganisaatioOid={props.organisaatioOid}
        label={t("Oppilaitos")}
        onChange={changeOrganisaatio}
      />
      <HakutilanneNavigation selectedOrganisaatio={props.organisaatioOid} />
      <Card>
        <CardHeader>
          <T id="kunnalleilmoitetut_otsikko" /> <Counter>{data.length}</Counter>
        </CardHeader>
        <CardBody>
          <KunnalleIlmoitetutTable data={data} />
        </CardBody>
      </Card>
    </Page>
  )
}
