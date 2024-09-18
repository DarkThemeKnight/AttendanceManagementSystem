const Modal = ({ heading, text, handleClose }) => {
  return (
    <div className="form modal">
      <div className="card">
        <h3>{heading}</h3>
        <p>{text}</p>
        <button onClick={handleClose}>close</button>
      </div>
    </div>
  );
};

export default Modal;
