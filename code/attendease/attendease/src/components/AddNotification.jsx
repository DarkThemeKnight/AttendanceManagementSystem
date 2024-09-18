import { useState } from "react";
import useFetch from "../hooks/useFetch";

const AddNotification = ({ isOpen, handleClose, handleNotification, BASE }) => {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [validUntil, setValidUntil] = useState("");
  const { token } = useFetch();

  const handleClick = async () => {
    await fetch(`${BASE}/admin/notification`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        title,
        content,
        validUntil,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        console.log(data);
        handleNotification();
        handleClose();
      })
      .catch((error) => {
        console.log("Failed to add notification", error);
      });
  };

  return (
    <div className="form">
      <form>
        <fieldset>
          <legend>Add a Notification</legend>
          <label htmlFor="title">Title</label>
          <input
            type="text"
            name="title"
            id="title"
            required
            placeholder="Add Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <label htmlFor="content">Content</label>
          <textarea
            name="content"
            required
            id="content"
            placeholder="Add Content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
          <label htmlFor="date">Valid Until</label>
          <input
            type="date"
            name="valid-until"
            id="date"
            required
            value={validUntil}
            onChange={(e) => setValidUntil(e.target.value)}
          />
          <div className="btns">
            <button type="button" onClick={handleClick}>
              Add Notification
            </button>
            <button type="button" onClick={handleClose}>
              Cancel
            </button>
          </div>
        </fieldset>
      </form>
    </div>
  );
};

export default AddNotification;
