import { useState } from "react";
import useFetch from "../hooks/useFetch";
import "../styles/initialize.css";
import Modal from "./Modal";

const Reset = ({ handleClose, BASE }) => {
  const [old, setOld] = useState("");
  const [newP, setNewP] = useState("");
  const [error, setError] = useState(null);
  const [heading, setHeading] = useState(null);
  const [text, setText] = useState(null);
  const { token } = useFetch();

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log(BASE);

    await fetch(`${BASE}/auth/updatePassword`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        oldPassword: old,
        newPassword: newP,
      }),
    })
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        console.log(data);
        if (data.message === "User does not exist") {
          throw new Error("Password change failed, User not found");
        }
        setHeading("Password changed successfully!");
        setText("Your password has been updated successfully!");
      })
      .catch((error) => {
        console.log("Failed to change password", error);
        setError(error.message);
      });
  };

  return (
    <div className="form">
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Change Password</legend>
          <label htmlFor="old">Old password</label>
          <input
            type="text"
            id="old"
            name="old"
            value={old}
            required
            placeholder="Enter your old password"
            onChange={(e) => setOld(e.target.value)}
          />

          <label htmlFor="duration">New password</label>
          <input
            type="text"
            id="duration"
            name="duration"
            value={newP}
            min={8}
            required
            placeholder="Enter new password"
            onChange={(e) => setNewP(e.target.value)}
          />
          {error && <p className="error">{error}</p>}
          <div className="btns">
            <button type="submit">Change Now</button>
            <button type="button" onClick={handleClose}>
              Cancel
            </button>
          </div>
        </fieldset>
      </form>
      {text && (
        <Modal text={text} handleClose={handleClose} heading={heading} />
      )}
    </div>
  );
};

export default Reset;
