import React from 'baret'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import Atom from 'bacon.atom'
import Http from '../util/http'
import {AikajaksoRaportti} from './AikajaksoRaportti'
import {VuosiluokkaRaporttiPaivalta} from './VuosiluokkaRaporttiPaivalta'
import {AmmatillinenSuoritusTiedotRaportti} from './AmmatillinenSuoritusTiedotRaportti'
import {RaporttiPaivalta} from './RaporttiPaivalta'

export const raportitContentP = () => {
  const organisaatioAtom = Atom()

  const mahdollisetRaportitP = organisaatioAtom
    .flatMapLatest(oppilaitos => oppilaitos ? Http.cachedGet(`/koski/api/raportit/mahdolliset-raportit/${oppilaitos.oid}`) : undefined)
    .toProperty()

  return Bacon.constant({
    content: (<div className='content-area raportit'>
      <div className='main-content'>
        <h2><Text name='Raportit'/></h2>
        <Organisaatio organisaatioAtom={organisaatioAtom} />
        {mahdollisetRaportitP.map(raportit => (
          <div>
            {raportit && raportit.length === 0 && <Text name='Tälle organisaatiolle ei löydy raportteja'/>}
            {raportit && raportit.length > 0 && <hr/>}
            {raportit && raportit.includes('ammatillinenopiskelijavuositiedot') && <Opiskelijavuositiedot organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('ammatillinentutkintosuoritustietojentarkistus') && <SuoritustietojenTarkistus organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('ammatillinenosittainensuoritustietojentarkistus') && <AmmatillinenOsittainenSuoritustietojenTarkistus organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('muuammatillinenkoulutus') && <MuuAmmatillinenRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('topksammatillinen') && <TOPKSAmmatillinenRaportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('perusopetuksenvuosiluokka') && <PerusopetuksenVuosiluokka organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('lukionsuoritustietojentarkistus') && <Lukioraportti organisaatioAtom={organisaatioAtom} />}
            {raportit && raportit.includes('esiopetuksenraportti') && <EsiopetusRaportti organisaatioAtom={organisaatioAtom}/>}
          </div>
        ))}
      </div>
    </div>),
    title: 'Raportit'
  })
}

const Organisaatio = ({organisaatioAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE', 'KOULUTUSTOIMIJA']
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

  return (<AmmatillinenSuoritusTiedotRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const AmmatillinenOsittainenSuoritustietojenTarkistus = ({organisaatioAtom}) => {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)'/>
  const descriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-description'/>

  return (<AmmatillinenSuoritusTiedotRaportti
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

  return (<AikajaksoRaportti
    organisaatioAtom={organisaatioAtom}
    apiEndpoint={'/lukionsuoritustietojentarkistus'}
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
