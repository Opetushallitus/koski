import React, { useCallback, useMemo, useState } from 'react'
import { useKoodisto, useKoodistoFiller } from '../appstate/koodisto'
import { useOrganisaatioHierarkia } from '../appstate/organisaatioHierarkia'
import { usePeruste } from '../appstate/peruste'
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
import { Checkbox } from '../components-v2/controls/Checkbox'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Select, SelectOption } from '../components-v2/controls/Select'
import { TextEdit } from '../components-v2/controls/TextField'
import { organisaatioOptionDisplay } from '../components-v2/opiskeluoikeus/OrganisaatioOption'
import { t } from '../i18n/i18n'
import { OrganisaatioHierarkia } from '../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Oppilaitos } from '../types/fi/oph/koski/schema/Oppilaitos'
import { OrganisaatioWithOid } from '../types/fi/oph/koski/schema/OrganisaatioWithOid'
import { PerusopetuksenLuokkaAste } from '../types/fi/oph/koski/schema/PerusopetuksenLuokkaAste'
import { PerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import {
  PerusopetuksenVuosiluokanSuoritus,
  isPerusopetuksenVuosiluokanSuoritus
} from '../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuoritus'
import { OppiaineenTaiToiminta_AlueenSuoritus } from '../types/fi/oph/koski/schema/OppiaineenTaiToimintaAlueenSuoritus'
import { PerusopetuksenToiminta_Alue } from '../types/fi/oph/koski/schema/PerusopetuksenToimintaAlue'
import { PerusopetuksenToiminta_AlueenSuoritus } from '../types/fi/oph/koski/schema/PerusopetuksenToimintaAlueenSuoritus'
import { Toimipiste } from '../types/fi/oph/koski/schema/Toimipiste'
import {
  luokkaAsteenOppiaineet,
  puuttuvatLuokkaAsteet
} from './luokkaAsteenOppiaineet'
import { isToimintaAlueittainOpiskelu } from './toimintaAlueittain'

export type UusiPerusopetuksenVuosiluokanSuoritusModalProps = CommonProps<{
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
  onSubmit: (suoritus: PerusopetuksenVuosiluokanSuoritus) => void
  onClose: () => void
}>

export const UusiPerusopetuksenVuosiluokanSuoritusModal: React.FC<
  UusiPerusopetuksenVuosiluokanSuoritusModalProps
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

  const perusteet = usePeruste('perusopetuksenvuosiluokka')
  const perusteOptions = useMemo<SelectOption<string>[]>(
    () =>
      (perusteet || []).map((p) => ({
        key: p.koodiarvo,
        label: `${p.koodiarvo} ${t(p.nimi)}`,
        value: p.koodiarvo
      })),
    [perusteet]
  )
  const [perusteenDiaarinumero, setPerusteenDiaarinumero] = useState<
    string | undefined
  >(() => findPerusteenDiaarinumero(opiskeluoikeus))

  const valid =
    luokkaAste !== undefined &&
    luokka !== undefined &&
    luokka.length > 0 &&
    !!toimipiste &&
    !!alkamispäivä &&
    !!perusteenDiaarinumero

  // Esitäytön kirjaustapa. Oletus lisätietolipusta, mutta käyttäjä voi vaihtaa:
  // lippu on päivämäärätön, joten päättynytkin toiminta-aluepäätös ohjaisi
  // muuten esitäytön yhä toiminta-alueisiin (TOR-2596).
  const [kirjataanToimintaAlueittain, setKirjataanToimintaAlueittain] =
    useState(() => isToimintaAlueittainOpiskelu(opiskeluoikeus))

  const pohjasuoritus = opiskeluoikeus.suoritukset[0]

  const onSubmit = useCallback(async () => {
    if (!valid) return
    const tunniste = (luokkaAsteet || []).find(
      (k) => k.koodiviite.koodiarvo === luokkaAste
    )?.koodiviite
    if (!tunniste || !toimipiste || !luokka || !alkamispäivä) return

    const osasuoritukset = createLuokkaAsteenOsasuoritukset(
      tunniste.koodiarvo,
      kirjataanToimintaAlueittain,
      perusteenDiaarinumero
    )

    const suoritus = PerusopetuksenVuosiluokanSuoritus({
      koulutusmoduuli: PerusopetuksenLuokkaAste({
        tunniste,
        perusteenDiaarinumero
      }),
      luokka,
      toimipiste,
      alkamispäivä,
      suorituskieli: pohjasuoritus?.suorituskieli,
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
    kirjataanToimintaAlueittain,
    pohjasuoritus?.suorituskieli,
    fillKoodistot,
    props
  ])

  return (
    <Modal
      {...common(props, ['UusiPerusopetuksenVuosiluokanSuoritusModal'])}
      onClose={props.onClose}
    >
      <TestIdLayer id="uusiVuosiluokanSuoritus">
        <ModalTitle>{t('Suorituksen lisäys')}</ModalTitle>
        <ModalBody>
          {/*
            Modaali kohdistaa auetessaan ensimmäiseen kenttään, ja Select avaa
            vaihtoehtolistansa onFocusissa myös ohjelmallisesta fokusoinnista.
            Valikkokentät ohitetaan siksi kohdistuksessa, jolloin fokus menee
            Luokka-kenttään — ensimmäiseen kenttään joka oikeasti odottaa
            syötettä — eikä yksikään lista aukea lomakkeen päälle.
          */}
          <Label label="Peruste">
            <Select
              options={perusteOptions}
              value={perusteenDiaarinumero}
              onChange={(o) => setPerusteenDiaarinumero(o?.value)}
              skipAutoFocus
              testId="peruste"
            />
          </Label>

          <Label label="Luokka-aste">
            <Select
              options={luokkaAsteOptions}
              value={luokkaAste}
              onChange={(o) => setLuokkaAste(o?.key)}
              inlineOptions
              skipAutoFocus
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

          <Checkbox
            checked={kirjataanToimintaAlueittain}
            onChange={setKirjataanToimintaAlueittain}
            label="Kirjataan toiminta-alueittain"
            testId="kirjaustapa"
          />
          <p className="uusiVuosiluokka__kirjaustapa-ohje">
            {t('Esitäyttää viisi toiminta-aluetta oppiaineiden sijaan.')}
          </p>
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
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
): string[] =>
  opiskeluoikeus.suoritukset
    .filter(isPerusopetuksenVuosiluokanSuoritus)
    .filter((s) => !s.jääLuokalle)
    .map((s) => s.koulutusmoduuli.tunniste.koodiarvo)

const viimeisinToimipiste = (
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
): OrganisaatioWithOid | undefined => {
  const vuosiluokat = opiskeluoikeus.suoritukset.filter(
    isPerusopetuksenVuosiluokanSuoritus
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
  opiskeluoikeus: PerusopetuksenOpiskeluoikeus
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
      display: organisaatioOptionDisplay(h),
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
): OppiaineenTaiToiminta_AlueenSuoritus[] | undefined => {
  if (luokkaAste === '9') {
    return undefined
  }

  const osasuoritukset = toimintaAlueittain
    ? toimintaAlueidenSuoritukset()
    : luokkaAsteenOppiaineet(luokkaAste, perusteenDiaarinumero)

  return osasuoritukset && osasuoritukset.length > 0
    ? osasuoritukset
    : undefined
}

const toimintaAlueidenSuoritukset =
  (): PerusopetuksenToiminta_AlueenSuoritus[] =>
    ['1', '2', '3', '4', '5'].map((koodiarvo) =>
      PerusopetuksenToiminta_AlueenSuoritus({
        koulutusmoduuli: PerusopetuksenToiminta_Alue({
          tunniste: Koodistokoodiviite({
            koodiarvo,
            koodistoUri: 'perusopetuksentoimintaalue'
          })
        })
      })
    )
