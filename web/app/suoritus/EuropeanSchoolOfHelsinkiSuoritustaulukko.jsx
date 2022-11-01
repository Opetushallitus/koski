import React from 'baret'
import { modelItems } from '../editor/EditorModel'
import { accumulateExpandedState } from '../editor/ExpandableItems'
import { suoritusValmis } from './Suoritus'
import { fetchLaajuudet, YhteensäSuoritettu } from './YhteensaSuoritettu'
import {
  ArvosanaColumn,
  getLaajuusYksikkö,
  groupSuoritukset,
  SuoritusColumn,
  LaajuusColumn,
  suoritusProperties,
  ExpandAllRows
} from './SuoritustaulukkoCommon'
import {
  selectOsasuoritusPrototype,
  osasuoritusPrototypes
} from '../esh/Osasuoritus'
import { EuropeanSchoolOfHelsinkiOsasuoritusEditor } from '../esh/EuropeanSchoolOfHelsinkiOsasuoritusEditor'
import UusiOsasuoritus from '../esh/UusiOsasuoritus'

export class EuropeanSchoolOfHelsinkiSuoritustaulukko extends React.Component {
  render() {
    let { parentSuoritus, suorituksetModel, nestedLevel = 0 } = this.props
    const context = suorituksetModel.context
    const suoritukset = modelItems(suorituksetModel) || []
    parentSuoritus = parentSuoritus || context.suoritus

    console.log('suorituksetModel', suorituksetModel)
    console.log('ModelItems', modelItems(suorituksetModel))

    if (suoritukset.length === 0 && !context.edit) {
      return null
    }

    const { isExpandedP, allExpandedP, toggleExpandAll, setExpanded } =
      accumulateExpandedState({
        suoritukset,
        filter: (s) => suoritusProperties(s).length > 0,
        component: this
      })

    const suoritusProtos = osasuoritusPrototypes(suorituksetModel)
    const suoritusProto = context.edit
      ? selectOsasuoritusPrototype(suoritusProtos)
      : suoritukset[0]

    const groupsP = groupSuoritukset(
      parentSuoritus,
      suoritukset,
      context,
      suoritusProto
    )

    const laajuusYksikkö = getLaajuusYksikkö(suoritusProto)
    const showExpandAll = suoritukset.some(
      (s) => suoritusProperties(s).length > 0
    )
    const showColumns = !(nestedLevel > 0 && suoritukset.length === 0)
    const canAddNewOsasuoritus = context.edit
    const showLaajuusYhteensä = nestedLevel === 0

    const columns = [SuoritusColumn, LaajuusColumn, ArvosanaColumn].filter(
      (column) =>
        column.shouldShow({
          parentSuoritus,
          suorituksetModel,
          suoritukset,
          context
        })
    )

    return (
      (suoritukset.length > 0 || context.edit) && (
        <div className="suoritus-taulukko">
          <table>
            {showExpandAll && (
              <ExpandAllRows
                allExpandedP={allExpandedP}
                toggleExpandAll={toggleExpandAll}
              />
            )}
            {groupsP.map((groups) =>
              groups.groupIds.map((groupId, i) => {
                const suorituksetForThisGroup = groups.grouped[groupId] || []
                console.log(
                  `suorituksetForThisGroup_${groupId}`,
                  suorituksetForThisGroup
                )
                const groupTitles = groups.groupTitles
                return (
                  <React.Fragment key={'group-fragment-' + i}>
                    <tbody
                      key={'group-' + i}
                      className={`group-header ${groupId}`}
                    >
                      <tr>
                        {showColumns &&
                          columns.map((column) => {
                            const suoritusTitle = groupTitles[groupId]
                            return column.renderHeader({
                              laajuusYksikkö,
                              suoritusTitle
                            })
                          })}
                      </tr>
                    </tbody>
                    {suorituksetForThisGroup.map((suoritus, j) => (
                      <EuropeanSchoolOfHelsinkiOsasuoritusEditor
                        baret-lift
                        key={i * 100 + j}
                        model={suoritus}
                        expanded={isExpandedP(suoritus)}
                        onExpand={setExpanded(suoritus)}
                        groupId={groupId}
                        columns={columns}
                        nestedLevel={nestedLevel + 1}
                      />
                    ))}
                    {canAddNewOsasuoritus && (
                      <SingleColumnRowTable
                        key={'group-' + i + '-new'}
                        className={'uusi-tutkinnon-osa ' + groupId}
                        colSpan={4}
                      >
                        <UusiOsasuoritus
                          suoritus={parentSuoritus}
                          suoritusPrototypes={suoritusProtos}
                          suorituksetModel={suorituksetModel}
                          groupId={groupId}
                          setExpanded={setExpanded}
                          groupTitles={groupTitles}
                        />
                      </SingleColumnRowTable>
                    )}
                    {showLaajuusYhteensä && (
                      <SingleColumnRowTable
                        key={'group- ' + i + '-footer'}
                        className="yhteensä"
                      >
                        <YhteensäSuoritettu
                          suoritukset={suorituksetForThisGroup}
                          laajuusP={fetchLaajuudet(
                            parentSuoritus,
                            groups.groupIds
                          ).map((l) => l[groupId])}
                          laajuusYksikkö={laajuusYksikkö}
                        />
                      </SingleColumnRowTable>
                    )}
                  </React.Fragment>
                )
              })
            )}
          </table>
        </div>
      )
    )
  }
}

const SingleColumnRowTable = ({ className, children, colSpan = 1 }) => (
  <tbody className={className}>
    <tr>
      <td colSpan={colSpan}>{children}</td>
    </tr>
  </tbody>
)

export const suorituksenTilaSymbol = (suoritus) =>
  // TODO: HTML Unicode käyttöön
  suoritusValmis(suoritus) ? '' : ''
