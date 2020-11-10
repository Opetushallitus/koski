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

// const defaultTabs = {
//   esi: 'Esiopetus',
//   perus: 'Perusopetus',
//   aikuistenPerus: 'Aikuisten perusopetus',
//   ammatillinen: 'Ammatillinen koulutus',
//   lukio: 'Lukiokoulutus',
//   muut: 'Muut',
// }

const raportitKategorioittain = [
  {
    tab: 'Esiopetus',
    heading: 'Esiopetuksen raportit',
    raportit: [
      {
        id: 'esiopetuksenraportti',
        name: 'Esiopetuksen raportti',
        compactName: 'Esiopetuksen raportti'
      },
      {
        id: 'esiopetuksenoppijamäärienraportti',
        name: 'Esiopetuksen oppijamäärien raportti',
        compactName: 'Oppijamäärät'
      }
    ]
  },
  {
    tab: 'Perusopetus',
    heading: 'Perusopetuksen raportit',
    raportit: [
      {
        id: 'perusopetuksenvuosiluokka',
        name: 'Perusopetuksen vuosiluokan raportti',
        compactName: 'Vuosiluokka'
      }
    ]
  }
]

export const raportitContentP = () => {
  const organisaatioAtom = Atom() // TODO: Siirrä tämäkin samaan stateen

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
        {raporttiP.map(raportti => <h3>{raportti.name}</h3>)}
        <Organisaatio organisaatioAtom={organisaatioAtom} />
        {mahdollisetRaportitP.map(raportit => (
          <div>
            {raportit && raportit.length === 0 && <Text name='Tälle organisaatiolle ei löydy raportteja'/>}
            {raportit && raportit.length > 0 && <hr/>}
            {document.location.search.includes('tilastoraportit=true') && raportit && <PaallekkaisetOpiskeluoikeudet organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('ammatillinenopiskelijavuositiedot') && <Opiskelijavuositiedot organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('ammatillinentutkintosuoritustietojentarkistus') && <SuoritustietojenTarkistus organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('ammatillinenosittainensuoritustietojentarkistus') && <AmmatillinenOsittainenSuoritustietojenTarkistus organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('muuammatillinenkoulutus') && <MuuAmmatillinenRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('topksammatillinen') && <TOPKSAmmatillinenRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('perusopetuksenvuosiluokka') && <PerusopetuksenVuosiluokka organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('perusopetuksenoppijamääräraportti') && <PerusopetuksenOppijamäärätRaportti organisaatioAtom={organisaatioAtom}/>}
            {raportit && raportit.includes('perusopetuksenlisäopetuksenoppijamääräraportti') && <PerusopetuksenLisäopetuksenOppijamäärätRaportti organisaatioAtom={organisaatioAtom}/>}
            {raportit && raportit.includes('lukionsuoritustietojentarkistus') && <Lukioraportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('lukiokurssikertyma') && <LukioKurssikertyma organisaatioAtom={organisaatioAtom}/>}
            {raportit && raportit.includes('lukiodiaibinternationalopiskelijamaarat') && <LukioDiaIBInternationalOpiskelijamaarat organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('luvaopiskelijamaarat') && <LuvaOpiskelijamaaratRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('aikuistenperusopetussuoritustietojentarkistus') && <AikuistenPerusopetusRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('esiopetuksenraportti') && <EsiopetusRaportti organisaatioAtom={organisaatioAtom}/>}
            {raportit && raportit.includes('esiopetuksenoppijamäärienraportti') && <EsiopetuksenOppijamäärätRaportti organisaatioAtom={organisaatioAtom}/>}
            {raportit && raportit.includes('aikuistenperusopetusoppijamäärienraportti') && <AikuistenPerusopetuksenOppijamäärätRaportti organisaatioAtom={organisaatioAtom}/>}
            {document.location.search.includes('tilastoraportit=true') && raportit && raportit.includes('aikuistenperusopetuskurssikertymänraportti') && <AikuistenPerusopetuksenKurssikertymäRaportti organisaatioAtom={organisaatioAtom}/>}
          </div>
        ))}
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
            {raportti.compactName || raportti.name}
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

const PaallekkaisetOpiskeluoikeudet = ({organisaatioAtom}) =>
  <AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/paallekkaisetopiskeluoikeudet'}
    title={<Text name='paallekkaiset-opiskeluoikeudet'/>}
    description={<Text name='paallekkaiset-opiskeluoikeudet'/>}
  />


const Opiskelijavuositiedot = ({organisaatioAtom}) => {
  const titleText = <Text name='Opiskelijavuositiedot'/>
  const descriptionText = <Text name='Opiskelijavuositiedot-description'/>

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
    title={titleText}
    description={descriptionText}
  />)
}

const SuoritustietojenTarkistus = ({organisaatioAtom}) => {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const descriptionText = <Text name='SuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const AmmatillinenOsittainenSuoritustietojenTarkistus = ({organisaatioAtom}) => {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)'/>
  const descriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const MuuAmmatillinenRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Suoritustiedot (muu ammatillinen koulutus)'/>
  const descriptionText = <Text name='muuammatillinenraportti-description'/>

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/muuammatillinen'}
    title={titleText}
    description={descriptionText}
  />)
}

const TOPKSAmmatillinenRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Suoritustiedot (TOPKS ammatillinen koulutus)'/>
  const descriptionText = <Text name='topksammatillinen-description'/>

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/topksammatillinen'}
    title={titleText}
    description={descriptionText}
  />)
}

const PerusopetuksenVuosiluokka = ({organisaatioAtom}) => {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti'/>
  const descriptionText = <Text name='PerusopetuksenVuosiluokka-description'/>
  const exampleText = <Text name='PerusopetuksenVuosiluokka-example'/>

  return (<VuosiluokkaRaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenvuosiluokka'}
    title={titleText}
    description={descriptionText}
    example={exampleText}
  />)
}

const Lukioraportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Lukioraportti-title'/>
  const descriptionText = <Text name='Lukioraportti-description'/>

  return (<AikajaksoRaporttiAikarajauksella
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukionsuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
    osasuoritusType={osasuoritusTypes.KURSSI}
  />)
}

const LukioKurssikertyma = ({organisaatioAtom}) => {
  return (<AikajaksoRaportti organisaatioAtom={organisaatioAtom}
                            apiEndpoint={'/lukiokurssikertymat'}
                            title={<Text name='lukion-kurssikertyma-title'/>}
                            description={<Text name='lukion-kurssikertyma-description'/>}/>)
}

const LukioDiaIBInternationalOpiskelijamaarat = ({organisaatioAtom}) => {
  const titleText = <Text name='lukiokoulutuksen-opiskelijamaarat-title'/>
  const descriptionText = <Text name='lukiokoulutuksen-opiskelijamaarat-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukiodiaibinternationalopiskelijamaarat'}
    title={titleText}
    description={descriptionText}
  />)
}

const LuvaOpiskelijamaaratRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='luva-opiskelijamaarat-title'/>
  const descriptionText = <Text name='luva-opiskelijamaarat-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/luvaopiskelijamaarat'}
    title={titleText}
    description={descriptionText}
  />)
}

const EsiopetusRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='esiopetusraportti-title'/>
  const descriptionText = <Text name='esiopetusraportti-description'/>
  const exampleText = <Text name='esiopetusraportti-example'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/esiopetus'}
    title={titleText}
    description={descriptionText}
    example={exampleText}
  />)
}

const EsiopetuksenOppijamäärätRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Esiopetus-oppilasmäärät-raportti-title'/>
  const descriptionText = <Text name='Esiopetus-oppilasmäärät-raportti-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/esiopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText}
  />)
}

const PerusopetuksenOppijamäärätRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Perusopetus-oppijamäärät-raportti-title'/>
  const descriptionText = <Text name='Perusopetus-oppijamäärät-raportti-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText}
  />)
}

const PerusopetuksenLisäopetuksenOppijamäärätRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-title'/>
  const descriptionText = <Text name='Perusopetus-lisäopetus-oppijamäärät-raportti-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/perusopetuksenlisaopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText}
  />)
}

const AikuistenPerusopetusRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='aikuisten-perusopetus-raportti-title'/>
  const descriptionText = <Text name='aikuisten-perusopetus-raportti-description'/>

  return (<AikuistenPerusopetuksenRaportit
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuisten-perusopetus-suoritustietojen-tarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const AikuistenPerusopetuksenOppijamäärätRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-title'/>
  const descriptionText = <Text name='Aikuisten-perusopetus-oppilasmäärät-raportti-description'/>

  return (<RaporttiPaivalta
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuistenperusopetuksenoppijamaaratraportti'}
    title={titleText}
    description={descriptionText}
  />)
}

const AikuistenPerusopetuksenKurssikertymäRaportti = ({organisaatioAtom}) => {
  const titleText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-title'/>
  const descriptionText = <Text name='Aikuisten-perusopetus-kurssikertymä-raportti-description'/>

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/aikuistenperusopetuksenkurssikertymaraportti'}
    title={titleText}
    description={descriptionText}
  />)
}
