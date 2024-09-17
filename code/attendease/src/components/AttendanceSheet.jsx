import { useState, useEffect } from "react";

const AttendanceSheet = ({ students, setSortID }) => {
  const [sortOrder, setSortOrder] = useState("all");
  const [filteredStudents, setFilteredStudents] = useState(students);

  useEffect(() => {
    let updatedStudents;
    if (sortOrder === "present") {
      setSortID(1);
      updatedStudents = students.filter(
        (student) => student.status === "PRESENT"
      );
    } else if (sortOrder === "absent") {
      setSortID(2);
      updatedStudents = students.filter(
        (student) => student.status === "ABSENT"
      );
    } else {
      updatedStudents = students;
      setSortID(0);
    }
    setFilteredStudents(updatedStudents);
  }, [sortOrder, students, setSortID]);

  return (
    <section className="tables">
      <div className="caption">
        <h3>Attendance Record</h3>
        <label className="sort">
          <select
            name="order"
            value={sortOrder}
            onChange={(e) => setSortOrder(e.target.value)}
          >
            <option value="all">All</option>
            <option value="absent">Absent</option>
            <option value="present">Present</option>
          </select>
        </label>
      </div>
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Student Name</th>
            <th>Matric Number</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {filteredStudents.length > 0 ? (
            filteredStudents.map((student, index) => (
              <tr key={`${student.id}${index}`}>
                <td>{index + 1}</td>
                <td>{student.firstname + " " + student.lastname}</td>
                <td>{student.matriculationNumber}</td>
                <td>{student.status}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="4">No records found</td>
            </tr>
          )}
        </tbody>
      </table>
    </section>
  );
};

export default AttendanceSheet;
