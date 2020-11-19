import React from 'baret'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Http from '../util/http'
import {AikajaksoRaportti} from './AikajaksoRaportti'
import {VuosiluokkaRaporttiPaivalta} from './VuosiluokkaRaporttiPaivalta'
import {AikajaksoRaporttiAikarajauksella, osasuoritusTypes} from './AikajaksoRaporttiAikarajauksella'
import {RaporttiPaivalta} from './RaporttiPaivalta'
import {AikuistenPerusopetuksenRaportit} from './AikuistenPerusopetuksenRaportit'
import {Tabs} from '../components/Tabs'
import Dropdown from '../components/Dropdown'

const kaikkiRaportitKategorioittain = [
  {
    tab: 'Esiopetus',
    heading: 'Esiopetuksen raportit',
    raportit: [
      {
        id: 'esiopetuksenraportti',
        name: 'Opiskeluoikeudet ja suoritustiedot',
        component: EsiopetusRaportti
      },
      {
        id: 'esiopetuksenoppijamäärienraportti',
        name: 'VOS-rahoituslaskennan tunnusluvut',
        component: EsiopetuksenOppijamäärätRaportti
      }
    ]
  },
  {
    tab: 'Perusopetus',
    heading: 'Perusopetuksen raportit',
    raportit: [
      {
        id: 'perusopetuksenvuosiluokka',
        name: 'Opiskeluoikeudet ja suoritustiedot',
        component: PerusopetuksenVuosiluokka
      },
      {
        id: 'perusopetuksenoppijamääräraportti',
        name: 'VOS-rahoituslaskennan tunnusluvut',
        component: PerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'perusopetuksenlisäopetuksenoppijamääräraportti',
        name: 'Lisäopetuksen VOS-rahoituslaskennan tunnusluvut',
        component: PerusopetuksenLisäopetuksenOppijamäärätRaportti
      }
    ]
  },
  {
    tab: 'Aikuisten perusopetus',
    heading: 'Aikuisten perusopetuksen raportit',
    raportit: [
      {
        id: 'aikuistenperusopetussuoritustietojentarkistus',
        name: 'Tarkistusraportti',
        component: AikuistenPerusopetusRaportti
      },
      {
        id: 'aikuistenperusopetusoppijamäärienraportti',
        name: 'VOS-rahoituslaskennan tunnusluvut',
        component: AikuistenPerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'aikuistenperusopetuskurssikertymänraportti',
        name: 'Kurssikertymä',
        component: AikuistenPerusopetuksenKurssikertymäRaportti
      }
    ]
  },
  {
    tab: 'Ammatillinen',
    heading: 'Ammatillisen koulutuksen raportit',
    raportit: [
      {
        id: 'ammatillinenopiskelijavuositiedot',
        name: 'Opiskelijavuositiedot',
        component: Opiskelijavuositiedot
      },
      {
        id: 'ammatillinentutkintosuoritustietojentarkistus',
        name: 'Suoritustiedot (koko tutkinto)',
        component: SuoritustietojenTarkistus
      },
      {
        id: 'ammatillinenosittainensuoritustietojentarkistus',
        name: 'Suoritustiedot (tutk. osia)',
        component: AmmatillinenOsittainenSuoritustietojenTarkistus
      },
      {
        id: 'muuammatillinenkoulutus',
        name: 'Suoritustiedot (muu ammatillinen)',
        component: MuuAmmatillinenRaportti
      },
      {
        id: 'topksammatillinen',
        name: 'Suoritustiedot (TOPKS)',
        component: TOPKSAmmatillinenRaportti
      }
    ]
  },
  {
    tab: 'Lukio',
    heading: 'Lukion raportit',
    raportit: [
      {
        id: 'lukionsuoritustietojentarkistus',
        name: 'Tarkistusraportti',
        component: Lukioraportti
      },
      {
        id: 'lukiokurssikertyma',
        name: 'VOS-tunnusluvut: Kurssikertymät',
        component: LukioKurssikertyma
      },
      {
        id: 'lukiodiaibinternationalopiskelijamaarat',
        name: 'VOS-tunnusluvut: Opiskelijamäärät',
        component: LukioDiaIBInternationalOpiskelijamaarat
      },
      {
        id: 'luvaopiskelijamaarat',
        name: 'VOS-tunnusluvut: Valmistavan koulutuksen opiskelijamäärät',
        component: LuvaOpiskelijamaaratRaportti
      }
    ]
  }
]

const getEnrichedRaportitKategorioittain = (organisaatiot) =>
  kaikkiRaportitKategorioittain.map(tab => {
    const visibleRaportit = tab.raportit.map(raportti => {
      const visibleOrganisaatiot = organisaatiot.filter(org => org.raportit.includes(raportti.id))
      return {
        ...raportti,
        visible: visibleOrganisaatiot.length > 0,
        organisaatiot: visibleOrganisaatiot
      }
    })

    return {
      ...tab,
      raportit: visibleRaportit,
      visible: visibleRaportit.length > 0
    }
  })

export const raportitContentP = () => {
  const organisaatiotP = Http.cachedGet('/koski/api/raportit/organisaatiot-ja-raporttityypit')
  const selectedTabIdxE = new Bacon.Bus()
  const selectedRaporttiIdxE = new Bacon.Bus()
  const selectedOrganisaatioOidE = new Bacon.Bus()

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
      const selectedTabIdx = tabs.findIndex(r => r.visible) || 0
      const selectedRaporttiIdx = 0

      return {
        ...state,
        selectedTabIdx,
        selectedRaporttiIdx,
        selectedOrganisaatio: tabs[selectedTabIdx].raportit[selectedRaporttiIdx].organisaatiot[0],
        tabs,
        organisaatiot
      }
    },
    selectedTabIdxE.skipDuplicates(), (state, selectedTabIdx) => {
      const organisaatiot = state.tabs[selectedTabIdx].raportit[0].organisaatiot
      return {
        ...state,
        selectedTabIdx,
        selectedRaporttiIdx: 0,
        selectedOrganisaatio: organisaatiot.find(org => org.oid === state.selectedOrganisaatio.oid) || organisaatiot[0]
      }
    },
    selectedRaporttiIdxE.skipDuplicates(), (state, selectedRaporttiIdx) => {
      const organisaatiot = state.tabs[state.selectedTabIdx].raportit[selectedRaporttiIdx].organisaatiot
      return {
        ...state,
        selectedRaporttiIdx,
        selectedOrganisaatio: organisaatiot.find(org => org.oid === state.selectedOrganisaatio.oid) || organisaatiot[0]
      }
    },
    selectedOrganisaatioOidE.skipDuplicates(), (state, selectedOrganisaatioOid) => ({
      ...state,
      selectedOrganisaatio: state.organisaatiot.find(org => org.oid === selectedOrganisaatioOid)
    })
  )

  const tabP = stateP.map(state => state.tabs[state.selectedTabIdx]).skipDuplicates()
  const raporttiP = Bacon.combineWith(stateP, tabP, (state, tab) => tab && tab.raportit[state.selectedRaporttiIdx]).skipDuplicates()
  const raporttiComponentP = raporttiP.map(raportti => raportti && raportti.component)

  return Bacon.constant({
    content: (
      <div className='content-area raportit'>
        <div className='main-content'>
          <Tabs
            optionsP={stateP.map(state => state.tabs.map((r, index) => ({
              id: index,
              name: r.tab,
              hidden: !r.visible
            })))}
            selectedP={stateP.map(state => state.selectedTabIdx)}
            onSelect={id => selectedTabIdxE.push(id)}
          />
          {tabP.map(tab => tab && <h2>{tab.heading}</h2>)}
          <RaporttiValitsin
            raportitP={tabP.map(tab => tab ? tab.raportit : [])}
            selectedP={stateP.map(state => state.selectedRaporttiIdx)}
            onSelect={idx => selectedRaporttiIdxE.push(idx)}
          />
          <OrganisaatioValitsin
            organisaatiotP={raporttiP.map(raportti => raportti ? raportti.organisaatiot : [])}
            selectedP={stateP.map(state => state.selectedOrganisaatio)}
            onSelect={org => selectedOrganisaatioOidE.push(org.oid)}
          />
          {raporttiComponentP.map(RC => RC
            ? <RC organisaatioP={stateP.map(state => state.selectedOrganisaatio)} />
            : null
          )}
        </div>
      </div>
    ),
    title: 'Raportit'
  })
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

const OrganisaatioValitsin = ({ organisaatiotP, selectedP, onSelect }) => (
  <div className="organisaatio-valitsin">
    <p><Text name="Valitse organisaatio" /></p>
    <Dropdown
      options={organisaatiotP}
      keyValue={org => org.oid}
      displayValue={org => t(org.nimi)}
      onSelectionChanged={onSelect}
      selected={selectedP}
      enableFilter
    />
  </div>
)

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

  return (<AikajaksoRaportti
    organisaatioP={organisaatioP}
    apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
    title={titleText}
    description={descriptionText} />)
}

function SuoritustietojenTarkistus({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const descriptionText = <Text name='SuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioP={organisaatioP}
    apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

function AmmatillinenOsittainenSuoritustietojenTarkistus({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)' />
  const descriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-description' />

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioP={organisaatioP}
    apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText} />)
}

function MuuAmmatillinenRaportti({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (muu ammatillinen koulutus)' />
  const descriptionText = <Text name='muuammatillinenraportti-description' />

  return (<AikajaksoRaportti
    organisaatioP={organisaatioP}
    apiEndpoint={'/muuammatillinen'}
    title={titleText}
    description={descriptionText} />)
}

function TOPKSAmmatillinenRaportti({ organisaatioP }) {
  const titleText = <Text name='Suoritustiedot (TOPKS ammatillinen koulutus)' />
  const descriptionText = <Text name='topksammatillinen-description' />

  return (<AikajaksoRaportti
    organisaatioP={organisaatioP}
    apiEndpoint={'/topksammatillinen'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenVuosiluokka({ organisaatioP }) {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti' />
  const descriptionText = <Text name='PerusopetuksenVuosiluokka-description' />
  const exampleText = <Text name='PerusopetuksenVuosiluokka-example' />

  return (<VuosiluokkaRaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/perusopetuksenvuosiluokka'}
    title={titleText}
    description={descriptionText}
    example={exampleText} />)
}

function Lukioraportti({ organisaatioP }) {
  const titleText = <Text name='Lukioraportti-title' />
  const descriptionText = <Text name='Lukioraportti-description' />

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioP={organisaatioP}
    apiEndpoint={'/lukionsuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
    osasuoritusType={osasuoritusTypes.KURSSI} />)
}

function LukioKurssikertyma({ organisaatioP }) {
  return (<AikajaksoRaportti organisaatioP={organisaatioP}
    apiEndpoint={'/lukiokurssikertymat'}
    title={<Text name='lukion-kurssikertyma-title' />}
    description={<Text name='lukion-kurssikertyma-description' />} />)
}

function LukioDiaIBInternationalOpiskelijamaarat({ organisaatioP }) {
  const titleText = <Text name='lukiokoulutuksen-opiskelijamaarat-title' />
  const descriptionText = <Text name='lukiokoulutuksen-opiskelijamaarat-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/lukiodiaibinternationalopiskelijamaarat'}
    title={titleText}
    description={descriptionText} />)
}

function LuvaOpiskelijamaaratRaportti({ organisaatioP }) {
  const titleText = <Text name='luva-opiskelijamaarat-title' />
  const descriptionText = <Text name='luva-opiskelijamaarat-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/luvaopiskelijamaarat'}
    title={titleText}
    description={descriptionText} />)
}

function EsiopetusRaportti({ organisaatioP }) {
  const titleText = <Text name='esiopetusraportti-title' />
  const descriptionText = <Text name='esiopetusraportti-description' />
  const exampleText = <Text name='esiopetusraportti-example' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/esiopetus'}
    title={titleText}
    description={descriptionText}
    example={exampleText} />)
}

function EsiopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Esiopetus-oppilasmäärät-raportti-title' />
  const descriptionText = <Text name='Esiopetus-oppilasmäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/esiopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Perusopetus-oppijamäärät-raportti-title' />
  const descriptionText = <Text name='Perusopetus-oppijamäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/perusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenLisäopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-title' />
  const descriptionText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/perusopetuksenlisaopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetusRaportti({ organisaatioP }) {
  const titleText = <Text name='aikuisten-perusopetus-raportti-title' />
  const descriptionText = <Text name='aikuisten-perusopetus-raportti-description' />

  return (<AikuistenPerusopetuksenRaportit
    organisaatioP={organisaatioP}
    apiEndpoint={'/aikuisten-perusopetus-suoritustietojen-tarkistus'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetuksenOppijamäärätRaportti({ organisaatioP }) {
  const titleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-title' />
  const descriptionText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioP={organisaatioP}
    apiEndpoint={'/aikuistenperusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetuksenKurssikertymäRaportti({ organisaatioP }) {
  const titleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-title' />
  const descriptionText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-description' />

  return (<AikajaksoRaportti
    organisaatioP={organisaatioP}
    apiEndpoint={'/aikuistenperusopetuksenkurssikertymaraportti'}
    title={titleText}
    description={descriptionText} />)
}
