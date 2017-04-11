import {
  modelData,
  modelItems,
  modelLookup,
  accumulateModelState,
  optionalPrototypeModel,
  modelSet,
  modelSetValue,
  pushModel
} from './EditorModel'
import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {PropertyEditor} from './PropertyEditor.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import * as Lukio from './Lukio.jsx'
import {Suoritustaulukko} from './Suoritustaulukko.jsx'
import {LuvaEditor} from './LuvaEditor.jsx'
import {PerusopetuksenOppiaineetEditor} from './PerusopetuksenOppiaineetEditor.jsx'
import {sortLanguages} from '../sorting'
import {Editor} from './Editor.jsx'
import ModalDialog from './ModalDialog.jsx'
import {setTila, suoritusValmis} from './Suoritus'

export const SuoritusEditor = React.createClass({
  render() {
    let {model} = this.props
    let excludedProperties = ['osasuoritukset', 'käyttäytymisenArvio', 'tila', 'vahvistus', 'jääLuokalle', 'pakollinen']

    let resolveEditor = (mdl) => {
      if (['perusopetuksenvuosiluokansuoritus', 'perusopetuksenoppimaaransuoritus', 'perusopetuksenlisaopetuksensuoritus', 'perusopetukseenvalmistavanopetuksensuoritus'].includes(mdl.value.classes[0])) {
        return <PerusopetuksenOppiaineetEditor model={mdl}/>
      }
      if (model.value.classes.includes('ammatillinenpaatasonsuoritus')) {
        return <Suoritustaulukko suoritukset={modelItems(model, 'osasuoritukset') || []}/>
      }
      if (mdl.value.classes.includes('lukionoppimaaransuoritus')) {
        return <Lukio.LukionOppiaineetEditor oppiaineet={modelItems(mdl, 'osasuoritukset') || []} />
      }
      if (mdl.value.classes.includes('lukionoppiaineenoppimaaransuoritus')) {
        return <Lukio.LukionOppiaineetEditor oppiaineet={[mdl]} />
      }
      if (model.value.classes.includes('lukioonvalmistavankoulutuksensuoritus')) {
        return <LuvaEditor suoritukset={modelItems(model, 'osasuoritukset') || []}/>
      }
      return <PropertyEditor model={mdl} propertyName="osasuoritukset"/>
    }

    let className = 'suoritus ' + model.value.classes.join(' ')

    return (<div className={className}>
      <TodistusLink suoritus={model} />
      <PropertiesEditor
        model={model}
        propertyFilter={p => !excludedProperties.includes(p.key)}
        getValueEditor={ (prop, getDefault) => prop.key === 'suorituskieli'
          ? <Editor model={modelLookup(model, 'suorituskieli')} sortBy={sortLanguages}/>
          : getDefault() }
      />
      <TilaJaVahvistus model={model} />
      <div className="osasuoritukset">{resolveEditor(model)}</div>
    </div>)
  }
})
SuoritusEditor.näytettäväPäätasonSuoritus = s => !['perusopetuksenvuosiluokka', 'korkeakoulunopintojakso'].includes(modelData(s).tyyppi.koodiarvo)

const TilaJaVahvistus = ({model}) => {
  let addingAtom = Atom(false)
  let merkitseValmiiksiCallback = (suoritusModel) => {
    if (suoritusModel) {
      pushModel(suoritusModel, model.context.changeBus)
    } else {
      addingAtom.set(false)
    }
  }
  let tila = modelData(model).tila.koodiarvo
  return (<div className="tila-vahvistus">
      <span className="tiedot">
        <span className="tila">
          Suoritus: <span className={ tila === 'VALMIS' ? 'valmis' : ''}>{ modelData(model).tila.koodiarvo }</span> { /* TODO: i18n */ }
        </span>
        {
          modelData(model).vahvistus && <PropertyEditor model={model} propertyName="vahvistus" edit="false"/>
        }
        <JääLuokalleTaiSiirretään model={model}/>
      </span>
      <span className="controls">
        {
          model.context.edit && tila === 'KESKEN' && <button className="merkitse-valmiiksi" onClick={() => addingAtom.modify(x => !x)}>Merkitse valmiiksi</button>
        }
      </span>
      {
        addingAtom.map(adding => adding && <MerkitseValmiiksiPopup suoritus={model} resultCallback={merkitseValmiiksiCallback}/>)
      }
    </div>
  )
}

const MerkitseValmiiksiPopup = ({ suoritus, resultCallback }) => {
  let submitBus = Bacon.Bus()
  let vahvistus = optionalPrototypeModel(modelLookup(suoritus, 'vahvistus'))
  suoritus = modelSet(suoritus, vahvistus, 'vahvistus')
  let toimipiste = modelLookup(suoritus, 'toimipiste')
  suoritus = modelSetValue(suoritus, toimipiste.value, 'vahvistus.myöntäjäOrganisaatio')
  suoritus = setTila(suoritus, 'VALMIS')
  let { modelP, errorP } = accumulateModelState(suoritus)
  let validP = errorP.not()
  modelP.sampledBy(submitBus).onValue(resultCallback)

  return (<ModalDialog className="merkitse-valmiiksi-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()}>
    <h2>Suoritus valmis</h2>
    <PropertiesEditor baret-lift model={modelP.map(s => modelLookup(s, 'vahvistus'))}  />
    <button disabled={validP.not()} onClick={() => submitBus.push()}>Merkitse valmiiksi</button>
  </ModalDialog>)
}

const JääLuokalleTaiSiirretään = ({model}) => {
  let jääLuokalle = modelData(model, 'jääLuokalle')
  let luokka = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  if (luokka && suoritusValmis(model)) {
    if (jääLuokalle === true) {
      return <div>Ei siirretä seuraavalle luokalle</div>
    } else if (jääLuokalle === false && luokka !== '9') {
      return <div>Siirretään seuraavalle luokalle</div>
    }
  }
  return null
}

const TodistusLink = React.createClass({
  render() {
    let {suoritus} = this.props
    let oppijaOid = suoritus.context.oppijaOid
    let suoritustyyppi = modelData(suoritus, 'tyyppi').koodiarvo
    let koulutusmoduuliKoodistoUri = modelData(suoritus, 'koulutusmoduuli').tunniste.koodistoUri
    let koulutusmoduuliKoodiarvo = modelData(suoritus, 'koulutusmoduuli').tunniste.koodiarvo
    let suoritusTila = modelData(suoritus, 'tila').koodiarvo
    let href = '/koski/todistus/' + oppijaOid + '?suoritustyyppi=' + suoritustyyppi + '&koulutusmoduuli=' + koulutusmoduuliKoodistoUri + '/' + koulutusmoduuliKoodiarvo
    return suoritusTila == 'VALMIS' && suoritustyyppi != 'korkeakoulututkinto' && suoritustyyppi != 'preiboppimaara' && suoritustyyppi != 'esiopetuksensuoritus' && !(koulutusmoduuliKoodistoUri == 'perusopetuksenluokkaaste' && koulutusmoduuliKoodiarvo == '9')
      ? <a className="todistus" href={href}>näytä todistus</a>
      : null
  }
})