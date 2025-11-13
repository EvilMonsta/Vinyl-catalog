import { createTheme, alpha } from '@mui/material/styles';

type Accent = 'teal' | 'blue' | 'purple';

const accents: Record<Accent, string> = {
  teal:   '#2dd4bf', // мягкая бирюза
  blue:   '#60a5fa',
  purple: '#a78bfa',
};

export const calmTheme = (accent: Accent = 'teal') => {
  const primary = accents[accent];

  return createTheme({
    palette: {
      mode: 'dark',
      background: {
        default: '#111418',
        paper:   '#151a1f',
      },
      text: {
        primary:   '#e7eaee',
        secondary: '#a9b1bb',
      },
      primary: { main: primary },
      divider: 'rgba(231,234,238,0.08)',
    },
    shape: { borderRadius: 12 },
    typography: {
      fontFamily:
        'Inter, system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif',
      h4: { fontWeight: 800, letterSpacing: .2 },
      h5: { fontSize: 22, fontWeight: 700, letterSpacing: .2, lineHeight: 1.3 },
      subtitle2: { fontWeight: 600 },
      body2: { fontSize: 14.5, lineHeight: 1.45 },
      button: { textTransform: 'none', fontWeight: 600 },
    },
    components: {
      // фон без «неонового» свечения
      MuiPaper: {
        styleOverrides: {
          root: { backgroundImage: 'none' },
        },
      },

      // карточка проще: мягкая тень, без градиентной рамки
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundColor: '#151a1f',
            boxShadow: '0 2px 10px rgba(0,0,0,.35)',
            transition: 'transform .18s ease, box-shadow .18s ease',
            '&:hover': {
              transform: 'translateY(-2px)',
              boxShadow: '0 6px 18px rgba(0,0,0,.45)',
            },
          },
        },
      },

      // appbar спокойнее
      MuiAppBar: {
        styleOverrides: {
          root: {
            boxShadow: '0 2px 12px rgba(0,0,0,.35)',
            background: '#0f1317',
          },
        },
      },

      // кнопки — без неон-бордеров
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: {
          root: {
            borderRadius: 10,
            paddingInline: 14,
          },
          containedPrimary: {
            backgroundColor: primary,
            '&:hover': { backgroundColor: alpha(primary, 0.9) },
          },
          textPrimary: {
            color: primary,
            '&:hover': { backgroundColor: alpha(primary, 0.08) },
          },
        },
      },

      // инпут поиска/поля — спокойный фон
      MuiInputBase: {
        styleOverrides: {
          root: {
            backgroundColor: '#0f1317',
            borderRadius: 10,
          },
        },
      },

      // меню/поповеры компактнее
      MuiMenu: {
        styleOverrides: {
          paper: {
            backgroundColor: '#141920',
            boxShadow: '0 8px 24px rgba(0,0,0,.5)',
            borderRadius: 12,
          },
        },
      },
      MuiPopover: {
        styleOverrides: {
          paper: {
            backgroundColor: '#141920',
            boxShadow: '0 8px 24px rgba(0,0,0,.5)',
            borderRadius: 12,
          },
        },
      },

      // разделители и карточные бордеры
      MuiDivider: {
        styleOverrides: {
          root: { borderColor: 'rgba(231,234,238,0.08)' },
        },
      },

      // скелетоны — под тёмный фон
      MuiSkeleton: {
        styleOverrides: {
          root: { backgroundColor: '#242a31' },
        },
      },
    },
  });
};
