import { FormEvent, useEffect, useState } from "react";
import { createTweet, deleteTweet, listTweets, listUsers, updateTweet } from "../lib/api";
import { getErrorMessage, isUnauthorized } from "../lib/error";
import type { Tweet, TweetRequest, User } from "../lib/types";

type TweetSectionProps = {
  token: string;
  onUnauthorized: () => void;
};

const initialForm = {
  content: "",
  authorId: ""
};

export function TweetSection({ token, onUnauthorized }: TweetSectionProps) {
  const [tweets, setTweets] = useState<Tweet[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [form, setForm] = useState(initialForm);
  const [editingTweetId, setEditingTweetId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const [tweetData, userData] = await Promise.all([listTweets(token), listUsers(token)]);
      setTweets(tweetData);
      setUsers(userData);
    } catch (err) {
      if (isUnauthorized(err)) {
        onUnauthorized();
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, [token]);

  function beginEdit(tweet: Tweet) {
    setEditingTweetId(tweet.id);
    setForm({
      content: tweet.content,
      authorId: String(tweet.author.id)
    });
    setError(null);
  }

  function resetForm() {
    setEditingTweetId(null);
    setForm(initialForm);
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.authorId) {
      setError("Select an author");
      return;
    }

    const payload: TweetRequest = {
      content: form.content,
      authorId: Number(form.authorId)
    };

    setError(null);
    setIsSubmitting(true);
    try {
      if (editingTweetId !== null) {
        await updateTweet(editingTweetId, payload, token);
      } else {
        await createTweet(payload, token);
      }
      await refresh();
      resetForm();
    } catch (err) {
      if (isUnauthorized(err)) {
        onUnauthorized();
        return;
      }
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(id: number) {
    setError(null);
    try {
      await deleteTweet(id, token);
      await refresh();
      if (editingTweetId === id) {
        resetForm();
      }
    } catch (err) {
      if (isUnauthorized(err)) {
        onUnauthorized();
        return;
      }
      setError(getErrorMessage(err));
    }
  }

  return (
    <section className="card">
      <h2 className="section-title">Tweets</h2>
      <p className="section-copy">Post, edit, and remove tweets. Select an existing user as author.</p>

      <form className="form-grid" onSubmit={onSubmit} data-testid="tweets-form">
        <div className="full">
          <label htmlFor="tweets-content">Content</label>
          <textarea
            id="tweets-content"
            maxLength={200}
            value={form.content}
            onChange={(event) => setForm({ ...form, content: event.target.value })}
            required
          />
        </div>

        <div>
          <label htmlFor="tweets-author">Author</label>
          <select
            id="tweets-author"
            value={form.authorId}
            onChange={(event) => setForm({ ...form, authorId: event.target.value })}
            required
          >
            <option value="">Select a user</option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>{`${user.handle} (id:${user.id})`}</option>
            ))}
          </select>
        </div>

        <div className="inline-actions" style={{ alignItems: "flex-end" }}>
          <button type="submit" className="btn-primary" disabled={isSubmitting} data-testid="tweets-submit">
            {isSubmitting ? "Saving..." : editingTweetId ? "Update tweet" : "Create tweet"}
          </button>
          {editingTweetId ? (
            <button type="button" className="btn-secondary" onClick={resetForm}>
              Cancel edit
            </button>
          ) : null}
        </div>
      </form>

      {error ? <div className="notice notice-error">{error}</div> : null}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Content</th>
              <th>Author</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={4}>Loading tweets...</td>
              </tr>
            ) : tweets.length === 0 ? (
              <tr>
                <td colSpan={4}>No tweets found.</td>
              </tr>
            ) : (
              tweets.map((tweet) => (
                <tr key={tweet.id} data-testid={`tweet-row-${tweet.id}`}>
                  <td>{tweet.id}</td>
                  <td>{tweet.content}</td>
                  <td>{tweet.author?.handle ?? `user-${tweet.author?.id}`}</td>
                  <td>
                    <div className="inline-actions">
                      <button type="button" className="btn-secondary" onClick={() => beginEdit(tweet)}>
                        Edit
                      </button>
                      <button
                        type="button"
                        className="btn-danger"
                        onClick={() => void handleDelete(tweet.id)}
                        data-testid={`delete-tweet-${tweet.id}`}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
