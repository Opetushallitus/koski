import { currentYear } from "../../src/utils/date"
import {
  dataTableEventuallyEquals,
  defaultLogin,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Perusopetuksen näkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await defaultLogin("/perusopetus")
    await textEventuallyEquals(
      ".card__header",
      `Perusopetuksen päättävät ${currentYear()} (9)`
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      `
        Aaltonen Ada Adalmiina	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	Hyväksytty: 1. Ressun lukio	Ressun lukio	Ressun lukio
        Kinnunen Jami Jalmari	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	warningEi opiskelupaikkaa	-	-
        Laitela Niklas Henri	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	warningEi opiskelupaikkaa	-	-
        Mäkinen Tapio Kalervo	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	warningEi opiskelupaikkaa	-	-
        Ojanen Jani Kalle	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	warningEi opiskelupaikkaa	-	-
        Pohjanen Anna Maria	Järvenpään yhteiskoulu	31.7.2005	9A	Puutteellinen	warningEi opiskelupaikkaa	-	-
        Raatikainen Hanna Sisko	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	warningEi opiskelupaikkaa	-	-
        Vuorenmaa Maija Kaarina	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	warningEi opiskelupaikkaa	-	-
        Ylänen Toni Vilhelm	Järvenpään yhteiskoulu	31.7.2005	9A	Luonnos	warningEi opiskelupaikkaa	-	-
      `
    )
  })
})
