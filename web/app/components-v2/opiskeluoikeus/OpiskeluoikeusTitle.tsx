import React from 'react'
import { formatYearRange } from '../../date/date'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { nonNull } from '../../util/fp/arrays'
import { viimeisinOpiskelujaksonTila } from '../../util/schema'
import { uncapitalize } from '../../util/strings'
import { baseProps, BaseProps } from '../baseProps'
import { Lowercase } from '../texts/Lowercase'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeusTitleProps = BaseProps & {
  opiskeluoikeus: Opiskeluoikeus
  // Nämä propertyt ylikirjoittavat opiskeluoikeudesta oletuksena tulkittavat arvot:
  oppilaitos?: string
  koulutus?: string
}

const join = (...as: Array<string | undefined>) => as.filter(nonNull).join(', ')

export const OpiskeluoikeusTitle = (props: OpiskeluoikeusTitleProps) => {
  const oppilaitosJaKoulutus = join(
    props.oppilaitos || t(props.opiskeluoikeus.oppilaitos?.nimi),
    uncapitalize(
      props.koulutus || t(props.opiskeluoikeus.suoritukset[0]?.tyyppi.nimi)
    )
  )
  const aikaväliJaTila = join(
    formatYearRange(
      props.opiskeluoikeus.alkamispäivä,
      props.opiskeluoikeus.päättymispäivä
    ),
    t(viimeisinOpiskelujaksonTila(props.opiskeluoikeus.tila))
  )

  const oid: string | undefined = (props.opiskeluoikeus as any).oid

  return (
    <h3 {...baseProps(props, 'OpiskeluoikeusTitle')}>
      <span className="OpiskeluoikeusTitle__title">
        {oppilaitosJaKoulutus} (<Lowercase>{aikaväliJaTila}</Lowercase>)
      </span>
      {oid && (
        <span className="OpiskeluoikeusTitle__oid">
          <Trans>Opiskeluoikeuden oid</Trans>: {oid}
        </span>
      )}
      {/* TODO TOR-1692: Opiskeluoikeuden versiohistoria */}
    </h3>
  )
}
