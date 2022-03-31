import React from 'baret'
import * as R from 'ramda'
import {
  footnoteDescriptions, footnotesForSuoritus, groupTitleForSuoritus, isToimintaAlueittain,
  isVuosiluokkaTaiPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle, pakollisetTitle, valinnaisetTitle,
  valmiitaSuorituksia,
  isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa,
  isPerusopetukseenValmistavanKoulutuksenSuoritus
} from './Perusopetus'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import {FootnoteDescriptions, FootnoteHint} from '../components/footnote'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {arvioituTaiVahvistettu, osasuoritukset} from '../suoritus/Suoritus'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {isKieliaine, isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {Editor} from '../editor/Editor'
import {expandableProperties} from './PerusopetuksenOppiaineRowEditor'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {isMobileAtom} from '../util/isMobileAtom'
import {KurssitListMobile} from '../kurssi/OmatTiedotKurssit'

export default ({model}) => {
  const oppiaineSuoritukset = modelItems(model, 'osasuoritukset')
  const footnotes = footnoteDescriptions(oppiaineSuoritukset)
  const showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (valmiitaSuorituksia(oppiaineSuoritukset) || isVuosiluokkaTaiPerusopetuksenOppimäärä(model))

  return showOppiaineet && (
    <div className='omattiedot-perusopetuksen-suoritukset'>
      <ArvosteluInfo model={model}/>
      <GroupedOppiaineet model={model} />
      <KäyttäytymisenArvio model={model}/>
      {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
    </div>
  )
}

const ArvosteluInfo = ({model}) => (
  <section className='arvostelu'>
    <h4><Text name={(isToimintaAlueittain(model) ? 'Toiminta-alueiden' : 'Oppiaineiden') + ' arvosanat'} /></h4>
    <Text name='Arvostelu 4-10, S (suoritettu) tai H (hylätty)'/>
  </section>
)

ArvosteluInfo.displayName = 'ArvosteluInfo'

const KäyttäytymisenArvio = ({model}) => {
  const käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  const shouldShow = käyttäytymisenArvioModel && modelData(käyttäytymisenArvioModel)
  return shouldShow ? (
      <section className='kayttaytymisen-arvio'>
        <h4><Text name="Käyttäytymisen arviointi"/></h4>
        <PropertiesEditor model={käyttäytymisenArvioModel} className='kansalainen'/>
      </section>
    ) : null
}

KäyttäytymisenArvio.displayName = 'KäyttäytymisenArvio'

const GroupedOppiaineet = ({model}) => {
  const groups = [pakollisetTitle, valinnaisetTitle]
  const groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))

  return groups.map(groupTitle => {
    const suoritukset = groupedSuoritukset[groupTitle]
    if (!suoritukset) return null

    return (
      <section className='suoritus-group' key={groupTitle}>
        {
          !isPerusopetukseenValmistavanKoulutuksenSuoritus(model) ?
            <h4><Text name={groupTitle}/></h4>
            : null
        }
        <Oppiainetaulukko model={model} suoritukset={suoritukset} showLaajuus={true}/>
      </section>
    )
  })
}

GroupedOppiaineet.displayName = 'GroupedOppiaineet'

const Oppiainetaulukko = ({model, suoritukset, showLaajuus}) => {
  const showArvosana = arvioituTaiVahvistettu(model) || !model.value.classes.includes('perusopetuksenoppimaaransuoritus')
  const filteredSuoritukset = isVuosiluokkaTaiPerusopetuksenOppimäärä(model)
    ? suoritukset
    : suoritukset.filter(s => arvioituTaiVahvistettu(s) || osasuoritukset(s).length)

  const valmistavanOpetuksenSuoritukset = filteredSuoritukset
    .filter(s => !isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(s))

  const muutSuoritukset = filteredSuoritukset
    .filter(s => isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(s))

  const perusopetukseenValmistavanListaus = (
    <tbody>
      <tr className="perusopetus-valmistava-oppiane-tyyppi"><Text name="Perusopetuksen valmistavan opetuksen opinnot"/></tr>
      <th className='oppiaine' scope='col'><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
      {showArvosana && <th className='arvosana' scope='col' style={{textAlign: 'center'}}><Text name='Arvosana'/></th>}
    {
      valmistavanOpetuksenSuoritukset.map(suoritus => (
        <OppiaineRow
          baret-lift
          key={suoritus.arrayKey}
          model={suoritus}
          showLaajuus={showLaajuus}
          showArvosana={showArvosana}
          isMobile={isMobileAtom}
          footnotes={footnotesForSuoritus(suoritus)}
        />
      ))
    }
     <tr className="perusopetus-valmistava-oppiane-tyyppi"><Text name="Perusopetuksen oppimäärään sisältyvät opinnot"/></tr>
     <th className='oppiaine' scope='col'><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
     {showArvosana && <th className='arvosana' scope='col' style={{textAlign: 'center'}}><Text name='Arvosana'/></th>}
    {
      muutSuoritukset.map(suoritus => (
        <OppiaineRow
          baret-lift
          key={suoritus.arrayKey}
          model={suoritus}
          showLaajuus={showLaajuus}
          showArvosana={showArvosana}
          isMobile={isMobileAtom}
          footnotes={footnotesForSuoritus(suoritus)}
        />
      ))
    }
    </tbody>
  )

  const perusopetuksenListaus = (
      <tbody>
      {
        filteredSuoritukset.map(suoritus => (
          <OppiaineRow
            baret-lift
            key={suoritus.arrayKey}
            model={suoritus}
            showLaajuus={showLaajuus}
            showArvosana={showArvosana}
            isMobile={isMobileAtom}
            footnotes={footnotesForSuoritus(suoritus)}
          />
        ))
      }
      </tbody>
    )

  return (
    <table className='suoritus-table'>
      <thead>
      {
      !isPerusopetukseenValmistavanKoulutuksenSuoritus(model) ?
        <tr>
          <th className='oppiaine' scope='col'><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
          {showArvosana && <th className='arvosana' scope='col' style={{textAlign: 'center'}}><Text name='Arvosana'/></th>}
        </tr>
        : null
      }
      </thead>
      {
        !isPerusopetukseenValmistavanKoulutuksenSuoritus(model) ?
          perusopetuksenListaus
          : perusopetukseenValmistavanListaus
      }
    </table>
  )
}

Oppiainetaulukko.displayName = 'Oppiainetaulukko'

class OppiaineRow extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      expanded: false
    }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand(e) {
    e.stopPropagation()
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {model, showLaajuus, showArvosana, isMobile, footnotes} = this.props
    const {expanded} = this.state

    const extraProperties = expandableProperties(model)
    const showExtraProperties = extraProperties.length > 0
    const hasLaajuus =  modelData(model, 'koulutusmoduuli.laajuus')
    const kurssit = osasuoritukset(model)

    const showKurssitRow = kurssit && kurssit.length > 0
    const showPropertiesRow = (showLaajuus && hasLaajuus) || showExtraProperties
    const expandable = showPropertiesRow || (isMobile && showKurssitRow)

    const oppiaine = modelLookup(model, 'koulutusmoduuli')

    const oppiaineRowClassName = 'oppiaine-row'
      + (expandable ? ' expandable' : '')
      + (expandable && expanded ? ' expanded' : '')

    const oppiaineClassName = `oppiaine ${isPaikallinen(oppiaine) ? 'paikallinen' : ''}`

    return [
      <tr className={oppiaineRowClassName} key='oppiaine-row' onClick={expandable ? this.toggleExpand : undefined}>
        <td className={oppiaineClassName}>
          {expandable && <span className='expand-icon' aria-hidden={true}>{(expanded ? '-' : '+')}</span>}
          {expandable
            ? <button className='inline-text-button' onClick={this.toggleExpand} aria-pressed={expanded}>{oppiaineTitle(oppiaine)}</button>
            : <span className='nimi'>{oppiaineTitle(oppiaine)}</span>}
        </td>
        {showArvosana && (
          <td className='arvosana'>
            <ArvosanaEditor model={model}/>
            {footnotes && footnotes.length > 0 &&
              footnotes.map(note => <FootnoteHint key={note.hint} title={note.title} hint={note.hint} />)}
          </td>
        )}
      </tr>,
      showPropertiesRow && expanded && (
        <tr className='properties-row' key='properties-row'>
          <td colSpan='2'>
            {(showLaajuus && hasLaajuus) && (
              <div className='properties kansalainen'>
                <table>
                  <tbody>
                  <tr className='property'>
                    <td className='label'><Text name='Laajuus'/></td>
                    <td className='value'><Editor model={model} path='koulutusmoduuli.laajuus'/></td>
                  </tr>
                  </tbody>
                </table>
              </div>
            )}
            {showExtraProperties && <PropertiesEditor className='kansalainen' properties={extraProperties} context={model.context}/>}
          </td>
        </tr>
      ),
      showKurssitRow && (
        isMobile
          ? (expandable && expanded && <tr className='kurssit-row-mobile' key='kurssit-row'><KurssitListMobile oppiaine={model}/></tr>)
          : (
              <tr className='kurssit-row' key='kurssit-row'>
                <td colSpan='2'>
                  <KurssitEditor model={model}/>
                </td>
              </tr>
            )
      )
    ]
  }
}

const oppiaineTitle = aine => {
  const kieliaine = isKieliaine(aine)
  const oppiaineenNimi = t(modelData(aine, 'tunniste.nimi'))
  const kielenNimi = kieliaine && t(modelData(aine, 'kieli.nimi'))
  return kieliaine ? `${oppiaineenNimi}, ${kielenNimi.toLowerCase()}` : oppiaineenNimi
}
