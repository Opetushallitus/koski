import path from "path";
import puppeteer, { Page } from "puppeteer";

type Environment = "local" | "dev" | "qa";

const RETRIES = 3;
const TIMEOUT_MS = 30000;

const ENVIRONMENTS: Record<Environment, string> = {
  local: process.env.OMADATA_URL || "http://localhost:7051",
  dev: "https://oph-koski-omadataoauth2sample-dev.testiopintopolku.fi",
  qa: "https://oph-koski-omadataoauth2sample-qa.testiopintopolku.fi",
};

const clickAndWait = async (
  page: Page,
  selector: string,
  options: { navigation?: boolean } = {}
) => {
  console.log(`Wait for ${selector}`)
  await page.waitForSelector(selector);

  if (options.navigation) {
    console.log(`Click for ${selector}`)
    await Promise.all([
      page.waitForNavigation({ waitUntil: "networkidle2" }),
      page.click(selector),
    ]);
  } else {
    await page.click(selector);
  }
};

const waitForUrlPart = async (page: Page, part: string) => {
  console.log("Wait for url part", part)
  await page.waitForFunction(
    expected => window.location.href.includes(expected),
    {},
    part
  );
};

const expectTexts = async (page: Page, texts: string[]) => {
  await page.waitForFunction(
    expected => {
      const content = document.documentElement?.innerText?.toLowerCase() || "";
      return expected.every(t => content.includes(t.toLowerCase()));
    },
    {},
    texts
  );
};

const withRetries = async <T>(
  attempts: number,
  fn: (attempt: number) => Promise<T>
): Promise<T> => {
  let lastError: unknown;

  for (let i = 0; i < attempts; i++) {
    try {
      return await fn(i + 1);
    } catch (error) {
      lastError = error;
      console.error(
        `Attempt ${i + 1}/${attempts} failed: ${
          error instanceof Error ? error.message : String(error)
        }`
      );
    }
  }

  throw lastError;
};

const suomiFiLogin = async (page: Page, opts: { local: boolean }) => {
  if (opts.local) {
    await page.waitForSelector("#hetu");
    await page.type("#hetu", "210281-8715");
    await clickAndWait(page, "button.koski-button.blue", {
      navigation: true,
    });
  } else {
    await clickAndWait(page, "#li_fakevetuma2");
    await clickAndWait(page, ".default-link");
    await clickAndWait(page, "#tunnistaudu");
    await clickAndWait(page, "#continue-button", { navigation: true });
  }
};

const expectPerson = (
  actual: Record<string, unknown>,
  expected: Record<string, unknown>
) => {
  for (const [key, value] of Object.entries(expected)) {
    if (actual[key] !== value) {
      throw new Error(
        `Mismatch for "${key}": expected ${value}, got ${actual[key]}`
      );
    }
  }
};

const authorizeAndVerifyData = async (page: Page) => {
  await waitForUrlPart(page, "omadata-oauth2/authorize");

  await expectTexts(page, [
    "nimi",
    "hetu",
    "Suoritetut tutkinnot",
    "Suostumuksesi",
  ]);

  await clickAndWait(page, "button.acceptance-button", {
    navigation: true,
  });

  await waitForUrlPart(page, "form-post-response-cb");

  const raw = await page.$eval("pre", el => el.textContent ?? "");
  const json = JSON.parse(raw) as { henkilö?: Record<string, unknown> };

  console.log("Check that assumed data is visible")
  expectPerson(json.henkilö ?? {}, {
    kutsumanimi: "Nordea",
    hetu: "210281-8715",
    syntymäaika: "1981-02-21",
  });
};

const runTest = async (environment: Environment) =>
  withRetries(RETRIES, async attempt => {
    console.log(
      `Running omadata-oauth2-sample smoke test (${environment}) – attempt ${attempt}/${RETRIES}`
    );

    const browser = await puppeteer.launch({ headless: true });
    const isLocal = environment === "local";
    const page = await browser.newPage();

    try {
      page.setDefaultTimeout(TIMEOUT_MS);
      page.setDefaultNavigationTimeout(TIMEOUT_MS);

      await page.goto(ENVIRONMENTS[environment], {
        waitUntil: "networkidle2",
      });

      await clickAndWait(
        page,
        'a[href="/api/openid-api-test?scope=HENKILOTIEDOT_KAIKKI_TIEDOT+OPISKELUOIKEUDET_SUORITETUT_TUTKINNOT"]',
        { navigation: true }
      );

      await suomiFiLogin(page, { local: isLocal });
      await authorizeAndVerifyData(page);

      console.log("Smoke test succeeded");
    } finally {
      await browser.close();
    }
  });

const environment = process.argv[2] as Environment | undefined;

if (!environment || !ENVIRONMENTS[environment]) {
  console.error(
    "Usage: pnpm ts-node src/omadata-oauth2-sample.ts <local|dev|qa>"
  );
  process.exit(1);
}

runTest(environment).catch(error => {
  console.error(
    error instanceof Error ? error.message : JSON.stringify(error)
  );
  process.exit(1);
});
