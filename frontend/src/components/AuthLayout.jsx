export default function AuthLayout({ title, subtitle, children }) {
  return (
    <main className="auth-page">
      <section className="auth-panel">
        <a className="auth-brand" href="/login">TaskFlow</a>
        <p className="eyebrow">Task management made clear</p>
        <h1>{title}</h1>
        <p className="auth-subtitle">{subtitle}</p>
        {children}
      </section>
    </main>
  );
}
