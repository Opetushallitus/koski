import puppeteer, { Page } from "puppeteer";

const retryCount = 5;
const defaultTimeoutSecs = 60;

const timeout = defaultTimeoutSecs * 1000;

const click = async (page: Page, selector: string) => {
  console.log(`Wait for ${selector}`);
  await page.waitForSelector(selector, { timeout });
  console.log(`Click ${selector}`);
  await page.click(selector);
};

const textContent = async (
  page: Page,
  selector: string
): Promise<string | null | undefined> => {
  console.log(`Wait for ${selector}`);
  const textSelector = await page.waitForSelector(selector, { timeout });
  const text = await textSelector?.evaluate((el) => el.textContent, {
    timeout,
  });
  console.log(`Received text for ${selector}: ${text}`);
  return text;
};

const suomiFiLogin = async (page: Page) => {
  await page.goto("https://testiopintopolku.fi/koski");

  await click(page, ".login-button");
  await click(page, "#li_fakevetuma2");
  await click(page, ".default-link");
  await click(page, "#tunnistaudu");
  await click(page, "#continue-button");

  const header = await textContent(page, ".header__name");

  return (
    header?.includes("Demo") &&
    header.includes("Nordea") &&
    header.includes("s. 21.2.1981")
  );
};

const eIdasLogin = async (page: Page) => {
  await page.goto("https://testiopintopolku.fi/koski");

  await click(page, ".login-button");
  await click(page, "#eidas_high");
  await click(page, "#li_EE");
  await click(page, "#continue-button");

  const header = await textContent(page, ".header__name");

  return (
    header?.includes("Amir Testi") &&
    header.includes("Aaltonen-Testi") &&
    header.includes("s. 9.7.1972")
  );
};

const runTest = async (
  tryToLogin: (page: Page) => Promise<boolean | undefined>,
  testName: string
): Promise<void> => {
  for (let i = 0; i < retryCount; i++) {
    console.log(`Attempt ${i + 1}/${retryCount} for ${testName}`);

    const browser = await puppeteer.launch({
      headless: true,
    });
    const page = await browser.newPage();
    const resultOk = await tryToLogin(page);
    await browser.close();

    if (resultOk) {
      console.log(`Success for ${testName}!`);
      return;
    }
    console.log(`Failed for ${testName}!`);
  }
  console.log(`Smoke test has failed for ${testName}.`);
  throw new Error(`Smoke test failed for ${testName}`);
};

const runTests = async (): Promise<void> => {
  await runTest(suomiFiLogin, "suomi.fi login test");
  await runTest(eIdasLogin, "eIDAS login test");
}

runTests();
