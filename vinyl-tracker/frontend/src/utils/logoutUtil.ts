let logoutCallback: (() => void) | null = null;

export const setLogoutCallback = (cb: () => void) => {
  logoutCallback = cb;
};

export const triggerLogout = () => {
  if (logoutCallback) {
    logoutCallback();
  } else {
    localStorage.clear();
    window.location.replace('/login');
  }
};
