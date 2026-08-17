import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <main className="not-found">
      <p className="eyebrow">404</p>
      <h1>Page not found</h1>
      <Link className="button" to="/dashboard">Back to TaskFlow</Link>
    </main>
  );
}
