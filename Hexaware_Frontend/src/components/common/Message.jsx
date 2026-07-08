function Message({ type = 'info', children }) {
  if (!children) return null;
  return <div className={`alert alert-${type} my-3`}>{children}</div>;
}

export default Message;
