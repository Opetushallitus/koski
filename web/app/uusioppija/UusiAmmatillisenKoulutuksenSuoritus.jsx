import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import TutkintoAutocomplete from '../virkailija/TutkintoAutocomplete'
import { ift } from '../util/util'
import Suoritustyyppi from './Suoritustyyppi'
import { koodiarvoMatch, koodistoValues } from './koodisto'
import SuoritustapaDropdown from './SuoritustapaDropdown'
import Text from '../i18n/Text'
import { setPeruste } from '../suoritus/PerusteDropdown'
import Peruste from './Peruste'
import PaikallinenKoulutusmoduuli from './PaikallinenKoulutusmoduuli'
import MuunAmmatillisenKoulutus from './MuunAmmatillisenKoulutus'

export default ({ suoritusAtom, oppilaitosAtom, suorituskieliAtom }) => {
  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/ammatillinentutkinto,nayttotutkintoonvalmistavakoulutus,ammatillinentutkintoosittainen,valma,telma,tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus,muuammatillinenkoulutus'
  )
  const tutkintoAtom = Atom()
  const suoritustyyppiAtom = Atom()
  const suoritustapaAtom = Atom()
  const perusteAtom = Atom()

  const koulutusmoduuliAtom = Atom()

  const koulutusmoduuliNimiAtom = Atom()
  const koulutusmoduuliKoodiAtom = Atom()
  const koulutusmoduuliKuvausAtom = Atom()
  const ammatilliseentehtavaanvalmistavakoulutusAtom = Atom()

  Bacon.combineWith(
    koulutusmoduuliNimiAtom,
    koulutusmoduuliKoodiAtom,
    koulutusmoduuliKuvausAtom,
    ammatilliseentehtavaanvalmistavakoulutusAtom,
    makeMuuAmmatillinenKoulutusmoduuli
  ).onValue((moduuli) => koulutusmoduuliAtom.set(moduuli))

  suoritustyypitP.onValue((tyypit) =>
    suoritustyyppiAtom.set(
      tyypit.find(
        koodiarvoMatch(
          'ammatillinentutkinto',
          'ammatillinentutkintoosittainen',
          'valma',
          'telma'
        )
      )
    )
  )
  oppilaitosAtom.changes().onValue(() => tutkintoAtom.set(undefined))
  suoritustyyppiAtom
    .changes()
    .onValue((tyyppi) =>
      koodiarvoMatch('valma', 'telma')(tyyppi)
        ? setPeruste(perusteAtom, tyyppi)
        : perusteAtom.set(null)
    )

  const makeSuoritus = (
    oppilaitos,
    suoritustyyppi,
    tutkinto,
    suorituskieli,
    suoritustapa,
    peruste,
    koulutusmoduuli
  ) => {
    const tutkintoData = tutkinto && {
      tunniste: {
        koodiarvo: tutkinto.tutkintoKoodi,
        koodistoUri: 'koulutus'
      },
      perusteenDiaarinumero: tutkinto.diaarinumero
    }
    if (
      koodiarvoMatch('ammatillinentutkinto')(suoritustyyppi) &&
      tutkinto &&
      oppilaitos &&
      suoritustapa
    ) {
      return {
        koulutusmoduuli: tutkintoData,
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: 'ammatillinentutkinto'
        },
        suoritustapa,
        suorituskieli
      }
    }
    if (
      koodiarvoMatch(
        'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus',
        'muuammatillinenkoulutus'
      )(suoritustyyppi) &&
      oppilaitos &&
      koulutusmoduuli
    ) {
      return {
        koulutusmoduuli,
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: suoritustyyppi.koodiarvo
        },
        suorituskieli
      }
    }
    if (
      koodiarvoMatch('ammatillinentutkintoosittainen')(suoritustyyppi) &&
      tutkinto &&
      oppilaitos &&
      suoritustapa
    ) {
      return {
        koulutusmoduuli: tutkintoData,
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: 'ammatillinentutkintoosittainen'
        },
        suoritustapa,
        suorituskieli
      }
    }
    if (
      koodiarvoMatch('nayttotutkintoonvalmistavakoulutus')(suoritustyyppi) &&
      tutkinto &&
      oppilaitos
    ) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999904',
            koodistoUri: 'koulutus'
          }
        },
        tutkinto: tutkintoData,
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: 'nayttotutkintoonvalmistavakoulutus'
        },
        suorituskieli
      }
    }
    if (koodiarvoMatch('valma')(suoritustyyppi) && oppilaitos && peruste) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999901',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'valma' },
        suorituskieli
      }
    }
    if (koodiarvoMatch('telma')(suoritustyyppi) && oppilaitos && peruste) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999903',
            koodistoUri: 'koulutus'
          },
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: { koodistoUri: 'suorituksentyyppi', koodiarvo: 'telma' },
        suorituskieli
      }
    }
  }

  Bacon.combineWith(
    oppilaitosAtom,
    suoritustyyppiAtom,
    tutkintoAtom,
    suorituskieliAtom,
    suoritustapaAtom,
    perusteAtom,
    koulutusmoduuliAtom,
    makeSuoritus
  ).onValue((suoritus) => suoritusAtom.set(suoritus))

  return (
    <div>
      <Suoritustyyppi
        suoritustyyppiAtom={suoritustyyppiAtom}
        suoritustyypitP={suoritustyypitP}
        title="Suoritustyyppi"
      />
      {ift(
        suoritustyyppiAtom.map(koodiarvoMatch('valma')),
        <Peruste {...{ suoritusTyyppiP: suoritustyyppiAtom, perusteAtom }} />
      )}
      {ift(
        suoritustyyppiAtom.map(
          koodiarvoMatch(
            'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
          )
        ),
        <PaikallinenKoulutusmoduuli
          nimi={koulutusmoduuliNimiAtom}
          koodiarvo={koulutusmoduuliKoodiAtom}
          kuvaus={koulutusmoduuliKuvausAtom}
        />
      )}
      {ift(
        suoritustyyppiAtom.map(koodiarvoMatch('muuammatillinenkoulutus')),
        <MuunAmmatillisenKoulutus
          {...{
            koulutusmoduuliNimiAtom,
            koulutusmoduuliKoodiAtom,
            koulutusmoduuliKuvausAtom,
            ammatilliseentehtavaanvalmistavakoulutusAtom
          }}
        />
      )}
      <div className="tutkinto-autocomplete">
        {ift(
          oppilaitosAtom.and(
            suoritustyyppiAtom
              .map(
                koodiarvoMatch(
                  'valma',
                  'telma',
                  'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus',
                  'muuammatillinenkoulutus'
                )
              )
              .not()
          ),
          <TutkintoAutocomplete
            tutkintoAtom={tutkintoAtom}
            oppilaitosP={oppilaitosAtom}
            title={<Text name="Tutkinto" />}
          />
        )}
      </div>

      {ift(
        suoritustyyppiAtom.map(
          koodiarvoMatch(
            'ammatillinentutkinto',
            'ammatillinentutkintoosittainen'
          )
        ),
        <SuoritustapaDropdown
          diaarinumero={tutkintoAtom.map('.diaarinumero')}
          suoritustapaAtom={suoritustapaAtom}
          title="Suoritustapa"
        />
      )}
    </div>
  )
}

const makeMuuAmmatillinenKoulutusmoduuli = (
  nimi,
  koodiarvo,
  kuvaus,
  ammatilliseentehtavaanvalmistavakoulutus
) => {
  if (ammatilliseentehtavaanvalmistavakoulutus) {
    return {
      tunniste: ammatilliseentehtavaanvalmistavakoulutus
    }
  } else if (nimi && kuvaus) {
    return {
      tunniste: {
        nimi: { fi: nimi },
        koodiarvo: koodiarvo || nimi
      },
      kuvaus: { fi: kuvaus }
    }
  }
}
