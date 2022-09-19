import bem from "bem-ts"
import { isEmpty } from "fp-ts/lib/Array"
import React from "react"
import {
  BorderlessCard,
  CardBody,
  CardHeader,
} from "../../components/containers/cards"
import { Column, ColumnsContainer } from "../../components/containers/Columns"
import { SuccessCircleIcon } from "../../components/icons/Icon"
import { InfoTooltip } from "../../components/tooltip/InfoTooltip"
import { t, T } from "../../i18n/i18n"
import { HakuLaajatTiedot } from "../../state/apitypes/haku"
import { HenkilöLaajatTiedot } from "../../state/apitypes/henkilo"
import { KansalainenOppijatiedot } from "../../state/apitypes/kansalainen"
import { isAktiivinenKuntailmoitus } from "../../state/apitypes/kuntailmoitus"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import { OppivelvollisuudenKeskeytys } from "../../state/apitypes/oppivelvollisuudenkeskeytys"
import {
  onOppivelvollisuudestaVapautettu,
  OppivelvollisuudestaVapautus,
} from "../../state/apitypes/oppivelvollisuudestavapautus"
import {
  Yhteystiedot,
  YhteystietojenAlkuperä,
} from "../../state/apitypes/yhteystiedot"
import { ISODate } from "../../state/common"
import { OppijaKuntailmoitus } from "./OppijaKuntailmoitus"
import { OppijanHaut } from "./OppijanHaut"
import { OppijanOpiskeluhistoria } from "./OppijanOpiskeluhistoria"
import { OppijanOppivelvollisuustiedot } from "./OppijanOppivelvollisuustiedot"
import { OppijanYhteystiedot } from "./OppijanYhteystiedot"
import "./OppijaView.less"
import {
  MinimiOpiskeluoikeus,
  MinimiOppijaKuntailmoitus,
} from "./typeIntersections"

const b = bem("oppijaview")

export type OppijaGridProps = {
  data: OppijaHakutilanteillaLaajatTiedot
}

export const OppijaGrid = (props: OppijaGridProps) => {
  return <Grid {...props.data.oppija} {...props.data} />
}

export type KansalainenGridProps = {
  data: KansalainenOppijatiedot
}

export const KansalainenGrid = (props: KansalainenGridProps) => {
  const kuntailmoitukset = props.data.kuntailmoitukset

  return (
    <Grid
      {...props.data.oppija}
      {...props.data}
      kuntailmoitukset={kuntailmoitukset}
      // TODO: Lisää tuki kansalaisen näkymälle
    />
  )
}

type GridProps = {
  henkilö: HenkilöLaajatTiedot
  kuntailmoitukset: MinimiOppijaKuntailmoitus[]
  opiskeluoikeudet: MinimiOpiskeluoikeus[]
  opiskelee: boolean
  oikeusKoulutuksenMaksuttomuuteenVoimassaAsti?: ISODate
  oppivelvollisuusVoimassaAsti: ISODate
  oppivelvollisuudenKeskeytykset: OppivelvollisuudenKeskeytys[]
  onOikeusTehdäKuntailmoitus?: boolean
  yhteystiedot: Yhteystiedot<YhteystietojenAlkuperä>[]
  hakutilanteet: HakuLaajatTiedot[]
  hakutilanneError?: string
  oppivelvollisuudestaVapautus?: OppivelvollisuudestaVapautus
  onOikeusMitätöidäOppivelvollisuudestaVapautus?: boolean
}

const Grid = (props: GridProps) => (
  <>
    {!onOppivelvollisuudestaVapautettu(props.oppivelvollisuudestaVapautus) && (
      <Kuntailmoitus
        aktiivisetKuntailmoitukset={props.kuntailmoitukset.filter(
          isAktiivinenKuntailmoitus
        )}
      />
    )}
    <ColumnsContainer>
      <Column size={4}>
        <BorderlessCard id="oppivelvollisuustiedot">
          <CardHeader>
            <T id="oppija__oppivelvollisuustiedot_otsikko" />
          </CardHeader>
          <CardBody>
            <OppijanOppivelvollisuustiedot
              henkilö={props.henkilö}
              opiskelee={props.opiskelee}
              oikeusKoulutuksenMaksuttomuuteenVoimassaAsti={
                props.oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
              }
              oppivelvollisuusVoimassaAsti={props.oppivelvollisuusVoimassaAsti}
              oppivelvollisuudenKeskeytykset={
                props.oppivelvollisuudenKeskeytykset
              }
              onOikeusTehdäKuntailmoitus={props.onOikeusTehdäKuntailmoitus}
              onOikeusMitätöidäOppivelvollisuudestaVapautus={
                props.onOikeusMitätöidäOppivelvollisuudestaVapautus
              }
              oppivelvollisuudestaVapautus={props.oppivelvollisuudestaVapautus}
            />
          </CardBody>
        </BorderlessCard>
      </Column>
      {!onOppivelvollisuudestaVapautettu(
        props.oppivelvollisuudestaVapautus
      ) && (
        <Column size={8}>
          <BorderlessCard id="yhteystiedot">
            <CardHeader>
              <T id="oppija__yhteystiedot_otsikko" />
              <InfoTooltip content={t("oppija__yhteystiedot_tooltip")} />
            </CardHeader>
            <CardBody>
              <OppijanYhteystiedot
                henkilö={props.henkilö}
                yhteystiedot={props.yhteystiedot}
              />
            </CardBody>
          </BorderlessCard>
        </Column>
      )}
    </ColumnsContainer>
    {!onOppivelvollisuudestaVapautettu(props.oppivelvollisuudestaVapautus) && (
      <ColumnsContainer>
        <Column size={4}>
          <BorderlessCard id="opiskeluhistoria">
            <CardHeader>
              <T id="oppija__opiskeluhistoria_otsikko" />
            </CardHeader>
            <CardBody>
              <OppijanOpiskeluhistoria
                henkilö={props.henkilö}
                opiskeluoikeudet={props.opiskeluoikeudet}
                kuntailmoitukset={props.kuntailmoitukset}
                oppivelvollisuudenKeskeytykset={
                  props.oppivelvollisuudenKeskeytykset
                }
              />
            </CardBody>
          </BorderlessCard>
        </Column>
        <Column size={8}>
          <BorderlessCard id="haut">
            <CardHeader>
              <T id="oppija__haut_otsikko" />
            </CardHeader>
            <CardBody>
              <OppijanHaut
                hakutilanteet={props.hakutilanteet}
                hakutilanneError={props.hakutilanneError}
              />
            </CardBody>
          </BorderlessCard>
        </Column>
      </ColumnsContainer>
    )}
  </>
)

type KuntailmoitusProps = {
  aktiivisetKuntailmoitukset: MinimiOppijaKuntailmoitus[]
}

const Kuntailmoitus = (props: KuntailmoitusProps) => (
  <>
    {isEmpty(props.aktiivisetKuntailmoitukset) ? (
      <EiIlmoituksiaMessage />
    ) : (
      props.aktiivisetKuntailmoitukset.map((kuntailmoitus, index) => (
        <OppijaKuntailmoitus key={index} kuntailmoitus={kuntailmoitus} />
      ))
    )}
  </>
)

const EiIlmoituksiaMessage = () => (
  <div className={b("eiilmoituksia")}>
    <SuccessCircleIcon color="green" />
    <T id="oppija__ei_ilmoituksia" />
  </div>
)
