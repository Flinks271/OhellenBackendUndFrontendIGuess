import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './app/AuthContext'
import AppLayout from './layouts/AppLayout'
import AuthPage from './pages/AuthPage'
import FameShamePage from './pages/FameShamePage'
import GamePage from './pages/GamePage'
import LobbyListPage from './pages/LobbyListPage'
import LobbyPage from './pages/LobbyPage'
import NotFoundPage from './pages/NotFoundPage'
import UserProfilePage from './pages/UserProfilePage'

function ProtectedRoutes() {
  const { token } = useAuth()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return (
    <AppLayout>
      <Routes>
        <Route path="/lobbies" element={<LobbyListPage />} />
        <Route path="/lobbies/:lobbyId" element={<LobbyPage />} />
        <Route path="/games/:gameId" element={<GamePage />} />
        <Route path="/users/:userId" element={<UserProfilePage />} />
        <Route path="/fame-shame" element={<FameShamePage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </AppLayout>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<AuthPage />} />
          <Route path="/*" element={<ProtectedRoutes />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
