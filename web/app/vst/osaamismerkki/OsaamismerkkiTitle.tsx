import React from 'react'
import { TreeHook } from '../../appstate/tree'
import { TestIdRoot, TestIdText } from '../../appstate/useTestId'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import { CommonProps, common } from '../../components-v2/CommonProps'
import { VirkailijaOnly } from '../../components-v2/access/VirkailijaOnly'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { ExpandButton } from '../../components-v2/controls/ExpandButton'
import { VersiohistoriaButton } from '../../components-v2/opiskeluoikeus/VersiohistoriaButton'
import { Lowercase } from '../../components-v2/texts/Lowercase'
import { Trans } from '../../components-v2/texts/Trans'
import { formatYearRange, yearFromIsoDateString } from '../../date/date'
import { t } from '../../i18n/i18n'
import ChevronDownIcon from '../../icons/ChevronDownIcon'
import ChevronUpIcon from '../../icons/ChevronUpIcon'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { nonNull } from '../../util/fp/arrays'
import { viimeisinOpiskelujaksonTila } from '../../util/schema'
import { uncapitalize } from '../../util/strings'

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
    <h3
      {...common(props, [
        'OsaamismerkkiTitle',
        kansalainenTaiSuoritusjako && 'OsaamismerkkiTitle__kansalainen'
      ])}
    >
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
            {props.tree && (
              <div className="OsaamismerkkiTitle__expand">
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
