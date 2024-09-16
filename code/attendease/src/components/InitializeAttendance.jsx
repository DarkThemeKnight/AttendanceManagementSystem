import { useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";
import "../styles/initialize.css";

const BASE = "http://192.168.43.218/api/v1";

const InitializeAttendance = ({ handleClose }) => {
  const [duration, setDuration] = useState(0);
  const [subjectCode, setSubjectCode] = useState("");
  const [courses, setCourses] = useState([]);
  const { token } = useFetch();

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
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await fetch(`${BASE}/attendance/initialize`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        subjectCode,
        duration,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        console.log(data);
        handleClose();
        localStorage.setItem("code", data.metaData.attendanceCode);
      })
      .catch((error) => {
        console.log("Failed to initialize attendance", error);
      });
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Initialize attendance</legend>
          <label htmlFor="course">Course code</label>
          <select
            name="course"
            id="course"
            required
            value={subjectCode}
            onChange={(e) => setSubjectCode(e.target.value)}
          >
            <option value="" disabled>
              Select course
            </option>
            {courses.map((course, index) => (
              <option value={course.subjectId} key={`${course}${index}`}>
                {course.subjectId}
              </option>
            ))}
          </select>
          <label htmlFor="duration">
            Duration
            {/* <input
              type="time"
              name="valid-until"
              required
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
            /> */}
          </label>
          <input
            type="number"
            id="duration"
            name="duration"
            value={duration}
            min={10}
            placeholder="Enter duration in minutes"
            onChange={(e) => setDuration(e.target.value)}
          />
          <div className="btns">
            <button type="submit">Initialize attendance</button>
            <button type="button" onClick={handleClose}>
              Cancel
            </button>
          </div>
        </fieldset>
      </form>
    </div>
  );
};

export default InitializeAttendance;
