import { parseISODate } from '../date/date'

type SuoritusWithOptionalVahvistus = {
  vahvistus?: {
    päivä: string
  }
}

type OsasuoritusWithKoulutusmoduuli = {
  koulutusmoduuli: object
}

const LAAJUUS_LEIKKURIPÄIVÄ = '2020-08-01'

export const shouldShowLaajuusColumn = ({
  editMode,
  isToimintaAlueittain,
  pakollinen,
  suoritus,
  osasuoritukset
}: {
  editMode: boolean
  isToimintaAlueittain: boolean
  pakollinen?: boolean
  suoritus?: SuoritusWithOptionalVahvistus
  osasuoritukset: OsasuoritusWithKoulutusmoduuli[]
}): boolean => {
  if (editMode) {
    return true
  }

  const sisältääLaajuudellisiaSuorituksia = osasuoritukset.some(hasLaajuus)
  if (!sisältääLaajuudellisiaSuorituksia) {
    return false
  }

  return pakollinen || isToimintaAlueittain
    ? vahvistusSalliiLaajuudenNäyttämisen(suoritus)
    : true
}

const hasLaajuus = (suoritus: OsasuoritusWithKoulutusmoduuli): boolean =>
  'laajuus' in suoritus.koulutusmoduuli &&
  !!(suoritus.koulutusmoduuli as { laajuus?: unknown }).laajuus

const vahvistusSalliiLaajuudenNäyttämisen = (
  suoritus?: SuoritusWithOptionalVahvistus
): boolean => {
  const vahvistusPäivä = suoritus?.vahvistus?.päivä
  const vahvistusDate = vahvistusPäivä && parseISODate(vahvistusPäivä)
  const leikkuriDate = parseISODate(LAAJUUS_LEIKKURIPÄIVÄ)

  return !!vahvistusDate && !!leikkuriDate && vahvistusDate >= leikkuriDate
}
