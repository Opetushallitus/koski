import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'

export const isCompletedJotpaOsasuoritus =
  (suoritus: VapaanSivistystyönJotpaKoulutuksenSuoritus) =>
  (rowIndex: number) => {
    const osasuoritus = (suoritus.osasuoritukset || [])[rowIndex]
    if (!osasuoritus) {
      return false
    }
    return (
      osasuoritus.arviointi !== undefined && osasuoritus.arviointi.length > 0
    )
  }
