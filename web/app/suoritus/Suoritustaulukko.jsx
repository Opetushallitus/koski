import React from 'baret'
import {modelData} from '../editor/EditorModel.js'
import {modelItems} from '../editor/EditorModel'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import {suoritusValmis} from './Suoritus'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {fetchLaajuudet, YhteensäSuoritettu} from './YhteensaSuoritettu'
import UusiTutkinnonOsa from '../ammatillinen/UusiTutkinnonOsa'
import {
  isValinnanMahdollisuus,
  isVälisuoritus,
  selectTutkinnonOsanSuoritusPrototype,
  tutkinnonOsaPrototypes
} from '../ammatillinen/TutkinnonOsa'
import {
  ArvosanaColumn,
  getLaajuusYksikkö,
  groupSuoritukset,
  isAmmatillinentutkinto,
  isNäyttötutkintoonValmistava,
  isYlioppilastutkinto,
  SuoritusColumn,
  KoepisteetColumn,
  LaajuusColumn,
  suoritusProperties,
  TutkintokertaColumn, ExpandAllRows
} from './SuoritustaulukkoCommon'
import {UusiTutkinnonOsaMuuAmmatillinen} from '../muuammatillinen/UusiTutkinnonOsaMuuAmmatillinen'
import {isMuutaAmmatillistaPäätasonSuoritus} from '../muuammatillinen/MuuAmmatillinen'
import {TutkinnonOsanSuoritusEditor} from '../ammatillinen/TutkinnonOsanSuoritusEditor'

const MAX_NESTED_LEVEL = 2

export class Suoritustaulukko extends React.Component {
  render() {
    let {parentSuoritus, suorituksetModel, nestedLevel = 0} = this.props
    const context = suorituksetModel.context
    const suoritukset = modelItems(suorituksetModel) || []
    parentSuoritus = parentSuoritus || context.suoritus

    if (suoritukset.length === 0 && !context.edit) {
      return null
    }

    const {
      isExpandedP,
      allExpandedP,
      toggleExpandAll,
      setExpanded
    } = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0 || isVälisuoritus(s),
      component: this
    })

    const vaatiiSuoritustavan = !modelData(parentSuoritus, 'suoritustapa') && context.edit && isAmmatillinentutkinto(parentSuoritus)
    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel)
    const suoritusProto = context.edit ? selectTutkinnonOsanSuoritusPrototype(suoritusProtos) : suoritukset[0]

    const groupsP = groupSuoritukset(parentSuoritus, suoritukset, context, suoritusProto)

    const samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProto)
    const showTila = !isNäyttötutkintoonValmistava(parentSuoritus)
    const showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)
    const showColumns = !(nestedLevel > 0 && suoritukset.length === 0)
    const canAddNewOsasuoritus = context.edit && nestedLevel < MAX_NESTED_LEVEL
    const showLaajuusYhteensä = nestedLevel === 0 && !isNäyttötutkintoonValmistava(parentSuoritus) && !isYlioppilastutkinto(parentSuoritus)

    const columns = [
      TutkintokertaColumn,
      SuoritusColumn,
      LaajuusColumn,
      KoepisteetColumn,
      ArvosanaColumn
    ].filter(column => column.shouldShow({parentSuoritus, suorituksetModel, suoritukset, context}))


    const UusiTutkinnonOsaComponent = isMuutaAmmatillistaPäätasonSuoritus(parentSuoritus)
      ? UusiTutkinnonOsaMuuAmmatillinen
      : UusiTutkinnonOsa

    return vaatiiSuoritustavan
      ? <Text name="Valitse ensin tutkinnon suoritustapa" />
      : (suoritukset.length > 0 || context.edit) && (
        <div className="suoritus-taulukko">
          <table>
            {
              showExpandAll &&
              <ExpandAllRows allExpandedP={allExpandedP}
                             toggleExpandAll={toggleExpandAll}
              />
            }
            {
              groupsP.map(groups => groups.groupIds.map((groupId, i) => {
                const suorituksetForThisGroup = (groups.grouped[groupId] || [])
                const groupTitles = groups.groupTitles
                return (
                  <React.Fragment key={'group-fragment-' + i}>
                    <tbody key={'group-' + i} className={`group-header ${groupId}`}>
                    <tr>
                      {
                        showColumns && columns.map(column => {
                          const suoritusTitle = isValinnanMahdollisuus(parentSuoritus) ? t('Osasuoritus') : groupTitles[groupId]
                          return column.renderHeader({laajuusYksikkö, suoritusTitle})
                        })
                      }
                    </tr>
                    </tbody>
                    {
                      suorituksetForThisGroup.map((suoritus, j) => (
                        <TutkinnonOsanSuoritusEditor baret-lift
                                                     key={i * 100 + j}
                                                     model={suoritus}
                                                     showScope={!samaLaajuusYksikkö}
                                                     showTila={showTila}
                                                     expanded={isExpandedP(suoritus)}
                                                     onExpand={setExpanded(suoritus)}
                                                     groupId={groupId}
                                                     columns={columns}
                                                     nestedLevel={nestedLevel + 1}
                        />
                      ))
                    }
                    {
                      canAddNewOsasuoritus &&
                      <SingleColumnRowTable key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId} colSpan={4}>
                        <UusiTutkinnonOsaComponent suoritus={parentSuoritus}
                                                   suoritusPrototypes={suoritusProtos}
                                                   suorituksetModel={suorituksetModel}
                                                   groupId={groupId}
                                                   setExpanded={setExpanded}
                                                   groupTitles={groupTitles}

                         />
                      </SingleColumnRowTable>
                    }
                    {
                      showLaajuusYhteensä &&
                      <SingleColumnRowTable key={'group- '+ i + '-footer'} className="yhteensä">
                        <YhteensäSuoritettu suoritukset={suorituksetForThisGroup}
                                            laajuusP={fetchLaajuudet(parentSuoritus, groups.groupIds).map(l => l[groupId])}
                                            laajuusYksikkö={laajuusYksikkö}
                        />
                      </SingleColumnRowTable>
                    }
                  </React.Fragment>
                )
              }
              ))
            }
          </table>
        </div>
    )
  }
}

const SingleColumnRowTable = ({className, children, colSpan = 1}) => (
  <tbody className={className}>
  <tr>
    <td colSpan={colSpan}>
      {children}
    </td>
  </tr>
  </tbody>
)

SingleColumnRowTable.displayName = 'SingleColumnRowTable'

export const suorituksenTilaSymbol = (suoritus) => isValinnanMahdollisuus(suoritus)
  ? ''
  : suoritusValmis(suoritus) ? '' : ''
