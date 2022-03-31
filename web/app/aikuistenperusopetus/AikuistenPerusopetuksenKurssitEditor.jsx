import React, {fromBacon} from 'baret'
import Atom from 'bacon.atom'
import {
  modelData,
  modelItems,
  modelLookup, modelSetValue
} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {ift} from '../util/util'
import UusiKurssiPopup from '../kurssi/UusiKurssiPopup'
import {KurssiEditor} from '../kurssi/KurssiEditor'
import {lisääKurssi, osasuoritusCountOk} from '../kurssi/kurssi'
import {koodistoValues} from '../uusioppija/koodisto'
import * as R from 'ramda'
import * as Bacon from 'baconjs'
import {parseLocation} from '../util/location'
import Http from '../util/http'
import {arvioituTaiVahvistettu, newOsasuoritusProto} from '../suoritus/Suoritus'
import {koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'

export const AikuistenPerusopetuksenKurssitEditor = ({model}) => {
  const osasuoritukset = modelLookup(model, 'osasuoritukset')

  const showUusiKurssiAtom = Atom(false)
  const kurssinSuoritusProto = newOsasuoritusProto(model, 'aikuistenperusopetuksenkurssinsuoritus')

  const showUusiAlkuvaiheenKurssiAtom = Atom(false)
  const alkuvaiheenKurssinSuoritusProto = newOsasuoritusProto(model, 'aikuistenperusopetuksenalkuvaiheenkurssinsuoritus')

  return (
    <div className="kurssit">
      <KurssitOtsikko model={model}/>
      <Kurssit model={model}/>
        {
          model.context.edit && osasuoritusCountOk(osasuoritukset) && (
            <React.Fragment>
              <UusiKurssi
                name={'Lisää kurssi'} model={model} classname={'uusi-kurssi'}
                showUusiKurssiAtom={showUusiKurssiAtom}
                kurssinSuoritusProto={kurssinSuoritusProto}
                customAlternativesCompletionFn={(oppiaine, kurssiProtos) => customAlternativesFn(oppiaine, kurssiProtos, 'koskioppiaineetyleissivistava')}
              />
              {
                fromBacon(oppiaineMyösAlkuvaiheessaP(model).map(myösAlkuvaiheessa =>
                  myösAlkuvaiheessa &&
                  <UusiKurssi
                    name={'Lisää alkuvaiheen kurssi'} model={model} classname={'uusi-alkuvaiheen-kurssi'}
                    showUusiKurssiAtom={showUusiAlkuvaiheenKurssiAtom}
                    kurssinSuoritusProto={alkuvaiheenKurssinSuoritusProto}
                    customAlternativesCompletionFn={(oppiaine, kurssiProtos) => customAlternativesFn(oppiaine, kurssiProtos, 'aikuistenperusopetuksenalkuvaiheenoppiaineet')}
                  />
                ))
              }
            </React.Fragment>
          )
        }
    </div>
  )
}

AikuistenPerusopetuksenKurssitEditor.displayName = 'AikuistenPerusopetuksenKurssitEditor'

const KurssitOtsikko = ({model}) => {
  let suorituksiaTehty = modelItems(model, 'osasuoritukset').filter(arvioituTaiVahvistettu).length > 0
  return (model.context.edit || suorituksiaTehty) && <h5><Text name="Kurssit"/></h5>
}

KurssitOtsikko.displayName = 'KurssitOtsikko'

const Kurssit = ({model}) => (
  <ul>
    {modelItems(model, 'osasuoritukset').map((kurssi, index) => <KurssiEditor key={index} kurssi={kurssi}/>)}
  </ul>
)

Kurssit.displayName = 'Kurssit'

const UusiKurssi = ({name, model, showUusiKurssiAtom, kurssinSuoritusProto, customAlternativesCompletionFn, classname}) => (
  <span className={classname}>
    <a onClick={() => showUusiKurssiAtom.set(true)}><Text name={name}/></a>
    {
      ift(showUusiKurssiAtom,
        <UusiKurssiPopup
          oppiaineenSuoritus={model}
          resultCallback={(kurssi) => lisääKurssi(kurssi, model, showUusiKurssiAtom, kurssinSuoritusProto)}
          toimipiste={modelData(model.context.toimipiste).oid}
          kurssiPrototypes={koulutusModuuliprototypes(kurssinSuoritusProto)}
          customAlternativesCompletionFn={customAlternativesCompletionFn}
        />)
    }
  </span>
)

UusiKurssi.displayName = 'UusiKurssi'

const oppiaineMyösAlkuvaiheessaP = (model) => {
  const oppiaineenKoodiarvo = modelData(model, 'koulutusmoduuli.tunniste.koodiarvo')
  return koodistoValues('aikuistenperusopetuksenalkuvaiheenoppiaineet').map(mahdollisetAlkuvaiheenOppiaineet =>
    mahdollisetAlkuvaiheenOppiaineet.find(o => o.koodiarvo === oppiaineenKoodiarvo)
  )
}

const customAlternativesFn = (oppiaine, kurssiPrototypes, oppiaineKoodisto) => {
  const oppiaineKoodiarvo = modelData(oppiaine, 'tunniste.koodiarvo')
  const oppimaaraKoodisto = modelData(oppiaine, 'kieli.koodistoUri') || modelData(oppiaine, 'oppimäärä.koodistoUri')
  const oppimaaraKoodiarvo = modelData(oppiaine, 'kieli.koodiarvo') || modelData(oppiaine, 'oppimäärä.koodiarvo')
  const oppimaaraDiaarinumero = modelData(oppiaine.context.suoritus, 'koulutusmoduuli.perusteenDiaarinumero')

  const fetchAlternatives = (model) => {
    const koodistoAlternativesPath = modelLookup(model, 'tunniste').alternativesPath
    const kurssiKoodistot = koodistoAlternativesPath && R.last(koodistoAlternativesPath.split('/'))

    const loc = parseLocation(`/koski/api/editor/koodit/${oppiaineKoodisto}/${oppiaineKoodiarvo}/kurssit/${kurssiKoodistot}`)
      .addQueryParams({oppimaaraKoodisto, oppimaaraKoodiarvo, oppimaaraDiaarinumero})

    return Http.cachedGet(loc.toString())
      .map(alternatives => alternatives.map(enumValue => modelSetValue(model, enumValue, 'tunniste')))
  }

  return Bacon.combineAsArray(kurssiPrototypes.map(fetchAlternatives)).last().map(R.unnest)
}
