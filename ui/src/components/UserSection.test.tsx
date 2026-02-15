import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { UserSection } from "./UserSection";
import * as api from "../lib/api";

vi.mock("../lib/api", async () => {
  const actual = await vi.importActual<typeof import("../lib/api")>("../lib/api");
  return {
    ...actual,
    listUsers: vi.fn(),
    createUser: vi.fn(),
    updateUser: vi.fn(),
    deleteUser: vi.fn()
  };
});

describe("UserSection", () => {
  it("submits create user payload and refreshes list", async () => {
    const listUsers = vi.mocked(api.listUsers);
    const createUser = vi.mocked(api.createUser);

    listUsers.mockResolvedValueOnce([]).mockResolvedValueOnce([
      {
        id: 44,
        firstName: "Ada",
        lastName: "Lovelace",
        email: "ada@example.com",
        handle: "adal"
      }
    ]);
    createUser.mockResolvedValue({
      id: 44,
      firstName: "Ada",
      lastName: "Lovelace",
      email: "ada@example.com",
      handle: "adal"
    });

    render(<UserSection token="token" onUnauthorized={vi.fn()} />);

    await screen.findByText("No users found.");

    await userEvent.type(screen.getByLabelText("First name"), "Ada");
    await userEvent.type(screen.getByLabelText("Last name"), "Lovelace");
    await userEvent.type(screen.getByLabelText("Email"), "ada@example.com");
    await userEvent.type(screen.getByLabelText("Handle"), "adal");
    await userEvent.type(screen.getByLabelText("Username"), "adal");
    await userEvent.type(screen.getByLabelText("Password"), "password123");
    await userEvent.click(screen.getByTestId("users-submit"));

    await waitFor(() => {
      expect(createUser).toHaveBeenCalledWith({
        firstName: "Ada",
        lastName: "Lovelace",
        email: "ada@example.com",
        handle: "adal",
        username: "adal",
        password: "password123"
      });
    });

    await screen.findByText("ada@example.com");
  });

  it("calls onUnauthorized when API returns 401", async () => {
    const listUsers = vi.mocked(api.listUsers);
    const onUnauthorized = vi.fn();

    listUsers.mockRejectedValue(new api.ApiClientError(401, "Unauthorized", null));

    render(<UserSection token="token" onUnauthorized={onUnauthorized} />);

    await waitFor(() => {
      expect(onUnauthorized).toHaveBeenCalled();
    });
  });
});
