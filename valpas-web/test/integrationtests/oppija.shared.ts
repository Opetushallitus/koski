import {
  contentEventuallyEquals,
  expectElementNotVisible,
} from "../integrationtests-env/browser/content"

const cardBodyEquals = (id: string, innerSelector?: string) => (
  expected: string
) =>
  contentEventuallyEquals(
    `#${id} .card__body ${innerSelector || ""}`.trim(),
    expected
  )
export const oppivelvollisuustiedotEquals = cardBodyEquals(
  "oppivelvollisuustiedot",
  ".infotable"
)
export const opiskeluhistoriaEquals = cardBodyEquals("opiskeluhistoria")
export const hautEquals = cardBodyEquals("haut")
export const ilmoitetutYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#ilmoitetut-yhteystiedot", expected)
export const virallisetYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#viralliset-yhteystiedot", expected)
export const turvakieltoVaroitusEquals = (expected: string) =>
  contentEventuallyEquals("#turvakielto-varoitus", expected)
export const turvakieltoVaroitusNotVisible = () =>
  expectElementNotVisible("#turvakielto-varoitus")
export const expectEiKuntailmoituksiaNotVisible = () =>
  expectElementNotVisible(".oppijaview__eiilmoituksia")

const rivi = (label: string, value?: string) =>
  value ? [label + ":", value] : []

export const oppivelvollisuustiedot = (p: {
  opiskelutilanne: string
  oppivelvollisuus: string
  oppivelvollisuudenKeskeytykset?: string[]
  maksuttomuusoikeus: string
  kuntailmoitusBtn?: true
  oppivelvollisuudenKeskeytysBtn?: true
}) =>
  [
    ...rivi("Opiskelutilanne", p.opiskelutilanne),
    ...rivi("Oppivelvollisuus", p.oppivelvollisuus),
    ...(p.oppivelvollisuudenKeskeytykset || []).map((d) => `Keskeytetty ${d}`),
    ...rivi("Oikeus opintojen maksuttomuuteen", p.maksuttomuusoikeus),
    ...(p.oppivelvollisuudenKeskeytysBtn ? ["Keskeytä oppivelvollisuus"] : []),
    ...(p.kuntailmoitusBtn
      ? ["Tee ilmoitus valvontavastuusta", "info_outline"]
      : []),
  ].join("\n")

export const historiaOpintoOikeus = (p: {
  otsikko: string
  tila: string
  maksuttomuus?: string[]
  toimipiste?: string
  ryhmä?: string
  vuosiluokkiinSitomatonOpetus?: boolean
  alkamispäivä: string
  päättymispäivä?: string
  perusopetusSuoritettu?: string
}) =>
  [
    "school",
    p.otsikko,
    ...rivi("Tila", p.tila),
    ...rivi("Maksuttomuus", p.maksuttomuus?.join("\n")),
    ...rivi("Oppilaitos/toimipiste", p.toimipiste),
    ...rivi("Ryhmä", p.ryhmä),
    ...rivi(
      "Muuta",
      p.vuosiluokkiinSitomatonOpetus
        ? "Vuosiluokkiin sitomaton opetus"
        : undefined
    ),
    ...rivi("Opiskeluoikeuden alkamispäivä", p.alkamispäivä),
    ...rivi("Opiskeluoikeuden päättymispäivä", p.päättymispäivä),
    ...rivi("Perusopetus suoritettu", p.perusopetusSuoritettu),
  ].join("\n")

export const historiaOppivelvollisuudenKeskeytys = (range: string) =>
  ["schedule", "Oppivelvollisuus", `Keskeytetty ${range}`].join("\n")

export const historiaOppivelvollisuudenKeskeytysToistaiseksi = (
  alkamispäivä: string
) =>
  [
    "schedule",
    "Oppivelvollisuus",
    `Keskeytetty toistaiseksi ${alkamispäivä} alkaen`,
  ].join("\n")

export const ilmoitetutYhteystiedot = (p: {
  pvm: string
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  matkapuhelin?: string
  sähköposti?: string
  lähde?: string
}) =>
  [
    "Ilmoitetut yhteystiedot",
    `keyboard_arrow_rightYhteystiedot – ${p.pvm}`,
    ...rivi("Lähiosoite", p.lähiosoite),
    ...rivi("Postitoimipaikka", p.postitoimipaikka),
    ...rivi("Maa", p.maa),
    ...rivi("Matkapuhelin", p.matkapuhelin),
    ...rivi("Sähköposti", p.sähköposti),
    ...(p.lähde ? [`Lähde: ${p.lähde}`] : []),
  ].join("\n")

export const historiaVastuuilmoitus = (p: {
  päivämäärä: string
  ilmoittaja?: string
  tahoJolleIlmoitettu?: string
}) =>
  [
    "schedule",
    "Vastuuilmoitus: Ei opiskelupaikkaa",
    ...rivi("Päivämäärä", p.päivämäärä),
    ...rivi("Ilmoittaja", p.ilmoittaja),
    ...rivi("Taho jolle ilmoitettu", p.tahoJolleIlmoitettu),
    "Lisätiedot",
  ].join("\n")

export const virallisetYhteystiedot = (p: {
  lähiosoite?: string
  postitoimipaikka?: string
  maa?: string
  puhelin?: string
  sähköposti?: string
}) =>
  [
    "Viralliset yhteystiedot",
    "keyboard_arrow_rightVTJ: Kotiosoite",
    ...rivi("Lähiosoite", p.lähiosoite),
    ...rivi("Postitoimipaikka", p.postitoimipaikka),
    ...rivi("Maa", p.maa),
    ...rivi("Puhelin", p.puhelin),
    ...rivi("Sähköposti", p.sähköposti),
  ].join("\n")

export const merge = (...items: string[]) => items.join("\n")
