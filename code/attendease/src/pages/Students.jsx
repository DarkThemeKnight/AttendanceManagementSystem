import { useNavigate } from "react-router-dom";
import StudentsList from "../components/StudentsList";
import { useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";

const Students = ({ BASE }) => {
  const [students, setStudents] = useState([]);
  const [courses, setCourses] = useState([]);
  const [subjectCode, setSubjectCode] = useState("MAT101");
  const navigate = useNavigate();
  const { token } = useFetch();

  useEffect(() => {
    const getStudents = async () => {
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

    getStudents();
  }, [token, subjectCode, BASE]);

  const handleClick = () => {
    navigate("./add");
  };

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
        console.log(data);
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

  useEffect(() => {
    const getCourses = async () => {
      await fetch(`${BASE}/attendance/mySubjects`, {
        mode: "cors",
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((response) => response.json())
        .then((data) => {
          setCourses(data.data);
        })
        .catch((error) => {
          console.error("Failed to fetch courses:", error);
        });
    };

    getCourses();
  }, [token, BASE]);

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
    </div>
  );
};

export default Students;
