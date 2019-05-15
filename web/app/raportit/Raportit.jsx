import React from 'baret'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import OrganisaatioPicker from '../virkailija/OrganisaatioPicker'
import Atom from 'bacon.atom'
import Http from '../util/http'
import {AikajaksoRaportti} from './AikajaksoRaportti'
import {VuosiluokkaRaporttiPaivalta} from './VuosiluokkaRaporttiPaivalta'

export const raportitContentP = () => {
  const oppilaitosAtom = Atom()

  const mahdollisetRaportitP = oppilaitosAtom
    .flatMapLatest(oppilaitos => oppilaitos ? Http.cachedGet(`/koski/api/raportit/mahdolliset-raportit/${oppilaitos.oid}`) : undefined)
    .toProperty()

  return Bacon.constant({
    content: (<div className='content-area raportit'>
      <div className='main-content'>
        <h2><Text name='Raportit'/></h2>
        <Oppilaitos oppilaitosAtom={oppilaitosAtom} />
        {mahdollisetRaportitP.map(raportit => (
          <div>
            {raportit && raportit.length === 0 && <Text name='Tälle oppilaitokselle ei löydy raportteja. Toistaiseksi ainoa käytössä oleva raportti on tarkoitettu vain ammatillisille oppilaitoksille.'/>}
            {raportit && raportit.length > 0 && <hr/>}
            {raportit && raportit.includes('ammatillinenopiskelijavuositiedot') && <Opiskelijavuositiedot oppilaitosAtom={oppilaitosAtom} />}
            {raportit && raportit.includes('ammatillinentutkintosuoritustietojentarkistus') && <SuoritustietojenTarkistus oppilaitosAtom={oppilaitosAtom} />}
            {raportit && raportit.includes('ammatillinenosittainensuoritustietojentarkistus') && <AmmatillinenOsittainenSuoritustietojenTarkistus oppilaitosAtom={oppilaitosAtom} />}
            {raportit && raportit.includes('perusopetuksenvuosiluokka') && <PerusopetuksenVuosiluokka oppilaitosAtom={oppilaitosAtom} />}
            {raportit && raportit.includes('lukionsuoritustietojentarkistus') && <Lukioraportti oppilaitosAtom={oppilaitosAtom} />}
          </div>
        ))}
      </div>
    </div>),
    title: 'Raportit'
  })
}

const Oppilaitos = ({oppilaitosAtom}) => {
  const selectableOrgTypes = ['OPPILAITOS', 'OPPISOPIMUSTOIMIPISTE', 'KOULUTUSTOIMIJA']
  return (<label className='oppilaitos'><Text name='Oppilaitos'/>
    {
      oppilaitosAtom.map(oppilaitos => (
        <OrganisaatioPicker
          preselectSingleOption={true}
          selectedOrg={{ oid: oppilaitos && oppilaitos.oid, nimi: oppilaitos && oppilaitos.nimi && t(oppilaitos.nimi) }}
          onSelectionChanged={org => oppilaitosAtom.set({oid: org && org.oid, nimi: org && org.nimi})}
          shouldShowOrg={org => !org.organisaatiotyypit.some(tyyppi => tyyppi === 'TOIMIPISTE')}
          canSelectOrg={(org) => org.organisaatiotyypit.some(ot => selectableOrgTypes.includes(ot))}
          clearText='tyhjennä'
          noSelectionText='Valitse...'
        />
      ))
    }
  </label>)
}

const Opiskelijavuositiedot = ({oppilaitosAtom}) => {
  const titleText = <Text name='Opiskelijavuositiedot'/>
  const descriptionText = <Text name='Opiskelijavuositiedot-description'/>

  return (<AikajaksoRaportti
    oppilaitosAtom={oppilaitosAtom}
    apiEndpoint={'/ammatillinenopiskelijavuositiedot'}
    title={titleText}
    description={descriptionText}
  />)
}

const SuoritustietojenTarkistus = ({oppilaitosAtom}) => {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, koko tutkinto)'/>
  const descriptionText = <Text name='SuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaportti
    oppilaitosAtom={oppilaitosAtom}
    apiEndpoint={'/ammatillinentutkintosuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const AmmatillinenOsittainenSuoritustietojenTarkistus = ({oppilaitosAtom}) => {
  const titleText = <Text name='Suoritustiedot (ammatillinen koulutus, tutkinnon osa/osia)'/>
  const descriptionText = <Text name='AmmatillinenOsittainenSuoritustietojenTarkistus-description'/>

  return (<AikajaksoRaportti
    oppilaitosAtom={oppilaitosAtom}
    apiEndpoint={'/ammatillinenosittainensuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}

const PerusopetuksenVuosiluokka = ({oppilaitosAtom}) => {
  const titleText = <Text name='Nuorten perusopetuksen opiskeluoikeus- ja suoritustietojen tarkistusraportti'/>
  const descriptionText = <Text name='PerusopetuksenVuosiluokka-description'/>
  const exampleText = <Text name='PerusopetuksenVuosiluokka-example'/>

  return (<VuosiluokkaRaporttiPaivalta
    oppilaitosAtom={oppilaitosAtom}
    apiEndpoint={'/perusopetuksenvuosiluokka'}
    title={titleText}
    description={descriptionText}
    example={exampleText}
  />)
}

const Lukioraportti = ({oppilaitosAtom}) => {
  const titleText = <Text name='Lukioraportti-title'/>
  const descriptionText = <Text name='Lukioraportti-description'/>

  return (<AikajaksoRaportti
    oppilaitosAtom={oppilaitosAtom}
    apiEndpoint={'/lukionsuoritustietojentarkistus'}
    title={titleText}
    description={descriptionText}
  />)
}
