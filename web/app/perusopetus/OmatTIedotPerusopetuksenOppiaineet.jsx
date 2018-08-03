import React from 'react'
import * as R from 'ramda'
import {
  footnoteDescriptions, footnotesForSuoritus, groupTitleForSuoritus, isToimintaAlueittain,
  isVuosiluokkaTaiPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle, pakollisetTitle, valinnaisetTitle,
  valmiitaSuorituksia
} from './Perusopetus'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import {FootnoteDescriptions, FootnoteHint} from '../components/footnote'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {arvioituTaiVahvistettu, osasuoritukset} from '../suoritus/Suoritus'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {isKieliaine} from '../suoritus/Koulutusmoduuli'

export default ({model}) => {
  // Tarviiko kontekstia?   model = addContext(model, { suoritus: model })

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

const GroupedOppiaineet = ({model}) => {
  const groups = [pakollisetTitle, valinnaisetTitle]
  const groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))

  return groups.map(groupTitle => {
    const suoritukset = groupedSuoritukset[groupTitle]
    const showLaajuus = groupTitle === valinnaisetTitle
    if (!suoritukset) return null

    return (
      <section className='suoritus-group' key={groupTitle}>
        <h4><Text name={groupTitle}/></h4>
        <Oppiainetaulukko model={model} suoritukset={suoritukset} showLaajuus={showLaajuus}/>
      </section>
    )
  })
}

const Oppiainetaulukko = ({model, suoritukset, showLaajuus}) => {
  const showArvosana = arvioituTaiVahvistettu(model) || !model.value.classes.includes('perusopetuksenoppimaaransuoritus') // TODO: Selvitä mikä logiikka täs oli
  const filteredSuoritukset = isVuosiluokkaTaiPerusopetuksenOppimäärä(model)
    ? suoritukset
    : suoritukset.filter(s => arvioituTaiVahvistettu(s) || osasuoritukset(s).length)

  return (
    <table className='suoritus-table'>
      <thead>
      <tr>
        <th className='oppiaine' scope='col'><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
        {showArvosana && <th className='arvosana' scope='col'><Text name='Arvosana'/></th>}
      </tr>
      </thead>
      <tbody>
      {
        filteredSuoritukset.map(suoritus => (
          <OppiaineRow
            key={suoritus.arrayKey}
            model={suoritus}
            showLaajuus={showLaajuus}
            showArvosana={showArvosana}
            footnotes={footnotesForSuoritus(suoritus)}
          />
        ))
      }
      </tbody>
    </table>
  )
}

class OppiaineRow extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      expanded: false
    }
  }

  render() {
    const {model, showLaajuus, showArvosana, footnotes} = this.props
    const {expanded} = this.state

    const oppiaine = modelLookup(model, 'koulutusmoduuli')

    return (
      <tr>
        <td className='oppiaine'>
          {oppiaineTitle(oppiaine)}
        </td>
        {showArvosana && <td className='arvosana'>
          <ArvosanaEditor model={model}/>
          {footnotes && footnotes.length > 0 && (
            <div className="footnotes-container">
              {footnotes.map(note => <FootnoteHint key={note.hint} title={note.title} hint={note.hint} />)}
            </div>
          )}
        </td>}
      </tr>
    )
  }
}

const oppiaineTitle = aine => {
  const kieliaine = isKieliaine(aine)
  const oppiaineenNimi = t(modelData(aine, 'tunniste.nimi'))
  const kielenNimi = kieliaine && t(modelData(aine, 'kieli.nimi'))
  return kieliaine ? `${oppiaineenNimi}, ${kielenNimi.toLowerCase()}` : oppiaineenNimi
}
