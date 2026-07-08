import { useEffect, useState } from 'react';
import Layout from './Layout.jsx';
import Message from './Message.jsx';
import ProfileImage from './ProfileImage.jsx';
import { getAuth, getUserId } from '../../utils/auth.js';
import {
  changePassword,
  deleteProfilePicture,
  getUserAccount,
  refreshStoredUser,
  updateUserAccount,
  uploadProfilePicture,
} from '../../services/userService.js';

const defaultAccount = {
  username: '',
  email: '',
  phoneNumber: '',
  profilePicture: '',
};

function AccountSettings() {
  const [account, setAccount] = useState(defaultAccount);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const userId = getUserId();

  useEffect(() => {
    async function loadAccount() {
      try {
        const user = await getUserAccount(userId);
        setAccount({
          username: user.username || '',
          email: user.email || '',
          phoneNumber: user.phoneNumber || '',
          profilePicture: user.profilePicture || '',
          role: user.role || getAuth().role,
          userId: user.userId || userId,
        });
      } catch (err) {
        const auth = getAuth();
        setAccount({ ...defaultAccount, ...auth, phoneNumber: '' });
        setError(err.userMessage || 'Unable to load account details');
      }
    }

    loadAccount();
  }, [userId]);

  const handleAccountChange = (e) => {
    setAccount({ ...account, [e.target.name]: e.target.value });
  };

  const handleAccountSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      const updated = await updateUserAccount(userId, {
        username: account.username,
        email: account.email,
        phoneNumber: account.phoneNumber,
      });
      refreshStoredUser(updated);
      setAccount({ ...account, ...updated });
      setMessage('Account details updated successfully.');
    } catch (err) {
      setError(err.userMessage || 'Unable to update account details');
    }
  };

  const handleProfilePictureChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setMessage('');
    setError('');

    try {
      const updated = await uploadProfilePicture(userId, file);
      setAccount({ ...account, ...updated });
      setMessage('Profile picture uploaded successfully.');
    } catch (err) {
      setError(err.userMessage || 'Unable to upload profile picture');
    }
  };

  const handleDeletePicture = async () => {
    setMessage('');
    setError('');

    try {
      const updated = await deleteProfilePicture(userId);
      setAccount({ ...account, ...updated });
      setMessage('Profile picture removed successfully.');
    } catch (err) {
      setError(err.userMessage || 'Unable to remove profile picture');
    }
  };

  const handlePasswordChange = (e) => {
    setPasswordForm({ ...passwordForm, [e.target.name]: e.target.value });
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('New password and confirm password must match');
      return;
    }

    try {
      await changePassword(userId, {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setMessage('Password changed successfully.');
    } catch (err) {
      setError(err.userMessage || 'Unable to change password');
    }
  };

  return (
    <Layout title="Account Settings" subtitle="Update your login profile, picture and password.">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>

      <div className="row g-4">
        <div className="col-lg-5">
          <div className="card page-card p-4 h-100">
            <div className="text-center mb-4">
              <ProfileImage src={account.profilePicture} size={120} alt={account.username} />
              <h5 className="mt-3 mb-0">{account.username}</h5>
              <small className="text-muted">{account.email}</small>
            </div>

            <label className="form-label">Upload Profile Picture</label>
            <input className="form-control" type="file" accept="image/*" onChange={handleProfilePictureChange} />
            <button className="btn btn-outline-danger w-100 mt-3" type="button" onClick={handleDeletePicture}>
              Delete Profile Picture
            </button>
          </div>
        </div>

        <div className="col-lg-7">
          <div className="card page-card p-4 mb-4">
            <h5 className="mb-3">Edit Personal Information</h5>
            <form onSubmit={handleAccountSubmit}>
              <div className="mb-3">
                <label className="form-label">Name</label>
                <input className="form-control" name="username" value={account.username} onChange={handleAccountChange} required minLength="3" />
              </div>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input className="form-control" name="email" type="email" value={account.email} onChange={handleAccountChange} required />
              </div>
              <div className="mb-3">
                <label className="form-label">Phone Number</label>
                <input className="form-control" name="phoneNumber" value={account.phoneNumber} onChange={handleAccountChange} pattern="^[6-9]\d{9}$" title="Phone number must be a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9." required />
              </div>
              <button className="btn btn-primary">Save Account Details</button>
            </form>
          </div>

          <div className="card page-card p-4">
            <h5 className="mb-3">Change Password</h5>
            <form onSubmit={handlePasswordSubmit}>
              <div className="mb-3">
                <label className="form-label">Current Password</label>
                <input className="form-control" type="password" name="currentPassword" value={passwordForm.currentPassword} onChange={handlePasswordChange} required />
              </div>
              <div className="mb-3">
                <label className="form-label">New Password</label>
                <input className="form-control" type="password" name="newPassword" value={passwordForm.newPassword} onChange={handlePasswordChange} required minLength="8" maxLength="20" pattern="^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).+$" title="New password must contain at least one uppercase letter, one lowercase letter, and one number." />
                <small className="text-muted">Use uppercase, lowercase and number.</small>
              </div>
              <div className="mb-3">
                <label className="form-label">Confirm New Password</label>
                <input className="form-control" type="password" name="confirmPassword" value={passwordForm.confirmPassword} onChange={handlePasswordChange} required />
              </div>
              <button className="btn btn-primary">Change Password</button>
            </form>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default AccountSettings;
