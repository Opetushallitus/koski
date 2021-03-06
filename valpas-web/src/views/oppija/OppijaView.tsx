import bem from "bem-ts"
import { isEmpty } from "fp-ts/lib/Array"
import React from "react"
import { fetchOppija, fetchOppijaCache } from "../../api/api"
import { ApiMethodState, useApiWithParams } from "../../api/apiHooks"
import { isSuccess, mapError, mapLoading, mapSuccess } from "../../api/apiUtils"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import {
  BorderlessCard,
  CardBody,
  CardHeader,
} from "../../components/containers/cards"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { Page } from "../../components/containers/Page"
import { BackIcon, SuccessCircleIcon } from "../../components/icons/Icon"
import { Spinner } from "../../components/icons/Spinner"
import { InfoTooltip } from "../../components/tooltip/InfoTooltip"
import { Heading } from "../../components/typography/headings"
import { T, t } from "../../i18n/i18n"
import { withRequiresJokinOikeus } from "../../state/accessRights"
import { isAktiivinenKuntailmoitus } from "../../state/apitypes/kuntailmoitus"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import {
  createHakutilannePathWithOrg as createHakutilannePathWithOrg,
  createHakutilannePathWithoutOrg as createHakutilannePathWithoutOrg,
  createKuntailmoitusPathWithOrg,
  OppijaViewRouteProps,
  parseQueryFromProps as parseSearchQueryFromProps,
} from "../../state/paths"
import { plainComponent } from "../../utils/plaincomponent"
import { OppijaKuntailmoitus } from "./OppijaKuntailmoitus"
import { OppijanHaut } from "./OppijanHaut"
import { OppijanOpiskeluhistoria } from "./OppijanOpiskeluhistoria"
import { OppijanOppivelvollisuustiedot } from "./OppijanOppivelvollisuustiedot"
import { OppijanYhteystiedot } from "./OppijanYhteystiedot"
import "./OppijaView.less"

const b = bem("oppijaview")

export type OppijaViewProps = OppijaViewRouteProps

export const OppijaView = withRequiresJokinOikeus((props: OppijaViewProps) => {
  const searchQuery = parseSearchQueryFromProps(props)
  const queryOid = props.match.params.oppijaOid!!
  const oppija = useApiWithParams(fetchOppija, [queryOid], fetchOppijaCache)
  const oppijaData = isSuccess(oppija) ? oppija.data : null

  return (
    <Page id="oppija">
      <BackNav
        hakutilanneRef={searchQuery.hakutilanneRef}
        kuntailmoitusRef={searchQuery.kuntailmoitusRef}
        oppija={isSuccess(oppija) ? oppija.data : undefined}
        prevPage={searchQuery.prev}
      />
      <OppijaHeadings oppija={oppija} oid={queryOid} />

      {mapLoading(oppija, () => (
        <Spinner />
      ))}

      {oppijaData && (
        <>
          <Kuntailmoitus oppija={oppijaData} />
          <ColumnsContainer>
            <Column size={4}>
              <BorderlessCard id="oppivelvollisuustiedot">
                <CardHeader>
                  <T id="oppija__oppivelvollisuustiedot_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOppivelvollisuustiedot oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
            <Column size={8}>
              <BorderlessCard id="yhteystiedot">
                <CardHeader>
                  <T id="oppija__yhteystiedot_otsikko" />
                  <InfoTooltip>
                    <T id="oppija__yhteystiedot_tooltip" />
                  </InfoTooltip>
                </CardHeader>
                <CardBody>
                  <OppijanYhteystiedot
                    henkilö={oppijaData.oppija.henkilö}
                    yhteystiedot={oppijaData.yhteystiedot}
                  />
                </CardBody>
              </BorderlessCard>
            </Column>
          </ColumnsContainer>
          <ColumnsContainer>
            <Column size={4}>
              <BorderlessCard id="opiskeluhistoria">
                <CardHeader>
                  <T id="oppija__opiskeluhistoria_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanOpiskeluhistoria oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
            <Column size={8}>
              <BorderlessCard id="haut">
                <CardHeader>
                  <T id="oppija__haut_otsikko" />
                </CardHeader>
                <CardBody>
                  <OppijanHaut oppija={oppijaData} />
                </CardBody>
              </BorderlessCard>
            </Column>
          </ColumnsContainer>
        </>
      )}
    </Page>
  )
})

type BackNavProps = {
  hakutilanneRef?: string
  kuntailmoitusRef?: string
  oppija?: OppijaHakutilanteillaLaajatTiedot
  prevPage?: string
}

const BackNav = (props: BackNavProps) => {
  const targetPath = () => {
    const fallback = props.oppija?.oppija.hakeutumisvalvovatOppilaitokset[0]
    if (props.prevPage) {
      return props.prevPage
    } else if (props.hakutilanneRef) {
      return createHakutilannePathWithOrg("", {
        organisaatioOid: props.hakutilanneRef,
      })
    } else if (props.kuntailmoitusRef) {
      return createKuntailmoitusPathWithOrg("", props.kuntailmoitusRef)
    } else if (fallback) {
      return createHakutilannePathWithOrg("", { organisaatioOid: fallback })
    } else {
      return createHakutilannePathWithoutOrg("")
    }
  }

  return (
    <div className={b("backbutton")}>
      <FlatLink to={targetPath()}>
        <BackIcon />
        <ButtonLabel>
          <T id="oppija__takaisin" />
        </ButtonLabel>
      </FlatLink>
    </div>
  )
}

const OppijaHeadings = (props: {
  oppija: ApiMethodState<OppijaHakutilanteillaLaajatTiedot>
  oid: string
}) => (
  <>
    <Heading>
      {mapLoading(props.oppija, () => t("oppija__oletusotsikko"))}
      {mapSuccess(
        props.oppija,
        (oppija) =>
          `${oppija.oppija.henkilö.sukunimi} ${oppija.oppija.henkilö.etunimet} (${oppija.oppija.henkilö.hetu})`
      )}
      {mapError(props.oppija, () => t("oppija__oletusotsikko"))}
    </Heading>
    <SecondaryOppijaHeading>
      {mapSuccess(props.oppija, (oppija) =>
        t("oppija__oppija_oid", { oid: oppija.oppija.henkilö.oid })
      )}
      {mapError(props.oppija, () => t("oppija__ei_löydy", { oid: props.oid }))}
    </SecondaryOppijaHeading>
  </>
)

const SecondaryOppijaHeading = plainComponent("h2", b("secondaryheading"))

type KuntailmoitusProps = {
  oppija: OppijaHakutilanteillaLaajatTiedot
}

const Kuntailmoitus = (props: KuntailmoitusProps) => {
  const aktiivisetKuntailmoitukset = props.oppija.kuntailmoitukset.filter(
    isAktiivinenKuntailmoitus
  )

  return (
    <>
      {isEmpty(aktiivisetKuntailmoitukset) ? (
        <EiIlmoituksiaMessage />
      ) : (
        aktiivisetKuntailmoitukset.map((kuntailmoitus, index) => (
          <OppijaKuntailmoitus key={index} kuntailmoitus={kuntailmoitus} />
        ))
      )}
    </>
  )
}

const EiIlmoituksiaMessage = () => (
  <div className={b("eiilmoituksia")}>
    <SuccessCircleIcon color="green" />
    <T id="oppija__ei_ilmoituksia" />
  </div>
)
