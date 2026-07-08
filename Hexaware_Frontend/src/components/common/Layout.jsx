import Sidebar from "./Sidebar.jsx";

function Layout({ title, subtitle, children }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="main-content">
        <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-4">
          <div>
            <h2 className="mb-1">{title}</h2>
            {subtitle && <p className="text-muted mb-0">{subtitle}</p>}
          </div>
        </div>
        {children}
      </main>
    </div>
  );
}

export default Layout;
