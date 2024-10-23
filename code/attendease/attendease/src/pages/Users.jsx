import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import useFetch from "../hooks/useFetch";
import LecturersList from "../components/LecturersList";
import "../styles/users.css";

const Users = ({ BASE }) => {
  const navigate = useNavigate();
  const [lecturers, setLecturers] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showBulk, setShowBulk] = useState(false);
  const [csvFile, setCsvFile] = useState(null);
  const [csvFileName, setCsvFileName] = useState(""); // State to hold file name
  const { token } = useFetch();

  useEffect(() => {
    const getLecturers = async () => {
      await fetch(`${BASE}/admin/instructor`, {
        mode: "cors",
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((response) => response.json())
        .then((data) => {
          setLecturers(data.data);
        })
        .catch((error) => {
          console.error("Failed to fetch lecturers:", error);
        });
    };

    getLecturers();
  }, [token, BASE]);

  const handleAddClick = () => {
    setShowModal(true);
    setShowBulk(false);
  };

  const handleSingleUserClick = () => {
    setShowModal(false);
    navigate("./add");
  };

  const handleBulkUserClick = () => {
    setShowBulk(true);
  };

  const handleCsvFileChange = (e) => {
    const file = e.target.files[0];
    setCsvFile(file);
    setCsvFileName(file ? file.name : ""); // Set the file name when a file is chosen
  };

  const handleCsvUpload = async () => {
    if (!csvFile) return alert("Please upload a CSV file.");

    const formData = new FormData();
    formData.append("file", csvFile);

    await fetch(`${BASE}/admin/register/bulk`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    })
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          alert("Failed to upload CSV file.");
        }
      })
      .then((data) => {
        console.log("CSV file uploaded successfully.", data);
        setCsvFileName("");
        setShowModal(false);
      })
      .catch((error) => {
        console.error("Failed to upload CSV file:", error);
      });
  };

  return (
    <div className="layout">
      <div className="add">
        <h2>Lecturers</h2>
        <button onClick={handleAddClick}>Add User</button>
      </div>

      <LecturersList lecturers={lecturers} />

      {/* Modal for adding user options */}
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            {!showBulk && (
              <>
                <h3>Select an option</h3>
                <button className="modal-btn" onClick={handleSingleUserClick}>
                  Add Single User
                </button>
                <button className="modal-btn" onClick={handleBulkUserClick}>
                  Bulk Upload CSV
                </button>
              </>
            )}

            {showBulk && (
              <div className="csv-upload">
                <p>
                  <strong>Required CSV Format:</strong>
                  <br />
                  <code>id,firstname,lastname,email,phoneNumber,role</code>
                </p>
                <input
                  type="file"
                  accept=".csv"
                  onChange={handleCsvFileChange}
                  style={{ display: csvFileName ? "none" : "block" }}
                />
                {csvFileName && <p>File selected: {csvFileName}</p>}
                <button className="upload-btn" onClick={handleCsvUpload}>
                  Upload CSV
                </button>
              </div>
            )}
            <button
              className="close-modal"
              onClick={() => {
                setShowModal(false);
                setShowBulk(false);
                setCsvFileName("");
              }}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Users;
