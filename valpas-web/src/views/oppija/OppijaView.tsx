import bem from "bem-ts"
import * as A from "fp-ts/Array"
import { pipe } from "fp-ts/lib/function"
import * as O from "fp-ts/Option"
import * as string from "fp-ts/string"
import React, { useMemo } from "react"
import { fetchOppija, fetchOppijaCache } from "../../api/api"
import { ApiMethodState, useApiWithParams } from "../../api/apiHooks"
import { isSuccess, mapError, mapLoading, mapSuccess } from "../../api/apiUtils"
import { ButtonLabel } from "../../components/buttons/ButtonLabel"
import { FlatLink } from "../../components/buttons/FlatButton"
import { Page } from "../../components/containers/Page"
import { BackIcon } from "../../components/icons/Icon"
import { Spinner } from "../../components/icons/Spinner"
import { Aikaleima } from "../../components/shared/Aikaleima"
import { Heading } from "../../components/typography/headings"
import { T, t } from "../../i18n/i18n"
import { withRequiresJokinOikeus } from "../../state/accessRights"
import { HenkilöLaajatTiedot } from "../../state/apitypes/henkilo"
import { OppijaHakutilanteillaLaajatTiedot } from "../../state/apitypes/oppija"
import {
  hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg,
  hakutilannePathWithOrg,
  hakutilannePathWithoutOrg,
  kuntailmoitusPathWithOrg,
  kuntarouhintaPathWithOid,
  nivelvaiheenHakutilannePathWithOrg,
  OppijaPathBackRefs,
  OppijaViewRouteProps,
  parseQueryFromProps as parseSearchQueryFromProps,
  suorittaminenPathWithOrg,
  suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg,
} from "../../state/paths"
import { plainComponent } from "../../utils/plaincomponent"
import { OppijaGrid } from "./OppijaGrid"
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
        oppija={isSuccess(oppija) ? oppija.data : undefined}
        {...searchQuery}
      />
      <OppijaHeadings oppija={oppija} oid={queryOid} />
      {mapLoading(oppija, () => (
        <Spinner />
      ))}
      {oppijaData && <Aikaleima />}
      {oppijaData && <OppijaGrid data={oppijaData} />}
    </Page>
  )
})

export type OppijaViewBackNavProps = {
  oppija?: OppijaHakutilanteillaLaajatTiedot
} & OppijaPathBackRefs

const BackNav = (props: OppijaViewBackNavProps) => {
  const targetPath = useMemo(() => {
    const fallback = pipe(
      O.fromNullable(props.oppija?.oppija.hakeutumisvalvovatOppilaitokset),
      O.map(A.sort(string.Ord)),
      O.chain(A.head),
      O.toNullable
    )

    if (props.prev) {
      return props.prev
    } else if (props.hakutilanneRef) {
      return hakutilannePathWithOrg.href(null, {
        organisaatioOid: props.hakutilanneRef,
      })
    } else if (props.hakutilanneNivelvaiheRef) {
      return nivelvaiheenHakutilannePathWithOrg.href(null, {
        organisaatioOid: props.hakutilanneNivelvaiheRef,
      })
    } else if (props.hakutilanneIlmoitetutRef) {
      return hakeutumisvalvonnanKunnalleIlmoitetutPathWithOrg.href(null, {
        organisaatioOid: props.hakutilanneIlmoitetutRef,
      })
    } else if (props.kuntailmoitusRef) {
      return kuntailmoitusPathWithOrg.href(null, props.kuntailmoitusRef)
    } else if (props.suorittaminenRef) {
      return suorittaminenPathWithOrg.href(null, props.suorittaminenRef)
    } else if (props.suorittaminenIlmoitetutRef) {
      return suorittamisvalvonnanKunnalleIlmoitetutPathWithOrg.href(null, {
        organisaatioOid: props.suorittaminenIlmoitetutRef,
      })
    } else if (props.kuntaRef) {
      return kuntarouhintaPathWithOid.href(null, {
        organisaatioOid: props.kuntaRef,
      })
    } else if (fallback) {
      return hakutilannePathWithOrg.href(null, { organisaatioOid: fallback })
    } else {
      return hakutilannePathWithoutOrg.href()
    }
  }, [props])

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
      {mapSuccess(props.oppija, (oppija) =>
        nimiWithOptionalHetu(oppija.oppija.henkilö)
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

const nimiWithOptionalHetu = (henkilö: HenkilöLaajatTiedot): string =>
  `${henkilö.sukunimi} ${henkilö.etunimet}` +
  (henkilö.hetu ? ` (${henkilö.hetu})` : "")

const SecondaryOppijaHeading = plainComponent("h2", b("secondaryheading"))
