import { useState } from "react";
import ReactMarkdown from "react-markdown";
import { streamAIMessage } from "../services/api";

export default function AIAssistant() {

    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

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
                    Local AI • Ollama
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
                                    <ReactMarkdown>
                                        {message.content}
                                    </ReactMarkdown>
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