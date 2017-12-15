import React from 'baret'
import {Editor} from './Editor'
import {PropertyEditor} from './PropertyEditor'
import {wrapOptional} from './EditorModel'
import R from 'ramda'
import {
  addContext,
  contextualizeSubModel,
  ensureArrayKey,
  findModelProperty,
  modelData,
  modelItems,
  modelLookup,
  modelSet,
  modelSetValue,
  oneOfPrototypes,
  pushModel
} from './EditorModel'
import {arvioituTaiVahvistettu, osasuoritukset} from './Suoritus'
import {UusiPerusopetuksenOppiaineDropdown} from './UusiPerusopetuksenOppiaineDropdown'
import {accumulateExpandedState} from './ExpandableItems'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {isToimintaAlueittain, isYsiluokka, jääLuokalle, luokkaAste, luokkaAsteenOsasuoritukset} from './Perusopetus'
import {expandableProperties, PerusopetuksenOppiaineRowEditor} from './PerusopetuksenOppiaineRowEditor'

var pakollisetTitle = 'Pakolliset oppiaineet'
var valinnaisetTitle = 'Valinnaiset oppiaineet'
let groupTitleForSuoritus = suoritus => modelData(suoritus).koulutusmoduuli.pakollinen ? pakollisetTitle : valinnaisetTitle

export const PerusopetuksenOppiaineetEditor = ({model}) => {
  model = addContext(model, { suoritus: model })
  let oppiaineSuoritukset = modelItems(model, 'osasuoritukset')

  let korotus = oppiaineSuoritukset.find(s => modelData(s, 'korotus')) ? ['† = perusopetuksen päättötodistuksen arvosanan korotus'] : []
  let yksilöllistetty = oppiaineSuoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä')) ? ['* = yksilöllistetty oppimäärä'] : []
  let painotettu = oppiaineSuoritukset.find(s => modelData(s, 'painotettuOpetus')) ? ['** = painotettu opetus'] : []
  let selitteet = korotus.concat(yksilöllistetty).concat(painotettu).join(', ')
  let uusiOppiaineenSuoritus = model.context.edit ? createOppiaineenSuoritus(modelLookup(model, 'osasuoritukset')) : null
  let showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (model.context.edit || valmiitaSuorituksia(oppiaineSuoritukset))

  if (isYsiluokka(model) && jääLuokalle(model) && oppiaineSuoritukset.length == 0) {
    luokkaAsteenOsasuoritukset(luokkaAste(model), isToimintaAlueittain(model)).onValue(oppiaineet => {
      pushModel(modelSetValue(model, oppiaineet.value, 'osasuoritukset'))
    })
  } else if (isYsiluokka(model) && !jääLuokalle(model) && oppiaineSuoritukset.length > 0) {
    pushModel(modelSetValue(model, [], 'osasuoritukset'))
  }

  return (<div className="oppiaineet">
    { isYsiluokka(model) && (<div className="ysiluokka-jaa-luokalle">
        <PropertyEditor model={model} propertyName="jääLuokalle" />
        { model.context.edit && <em><Text name="Oppiaineiden arvioinnit syötetään 9. vuosiluokalla vain, jos oppilas jää luokalle"/></em> }
      </div>)
    }
    { showOppiaineet && (<div>
        <h5><Text name={(isToimintaAlueittain(model) ? 'Toiminta-alueiden' : 'Oppiaineiden') + ' arvosanat'} /></h5>
        <p><Text name="Arvostelu 4-10, S (suoritettu) tai H (hylätty)"/></p>
        {
          hasPakollisuus(model, uusiOppiaineenSuoritus)
            ? <GroupedOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
            : <SimpleOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
        }
        {selitteet && <p className="selitteet">{selitteet}</p>}
      </div>)
    }
  </div>)
}

const valmiitaSuorituksia = oppiaineSuoritukset => {
  let valmiitaKursseja = () => oppiaineSuoritukset.flatMap(oppiaine => modelItems(oppiaine, 'osasuoritukset')).filter(arvioituTaiVahvistettu)
  return oppiaineSuoritukset.filter(arvioituTaiVahvistettu).length > 0 || valmiitaKursseja().length > 0
}

const hasPakollisuus = (model, uusiOppiaineenSuoritus) => {
  let oppiaineHasPakollisuus = (oppiaine) => findModelProperty(oppiaine, p=>p.key=='pakollinen')
  let koulutusmoduuliProtos = oneOfPrototypes(modelLookup(uusiOppiaineenSuoritus, 'koulutusmoduuli'))
  return !isToimintaAlueittain(model) && (koulutusmoduuliProtos.some(oppiaineHasPakollisuus) || modelItems(model, 'osasuoritukset').map(m => modelLookup(m, 'koulutusmoduuli')).some(oppiaineHasPakollisuus))
}

const GroupedOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let groups = [pakollisetTitle, valinnaisetTitle]
  let groupedSuoritukset = R.groupBy(groupTitleForSuoritus, modelItems(model, 'osasuoritukset'))
  return (<span>{groups.map(pakollisuus => {
    let pakollinen = pakollisuus === 'Pakolliset oppiaineet'
    let suoritukset = groupedSuoritukset[pakollisuus] || []

    return (<section className={pakollinen ? 'pakolliset' : 'valinnaiset'} key={pakollisuus}>
      <Oppiainetaulukko model={model} title={groups.length > 1 && pakollisuus} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} pakolliset={pakollinen} />
      {
        pakollinen ? null : <KäyttäytymisenArvioEditor model={model} />
      }
    </section>)
  })}</span>)
}

const SimpleOppiaineetEditor = ({model, uusiOppiaineenSuoritus}) => {
  let suoritukset = modelItems(model, 'osasuoritukset')
  return (<span>
    <Oppiainetaulukko model={model} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
    <KäyttäytymisenArvioEditor model={model}/>
  </span>)
}

const KäyttäytymisenArvioEditor = ({model}) => {
  let edit = model.context.edit
  let käyttäytymisenArvioModel = modelLookup(model, 'käyttäytymisenArvio')
  return (käyttäytymisenArvioModel && (edit || modelData(käyttäytymisenArvioModel)))? (<div className="kayttaytyminen">
    <h5><Text name="Käyttäytymisen arviointi"/></h5>
    {
      <Editor model={model} path="käyttäytymisenArvio"/>
    }
  </div>) : null

}

let createOppiaineenSuoritus = (suoritukset) => {
  suoritukset = wrapOptional(suoritukset)
  let newItemIndex = modelItems(suoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(suoritukset.arrayPrototype, suoritukset, newItemIndex)
  let preferredClass = isToimintaAlueittain(oppiaineenSuoritusProto) ? 'toiminta_alueensuoritus' : 'oppiaineensuoritus'
  let sortValue = (suoritusProto) => suoritusProto.value.classes.includes(preferredClass) ? 0 : 1
  let options = oneOfPrototypes(oppiaineenSuoritusProto).sort((a, b) => sortValue(a) - sortValue(b))
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, suoritukset, newItemIndex)
}

class Oppiainetaulukko extends React.Component {
  render() {
    let {model, suoritukset, title, pakolliset, uusiOppiaineenSuoritus} = this.props
    let { isExpandedP, setExpanded } = accumulateExpandedState({suoritukset, filter: s => expandableProperties(s).length > 0, component: this})

    let edit = model.context.edit
    let showLaajuus = (!!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus')) && !edit && !pakolliset) || (edit && !pakolliset)
    let showFootnotes = !edit && !!suoritukset.find(s => modelData(s, 'yksilöllistettyOppimäärä') ||modelData(s, 'painotettuOpetus') || modelData(s, 'korotus'))

    let addOppiaine = oppiaine => {
      var suoritusUudellaOppiaineella = modelSet(uusiOppiaineenSuoritus, oppiaine, 'koulutusmoduuli')
      pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
      ensureArrayKey(suoritusUudellaOppiaineella)
      setExpanded(suoritusUudellaOppiaineella)(true)
    }

    if (suoritukset.length == 0 && !model.context.edit) return null
    let placeholder = t(
      isToimintaAlueittain(model)
        ? 'Lisää toiminta-alue'
        : (pakolliset == undefined
          ? 'Lisää oppiaine'
          : (pakolliset
            ? 'Lisää pakollinen oppiaine'
            : 'Lisää valinnainen oppiaine')))

    return (<section>
        {title && <h5><Text name={title} /></h5>}
        { suoritukset.length > 0 && (
          <table>
            <thead>
            <tr>
              <th className="oppiaine"><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
              <th className="arvosana" colSpan={(showFootnotes && !showLaajuus) ? '2' : '1'}><Text name="Arvosana"/></th>
              {showLaajuus && <th className="laajuus" colSpan={showFootnotes ? '2' : '1'}><Text name="Laajuus"/></th>}
            </tr>
            </thead>
            {
              suoritukset.filter(s => edit || arvioituTaiVahvistettu(s) || osasuoritukset(s).length).map((suoritus) => (<PerusopetuksenOppiaineRowEditor baret-lift
                                                                                                                     key={suoritus.arrayKey} model={suoritus} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}
                                                                                                                     expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)}
                                                                                                                     showLaajuus={showLaajuus} showFootnotes={showFootnotes}/> ))
            }
          </table>
        )}
        <UusiPerusopetuksenOppiaineDropdown suoritukset={suoritukset} oppiaineenSuoritus={uusiOppiaineenSuoritus}
                                            pakollinen={pakolliset} resultCallback={addOppiaine}
                                            organisaatioOid={modelData(model.context.toimipiste).oid}
                                            placeholder={placeholder}/>
      </section>
    )
  }
}