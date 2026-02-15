import { expect, test } from "@playwright/test";
import { signupAndLogin, uniqueSuffix } from "./helpers";

test("users CRUD from dashboard", async ({ page }) => {
  await signupAndLogin(page, uniqueSuffix());

  const suffix = uniqueSuffix();
  const email = `crud${suffix}@example.com`;
  const handle = `crud${suffix}`;

  await page.getByLabel("First name").fill("Crud");
  await page.getByLabel("Last name").fill("User");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Handle").fill(handle);
  await page.getByLabel("Username").fill(`crud-u-${suffix}`);
  await page.getByLabel("Password").fill(`Pass${suffix}!`);
  await page.getByTestId("users-submit").click();

  await expect(page.getByText(email)).toBeVisible();

  const row = page.locator("tr", { hasText: handle }).first();
  await row.getByRole("button", { name: "Edit" }).click();
  await page.getByLabel("Last name").fill("Updated");
  await page.getByLabel("Username").fill(`crud-u2-${suffix}`);
  await page.getByLabel("Password").fill(`Pass2-${suffix}!`);
  await page.getByTestId("users-submit").click();

  await expect(page.getByText("Updated")).toBeVisible();

  const updatedRow = page.locator("tr", { hasText: handle }).first();
  await updatedRow.getByRole("button", { name: "Delete" }).click();

  await expect(page.getByText(email)).toHaveCount(0);
});
