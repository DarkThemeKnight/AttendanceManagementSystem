import { useState } from "react";

const StudentsList = ({ students, handleSuspend }) => {
  const [searchQuery, setSearchQuery] = useState("");

  // Function to handle search input change
  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  // Function to filter students based on search query
  const filteredStudents = students.filter((student) => {
    const fullName = `${student.firstname} ${student.lastname}`.toLowerCase();
    return (
      fullName.includes(searchQuery.toLowerCase()) ||
      student.studentId.toString().includes(searchQuery)
    );
  });

  return (
    <section className="tables">
      <div className="caption">
        <h3>Current Students</h3>
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
            <th>Student Name</th>
            <th>Student ID</th>
            <th>Score</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredStudents.map((student, index) => (
            <tr key={`${student.studentId}-${index}`}>
              <td>{index + 1}</td>
              <td>{student.firstname + " " + student.lastname}</td>
              <td>{student.studentId}</td>
              <td
                style={{
                  color: student.percentage < 70 ? "red" : "green",
                  fontWeight: "bold",
                }}
              >
                {student.percentage} %
              </td>
              <td className="table-btn">
                {!student.suspended ? (
                  <button
                    onClick={() => handleSuspend(student.studentId, true)}
                  >
                    Suspend
                  </button>
                ) : (
                  <button
                    onClick={() => handleSuspend(student.studentId, false)}
                    style={{
                      backgroundColor: "#33CC66",
                      borderColor: "#33CC66",
                    }}
                  >
                    Restore
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
};

export default StudentsList;
