import { useEffect, useState } from 'react';
import { createUser, getUsers } from '../services/api';

function Users() {

    const [users, setUsers] = useState([]);
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');

    const loadUsers = () => {
        getUsers()
            .then(data => setUsers(data))
            .catch(error => console.error(error));
    };

    useEffect(() => {
        loadUsers();
    }, []);

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            await createUser({
                username,
                email
            });

            setUsername('');
            setEmail('');
            setMessage('User created successfully! 🚀');

            loadUsers();

        } catch (error) {
            setMessage('Failed to create user ❌');
        }
    };

    return (
        <div className="users-page">

            <h1>Users</h1>

            <p>Manage DevMind AI users</p>

            <form onSubmit={handleSubmit}>

                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    required
                />

                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    required
                />

                <button type="submit">
                    Create User
                </button>

            </form>

            {message && <p>{message}</p>}

            <h2>All Users</h2>

            <div className="users-list">

                {users.map(user => (
                    <div className="user-card" key={user.id}>

                        <h3>{user.username}</h3>

                        <p>{user.email}</p>

                        <small>User ID: {user.id}</small>

                    </div>
                ))}

            </div>

        </div>
    );
}

export default Users;