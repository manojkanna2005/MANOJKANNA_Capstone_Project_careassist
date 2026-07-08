function ProfileImage({ src, size = 46, alt = "Profile" }) {
  const imageSrc = src && src.trim() !== "" ? src : "/favicon.svg";

  return (
    <img
      src={imageSrc}
      alt={alt}
      style={{
        width: `${size}px`,
        height: `${size}px`,
        objectFit: "cover",
        borderRadius: "50%",
        border: "2px solid #ffffff",
        backgroundColor: "#ffffff",
      }}
    />
  );
}

export default ProfileImage;
