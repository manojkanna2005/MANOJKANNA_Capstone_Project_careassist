import AppRoutes from './routes/AppRoutes.jsx';
import { useValidationFeedback } from './utils/validationFeedback.js';

function App() {
  useValidationFeedback();
  return <AppRoutes />;
}

export default App;
