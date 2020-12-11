import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Http from '../util/http'
import {AikajaksoRaportti} from './AikajaksoRaportti'
import {VuosiluokkaRaporttiPaivalta} from './VuosiluokkaRaporttiPaivalta'
import {AikajaksoRaporttiAikarajauksella, osasuoritusTypes} from './AikajaksoRaporttiAikarajauksella'
import {RaporttiPaivalta} from './RaporttiPaivalta'
import {AikuistenPerusopetuksenRaportit} from './AikuistenPerusopetuksenRaportit'
import {Tabs} from '../components/Tabs'
import { OrganisaatioDropdown } from './OrganisaatioDropdown'
import {filterOrgTreeByRaporttityyppi} from './raporttiUtils'
import { contentWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { replaceLocation } from '../util/location'
import { Paragraphs } from '../i18n/Paragraphs'

const kaikkiRaportitKategorioittain = [
  {
    id: 'esiopetus',
    tab: 'raporttikategoria-tab-esiopetus',
    heading: 'raporttikategoria-heading-esiopetus',
    raportit: [
      {
        id: 'esiopetuksenraportti',
        name: 'raportti-tab-esiopetuksenraportti',
        component: EsiopetusRaportti
      },
      {
        id: 'esiopetuksenoppijamäärienraportti',
        name: 'raportti-tab-esiopetuksenoppijamäärienraportti',
        component: EsiopetuksenOppijamäärätRaportti
      }
    ]
  },
  {
    id: 'perusopetus',
    tab: 'raporttikategoria-tab-perusopetus',
    heading: 'raporttikategoria-heading-perusopetus',
    raportit: [
      {
        id: 'perusopetuksenvuosiluokka',
        name: 'raportti-tab-perusopetuksenvuosiluokka',
        component: PerusopetuksenVuosiluokka
      },
      {
        id: 'perusopetuksenoppijamääräraportti',
        name: 'raportti-tab-perusopetuksenoppijamääräraportti',
        component: PerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'perusopetuksenlisäopetuksenoppijamääräraportti',
        name: 'raportti-tab-perusopetuksenlisäopetuksenoppijamääräraportti',
        component: PerusopetuksenLisäopetuksenOppijamäärätRaportti
      }
    ]
  },
  {
    id: 'aikuisten-perusopetus',
    tab: 'raporttikategoria-tab-aikuisten-perusopetus',
    heading: 'raporttikategoria-heading-aikuisten-perusopetus',
    raportit: [
      {
        id: 'aikuistenperusopetussuoritustietojentarkistus',
        name: 'raportti-tab-aikuistenperusopetussuoritustietojentarkistus',
        component: AikuistenPerusopetusRaportti
      },
      {
        id: 'aikuistenperusopetusoppijamäärienraportti',
        name: 'raportti-tab-aikuistenperusopetusoppijamäärienraportti',
        component: AikuistenPerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'aikuistenperusopetuskurssikertymänraportti',
        name: 'raportti-tab-aikuistenperusopetuskurssikertymänraportti',
        component: AikuistenPerusopetuksenKurssikertymäRaportti
      }
    ]
  },
  {
    id: 'ammatillinen-koulutus',
    tab: 'raporttikategoria-tab-ammatillinen-koulutus',
    heading: 'raporttikategoria-heading-ammatillinen-koulutus',
    raportit: [
      {
        id: 'ammatillinenopiskelijavuositiedot',
        name: 'raportti-tab-ammatillinenopiskelijavuositiedot',
        component: Opiskelijavuositiedot
      },
      {
        id: 'ammatillinentutkintosuoritustietojentarkistus',
        name: 'raportti-tab-ammatillinentutkintosuoritustietojentarkistus',
        component: SuoritustietojenTarkistus
      },
      {
        id: 'ammatillinenosittainensuoritustietojentarkistus',
        name: 'raportti-tab-ammatillinenosittainensuoritustietojentarkistus',
        component: AmmatillinenOsittainenSuoritustietojenTarkistus
      },
      {
        id: 'muuammatillinenkoulutus',
        name: 'raportti-tab-muuammatillinenkoulutus',
        component: MuuAmmatillinenRaportti
      },
      {
        id: 'topksammatillinen',
        name: 'raportti-tab-topksammatillinen',
        component: TOPKSAmmatillinenRaportti
      }
    ]
  },
  {
    id: 'lukio',
    tab: 'raporttikategoria-tab-lukio',
    heading: 'raporttikategoria-heading-lukio',
    raportit: [
      {
        id: 'lukionsuoritustietojentarkistus',
        name: 'raportti-tab-lukionsuoritustietojentarkistus',
        component: Lukioraportti
      },
      {
        id: 'lukiokurssikertyma',
        name: 'raportti-tab-lukiokurssikertyma',
        component: LukioKurssikertyma
      },
      {
        id: 'lukiodiaibinternationalopiskelijamaarat',
        name: 'raportti-tab-lukiodiaibinternationalopiskelijamaarat',
        component: LukioDiaIBInternationalOpiskelijamaarat
      },
      {
        id: 'luvaopiskelijamaarat',
        name: 'raportti-tab-luvaopiskelijamaarat',
        component: LuvaOpiskelijamaaratRaportti
      }
    ]
  },
  {
    id: 'muut',
    tab: 'raporttikategoria-tab-muut',
    heading: 'raportti-tab-paallekkaisetopiskeluoikeudet',
    raportit: [
      {
        id: 'paallekkaisetopiskeluoikeudet',
        name: 'raportti-tab-paallekkaisetopiskeluoikeudet',
        component: PaallekkaisetOpiskeluoikeudet,
        visibleForAllOrgs: true,
        guard: () => document.location.search.includes('tilastoraportit=true')
      }
    ]
  }
]

const getEnrichedRaportitKategorioittain = (organisaatiot) =>
  kaikkiRaportitKategorioittain.map(tab => {
    const raportit = tab.raportit.map(raportti => {
      const visibleOrganisaatiot = raportti.visibleForAllOrgs
        ? organisaatiot
        : organisaatiot.filter(org => org.raportit.includes(raportti.id))
      return {
        ...raportti,
        visible: (raportti.visibleForAllOrgs || visibleOrganisaatiot.length > 0) && (raportti.guard ? raportti.guard() : true),
        organisaatiot: visibleOrganisaatiot
      }
    })

    return {
      ...tab,
      raportit: raportit,
      visible: raportit.filter(r => r.visible).length > 0
    }
  })

const organiaatiotTreeIncludes = (organisaatiot, oid) =>
  organisaatiot.some(org => org.oid === oid || organiaatiotTreeIncludes(org.children, oid))

const preselectOrganisaatio = (raportti, selectedOrganisaatio) => {
  if (raportti.visibleForAllOrgs) {
    return selectedOrganisaatio
  }
  const filteredOrganisaatiot = filterOrgTreeByRaporttityyppi(raportti.id, raportti.organisaatiot)
  return selectedOrganisaatio && organiaatiotTreeIncludes(filteredOrganisaatiot, selectedOrganisaatio.oid)
          ? selectedOrganisaatio
          : filteredOrganisaatiot[0]
}

const findIndexById = (arr, value) => {
  const index = arr.findIndex(item => item.id === value)
  return index >= 0 ? index : null
}

const getInitialState = (pathTokens) => {
  const [pathKategoriaId, pathRaporttiId] = pathTokens
  const tabIdxByPath = findIndexById(kaikkiRaportitKategorioittain, pathKategoriaId)
  const raporttiIdxByPath = tabIdxByPath && findIndexById(kaikkiRaportitKategorioittain[tabIdxByPath].raportit, pathRaporttiId)

  return {
    tabIdxByPath,
    raporttiIdxByPath,
    selectedTabIdx: 0,
    selectedRaporttiIdx: 0,
    selectedOrganisaatio: null,
    tabs: [],
    organisaatiot: [],
    dbUpdated: null
  }
}

export const raportitContentP = (pathTokens) => {
  const organisaatiotP = Http.cachedGet('/koski/api/raportit/organisaatiot-ja-raporttityypit')
  const dbUpdatedP = Http.get('/koski/api/raportit/paivitysaika')
  const selectedTabIdxE = new Bacon.Bus()
  const selectedRaporttiIdxE = new Bacon.Bus()
  const selectedOrganisaatioE = new Bacon.Bus()

  const stateP = Bacon.update(
    getInitialState(pathTokens),
    dbUpdatedP.toEventStream(), (state, dbUpdated) => ({
      ...state,
      dbUpdated
    }),
    organisaatiotP.toEventStream(), (state, organisaatiot) => {
      const tabs = getEnrichedRaportitKategorioittain(organisaatiot)
      const selectedTabIdx = state.tabIdxByPath && tabs[state.tabIdxByPath].visible
        ? state.tabIdxByPath
        : tabs.findIndex(r => r.visible)
      const selectedRaporttiIdx = state.raporttiIdxByPath && tabs[selectedTabIdx].raportit[state.raporttiIdxByPath].visible
        ? state.raporttiIdxByPath
        : 0

      return {
        ...state,
        selectedTabIdx,
        selectedRaporttiIdx,
        selectedOrganisaatio: selectedTabIdx >= 0
          ? tabs[selectedTabIdx].raportit[selectedRaporttiIdx].organisaatiot[0]
          : null,
        tabs,
        organisaatiot
      }
    },
    selectedTabIdxE.skipDuplicates(), (state, selectedTabIdx) => {
      const tab = state.tabs[selectedTabIdx]
      const selectedRaporttiIdx = tab.raportit.findIndex(r => r.visible)
      return {
        ...state,
        selectedTabIdx,
        selectedRaporttiIdx,
        selectedOrganisaatio: preselectOrganisaatio(tab.raportit[selectedRaporttiIdx], state.selectedOrganisaatio)
      }
    },
    selectedRaporttiIdxE, (state, selectedRaporttiIdx) => {
      return {
        ...state,
        selectedRaporttiIdx,
        selectedOrganisaatio: preselectOrganisaatio(state.tabs[state.selectedTabIdx].raportit[selectedRaporttiIdx], state.selectedOrganisaatio)
      }
    },
    selectedOrganisaatioE.skipDuplicates(), (state, selectedOrganisaatio) => ({
      ...state,
      selectedOrganisaatio
    })
  )

  stateP.filter(x => x).forEach(state => {
    const tab = state.tabs[state.selectedTabIdx]
    const raportti = tab && tab.raportit[state.selectedRaporttiIdx]
    if (tab && raportti) {
      replaceLocation(`/koski/raportit/${tab.id}/${raportti.id}`)
    }
  })

  return contentWithLoadingIndicator(organisaatiotP.map(() => ({
    title: 'Raportit',
    content: (
      <div className='content-area raportit'>
        <RaportitContent
          stateP={stateP}
          onSelectTab={index => selectedTabIdxE.push(index)}
          onSelectRaportti={index => selectedRaporttiIdxE.push(index)}
          onSelectOrganisaatio={org => selectedOrganisaatioE.push(org)}
        />
      </div>
    )
  })))
}

const RaportitContent = ({
  stateP,
  onSelectTab,
  onSelectRaportti,
  onSelectOrganisaatio
}) => {
  const tabP = stateP.map(state => state.tabs[state.selectedTabIdx]).skipDuplicates()
  const raporttiP = Bacon.combineWith(stateP, tabP, (state, tab) => tab && tab.raportit[state.selectedRaporttiIdx]).skipDuplicates()
  const raporttiComponentP = raporttiP.map(raportti => raportti && raportti.component)

  return (
    <div className='main-content'>
      {stateP.map(state => state.organisaatiot.length > 0
        ? (
          <Tabs
            optionsP={stateP.map(s => s.tabs.map((r, index) => ({
              id: index,
              name: <Text name={r.tab} />,
              hidden: !r.visible
            })))}
            selectedP={stateP.map(s => s.selectedTabIdx)}
            onSelect={onSelectTab}
          />
        ) : (
          <div className="error">
            <Text name="virhe-ei-organisaatiokayttaoikeuksia" />
          </div>
        )
      )}
      {tabP.map(tab => tab && <h2><Text name={tab.heading} /></h2>)}
      <RaporttiValitsin
        raportitP={tabP.map(tab => tab ? tab.raportit : [])}
        selectedP={stateP.map(state => state.selectedRaporttiIdx)}
        onSelect={onSelectRaportti}
      />
      <OrganisaatioValitsin
        raporttiP={raporttiP}
        selectedP={stateP.map(state => state.selectedOrganisaatio)}
        onSelect={onSelectOrganisaatio}
      />
      {raporttiComponentP.map(RC => RC ? <RC stateP={stateP} /> : null)}
    </div>
  )
}

const RaporttiValitsin = ({ raportitP, selectedP, onSelect }) => (
  <div className="raportti-valitsin">
    {raportitP.map(raportit => raportit.length < 2 ? null : (
      <ul className="pills-container">
        {raportit.map((raportti, index) => (
          raportti.visible
            ? (
              <li
                key={raportti.id}
                onClick={() => onSelect(index)}
                className={selectedP.map(selectedIdx => index === selectedIdx ? 'pills-item pills-item-selected' : 'pills-item')}
              >
                <Text name={raportti.compactName || raportti.name} />
              </li>
            ) : null
        ))}
      </ul>
    ))}
  </div>
)

const OrganisaatioValitsin = ({ raporttiP, selectedP, onSelect }) => {
  const organisaatiotP = raporttiP.map(raportti => {
      if (!raportti) {
        return []
      }
      return raportti.visibleForAllOrgs
        ? raportti.organisaatiot
        : filterOrgTreeByRaporttityyppi(raportti.id, raportti.organisaatiot)
    }
  )

  return (
    <div className="organisaatio-valitsin">
      {organisaatiotP.map(organisaatiot => organisaatiot.length === 0 ? null : (
        <OrganisaatioDropdown
          organisaatiotP={organisaatiotP}
          selectedP={selectedP}
          onSelect={onSelect}
        />
      ))}
    </div>
  )
}

function PaallekkaisetOpiskeluoikeudet({ stateP }) {
  return (
    <AikajaksoRaportti
      stateP={stateP}
      apiEndpoint={'/paallekkaisetopiskeluoikeudet'}
      title={<Text name='paallekkaiset-opiskeluoikeudet'/>}
      description={<Text name='paallekkaiset-opiskeluoikeudet'/>}
    />
  )
}

function Opiskelijavuositiedot({ stateP }) {
  const titleText = <Text name='Opiskelijavuositiedot' />
  const descriptionText = <Text name='Opiskelijavuositiedot-description' />

  return (
    <AikajaksoRaportti
      stateP={stateP}
      apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function SuoritustietojenTarkistus({ stateP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const shortDescriptionText = <Text name='SuoritustietojenTarkistus-short-description'/>
  const exampleText = <Paragraphs name='SuoritustietojenTarkistus-example'/>

  return (
    <AikajaksoRaporttiAikarajauksella
      stateP={stateP}
      apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function AmmatillinenOsittainenSuoritustietojenTarkistus({ stateP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)' />
  const shortDescriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-short-description' />
  const exampleText = <Paragraphs name='AmmatillinenOsittainenSuoritustietojenTarkistus-example' />

  return (
    <AikajaksoRaporttiAikarajauksella
      stateP={stateP}
      apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function MuuAmmatillinenRaportti({ stateP }) {
  const titleText = <Text name='Suoritustiedot (muu ammatillinen koulutus)' />
  const descriptionText = <Text name='muuammatillinenraportti-description' />

  return (
    <AikajaksoRaportti
      stateP={stateP}
      apiEndpoint={'/muuammatillinen'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function TOPKSAmmatillinenRaportti({ stateP }) {
  const titleText = <Text name='Suoritustiedot (TOPKS ammatillinen koulutus)' />
  const descriptionText = <Text name='topksammatillinen-description' />

  return (
    <AikajaksoRaportti
      stateP={stateP}
      apiEndpoint={'/topksammatillinen'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function PerusopetuksenVuosiluokka({ stateP }) {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti' />
  const shortDescriptionText = <Text name='PerusopetuksenVuosiluokka-short-description' />
  const dateInputHelpText = <Text name='PerusopetuksenVuosiluokka-date-input-help' />
  const helpText = <Paragraphs name='PerusopetuksenVuosiluokka-help' />
  const exampleText = <Paragraphs name='PerusopetuksenVuosiluokka-example' />

  return (
    <VuosiluokkaRaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/perusopetuksenvuosiluokka'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      help={helpText}
      example={exampleText}
    />
  )
}

function Lukioraportti({ stateP }) {
  const titleText = <Text name='Lukioraportti-title' />
  const shortDescriptionText = <Text name='Lukioraportti-short-description' />
  const exampleText = <Text name='Lukioraportti-example' />

  return (
    <AikajaksoRaporttiAikarajauksella
      stateP={stateP}
      apiEndpoint={'/lukionsuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
      osasuoritusType={osasuoritusTypes.KURSSI}
    />
  )
}

function LukioKurssikertyma({ stateP }) {
  const title = <Text name='lukion-kurssikertyma-title' />
  const shortDescriptionText = <Text name='lukion-kurssikertyma-short-description' />
  const dateInputHelpText = <Text name='lukion-kurssikertyma-date-input-help' />
  const exampleText = <Paragraphs name='Lukioraportti-example' />

  return (
    <AikajaksoRaportti stateP={stateP}
      apiEndpoint={'/lukiokurssikertymat'}
      title={title}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function LukioDiaIBInternationalOpiskelijamaarat({ stateP }) {
  const titleText = <Text name='lukiokoulutuksen-opiskelijamaarat-title' />
  const shortDescriptionText = <Text name='lukiokoulutuksen-opiskelijamaarat-short-description' />
  const dateInputHelpText = <Text name='lukiokoulutuksen-opiskelijamaarat-date-input-help' />
  const exampleText = <Paragraphs name='lukiokoulutuksen-opiskelijamaarat-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/lukiodiaibinternationalopiskelijamaarat'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function LuvaOpiskelijamaaratRaportti({ stateP }) {
  const titleText = <Text name='luva-opiskelijamaarat-title' />
  const shortDescriptionText = <Text name='luva-opiskelijamaarat-short-description' />
  const dateInputHelpText = <Text name='luva-opiskelijamaarat-date-input-help' />
  const exampleText = <Paragraphs name='luva-opiskelijamaarat-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/luvaopiskelijamaarat'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function EsiopetusRaportti({ stateP }) {
  const titleText = <Text name='esiopetusraportti-title' />
  const shortDescriptionText = <Text name="esiopetusraportti-short-description" />
  const dateInputHelpText = <Text name="esiopetusraportti-date-input-help" />
  const exampleText = <Paragraphs name='esiopetusraportti-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/esiopetus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function EsiopetuksenOppijamäärätRaportti({ stateP }) {
  const titleText = <Text name='Esiopetus-oppilasmäärät-raportti-title' />
  const shortDescriptionText = <Text name='Esiopetus-oppilasmäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Esiopetus-oppilasmäärät-raportti-date-input-help' />
  const exampleText = <Paragraphs name='Esiopetus-oppilasmäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/esiopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function PerusopetuksenOppijamäärätRaportti({ stateP }) {
  const titleText = <Text name='Perusopetus-oppijamäärät-raportti-title' />
  const shortDescriptionText = <Text name='Perusopetus-oppijamäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Perusopetus-oppijamäärät-raportti-date-input-help' />
  const exampleText = <Paragraphs name='Perusopetus-oppijamäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/perusopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function PerusopetuksenLisäopetuksenOppijamäärätRaportti({ stateP }) {
  const titleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-title' />
  const shortDescriptionText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-date-input-help' />
  const exampleText = <Paragraphs name='Perusopetus-lisäopetus-oppijamäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/perusopetuksenlisaopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetusRaportti({ stateP }) {
  const titleText = <Text name='aikuisten-perusopetus-raportti-title' />
  const shortDescriptionText = <Text name='aikuisten-perusopetus-raportti-short-description' />
  const exampleText = <Paragraphs name='aikuisten-perusopetus-raportti-example' />

  return (
    <AikuistenPerusopetuksenRaportit
      stateP={stateP}
      apiEndpoint={'/aikuisten-perusopetus-suoritustietojen-tarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetuksenOppijamäärätRaportti({ stateP }) {
  const titleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-title' />
  const shortDescriptionText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-date-input-help' />
  const exampleText = <Paragraphs name='Aikuisten-perusopetus-oppilasmäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      stateP={stateP}
      apiEndpoint={'/aikuistenperusopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetuksenKurssikertymäRaportti({ stateP }) {
  const titleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-title' />
  const shortDescriptionText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-short-description' />
  const dateInputHelpText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-date-input-help' />
  const exampleText = <Paragraphs name='Aikuisten-perusopetus-kurssikertymä-raportti-example' />

  return (
    <AikajaksoRaportti
      stateP={stateP}
      apiEndpoint={'/aikuistenperusopetuksenkurssikertymaraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}
