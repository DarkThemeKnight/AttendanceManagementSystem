import { useNavigate } from "react-router-dom";
import StudentsList from "../components/StudentsList";
import { useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";

const Students = ({ BASE }) => {
  const [students, setStudents] = useState([]);
  const [courses, setCourses] = useState([]);
  const [subjectCode, setSubjectCode] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [showBulk, setShowBulk] = useState(false);
  const [csvFile, setCsvFile] = useState(null);
  const [csvFileName, setCsvFileName] = useState(""); // State to hold file name
  const navigate = useNavigate();
  const { token } = useFetch();

  // Moved the getStudents function out of useEffect
  const getStudents = async () => {
    if (!subjectCode) return;

    await fetch(`${BASE}/attendance?subjectCode=${subjectCode}`, {
      mode: "cors",
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((response) => response.json())
      .then((data) => {
        setStudents(data.students);
      })
      .catch((error) => {
        console.error("Failed to fetch students:", error);
      });
  };

  // Fetch students when the subjectCode changes
  useEffect(() => {
    getStudents();
  }, [token, subjectCode, BASE]);

  const handleClick = () => {
    setShowModal(true);
    setShowBulk(false);
  };

  const handleSingleStudentClick = () => {
    setShowModal(false);
    navigate("./add");
  };

  const handleBulkStudentClick = () => {
    setShowBulk(true);
  };

  const handleCsvFileChange = (e) => {
    const file = e.target.files[0];
    setCsvFile(file);
    setCsvFileName(file ? file.name : ""); // Set the file name when a file is chosen
  };

  // Updated handleCsvUpload to call getStudents after successful upload
  const handleCsvUpload = async () => {
    if (!csvFile) return alert("Please upload a CSV file.");

    const formData = new FormData();
    formData.append("file", csvFile);

    await fetch(`${BASE}/attendance/add/bulk`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
    })
      .then((response) => {
        if (response.ok) {
          console.log("CSV file uploaded successfully.");
          return response.json();
        } else {
          alert("Failed to upload CSV file.");
        }
      })
      .then((data) => {
        console.log(data);
        setCsvFileName("");
        setShowModal(false);

        // Call getStudents after successful CSV upload
        getStudents();
      })
      .catch((error) => {
        console.error("Failed to upload CSV file:", error);
      });
  };

  // Handle suspending the student
  const handleSuspend = async (studentId, suspend) => {
    await fetch(
      `${BASE}/attendance/suspend?subjectCode=${subjectCode}&studentId=${studentId}&suspend=${suspend}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          subjectCode,
          studentId,
          suspend,
        }),
      }
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("Student not found");
        }
        return response.json();
      })
      .then((data) => {
        setStudents((prevStudents) =>
          prevStudents.map((student) =>
            student.studentId === studentId
              ? { ...student, suspended: suspend }
              : student
          )
        );
      })
      .catch((error) => {
        console.log("Error: ", error);
      });
  };

  // Fetch the courses and set the default subjectCode
  useEffect(() => {
    const getCourses = async () => {
      await fetch(`${BASE}/attendance/mySubjects`, {
        mode: "cors",
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((response) => response.json())
        .then((data) => {
          setCourses(data.data);
          // Set the subjectCode to the first course subjectId if available
          if (data.data.length > 0 && !subjectCode) {
            setSubjectCode(data.data[0].subjectId);
          }
        })
        .catch((error) => {
          console.error("Failed to fetch courses:", error);
        });
    };

    getCourses();
  }, [token, BASE, subjectCode]);

  return (
    <div className="layout">
      <div className="add">
        <h2>Students</h2>
        <label>
          Select Subject:
          <select
            name="subject"
            value={subjectCode}
            onChange={(e) => setSubjectCode(e.target.value)}
          >
            {courses.map((course, index) => (
              <option value={course.subjectId} key={`${course}${index}`}>
                {course.subjectId}
              </option>
            ))}
          </select>
        </label>
        <button onClick={handleClick}>Add Student</button>
      </div>
      <StudentsList students={students} handleSuspend={handleSuspend} />

      {/* Modal for adding user options */}
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            {!showBulk && (
              <>
                <h3>Select an option</h3>
                <button
                  className="modal-btn"
                  onClick={handleSingleStudentClick}
                >
                  Add Single Student
                </button>
                <button className="modal-btn" onClick={handleBulkStudentClick}>
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

export default Students;
