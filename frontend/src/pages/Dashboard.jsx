import { useEffect, useState } from 'react';
import { getHealth, getUsers } from '../services/api';

function Dashboard() {

    const [backendStatus, setBackendStatus] = useState('Checking...');
    const [users, setUsers] = useState([]);

    useEffect(() => {

        getHealth()
            .then(data => setBackendStatus(data))
            .catch(() => setBackendStatus('Backend connection failed ❌'));

        getUsers()
            .then(data => setUsers(data))
            .catch(error => console.error(error));

    }, []);

    return (
        <div className="dashboard">

            <h1>DevMind AI</h1>

            <p>AI Software Development Assistant</p>

            <section className="stats">

                <div className="card">
                    <h3>Backend</h3>
                    <p>{backendStatus}</p>
                </div>

                <div className="card">
                    <h3>Users</h3>
                    <p>{users.length}</p>
                </div>

                <section>
                  <h2>Projects</h2>

                  {projects.length === 0 ? (
                    <p>No projects found.</p>
                  ) : (
                    projects.map(project => (
                      <div key={project.id}>
                        <h3>{project.name}</h3>
                        <p>{project.description}</p>
                        <p>Language: {project.language}</p>
                        <p>Project ID: {project.id}</p>
                      </div>
                    ))
                  )}
                </section>

            </section>

        </div>
    );
}

export default Dashboard;