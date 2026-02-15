import { expect, test } from "@playwright/test";
import { signupAndLogin, uniqueSuffix } from "./helpers";

test("tweets CRUD from dashboard", async ({ page }) => {
  await signupAndLogin(page, uniqueSuffix());

  await page.getByTestId("tab-tweets").click();

  const content = `tweet-${uniqueSuffix()}`;
  const authorValue = await page.locator("#tweets-author option").nth(1).getAttribute("value");
  if (!authorValue) {
    throw new Error("No author option was available for tweet creation");
  }

  await page.getByLabel("Content").fill(content);
  await page.getByLabel("Author").selectOption(authorValue);
  await page.getByTestId("tweets-submit").click();

  await expect(page.getByText(content)).toBeVisible();

  const row = page.locator("tr", { hasText: content }).first();
  await row.getByRole("button", { name: "Edit" }).click();
  const updatedContent = `${content}-updated`;
  await page.getByLabel("Content").fill(updatedContent);
  await page.getByTestId("tweets-submit").click();

  await expect(page.getByText(updatedContent)).toBeVisible();

  const updatedRow = page.locator("tr", { hasText: updatedContent }).first();
  await updatedRow.getByRole("button", { name: "Delete" }).click();

  await expect(page.getByText(updatedContent)).toHaveCount(0);
});
