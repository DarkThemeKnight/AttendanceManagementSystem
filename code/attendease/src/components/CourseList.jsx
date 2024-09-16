import { useState } from "react";

const CourseList = ({ courses }) => {
  const [searchQuery, setSearchQuery] = useState("");

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const filteredCourses = courses.filter((course) => {
    const fullName = `${course.subject_title}`.toLowerCase();

    return (
      fullName.includes(searchQuery.toLowerCase()) ||
      course.subject_code.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <section className="tables">
      <div className="caption">
        <h3>Current Courses</h3>
        <input
          type="search"
          name="search"
          id="search"
          placeholder="Search..."
          value={searchQuery}
          onChange={handleSearchChange}
        />
      </div>
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Course Code</th>
            <th>Course Title</th>
            {/* <th>Lecturer Name</th> */}
            <th>Lecturer ID</th>
          </tr>
        </thead>
        <tbody>
          {filteredCourses.map((course, index) => (
            <tr key={`${course[0]}${index}${course[1]}`}>
              <td>{index + 1}</td>
              <td>{course.subject_code}</td>
              <td>{course.subject_title}</td>
              <td>{course.id_lecturer_in_charge}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
};

export default CourseList;
