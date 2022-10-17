export const sortOpiskeluoikeudetJaSuoritukset = (opiskeluoikeudet) => {
  return opiskeluoikeudet.sort(opiskeluoikeusOrdering).map((opiskeluoikeus) => {
    const suorituksetSorted = opiskeluoikeus.suoritukset.sort(suoritusOrdering)
    return { ...opiskeluoikeus, suoritukset: suorituksetSorted }
  })
}

const opiskeluoikeusOrdering = (o1, o2) => {
  const alkamispaiva1 = alkamispaivaAsDate(o1)
  const paattymispaiva1 = paattymispaivaAsDate(o1)

  const alkamispaiva2 = alkamispaivaAsDate(o2)
  const paattymispaiva2 = paattymispaivaAsDate(o2)

  if (alkamispaiva1 === alkamispaiva2) {
    return paattymispaiva2 - paattymispaiva1
  } else {
    return alkamispaiva2 - alkamispaiva1
  }
}

const suoritusOrdering = (s1, s2) => {
  const vavhistus1 = vahvistuspaivaAsDate(s1)
  const vahvistus2 = vahvistuspaivaAsDate(s2)

  return vahvistus2 - vavhistus1
}

const alkamispaivaAsDate = (opiskeluoikeus) => {
  const alkamispaiva = opiskeluoikeus.alkamispäivä || infinity
  return new Date(alkamispaiva)
}

const paattymispaivaAsDate = (opiskeluoikeus) => {
  const päättymispäivä = opiskeluoikeus.päättymispäivä || infinity
  return new Date(päättymispäivä)
}

const vahvistuspaivaAsDate = (suoritus) => {
  const paiva = (suoritus.vahvistus && suoritus.vahvistus.päivä) || infinity
  return new Date(paiva)
}

const infinity = '9999-01-01'
