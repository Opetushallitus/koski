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
import { withRequiresHakeutumisenValvonta } from "../../state/accessRights"
import { isAktiivinenKuntailmoitus } from "../../state/apitypes/kuntailmoitus"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import { isFeatureFlagEnabled } from "../../state/featureFlags"
import {
  createHakutilannePathWithOrg as createHakutilannePathWithOrg,
  createHakutilannePathWithoutOrg as createHakutilannePathWithoutOrg,
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

// TODO: Testidataa UI:n kehittämistä varten ennen kuin API alkaa palauttaa näitä
const concatOppijaMockData = (
  oppija: OppijaHakutilanteillaLaajatTiedot
): OppijaHakutilanteillaLaajatTiedot =>
  !isFeatureFlagEnabled("mock") ||
  oppija.oppija.henkilö.oid !== "1.2.246.562.24.00000000028"
    ? oppija
    : {
        ...oppija,
        kuntailmoitukset: [
          ...oppija.kuntailmoitukset,
          {
            aktiivinen: true,
            kuntailmoitus: {
              kunta: {
                oid: "123",
                nimi: { fi: "Kokkola" },
              },
              aikaleima: "2020-05-21T15:38:00Z",
              tekijä: {
                organisaatio: {
                  oid: "321",
                  nimi: { fi: "Elämäm koulu" },
                },
                henkilö: {
                  etunimet: "Heikki Kalevi",
                  sukunimi: "Randelin",
                  email: "randelin@sposti.fi",
                  puhelinnumero: "04012345666",
                },
              },
              yhteydenottokieli: {
                koodistoUri: "kieli",
                koodiarvo: "FI",
              },
              oppijanYhteystiedot: {
                puhelinnumero: "05012322121",
                email: "oppija@spot.fi",
                lähiosoite: "Lähikatu 5",
                postinumero: "04444",
                postitoimipaikka: "Järvenpää",
                maa: {
                  koodistoUri: "maatjavaltiot2",
                  koodiarvo: "123",
                  nimi: { fi: "Suomi" },
                },
              },
              hakenutMuualle: true,
            },
          },
          {
            aktiivinen: false,
            kuntailmoitus: {
              kunta: {
                oid: "123",
                nimi: { fi: "Kokkola" },
              },
              aikaleima: "2000-04-21T15:38:00Z",
              tekijä: {
                organisaatio: {
                  oid: "321",
                  nimi: { fi: "Elämäm koulu" },
                },
                henkilö: {
                  etunimet: "Heikki Kalevi",
                  sukunimi: "Randelin",
                  email: "randelin@sposti.fi",
                  puhelinnumero: "04012345666",
                },
              },
              yhteydenottokieli: {
                koodistoUri: "kieli",
                koodiarvo: "FI",
              },
              oppijanYhteystiedot: {
                puhelinnumero: "05012322121",
                email: "oppija@spot.fi",
                lähiosoite: "Lähikatu 5",
                postinumero: "04444",
                postitoimipaikka: "Järvenpää",
                maa: {
                  koodistoUri: "maatjavaltiot2",
                  koodiarvo: "123",
                  nimi: { fi: "Suomi" },
                },
              },
              hakenutMuualle: true,
            },
          },
        ],
      }

export const OppijaView = withRequiresHakeutumisenValvonta(
  (props: OppijaViewProps) => {
    const searchQuery = parseSearchQueryFromProps(props)
    const queryOid = props.match.params.oppijaOid!!
    const oppija = useApiWithParams(fetchOppija, [queryOid], fetchOppijaCache)
    const oppijaData = isSuccess(oppija)
      ? concatOppijaMockData(oppija.data)
      : null
    const aktiivisetKuntailmoitukset =
      oppijaData?.kuntailmoitukset.filter(isAktiivinenKuntailmoitus) || []

    return (
      <Page id="oppija">
        <BackNav
          organisaatioRef={searchQuery.organisaatioRef}
          oppija={isSuccess(oppija) ? oppija.data : undefined}
        />
        <OppijaHeadings oppija={oppija} oid={queryOid} />

        {mapLoading(oppija, () => (
          <Spinner />
        ))}

        {isEmpty(aktiivisetKuntailmoitukset) && <EiIlmoituksiaMessage />}
        {aktiivisetKuntailmoitukset.map((kuntailmoitus, index) => (
          <OppijaKuntailmoitus key={index} kuntailmoitus={kuntailmoitus} />
        ))}

        {oppijaData && (
          <>
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
  }
)

type BackNavProps = {
  organisaatioRef?: string
  oppija?: OppijaHakutilanteillaLaajatTiedot
}

const BackNav = (props: BackNavProps) => {
  const organisaatioOid =
    props.organisaatioRef || props.oppija?.oppija.oikeutetutOppilaitokset[0]
  const targetPath = organisaatioOid
    ? createHakutilannePathWithOrg("", { organisaatioOid })
    : createHakutilannePathWithoutOrg("")

  return (
    <div className={b("backbutton")}>
      <FlatLink to={targetPath}>
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

const EiIlmoituksiaMessage = () => (
  <div className={b("eiilmoituksia")}>
    <SuccessCircleIcon color="green" />
    <T id="oppija__ei_ilmoituksia" />
  </div>
)
