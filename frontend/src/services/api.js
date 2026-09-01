const API_BASE_URL = 'http://localhost:8080/api';

export const getHealth = async () => {
    const response = await fetch(`${API_BASE_URL}/health`);

    if (!response.ok) {
        throw new Error('Backend request failed');
    }

    return response.text();
};

export const getUsers = async () => {
    const response = await fetch(`${API_BASE_URL}/users`);

    if (!response.ok) {
        throw new Error('Failed to fetch users');
    }

    return response.json();
};

export const getProjects = async () => {
    const response = await fetch(`${API_BASE_URL}/projects`);

    if (!response.ok) {
        throw new Error('Failed to fetch projects');
    }

    return response.json();
};

export const createUser = async (user) => {
    const response = await fetch(`${API_BASE_URL}/users`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(user),
    });

    if (!response.ok) {
        throw new Error('Failed to create user');
    }

    return response.json();
};