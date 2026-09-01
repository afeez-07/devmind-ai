// import { useEffect, useState } from "react";
//
// function App() {
//     const [message, setMessage] = useState("Connecting to backend...");
//     const [loading, setLoading] = useState(true);
//
//     useEffect(() => {
//         fetch("http://localhost:8080/api/health")
//             .then((response) => {
//                 if (!response.ok) {
//                     throw new Error("Backend request failed");
//                 }
//
//                 return response.text();
//             })
//             .then((data) => {
//                 setMessage(data);
//             })
//             .catch((error) => {
//                 console.error(error);
//                 setMessage("Backend connection failed ❌");
//             })
//             .finally(() => {
//                 setLoading(false);
//             });
//     }, []);
//
//     return (
//         <div>
//             <h1>DevMind AI</h1>
//
//             <p>AI Software Development Assistant</p>
//
//             <hr />
//
//             <h2>Backend Status</h2>
//
//             {loading ? (
//                 <p>Connecting... ⏳</p>
//             ) : (
//                 <p>{message}</p>
//             )}
//         </div>
//     );
// }
//
// export default App;

// import Dashboard from './pages/Dashboard';
//
// function App() {
//     return <Dashboard />;
// }
//
// export default App;

// import Users from './pages/Users';
//
// function App() {
//     return <Users />;
// }
//
// export default App;

// import { useEffect, useState } from 'react';
// import Users from './pages/Users';
//
// function App() {
//     const [projects, setProjects] = useState([]);
//     const [name, setName] = useState('');
//     const [description, setDescription] = useState('');
//     const [language, setLanguage] = useState('');
//     const [message, setMessage] = useState('');
//
//     useEffect(() => {
//         fetch('http://localhost:8080/api/projects')
//             .then((response) => response.json())
//             .then((data) => setProjects(data))
//             .catch((error) => console.error('Error fetching projects:', error));
//     }, []);
//
//     const createProject = (event) => {
//         event.preventDefault();
//
//         fetch('http://localhost:8080/api/projects', {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json',
//             },
//             body: JSON.stringify({
//                 name: name,
//                 description: description,
//                 language: language,
//             }),
//         })
//             .then((response) => {
//                 if (!response.ok) {
//                     throw new Error('Failed to create project');
//                 }
//
//                 return response.json();
//             })
//             .then((newProject) => {
//                 setProjects((currentProjects) => [
//                     ...currentProjects,
//                     newProject,
//                 ]);
//
//                 setName('');
//                 setDescription('');
//                 setLanguage('');
//
//                 setMessage('Project created successfully! 🚀');
//             })
//             .catch((error) => {
//                 console.error(error);
//                 setMessage('Failed to create project ❌');
//             });
//     };
//
//     return (
//         <>
//             <Users />
//
//             <section>
//                 <h1>Projects</h1>
//                 <p>DevMind AI Projects</p>
//
//                 <form onSubmit={createProject}>
//                     <input
//                         type="text"
//                         placeholder="Project name"
//                         value={name}
//                         onChange={(event) => setName(event.target.value)}
//                         required
//                     />
//
//                     <input
//                         type="text"
//                         placeholder="Description"
//                         value={description}
//                         onChange={(event) => setDescription(event.target.value)}
//                         required
//                     />
//
//                     <input
//                         type="text"
//                         placeholder="Language"
//                         value={language}
//                         onChange={(event) => setLanguage(event.target.value)}
//                         required
//                     />
//
//                     <button type="submit">
//                         Create Project
//                     </button>
//                 </form>
//
//                 {message && <p>{message}</p>}
//
//                 <h2>All Projects</h2>
//
//                 {projects.length === 0 ? (
//                     <p>No projects found.</p>
//                 ) : (
//                     projects.map((project) => (
//                         <div key={project.id}>
//                             <h3>{project.name}</h3>
//                             <p>{project.description}</p>
//                             <p>Language: {project.language}</p>
//                             <p>Project ID: {project.id}</p>
//                         </div>
//                     ))
//                 )}
//             </section>
//         </>
//     );
// }
//
// export default App;

import { useEffect, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api";

function App() {
  const [activePage, setActivePage] = useState("dashboard");

  const [backendStatus, setBackendStatus] = useState("Checking...");
  const [users, setUsers] = useState([]);
  const [projects, setProjects] = useState([]);

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");

  const [projectName, setProjectName] = useState("");
  const [description, setDescription] = useState("");
  const [language, setLanguage] = useState("");

  const [message, setMessage] = useState("");

  // Check backend
  useEffect(() => {
    fetch(`${API_URL}/health`)
      .then((response) => response.text())
      .then((data) => setBackendStatus(data))
      .catch(() => setBackendStatus("Backend connection failed ❌"));
  }, []);

  // Fetch users
  const fetchUsers = () => {
    fetch(`${API_URL}/users`)
      .then((response) => response.json())
      .then((data) => setUsers(data))
      .catch((error) => console.error("Users error:", error));
  };

  // Fetch projects
  const fetchProjects = () => {
    fetch(`${API_URL}/projects`)
      .then((response) => response.json())
      .then((data) => setProjects(data))
      .catch((error) => console.error("Projects error:", error));
  };

  useEffect(() => {
    fetchUsers();
    fetchProjects();
  }, []);

  // Create user
  const createUser = async (event) => {
    event.preventDefault();

    try {
      const response = await fetch(`${API_URL}/users`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          email,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to create user");
      }

      setUsername("");
      setEmail("");
      setMessage("User created successfully 🚀");

      fetchUsers();
    } catch (error) {
      setMessage("Failed to create user ❌");
      console.error(error);
    }
  };

  // Create project
  const createProject = async (event) => {
    event.preventDefault();

    try {
      const response = await fetch(`${API_URL}/projects`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: projectName,
          description,
          language,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to create project");
      }

      setProjectName("");
      setDescription("");
      setLanguage("");
      setMessage("Project created successfully 🚀");

      fetchProjects();
    } catch (error) {
      setMessage("Failed to create project ❌");
      console.error(error);
    }
  };

  return (
    <div className="app">

      {/* Sidebar */}
      <aside className="sidebar">
        <div className="logo">
          <h1>DevMind AI</h1>
          <p>AI Software Development Assistant</p>
        </div>

        <nav>
          <button
            className={activePage === "dashboard" ? "active" : ""}
            onClick={() => setActivePage("dashboard")}
          >
            🏠 Dashboard
          </button>

          <button
            className={activePage === "users" ? "active" : ""}
            onClick={() => setActivePage("users")}
          >
            👥 Users
          </button>

          <button
            className={activePage === "projects" ? "active" : ""}
            onClick={() => setActivePage("projects")}
          >
            📁 Projects
          </button>

          <button
            className={activePage === "assistant" ? "active" : ""}
            onClick={() => setActivePage("assistant")}
          >
            🤖 AI Assistant
          </button>
        </nav>
      </aside>

      {/* Main content */}
      <main className="main">

        {/* Header */}
        <header className="topbar">
          <div>
            <h2>
              {activePage === "dashboard" && "Dashboard"}
              {activePage === "users" && "Users"}
              {activePage === "projects" && "Projects"}
              {activePage === "assistant" && "AI Assistant"}
            </h2>
          </div>

          <div className="backend-status">
            <span className="status-dot"></span>
            Backend Online
          </div>
        </header>

        {/* Dashboard */}
        {activePage === "dashboard" && (
          <section className="page">

            <div className="welcome">
              <h1>Welcome to DevMind AI 👋</h1>
              <p>
                Your AI-powered software development workspace.
              </p>
            </div>

            <div className="stats">

              <div className="stat-card">
                <span>👥</span>
                <h3>{users.length}</h3>
                <p>Total Users</p>
              </div>

              <div className="stat-card">
                <span>📁</span>
                <h3>{projects.length}</h3>
                <p>Total Projects</p>
              </div>

              <div className="stat-card">
                <span>⚡</span>
                <h3>UP</h3>
                <p>Backend Status</p>
              </div>

            </div>

            <div className="section-card">
              <h2>Recent Projects</h2>

              {projects.length === 0 ? (
                <p>No projects available.</p>
              ) : (
                <div className="project-grid">
                  {projects.map((project) => (
                    <div className="project-card" key={project.id}>
                      <h3>{project.name}</h3>
                      <p>{project.description}</p>
                      <span>{project.language}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </section>
        )}

        {/* Users */}
        {activePage === "users" && (
          <section className="page">

            <div className="section-card">
              <h2>Create User</h2>

              <form onSubmit={createUser} className="form">

                <input
                  type="text"
                  placeholder="Username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />

                <input
                  type="email"
                  placeholder="Email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />

                <button type="submit">
                  Create User
                </button>

              </form>

              {message && <p className="message">{message}</p>}
            </div>

            <div className="section-card">
              <h2>All Users</h2>

              <div className="user-list">

                {users.map((user) => (
                  <div className="user-card" key={user.id}>
                    <div>
                      <h3>{user.username}</h3>
                      <p>{user.email}</p>
                    </div>

                    <span>ID: {user.id}</span>
                  </div>
                ))}

              </div>
            </div>

          </section>
        )}

        {/* Projects */}
        {activePage === "projects" && (
          <section className="page">

            <div className="section-card">
              <h2>Create Project</h2>

              <form onSubmit={createProject} className="form">

                <input
                  type="text"
                  placeholder="Project name"
                  value={projectName}
                  onChange={(e) => setProjectName(e.target.value)}
                  required
                />

                <input
                  type="text"
                  placeholder="Description"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />

                <input
                  type="text"
                  placeholder="Programming language"
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  required
                />

                <button type="submit">
                  Create Project
                </button>

              </form>

              {message && <p className="message">{message}</p>}
            </div>

            <div className="section-card">
              <h2>All Projects</h2>

              <div className="project-grid">

                {projects.map((project) => (
                  <div className="project-card" key={project.id}>

                    <h3>{project.name}</h3>

                    <p>{project.description}</p>

                    <div className="project-info">
                      <span>{project.language}</span>
                      <small>ID: {project.id}</small>
                    </div>

                  </div>
                ))}

              </div>
            </div>

          </section>
        )}

        {/* AI Assistant */}
        {activePage === "assistant" && (
          <section className="page">

            <div className="assistant-card">

              <div className="robot">
                🤖
              </div>

              <h1>DevMind AI Assistant</h1>

              <p>
                Your AI software development assistant is coming soon.
              </p>

              <div className="coming-soon">
                AI Engine — Coming Soon 🚀
              </div>

            </div>

          </section>
        )}

      </main>
    </div>
  );
}

export default App;