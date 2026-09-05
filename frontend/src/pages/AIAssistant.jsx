import { useEffect, useState } from "react";
import ReactMarkdown from "react-markdown";
import { streamAIMessage, getAIProvider, } from "../services/api";

export default function AIAssistant() {

    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [provider, setProvider] = useState("");

    useEffect(() => {
        const loadProvider = async () => {
            try {
                const data = await getAIProvider();
                setProvider(data.provider);
            } catch (err) {
                console.error("Failed to load AI provider:", err);
                setProvider("unknown");
            }
        };

        loadProvider();
    }, []);

    const developerTools = {
        explain: `Explain the following code clearly.

    Break down what the code does, explain the important parts, and mention any potential improvements.

    Code:

    `,

        debug: `Debug the following code.

    Identify the problem, explain why it happens, and provide the corrected code.

    Code:

    `,

        generate: `Generate clean, production-ready code for the following requirement.

    Explain the approach briefly and then provide the complete code.

    Requirement:

    `,

        optimize: `Analyze the following code for performance, readability, and maintainability.

    Suggest improvements and provide an optimized version of the code.

    Code:

    `,

        tests: `Write comprehensive unit tests for the following code.

    Include important edge cases and explain what each test verifies.

    Code:

    `,
    };

    const handleDeveloperTool = (tool) => {
        setInput(developerTools[tool]);
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        const trimmedInput = input.trim();

        if (!trimmedInput || loading) {
            return;
        }

        const userMessage = {
            role: "user",
            content: trimmedInput,
        };

        const updatedMessages = [
            ...messages,
            userMessage,
        ];

        setMessages(updatedMessages);
        setInput("");
        setLoading(true);
        setError("");

        try {

            const assistantMessage = {
                role: "assistant",
                content: "",
                streaming: true,
            };

            setMessages([
                ...updatedMessages,
                assistantMessage,
            ]);

            await streamAIMessage(
                updatedMessages,
                (chunk) => {

                    setMessages((currentMessages) => {

                        const updated = [...currentMessages];

                        const lastMessageIndex =
                            updated.length - 1;

                        updated[lastMessageIndex] = {
                            ...updated[lastMessageIndex],
                            content:
                                updated[lastMessageIndex].content + chunk,
                        };

                        return updated;
                    });
                }
            );

            setMessages((currentMessages) => {
                const updated = [...currentMessages];

                const lastMessageIndex = updated.length - 1;

                updated[lastMessageIndex] = {
                    ...updated[lastMessageIndex],
                    streaming: false,
                };

                return updated;
            });

        } catch (err) {

            console.error(err);

            setError(
                "Unable to connect to DevMind AI."
            );

        } finally {

            setLoading(false);
        }
    };

    const clearChat = () => {
        setMessages([]);
        setInput("");
        setError("");
    };

    return (
        <div className="page-container">

            <div className="page-header">
                <h1>🤖 AI Assistant</h1>

                <p>
                    Ask DevMind AI about coding, debugging and software development.
                </p>
            </div>

            <div className="ai-card">

                <div className="ai-status">
                    <span className="status-dot"></span>
                    {provider === "gemini"
                        ? "Gemini AI"
                        : provider === "ollama"
                            ? "Local AI • Ollama"
                            : "AI Provider"}
                </div>

                {/* Chat Messages */}

                <div className="chat-messages">

                    {messages.length === 0 && (
                        <div className="chat-empty">
                            <div className="chat-empty-icon">🤖</div>

                            <h3>Welcome to DevMind AI</h3>

                            <p>
                                Ask me about Java, Spring Boot, Python,
                                debugging, coding and more.
                            </p>
                        </div>
                    )}

                    {messages.map((message, index) => (

                        <div
                            key={index}
                            className={`chat-message ${message.role}`}
                        >

                            <div className="chat-message-label">
                                {message.role === "user"
                                    ? "👤 You"
                                    : "🤖 DevMind AI"}
                            </div>

                            <div className="chat-message-content">
                                {message.role === "assistant" ? (
                                    message.streaming ? (
                                        <div className="streaming-content">
                                            {message.content}
                                        </div>
                                    ) : (
                                        <div className="markdown-content">
                                            <ReactMarkdown>
                                                {message.content}
                                            </ReactMarkdown>
                                        </div>
                                    )
                                ) : (
                                    message.content
                                )}
                            </div>

                        </div>

                    ))}

                    {loading && (
                        <div className="chat-message assistant">

                            <div className="chat-message-label">
                                🤖 DevMind AI
                            </div>

                            <div className="chat-message-content">
                                Thinking...
                            </div>

                        </div>
                    )}

                </div>

                {/* Input */}

                <form onSubmit={handleSubmit} className="ai-chat-form">

                    <div className="ai-developer-tools">

                        <button
                            type="button"
                            onClick={() => handleDeveloperTool("explain")}
                            disabled={loading}
                        >
                            🧠 Explain Code
                        </button>

                        <button
                            type="button"
                            onClick={() => handleDeveloperTool("debug")}
                            disabled={loading}
                        >
                            🐞 Debug Code
                        </button>

                        <button
                            type="button"
                            onClick={() => handleDeveloperTool("generate")}
                            disabled={loading}
                        >
                            ⚡ Generate Code
                        </button>

                        <button
                            type="button"
                            onClick={() => handleDeveloperTool("optimize")}
                            disabled={loading}
                        >
                            🚀 Optimize Code
                        </button>

                        <button
                            type="button"
                            onClick={() => handleDeveloperTool("tests")}
                            disabled={loading}
                        >
                            🧪 Write Tests
                        </button>

                    </div>

                    <textarea
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        placeholder="Ask DevMind AI..."
                        rows="3"
                        disabled={loading}
                    />

                    <div className="ai-chat-actions">

                        <button
                            type="button"
                            onClick={clearChat}
                            disabled={messages.length === 0 || loading}
                        >
                            Clear Chat
                        </button>

                        <button
                            type="submit"
                            disabled={!input.trim() || loading}
                        >
                            {loading ? "Thinking..." : "Ask DevMind AI"}
                        </button>

                    </div>

                </form>

                {error && (
                    <div className="ai-error">
                        ⚠️ {error}
                    </div>
                )}

            </div>

        </div>
    );
}