import bem from "bem-ts"
import React, { useMemo, useState } from "react"
import { createKuntailmoitus } from "../../api/api"
import { useApiMethod, useOnApiSuccess } from "../../api/apiHooks"
import { isError, isLoading } from "../../api/apiUtils"
import { RaisedButton } from "../../components/buttons/RaisedButton"
import { LabeledCheckbox } from "../../components/forms/Checkbox"
import {
  displayOrd,
  Dropdown,
  DropdownOption,
  koodistoToOptions,
} from "../../components/forms/Dropdown"
import { TextField } from "../../components/forms/TextField"
import {
  CaretDownIcon,
  CaretUpIcon,
  SuccessCircleIcon,
  WarningIcon,
} from "../../components/icons/Icon"
import { Error } from "../../components/typography/error"
import { SecondaryHeading } from "../../components/typography/headings"
import { getLocalized, T, t } from "../../i18n/i18n"
import { HenkilöTiedot } from "../../state/apitypes/henkilo"
import { Kieli, Maa } from "../../state/apitypes/koodistot"
import {
  KuntailmoituksenTekijäLaajatTiedot,
  KuntailmoitusKunta,
  KuntailmoitusLaajatTiedot,
} from "../../state/apitypes/kuntailmoitus"
import {
  OppijanPohjatiedot,
  PohjatietoYhteystieto,
} from "../../state/apitypes/kuntailmoituspohjatiedot"
import {
  OpiskeluoikeusSuppeatTiedot,
  valvottavatOpiskeluoikeudet,
} from "../../state/apitypes/opiskeluoikeus"
import {
  lisätietoMatches,
  OpiskeluoikeusLisätiedot,
} from "../../state/apitypes/oppija"
import { trimOrganisaatio } from "../../state/apitypes/organisaatiot"
import {
  isAlkuperäHakemukselta,
  isAlkuperäRekisteristä,
} from "../../state/apitypes/yhteystiedot"
import { Oid } from "../../state/common"
import { expectNonEmptyString } from "../../state/formValidators"
import { FormValidators, useFormState } from "../../state/useFormState"
import { nonEmptyEvery, nonNull } from "../../utils/arrays"
import { removeFalsyValues } from "../../utils/objects"
import { plainComponent } from "../../utils/plaincomponent"
import "./IlmoitusForm.less"

const b = bem("ilmoitusform")

const koodiarvoFinland = "246"

export type IlmoitusFormValues = {
  asuinkunta?: string
  yhteydenottokieli?: string
  maa?: string
  postinumero: string
  postitoimipaikka: string
  lähiosoite: string
  puhelinnumero: string
  email: string
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: boolean
}

const initialValues: IlmoitusFormValues = {
  asuinkunta: undefined,
  yhteydenottokieli: "FI",
  maa: koodiarvoFinland,
  postinumero: "",
  postitoimipaikka: "",
  lähiosoite: "",
  puhelinnumero: "",
  email: "",
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: false,
}

const validators: FormValidators<IlmoitusFormValues> = {
  asuinkunta: [expectNonEmptyString("ilmoituslomake__pakollinen_tieto")],
  yhteydenottokieli: [],
  maa: [],
  postitoimipaikka: [],
  postinumero: [],
  lähiosoite: [],
  puhelinnumero: [],
  email: [],
  hakenutOpiskelemaanYhteyshakujenUlkopuolella: [],
}

const toKuntailmoitusLaajatTiedot = (
  form: IlmoitusFormValues,
  tekijä: KuntailmoituksenTekijäLaajatTiedot,
  kunnat: KuntailmoitusKunta[]
): KuntailmoitusLaajatTiedot | null => {
  const kunta = kunnat.find((k) => k.oid === form.asuinkunta)

  return nonNull(kunta)
    ? {
        kunta,
        tekijä: {
          organisaatio: trimOrganisaatio(tekijä.organisaatio),
          henkilö: tekijä.henkilö,
        },
        yhteydenottokieli: nonNull(form.yhteydenottokieli)
          ? {
              koodistoUri: "kieli",
              koodiarvo: form.yhteydenottokieli as Kieli["koodiarvo"],
            }
          : undefined,
        oppijanYhteystiedot: removeFalsyValues({
          puhelinnumero: form.puhelinnumero,
          email: form.email,
          lähiosoite: form.lähiosoite,
          postinumero: form.postinumero,
          postitoimipaikka: form.postitoimipaikka,
          maa: nonNull(form.maa)
            ? {
                koodistoUri: "maatjavaltiot2",
                koodiarvo: form.maa,
              }
            : undefined,
        }),
        hakenutMuualle: form.hakenutOpiskelemaanYhteyshakujenUlkopuolella,
      }
    : null
}

export type IlmoitusFormProps = {
  oppijaTiedot: HenkilöTiedot
  opiskeluoikeudet?: OpiskeluoikeusSuppeatTiedot[]
  lisätiedot?: OpiskeluoikeusLisätiedot[]
  pohjatiedot: OppijanPohjatiedot
  kunnat: Array<KuntailmoitusKunta>
  maat: Array<Maa>
  kielet: Array<Kieli>
  tekijä: KuntailmoituksenTekijäLaajatTiedot
  formIndex: number
  numberOfForms: number
  onSubmit?: (values: IlmoitusFormValues) => void
}

export const IlmoitusForm = (props: IlmoitusFormProps) => {
  const form = useFormState({
    initialValues: {
      ...initialValues,
      yhteydenottokieli: props.pohjatiedot.yhteydenottokieli?.koodiarvo,
      hakenutOpiskelemaanYhteyshakujenUlkopuolella: defaultMuuHakuValue(
        props.oppijaTiedot.oid,
        props.tekijä.organisaatio.oid,
        props.lisätiedot,
        props.opiskeluoikeudet
      ),
    },
    validators,
  })
  const [isOpen, setOpen] = useState(true)
  const [isSubmitted, setSubmitted] = useState(false)
  const send = useApiMethod(createKuntailmoitus)

  const submit = form.submitCallback(async (formData) => {
    const kuntailmoitus = toKuntailmoitusLaajatTiedot(
      formData,
      props.tekijä,
      props.kunnat
    )

    if (kuntailmoitus) {
      await send.call(props.oppijaTiedot.oid, kuntailmoitus)
    }
  })

  useOnApiSuccess(send, () => {
    setSubmitted(true)
    if (props.onSubmit) {
      form.submitCallback(props.onSubmit)()
    }
  })

  const kunnat = props.kunnat
  const kuntaOptions = useMemo(() => kunnatToOptions(kunnat), [kunnat])

  const errorMessages = [
    form.allFieldsValidated && !form.isValid
      ? t("ilmoituslomake__täytä_pakolliset_tiedot")
      : null,
    ...(isError(send) ? send.errors.map((e) => e.message) : []),
  ].filter(nonNull)

  return (
    <IlmoitusFormFrame>
      <IlmoitusHeader
        henkilö={props.oppijaTiedot}
        pohjatiedot={props.pohjatiedot}
        formIndex={props.formIndex}
        numberOfForms={props.numberOfForms}
        isOpen={isOpen}
        isSubmitted={isSubmitted}
        onClick={() => setOpen(!isOpen)}
      />
      {isOpen && !isSubmitted ? (
        <IlmoitusBody>
          <IlmoitusPrefillSelector
            pohjatiedot={props.pohjatiedot}
            onSelect={form.patch}
          />
          {props.pohjatiedot.turvakielto && <TurvakieltoWarning />}
          <Dropdown
            label={t("ilmoituslomake__asuinkunta")}
            required
            options={kuntaOptions}
            sort={displayOrd}
            testId="asuinkunta"
            {...form.fieldProps("asuinkunta")}
          />
          <Dropdown
            label={t("ilmoituslomake__yhteydenottokieli")}
            options={koodistoToOptions(props.kielet)}
            testId="yhteydenottokieli"
            {...form.fieldProps("yhteydenottokieli")}
          />
          <SecondaryHeading className={b("muutyhteystiedototsikko")}>
            <T id="ilmoituslomake__muut_yhteystiedot" />
          </SecondaryHeading>
          <Dropdown
            label={t("ilmoituslomake__maa")}
            options={koodistoToOptions(props.maat)}
            testId="maa"
            {...form.fieldProps("maa")}
          />
          <TextField
            label={t("ilmoituslomake__postinumero")}
            testId="postinumero"
            {...form.fieldProps("postinumero")}
          />
          <TextField
            label={t("ilmoituslomake__postitoimipaikka")}
            testId="postitoimipaikka"
            {...form.fieldProps("postitoimipaikka")}
          />
          <TextField
            label={t("ilmoituslomake__katuosoite")}
            testId="katuosoite"
            {...form.fieldProps("lähiosoite")}
          />
          <TextField
            label={t("ilmoituslomake__puhelinnumero")}
            testId="puhelinnumero"
            {...form.fieldProps("puhelinnumero")}
          />
          <TextField
            label={t("ilmoituslomake__sähköposti")}
            testId="sähköposti"
            {...form.fieldProps("email")}
          />
          <LabeledCheckbox
            label={t(
              "ilmoituslomake__hakenut_opiskelemaan_yhteishakujen_ulkopuolella"
            )}
            {...form.fieldProps("hakenutOpiskelemaanYhteyshakujenUlkopuolella")}
          />

          {errorMessages.map((error, index) => (
            <Error key={index}>{error}</Error>
          ))}

          <RaisedButton
            className={b("submit")}
            disabled={form.isValid ? false : "byLook"}
            onClick={() => {
              if (!isLoading(send)) {
                if (form.isValid) {
                  submit()
                } else {
                  form.validateAll()
                }
              }
            }}
          >
            <T id="ilmoituslomake__ilmoita_asuinkunnalle" />
          </RaisedButton>
        </IlmoitusBody>
      ) : null}
    </IlmoitusFormFrame>
  )
}

const defaultMuuHakuValue = (
  oppijaOid: Oid,
  organisaatioOid: Oid,
  lisätiedot?: OpiskeluoikeusLisätiedot[],
  opiskeluoikeudet?: OpiskeluoikeusSuppeatTiedot[]
): boolean => {
  const definedLisätiedot = lisätiedot || []
  const definedOpiskeluoikeudet = opiskeluoikeudet || []
  const matchesOpiskeluoikeus = (opiskeluoikeus: OpiskeluoikeusSuppeatTiedot) =>
    lisätietoMatches(
      oppijaOid,
      opiskeluoikeus.oid,
      opiskeluoikeus.oppilaitos.oid
    )
  return nonEmptyEvery(
    valvottavatOpiskeluoikeudet(organisaatioOid, definedOpiskeluoikeudet),
    (oo) => definedLisätiedot.find(matchesOpiskeluoikeus(oo))?.muuHaku === true
  )
}

export type IlmoitusHeaderProps = {
  henkilö: HenkilöTiedot
  pohjatiedot: OppijanPohjatiedot
  formIndex: number
  numberOfForms: number
  isOpen: boolean
  isSubmitted: boolean
  onClick: () => void
}

const IlmoitusHeader = (props: IlmoitusHeaderProps) => (
  <IlmoitusHeaderFrame onClick={props.onClick}>
    <IlmoitusTitle>
      <IlmoitusTitleIndex>
        {props.formIndex + 1}/{props.numberOfForms}
      </IlmoitusTitleIndex>
      <IlmoitusTitleTexts>
        <IlmoitusTitleText>
          {props.henkilö.sukunimi} {props.henkilö.etunimet}
          {props.pohjatiedot.hetu && ` (${props.pohjatiedot.hetu})`}
        </IlmoitusTitleText>
        <IlmoitusSubtitle>Oppija {props.henkilö.oid}</IlmoitusSubtitle>
      </IlmoitusTitleTexts>
      {!props.isSubmitted ? (
        <IlmoitusTitleCaret>
          {props.isOpen ? <CaretDownIcon /> : <CaretUpIcon />}
        </IlmoitusTitleCaret>
      ) : null}
    </IlmoitusTitle>
    {props.isSubmitted ? (
      <IlmoitusSubmitted data-testid="submitted">
        <SuccessCircleIcon inline color="white" />
        <T id="ilmoituslomake__ilmoitus_lähetetty" />
      </IlmoitusSubmitted>
    ) : null}
  </IlmoitusHeaderFrame>
)

const IlmoitusFormFrame = plainComponent("div", b("frame"))
const IlmoitusHeaderFrame = plainComponent("header", b("header"))
const IlmoitusTitle = plainComponent("h3", b("title"))
const IlmoitusTitleIndex = plainComponent("div", b("titleindex"))
const IlmoitusTitleTexts = plainComponent("div", b("titletexts"))
const IlmoitusTitleText = plainComponent("div", b("titletext"))
const IlmoitusTitleCaret = plainComponent("div", b("titlecaret"))
const IlmoitusSubtitle = plainComponent("h4", b("subtitle"))
const IlmoitusSubmitted = plainComponent("div", b("submitted"))
const IlmoitusBody = plainComponent("div", b("body"))

type IlmoitusPrefillSelectorProps = {
  pohjatiedot: OppijanPohjatiedot
  onSelect: (values: Partial<IlmoitusFormValues>) => void
}

const IlmoitusPrefillSelector = (props: IlmoitusPrefillSelectorProps) =>
  props.pohjatiedot.yhteystiedot.length > 0 ? (
    <div className={b("prefill")}>
      <T id="ilmoituslomake__esitäytä_yhteystiedoilla" />
      <ul className={b("prefilllist")}>
        {props.pohjatiedot.yhteystiedot.map((yhteystieto, index) => (
          <li
            key={index}
            className={b("prefillitem")}
            onClick={() =>
              props.onSelect({
                ...yhteystieto.yhteystiedot,
                asuinkunta: yhteystieto.kunta?.oid,
                maa: yhteystieto.yhteystiedot.maa?.koodiarvo,
              })
            }
          >
            {index + 1}) {yhteystiedonNimi(yhteystieto)}
          </li>
        ))}
      </ul>
    </div>
  ) : null

const yhteystiedonNimi = (yhteystieto: PohjatietoYhteystieto): string => {
  const alkuperä = yhteystieto.yhteystietojenAlkuperä
  if (isAlkuperäRekisteristä(alkuperä)) {
    return getLocalized(alkuperä.alkuperä.nimi) || "?"
  }
  if (isAlkuperäHakemukselta(alkuperä)) {
    return getLocalized(alkuperä.hakuNimi) || "?"
  }
  return "?"
}

const TurvakieltoWarning = () => (
  <div className={b("turvakielto")}>
    <WarningIcon />
    <div className={b("turvakieltotext")}>
      <T id="ilmoituslomake__turvakielto_ohje" />
    </div>
  </div>
)

const kunnatToOptions = (kunnat: KuntailmoitusKunta[]): DropdownOption<Oid>[] =>
  kunnat.map((kunta) => ({
    value: kunta.oid,
    display: getLocalized(kunta.kotipaikka?.nimi || kunta.nimi) || kunta.oid,
  }))
