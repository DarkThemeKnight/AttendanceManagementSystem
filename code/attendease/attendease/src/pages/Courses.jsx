import { useNavigate } from "react-router-dom";
import CourseList from "../components/CourseList";
import { useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";

const Courses = ({ BASE }) => {
  const navigate = useNavigate();
  const [courses, setCourses] = useState([]);
  const { token } = useFetch();

  useEffect(() => {
    const getCourses = async () => {
      await fetch(`${BASE}/admin/subject?student=false`, {
        mode: "cors",
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((response) => response.json())
        .then((data) => {
          setCourses(data.data);
        })
        .catch((error) => {
          console.error("Failed to fetch lecturers:", error);
        });
    };

    getCourses();
  }, [token, BASE]);

  const handleClick = () => {
    navigate("./add");
  };

  return (
    <div className="layout">
      <div className="add">
        <h2>Courses</h2>
        <button onClick={handleClick}>Add Course</button>
      </div>
      <CourseList courses={courses} />
    </div>
  );
};

export default Courses;
