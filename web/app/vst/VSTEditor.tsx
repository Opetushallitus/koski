import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { append } from '../util/fp/arrays'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  FormModel,
  FormOptic,
  getValue,
  useForm
} from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { localize, t } from '../i18n/i18n'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { KoodiarvotOf } from '../util/koodisto'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { assertNever } from '../util/selfcare'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { Trans } from '../components-v2/texts/Trans'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  constructOsasuorituksetOpenState,
  OsasuorituksetExpandedState,
  OsasuoritusRowData,
  OsasuoritusTable,
  osasuoritusTestId,
  SetModal,
  SetOsasuoritusOpen,
  ToggleOsasuoritusOpen
} from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { FormField } from '../components-v2/forms/FormField'
import { LocalizedTextView } from '../components-v2/controls/LocalizedTestField'
import { DateView, DateEdit } from '../components-v2/controls/DateField'
import {
  LaajuusEdit,
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../components-v2/opiskeluoikeus/ArvosanaField'
import { lastElement } from '../util/optics'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  Koodistokoodiviite,
  isKoodistokoodiviite
} from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../components-v2/opiskeluoikeus/SuorituskieliField'
import {
  TodistuksellaNäkyvätLisätiedotEdit,
  TodistuksellaNäkyvätLisätiedotView
} from '../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import {
  PaikallinenKoodi,
  isPaikallinenKoodi
} from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import {
  isVapaanSivistystyönJotpaKoulutuksenSuoritus,
  VapaanSivistystyönJotpaKoulutuksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import {
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus,
  VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import {
  ToimipisteEdit,
  ToimipisteView
} from '../components-v2/opiskeluoikeus/OpiskeluoikeudenToimipiste'
import {
  hasArviointi,
  hasOsasuoritustenOsasuorituksia,
  isLaajuuksellinenVSTKoulutusmoduuli,
  isPerusteellinenVSTKoulutusmoduuli,
  VSTOsasuoritus,
  VSTOsasuoritusOsasuorituksilla
} from './typeguards'
import {
  createVstArviointi,
  resolveOpiskeluoikeudenTilaClass
} from './resolvers'
import { UusiVSTOsasuoritusModal } from './UusiVSTOsasuoritusModal'
import { VSTLisatiedot } from './VSTLisatiedot'
import {
  OpintokokonaisuusEdit,
  OpintokokonaisuusView
} from '../components-v2/opiskeluoikeus/OpintokokonaisuusField'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import {
  isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus,
  VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönLukutaidonKokonaisuus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaidonKokonaisuus'
import {
  TaitotasoEdit,
  TaitotasoView
} from '../components-v2/opiskeluoikeus/TaitotasoField'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { PaikallinenOsasuoritusSelect } from '../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { subTestId } from '../components-v2/CommonProps'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'

/**
 * Tarkistaa, että koodistokoodiviitteen koodistoUri ja koodiarvo täsmäävät haluttuun arvoon. Tämä lisäksi tarkentaa Koodistokoodiviitteen tyypin.
 * @param val Tarkistettava koodistokoodiviite
 * @param koodistoUri Koodiston URI
 * @param koodiarvo  Koodiarvo
 * @returns Onko Koodistokoodiviite tyyppiä Koodistokoodiviite<T, K>
 */
function narrowKoodistokoodiviite<T extends string, K extends string>(
  val: unknown,
  koodistoUri: T,
  koodiarvo?: K
): val is K extends string
  ? Koodistokoodiviite<T, K>
  : Koodistokoodiviite<T, string> {
  if (!isKoodistokoodiviite(val)) {
    return false
  }
  if (val.koodistoUri !== koodistoUri) {
    return false
  }
  if (koodiarvo !== undefined && val.koodiarvo !== koodiarvo) {
    return false
  }
  return true
}

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

const vstNimi = (opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus) =>
  `${t(
    opiskeluoikeus.suoritukset[0]?.koulutusmoduuli.tunniste.nimi
  )}`.toLowerCase()

const vstSuorituksenNimi = (suoritus: VapaanSivistystyönPäätasonSuoritus) => {
  const titles: Record<
    KoodiarvotOf<VapaanSivistystyönPäätasonSuoritus['tyyppi']>,
    string
  > = {
    vstjotpakoulutus: 'Vapaan sivistystyön koulutus',
    vstlukutaitokoulutus: 'Lukutaitokoulutus oppivelvollisille',
    vstmaahanmuuttajienkotoutumiskoulutus:
      'Kotoutumiskoulutus oppivelvollisille',
    vstoppivelvollisillesuunnattukoulutus:
      'Kansanopistojen vapaan sivistystyön koulutus oppivelvollisille',
    vstvapaatavoitteinenkoulutus: 'Vapaan sivistystyön koulutus'
  }

  return localize(`${t(titles[suoritus.tyyppi.koodiarvo])}`)
}

const defaultLaajuusOpintopisteissa = LaajuusOpintopisteissä({
  arvo: 0,
  yksikkö: Koodistokoodiviite({
    koodiarvo: '2',
    nimi: Finnish({
      fi: 'opintopistettä',
      sv: 'studiepoäng',
      en: 'ECTS credits'
    }),
    lyhytNimi: Finnish({
      fi: 'op',
      sv: 'sp',
      en: 'ECTS cr'
    }),
    koodistoUri: 'opintojenlaajuusyksikko'
  })
})

type ModalState = {
  open: boolean
  data: {
    osasuoritusPath: FormOptic<
      VapaanSivistystyönPäätasonSuoritus,
      VSTOsasuoritusOsasuorituksilla
    >
  } | null
}

const createVstOpiskeluoikeusjakso =
  (form: FormModel<VapaanSivistystyönOpiskeluoikeus>) =>
  (seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>) => {
    const { $class: jaksoClass } = form.state.tila.opiskeluoikeusjaksot[0]
    switch (jaksoClass) {
      case VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className:
        return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso>
        )
      case OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className:
        return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
        )
      case VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className:
        return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
          seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso>
        )
      default:
        return assertNever(jaksoClass)
    }
  }

// TODO: Siivoa omaan tiedostoon
function hasOpintokokonaisuus(
  x: any
): x is
  | VapaanSivistystyönJotpaKoulutuksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus {
  return (
    isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(x) ||
    isVapaanSivistystyönJotpaKoulutuksenSuoritus(x)
  )
}

export const VSTEditor: React.FC<VSTEditorProps> = (props) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema('VapaanSivistystyönOpiskeluoikeus')

  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  const [modal, setModal] = useState<ModalState>({ open: false, data: null })

  // Oppilaitos
  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  // Päätason suoritus
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  const appendOsasuoritus = useCallback(
    (newOsasuoritus: VSTOsasuoritus) => {
      const osPath = modal.data?.osasuoritusPath
      if (osPath !== undefined) {
        form.updateAt(osPath, (päätaso) => ({
          ...päätaso,
          // @ts-expect-error
          osasuoritukset: append(newOsasuoritus)(päätaso.osasuoritukset)
        }))
      }
    },
    [form, modal.data?.osasuoritusPath]
  )

  const appendPäätasonOsasuoritus = useCallback(
    (newOsasuoritus: VSTOsasuoritus) => {
      form.updateAt(päätasonSuoritus.path, (päätaso) => ({
        ...päätaso,
        // @ts-expect-error
        osasuoritukset: append(newOsasuoritus)(päätaso.osasuoritukset)
      }))
    },
    [form, päätasonSuoritus.path]
  )

  const createOsasuoritus = useCallback(
    (tunniste: PaikallinenKoodi | Koodistokoodiviite) => {
      // 1) VapaanSivistystyönJotpaKoulutuksenSuoritus
      if (
        isVapaanSivistystyönJotpaKoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        ) &&
        isPaikallinenKoodi(tunniste)
      ) {
        appendOsasuoritus(
          VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
              tunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )

        // 2) OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
      } else if (
        isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
          päätasonSuoritus.suoritus
        ) &&
        isKoodistokoodiviite(tunniste)
      ) {
        if (
          narrowKoodistokoodiviite(
            tunniste,
            'vstkoto2022kokonaisuus',
            'kielijaviestintaosaaminen'
          )
        ) {
          appendOsasuoritus(
            VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli({
                  tunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                })
            })
          )
        } else if (
          narrowKoodistokoodiviite(tunniste, 'vstkoto2022kokonaisuus', 'ohjaus')
        ) {
          appendOsasuoritus(
            VSTKotoutumiskoulutuksenOhjauksenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022({
                  tunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                })
            })
          )
        } else if (
          narrowKoodistokoodiviite(
            tunniste,
            'vstkoto2022kokonaisuus',
            'valinnaisetopinnot'
          )
        ) {
          appendOsasuoritus(
            VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(
                  {
                    tunniste,
                    laajuus: defaultLaajuusOpintopisteissa
                  }
                )
            })
          )
        } else if (
          narrowKoodistokoodiviite(
            tunniste,
            'vstkoto2022kokonaisuus',
            'yhteiskuntajatyoelamaosaaminen'
          )
        ) {
          appendOsasuoritus(
            VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(
                  {
                    tunniste,
                    laajuus: defaultLaajuusOpintopisteissa
                  }
                )
            })
          )
        }
      } else if (
        isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        )
      ) {
        console.log(
          'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
        )
      } else if (
        isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        )
      ) {
        console.log(
          'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
        )
      } else if (
        isVapaanSivistystyönLukutaitokoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        ) &&
        isKoodistokoodiviite(tunniste) &&
        narrowKoodistokoodiviite(tunniste, 'vstlukutaitokoulutuksenkokonaisuus')
      ) {
        appendOsasuoritus(
          VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
            koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
              tunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else if (
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        ) &&
        isPaikallinenKoodi(tunniste)
      ) {
        appendOsasuoritus(
          VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli:
              VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
                tunniste,
                laajuus: defaultLaajuusOpintopisteissa,
                // TODO: Kytke lomake
                kuvaus: Finnish({ fi: 'Foo Bar' })
              })
          })
        )
      } else if (
        isVapaanSivistystyönLukutaitokoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        ) &&
        narrowKoodistokoodiviite(tunniste, 'vstlukutaitokoulutuksenkokonaisuus')
      ) {
        appendOsasuoritus(
          VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
            koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
              tunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else {
        throw new Error(
          `Not yet implemented for ${päätasonSuoritus.suoritus.$class}`
        )
      }
      setModal({ open: false, data: null })
    },
    [appendOsasuoritus, päätasonSuoritus.suoritus]
  )

  const createPaikallinenPäätasonOsasuoritus = useCallback(
    (tunniste: PaikallinenKoodi, isNew: boolean) => {
      console.log('createPaikallinenPäätasonOsasuoritus tunniste', tunniste)
      // 1) VapaanSivistystyönJotpaKoulutuksenSuoritus
      if (
        isVapaanSivistystyönJotpaKoulutuksenSuoritus(päätasonSuoritus.suoritus)
      ) {
        appendPäätasonOsasuoritus(
          VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
              tunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )

        // 2) VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
      } else if (
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
          päätasonSuoritus.suoritus
        ) &&
        isPaikallinenKoodi(tunniste)
      ) {
        appendPäätasonOsasuoritus(
          VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli:
              VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
                tunniste,
                laajuus: defaultLaajuusOpintopisteissa,
                // TODO: Kytke lomake
                kuvaus: Finnish({ fi: 'Foo Bar' })
              })
          })
        )
      } else {
        throw new Error(
          `Not yet implemented for ${päätasonSuoritus.suoritus.$class}`
        )
      }
    },
    [appendPäätasonOsasuoritus, päätasonSuoritus.suoritus]
  )

  const [osasuorituksetOpenState, setOsasuorituksetOpenState] =
    useState<OsasuorituksetExpandedState>([])

  // TODO: Osasuorituksen lisääminen sulkee kaiken. Pitäisi mergettää vanha lista uuden päälle, jotta tilatieto säilyy.
  useEffect(() => {
    setOsasuorituksetOpenState((oldState) => {
      return constructOsasuorituksetOpenState(
        oldState,
        0,
        päätasonSuoritus.index,
        päätasonSuoritus.suoritus.osasuoritukset || []
      )
    })
  }, [päätasonSuoritus.index, päätasonSuoritus.suoritus.osasuoritukset])

  const allOsasuorituksetOpen = osasuorituksetOpenState.every(
    (val) => val.expanded === true
  )

  const isAnyModalOpen =
    Object.values(osasuorituksetOpenState).some(
      (val) => val.expanded === true
    ) || allOsasuorituksetOpen

  const toggleOsasuorituksetOpenState = useCallback(() => {
    setOsasuorituksetOpenState((oldState) =>
      oldState.map((item) => ({ ...item, expanded: !isAnyModalOpen }), oldState)
    )
  }, [isAnyModalOpen])

  const setOsasuorituksetStateHandler = useCallback(
    (key: string, expanded: boolean) => {
      setOsasuorituksetOpenState((oldState) =>
        oldState.map((s) => {
          if (s.key === key) {
            return { ...s, expanded }
          } else {
            return s
          }
        })
      )
    },
    []
  )

  const suorituksenVahvistus = useMemo(() => {
    // const suoritukset = getValue(päätasonSuoritus.path)(form.state)
    // const _os = suoritukset?.osasuoritukset as VSTOsasuoritus[] | undefined
    // TODO: Logiikka suorituksen vahvistukselle
    return false
  }, [])

  const rootLevel = 0

  // Render
  return (
    <>
      <OpiskeluoikeusTitle
        opiskeluoikeus={form.state}
        opiskeluoikeudenNimi={vstNimi(form.state)}
      />
      <EditorContainer
        form={form}
        invalidatable={props.invalidatable}
        oppijaOid={props.oppijaOid}
        suorituksenNimi={vstSuorituksenNimi}
        createOpiskeluoikeusjakso={createVstOpiskeluoikeusjakso(form)}
        opiskeluoikeusJaksoClassName={resolveOpiskeluoikeudenTilaClass(
          päätasonSuoritus
        )}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={setPäätasonSuoritus}
        testId="vst-editor-container"
      >
        {form.editMode && modal.open && (
          <UusiVSTOsasuoritusModal
            päätasonSuoritus={päätasonSuoritus}
            data={modal.data}
            onCancel={() => {
              setModal({ open: false, data: null })
            }}
            onSubmit={(data) => {
              // Luodaan uusi osasuoritus
              createOsasuoritus(data)
            }}
          />
        )}
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <KeyValueTable>
          <KeyValueRow
            label="Oppilaitos / toimipiste"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.oppilaitos`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('toimipiste')}
              view={ToimipisteView}
              edit={ToimipisteEdit}
              editProps={{
                onChangeToimipiste: (data) => {
                  form.updateAt(
                    päätasonSuoritus.path.prop('toimipiste').optional(),
                    () => data
                  )
                }
              }}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Koulutus"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutus`}
          >
            <Trans>
              {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}
            </Trans>
          </KeyValueRow>
          <KeyValueRow
            label="Koulutusmoduuli"
            indent={2}
            testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutusmoduuli`}
          >
            {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
          </KeyValueRow>
          {isPerusteellinenVSTKoulutusmoduuli(
            päätasonSuoritus.suoritus.koulutusmoduuli
          ) && (
            <KeyValueRow
              label="Peruste"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.peruste`}
            >
              <FormField
                form={form}
                path={päätasonSuoritus.path
                  .prop('koulutusmoduuli')
                  .guard(isPerusteellinenVSTKoulutusmoduuli)
                  .prop('perusteenDiaarinumero')
                  .optional()}
                view={PerusteView}
                edit={PerusteEdit}
                testId="koulutusmoduuli.peruste"
                // TODO: Olisi kiva, jos Edit-komponentin vaaditut propsit eivät tyypity Partial-tyyppisiksi.
                editProps={{
                  diaariNumero: 'vstmaahanmuuttajienkotoutumiskoulutus'
                }}
              />
            </KeyValueRow>
          )}
          {hasOpintokokonaisuus(päätasonSuoritus.suoritus) && (
            <KeyValueRow
              label="Opintokokonaisuus"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.peruste`}
            >
              <FormField
                form={form}
                path={päätasonSuoritus.path
                  .guard(hasOpintokokonaisuus)
                  .prop('koulutusmoduuli')
                  .prop('opintokokonaisuus')
                  .optional()}
                view={OpintokokonaisuusView}
                edit={OpintokokonaisuusEdit}
              />
            </KeyValueRow>
          )}
          {isLaajuuksellinenVSTKoulutusmoduuli(
            päätasonSuoritus.suoritus.koulutusmoduuli
          ) && (
            <KeyValueRow
              label="Laajuus"
              indent={2}
              testId={`vst.suoritukset.${päätasonSuoritus.index}.koulutuksen-laajuus`}
            >
              <FormField
                form={form}
                path={päätasonSuoritus.path
                  .prop('koulutusmoduuli')
                  .guard(isLaajuuksellinenVSTKoulutusmoduuli)
                  .prop('laajuus')}
                view={LaajuusView}
                edit={LaajuusEdit}
                editProps={{
                  createLaajuus: (arvo: number) => {
                    return LaajuusOpintopisteissä({
                      arvo,
                      yksikkö: Koodistokoodiviite({
                        koodistoUri: 'opintojenlaajuusyksikko',
                        koodiarvo: '2'
                      })
                    })
                  }
                }}
              />
            </KeyValueRow>
          )}
          <KeyValueRow
            label="Opetuskieli"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.opetuskieli`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('suorituskieli')}
              // TODO: Path on suorituskieli, mutta käyttöliittymässä käytetään opetuskieltä?
              view={SuorituskieliView}
              edit={SuorituskieliEdit}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Todistuksella näkyvät lisätiedot"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.todistuksella-nakyvat-lisatiedot`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop(
                'todistuksellaNäkyvätLisätiedot'
              )}
              view={TodistuksellaNäkyvätLisätiedotView}
              edit={TodistuksellaNäkyvätLisätiedotEdit}
            />
          </KeyValueRow>
        </KeyValueTable>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suorituksenVahvistus}
          testId={päätasonSuoritus.testId}
        />
        <Spacer />
        {päätasonSuoritus.suoritus.osasuoritukset && (
          <>
            <RaisedButton
              data-testid={`suoritukset.${päätasonSuoritus.index}.expand`}
              onClick={(e) => {
                e.preventDefault()
                toggleOsasuorituksetOpenState()
              }}
            >
              {isAnyModalOpen ? t('Sulje kaikki') : t('Avaa kaikki')}
            </RaisedButton>
            <Spacer />
            <OsasuoritusTable
              level={rootLevel}
              openState={osasuorituksetOpenState}
              toggleModal={toggleOsasuorituksetOpenState}
              setOsasuoritusOpen={setOsasuorituksetStateHandler}
              editMode={form.editMode}
              // Path, johon uusi osasuoritus lisätään
              pathWithOsasuoritukset={päätasonSuoritus.path}
              setModal={setModal}
              completed={(rowIndex) => {
                const osasuoritukset =
                  päätasonSuoritus.suoritus.osasuoritukset || []
                const os = osasuoritukset[rowIndex]
                if (!os) {
                  return false
                }
                // TODO: Logiikka siistimmäksi
                if (
                  os.$class ===
                  'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
                ) {
                  return true
                }
                if (!hasArviointi(os)) {
                  return undefined
                }
                return os.arviointi !== undefined && os.arviointi.length > 0
              }}
              rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
                (_os, osasuoritusIndex) => {
                  return osasuoritusToTableRow(
                    // Riveillä on sama taso kuin OsasuoritusTable-komponentilla
                    0,
                    form,
                    päätasonSuoritus.path,
                    päätasonSuoritus.index,
                    osasuoritusIndex,
                    osasuorituksetOpenState,
                    toggleOsasuorituksetOpenState,
                    setOsasuorituksetStateHandler,
                    setModal,
                    allOsasuorituksetOpen
                  )
                }
              )}
              onRemove={(i) => {
                form.updateAt(
                  päätasonSuoritus.path.prop('osasuoritukset').optional(),
                  (osasuoritukset) =>
                    // TODO: TypeScriptin rajoitusten takia tämä on type-castattava erikseen arrayksi.
                    (osasuoritukset as any[]).filter((_, index) => index !== i)
                )
              }}
            />
            <KeyValueTable>
              <KeyValueRow
                label="Yhteensä"
                testId={`vst.suoritukset.${päätasonSuoritus.index}.yhteensa`}
              >
                {/* TODO: Korjaa tyypitys */}
                {/* @ts-expect-error */}
                {päätasonSuoritus.suoritus.osasuoritukset.reduce(
                  (prev: number, curr: any) =>
                    prev + (curr.koulutusmoduuli.laajuus?.arvo || 0),
                  0
                )}{' '}
                {päätasonSuoritus.suoritus.osasuoritukset.length > 0 && (
                  <Trans>
                    {päätasonSuoritus.suoritus.osasuoritukset[0].koulutusmoduuli
                      .laajuus?.yksikkö?.lyhytNimi || ''}
                  </Trans>
                )}
              </KeyValueRow>
            </KeyValueTable>
          </>
        )}
        {form.editMode && (
          <ColumnRow>
            <Column span={{ default: 1, phone: 0 }} />
            <Column span={{ default: 14, small: 10, phone: 24 }}>
              {(isVapaanSivistystyönJotpaKoulutuksenSuoritus(
                päätasonSuoritus.suoritus
              ) ||
                isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
                  päätasonSuoritus.suoritus
                )) && (
                <PaikallinenOsasuoritusSelect
                  tunnisteet={[]}
                  onSelect={createPaikallinenPäätasonOsasuoritus}
                  onRemove={() => console.log('TODO: onRemove')}
                  testId={subTestId(päätasonSuoritus, 'addOsasuoritus')}
                />
              )}
              {isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
                päätasonSuoritus.suoritus
              ) && (
                <KoodistoSelect
                  koodistoUri="vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"
                  addNewText={t('Lisää osasuoritus')}
                  onSelect={(tunniste, isNew) =>
                    console.log('new tunniste', tunniste)
                  }
                  onRemove={() => console.log('TODO: onRemove')}
                  testId={subTestId(päätasonSuoritus, 'addOsasuoritus')}
                />
              )}
              {isVapaanSivistystyönLukutaitokoulutuksenSuoritus(
                päätasonSuoritus.suoritus
              ) && (
                <KoodistoSelect
                  koodistoUri="vstlukutaitokoulutuksenkokonaisuus"
                  addNewText={t('Lisää kokonaisuus')}
                  onSelect={(tunniste, isNew) =>
                    console.log('new tunniste', tunniste)
                  }
                  onRemove={() => console.log('TODO: onRemove')}
                  testId={subTestId(päätasonSuoritus, 'addOsasuoritus')}
                />
              )}
              {isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
                päätasonSuoritus.suoritus
              ) && (
                <KoodistoSelect
                  koodistoUri="vstosaamiskokonaisuus"
                  addNewText={t('Lisää osaamiskokonaisuus')}
                  onSelect={(tunniste, isNew) =>
                    console.log('new tunniste', tunniste)
                  }
                  onRemove={() => console.log('TODO: onRemove')}
                  testId={subTestId(päätasonSuoritus, 'addOsaamiskokonaisuus')}
                />
              )}
            </Column>
          </ColumnRow>
        )}
      </EditorContainer>
    </>
  )
}

const osasuoritusToTableRow = (
  level: number,
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>,
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönPäätasonSuoritus
  >,
  suoritusIndex: number,
  osasuoritusIndex: number,
  // Tilanhallintaa modaalien availuun ja osasuoritusikkunan toimintaan
  osasuorituksetExpandedState: OsasuorituksetExpandedState,
  toggleOsasuoritusOpen: ToggleOsasuoritusOpen,
  setOsasuoritusOpen: SetOsasuoritusOpen,
  setModal: SetModal,
  allOpen: boolean
): OsasuoritusRowData<'Osasuoritus' | 'Laajuus' | 'Arvosana' | 'Taitotaso'> => {
  const osasuoritus = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  const osasuoritusValue = getValue(osasuoritus)(form.state)
  const suoritusValue = getValue(suoritusPath.prop('osasuoritukset'))(
    form.state
  )
  const hasArvioinnit =
    suoritusValue &&
    (suoritusValue as any[]).some((v) => {
      return hasArviointi(v)
    })

  console.log('hasArvioinnit', hasArvioinnit)

  const columns = {
    Osasuoritus: (
      <FormField
        form={form}
        path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
        view={LocalizedTextView}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'nimi'
        )}
      />
    ),
    Laajuus: (
      <FormField
        form={form}
        path={osasuoritus.path('koulutusmoduuli.laajuus')}
        view={LaajuusView}
        edit={LaajuusOpintopisteissäEdit}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'laajuus'
        )}
      />
    ),
    Arvosana: (
      <FormField
        form={form}
        path={osasuoritus.path('arviointi')}
        view={ArvosanaView}
        edit={(arvosanaProps) => {
          if (osasuoritusValue === undefined) {
            return null
          }
          if (!hasArviointi(osasuoritusValue)) {
            return null
          }
          return (
            <ArvosanaEdit
              {...arvosanaProps}
              createArviointi={(arvosana) => {
                return createVstArviointi(osasuoritusValue)(arvosana)
              }}
            />
          )
        }}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'arvosana'
        )}
      />
    ),
    Taitotaso: (
      <FormField
        form={form}
        path={osasuoritus
          .guard(isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus)
          .prop('arviointi')}
        view={TaitotasoView}
        edit={(taitotasoProps) => {
          if (osasuoritusValue === undefined) {
            return null
          }
          return <TaitotasoEdit {...taitotasoProps} />
        }}
        testId={osasuoritusTestId(
          suoritusIndex,
          level,
          osasuoritusIndex,
          'arvosana'
        )}
      />
    )
  }

  const { Osasuoritus, Laajuus, Arvosana, Taitotaso } = columns

  // TODO: Siisti
  const isExpandable =
    osasuoritusValue?.$class !==
    'fi.oph.koski.schema.VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'

  const isLukutaitokoulutus =
    isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      osasuoritusValue
    )

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: isExpandable,
    // TODO: Siisti kolumnilogiikka
    columns: hasArvioinnit
      ? isLukutaitokoulutus
        ? { Osasuoritus, Laajuus, Arvosana, Taitotaso }
        : { Osasuoritus, Laajuus, Arvosana }
      : { Osasuoritus, Laajuus },
    content: (
      <VSTOsasuoritusProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        osasuoritusOpenState={osasuorituksetExpandedState}
        setOpenStateHandler={setOsasuoritusOpen}
        toggleOsasuoritusOpen={toggleOsasuoritusOpen}
        allOpen={allOpen}
        setModal={setModal}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritus}
      />
    )
  }
}

type VSTOsasuoritusPropertiesProps = {
  osasuoritusIndex: number
  level: number
  toggleOsasuoritusOpen: ToggleOsasuoritusOpen
  setOpenStateHandler: SetOsasuoritusOpen
  setModal?: SetModal
  osasuoritusOpenState: OsasuorituksetExpandedState
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<VapaanSivistystyönOpiskeluoikeus, VSTOsasuoritus>
  allOpen: boolean
}

const VSTOsasuoritusProperties: React.FC<VSTOsasuoritusPropertiesProps> = (
  props
) => {
  const osasuoritusArvioinnillaPath = props.osasuoritusPath.guard(hasArviointi)
  const viimeisinArviointiPath = osasuoritusArvioinnillaPath
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  const arvioitu =
    osasuoritus !== undefined &&
    'arviointi' in osasuoritus &&
    osasuoritus?.arviointi !== undefined &&
    osasuoritus?.arviointi.length > 0

  return (
    <div>
      {arvioitu && (
        <OsasuoritusProperty label="Arviointi">
          <OsasuoritusSubproperty label="Arvosana">
            <FormField
              form={props.form}
              path={osasuoritusArvioinnillaPath.prop('arviointi').optional()}
              view={ArvosanaView}
              edit={(arvosanaProps) => {
                if (!hasArviointi(osasuoritus)) {
                  // TODO: Poista, kun vaikuttaa toimivalta
                  return <div>{'Ei arviointia'}</div>
                }
                return (
                  <ArvosanaEdit
                    {...arvosanaProps}
                    createArviointi={(arvosana) => {
                      return createVstArviointi(osasuoritus)(arvosana)
                    }}
                  />
                )
              }}
              testId={`vst.arvosana`}
            />
          </OsasuoritusSubproperty>
          <OsasuoritusSubproperty rowNumber={1} label="Päivämäärä">
            <FormField
              form={props.form}
              // TODO: Korjaa tyypitys
              // @ts-expect-error
              path={viimeisinArviointiPath.prop('päivä')}
              view={DateView}
              edit={DateEdit}
              testId={`vst.arvostelunPvm`}
            />
          </OsasuoritusSubproperty>
        </OsasuoritusProperty>
      )}
      {hasOsasuoritustenOsasuorituksia(osasuoritus) && (
        <OsasuoritusTable
          level={props.level + 1}
          // Path, johon uusi osasuoritus lisätään
          pathWithOsasuoritukset={props.osasuoritusPath}
          openState={props.osasuoritusOpenState}
          setOsasuoritusOpen={props.setOpenStateHandler}
          toggleModal={props.toggleOsasuoritusOpen}
          setModal={props.setModal}
          editMode={props.form.editMode}
          onRemove={(i) => {
            props.form.updateAt(
              props.osasuoritusPath
                .guard(hasOsasuoritustenOsasuorituksia)
                .prop('osasuoritukset')
                .optional(),
              // TODO: TypeScript-kääntäjän rajoitusten takia pitää olla näin.
              (os) => (os as any[]).filter((_, index) => i !== index)
            )
          }}
          rows={(osasuoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) => {
              return osasuoritusToTableRow(
                props.level + 1,
                props.form,
                // @ts-expect-error
                props.osasuoritusPath,
                props.osasuoritusIndex,
                osasuoritusIndex,
                props.osasuoritusOpenState,
                props.toggleOsasuoritusOpen,
                props.setOpenStateHandler,
                props.setModal,
                props.allOpen
              )
            }
          )}
        />
      )}
    </div>
  )
}
