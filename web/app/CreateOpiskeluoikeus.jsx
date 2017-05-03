import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import R from 'ramda'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import {formatISODate} from './date.js'
import Dropdown from './Dropdown.jsx'
import DateInput from './DateInput.jsx'
import OrganisaatioPicker from './OrganisaatioPicker.jsx'
import {UusiPerusopetuksenOppiaineEditor} from './editor/UusiPerusopetuksenOppiaineEditor.jsx'
import {accumulateModelState, modelLookup, modelData} from './editor/EditorModel'
import {editorMapping} from './editor/Editors.jsx'
import {Editor} from './editor/Editor.jsx'
import {PropertyEditor} from './editor/PropertyEditor.jsx'

const Oppilaitos = ({oppilaitosAtom}) => (<label className='oppilaitos'>Oppilaitos
  {
    oppilaitosAtom.map(oppilaitos => (
      <OrganisaatioPicker
        preselectSingleOption={true}
        selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && oppilaitos.nimi.fi }}
        onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
        shouldShowOrg={org => !org.organisaatiotyypit.some(t => t === 'TOIMIPISTE')}
        canSelectOrg={(org) => org.organisaatiotyypit.some(t => t === 'OPPILAITOS') }
        clearText="tyhjennä"
      />
    ))
  }
</label>)

const Tutkinto = ({tutkintoAtom, opiskeluoikeudenTyyppiP, oppilaitosP}) =>{
  return (<div>
    {
      Bacon.combineWith(oppilaitosP, opiskeluoikeudenTyyppiP, tutkintoAtom, (oppilaitos, tyyppi, tutkinto) =>
        oppilaitos && tyyppi && tyyppi.koodiarvo == 'ammatillinenkoulutus' && (
          <label className='tutkinto'>Tutkinto<Autocomplete
            resultAtom={tutkintoAtom}
            fetchItems={(value) => (value.length >= 3)
                ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value)
                : Bacon.constant([])}
            disabled={!oppilaitos}
            selected={tutkinto}
          /></label>
        )
      )
    }
  </div> )
}

const Oppimäärä = ({oppimääräAtom, opiskeluoikeudenTyyppiP, oppimäärätP}) => {
  let shouldShowP = opiskeluoikeudenTyyppiP.map(tyyppi => tyyppi && tyyppi.koodiarvo == 'perusopetus').skipDuplicates()
  return (<div>
    {
      shouldShowP.map(show => show && <KoodistoDropdown
            className="oppimaara"
            title="Oppimäärä"
            optionsP = { oppimäärätP }
            atom = {oppimääräAtom} />
      )
    }
  </div> )
}

const KoodistoDropdown = ({ className, title, optionsP, atom}) => {
  let onChange = (value) => { atom.set(value) }
  let optionCountP = Bacon.combineWith(optionsP, atom, (options, selected) => !!selected && options.length).skipDuplicates()

  return (<div>{
    optionCountP.map(count =>
    {
      if (!count) return null
      if (count == 1) return <label className={className}>{title}<input type="text" className={className} disabled value={atom.map('.nimi.fi').map(x => x || '')}></input></label>
      return (<label className={className}>{title}<Dropdown
        options={optionsP}
        keyValue={option => option.koodiarvo}
        displayValue={option => option.nimi.fi}
        onSelectionChanged={option => onChange(option)}
        selected={atom}/></label>)
    })
  }</div>)
}

const OpiskeluoikeudenTyyppi = ({opiskeluoikeudenTyyppiAtom, opiskeluoikeustyypitP}) => <KoodistoDropdown className="opiskeluoikeudentyyppi" title="Opiskeluoikeus" optionsP={opiskeluoikeustyypitP} atom={opiskeluoikeudenTyyppiAtom}/>

const Aloituspäivä = ({dateAtom}) => {
  return (<label className='aloituspaiva'>Aloituspäivä
    <DateInput value={dateAtom.get()} valueCallback={(value) => dateAtom.set(value)} validityCallback={(valid) => !valid && dateAtom.set(undefined)} />
  </label>)
}

const OpiskeluoikeudenTila = ({tilaAtom, opiskeluoikeudenTilatP}) => {
  return (<KoodistoDropdown
    className="opiskeluoikeudentila"
    title="Opiskeluoikeuden tila"
    optionsP={opiskeluoikeudenTilatP}
    atom={tilaAtom}/>)
}

const OppiaineEditor = ({suoritusPrototypeP, oppiaineenSuoritusAtom}) => { // Yleinen prototyyppi suoritukselle
  return (<span>
    {
      suoritusPrototypeP.map(oppiaineenSuoritus => {
        let suoritusPrototypeAtom = Atom(undefined) // Valittu prototyyppi suoritukselle, valitaan UusiPerusopetuksenOppiaineEditorilla
        if (!oppiaineenSuoritus) return null
        oppiaineenSuoritus = Editor.setupContext(oppiaineenSuoritus, {edit:true, editorMapping})

        let suoritusModelP = suoritusPrototypeAtom.flatMapLatest(oppiainePrototype => {
          return oppiainePrototype && accumulateModelState(oppiainePrototype)
        }).toProperty()

        suoritusModelP.map(modelData).onValue(suoritus => oppiaineenSuoritusAtom.set(suoritus))
        return (<span>
          <label className="oppiaine">Oppiaine <UusiPerusopetuksenOppiaineEditor oppiaineenSuoritus={oppiaineenSuoritus} selected={suoritusPrototypeAtom} resultCallback={s => suoritusPrototypeAtom.set(s)} pakollinen={true} enableFilter={false}/></label>
          { suoritusModelP.map(model =>
            model && <label><PropertyEditor model={modelLookup(model, 'koulutusmoduuli')} propertyName="kieli"/></label> )
          }
        </span>)
      })
    }
  </span>)
}

const makeSuorituksetAmmatillinen = (oppilaitos, tutkinto) => {
  if (tutkinto && oppilaitos) {
    return [{
      koulutusmoduuli: {
        tunniste: {
          koodiarvo: tutkinto.tutkintoKoodi,
          koodistoUri: 'koulutus'
        },
        perusteenDiaarinumero: tutkinto.diaarinumero
      },
      toimipiste : oppilaitos,
      tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
      tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'ammatillinentutkinto'}
    }]
  }
}

const makeSuorituksetPerusopetus = (oppilaitos, oppimäärä, oppiaineenSuoritus) => {
  if (oppilaitos && oppimäärä && oppimäärä.koodiarvo == 'perusopetuksenoppimaara') {
    return [{
      koulutusmoduuli: {
        tunniste: {
          koodiarvo: '201101',
          koodistoUri: 'koulutus'
        }
      },
      toimipiste: oppilaitos,
      tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
      oppimäärä: { koodistoUri: 'perusopetuksenoppimaara', koodiarvo: 'perusopetus'},
      suoritustapa: { koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'},
      tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetuksenoppimaara'}
    }]
  } else if (oppilaitos && oppimäärä && oppimäärä.koodiarvo == 'perusopetuksenoppiaineenoppimaara' && oppiaineenSuoritus) {
    var suoritusTapaJaToimipiste = {toimipiste: oppilaitos, suoritustapa: {koodistoUri: 'perusopetuksensuoritustapa', koodiarvo: 'koulutus'}}
    return [R.merge(oppiaineenSuoritus, suoritusTapaJaToimipiste)]
  }
}

const makeSuorituksetPerusopetukseenValmistavaOpetus = (oppilaitos) => {
  if (oppilaitos) {
    return [{
      koulutusmoduuli: {
        tunniste: {
          koodiarvo: '999905',
          koodistoUri: 'koulutus'
        }
      },
      toimipiste: oppilaitos,
      tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
      tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetukseenvalmistavaopetus'}
    }]
  }
}

const makeSuorituksetPerusopetuksenLisäopetus = (oppilaitos) => {
  if (oppilaitos) {
    return [{
      koulutusmoduuli: {
        tunniste: {
          koodiarvo: '020075',
          koodistoUri: 'koulutus'
        }
      },
      toimipiste: oppilaitos,
      tila: { koodistoUri: 'suorituksentila', koodiarvo: 'KESKEN'},
      tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'perusopetuksenlisaopetus'}
    }]
  }
}


var makeOpiskeluoikeus = (date, oppilaitos, tyyppi, suoritukset, tila) => {
  return date && oppilaitos && tyyppi && suoritukset && tila && {
      tyyppi: tyyppi,
      oppilaitos: oppilaitos,
      alkamispäivä: formatISODate(date),
      tila: {
        opiskeluoikeusjaksot: [ { alku: formatISODate(date), tila }]
      },
      suoritukset
    }
}

export default ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const tutkintoAtom = Atom()
  const tyyppiAtom = Atom()
  const tilaAtom = Atom()
  const oppimääräAtom = Atom()
  const oppiaineenSuoritusAtom = Atom()

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  opiskeluoikeustyypitP.onValue(tyypit => tyyppiAtom.set(tyypit[0]))

  const opiskeluoikeudenTilatP = Http.cachedGet('/koski/api/editor/koodit/koskiopiskeluoikeudentila').map(tilat => tilat.map(t => t.data))
  opiskeluoikeudenTilatP.onValue(tilat => tilaAtom.set(tilat.find(t => t.koodiarvo == 'lasna')))

  const oppimäärätP = Http.cachedGet('/koski/api/editor/koodit/suorituksentyyppi/perusopetuksenoppimaara,perusopetuksenoppiaineenoppimaara').map(tilat => tilat.map(t => t.data))
  oppimäärätP.onValue(oppimäärät => oppimääräAtom.set(oppimäärät.find(o => o.koodiarvo == 'perusopetuksenoppimaara')))

  const suorituksetP = tyyppiAtom.map('.koodiarvo').decode({
    'ammatillinenkoulutus': Bacon.combineWith(oppilaitosAtom, tutkintoAtom, makeSuorituksetAmmatillinen),
    'perusopetus': Bacon.combineWith(oppilaitosAtom, oppimääräAtom, oppiaineenSuoritusAtom, makeSuorituksetPerusopetus),
    'perusopetukseenvalmistavaopetus': Bacon.combineWith(oppilaitosAtom, makeSuorituksetPerusopetukseenValmistavaOpetus),
    'perusopetuksenlisaopetus': Bacon.combineWith(oppilaitosAtom, makeSuorituksetPerusopetuksenLisäopetus)
  })

  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const opiskeluoikeusP = Bacon.combineWith(dateAtom, oppilaitosAtom, tyyppiAtom, suorituksetP, tilaAtom, makeOpiskeluoikeus)
  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  const suoritusPrototypeP = oppimääräAtom.map('.koodiarvo').flatMap(oppimäärä => {
    if (oppimäärä == 'perusopetuksenoppiaineenoppimaara') {
      return Http.cachedGet('/koski/api/editor/prototype/fi.oph.koski.schema.PerusopetuksenOppiaineenOppimääränSuoritus')
    }
  })

  return (<div>
      <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
      <OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={tyyppiAtom} opiskeluoikeustyypitP={opiskeluoikeustyypitP}/>
      <Tutkinto tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom} opiskeluoikeudenTyyppiP={tyyppiAtom}/>
      <Oppimäärä oppimääräAtom={oppimääräAtom} opiskeluoikeudenTyyppiP={tyyppiAtom} oppimäärätP={oppimäärätP}/>
      <OppiaineEditor suoritusPrototypeP={suoritusPrototypeP} oppiaineenSuoritusAtom={oppiaineenSuoritusAtom}/>
      <Aloituspäivä dateAtom={dateAtom} />
      <OpiskeluoikeudenTila tilaAtom={tilaAtom} opiskeluoikeudenTilatP={opiskeluoikeudenTilatP} />
  </div>)
}