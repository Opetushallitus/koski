import React from 'react'
import { TreeHook } from '../../appstate/tree'
import { TestIdRoot, TestIdText } from '../../appstate/useTestId'
import { useKansalainenTaiSuoritusjako } from '../../appstate/user'
import { formatYearRange, yearFromIsoDateString } from '../../date/date'
import { t } from '../../i18n/i18n'
import ChevronDownIcon from '../../icons/ChevronDownIcon'
import ChevronUpIcon from '../../icons/ChevronUpIcon'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { nonNull } from '../../util/fp/arrays'
import { viimeisinOpiskelujaksonTila } from '../../util/schema'
import { uncapitalize } from '../../util/strings'
import { CommonProps, common } from '../CommonProps'
import { VirkailijaOnly } from '../access/VirkailijaOnly'
import { Column, ColumnRow } from '../containers/Columns'
import { ExpandButton } from '../controls/ExpandButton'
import { Lowercase } from '../texts/Lowercase'
import { Trans } from '../texts/Trans'
import { VersiohistoriaButton } from './VersiohistoriaButton'

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
