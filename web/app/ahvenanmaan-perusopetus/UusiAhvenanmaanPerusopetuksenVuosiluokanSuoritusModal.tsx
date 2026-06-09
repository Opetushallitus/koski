import React, { useCallback, useMemo, useState } from 'react'
import { useKoodisto, useKoodistoFiller } from '../appstate/koodisto'
import { useOrganisaatioHierarkia } from '../appstate/organisaatioHierarkia'
import { TestIdLayer } from '../appstate/useTestId'
import { common, CommonProps } from '../components-v2/CommonProps'
import { Label } from '../components-v2/containers/Label'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { DateEdit } from '../components-v2/controls/DateField'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Select, SelectOption } from '../components-v2/controls/Select'
import { TextEdit } from '../components-v2/controls/TextField'
import { t } from '../i18n/i18n'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus'
import { AhvenanmaanPerusopetuksenLuokkaAste } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenLuokkaAste'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'
import { AhvenanmaanPerusopetuksenToimintaAlue } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenToimintaAlue'
import {
  AhvenanmaanPerusopetuksenToimintaAlueenSuoritus,
  isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenToimintaAlueenSuoritus'
import {
  AhvenanmaanPerusopetuksenVuosiluokanSuoritus,
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVuosiluokanSuoritus'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { OrganisaatioWithOid } from '../types/fi/oph/koski/schema/OrganisaatioWithOid'
import { Toimipiste } from '../types/fi/oph/koski/schema/Toimipiste'
import { puuttuvatLuokkaAsteet } from '../perusopetus-v2/luokkaAsteenOppiaineet'
import { ahvenanmaanLuokkaAsteenOppiaineet } from './ahvenanmaanLuokkaAsteenOppiaineet'

// Ahvenanmaan opetussuunnitelman diaarinumero (ÅLp21). Käytetään oletuksena, jos
// opiskeluoikeudessa ei vielä ole suoritusta jolta diaarinumeron voisi periä.
const AHVENANMAAN_DIAARINUMERO = 'ÅLR2020/9841'

export type UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModalProps =
  CommonProps<{
    opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus
    onSubmit: (suoritus: AhvenanmaanPerusopetuksenVuosiluokanSuoritus) => void
    onClose: () => void
  }>

export const UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal: React.FC<
  UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModalProps
> = (props) => {
  const { opiskeluoikeus } = props
  const fillKoodistot = useKoodistoFiller()

  const missingLuokkaAsteet = useMemo(
    () => puuttuvatLuokkaAsteet(existingLuokkaAsteet(opiskeluoikeus)),
    [opiskeluoikeus]
  )

  const luokkaAsteet = useKoodisto(
    'perusopetuksenluokkaaste',
    missingLuokkaAsteet
  )
  const luokkaAsteOptions = useMemo(
    () =>
      (luokkaAsteet || [])
        .map(
          (
            k
          ): SelectOption<Koodistokoodiviite<'perusopetuksenluokkaaste'>> => ({
            key: k.koodiviite.koodiarvo,
            label: t(k.koodiviite.nimi),
            value: k.koodiviite
          })
        )
        .sort((a, b) => parseInt(a.key, 10) - parseInt(b.key, 10)),
    [luokkaAsteet]
  )

  const [luokkaAste, setLuokkaAste] = useState<string | undefined>(
    missingLuokkaAsteet[0]
  )
  const [luokka, setLuokka] = useState<string | undefined>(undefined)
  const [alkamispäivä, setAlkamispäivä] = useState<string | undefined>(
    undefined
  )
  const [toimipiste, setToimipiste] = useState<OrganisaatioWithOid | undefined>(
    viimeisinToimipiste(opiskeluoikeus)
  )

  const [toimipisteQuery, setToimipisteQuery] = useState('')
  const hierarkia = useOrganisaatioHierarkia(toimipisteQuery)
  const toimipisteOptions = useMemo(
    () => hierarkiaToOptions(hierarkia),
    [hierarkia]
  )

  // Ahvenanmaan ops ei ole ePerusteissa, joten diaarinumeroa ei valita
  // pudotusvalikosta vaan se peritään olemassa olevasta suorituksesta.
  const perusteenDiaarinumero =
    findPerusteenDiaarinumero(opiskeluoikeus) ?? AHVENANMAAN_DIAARINUMERO

  const valid =
    luokkaAste !== undefined &&
    luokka !== undefined &&
    luokka.length > 0 &&
    !!toimipiste &&
    !!alkamispäivä

  const pohjasuoritus = opiskeluoikeus.suoritukset[0]

  const onSubmit = useCallback(async () => {
    if (!valid) return
    const tunniste = (luokkaAsteet || []).find(
      (k) => k.koodiviite.koodiarvo === luokkaAste
    )?.koodiviite
    if (!tunniste || !toimipiste || !luokka || !alkamispäivä) return

    const osasuoritukset = createLuokkaAsteenOsasuoritukset(
      tunniste.koodiarvo,
      isToimintaAlueittain(opiskeluoikeus),
      perusteenDiaarinumero
    )

    const suoritus = AhvenanmaanPerusopetuksenVuosiluokanSuoritus({
      koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste({
        tunniste,
        perusteenDiaarinumero
      }),
      luokka,
      toimipiste,
      alkamispäivä,
      suorituskieli:
        pohjasuoritus?.suorituskieli ??
        Koodistokoodiviite({ koodiarvo: 'SV', koodistoUri: 'kieli' }),
      osasuoritukset
    })

    const filled = await fillKoodistot(suoritus)
    props.onSubmit(filled)
  }, [
    valid,
    luokkaAsteet,
    luokkaAste,
    toimipiste,
    luokka,
    alkamispäivä,
    perusteenDiaarinumero,
    opiskeluoikeus,
    pohjasuoritus?.suorituskieli,
    fillKoodistot,
    props
  ])

  return (
    <Modal
      {...common(props, [
        'UusiAhvenanmaanPerusopetuksenVuosiluokanSuoritusModal'
      ])}
      onClose={props.onClose}
    >
      <TestIdLayer id="uusiVuosiluokanSuoritus">
        <ModalTitle>{t('Suorituksen lisäys')}</ModalTitle>
        <ModalBody>
          <Label label="Luokka-aste">
            <Select
              options={luokkaAsteOptions}
              value={luokkaAste}
              onChange={(o) => setLuokkaAste(o?.key)}
              inlineOptions
              testId="tunniste"
            />
          </Label>

          <Label label="Luokka">
            <TextEdit value={luokka} onChange={setLuokka} testId="luokka" />
          </Label>

          <Label label="Toimipiste">
            <Select
              options={toimipisteOptions}
              value={toimipiste?.oid}
              onChange={(o) => setToimipiste(o?.value)}
              onSearch={setToimipisteQuery}
              testId="toimipiste"
            />
          </Label>

          <Label label="Alkamispäivä">
            <DateEdit
              value={alkamispäivä}
              onChange={setAlkamispäivä}
              testId="alkamispäivä"
            />
          </Label>
        </ModalBody>

        <ModalFooter>
          <FlatButton onClick={props.onClose} testId="cancel">
            {t('Peruuta')}
          </FlatButton>
          <RaisedButton onClick={onSubmit} disabled={!valid} testId="submit">
            {t('Lisää')}
          </RaisedButton>
        </ModalFooter>
      </TestIdLayer>
    </Modal>
  )
}

const existingLuokkaAsteet = (
  opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus
): string[] =>
  opiskeluoikeus.suoritukset
    .filter(isAhvenanmaanPerusopetuksenVuosiluokanSuoritus)
    .filter((s) => !s.jääLuokalle)
    .map((s) => s.koulutusmoduuli.tunniste.koodiarvo)

// Toiminta-alueittain opiskelu päätellään olemassa olevista osasuorituksista
// (Ahvenanmaan lisätiedoissa ei ole tätä lippua).
const isToimintaAlueittain = (
  opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus
): boolean =>
  opiskeluoikeus.suoritukset.some((s) =>
    (s.osasuoritukset || []).some(
      isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus
    )
  )

const viimeisinToimipiste = (
  opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus
): OrganisaatioWithOid | undefined => {
  const vuosiluokat = opiskeluoikeus.suoritukset.filter(
    isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
  )
  const latest = vuosiluokat.reduce<(typeof vuosiluokat)[number] | undefined>(
    (acc, s) => {
      const asteAcc = acc
        ? parseInt(acc.koulutusmoduuli.tunniste.koodiarvo, 10)
        : -Infinity
      const asteS = parseInt(s.koulutusmoduuli.tunniste.koodiarvo, 10)
      return asteS > asteAcc ? s : acc
    },
    undefined
  )
  if (latest) return latest.toimipiste
  return opiskeluoikeus.suoritukset[0]?.toimipiste
}

const findPerusteenDiaarinumero = (
  opiskeluoikeus: AhvenanmaanPerusopetuksenOpiskeluoikeus
): string | undefined => {
  for (const s of opiskeluoikeus.suoritukset) {
    const km = (s as { koulutusmoduuli?: { perusteenDiaarinumero?: string } })
      .koulutusmoduuli
    if (km?.perusteenDiaarinumero) return km.perusteenDiaarinumero
  }
  return undefined
}

const hierarkiaToOptions = (
  hs: OrganisaatioHierarkia[]
): SelectOption<OrganisaatioWithOid>[] =>
  hs.map((h) => {
    const isOppilaitos = h.organisaatiotyypit.includes('OPPILAITOS')
    const org: OrganisaatioWithOid = isOppilaitos
      ? Oppilaitos({
          oid: h.oid,
          nimi: h.nimi,
          // @ts-expect-error — hierarkia sisältää oppilaitosnumeron ja kotipaikan
          oppilaitosnumero: h.oppilaitosnumero,
          // @ts-expect-error
          kotipaikka: h.kotipaikka
        })
      : Toimipiste({ oid: h.oid, nimi: h.nimi })
    return {
      key: h.oid,
      label: t(h.nimi),
      value: org,
      children:
        h.children && h.children.length > 0
          ? hierarkiaToOptions(h.children)
          : undefined,
      ignoreFilter: true
    }
  })

const createLuokkaAsteenOsasuoritukset = (
  luokkaAste: string,
  toimintaAlueittain: boolean,
  perusteenDiaarinumero?: string
): AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus[] | undefined => {
  // 9. luokan päättövuoden arvosanat kirjataan päättötodistukselle
  // (avgångsbetyg), joten vuosiluokan suoritus jätetään tyhjäksi – kuten
  // manner-Suomessa. Luokalle jäävän 9. luokan läsårsbetyg täytetään käsin.
  if (luokkaAste === '9') {
    return undefined
  }

  const osasuoritukset = toimintaAlueittain
    ? toimintaAlueidenSuoritukset()
    : ahvenanmaanLuokkaAsteenOppiaineet(luokkaAste, perusteenDiaarinumero)

  return osasuoritukset && osasuoritukset.length > 0
    ? osasuoritukset
    : undefined
}

const toimintaAlueidenSuoritukset =
  (): AhvenanmaanPerusopetuksenToimintaAlueenSuoritus[] =>
    ['1', '2', '3', '4', '5'].map((koodiarvo) =>
      AhvenanmaanPerusopetuksenToimintaAlueenSuoritus({
        koulutusmoduuli: AhvenanmaanPerusopetuksenToimintaAlue({
          tunniste: Koodistokoodiviite({
            koodiarvo,
            koodistoUri: 'ahvenanmaanperusopetuksentoimintaalue'
          })
        })
      })
    )
