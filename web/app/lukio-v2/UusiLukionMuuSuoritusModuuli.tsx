import React from 'react'
import { LukionMuuModuuliMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionMuuModuuliMuissaOpinnoissa2019'
import { LukionModuulinSuoritusMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinSuoritusMuissaOpinnoissa2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019'
import { UusiLukionModuuliModaali } from './UusiLukionModuuli'

// Rinnakkainen UusiLukionModuuli.tsx:n kanssa "muissa opinnoissa"
// (lukiodiplomit/muut suoritukset/teemaopinnot) -kontekstia varten: jaettu
// UusiLukionModuuliModaali hoitaa koko lomakkeen (paikallisen kurssin lisäys,
// koodistohaku jne.) - tässä annetaan vain miten VALTAKUNNALLINEN moduuli
// rakennetaan tässä kontekstissa. Ei oppiaineen koodiarvoon perustuvaa
// 3-jakoa (ÄI/vieras kieli/muu) kuten UusiLukionModuuli.tsx:ssä, koska rivi
// ei ole mikään näistä oppiaineista - valtakunnallinen moduuli rakennetaan
// aina LukionMuuModuuliMuissaOpinnoissa2019:nä. Jätetään samasta syystä kuin
// UusiLukionModuuli.tsx:ssä pois LukionVieraanKielenModuuliMuissaOpinnoissa2019
// (vaatisi erillisen kieli-valinnan, jolle ei ole testattua vanhan
// käyttöliittymän skenaariota).
export const UusiLukionMuuSuoritusModuuli: React.FC<{
  olemassaOlevatKoodiarvot: string[]
  onAdd: (
    kurssi: LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019
  ) => void
  onClose: () => void
}> = ({ olemassaOlevatKoodiarvot, onAdd, onClose }) => (
  <UusiLukionModuuliModaali
    olemassaOlevatKoodiarvot={olemassaOlevatKoodiarvot}
    buildValtakunnallinenSuoritus={(tunniste, laajuus) =>
      LukionModuulinSuoritusMuissaOpinnoissa2019({
        koulutusmoduuli: LukionMuuModuuliMuissaOpinnoissa2019({
          tunniste,
          laajuus,
          pakollinen: false
        })
      })
    }
    onAdd={onAdd}
    onClose={onClose}
  />
)
