import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { TweetSection } from "./TweetSection";
import * as api from "../lib/api";

vi.mock("../lib/api", async () => {
  const actual = await vi.importActual<typeof import("../lib/api")>("../lib/api");
  return {
    ...actual,
    listTweets: vi.fn(),
    listUsers: vi.fn(),
    createTweet: vi.fn(),
    updateTweet: vi.fn(),
    deleteTweet: vi.fn()
  };
});

describe("TweetSection", () => {
  it("submits create tweet payload", async () => {
    const listTweets = vi.mocked(api.listTweets);
    const listUsers = vi.mocked(api.listUsers);
    const createTweet = vi.mocked(api.createTweet);

    listUsers.mockResolvedValue([
      {
        id: 7,
        firstName: "Linus",
        lastName: "Torvalds",
        email: "linus@example.com",
        handle: "linus"
      }
    ]);
    listTweets.mockResolvedValueOnce([]).mockResolvedValueOnce([
      {
        id: 12,
        content: "hello world",
        author: {
          id: 7,
          firstName: "Linus",
          lastName: "Torvalds",
          email: "linus@example.com",
          handle: "linus"
        }
      }
    ]);
    createTweet.mockResolvedValue({
      id: 12,
      content: "hello world",
      author: {
        id: 7,
        firstName: "Linus",
        lastName: "Torvalds",
        email: "linus@example.com",
        handle: "linus"
      }
    });

    render(<TweetSection token="token" onUnauthorized={vi.fn()} />);

    await screen.findByText("No tweets found.");

    await userEvent.type(screen.getByLabelText("Content"), "hello world");
    await userEvent.selectOptions(screen.getByLabelText("Author"), "7");
    await userEvent.click(screen.getByTestId("tweets-submit"));

    await waitFor(() => {
      expect(createTweet).toHaveBeenCalledWith({ content: "hello world", authorId: 7 }, "token");
    });

    await screen.findByText("hello world");
  });

  it("calls onUnauthorized when list call returns 401", async () => {
    const listTweets = vi.mocked(api.listTweets);
    const listUsers = vi.mocked(api.listUsers);
    const onUnauthorized = vi.fn();

    listTweets.mockRejectedValue(new api.ApiClientError(401, "Unauthorized", null));
    listUsers.mockResolvedValue([]);

    render(<TweetSection token="token" onUnauthorized={onUnauthorized} />);

    await waitFor(() => {
      expect(onUnauthorized).toHaveBeenCalled();
    });
  });
});
