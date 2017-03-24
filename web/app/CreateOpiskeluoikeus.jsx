import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Autocomplete from './Autocomplete.jsx'
import Http from './http'
import {showInternalError} from './location.js'
import {formatISODate} from './date.js'
import Dropdown from './Dropdown.jsx'
import DateInput from './DateInput.jsx'
import OrganisaatioPicker from './OrganisaatioPicker.jsx'

const Oppilaitos = ({oppilaitosAtom}) => (<label className='oppilaitos'>Oppilaitos
  {
    oppilaitosAtom.map(oppilaitos => (
      <OrganisaatioPicker
        selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && oppilaitos.nimi.fi }}
        onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
        filterOrgs={org => !org.organisaatiotyypit.some(t => t === 'TOIMIPISTE')}
        renderOrg={(org, defaultRender) => org.organisaatiotyypit.some(t => t === 'OPPILAITOS') ? defaultRender(org) : <span>{org.nimi.fi}</span> }
        clearText="tyhjennä"
      />
    ))
  }
</label>)

const Tutkinto = ({tutkintoAtom, opiskeluoikeudenTyyppiP, oppilaitosP}) =>{
  return (<div>
    {
      Bacon.combineWith(oppilaitosP, opiskeluoikeudenTyyppiP, tutkintoAtom, (oppilaitos, tyyppi, tutkinto) =>
        oppilaitos && tyyppi && tyyppi.koodiarvo == 'ammatillinenkoulutus' && <label className='tutkinto'>Tutkinto<Autocomplete
          resultAtom={tutkintoAtom}
          fetchItems={(value) => (value.length >= 3)
            ? Http.cachedGet('/koski/api/tutkinnonperusteet/oppilaitos/' + oppilaitos.oid + '?query=' + value).doError(showInternalError)
            : Bacon.constant([])}
          disabled={!oppilaitos}
          selected={tutkinto}
        /></label>
      )
    }
  </div> )
}

const KoodistoDropdown = ({ className, title, optionsP, pickInitialValue, atom}) => {
  let onChange = (value) => { atom.set(value) }

  optionsP.map(pickInitialValue).onValue(value => {
    atom.set(value)
  })

  return (<div>{
      Bacon.combineWith(optionsP, atom, (options, selected) => selected && (
        <label className={className}>{title}<Dropdown
          options={options}
          keyValue={option => option.koodiarvo}
          displayValue={option => option.nimi.fi}
          onSelectionChanged={option => onChange(option)}
          selected={selected}
        /></label>
      ))
  }</div>)
}

const OpiskeluoikeudenTyyppi = ({opiskeluoikeudenTyyppiAtom, opiskeluoikeustyypitP}) => {
  return <KoodistoDropdown className="opiskeluoikeudentyyppi" title="Opiskeluoikeus" optionsP={opiskeluoikeustyypitP} pickInitialValue={(values) => values[0]} atom={opiskeluoikeudenTyyppiAtom}/>
}

const Aloituspäivä = ({dateAtom}) => {
  return (<label className='aloituspaiva'>Aloituspäivä
    <DateInput value={dateAtom.get()} valueCallback={(value) => dateAtom.set(value)} validityCallback={(valid) => !valid && dateAtom.set(undefined)} />
  </label>)
}

const OpiskeluoikeudenTila = ({tilaAtom}) => {
  const opiskeluoikeudenTilatP = Http.cachedGet('/koski/api/editor/koodit/koskiopiskeluoikeudentila').map(tilat => tilat.map(t => t.data))
  return (<KoodistoDropdown
    className="opiskeluoikeudentila"
    title="Opiskeluoikeuden tila"
    optionsP={opiskeluoikeudenTilatP}
    pickInitialValue={tilat => tilat.find(t => t.koodiarvo == 'lasna')}
    atom={tilaAtom}/>)
}

var makeSuoritukset = (tyyppi, tutkinto, oppilaitos) => {
  if (tutkinto && oppilaitos && tyyppi && tyyppi.koodiarvo == 'ammatillinenkoulutus') {
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
  } else if (oppilaitos && tyyppi && tyyppi.koodiarvo == 'perusopetus') {
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

export const Opiskeluoikeus = ({opiskeluoikeusAtom}) => {
  const dateAtom = Atom(new Date())
  const oppilaitosAtom = Atom()
  const tutkintoAtom = Atom()
  const opiskeluoikeudenTyyppiAtom = Atom()
  const tilaAtom = Atom()

  const opiskeluoikeustyypitP = oppilaitosAtom
    .flatMapLatest((oppilaitos) => (oppilaitos ? Http.cachedGet(`/koski/api/oppilaitos/opiskeluoikeustyypit/${oppilaitos.oid}`) : []))
    .toProperty()

  const suorituksetP = Bacon.combineWith(opiskeluoikeudenTyyppiAtom, tutkintoAtom, oppilaitosAtom, makeSuoritukset)

  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))

  const opiskeluoikeusP = Bacon.combineWith(dateAtom, oppilaitosAtom, opiskeluoikeudenTyyppiAtom, suorituksetP, tilaAtom, makeOpiskeluoikeus)
  opiskeluoikeusP.changes().onValue((oo) => opiskeluoikeusAtom.set(oo))

  return (<div>
      <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
      <OpiskeluoikeudenTyyppi opiskeluoikeudenTyyppiAtom={opiskeluoikeudenTyyppiAtom} opiskeluoikeustyypitP={opiskeluoikeustyypitP}/>
      <Tutkinto tutkintoAtom={tutkintoAtom} oppilaitosP={oppilaitosAtom} opiskeluoikeudenTyyppiP={opiskeluoikeudenTyyppiAtom}/>
      <Aloituspäivä dateAtom={dateAtom} />
      <OpiskeluoikeudenTila tilaAtom={tilaAtom} />
  </div>)
}