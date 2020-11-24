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

const kaikkiRaportitKategorioittain = [
  {
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

export const raportitContentP = () => {
  const organisaatiotP = Http.cachedGet('/koski/api/raportit/organisaatiot-ja-raporttityypit')
  const selectedTabIdxE = new Bacon.Bus()
  const selectedRaporttiIdxE = new Bacon.Bus()
  const selectedOrganisaatioE = new Bacon.Bus()

  const stateP = Bacon.update(
    {
      selectedTabIdx: 0,
      selectedRaporttiIdx: 0,
      selectedOrganisaatio: null,
      tabs: [],
      organisaatiot: []
    },
    organisaatiotP.toEventStream(), (state, organisaatiot) => {
      const tabs = getEnrichedRaportitKategorioittain(organisaatiot)
      const selectedTabIdx = tabs.findIndex(r => r.visible)
      const selectedRaporttiIdx = 0

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
  })),
  )
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
      <Tabs
        optionsP={stateP.map(state => state.tabs.map((r, index) => ({
          id: index,
          name: <Text name={r.tab} />,
          hidden: !r.visible
        })))}
        selectedP={stateP.map(state => state.selectedTabIdx)}
        onSelect={onSelectTab}
      />
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
      {raporttiComponentP.map(RC => RC
        ? <RC organisaatioP={stateP.map(state => state.selectedOrganisaatio)} />
        : null
      )}
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

function PaallekkaisetOpiskeluoikeudet({ organisaatioP }) {
  return (
    <AikajaksoRaportti
      organisaatioP={organisaatioP}
      apiEndpoint={'/paallekkaisetopiskeluoikeudet'}
      title={<Text name='paallekkaiset-opiskeluoikeudet'/>}
      description={<Text name='paallekkaiset-opiskeluoikeudet'/>}
    />
  )
}

function Opiskelijavuositiedot({ organisaatioP }) {
  const titleText = <Text name='Opiskelijavuositiedot' />
  const descriptionText = <Text name='Opiskelijavuositiedot-description' />

  return (
    <AikajaksoRaportti
      organisaatioP={organisaatioP}
      apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function SuoritustietojenTarkistus({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const shortDescriptionText = <Text name='SuoritustietojenTarkistus-short-description'/>
  const exampleText = <Text name='SuoritustietojenTarkistus-example'/>

  return (
    <AikajaksoRaporttiAikarajauksella
      organisaatioP={organisaatioP}
      apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function AmmatillinenOsittainenSuoritustietojenTarkistus({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)' />
  const shortDescriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-short-description' />
  const exampleText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-example' />

  return (
    <AikajaksoRaporttiAikarajauksella
      organisaatioP={organisaatioP}
      apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function MuuAmmatillinenRaportti({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (muu ammatillinen koulutus)' />
  const descriptionText = <Text name='muuammatillinenraportti-description' />

  return (
    <AikajaksoRaportti
      organisaatioP={organisaatioP}
      apiEndpoint={'/muuammatillinen'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function TOPKSAmmatillinenRaportti({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (TOPKS ammatillinen koulutus)' />
  const descriptionText = <Text name='topksammatillinen-description' />

  return (
    <AikajaksoRaportti
      organisaatioP={organisaatioP}
      apiEndpoint={'/topksammatillinen'}
      title={titleText}
      shortDescription={descriptionText}
    />
  )
}

function PerusopetuksenVuosiluokka({ organisaatioP }) {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti' />
  const shortDescriptionText = <Text name='PerusopetuksenVuosiluokka-short-description' />
  const dateInputHelpText = <Text name='PerusopetuksenVuosiluokka-date-input-help' />
  const helpText = <Text name='PerusopetuksenVuosiluokka-help' />
  const exampleText = <Text name='PerusopetuksenVuosiluokka-example' />

  return (
    <VuosiluokkaRaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/perusopetuksenvuosiluokka'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      help={helpText}
      example={exampleText}
    />
  )
}

function Lukioraportti({ organisaatioP }) {
  const titleText = <Text name='Lukioraportti-title' />
  const shortDescriptionText = <Text name='Lukioraportti-short-description' />
  const exampleText = <Text name='Lukioraportti-example' />

  return (
    <AikajaksoRaporttiAikarajauksella
      organisaatioP={organisaatioP}
      apiEndpoint={'/lukionsuoritustietojentarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
      osasuoritusType={osasuoritusTypes.KURSSI}
    />
  )
}

function LukioKurssikertyma({ organisaatioP }) {
  const title = <Text name='lukion-kurssikertyma-title' />
  const shortDescriptionText = <Text name='lukion-kurssikertyma-short-description' />
  const dateInputHelpText = <Text name='lukion-kurssikertyma-date-input-help' />
  const exampleText = <Text name='Lukioraportti-example' />

  return (
    <AikajaksoRaportti organisaatioP={organisaatioP}
      apiEndpoint={'/lukiokurssikertymat'}
      title={title}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function LukioDiaIBInternationalOpiskelijamaarat({ organisaatioP }) {
  const titleText = <Text name='lukiokoulutuksen-opiskelijamaarat-title' />
  const shortDescriptionText = <Text name='lukiokoulutuksen-opiskelijamaarat-short-description' />
  const dateInputHelpText = <Text name='lukiokoulutuksen-opiskelijamaarat-date-input-help' />
  const exampleText = <Text name='lukiokoulutuksen-opiskelijamaarat-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/lukiodiaibinternationalopiskelijamaarat'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function LuvaOpiskelijamaaratRaportti({ organisaatioP }) {
  const titleText = <Text name='luva-opiskelijamaarat-title' />
  const shortDescriptionText = <Text name='luva-opiskelijamaarat-short-description' />
  const dateInputHelpText = <Text name='luva-opiskelijamaarat-date-input-help' />
  const exampleText = <Text name='luva-opiskelijamaarat-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/luvaopiskelijamaarat'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function EsiopetusRaportti({ organisaatioP }) {
  const titleText = <Text name='esiopetusraportti-title' />
  const shortDescriptionText = <Text name="esiopetusraportti-short-description" />
  const dateInputHelpText = <Text name="esiopetusraportti-date-input-help" />
  const exampleText = <Text name='esiopetusraportti-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/esiopetus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function EsiopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Esiopetus-oppilasmäärät-raportti-title' />
  const shortDescriptionText = <Text name='Esiopetus-oppilasmäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Esiopetus-oppilasmäärät-raportti-date-input-help' />
  const exampleText = <Text name='Esiopetus-oppilasmäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/esiopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function PerusopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Perusopetus-oppijamäärät-raportti-title' />
  const shortDescriptionText = <Text name='Perusopetus-oppijamäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Perusopetus-oppijamäärät-raportti-date-input-help' />
  const exampleText = <Text name='Perusopetus-oppijamäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/perusopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function PerusopetuksenLisäopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-title' />
  const shortDescriptionText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-date-input-help' />
  const exampleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/perusopetuksenlisaopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetusRaportti({ organisaatioP }) {
  const titleText = <Text name='aikuisten-perusopetus-raportti-title' />
  const shortDescriptionText = <Text name='aikuisten-perusopetus-raportti-short-description' />
  const exampleText = <Text name='aikuisten-perusopetus-raportti-example' />

  return (
    <AikuistenPerusopetuksenRaportit
      organisaatioP={organisaatioP}
      apiEndpoint={'/aikuisten-perusopetus-suoritustietojen-tarkistus'}
      title={titleText}
      shortDescription={shortDescriptionText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-title' />
  const shortDescriptionText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-short-description' />
  const dateInputHelpText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-date-input-help' />
  const exampleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-example' />

  return (
    <RaporttiPaivalta
      organisaatioP={organisaatioP}
      apiEndpoint={'/aikuistenperusopetuksenoppijamaaratraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}

function AikuistenPerusopetuksenKurssikertymäRaportti({ organisaatioP }) {
  const titleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-title' />
  const shortDescriptionText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-short-description' />
  const dateInputHelpText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-date-input-help' />
  const exampleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-example' />

  return (
    <AikajaksoRaportti
      organisaatioP={organisaatioP}
      apiEndpoint={'/aikuistenperusopetuksenkurssikertymaraportti'}
      title={titleText}
      shortDescription={shortDescriptionText}
      dateInputHelp={dateInputHelpText}
      example={exampleText}
    />
  )
}
