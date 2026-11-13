import React, { useContext, useMemo, useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import {
  filterOptions,
  mapOptionLabels,
  Select,
  SelectOption,
  useKoodistoOptions
} from '../components-v2/controls/Select'
import { LaajuusEdit } from '../components-v2/opiskeluoikeus/LaajuusField'
import { TextEdit } from '../components-v2/controls/TextField'
import { OpiskeluoikeusContext } from '../appstate/opiskeluoikeus'
import { usePreferences } from '../appstate/preferences'
import { finnish, t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukionÄidinkielenModuuli2019 } from '../types/fi/oph/koski/schema/LukionAidinkielenModuuli2019'
import { LukionMuuModuuliOppiaineissa2019 } from '../types/fi/oph/koski/schema/LukionMuuModuuliOppiaineissa2019'
import { LukionVieraanKielenModuuliOppiaineissa2019 } from '../types/fi/oph/koski/schema/LukionVieraanKielenModuuliOppiaineissa2019'
import { LukionModuulinSuoritusOppiaineissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinSuoritusOppiaineissa2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 } from '../types/fi/oph/koski/schema/LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019'
import { LukionPaikallinenOpintojakso2019 } from '../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import { LukionPaikallisenOpintojaksonSuoritus2019 } from '../types/fi/oph/koski/schema/LukionPaikallisenOpintojaksonSuoritus2019'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { koodiviiteId } from '../util/koodisto'

const VIERAAN_KIELEN_OPPIAINEKOODIT = ['A', 'B1', 'B2', 'B3', 'AOM']

const UUSI_PAIKALLINEN_KURSSI_KEY = 'uusi-paikallinen-kurssi'
const PAIKALLINEN_OPINTOJAKSO_PREF_TYPE = 'lukionpaikallinenopintojakso2019'

// Pelkkä koodiston nimi (esim. "Ruotsi A2, kurssi 2") ei riitä hakusanaksi -
// käyttäjä hakee yleensä kurssikoodilla (esim. "RUA2"). Sama temppu kuin
// ib/state/options.ts:n labelWithKoodiarvo, mutta paikallisena kopiona ettei
// lukio-v2 riipu IB:n tiedostoista. Jaettu lukio-v2:n sisällä (myös
// UusiOmanAidinkielenOpintojenKurssi.tsx:ssä), ei vain tämän tiedoston oma.
export const labelWithKoodiarvo = (
  o: SelectOption<Koodistokoodiviite<'moduulikoodistolops2021'>>
): string => (o.value?.koodiarvo ? `${o.value.koodiarvo} ${o.label}` : o.label)

// Sama kolmijako kuin oppiaineen koulutusmoduulilla
// (uusiValtakunnallinenLukionOppiaine, LukionOppiaineet.tsx), mutta moduulin
// tyyppi määräytyy OPPIAINEEN koodiarvosta, ei lisättävän moduulin omasta
// koodista. Ei tuoda ib/oppiaineet/tunnisteet.ts:n vastaavia tunnistimia -
// lukio-v2 ei riipu IB-koulutusmuodon koodista, vaikka luokittelu sattuukin
// olemaan sama.
const moduulinKoulutusmoduuli = (
  oppiaineKoodiarvo: string,
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021'>,
  laajuus: LaajuusOpintopisteissä
) => {
  if (oppiaineKoodiarvo === 'AI') {
    return LukionÄidinkielenModuuli2019({
      tunniste,
      laajuus,
      pakollinen: false
    })
  }
  if (VIERAAN_KIELEN_OPPIAINEKOODIT.includes(oppiaineKoodiarvo)) {
    return LukionVieraanKielenModuuliOppiaineissa2019({
      tunniste,
      laajuus,
      pakollinen: false
    })
  }
  return LukionMuuModuuliOppiaineissa2019({
    tunniste,
    laajuus,
    pakollinen: false
  })
}

// Jaettu modaali "Lisää osasuoritus" -toiminnolle sekä oppiaineiden että
// lukiodiplomien/muiden suoritusten/teemaopintojen (MuidenLukioOpintojenSuoritus2019,
// ks. UusiLukionMuuSuoritusModuuli.tsx) alle - molemmissa konteksteissa
// valinnat ja paikallisen opintojakson lisäys ovat identtiset, ainoastaan
// VALTAKUNNALLISEN moduulin koulutusmoduuli-tyyppi eroaa, siksi se annetaan
// kutsujalta buildValtakunnallinenSuoritus-callbackina.
export const UusiLukionModuuliModaali = <TValtakunnallinenSuoritus,>({
  olemassaOlevatKoodiarvot,
  buildValtakunnallinenSuoritus,
  onAdd,
  onClose
}: {
  olemassaOlevatKoodiarvot: string[]
  buildValtakunnallinenSuoritus: (
    tunniste: Koodistokoodiviite<'moduulikoodistolops2021'>,
    laajuus: LaajuusOpintopisteissä
  ) => TValtakunnallinenSuoritus
  onAdd: (
    kurssi:
      TValtakunnallinenSuoritus | LukionPaikallisenOpintojaksonSuoritus2019
  ) => void
  onClose: () => void
}) => {
  const [mode, setMode] = useState<'valtakunnallinen' | 'paikallinen'>(
    'valtakunnallinen'
  )
  const [tunniste, setTunniste] = useState<
    Koodistokoodiviite<'moduulikoodistolops2021'> | undefined
  >(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )
  const [paikallinenKoodiarvo, setPaikallinenKoodiarvo] = useState('')
  const [paikallinenNimi, setPaikallinenNimi] = useState('')
  const [paikallinenKuvaus, setPaikallinenKuvaus] = useState('')

  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const { preferences: paikallisetKurssit, remove: removePaikallinenKurssi } =
    usePreferences<LukionPaikallinenOpintojakso2019>(
      organisaatio?.oid,
      PAIKALLINEN_OPINTOJAKSO_PREF_TYPE
    )

  const kaikkiTunnisteet = useKoodistoOptions('moduulikoodistolops2021')
  const tunnisteOptions: SelectOption<
    Koodistokoodiviite<'moduulikoodistolops2021'> | PaikallinenKoodi
  >[] = useMemo(
    () => [
      ...mapOptionLabels(labelWithKoodiarvo)(
        filterOptions<Koodistokoodiviite<'moduulikoodistolops2021'>>(
          (o) =>
            !o.value || !olemassaOlevatKoodiarvot.includes(o.value.koodiarvo)
        )(kaikkiTunnisteet)
      ),
      ...(paikallisetKurssit || [])
        .filter((k) => !olemassaOlevatKoodiarvot.includes(k.tunniste.koodiarvo))
        .map((k) => ({
          key: `paikallinen-${k.tunniste.koodiarvo}`,
          label: `${k.tunniste.koodiarvo} ${t(k.tunniste.nimi)}`,
          value: k.tunniste,
          removable: true
        })),
      {
        key: UUSI_PAIKALLINEN_KURSSI_KEY,
        label: t('Lisää paikallinen kurssi...'),
        isAddNew: true,
        value: undefined
      }
    ],
    [kaikkiTunnisteet, olemassaOlevatKoodiarvot, paikallisetKurssit]
  )

  const voiLisätä =
    mode === 'valtakunnallinen'
      ? Boolean(tunniste && laajuus)
      : Boolean(
          paikallinenKoodiarvo &&
          paikallinenNimi &&
          paikallinenKuvaus &&
          laajuus
        )

  const lisää = () => {
    if (!laajuus || !voiLisätä) return
    if (mode === 'paikallinen') {
      onAdd(
        LukionPaikallisenOpintojaksonSuoritus2019({
          koulutusmoduuli: LukionPaikallinenOpintojakso2019({
            tunniste: PaikallinenKoodi({
              koodiarvo: paikallinenKoodiarvo,
              nimi: finnish(paikallinenNimi)
            }),
            laajuus,
            kuvaus: finnish(paikallinenKuvaus),
            pakollinen: false
          })
        })
      )
      return
    }
    if (!tunniste) return
    onAdd(buildValtakunnallinenSuoritus(tunniste, laajuus))
  }

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {mode === 'valtakunnallinen' ? (
          <label>
            {t('Osasuoritus')}
            <Select
              options={tunnisteOptions}
              value={tunniste && koodiviiteId(tunniste)}
              onChange={(option) => {
                if (!option) return
                if (option.key === UUSI_PAIKALLINEN_KURSSI_KEY) {
                  setMode('paikallinen')
                  return
                }
                if (option.key.startsWith('paikallinen-')) {
                  const stored = (paikallisetKurssit || []).find(
                    (k) => k.tunniste.koodiarvo === option.value?.koodiarvo
                  )
                  if (stored) {
                    onAdd(
                      LukionPaikallisenOpintojaksonSuoritus2019({
                        koulutusmoduuli: stored
                      })
                    )
                  }
                  return
                }
                setTunniste(
                  option.value as
                    Koodistokoodiviite<'moduulikoodistolops2021'> | undefined
                )
              }}
              onRemove={(option) => {
                if (option.key.startsWith('paikallinen-') && option.value) {
                  removePaikallinenKurssi(option.value.koodiarvo)
                }
              }}
              testId="tunniste"
            />
          </label>
        ) : (
          <>
            <label>
              {t('Koodi')}
              <TextEdit
                value={paikallinenKoodiarvo}
                onChange={(value) => setPaikallinenKoodiarvo(value || '')}
                placeholder={t('Koodi')}
                testId="paikallinenKoodi"
              />
            </label>
            <label>
              {t('Nimi')}
              <TextEdit
                value={paikallinenNimi}
                onChange={(value) => setPaikallinenNimi(value || '')}
                placeholder={t('Nimi')}
                testId="paikallinenNimi"
              />
            </label>
            <label>
              {t('Kuvaus')}
              <TextEdit
                value={paikallinenKuvaus}
                onChange={(value) => setPaikallinenKuvaus(value || '')}
                placeholder={t('Kuvaus')}
                large
                testId="paikallinenKuvaus"
              />
            </label>
          </>
        )}
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={laajuus}
            onChange={setLaajuus}
            createLaajuus={(arvo) => LaajuusOpintopisteissä({ arvo })}
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton onClick={lisää} disabled={!voiLisätä} testId="submit">
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

export const UusiLukionModuuli: React.FC<{
  oppiaineKoodiarvo: string
  olemassaOlevatKoodiarvot: string[]
  onAdd: (
    kurssi: LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019
  ) => void
  onClose: () => void
}> = ({ oppiaineKoodiarvo, olemassaOlevatKoodiarvot, onAdd, onClose }) => (
  <UusiLukionModuuliModaali
    olemassaOlevatKoodiarvot={olemassaOlevatKoodiarvot}
    buildValtakunnallinenSuoritus={(tunniste, laajuus) =>
      LukionModuulinSuoritusOppiaineissa2019({
        koulutusmoduuli: moduulinKoulutusmoduuli(
          oppiaineKoodiarvo,
          tunniste,
          laajuus
        )
      })
    }
    onAdd={onAdd}
    onClose={onClose}
  />
)
