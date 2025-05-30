import { useState } from "react";

interface EmployeePairResult {
    empId1: number;
    empId2: number;
    projectId: number;
    daysWorked: number;
}

export default function UploadAndTable() {
    const [file, setFile] = useState<File | null>(null);
    const [rows, setRows] = useState<EmployeePairResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const selectedFile = event.target.files?.[0] || null;
        setFile(selectedFile);
        setError(null);
    };

    const handleUpload = async () => {
        if (!file) return;
        setLoading(true);
        setError(null);

        const formData = new FormData();
        formData.append("file", file);

        try {
            const baseUrl = import.meta.env.VITE_API_BASE_URL;
            const response = await fetch(`${baseUrl}/api/upload`, {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                throw new Error("Upload failed");
            }

            const data: EmployeePairResult[] = await response.json();
            setRows(data);
        } catch (err) {
            console.error("Upload error", err);
            setError("Failed to upload file");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: 800, margin: "2rem auto", padding: "1rem" }}>
            <h2>Upload CSV and Discover Employee Collaboration Durations</h2>
            <input type="file" accept=".csv" onChange={handleFileChange} />
            <button onClick={handleUpload} disabled={!file || loading} style={{ marginLeft: "1rem" }}>
                {loading ? "Uploading..." : "Upload"}
            </button>

            {error && <p style={{ color: "red" }}>{error}</p>}

            {rows.length > 0 && (
                <table style={{ width: "100%", marginTop: "1rem", borderCollapse: "collapse" }}>
                    <thead>
                    <tr>
                        <th style={th}>Employee ID #1</th>
                        <th style={th}>Employee ID #2</th>
                        <th style={th}>Days worked</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rows.map((row, i) => (
                        <tr key={i}>
                            <td style={td}>{row.empId1}</td>
                            <td style={td}>{row.empId2}</td>
                            <td style={td}>{row.daysWorked}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

const th: React.CSSProperties = {
    border: "1px solid #ccc",
    padding: "0.5rem",
    backgroundColor: "#f4f4f4",
    textAlign: "left",
};

const td: React.CSSProperties = {
    border: "1px solid #ccc",
    padding: "0.5rem",
};
