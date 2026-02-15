import { expect, test } from "@playwright/test";
import { signupAndLogin, uniqueSuffix } from "./helpers";

test("invalid session token forces redirect to login", async ({ page }) => {
  await signupAndLogin(page, uniqueSuffix());

  await page.evaluate(() => {
    localStorage.setItem("twitter_access_token", "not-a-valid-token");
  });

  await page.goto("/dashboard");

  await expect(page.getByRole("heading", { name: "Log in" })).toBeVisible();
});
