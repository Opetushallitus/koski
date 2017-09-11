import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {modelData} from './EditorModel.js'
import {
  ensureArrayKey,
  modelSet,
  modelSetData,
  modelSetTitle,
  modelSetValue,
  modelSetValues,
  pushModel
} from './EditorModel'
import R from 'ramda'
import {t} from '../i18n'
import Text from '../Text.jsx'
import {enumValueToKoodiviiteLens, toKoodistoEnumValue} from '../koodistot'
import KoodistoDropdown from '../KoodistoDropdown.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'
import Http from '../http'
import {ift} from '../util'
import ModalDialog from './ModalDialog.jsx'
import TutkintoAutocomplete from '../TutkintoAutocomplete.jsx'
import {placeholderForNonGrouped} from './Suoritus'
import {createTutkinnonOsanSuoritusPrototype} from './TutkinnonOsa'

export default ({ suoritus, groupId, suoritusPrototype, suoritukset, suorituksetModel, setExpanded, groupTitles }) => {
  let koulutusModuuliprotos = koulutusModuuliprototypes(suoritusPrototype)
  let koulutusmoduuliProto = koulutusModuuliprotos.find(R.complement(isPaikallinen))
  let paikallinenKoulutusmoduuli = koulutusModuuliprotos.find(isPaikallinen)

  let käytössäolevatKoodiarvot = suoritukset.map(s => modelData(s, 'koulutusmoduuli.tunniste').koodiarvo)

  let diaarinumero = modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero')
  let suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')

  if (!diaarinumero || !suoritustapa) return null

  let osatP = fetchLisättävätTutkinnonOsat(diaarinumero, suoritustapa, groupId)

  return (<span>
    {
      osatP.map(lisättävätTutkinnonOsat => {
          return (<div>
            <LisääRakenteeseenKuuluvaTutkinnonOsa {...{ addTutkinnonOsa, lisättävätTutkinnonOsat, koulutusmoduuliProto, groupId, käytössäolevatKoodiarvot}} />
            <LisääOsaToisestaTutkinnosta {...{addTutkinnonOsa, lisättävätTutkinnonOsat, suoritus, suoritustapa, koulutusmoduuliProto, groupId, diaarinumero}}/>
            <LisääPaikallinenTutkinnonOsa {...{lisättävätTutkinnonOsat, addTutkinnonOsa, paikallinenKoulutusmoduuli, groupId}}/>
          </div>)
        }
      )
    }
  </span>)

  function addTutkinnonOsa(koulutusmoduuli, tutkinto) {
    if (groupId == placeholderForNonGrouped) groupId = undefined

    let uusiSuoritus = modelSet(createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId), koulutusmoduuli, 'koulutusmoduuli')
    if (groupId) {
      uusiSuoritus = modelSetValue(uusiSuoritus, toKoodistoEnumValue('ammatillisentutkinnonosanryhma', groupId, groupTitles[groupId]), 'tutkinnonOsanRyhmä')
    }
    if (tutkinto) {
      uusiSuoritus = modelSetData(uusiSuoritus, {
        tunniste: { koodiarvo: tutkinto.tutkintoKoodi, nimi: tutkinto.nimi, koodistoUri: 'koulutus' },
        perusteenDiaarinumero: tutkinto.diaarinumero
      }, 'tutkinto')
    }
    pushModel(ensureArrayKey(uusiSuoritus))
    setExpanded(uusiSuoritus)(true)
  }
}

const LisääRakenteeseenKuuluvaTutkinnonOsa = ({lisättävätTutkinnonOsat, addTutkinnonOsa, koulutusmoduuliProto, käytössäolevatKoodiarvot}) => {
  let selectedAtom = Atom(undefined)
  selectedAtom.filter(R.identity).onValue((newItem) => {
    addTutkinnonOsa(modelSetTitle(modelSetValues(koulutusmoduuliProto, { tunniste: newItem }), newItem.title))
  })
  let osat = lisättävätTutkinnonOsat.osat.filter(osa => !käytössäolevatKoodiarvot.includes(osa.koodiarvo))
  return osat.length > 0 && <LisääTutkinnonOsaDropdown selectedAtom={selectedAtom} osat={osat}/>
}

const LisääPaikallinenTutkinnonOsa = ({lisättävätTutkinnonOsat, addTutkinnonOsa, paikallinenKoulutusmoduuli}) => {
  let lisääPaikallinenAtom = Atom(false)
  let lisääPaikallinenTutkinnonOsa = (osa) => {
    lisääPaikallinenAtom.set(false)
    if (osa) {
      addTutkinnonOsa(osa)
    }
  }
  let nameAtom = Atom('')
  let selectedAtom = nameAtom
    .view(name => modelSetTitle(modelSetValues(paikallinenKoulutusmoduuli, { 'kuvaus.fi': { data: name}, 'tunniste.nimi.fi': { data: name}, 'tunniste.koodiarvo': { data: name } }), name))

  return (<span className="paikallinen-tutkinnon-osa">
    {
      lisättävätTutkinnonOsat.paikallinenOsa && <a className="add-link" onClick={() => lisääPaikallinenAtom.set(true)}>
        <Text name="Lisää paikallinen tutkinnon osa"/>
      </a>
    }
    { ift(lisääPaikallinenAtom, (<ModalDialog className="lisaa-paikallinen-tutkinnon-osa-modal" onDismiss={lisääPaikallinenTutkinnonOsa} onSubmit={() => lisääPaikallinenTutkinnonOsa(selectedAtom.get())} okTextKey="Lisää tutkinnon osa" validP={selectedAtom} submitOnEnterKey="false">
        <h2><Text name="Paikallisen tutkinnon osan lisäys"/></h2>
        <input type="text" onChange={event => nameAtom.set(event.target.value)}/>
      </ModalDialog>)
    ) }
  </span>)
}


const LisääOsaToisestaTutkinnosta = ({lisättävätTutkinnonOsat, suoritus, suoritustapa, koulutusmoduuliProto, addTutkinnonOsa, diaarinumero}) => {
  let oppilaitos = modelData(suoritus, 'toimipiste')
  console.log('s', suoritus)
  let lisääOsaToisestaTutkinnostaAtom = Atom(false)
  let lisääOsaToisestaTutkinnosta = (tutkinto, osa) => {
    lisääOsaToisestaTutkinnostaAtom.set(false)
    addTutkinnonOsa(modelSetTitle(modelSetValues(koulutusmoduuliProto, { tunniste: osa }), osa.title), tutkinto.diaarinumero != diaarinumero && tutkinto)
  }
  let tutkintoAtom = Atom()
  let tutkinnonOsaAtom = Atom()
  let osatP = tutkintoAtom.flatMapLatest(tutkinto => {
    if (!tutkinto) return []
    return Bacon.once([]).concat(fetchLisättävätTutkinnonOsat(tutkinto.diaarinumero, suoritustapa).doLog('lisättävät').map('.osat'))
  }).toProperty()

  return (<span className="osa-toisesta-tutkinnosta">
    {
      lisättävätTutkinnonOsat.osaToisestaTutkinnosta && <a className="add-link" onClick={() => lisääOsaToisestaTutkinnostaAtom.set(true)}>
        <Text name="Lisää tutkinnon osa toisesta tutkinnosta"/>
      </a>
    }
    { ift(lisääOsaToisestaTutkinnostaAtom, <ModalDialog className="lisaa-tutkinnon-osa-toisesta-tutkinnosta-modal" onDismiss={lisääOsaToisestaTutkinnosta} onSubmit={() => lisääOsaToisestaTutkinnosta(tutkintoAtom.get(), tutkinnonOsaAtom.get())} okTextKey="Lisää tutkinnon osa" validP={tutkinnonOsaAtom} submitOnEnterKey="false">
      <h2><Text name="Tutkinnon osan lisäys toisesta tutkinnosta"/></h2>
      <div className="valinnat">
        <TutkintoAutocomplete tutkintoAtom={tutkintoAtom} oppilaitosP={Bacon.constant(oppilaitos)} />
        { ift(osatP.map('.length'),
          <LisääTutkinnonOsaDropdown selectedAtom={tutkinnonOsaAtom} osat={osatP}/>
        )}
      </div>

    </ModalDialog>)
    }
  </span>)
}

const fetchLisättävätTutkinnonOsat = (diaarinumero, suoritustapa, groupId) => Http
  .cachedGet(`/koski/api/tutkinnonperusteet/tutkinnonosat/${encodeURIComponent(diaarinumero)}/${encodeURIComponent(suoritustapa)}` + (!groupId || groupId == placeholderForNonGrouped ? '' : '/'  + encodeURIComponent(groupId)))

const LisääTutkinnonOsaDropdown = ({selectedAtom, osat}) => {
  return (<KoodistoDropdown
    className="tutkinnon-osat"
    options={osat}
    selected={ selectedAtom.view(enumValueToKoodiviiteLens) }
    enableFilter="true"
    selectionText={ t('Lisää tutkinnon osa')}
    showKoodiarvo="true"
  />)
}

