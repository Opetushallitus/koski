import React, { useCallback, useMemo, useRef, useState } from 'react'
import {
  createLocalThenApiCache,
  isSuccess,
  useApiWithParams
} from '../../api-fetch'
import { TreeHook } from '../../appstate/tree'
import { TestIdLayer, TestIdRoot, TestIdText } from '../../appstate/useTestId'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import {
  ISO2FinnishDateTime,
  formatYearRange,
  yearFromIsoDateString
} from '../../date/date'
import { t } from '../../i18n/i18n'
import ChevronDownIcon from '../../icons/ChevronDownIcon'
import ChevronUpIcon from '../../icons/ChevronUpIcon'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { last, nonNull } from '../../util/fp/arrays'
import { fetchVersiohistoria } from '../../util/koskiApi'
import { viimeisinOpiskelujaksonTila } from '../../util/schema'
import { uncapitalize } from '../../util/strings'
import { currentQueryWith, parseQuery } from '../../util/url'
import { CommonProps, common, cx } from '../CommonProps'
import { VirkailijaOnly } from '../access/VirkailijaOnly'
import { Column, ColumnRow } from '../containers/Columns'
import { PositionalPopup } from '../containers/PositionalPopup'
import { ExpandButton } from '../controls/ExpandButton'
import { FlatButton } from '../controls/FlatButton'
import { LinkButton } from '../controls/LinkButton'
import { Lowercase } from '../texts/Lowercase'
import { Trans } from '../texts/Trans'

export type OpiskeluoikeusTitleProps = CommonProps<{
  opiskeluoikeus: Opiskeluoikeus
  tree?: Pick<TreeHook, 'isOpen' | 'toggle'>
  // Nämä propertyt ylikirjoittavat opiskeluoikeudesta oletuksena tulkittavat arvot:
  oppilaitos?: string
  opiskeluoikeudenNimi?: string
}>

const join = (...as: Array<string | undefined>) => as.filter(nonNull).join(', ')

export const OpiskeluoikeusTitle = (props: OpiskeluoikeusTitleProps) => {
  const koulutuksenNimi =
    props.opiskeluoikeudenNimi ||
    t(props.opiskeluoikeus.suoritukset[0]?.tyyppi.nimi)

  const kansalainenTaiSuoritusjako = useKansalainenTaiSuoritusjako()
  const otsikkoteksti = kansalainenTaiSuoritusjako
    ? koulutuksenNimi
    : join(
        props.oppilaitos || t(props.opiskeluoikeus.oppilaitos?.nimi),
        uncapitalize(koulutuksenNimi)
      )

  const vainYhdenPäättävänTilanVuosi =
    props.opiskeluoikeus.päättymispäivä &&
    props.opiskeluoikeus.tila.opiskeluoikeusjaksot.length <= 1 &&
    yearFromIsoDateString(props.opiskeluoikeus.päättymispäivä)

  const vuodet =
    vainYhdenPäättävänTilanVuosi ||
    formatYearRange(
      props.opiskeluoikeus.alkamispäivä,
      props.opiskeluoikeus.päättymispäivä
    )

  const aikaväliJaTila = join(
    vuodet,
    t(viimeisinOpiskelujaksonTila(props.opiskeluoikeus.tila)?.nimi)
  )

  const oid: string | undefined = (props.opiskeluoikeus as any).oid

  const titleSpan = 12
  const oidSpan = 12

  const children: React.JSX.Element = (
    <h3
      {...common(props, [
        'OpiskeluoikeusTitle',
        kansalainenTaiSuoritusjako && 'OpiskeluoikeusTitle__kansalainen'
      ])}
    >
      <ColumnRow>
        <Column
          className="OpiskeluoikeusTitle__title"
          span={{ default: titleSpan, small: 2 * titleSpan }}
          align={{ default: 'left', small: 'left' }}
        >
          <TestIdText id="nimi">
            {otsikkoteksti} {'('}
            <Lowercase>{aikaväliJaTila}</Lowercase>
            {')'}
          </TestIdText>
        </Column>

        {oid && (
          <Column
            className="OpiskeluoikeusTitle__oid"
            span={{ default: oidSpan, small: 2 * oidSpan }}
            align={{ default: 'right', small: 'left' }}
          >
            <TestIdText id="oid">
              <Trans>{'Opiskeluoikeuden oid'}</Trans>
              {': '}
              {oid}
            </TestIdText>
            <VirkailijaOnly>
              <VersiohistoriaButton opiskeluoikeusOid={oid} />
            </VirkailijaOnly>
            {props.tree && (
              <div className="OpiskeluoikeusTitle__expand">
                {props.tree.isOpen ? <ChevronUpIcon /> : <ChevronDownIcon />}
              </div>
            )}
          </Column>
        )}
      </ColumnRow>
    </h3>
  )

  return (
    <TestIdRoot id="opiskeluoikeus">
      {props.tree ? (
        <ExpandButton
          expanded={props.tree.isOpen}
          onChange={props.tree.toggle}
          label={t('Avaa opiskeluoikeus')}
        >
          {children}
        </ExpandButton>
      ) : (
        <>{children}</>
      )}
    </TestIdRoot>
  )
}

type VersiohistoriaButtonProps = CommonProps<{
  opiskeluoikeusOid: string
}>

const VersiohistoriaButton: React.FC<VersiohistoriaButtonProps> = (props) => {
  const buttonRef = useRef(null)
  const [versiohistoriaVisible, setVersiohistoriaVisible] = useState(false)
  const toggleList = useCallback(
    () => setVersiohistoriaVisible(!versiohistoriaVisible),
    [versiohistoriaVisible]
  )
  const hideList = useCallback(() => setVersiohistoriaVisible(false), [])

  return (
    <TestIdLayer id="versiohistoria">
      <span className="VersiohistoriaButton" ref={buttonRef}>
        <FlatButton
          onClick={toggleList}
          aria-haspopup="menu"
          aria-expanded={versiohistoriaVisible}
          testId="button"
        >
          {t('Versiohistoria')}
        </FlatButton>
        <PositionalPopup
          align="right"
          onDismiss={hideList}
          open={versiohistoriaVisible}
          parentRef={buttonRef}
        >
          <VersiohistoriaList
            opiskeluoikeusOid={props.opiskeluoikeusOid}
            open={versiohistoriaVisible}
          />
        </PositionalPopup>
      </span>
    </TestIdLayer>
  )
}

type VersiohistoriaListProps = CommonProps<{
  opiskeluoikeusOid: string
  open: boolean
}>

const versiolistaCache = createLocalThenApiCache(fetchVersiohistoria)

const VersiohistoriaList: React.FC<VersiohistoriaListProps> = (props) => {
  const historia = useApiWithParams(
    fetchVersiohistoria,
    props.open ? [props.opiskeluoikeusOid] : undefined,
    versiolistaCache
  )

  const currentVersion = useMemo(() => {
    const v = parseQuery(window.location.search).versionumero
    return v
      ? parseInt(v)
      : isSuccess(historia)
        ? last(historia.data)?.versionumero
        : undefined
  }, [historia])

  return isSuccess(historia) ? (
    <TestIdLayer id="list">
      <ul className="VersiohistoriaList" role="navigation">
        {historia.data.map((versio) => (
          <li
            key={versio.versionumero}
            className={cx(
              'VersiohistoriaList__item',
              currentVersion === versio.versionumero &&
                'VersiohistoriaList__item--current'
            )}
          >
            <LinkButton
              href={currentQueryWith({
                opiskeluoikeus: props.opiskeluoikeusOid,
                versionumero: versio.versionumero
              })}
              testId={versio.versionumero}
            >
              {`v${versio.versionumero}`}{' '}
              {ISO2FinnishDateTime(versio.aikaleima)}
            </LinkButton>
          </li>
        ))}
      </ul>
    </TestIdLayer>
  ) : null
}
