import React from 'baret'
import Bacon from 'baconjs'
import {Editor} from '../editor/Editor'
import {PropertyEditor} from '../editor/PropertyEditor'
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
  pushModel,
  wrapOptional
} from '../editor/EditorModel'
import * as R from 'ramda'
import {arvioituTaiVahvistettu, osasuoritukset} from '../suoritus/Suoritus'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {
  footnoteDescriptions, footnotesForSuoritus, groupTitleForSuoritus,
  isPäättötodistus,
  isToimintaAlueittain,
  isVuosiluokkaTaiPerusopetuksenOppimäärä,
  isYsiluokka,
  jääLuokalle,
  luokkaAste,
  luokkaAsteenOsasuoritukset,
  oppimääränOsasuoritukset, pakollisetTitle, valinnaisetTitle, valmiitaSuorituksia
} from './Perusopetus'
import {expandableProperties, PerusopetuksenOppiaineRowEditor} from './PerusopetuksenOppiaineRowEditor'
import {UusiPerusopetuksenOppiaineDropdown} from './UusiPerusopetuksenOppiaineDropdown'
import {FootnoteDescriptions} from '../components/footnote'
import {parseISODate} from '../date/date'


export const PerusopetuksenOppiaineetEditor = ({model}) => {
  model = addContext(model, { suoritus: model })
  let oppiaineSuoritukset = modelItems(model, 'osasuoritukset')

  const footnotes = footnoteDescriptions(oppiaineSuoritukset)
  let osasuorituksetModel = modelLookup(model, 'osasuoritukset')
  let uusiOppiaineenSuoritus = model.context.edit ? createOppiaineenSuoritus(osasuorituksetModel) : null
  let uusiPerusopetukseenValmistavanOppiaineenSuoritus = model.context.edit && isPerusopetukseenValmistava(model) ? createOppiaineenSuoritus(osasuorituksetModel, () => 'perusopetukseenvalmistavanopetuksenoppiaineensuoritus') : null
  let showOppiaineet = !(isYsiluokka(model) && !jääLuokalle(model)) && (model.context.edit || valmiitaSuorituksia(oppiaineSuoritukset) || isVuosiluokkaTaiPerusopetuksenOppimäärä(model))

  if (model.context.edit) {
    if (!valmiitaSuorituksia(oppiaineSuoritukset)) {
      prefillOsasuorituksetIfNeeded(model, oppiaineSuoritukset)
    } else if (isYsiluokka(model) && !jääLuokalle(model)) {
      emptyOsasuoritukset(model)
    }
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
          hasPakollisuus(model, uusiOppiaineenSuoritus) && !isPerusopetukseenValmistava(model)
            ? <GroupedOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus}/>
            : <SimpleOppiaineetEditor model={model} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} uusiPerusopetukseenValmistavanOppiaineenSuoritus={uusiPerusopetukseenValmistavanOppiaineenSuoritus} />
        }
        {!R.isEmpty(footnotes) && <FootnoteDescriptions data={footnotes}/>}
      </div>)
    }
  </div>)
}

const prefillOsasuorituksetIfNeeded = (model, currentSuoritukset) => {
  const wrongOsasuorituksetTemplateP = fetchOsasuorituksetTemplate(model, !isToimintaAlueittain(model))
  const hasWrongPrefillP = wrongOsasuorituksetTemplateP.map(wrongOsasuorituksetTemplate =>
    // esitäyttödatan tyyppi ei sisällä nimi ja versiotietoja, poistetaan tyyppi koska se ei ole relevanttia vertailussa
    currentSuoritukset.length > 0 && R.equals(wrongOsasuorituksetTemplate.value.map(modelDataIlmanTyyppiä), currentSuoritukset.map(modelDataIlmanTyyppiä))
  )
  const changeTemplateP = hasWrongPrefillP.or(Bacon.constant(isYsiluokka(model) && jääLuokalle(model)))
  fetchOsasuorituksetTemplate(model, isToimintaAlueittain(model)).filter(changeTemplateP)
    .onValue(osasuorituksetTemplate => pushModel(modelSetValue(model, osasuorituksetTemplate.value, 'osasuoritukset')))
}

const emptyOsasuoritukset = model => pushModel(modelSetValue(model, [], 'osasuoritukset'))

const fetchOsasuorituksetTemplate = (model, toimintaAlueittain) => isPäättötodistus(model)
  ? oppimääränOsasuoritukset(modelData(model, 'tyyppi'), toimintaAlueittain)
  : luokkaAste(model)
    ? luokkaAsteenOsasuoritukset(luokkaAste(model), toimintaAlueittain)
    : Bacon.constant({value: []})

const modelDataIlmanTyyppiä = suoritus => R.dissoc('tyyppi', modelData(suoritus))

const hasPakollisuus = (model, uusiOppiaineenSuoritus) => {
  let oppiaineHasPakollisuus = (oppiaine) => findModelProperty(oppiaine, p => p.key === 'pakollinen')
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

const SimpleOppiaineetEditor = ({model, uusiOppiaineenSuoritus, uusiPerusopetukseenValmistavanOppiaineenSuoritus}) => {
  let suoritukset = modelItems(model, 'osasuoritukset')
  return (<span>
    <Oppiainetaulukko model={model} suoritukset={suoritukset} uusiOppiaineenSuoritus={uusiOppiaineenSuoritus} uusiPerusopetukseenValmistavanOppiaineenSuoritus={uusiPerusopetukseenValmistavanOppiaineenSuoritus} />
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

const isPerusopetukseenValmistava = model => model.value.classes.includes('perusopetukseenvalmistavanopetuksensuoritus')
const createOppiaineenSuoritus = (suoritukset, preferredClassF = proto => isToimintaAlueittain(proto) ? 'toiminta_alueensuoritus' : 'oppiaineensuoritus') => {
  suoritukset = wrapOptional(suoritukset)
  let newItemIndex = modelItems(suoritukset).length
  let oppiaineenSuoritusProto = contextualizeSubModel(suoritukset.arrayPrototype, suoritukset, newItemIndex)
  let preferredClass = preferredClassF(oppiaineenSuoritusProto)
  let sortValue = (suoritusProto) => suoritusProto.value.classes.includes(preferredClass) ? 0 : 1
  let options = oneOfPrototypes(oppiaineenSuoritusProto).sort((a, b) => sortValue(a) - sortValue(b))
  oppiaineenSuoritusProto = options[0]
  return contextualizeSubModel(oppiaineenSuoritusProto, suoritukset, newItemIndex)
}

class Oppiainetaulukko extends React.Component {
  render() {
    let {model, suoritukset, title, pakolliset, uusiOppiaineenSuoritus, uusiPerusopetukseenValmistavanOppiaineenSuoritus} = this.props
    let { isExpandedP, setExpanded } = accumulateExpandedState({suoritukset, filter: s => expandableProperties(s).length > 0, component: this})

    const edit = model.context.edit
    const showArvosana = edit || arvioituTaiVahvistettu(model) || !model.value.classes.includes('perusopetuksenoppimaaransuoritus')
    const uudellaSuorituksellaLaajuus = () => !!modelLookup(uusiOppiaineenSuoritus ? uusiOppiaineenSuoritus : createOppiaineenSuoritus(modelLookup(model, 'osasuoritukset')), 'koulutusmoduuli.laajuus')
    const sisältääLajuudellisiaSuorituksia = !!suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus'))
    const päätasonSuorituksenVahvituspäivä = modelData(model, 'vahvistus.päivä')
    const vahvistusSalliiLaajuudenNäyttämisen =  päätasonSuorituksenVahvituspäivä && parseISODate(päätasonSuorituksenVahvituspäivä) >= parseISODate('2020-08-01')

    const showLaajuus = edit
      ? uudellaSuorituksellaLaajuus()
      : pakolliset
        ? vahvistusSalliiLaajuudenNäyttämisen && sisältääLajuudellisiaSuorituksia
        : sisältääLajuudellisiaSuorituksia

    const showFootnotes = !edit && !R.isEmpty(footnoteDescriptions(suoritukset))

    let addOppiaine = oppiaineenSuoritus => oppiaine => {
      var suoritusUudellaOppiaineella = modelSet(oppiaineenSuoritus, oppiaine, 'koulutusmoduuli')
      pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
      ensureArrayKey(suoritusUudellaOppiaineella)
      setExpanded(suoritusUudellaOppiaineella)(true)
    }

    let selectSuoritusProto = suoritus => suoritus.value.classes[0] === 'perusopetukseenvalmistavanopetuksenoppiaineensuoritus'
      ? uusiPerusopetukseenValmistavanOppiaineenSuoritus
      : uusiOppiaineenSuoritus

    if (suoritukset.length == 0 && !model.context.edit) return null
    let placeholder = t(
      isToimintaAlueittain(model)
        ? 'Lisää toiminta-alue'
        : (pakolliset == undefined
          ? 'Lisää oppiaine'
          : (pakolliset
            ? 'Lisää pakollinen oppiaine'
            : 'Lisää valinnainen oppiaine')))

    return (<section className="oppiaine-taulukko">
        {title && <h5><Text name={title} /></h5>}
        { suoritukset.length > 0 && (
          <table>
            <thead>
            <tr>
              <th className="oppiaine"><Text name={isToimintaAlueittain(model) ? 'Toiminta-alue' : 'Oppiaine'}/></th>
              {showArvosana && <th className="arvosana" colSpan={(showFootnotes && !showLaajuus) ? '2' : '1'}><Text name="Arvosana"/></th>}
              {showLaajuus && <th className="laajuus" colSpan={showFootnotes ? '2' : '1'}><Text name="Laajuus"/></th>}
            </tr>
            </thead>
            {
              suoritukset.filter(s => edit || arvioituTaiVahvistettu(s) || osasuoritukset(s).length || isVuosiluokkaTaiPerusopetuksenOppimäärä(model)).map((suoritus) => (
                <PerusopetuksenOppiaineRowEditor
                  baret-lift
                  key={suoritus.arrayKey}
                  model={suoritus}
                  uusiOppiaineenSuoritus={selectSuoritusProto(suoritus)}
                  expanded={isExpandedP(suoritus)}
                  onExpand={setExpanded(suoritus)}
                  showArvosana={showArvosana}
                  showLaajuus={showLaajuus}
                  footnotes={footnotesForSuoritus(suoritus)}
                />
              ))
            }
          </table>
        )}
        <UusiPerusopetuksenOppiaineDropdown suoritukset={suoritukset} oppiaineenSuoritus={uusiOppiaineenSuoritus}
                                            pakollinen={pakolliset} resultCallback={addOppiaine(uusiOppiaineenSuoritus)}
                                            organisaatioOid={modelData(model.context.toimipiste).oid}
                                            placeholder={placeholder}/>
        {
          uusiPerusopetukseenValmistavanOppiaineenSuoritus &&
          <span className='uusi-perusopetukseen-valmistava-oppiaine'>
          <UusiPerusopetuksenOppiaineDropdown suoritukset={suoritukset} oppiaineenSuoritus={uusiPerusopetukseenValmistavanOppiaineenSuoritus}
                                              pakollinen={pakolliset} resultCallback={addOppiaine(uusiPerusopetukseenValmistavanOppiaineenSuoritus)}
                                              organisaatioOid={modelData(model.context.toimipiste).oid}
                                              placeholder={t('Lisää perusopetukseen valmistava oppiaine')}
                                            />
          </span>
        }

      </section>
    )
  }
}
