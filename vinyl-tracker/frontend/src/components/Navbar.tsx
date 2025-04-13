import {
  AppBar, Toolbar, Button, IconButton, Avatar, Box,
  Menu, MenuItem, Typography,
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

export default function Navbar() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget);
  const handleClose = () => setAnchorEl(null);

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component={Link} to="/" sx={{ textDecoration: 'none', color: 'inherit' }}>
          Винилы
        </Typography>
        <Box sx={{ flexGrow: 1 }} />

        {!auth?.user ? (
          <Button color="inherit" component={Link} to="/login">
            Авторизация
          </Button>
        ) : (
          <>
            <IconButton onClick={handleClick} sx={{ p: 0 }}>
              <Avatar alt={auth.user.username} src={`/api/user/avatar/${auth.user.username}`} />
            </IconButton>
            <Menu anchorEl={anchorEl} open={open} onClose={handleClose}>
              <MenuItem onClick={() => { navigate('/profile'); handleClose(); }}>Профиль</MenuItem>
              <MenuItem onClick={() => { navigate('/subscription'); handleClose(); }}>Подписка</MenuItem>
              <MenuItem
                onClick={() => {
                  handleClose();
                  auth.logout(); // сброс и редирект
                }}
              >
                Выход
              </MenuItem>
            </Menu>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}
