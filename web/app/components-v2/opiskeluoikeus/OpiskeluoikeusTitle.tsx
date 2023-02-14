import React, { useCallback, useMemo, useState } from 'react'
import { isSuccess, useApiWithParams } from '../../api-fetch'
import { formatYearRange, ISO2FinnishDateTime } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { last, nonNull } from '../../util/fp/arrays'
import { fetchVersiohistoria } from '../../util/koskiApi'
import { viimeisinOpiskelujaksonTila } from '../../util/schema'
import { uncapitalize } from '../../util/strings'
import { currentQueryWith, parseQuery } from '../../util/url'
import { common, CommonProps, cx } from '../CommonProps'
import { PositionalPopup } from '../containers/PositionalPopup'
import { FlatButton } from '../controls/FlatButton'
import { Lowercase } from '../texts/Lowercase'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeusTitleProps = CommonProps<{
  opiskeluoikeus: Opiskeluoikeus
  // Nämä propertyt ylikirjoittavat opiskeluoikeudesta oletuksena tulkittavat arvot:
  oppilaitos?: string
  opiskeluoikeudenNimi?: string
}>

const join = (...as: Array<string | undefined>) => as.filter(nonNull).join(', ')

export const OpiskeluoikeusTitle = (props: OpiskeluoikeusTitleProps) => {
  const oppilaitosJaKoulutus = join(
    props.oppilaitos || t(props.opiskeluoikeus.oppilaitos?.nimi),
    uncapitalize(
      props.opiskeluoikeudenNimi ||
        t(props.opiskeluoikeus.suoritukset[0]?.tyyppi.nimi)
    )
  )
  const aikaväliJaTila = join(
    formatYearRange(
      props.opiskeluoikeus.alkamispäivä,
      props.opiskeluoikeus.päättymispäivä
    ),
    t(viimeisinOpiskelujaksonTila(props.opiskeluoikeus.tila)?.nimi)
  )

  const oid: string | undefined = (props.opiskeluoikeus as any).oid

  return (
    <h3 {...common(props, ['OpiskeluoikeusTitle', 'darkBackground'])}>
      <span className="OpiskeluoikeusTitle__title">
        {oppilaitosJaKoulutus} {'('}
        <Lowercase>{aikaväliJaTila}</Lowercase>
        {')'}
      </span>
      {oid && (
        <span className="OpiskeluoikeusTitle__oid">
          <Trans>{'Opiskeluoikeuden oid'}</Trans>
          {': '}
          {oid}
          <VersiohistoriaButton opiskeluoikeusOid={oid} />
        </span>
      )}
    </h3>
  )
}

type VersiohistoriaButtonProps = {
  opiskeluoikeusOid: string
}

const VersiohistoriaButton: React.FC<VersiohistoriaButtonProps> = (props) => {
  const [versiohistoriaVisible, setVersiohistoriaVisible] = useState(false)
  const showList = useCallback(() => setVersiohistoriaVisible(true), [])
  const hideList = useCallback(() => setVersiohistoriaVisible(false), [])

  return (
    <span className="VersiohistoriaButton">
      <FlatButton onClick={showList}>{t('Versiohistoria')}</FlatButton>
      {versiohistoriaVisible && (
        <PositionalPopup align="right" onDismiss={hideList}>
          <VersiohistoriaList opiskeluoikeusOid={props.opiskeluoikeusOid} />
        </PositionalPopup>
      )}
    </span>
  )
}

type VersiohistoriaListProps = {
  opiskeluoikeusOid: string
}

const VersiohistoriaList: React.FC<VersiohistoriaListProps> = (props) => {
  const historia = useApiWithParams(fetchVersiohistoria, [
    props.opiskeluoikeusOid
  ])

  const currentVersion = useMemo(() => {
    const v = parseQuery(window.location.search).versionumero
    return v
      ? parseInt(v)
      : isSuccess(historia)
      ? last(historia.data)?.versionumero
      : undefined
  }, [historia])

  return isSuccess(historia) ? (
    <ul className="VersiohistoriaList">
      {historia.data.map((versio) => (
        <li
          key={versio.versionumero}
          className={cx(
            'VersiohistoriaList__item',
            currentVersion === versio.versionumero &&
              'VersiohistoriaList__item--current'
          )}
        >
          <a
            href={currentQueryWith({
              opiskeluoikeus: props.opiskeluoikeusOid,
              versionumero: versio.versionumero
            })}
          >
            {`v${versio.versionumero}`} {ISO2FinnishDateTime(versio.aikaleima)}
          </a>
        </li>
      ))}
    </ul>
  ) : null
}
