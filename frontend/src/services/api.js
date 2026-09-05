//const API_BASE_URL = 'http://localhost:8080/api';
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ||
    'http://localhost:8080/api';

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

export const sendAIMessage = async (messages) => {
    const response = await fetch(`${API_BASE_URL}/ai/chat`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            messages: messages,
        }),
    });

    if (!response.ok) {
        throw new Error("Failed to get AI response");
    }

    return response.json();
};

export const getAIProvider = async () => {
    const response = await fetch(`${API_BASE_URL}/ai/provider`);

    if (!response.ok) {
        throw new Error("Failed to get AI provider");
    }

    return response.json();
};

export const streamAIMessage = async (messages, onChunk) => {

    const response = await fetch(
        `${API_BASE_URL}/ai/chat/stream`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                messages: messages,
            }),
        }
    );

    if (!response.ok) {
        throw new Error("Failed to connect to AI");
    }

    if (!response.body) {
        throw new Error("Streaming is not supported");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");

    let buffer = "";

    const processEvent = (event) => {

        const lines = event.split("\n");
        const dataLines = [];

        for (const line of lines) {

            if (line.startsWith("data:")) {

                let data = line.substring(5);

                // Remove only the optional SSE separator space.
                if (data.startsWith(" ")) {
                    data = data.substring(1);
                }

                dataLines.push(data);
            }
        }

        if (dataLines.length === 0) {
            return;
        }

        const data = dataLines.join("\n");

        if (!data.trim()) {
            return;
        }

        try {

            const payload = JSON.parse(data);

            if (payload.content !== undefined) {
                onChunk(payload.content);
            }

        } catch (error) {

            console.error(
                "Invalid SSE JSON:",
                data
            );
        }
    };

    while (true) {

        const { value, done } = await reader.read();

        if (done) {
            break;
        }

        buffer += decoder.decode(value, {
            stream: true,
        });

        const events = buffer.split("\n\n");

        buffer = events.pop() || "";

        for (const event of events) {
            processEvent(event);
        }
    }

    // Process any remaining event
    if (buffer.trim()) {
        processEvent(buffer);
    }
};