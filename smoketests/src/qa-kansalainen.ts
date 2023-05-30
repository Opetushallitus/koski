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

const tryToLogin = async (page: Page) => {
  await page.goto("https://testiopintopolku.fi/koski");

  await click(page, ".login-button");
  await click(page, "#li_fakevetuma2");
  await click(page, ".default-link");
  await click(page, "#tunnistaudu");
  await click(page, "#continue-button");

  return (
    (await textContent(page, ".header__name")) === "Nordea Demos. 21.2.1981"
  );
};

const runTest = async (): Promise<void> => {
  for (let i = 0; i < retryCount; i++) {
    console.log(`Attempt ${i + 1}/${retryCount}`);

    const browser = await puppeteer.launch({
      headless: "new",
    });
    const page = await browser.newPage();
    const resultOk = await tryToLogin(page);
    await browser.close();

    if (resultOk) {
      console.log("Success!");
      return;
    }
    console.log("Failed!");
  }
  console.log("Smoke test has failed.");
  throw new Error("Smoke test failed");
};

runTest();
