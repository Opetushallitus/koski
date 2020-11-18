import React from 'baret'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import Atom from 'bacon.atom'
import Http from '../util/http'
import {AikajaksoRaportti} from './AikajaksoRaportti'
import {VuosiluokkaRaporttiPaivalta} from './VuosiluokkaRaporttiPaivalta'
import {AikajaksoRaporttiAikarajauksella, osasuoritusTypes} from './AikajaksoRaporttiAikarajauksella'
import {RaporttiPaivalta} from './RaporttiPaivalta'
import {AikuistenPerusopetuksenRaportit} from './AikuistenPerusopetuksenRaportit'
import {Tabs} from '../components/Tabs'

const raportitKategorioittain = [
  {
    tab: 'Esiopetus',
    heading: 'Esiopetuksen raportit',
    raportit: [
      {
        id: 'esiopetuksenraportti',
        name: 'Esiopetuksen raportti',
        component: EsiopetusRaportti
      },
      {
        id: 'esiopetuksenoppijamäärienraportti',
        name: 'Esiopetuksen oppijamäärien raportti',
        compactName: 'Oppijamäärät',
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
        name: 'Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti',
        compactName: 'Tarkistusraportti',
        component: PerusopetuksenVuosiluokka
      },
      {
        id: 'perusopetuksenoppijamääräraportti',
        name: 'Perusopetus-oppijamäärät-raportti-title',
        compactName: 'Oppijamäärät',
        component: PerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'perusopetuksenlisäopetuksenoppijamääräraportti',
        name: 'Perusopetus-lisäopetus-oppijamäärät-raportti-title',
        compactName: 'Lisäopetuksen oppijamäärät',
        component: PerusopetuksenLisäopetuksenOppijamäärätRaportti
      },
    ]
  },
  {
    tab: 'Aikuisten perusopetus',
    heading: 'Aikuisten perusopetuksen raportit',
    raportit: [
      {
        id: 'aikuistenperusopetussuoritustietojentarkistus',
        name: 'aikuisten-perusopetus-raportti-title',
        component: AikuistenPerusopetusRaportti
      },
      {
        id: 'aikuistenperusopetusoppijamäärienraportti',
        name: 'Aikuisten-perusopetus-oppilasmäärät-raportti-title',
        component: AikuistenPerusopetuksenOppijamäärätRaportti
      },
      {
        id: 'aikuistenperusopetuskurssikertymänraportti',
        name: 'Aikuisten-perusopetus-kurssikertymä-raportti-title',
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
        component: Opiskelijavuositiedot,
      },
      {
        id: 'ammatillinentutkintosuoritustietojentarkistus',
        name: 'Suoritustiedot (ammatillinen koulutus, koko tutkinto)',
        compactName: 'Suoritustiedot (koko tutkinto)',
        component: SuoritustietojenTarkistus
      },
      {
        id: 'ammatillinenosittainensuoritustietojentarkistus',
        name: 'Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)',
        compactName: 'Suoritustiedot (tutk. osia)',
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
        name: 'Lukioraportti-title',
        component: Lukioraportti
      },
      {
        id: 'lukiokurssikertyma',
        name: 'lukion-kurssikertyma-title',
        component: LukioKurssikertyma
      },
      {
        id: 'lukiodiaibinternationalopiskelijamaarat',
        name: 'lukiokoulutuksen-opiskelijamaarat-title',
        component: LukioDiaIBInternationalOpiskelijamaarat
      },
      {
        id: 'luvaopiskelijamaarat',
        name: 'luva-opiskelijamaarat-title',
        component: LuvaOpiskelijamaaratRaportti
      }
    ]
  }
]

export const raportitContentP = () => {
  const organisaatioAtom = Atom()

  const tabIdE = new Bacon.Bus()
  const raporttiIdE = new Bacon.Bus()

  const stateP = Bacon.update(
    { tabId: 0, raporttiId: 0 },
    tabIdE, (state, tabId) => ({
      ...state,
      tabId,
      raporttiId: 0
    }),
    raporttiIdE, (state, raporttiId) => ({
      ...state,
      raporttiId
    })
  )

  const tabP = stateP.map(state => raportitKategorioittain[state.tabId])
  const raporttiP = stateP.map(state => raportitKategorioittain[state.tabId].raportit[state.raporttiId])

  const mahdollisetRaportitP = organisaatioAtom
    .flatMapLatest(oppilaitos => oppilaitos ? Http.cachedGet(`/koski/api/raportit/mahdolliset-raportit/${oppilaitos.oid}`) : undefined)
    .toProperty()

  const raporttiComponentP = Bacon.combineWith(
    raporttiP,
    mahdollisetRaportitP,
    (raportti, mahdollisetRaportit) =>
      raportti && (mahdollisetRaportit || []).includes(raportti.id)
        ? raportti.component
        : null
  )

  return Bacon.constant({
    content: (<div className='content-area raportit'>
      <div className='main-content'>
        <Tabs
          options={raportitKategorioittain.map((r, index) => ({ id: index, name: r.tab }))}
          selectedP={stateP.map(state => state.tabId)}
          onSelect={id => tabIdE.push(id)}
        />
        {tabP.map(tab => <h2>{tab.heading}</h2>)}
        <RaporttiValitsin
          raportitP={tabP.map(tab => tab.raportit)}
          selectedP={stateP.map(state => state.raporttiId)}
          onSelect={id => raporttiIdE.push(id)}
        />
        <Organisaatio organisaatioAtom={organisaatioAtom} />
        {raporttiComponentP.map(Component => Component ? <Component organisaatioAtom={organisaatioAtom} /> : null)}
      </div>
    </div>),
    title: 'Raportit'
  })
}

const RaporttiValitsin = ({ raportitP, selectedP, onSelect }) => (
  <div>
    {raportitP.map(raportit => raportit.length < 2 ? null : (
      <ul className="pills-container">
        {raportit.map((raportti, index) => (
          <li
            key={raportti.id}
            onClick={() => onSelect(index)}
            className={selectedP.map(selectedIdx => index === selectedIdx ? 'pills-item pills-item-selected' : 'pills-item')}
          >
            <Text name={raportti.compactName || raportti.name} />
          </li>
        ))}
      </ul>
    ))}
  </div>
)

const Organisaatio = ({organisaatioAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE', 'KOULUTUSTOIMIJA', 'VARHAISKASVATUKSEN_TOIMIPAIKKA', 'OSTOPALVELUTAIPALVELUSETELI']
  return (<label className='raportit-organisaatio'><Text name='Organisaatio'/>
    {
      organisaatioAtom.map(organisaatio => (
        <OrganisaatioPicker
          preselectSingleOption={true}
          selectedOrg={{ oid: organisaatio && organisaatio.oid, nimi: organisaatio && organisaatio.nimi && t(organisaatio.nimi) }}
          onSelectionChanged={org => organisaatioAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
          shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
          canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
          clearText='tyhjennä'
          noSelectionText='Valitse...'
        />
      ))
    }
  </label>)
}

function PaallekkaisetOpiskeluoikeudet({organisaatioAtom}) {
  return (
    <AikajaksoRaportti
      organisaatioAtom={organisaatioAtom}
      apiEndpoint={'/paallekkaisetopiskeluoikeudet'}
      title={<Text name='paallekkaiset-opiskeluoikeudet'/>}
      description={<Text name='paallekkaiset-opiskeluoikeudet'/>}
    />
  )
}

function Opiskelijavuositiedot({ organisaatioAtom }) {
  const titleText = <Text name='Opiskelijavuositiedot' />
  const descriptionText = <Text name='Opiskelijavuositiedot-description' />

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
    title={titleText}
    description={descriptionText} />)
}

function SuoritustietojenTarkistus({ organisaatioAtom }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const descriptionText = <Text name='SuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

function AmmatillinenOsittainenSuoritustietojenTarkistus({ organisaatioAtom }) {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)' />
  const descriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-description' />

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText} />)
}

function MuuAmmatillinenRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Suoritustiedot (muu ammatillinen koulutus)' />
  const descriptionText = <Text name='muuammatillinenraportti-description' />

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/muuammatillinen'}
    title={titleText}
    description={descriptionText} />)
}

function TOPKSAmmatillinenRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Suoritustiedot (TOPKS ammatillinen koulutus)' />
  const descriptionText = <Text name='topksammatillinen-description' />

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/topksammatillinen'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenVuosiluokka({ organisaatioAtom }) {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti' />
  const descriptionText = <Text name='PerusopetuksenVuosiluokka-description' />
  const exampleText = <Text name='PerusopetuksenVuosiluokka-example' />

  return (<VuosiluokkaRaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenvuosiluokka'}
    title={titleText}
    description={descriptionText}
    example={exampleText} />)
}

function Lukioraportti({ organisaatioAtom }) {
  const titleText = <Text name='Lukioraportti-title' />
  const descriptionText = <Text name='Lukioraportti-description' />

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukionsuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
    osasuoritusType={osasuoritusTypes.KURSSI} />)
}

function LukioKurssikertyma({ organisaatioAtom }) {
  return (<AikajaksoRaportti organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukiokurssikertymat'}
    title={<Text name='lukion-kurssikertyma-title' />}
    description={<Text name='lukion-kurssikertyma-description' />} />)
}

function LukioDiaIBInternationalOpiskelijamaarat({ organisaatioAtom }) {
  const titleText = <Text name='lukiokoulutuksen-opiskelijamaarat-title' />
  const descriptionText = <Text name='lukiokoulutuksen-opiskelijamaarat-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukiodiaibinternationalopiskelijamaarat'}
    title={titleText}
    description={descriptionText} />)
}

function LuvaOpiskelijamaaratRaportti({ organisaatioAtom }) {
  const titleText = <Text name='luva-opiskelijamaarat-title' />
  const descriptionText = <Text name='luva-opiskelijamaarat-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/luvaopiskelijamaarat'}
    title={titleText}
    description={descriptionText} />)
}

function EsiopetusRaportti({ organisaatioAtom }) {
  const titleText = <Text name='esiopetusraportti-title' />
  const descriptionText = <Text name='esiopetusraportti-description' />
  const exampleText = <Text name='esiopetusraportti-example' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/esiopetus'}
    title={titleText}
    description={descriptionText}
    example={exampleText} />)
}

function EsiopetuksenOppijamäärätRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Esiopetus-oppilasmäärät-raportti-title' />
  const descriptionText = <Text name='Esiopetus-oppilasmäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/esiopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenOppijamäärätRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Perusopetus-oppijamäärät-raportti-title' />
  const descriptionText = <Text name='Perusopetus-oppijamäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function PerusopetuksenLisäopetuksenOppijamäärätRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-title' />
  const descriptionText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenlisaopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetusRaportti({ organisaatioAtom }) {
  const titleText = <Text name='aikuisten-perusopetus-raportti-title' />
  const descriptionText = <Text name='aikuisten-perusopetus-raportti-description' />

  return (<AikuistenPerusopetuksenRaportit
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuisten-perusopetus-suoritustietojen-tarkistus'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetuksenOppijamäärätRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-title' />
  const descriptionText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-description' />

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuistenperusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText} />)
}

function AikuistenPerusopetuksenKurssikertymäRaportti({ organisaatioAtom }) {
  const titleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-title' />
  const descriptionText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-description' />

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuistenperusopetuksenkurssikertymaraportti'}
    title={titleText}
    description={descriptionText} />)
}
