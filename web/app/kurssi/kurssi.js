export const findKoodistoByDiaarinumero = (kurssiKoodistot, oppimaaranDiaarinumero) => {
  if (!kurssiKoodistot) return null

  return kurssiKoodistot.length > 1
    ? kurssiKoodistot.find(k => {
      const diaarinumeroaVastaavaKurssikoodisto = () => {
        switch (oppimaaranDiaarinumero) {
          case '60/011/2015':
          case '70/011/2015':
          case '56/011/2015': // Lukiokoulutukseen valmistava koulutus (valinnaisina suoritetut lukiokurssit)
            return 'lukionkurssit'
          case '33/011/2003':
            return 'lukionkurssitops2003nuoret'
          case '4/011/2004':
            return 'lukionkurssitops2004aikuiset'
        }
      }

      return k === diaarinumeroaVastaavaKurssikoodisto()
    })
    : kurssiKoodistot[0]
}

export const findDefaultKoodisto = kurssiKoodistot =>
  kurssiKoodistot.includes('lukionkurssit') ? 'lukionkurssit' : undefined
