import React, { ReactNode } from 'baret'
import * as R from 'ramda'
import { tutkinnonOsaPrototypes } from '../ammatillinen/TutkinnonOsa'
import { modelData, modelItems } from '../editor/EditorModel'
import { accumulateExpandedState } from '../editor/ExpandableItems'
import { t } from '../i18n/i18n'
import Text from '../i18n/Text'
import {
  ArvosanaColumn,
  ExpandAllRows,
  getLaajuusYksikkö,
  LaajuusColumn,
  SuoritusColumn,
  TaitotasoColumn
} from '../suoritus/SuoritustaulukkoCommon'
import { Contextualized } from '../types/EditorModelContext'
import { EditorModel, ObjectModel } from '../types/EditorModels'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import { numberToString } from '../util/format'
import { MuuKuinSäänneltyOsasuoritusEditor } from './MuuKuinSäänneltyOsasuoritusEditor'
import { UusiMuuKuinSäänneltyOsasuoritus } from './UusiMuuKuinSäänneltyOsasuoritus'

const MAX_NESTED_LEVEL = 2

export type MuuKuinSäänneltySuoritustaulukkoProps = {
  parentSuoritus: OsasuoritusEditorModel
  suorituksetModel?: OsasuoritusEditorModel
  nestedLevel?: number
}

export class MuuKuinSäänneltySuoritustaulukko extends React.Component<MuuKuinSäänneltySuoritustaulukkoProps> {
  render() {
    const { parentSuoritus, suorituksetModel, nestedLevel = 0 } = this.props
    const context = parentSuoritus.context
    const suoritukset = modelItems(suorituksetModel) || []

    if (
      (suoritukset.length === 0 && !context.edit) ||
      nestedLevel >= MAX_NESTED_LEVEL ||
      suorituksetModel === undefined
    ) {
      return null
    }

    const { isExpandedP, allExpandedP, toggleExpandAll, setExpanded } =
      accumulateExpandedState({ suoritukset, component: this })

    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel)
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProtos[0])

    const suoritusTitle = nestedLevel === 0 ? t('Koulutus') : t('Osasuoritus')

    const columns = [
      SuoritusColumn,
      LaajuusColumn,
      ArvosanaColumn,
      TaitotasoColumn
    ].filter((column) =>
      column.shouldShow({
        parentSuoritus,
        suoritukset,
        suorituksetModel,
        context
      })
    )

    return (
      <div className="suoritus-taulukko">
        <table>
          {nestedLevel === 0 && (
            <ExpandAllRows
              allExpandedP={allExpandedP}
              // @ts-expect-error
              toggleExpandAll={toggleExpandAll}
            />
          )}
          <tbody className="taulukko-headers">
            <tr>
              {columns.map(
                (
                  column // @ts-expect-error SuoritustaulukkoCommon
                ) => column.renderHeader({ laajuusYksikkö, suoritusTitle })
              )}
            </tr>
          </tbody>
          {suoritukset.map((suoritus, i) => (
            <MuuKuinSäänneltyOsasuoritusEditor
              baret-lift
              key={i}
              model={suoritus}
              expanded={isExpandedP(suoritus)}
              onExpand={setExpanded(suoritus)}
              columns={columns}
              nestedLevel={nestedLevel + 1}
            />
          ))}
          {context.edit && (
            <SingleColumnRowTable colSpan={4}>
              <UusiMuuKuinSäänneltyOsasuoritus
                // @ts-expect-error TutkinnonOsa
                suoritusPrototypes={
                  suoritusProtos as Array<ObjectModel & Contextualized>
                }
                setExpanded={setExpanded}
              />
            </SingleColumnRowTable>
          )}
          {nestedLevel === 0 && (
            <SingleColumnRowTable className={'yhteislaajuus'}>
              <YhteensäSuoritettu
                suoritukset={suoritukset}
                // @ts-expect-error YhteensäSuoritettu
                laajuusYksikkö={laajuusYksikkö}
              />
            </SingleColumnRowTable>
          )}
        </table>
      </div>
    )
  }
}

type YhteensäSuoritettuProps = {
  suoritukset: EditorModel[]
  laajuusYksikkö: string
}

const YhteensäSuoritettu = ({
  suoritukset,
  laajuusYksikkö
}: YhteensäSuoritettuProps) => {
  const laajuudetYhteensä = R.sum(
    R.map(
      (item) => modelData(item, 'koulutusmoduuli.laajuus.arvo') || 0,
      suoritukset
    )
  )
  return (
    <div>
      {/* @ts-expect-error TODO: Tyypitä Text */}
      <Text name="Yhteensä" />{' '}
      <span className="laajuudet-yhteensä">
        {numberToString(laajuudetYhteensä)}
      </span>{' '}
      {laajuusYksikkö}
    </div>
  )
}

type SingleColumnRowTableProps = {
  children?: ReactNode
  className?: string
  colSpan?: number
}

const SingleColumnRowTable = ({
  children,
  className = '',
  colSpan = 1
}: SingleColumnRowTableProps) => (
  <tbody className={className}>
    <tr>
      <td colSpan={colSpan}>{children}</td>
    </tr>
  </tbody>
)
