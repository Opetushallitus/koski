import { currentYear } from "../../src/utils/date"
import {
  dataTableEventuallyEquals,
  defaultLogin,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Perusopetuksen näkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await defaultLogin("/oppijat")
    await textEventuallyEquals(
      ".card__header",
      `Perusopetuksen päättävät ${currentYear()} (9)`
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      `
      Aaltonen Ada Adalmiina	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	Hyväksytty: 1. Ressun lukio	1. Ressun lukio	1. Ressun lukio
      Kinnunen Jami Jalmari	Järvenpään yhteiskoulu	31.7.2005	9A	Aktiivinen	Hyväksytty: 1. Ressun lukio	1. Ressun lukio	-
      Laitela Niklas Henri	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Mäkinen Tapio Kalervo	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Ojanen Jani Kalle	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Pohjanen Anna Maria	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Raatikainen Hanna Sisko	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Vuorenmaa Maija Kaarina	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      Ylänen Toni Vilhelm	Järvenpään yhteiskoulu	31.7.2005	9A	Ei hakemusta	-	-	-
      `
    )
  })
})
