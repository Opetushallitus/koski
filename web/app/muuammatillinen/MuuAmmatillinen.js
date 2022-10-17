const isMuuAmmatillisenSuoritus = (suoritus) =>
  suoritus.value.classes.includes('muunammatillisenkoulutuksensuoritus')
const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = (suoritus) =>
  suoritus.value.classes.includes(
    'tutkinnonosaapienemmistakokonaisuuksistakoostuvasuoritus'
  )

export const isMuutaAmmatillistaPäätasonSuoritus = (s) =>
  isMuuAmmatillisenSuoritus(s) ||
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(s)
