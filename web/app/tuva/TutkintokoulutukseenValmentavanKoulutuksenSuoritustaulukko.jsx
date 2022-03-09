import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {tutkinnonOsaPrototypes} from '../ammatillinen/TutkinnonOsa'
import {TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditor} from './TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditor'
import {UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus} from './UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus'
import {
  ArvosanaColumn,
  ExpandAllRows,
  getLaajuusYksikkö,
  LaajuusColumn,
  SuoritusColumn
} from '../suoritus/SuoritustaulukkoCommon'
import {numberToString} from '../util/format'

export class TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko extends React.Component {
  render() {
    const {parentSuoritus, suorituksetModel, nestedLevel = 0} = this.props
    const context = parentSuoritus.context
    const suoritukset = modelItems(suorituksetModel) || []
    const maxNestedLevel = 2

    if (suoritukset.length === 0 && !context.edit || nestedLevel >= maxNestedLevel || suorituksetModel === undefined) {
      return null
    }

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({suoritukset, component: this})

    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel)
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProtos[0])
    const title = t('Osasuoritus')
    const paikallinenOsaSuoritusTitle = nestedLevel === 0 ? title : t('Valinnaisen koulutuksen osan paikallinen osasuoritus')

    const columns = [SuoritusColumn, LaajuusColumn, ArvosanaColumn]
      .filter(column => column.shouldShow({parentSuoritus, suoritukset, suorituksetModel, context}))

    return (
      <div className='suoritus-taulukko'>
        <table>
          {
            nestedLevel === 0 &&
            <ExpandAllRows allExpandedP={allExpandedP}
                           toggleExpandAll={toggleExpandAll}

            />
          }
          <tbody className='taulukko-headers'>
            <tr>
              {columns.map(column => column.renderHeader({laajuusYksikkö, suoritusTitle: paikallinenOsaSuoritusTitle}))}
            </tr>
          </tbody>
          {
            suoritukset.map((suoritus, i) => (
              <TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditor baret-lift
                                                                           key={i}
                                                                           model={suoritus}
                                                                           expanded={isExpandedP(suoritus)}
                                                                           onExpand={setExpanded(suoritus)}
                                                                           columns={columns}
                                                                           nestedLevel={nestedLevel + 1}
              />
            ))
          }
          {
            context.edit &&
            <SingleColumnRowTable colSpan={4}>
              <UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus suoritusPrototypes={suoritusProtos}
                                                                         setExpanded={setExpanded}
                                                                         suoritukset={suoritukset}

              />
            </SingleColumnRowTable>
          }
          {
            nestedLevel === 0 &&
            <SingleColumnRowTable className={'yhteislaajuus'}>
              <YhteensäSuoritettu suoritukset={suoritukset}
                                  laajuusYksikkö={laajuusYksikkö}
              />
            </SingleColumnRowTable>
          }
        </table>
      </div>
    )
  }
}

const YhteensäSuoritettu = ({suoritukset, laajuusYksikkö}) => {
  const laajuudetYhteensä = R.sum(R.map(item => modelData(item, 'koulutusmoduuli.laajuus.arvo') || 0, suoritukset))
  return (
    <div>
      <Text name="Yhteensä"/>
      {' '}
      <span className="laajuudet-yhteensä">{numberToString(laajuudetYhteensä)}</span>
      {' '}
      {laajuusYksikkö}
    </div>
  )
}

const SingleColumnRowTable = ({children, className = '', colSpan = 1}) => (
  <tbody className={className}>
  <tr>
    <td colSpan={colSpan}>
      {children}
    </td>
  </tr>
  </tbody>
)
