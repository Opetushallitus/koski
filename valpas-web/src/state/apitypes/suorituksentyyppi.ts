import { getLocalizedMaybe, t } from "../../i18n/i18n"
import { KoodistoKoodiviite } from "./koodistot"

export type Suorituksentyyppi = KoodistoKoodiviite<"suorituksentyyppi", string>

export const suorituksenTyyppiToKoulutustyyppi = (
  tyyppi: Suorituksentyyppi,
): string => {
  if (tyyppi.koodiarvo === "valma") {
    return t("koulutustyyppi_valma")
  } else if (tyyppi.koodiarvo === "telma") {
    return t("koulutustyyppi_telma")
  } else if (tyyppi.koodiarvo.startsWith("vst")) {
    return t("koulutustyyppi_vst")
  } else if (
    tyyppi.koodiarvo.startsWith("ib") ||
    tyyppi.koodiarvo.startsWith("preib")
  ) {
    return t("koulutustyyppi_ib")
  } else if (tyyppi.koodiarvo.startsWith("internationalschool")) {
    return t("koulutustyyppi_internationalschool")
  } else if (tyyppi.koodiarvo.startsWith("europeanschoolofhelsinki")) {
    return t("koulutustyyppi_europeanschool")
  } else if (tyyppi.koodiarvo.startsWith("dia")) {
    return t("koulutustyyppi_dia")
  } else if (tyyppi.koodiarvo === "perusopetuksenvuosiluokka") {
    return t("koulutustyyppi_perusopetus")
  } else if (tyyppi.koodiarvo.startsWith("aikuistenperusopetuksen")) {
    return t("koulutustyyppi_aikuistenperusopetus")
  } else if (tyyppi.koodiarvo === "tuvakoulutuksensuoritus") {
    return t("koulutustyyppi_tuva")
  } else {
    return getLocalizedMaybe(tyyppi.nimi) || tyyppi.koodiarvo
  }
}
