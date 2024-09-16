import { useState } from "react";
import "../styles/form.css";
import { useNavigate } from "react-router-dom";
import postReq from "../logic/postReq";

const Form = ({ setJwtToken }) => {
  const [category, setCategory] = useState("lecturer");
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(""); // Added for error handling
  const navigate = useNavigate();

  const handleCategoryChange = (e) => {
    setCategory(e.target.value);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setError("");
    postReq(userId, password, category, navigate, setError, setJwtToken); // Pass setJwtToken function
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Login to your dashboard</legend>
          <label htmlFor="id">ID:</label>
          <input
            type="text"
            id="id"
            name="id"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
            placeholder="Enter your id"
          />
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            name="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="Enter your password"
          />
          <fieldset className="radio">
            <legend>Select your role</legend>
            <label>
              <input
                type="radio"
                name="role"
                value="lecturer"
                checked={category === "lecturer"}
                onChange={handleCategoryChange}
              />
              Lecturer
            </label>
            <label>
              <input
                type="radio"
                value="admin"
                name="role"
                checked={category === "admin"}
                onChange={handleCategoryChange}
              />
              Admin
            </label>
            <label>
              <input
                type="radio"
                value="student"
                name="role"
                checked={category === "student"}
                onChange={handleCategoryChange}
              />
              Student
            </label>
          </fieldset>
          {error && <p className="error">{error}</p>}{" "}
          {/* Display error message */}
          <button type="submit">Login</button>
        </fieldset>
      </form>
    </div>
  );
};

export default Form;
