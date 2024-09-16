import React, { useState } from "react";
import "../styles/student.css";
import "../styles/modal.css";
import account from "../asset/account.svg";
import useFetch from "../hooks/useFetch";
import Modal from "../components/Modal";

const Student = ({ BASE }) => {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [text, setText] = useState(null);
  const [heading, setHeading] = useState(null);
  const { token } = useFetch();

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    setFile(selectedFile);
    setPreview(URL.createObjectURL(selectedFile));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (file) {
      const formData = new FormData();
      formData.append("file", file);

      await fetch(`${BASE}/students/image`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Invalid number of faces, please try again!");
          }
          response.json();
        })
        .then((data) => {
          console.log("File uploaded successfully:", data);
          setHeading("File uploaded successfully!");
          setText("Your image has been uploaded successfully!");
        })
        .catch((error) => {
          console.error("Error uploading file:", error);
          setHeading("Image rejected!");
          setText(error.message);
          // Handle error here
        });
    } else {
      setHeading("Can't Upload image");
      setText("No file selected!");
    }
  };

  const handleClose = () => {
    setText(null);
  };
  return (
    <div className="student layout">
      <div className="form">
        <form onSubmit={handleSubmit}>
          <div className="image">
            {!preview && <img src={account} alt="Preview" />}
            {preview && <img src={preview} alt="Preview" />}
          </div>
          <fieldset>
            <label>
              Upload an image
              <input
                type="file"
                name="file"
                accept=".png, .jpg, .jpeg"
                onChange={handleFileChange}
              />
            </label>
            <button type="submit">Upload</button>
          </fieldset>
        </form>
      </div>
      {text && (
        <Modal text={text} handleClose={handleClose} heading={heading} />
      )}
    </div>
  );
};

export default Student;
