import { createContext, useContext, useMemo, useState } from 'react'
import { clearToken, getToken, login as loginRequest } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = getToken()
    return token ? { token } : null
  })

  const login = async (username, password) => {
    const data = await loginRequest(username, password)
    setUser({ token: data.token, name: data.name })
  }

  const logout = () => {
    clearToken()
    setUser(null)
  }

  const value = useMemo(() => ({ user, login, logout }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}