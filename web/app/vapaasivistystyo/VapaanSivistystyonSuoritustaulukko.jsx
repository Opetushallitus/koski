import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {tutkinnonOsaPrototypes} from '../ammatillinen/TutkinnonOsa'
import {UusiVapaanSivistystyonOsasuoritus} from '../vapaasivistystyo/UusiVapaanSivistystyonOsasuoritus'
import {VapaanSivistystyonOsasuoritusEditor} from './VapaanSivistystyonOsasuoritusEditor'
import {
  ArvosanaColumn,
  ExpandAllRows,
  getLaajuusYksikkö,
  LaajuusColumn,
  SuoritusColumn,
  TaitotasoColmn
} from '../suoritus/SuoritustaulukkoCommon'
import {numberToString} from '../util/format'

const MAX_NESTED_LEVEL_VAPAATAVOITTEINEN = 3
const MAX_NESTED_LEVEL_MUUT = 2

export class VapaanSivistystyonSuoritustaulukko extends React.Component {
  render() {
    const {parentSuoritus, suorituksetModel, nestedLevel = 0} = this.props
    const context = parentSuoritus.context
    const suoritukset = modelItems(suorituksetModel) || []
    const parentOneOf = (...classes) => classes.some(c => parentSuoritus.value.classes.includes(c))
    const maxNestedLevel = parentOneOf('vapaansivistystyonvapaatavoitteisenkoulutuksenosasuorituksensuoritus') ? MAX_NESTED_LEVEL_VAPAATAVOITTEINEN : MAX_NESTED_LEVEL_MUUT

    if (suoritukset.length === 0 && !context.edit || nestedLevel >= maxNestedLevel || suorituksetModel === undefined) {
      return null
    }

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({suoritukset, component: this})

    const suoritusProtos = tutkinnonOsaPrototypes(suorituksetModel)
    const laajuusYksikkö = getLaajuusYksikkö(suoritusProtos[0])
    const osaAlueTitle = () => {
      if (parentOneOf('oppivelvollisillesuunnattuvapaansivistystyonkoulutuksensuoritus')) {
        return (t('Osaamiskokonaisuus'))
      } else if (parentOneOf('vapaansivistystyonvapaatavoitteisenkoulutuksensuoritus')) {
        return (t('Koulutus'))
      } else {
       return t('Osa-alue')
      }
    }
    const suoritusTitle = nestedLevel === 0 ? osaAlueTitle() : t('Opintokokonaisuus')

    const columns = [SuoritusColumn, LaajuusColumn, ArvosanaColumn, TaitotasoColmn]
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
              {columns.map(column => column.renderHeader({laajuusYksikkö, suoritusTitle}))}
            </tr>
          </tbody>
          {
            suoritukset.map((suoritus, i) => (
              <VapaanSivistystyonOsasuoritusEditor baret-lift
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
              <UusiVapaanSivistystyonOsasuoritus suoritusPrototypes={suoritusProtos}
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

YhteensäSuoritettu.displayName = 'YhteensäSuoritettu'

const SingleColumnRowTable = ({children, className = '', colSpan = 1}) => (
  <tbody className={className}>
  <tr>
    <td colSpan={colSpan}>
      {children}
    </td>
  </tr>
  </tbody>
)

SingleColumnRowTable.displayName = 'SingleColumnRowTable'
