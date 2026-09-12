import axios from 'axios'

function loginPathForCurrentPage() {
  const path = window.location.pathname
  if (path === '/aqiFeedback') return '/neps/login'
  if (path.startsWith('/nepg/')) return '/nepg/login'
  if (path.startsWith('/nepm/')) return '/nepm/login'
  if (path.startsWith('/nepv/')) return '/nepv/login'
  return null
}

export const request = axios.create({ baseURL: '/api', timeout: 5000 })

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const loginPath = loginPathForCurrentPage()
      if (loginPath && window.location.pathname !== loginPath) window.location.assign(loginPath)
    }
    return Promise.reject(error)
  },
)
