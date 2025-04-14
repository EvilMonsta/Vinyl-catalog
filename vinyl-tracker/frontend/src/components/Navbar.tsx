import {
  AppBar,
  Toolbar,
  Button,
  IconButton,
  Avatar,
  Box,
  Menu,
  MenuItem,
  Typography,
  InputBase,
  Popover,
  ListItem,
  ListItemButton,
  ListItemText,
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

const SearchIcon = () => (
  <Box
    component="span"
    sx={{
      display: 'inline-block',
      width: 24,
      height: 24,
      mr: 1,
      '& svg': {
        fill: '#aaa',
        transition: 'fill 0.3s ease',
      },
      '&:hover svg': {
        fill: '#7cf152',
      },
    }}
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      height="24"
      viewBox="0 0 24 24"
      width="24"
    >
      <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zM10 14a4 4 0 110-8 4 4 0 010 8z" />
    </svg>
  </Box>
);

export default function Navbar() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [exploreAnchor, setExploreAnchor] = useState<null | HTMLElement>(null);

  const open = Boolean(anchorEl);
  const exploreOpen = Boolean(exploreAnchor);

  const handleAvatarClick = (event: React.MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget);
  const handleClose = () => setAnchorEl(null);

  const handleExploreClick = (event: React.MouseEvent<HTMLElement>) => setExploreAnchor(event.currentTarget);
  const handleExploreClose = () => setExploreAnchor(null);

  return (
    <AppBar position="static" sx={{ bgcolor: '#121212', boxShadow: '0 0 8px rgba(0,255,255,0.1)' }}>
      <Toolbar sx={{ gap: 2 }}>
        <Typography
          variant="h6"
          component={Link}
          to="/"
          sx={{
            textDecoration: 'none',
            color: '#00e5ff',
            fontWeight: 'bold',
            textShadow: '0 0 5px #00e5ff',
          }}
        >
          Винилы
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1, maxWidth: 600 }}>
          <InputBase
            placeholder="Поиск по названию, исполнителю и т.д."
            sx={{
              color: 'inherit',
              flex: 1,
              fontSize: '0.95rem',
              backgroundColor: 'rgba(255,255,255,0.05)',
              borderRadius: 2,
              px: 2,
              py: 0.5,
              ml: 2,
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              '& input': {
                padding: 0,
              },
            }}
            startAdornment={<SearchIcon />}
          />

          <Button
            onClick={handleExploreClick}
            sx={{
              ml: 2,
              color: '#00e5ff',
              fontWeight: 'bold',
              textTransform: 'none',
              '&:hover': { color: '#7cf152' },
            }}
          >
            Изучить
          </Button>

          <Popover
            open={exploreOpen}
            anchorEl={exploreAnchor}
            onClose={handleExploreClose}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
            transformOrigin={{ vertical: 'top', horizontal: 'left' }}
            PaperProps={{
              sx: {
                mt: 1,
                p: 2,
                bgcolor: '#1f1f1f',
                color: '#ccc',
                borderRadius: 2,
                boxShadow: '0 0 12px rgba(0,255,255,0.1)',
                minWidth: 800,
              },
            }}
          >
            <Box display="flex" justifyContent="space-evenly" width="100%" alignItems="flex-start">
              {[{
                title: 'Открыть',
                links: [
                  { label: 'Каталог всех пластинок', path: '/catalog' },
                  { label: 'Расширенный поиск', path: '/advanced-search' },
                  { label: 'Популярные релизы', path: '/popular' },
                ],
              }, {
                title: 'Жанры',
                links: ['Rock', 'Electronic', 'Pop', 'Jazz', 'Hip-Hop', 'Classical', 'Folk'].map(g => ({ label: g, path: `/genre/${g.toLowerCase()}` })),
              }, {
                title: 'Внести вклад',
                links: [
                  { label: 'Рейтинг', path: '/rating' },
                  { label: 'Предложить релиз', path: '/contribute' },
                  { label: 'Подписка', path: '/subscription' },
                ],
              }].map((section) => (
                <Box key={section.title} flex={1}>
                  <Typography sx={{ color: '#7cf152', fontWeight: 'bold', fontSize: '1rem', mb: 2, textAlign: 'left', pl: 1 }}>
                    {section.title}
                  </Typography>
                  {section.links.map(({ label, path }) => (
                    <ListItem key={label} disablePadding sx={{ justifyContent: 'flex-start' }}>
                      <ListItemButton
                        component={Link}
                        to={path}
                        sx={{ py: 0.5, minHeight: 34 }}
                      >
                        <ListItemText
                          primary={label}
                          primaryTypographyProps={{
                            sx: {
                              fontSize: '0.8rem',
                              color: '#ccc',
                              textDecoration: 'none',
                            },
                          }}
                        />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </Box>
              ))}
            </Box>
          </Popover>
        </Box>

        <Box sx={{ ml: 'auto' }}>
          {!auth?.user ? (
            <Button color="inherit" component={Link} to="/login">
              Авторизация
            </Button>
          ) : (
            <>
              <IconButton onClick={handleAvatarClick} sx={{ p: 0 }}>
                <Avatar alt={auth.user.username} src={`/api/user/avatar/${auth.user.username}`} />
              </IconButton>

              <Menu anchorEl={anchorEl} open={open} onClose={handleClose}>
                <MenuItem onClick={() => { navigate('/profile'); handleClose(); }}>Профиль</MenuItem>
                <MenuItem onClick={() => { navigate('/subscription'); handleClose(); }}>Подписка</MenuItem>

                {auth.user?.role === 'ADMIN' && (
                  <MenuItem onClick={() => { navigate('/admin/users'); handleClose(); }}>
                    Админка
                  </MenuItem>
                )}

                <MenuItem
                  onClick={() => {
                    handleClose();
                    auth.logout();
                  }}
                >
                  Выход
                </MenuItem>
              </Menu>


            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}
