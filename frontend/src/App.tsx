import './App.css'

function App() {
  return (
    <main className="app-shell">
      <p className="eyebrow">PulseOps</p>
      <h1>API monitoring dashboard</h1>
      <p className="intro">
        We will build this screen one piece at a time, starting with a monitor
        list and then connecting it to the Spring backend.
      </p>
      <section className="monitor-section">
        <h2>Monitors</h2>

        <div className="monitor-card">
          <div>
            <h3>GitHub API</h3>
            <p>https://api.github.com</p>
          </div>

          <div className="monitor-meta">
            <span className="status-pill">UP</span>
            <span>28ms</span>
          </div>
        </div>
      </section>

    </main>
  )
}

export default App
