import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'

import { koodiarvoMatch, koodistoValues } from './koodisto'
import Suoritustyyppi from './Suoritustyyppi'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import { ift } from '../util/util'
import { setPeruste } from '../suoritus/PerusteDropdown'
import Text from '../i18n/Text'
import Input from '../components/Input'

export default ({
  suoritusAtom,
  oppilaitosAtom,
  oppimääräAtom,
  taiteenalaAtom
}) => {
  const yleisenSuoritus = (tyyppi) =>
    koodiarvoMatch(
      'taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot',
      'taiteenperusopetuksenyleisenoppimaaranteemaopinnot'
    )(tyyppi)
  const laajanSuoritus = (tyyppi) =>
    koodiarvoMatch(
      'taiteenperusopetuksenlaajanoppimaaranperusopinnot',
      'taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
    )(tyyppi)

  const suoritustyypitP = koodistoValues(
    'suorituksentyyppi/taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot,taiteenperusopetuksenyleisenoppimaaranteemaopinnot,taiteenperusopetuksenlaajanoppimaaranperusopinnot,taiteenperusopetuksenlaajanoppimaaransyventavatopinnot'
  )
  const suoritustyypitYleinenP = suoritustyypitP.map((values) =>
    values.filter(yleisenSuoritus).reverse()
  )
  const suoritustyypitLaajaP = suoritustyypitP.map((values) =>
    values.filter(laajanSuoritus)
  )

  const suoritustyyppiAtom = Atom()
  const perusteAtom = Atom()
  const taiteenAlatP = koodistoValues('taiteenperusopetustaiteenala')

  oppimääräAtom.changes().onValue((oppimäärä) => {
    suoritustyyppiAtom.set(undefined)
    perusteAtom.set(undefined)

    if (koodiarvoMatch('yleinenoppimaara')(oppimäärä)) {
      suoritustyypitYleinenP.map((tyypit) =>
        suoritustyyppiAtom.set(tyypit.find(yleisenSuoritus))
      )
    } else if (koodiarvoMatch('laajaoppimaara')(oppimäärä)) {
      suoritustyypitLaajaP.map((tyypit) =>
        suoritustyyppiAtom.set(tyypit.find(laajanSuoritus))
      )
    }
  })

  suoritustyyppiAtom.changes().onValue((tyyppi) => {
    if (yleisenSuoritus(tyyppi)) {
      setPeruste(perusteAtom, tyyppi, 'OPH-2069-2017')
    } else if (laajanSuoritus(tyyppi)) {
      setPeruste(perusteAtom, tyyppi, 'OPH-2068-2017')
    } else {
      perusteAtom.set(undefined)
    }
  })

  const makeSuoritus = (oppilaitos, peruste, taiteenala, suoritusTyyppi) => {
    if (oppilaitos) {
      return {
        koulutusmoduuli: {
          tunniste: {
            koodiarvo: '999907',
            koodistoUri: 'koulutus'
          },
          taiteenala: taiteenala,
          perusteenDiaarinumero: peruste
        },
        toimipiste: oppilaitos,
        tyyppi: {
          koodistoUri: 'suorituksentyyppi',
          koodiarvo: suoritusTyyppi ? suoritusTyyppi.koodiarvo : undefined
        }
      }
    }
  }
  const suoritusP = Bacon.combineWith(
    oppilaitosAtom,
    perusteAtom,
    taiteenalaAtom,
    suoritustyyppiAtom,
    makeSuoritus
  )
  suoritusP
    .filter('.koulutusmoduuli.perusteenDiaarinumero')
    .onValue((suoritus) => suoritusAtom.set(suoritus))
  return (
    <span>
      {ift(
        oppimääräAtom.map((o) => o && o.koodiarvo === 'yleinenoppimaara'),
        <Suoritustyyppi
          suoritustyyppiAtom={suoritustyyppiAtom}
          suoritustyypitP={suoritustyypitYleinenP}
          title="Suoritustyyppi"
        />
      )}
      {ift(
        oppimääräAtom.map((o) => o && o.koodiarvo === 'laajaoppimaara'),
        <Suoritustyyppi
          suoritustyyppiAtom={suoritustyyppiAtom}
          suoritustyypitP={suoritustyypitLaajaP}
          title="Suoritustyyppi"
        />
      )}
      <label className="peruste">
        <Text name="Peruste" />
        <Input id="tpo-peruste-input" disabled={true} value={perusteAtom} />
      </label>
      <label id="tpo-taiteenala">
        <KoodistoDropdown
          className="tpo-taiteenala"
          title="Taiteenala"
          options={taiteenAlatP}
          selected={taiteenalaAtom}
        />
      </label>
    </span>
  )
}
