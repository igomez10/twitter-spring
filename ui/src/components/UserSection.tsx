import { FormEvent, useEffect, useState } from "react";
import { createUser, deleteUser, listUsers, updateUser } from "../lib/api";
import { getErrorMessage, isUnauthorized } from "../lib/error";
import type { User, UserRequest } from "../lib/types";

type UserSectionProps = {
  token: string;
  onUnauthorized: () => void;
};

const initialForm: UserRequest = {
  firstName: "",
  lastName: "",
  email: "",
  handle: "",
  username: "",
  password: ""
};

export function UserSection({ token, onUnauthorized }: UserSectionProps) {
  const [users, setUsers] = useState<User[]>([]);
  const [form, setForm] = useState<UserRequest>(initialForm);
  const [editingUserId, setEditingUserId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function refreshUsers() {
    setIsLoading(true);
    setError(null);
    try {
      const data = await listUsers(token);
      setUsers(data);
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
    void refreshUsers();
  }, [token]);

  function beginEdit(user: User) {
    setEditingUserId(user.id);
    setForm({
      firstName: user.firstName ?? "",
      lastName: user.lastName ?? "",
      email: user.email,
      handle: user.handle,
      username: "",
      password: ""
    });
    setError(null);
  }

  function resetForm() {
    setEditingUserId(null);
    setForm(initialForm);
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      if (editingUserId !== null) {
        await updateUser(editingUserId, form, token);
      } else {
        await createUser(form);
      }
      await refreshUsers();
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
      await deleteUser(id, token);
      await refreshUsers();
      if (editingUserId === id) {
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
      <h2 className="section-title">Users</h2>
      <p className="section-copy">Create, update, and soft-delete users. Update requires username and password.</p>

      <form className="form-grid" onSubmit={onSubmit} data-testid="users-form">
        <div>
          <label htmlFor="users-firstName">First name</label>
          <input
            id="users-firstName"
            value={form.firstName}
            onChange={(event) => setForm({ ...form, firstName: event.target.value })}
          />
        </div>

        <div>
          <label htmlFor="users-lastName">Last name</label>
          <input
            id="users-lastName"
            value={form.lastName}
            onChange={(event) => setForm({ ...form, lastName: event.target.value })}
          />
        </div>

        <div>
          <label htmlFor="users-email">Email</label>
          <input
            id="users-email"
            type="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            required
          />
        </div>

        <div>
          <label htmlFor="users-handle">Handle</label>
          <input
            id="users-handle"
            value={form.handle}
            onChange={(event) => setForm({ ...form, handle: event.target.value })}
            required
          />
        </div>

        <div>
          <label htmlFor="users-username">Username</label>
          <input
            id="users-username"
            value={form.username}
            onChange={(event) => setForm({ ...form, username: event.target.value })}
            required
          />
        </div>

        <div>
          <label htmlFor="users-password">Password</label>
          <input
            id="users-password"
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            required
          />
        </div>

        <div className="full inline-actions">
          <button type="submit" className="btn-primary" disabled={isSubmitting} data-testid="users-submit">
            {isSubmitting ? "Saving..." : editingUserId ? "Update user" : "Create user"}
          </button>
          {editingUserId ? (
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
              <th>Name</th>
              <th>Email</th>
              <th>Handle</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={5}>Loading users...</td>
              </tr>
            ) : users.length === 0 ? (
              <tr>
                <td colSpan={5}>No users found.</td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id} data-testid={`user-row-${user.id}`}>
                  <td>{user.id}</td>
                  <td>{`${user.firstName ?? ""} ${user.lastName ?? ""}`.trim() || "-"}</td>
                  <td>{user.email}</td>
                  <td>{user.handle}</td>
                  <td>
                    <div className="inline-actions">
                      <button type="button" className="btn-secondary" onClick={() => beginEdit(user)}>
                        Edit
                      </button>
                      <button
                        type="button"
                        className="btn-danger"
                        onClick={() => void handleDelete(user.id)}
                        data-testid={`delete-user-${user.id}`}
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
