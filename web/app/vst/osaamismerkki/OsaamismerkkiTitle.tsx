import React, { useCallback, useMemo, useRef, useState } from 'react'
import {
  createLocalThenApiCache,
  isSuccess,
  useApiWithParams
} from '../../api-fetch'
import { TreeHook } from '../../appstate/tree'
import { TestIdLayer, TestIdRoot, TestIdText } from '../../appstate/useTestId'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import { CommonProps, common, cx } from '../../components-v2/CommonProps'
import { VirkailijaOnly } from '../../components-v2/access/VirkailijaOnly'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { PositionalPopup } from '../../components-v2/containers/PositionalPopup'
import { ExpandButton } from '../../components-v2/controls/ExpandButton'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { LinkButton } from '../../components-v2/controls/LinkButton'
import { Lowercase } from '../../components-v2/texts/Lowercase'
import { Trans } from '../../components-v2/texts/Trans'
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

export type OsaamismerkkiTitleProps = CommonProps<{
  opiskeluoikeus: Opiskeluoikeus
  kuva?: React.ReactNode
  tree?: Pick<TreeHook, 'isOpen' | 'toggle'>
  // Nämä propertyt ylikirjoittavat opiskeluoikeudesta oletuksena tulkittavat arvot:
  oppilaitos?: string
  opiskeluoikeudenNimi?: string
}>

const join = (...as: Array<string | undefined>) => as.filter(nonNull).join(', ')

const span = (defaultSize: number, small?: number) => ({
  default: defaultSize,
  small: small || defaultSize
})
const subtractSpan = (
  a: ReturnType<typeof span>,
  b: ReturnType<typeof span>
) => ({ default: a.default - b.default, small: a.small - b.small })

export const OsaamismerkkiTitle = (props: OsaamismerkkiTitleProps) => {
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

  const kuvaSpan = props.kuva ? span(2, 4) : span(0)
  const expandSpan = props.tree ? span(1) : span(0)
  const titleSpan = subtractSpan(span(12, 24), kuvaSpan)
  const oidSpan = subtractSpan(span(12, 24), expandSpan)

  const children: React.JSX.Element = (
    <h3 {...common(props, ['OsaamismerkkiTitle'])}>
      <ColumnRow>
        {props.kuva && (
          <Column className="OsaamismerkkiTitle__kuva" span={kuvaSpan}>
            <TestIdText id="kuva">{props.kuva}</TestIdText>
          </Column>
        )}
        <Column
          className="OsaamismerkkiTitle__title"
          span={titleSpan}
          align={{ default: 'left', small: 'left' }}
        >
          <TestIdText id="nimi">
            {otsikkoteksti}
            <br />
            {'('}
            <Lowercase>{aikaväliJaTila}</Lowercase>
            {')'}
          </TestIdText>
        </Column>

        {oid && (
          <Column
            className="OsaamismerkkiTitle__oid"
            span={oidSpan}
            align={{ default: 'right', small: 'left' }}
            start={{ default: 0, small: kuvaSpan.small }}
          >
            <TestIdText id="oid">
              <Trans>{'Opiskeluoikeuden oid'}</Trans>
              {': '}
              {oid}
            </TestIdText>
            <VirkailijaOnly>
              <VersiohistoriaButton opiskeluoikeusOid={oid} />
            </VirkailijaOnly>
          </Column>
        )}

        {props.tree && (
          <Column className="OsaamismerkkiTitle__expand" span={expandSpan}>
            {props.tree.isOpen ? <ChevronUpIcon /> : <ChevronDownIcon />}
            {/* <ExpandButtonIcon expanded={props.tree.isOpen} /> */}
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
