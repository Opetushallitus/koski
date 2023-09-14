import React, { useCallback, useMemo } from 'react'
import { useSchema } from '../appstate/constraints'
import { append } from '../util/fp/arrays'
import { KansalainenOnly } from '../components-v2/access/KansalainenOnly'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { FormOptic, getValue, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OpiskeluoikeusTitle } from '../components-v2/opiskeluoikeus/OpiskeluoikeusTitle'
import { t } from '../i18n/i18n'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { Trans } from '../components-v2/texts/Trans'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { FormField } from '../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import {
  Koodistokoodiviite,
  isKoodistokoodiviite
} from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../components-v2/opiskeluoikeus/SuorituskieliField'
import {
  TodistuksellaNäkyvätLisätiedotEdit,
  TodistuksellaNäkyvätLisätiedotView
} from '../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import {
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import {
  PaikallinenKoodi,
  isPaikallinenKoodi
} from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { isVapaanSivistystyönJotpaKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'
import {
  ToimipisteEdit,
  ToimipisteView
} from '../components-v2/opiskeluoikeus/OpiskeluoikeudenToimipiste'
import {
  isVSTOsasuoritusArvioinnilla,
  hasOpintokokonaisuus,
  isLaajuuksellinenVSTKoulutusmoduuli,
  isPerusteellinenVSTKoulutusmoduuli,
  narrowKoodistokoodiviite,
  VSTOsasuoritus
} from './typeguards'
import {
  createVstOpiskeluoikeusjakso,
  defaultLaajuusOpintopisteissa,
  resolveDiaarinumero,
  resolveOpiskeluoikeudenTilaClass,
  vstNimi,
  vstSuorituksenNimi
} from './resolvers'
import { VSTLisatiedot } from './VSTLisatiedot'
import {
  OpintokokonaisuusEdit,
  OpintokokonaisuusView
} from '../components-v2/opiskeluoikeus/OpintokokonaisuusField'
import {
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
import { VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
import {
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import {
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenKoulutusmoduuli2022'
import {
  PerusteEdit,
  PerusteView
} from '../components-v2/opiskeluoikeus/PerusteField'
import {
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönLukutaidonKokonaisuus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaidonKokonaisuus'
import {
  AddNewVSTOsasuoritusView,
  osasuoritusToTableRow
} from './VSTOsasuoritusProperties'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus'
import { VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsasuoritus'
import {
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus,
  OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOsaamiskokonaisuus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
import { VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenAlaosasuoritus'
import { VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaamisenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { viimeisinArviointi } from '../util/schema'
import { isObjectConstraint } from '../types/fi/oph/koski/typemodel/ObjectConstraint'
import { isOptionalConstraint } from '../types/fi/oph/koski/typemodel/OptionalConstraint'
import { Infobox } from '../components/Infobox'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { MuuallaSuoritetutVapaanSivistystyönOpinnot } from '../types/fi/oph/koski/schema/MuuallaSuoritetutVapaanSivistystyonOpinnot'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaisetSuuntautumisopinnot'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { VapaanSivistystyönVapaatavoitteinenKoulutus } from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteinenKoulutus'
import { useOsasuorituksetExpand } from './../osasuoritus/hooks'

type VSTEditorProps =
  AdaptedOpiskeluoikeusEditorProps<VapaanSivistystyönOpiskeluoikeus>

export const VSTEditor: React.FC<VSTEditorProps> = (props) => {
  // Opiskeluoikeus
  const opiskeluoikeusSchema = useSchema(
    VapaanSivistystyönOpiskeluoikeus.className
  )
  const vapaanSivistystyönVapaatavoitteinenKoulutusSchema = useSchema(
    VapaanSivistystyönVapaatavoitteinenKoulutus.className
  )
  const { infoLinkTitle, infoLinkUrl, infoDescription } = useMemo(() => {
    if (vapaanSivistystyönVapaatavoitteinenKoulutusSchema === null) {
      return {}
    }
    if (
      isObjectConstraint(vapaanSivistystyönVapaatavoitteinenKoulutusSchema) &&
      isOptionalConstraint(
        vapaanSivistystyönVapaatavoitteinenKoulutusSchema.properties
          .opintokokonaisuus
      )
    ) {
      const {
        infoLinkTitle: infoTitle,
        infoLinkUrl: infoUrl,
        infoDescription: infoDesc
      } = vapaanSivistystyönVapaatavoitteinenKoulutusSchema.properties
        .opintokokonaisuus
      return {
        infoLinkTitle: infoTitle,
        infoLinkUrl: infoUrl,
        infoDescription: infoDesc
      }
    }
    return {}
  }, [vapaanSivistystyönVapaatavoitteinenKoulutusSchema])
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)

  // Oppilaitos
  const organisaatio =
    props.opiskeluoikeus.oppilaitos || props.opiskeluoikeus.koulutustoimija

  // Päätason suoritus
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  const appendOsasuoritus = useCallback(
    (
      // TODO: Path-tyypitys
      path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
      newOsasuoritus: VSTOsasuoritus
    ) => {
      if (form.editMode) {
        form.updateAt(path, (osasuoritus) => ({
          ...osasuoritus,
          osasuoritukset: append(newOsasuoritus)(osasuoritus.osasuoritukset)
        }))
      }
    },
    [form]
  )

  const createOsasuoritusV2 = useCallback(
    (suoritusPath: any, osasuoritus: VSTOsasuoritus) => {
      appendOsasuoritus(suoritusPath, osasuoritus)
    },
    [appendOsasuoritus]
  )

  const createOsasuoritus = useCallback(
    (
      suoritusPath: any,
      osasuoritusTunniste: PaikallinenKoodi | Koodistokoodiviite
    ) => {
      if (!form.editMode) {
        return void 0
      }
      const suoritusAtPath = getValue(suoritusPath)(form.state) as any
      if (suoritusAtPath === undefined) {
        return void 0
      }
      if (
        isVapaanSivistystyönJotpaKoulutuksenSuoritus(suoritusAtPath) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
              tunniste: osasuoritusTunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else if (
        isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
          suoritusAtPath
        ) &&
        isKoodistokoodiviite(osasuoritusTunniste)
      ) {
        if (
          narrowKoodistokoodiviite(
            osasuoritusTunniste,
            'vstkoto2022kokonaisuus',
            'kielijaviestintaosaaminen'
          )
        ) {
          appendOsasuoritus(
            suoritusPath,
            VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli({
                  tunniste: osasuoritusTunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                })
            })
          )
        } else if (
          narrowKoodistokoodiviite(
            osasuoritusTunniste,
            'vstkoto2022kokonaisuus',
            'ohjaus'
          )
        ) {
          appendOsasuoritus(
            suoritusPath,
            VSTKotoutumiskoulutuksenOhjauksenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022({
                  tunniste: osasuoritusTunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                })
            })
          )
        } else if (
          narrowKoodistokoodiviite(
            osasuoritusTunniste,
            'vstkoto2022kokonaisuus',
            'valinnaisetopinnot'
          )
        ) {
          appendOsasuoritus(
            suoritusPath,
            VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022(
                  {
                    tunniste: osasuoritusTunniste,
                    laajuus: defaultLaajuusOpintopisteissa
                  }
                )
            })
          )
        } else if (
          narrowKoodistokoodiviite(
            osasuoritusTunniste,
            'vstkoto2022kokonaisuus',
            'yhteiskuntajatyoelamaosaaminen'
          )
        ) {
          appendOsasuoritus(
            suoritusPath,
            VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022({
              koulutusmoduuli:
                VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022(
                  {
                    tunniste: osasuoritusTunniste,
                    laajuus: defaultLaajuusOpintopisteissa
                  }
                )
            })
          )
        }
      } else if (
        isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          suoritusAtPath
        ) &&
        narrowKoodistokoodiviite(osasuoritusTunniste, 'vstosaamiskokonaisuus')
      ) {
        appendOsasuoritus(
          suoritusPath,
          OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
            {
              koulutusmoduuli:
                OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus({
                  tunniste: osasuoritusTunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                })
            }
          )
        )
      } else if (
        isVapaanSivistystyönLukutaitokoulutuksenSuoritus(suoritusAtPath) &&
        isKoodistokoodiviite(osasuoritusTunniste) &&
        narrowKoodistokoodiviite(
          osasuoritusTunniste,
          'vstlukutaitokoulutuksenkokonaisuus'
        )
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
            koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
              tunniste: osasuoritusTunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else if (
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli:
              VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
                tunniste: osasuoritusTunniste,
                laajuus: defaultLaajuusOpintopisteissa,
                kuvaus: Finnish({ fi: '' })
              })
          })
        )
      } else if (
        isVapaanSivistystyönLukutaitokoulutuksenSuoritus(suoritusAtPath) &&
        narrowKoodistokoodiviite(
          osasuoritusTunniste,
          'vstlukutaitokoulutuksenkokonaisuus'
        )
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
            koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
              tunniste: osasuoritusTunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else if (
        isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus({
            koulutusmoduuli:
              VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
                {
                  kuvaus: Finnish({ fi: '' }),
                  tunniste: osasuoritusTunniste
                }
              )
          })
        )
      } else if (
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli:
              VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
                kuvaus: Finnish({ fi: '' }),
                laajuus: defaultLaajuusOpintopisteissa,
                tunniste: osasuoritusTunniste
              })
          })
        )
      } else if (
        isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
            {
              koulutusmoduuli:
                OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus({
                  kuvaus: Finnish({ fi: '' }),
                  laajuus: defaultLaajuusOpintopisteissa,
                  tunniste: osasuoritusTunniste
                })
            }
          )
        )
      } else if (
        isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
            {
              koulutusmoduuli:
                VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
                  {
                    kuvaus: Finnish({ fi: '' }),
                    tunniste: osasuoritusTunniste
                  }
                )
            }
          )
        )
      } else if (
        isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
          suoritusAtPath
        ) &&
        narrowKoodistokoodiviite(
          osasuoritusTunniste,
          'vstkoto2022yhteiskuntajatyoosaamiskoulutus'
        )
      ) {
        appendOsasuoritus(
          suoritusPath,
          VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus({
            koulutusmoduuli:
              VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022(
                {
                  tunniste: osasuoritusTunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                }
              )
          })
        )
      } else if (
        isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
          suoritusAtPath
        ) &&
        narrowKoodistokoodiviite(
          osasuoritusTunniste,
          'vstkoto2022kielijaviestintakoulutus'
        )
      ) {
        appendOsasuoritus(
          suoritusPath,
          VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus({
            koulutusmoduuli:
              VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
                {
                  tunniste: osasuoritusTunniste,
                  laajuus: defaultLaajuusOpintopisteissa
                }
              )
          })
        )
      } else if (
        isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
          suoritusAtPath
        ) &&
        isPaikallinenKoodi(osasuoritusTunniste)
      ) {
        appendOsasuoritus(
          suoritusPath,
          VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus({
            koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus({
              tunniste: osasuoritusTunniste,
              laajuus: defaultLaajuusOpintopisteissa
            })
          })
        )
      } else if (
        isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
          suoritusAtPath
        ) &&
        isKoodistokoodiviite(osasuoritusTunniste) &&
        narrowKoodistokoodiviite(
          osasuoritusTunniste,
          'vstmuutopinnot',
          'valinnaisetsuuntautumisopinnot'
        )
      ) {
        appendOsasuoritus(
          suoritusPath,
          OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
            {
              tyyppi: Koodistokoodiviite({
                koodiarvo: 'vstvalinnainensuuntautuminen',
                koodistoUri: 'suorituksentyyppi'
              }),
              koulutusmoduuli:
                OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
                  {
                    tunniste: osasuoritusTunniste
                  }
                )
            }
          )
        )
      } else if (
        isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
          suoritusAtPath
        )
      ) {
        if (isPaikallinenKoodi(osasuoritusTunniste)) {
          appendOsasuoritus(
            suoritusPath,
            OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
              {
                koulutusmoduuli:
                  OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
                    {
                      tunniste: osasuoritusTunniste,
                      kuvaus: Finnish({ fi: '' }),
                      laajuus: defaultLaajuusOpintopisteissa
                    }
                  )
              }
            )
          )
        } else if (
          isKoodistokoodiviite(osasuoritusTunniste) &&
          narrowKoodistokoodiviite(
            osasuoritusTunniste,
            'vstmuuallasuoritetutopinnot'
          )
        ) {
          appendOsasuoritus(
            suoritusPath,
            MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
              {
                koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot({
                  kuvaus: Finnish({ fi: '' }),
                  laajuus: defaultLaajuusOpintopisteissa,
                  tunniste: osasuoritusTunniste
                })
              }
            )
          )
        } else {
          throw new Error('Unexpected branch')
        }
      } else if (
        isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
          suoritusAtPath
        )
      ) {
        console.log(osasuoritusTunniste)
      } else {
        throw new Error(
          `Not yet implemented for ${suoritusAtPath?.$class}, ${osasuoritusTunniste?.$class}`
        )
      }
    },
    [appendOsasuoritus, form.editMode, form.state]
  )

  const rootLevel = 0

  const {
    osasuorituksetOpenState,
    rootLevelOsasuoritusOpen,
    closeAllOsasuoritukset,
    openAllOsasuoritukset,
    setOsasuorituksetStateHandler
  } = useOsasuorituksetExpand(päätasonSuoritus)

  const suorituksenVahvistus = useMemo(() => {
    if (päätasonSuoritus.suoritus.osasuoritukset === undefined) {
      return false
    }
    const kaikkiArvioinnit = päätasonSuoritus.suoritus.osasuoritukset.flatMap(
      (osasuoritus) => {
        if (isVSTOsasuoritusArvioinnilla(osasuoritus)) {
          if ('arviointi' in osasuoritus) {
            return viimeisinArviointi(osasuoritus.arviointi as any)
          } else {
            return undefined
          }
        } else {
          return []
        }
      }
    )
    return !kaikkiArvioinnit.every((a) => a !== undefined)
  }, [päätasonSuoritus.suoritus.osasuoritukset])

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
        createOpiskeluoikeusjakso={createVstOpiskeluoikeusjakso(
          päätasonSuoritus
        )}
        opiskeluoikeusJaksoClassName={resolveOpiskeluoikeudenTilaClass(
          päätasonSuoritus
        )}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={setPäätasonSuoritus}
        testId="vst-editor-container"
      >
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
                onChangeToimipiste: (data: any) => {
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
                editProps={{
                  diaariNumero: resolveDiaarinumero(
                    päätasonSuoritus.suoritus.koulutusmoduuli
                  )
                }}
              />
            </KeyValueRow>
          )}
          {hasOpintokokonaisuus(päätasonSuoritus.suoritus) && (
            <>
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
              {infoLinkTitle && infoLinkUrl && infoDescription && (
                <Infobox>
                  <>
                    {t(`infoDescription:${infoDescription}`)}
                    <br />
                    <a
                      href={t(`infoLinkUrl:${infoLinkUrl}`)}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {t(`infoLinkTitle:${infoLinkTitle}`)}
                    </a>
                  </>
                </Infobox>
              )}
            </>
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
                  createLaajuus: (arvo: number) =>
                    LaajuusOpintopisteissä({
                      arvo,
                      yksikkö: Koodistokoodiviite({
                        koodistoUri: 'opintojenlaajuusyksikko',
                        koodiarvo: '2'
                      })
                    })
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
          <RaisedButton
            data-testid={`suoritukset.${päätasonSuoritus.index}.expand`}
            onClick={(e) => {
              e.preventDefault()
              if (rootLevelOsasuoritusOpen) {
                closeAllOsasuoritukset()
              } else {
                openAllOsasuoritukset()
              }
            }}
          >
            {rootLevelOsasuoritusOpen ? t('Sulje kaikki') : t('Avaa kaikki')}
          </RaisedButton>
        )}
        <Spacer />
        <OsasuoritusTable
          editMode={form.editMode}
          level={rootLevel}
          openState={osasuorituksetOpenState}
          setOsasuoritusOpen={setOsasuorituksetStateHandler}
          addNewOsasuoritusView={AddNewVSTOsasuoritusView}
          addNewOsasuoritusViewProps={{
            form,
            createOsasuoritus,
            createOsasuoritusV2,
            level: rootLevel,
            // Polku, johon uusi osasuoritus lisätään. Polun tulee sisältää "osasuoritukset"-property.
            // @ts-expect-error Korjaa tyyppi
            pathWithOsasuoritukset: päätasonSuoritus.path
          }}
          completed={(rowIndex) => {
            const osasuoritus = (päätasonSuoritus.suoritus.osasuoritukset ||
              [])[rowIndex]
            if (!osasuoritus) {
              return false
            }
            if (
              osasuoritus.$class ===
              VSTKotoutumiskoulutuksenOhjauksenSuoritus2022.className
            ) {
              return true
            }
            if (!isVSTOsasuoritusArvioinnilla(osasuoritus)) {
              // Palauttamalla undefined ei näytetä kesken tai valmistunut -merkkiä
              return undefined
            }
            return (
              osasuoritus.arviointi !== undefined &&
              osasuoritus.arviointi.length > 0
            )
          }}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                level: rootLevel,
                form,
                allOsasuorituksetOpen: rootLevelOsasuoritusOpen,
                createOsasuoritus: createOsasuoritus,
                createOsasuoritusV2: createOsasuoritusV2,
                osasuorituksetExpandedState: osasuorituksetOpenState,
                osasuoritusIndex,
                setOsasuoritusOpen: setOsasuorituksetStateHandler,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path
              })
          )}
          onRemove={(i) => {
            form.updateAt(
              päätasonSuoritus.path.prop('osasuoritukset').optional(),
              (osasuoritukset) =>
                (osasuoritukset as any[]).filter((_, index) => index !== i)
            )
          }}
        />
        <KeyValueTable>
          <KeyValueRow
            label="Yhteensä"
            testId={`vst.suoritukset.${päätasonSuoritus.index}.yhteensa`}
          >
            {(
              (päätasonSuoritus.suoritus.osasuoritukset || []) as Array<
                | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus
                | VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022
                | OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
                | VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
                | VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
                | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
              >
            ).reduce(
              (prev: number, curr) =>
                prev + (curr.koulutusmoduuli.laajuus?.arvo || 0),
              0
            )}{' '}
            {päätasonSuoritus.suoritus.osasuoritukset !== undefined &&
              päätasonSuoritus.suoritus.osasuoritukset.length > 0 && (
                <Trans>
                  {päätasonSuoritus.suoritus.osasuoritukset[0].koulutusmoduuli
                    .laajuus?.yksikkö?.lyhytNimi || ''}
                </Trans>
              )}
          </KeyValueRow>
        </KeyValueTable>
      </EditorContainer>
    </>
  )
}
