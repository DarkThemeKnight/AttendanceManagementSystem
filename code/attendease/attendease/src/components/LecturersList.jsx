import { useState } from "react";

const LecturersList = ({ lecturers }) => {
  const [searchQuery, setSearchQuery] = useState("");

  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const filteredUsers = lecturers.filter((lecturer) => {
    const fullName = `${lecturer.firstName} ${lecturer.lastName}`.toLowerCase();

    return (
      fullName.includes(searchQuery.toLowerCase()) ||
      lecturer.id.toString().includes(searchQuery)
    );
  });

  return (
    <section className="tables">
      <div className="caption">
        <h3>Current Lecturers</h3>
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
            <th>Lecturer Name</th>
            <th>Email</th>
            <th>Lecturer ID</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredUsers.map((lecturer, index) => (
            <tr key={lecturer.id}>
              <td>{index + 1}</td>
              <td>{lecturer.firstName + " " + lecturer.lastName}</td>
              <td>{lecturer.schoolEmail}</td>
              <td>{lecturer.id}</td>
              <td>{lecturer.accountStatus}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
};

export default LecturersList;
