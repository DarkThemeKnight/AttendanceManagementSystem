import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import useFetch from "../hooks/useFetch";
import Modal from "./Modal";

const AddCourse = ({ BASE }) => {
  const navigate = useNavigate();
  const { token } = useFetch();
  const [subjectCode, setSubjectCode] = useState("");
  const [title, setTitle] = useState("");
  const [text, setText] = useState("");
  const [heading, setHeading] = useState("");
  const [lecturerInCharge, setLecturerInCharge] = useState("");
  const [lecturers, setLecturers] = useState([]);

  const handleClick = (e) => {
    e.preventDefault();
    if (title && subjectCode && lecturerInCharge) {
      addCourse();
    }
  };

  const handleClose = () => {
    setText(null);
    navigate(-1);
  };
  const addCourse = async () => {
    await fetch(`${BASE}/admin/add-subject`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },

      body: JSON.stringify({
        subject_code: subjectCode,
        subject_title: title,
        id_lecturer_in_charge: lecturerInCharge,
      }),
    })
      .then((response) => {
        if (response.status !== 200) {
          throw new Error(`${title} Already Exists!`);
        }
        response.json();
      })
      .then((data) => {
        console.log(data);
        setHeading("Course added successfully!");
        setText(`${title} (${subjectCode}) has been added successfully!`);
      })
      .catch((error) => {
        console.log("Failed to add course", error);
        setHeading("Course not added!");
        setText(error.message);
      });
  };

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

  return (
    <div className="layout">
      <h2>Add Course</h2>

      <form onSubmit={handleClick}>
        <fieldset>
          <legend>Add a Course</legend>
          <label htmlFor="course-code">Course Code</label>
          <input
            type="text"
            name="course-code"
            id="course-code"
            required
            placeholder="Add Course code"
            value={subjectCode}
            onChange={(e) => setSubjectCode(e.target.value)}
          />
          <label htmlFor="title">Course Title</label>
          <input
            type="text"
            name="course-title"
            id="title"
            required
            placeholder="Add Course title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <label htmlFor="lecturer-id">Lecturer in charge</label>
          <select
            name="lecturer-id"
            id="lecturer-id"
            defaultValue={lecturerInCharge}
            onChange={(e) => setLecturerInCharge(e.target.value)}
            required
          >
            <option value="" disabled>
              Enter ID of the Lecturer
            </option>
            {lecturers.map((lecturer) => (
              <option value={lecturer.id} key={lecturer.id}>
                {lecturer.id}
              </option>
            ))}
          </select>
          <button>Add Now</button>
        </fieldset>
      </form>
      {text && (
        <Modal text={text} handleClose={handleClose} heading={heading} />
      )}
    </div>
  );
};

export default AddCourse;
