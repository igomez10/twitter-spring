import { FormEvent, useState } from "react";
import { Link } from "react-router-dom";
import { createUser } from "../lib/api";
import { getErrorMessage } from "../lib/error";
import type { UserRequest } from "../lib/types";

const initialForm: UserRequest = {
  firstName: "",
  lastName: "",
  email: "",
  handle: "",
  username: "",
  password: ""
};

export function SignupPage() {
  const [form, setForm] = useState<UserRequest>(initialForm);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setIsSubmitting(true);
    try {
      await createUser(form);
      setSuccess("Account created. You can now sign in.");
      setForm({ ...initialForm, username: form.username });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="card auth-card">
        <h1 className="title">Create account</h1>
        <p className="subtitle">Sign up to get a role-enabled account for tweet and user APIs.</p>

        <form className="form-grid" onSubmit={onSubmit} data-testid="signup-form">
          <div>
            <label htmlFor="firstName">First name</label>
            <input
              id="firstName"
              value={form.firstName}
              onChange={(event) => setForm({ ...form, firstName: event.target.value })}
            />
          </div>

          <div>
            <label htmlFor="lastName">Last name</label>
            <input
              id="lastName"
              value={form.lastName}
              onChange={(event) => setForm({ ...form, lastName: event.target.value })}
            />
          </div>

          <div>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              required
            />
          </div>

          <div>
            <label htmlFor="handle">Handle</label>
            <input
              id="handle"
              value={form.handle}
              onChange={(event) => setForm({ ...form, handle: event.target.value })}
              required
            />
          </div>

          <div>
            <label htmlFor="signup-username">Username</label>
            <input
              id="signup-username"
              value={form.username}
              onChange={(event) => setForm({ ...form, username: event.target.value })}
              required
            />
          </div>

          <div>
            <label htmlFor="signup-password">Password</label>
            <input
              id="signup-password"
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </div>

          <div className="full inline-actions">
            <button type="submit" className="btn-primary" disabled={isSubmitting} data-testid="signup-submit">
              {isSubmitting ? "Creating..." : "Create account"}
            </button>
            <Link to="/login">Back to login</Link>
          </div>
        </form>

        {error ? <div className="notice notice-error">{error}</div> : null}
        {success ? <div className="notice notice-success">{success}</div> : null}
      </section>
    </main>
  );
}
