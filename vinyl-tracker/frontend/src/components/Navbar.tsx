import {
  AppBar, Toolbar, Button, IconButton, Avatar, Box, Menu, MenuItem,
  Typography, InputBase, Paper, Popover, ListItem, ListItemButton, ListItemText, Popper, Container
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState, useEffect } from 'react';
import axios from '../api/axios';
import VinylDetailsModal from './VinylDetailsModal';

const SearchIcon = () => (
  <Box component="span" sx={{ display:'inline-block', width:20, height:20, mr:1, '& svg':{ fill:'#9aa4b2' } }}>
    <svg xmlns="http://www.w3.org/2000/svg" height="20" viewBox="0 0 24 24" width="20">
      <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zM10 14a4 4 0 110-8 4 4 0 010 8z"/>
    </svg>
  </Box>
);

interface Vinyl {
  id: number; title: string; artist: string; releaseYear: number;
  description: string; genreId: number; coverUrl: string;
}

const genreLinks = [
  { label: 'Rock', id: 1 },
  { label: 'Electronic', id: 5 },
  { label: 'Pop', id: 2 },
  { label: 'Jazz', id: 4 },
  { label: 'Hip-Hop', id: 3 },
  { label: 'Rap', id: 6 },
];

export default function Navbar() {
  const auth = useAuth();
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [exploreAnchor, setExploreAnchor] = useState<null | HTMLElement>(null);
  const [selectedVinyl, setSelectedVinyl] = useState<Vinyl | null>(null);

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Vinyl[]>([]);
  const [searchAnchor, setSearchAnchor] = useState<null | HTMLElement>(null);

  const open = Boolean(anchorEl);
  const exploreOpen = Boolean(exploreAnchor);

  const handleAvatarClick = (e: React.MouseEvent<HTMLElement>) => setAnchorEl(e.currentTarget);
  const handleClose = () => setAnchorEl(null);
  const handleExploreClick = (e: React.MouseEvent<HTMLElement>) => setExploreAnchor(e.currentTarget);
  const handleExploreClose = () => setExploreAnchor(null);

  useEffect(() => {
    const t = setTimeout(() => {
      if (searchQuery.trim().length > 1) {
        axios.get(`/api/vinyls/search/global`, { params: { query: searchQuery } })
          .then(res => setSearchResults(res.data.slice(0, 6)))
          .catch(() => setSearchResults([]));
      } else setSearchResults([]);
    }, 500);
    return () => clearTimeout(t);
  }, [searchQuery]);

  return (
    <AppBar position="sticky" color="transparent" sx={{ backdropFilter:'blur(6px)', borderBottom:'1px solid', borderColor:'divider' }}>
      <Container maxWidth="lg">
        <Toolbar disableGutters sx={{ gap: 2, minHeight: 64 }}>
          <Typography
            variant="h6"
            component={Link}
            to="/"
            style={{ textDecoration: 'none' }}
            sx={{ color:'text.primary', fontWeight: 800, letterSpacing:.3 }}
          >
            VinTrack
          </Typography>

          <Box sx={{ display:'flex', alignItems:'center', flexGrow:1, maxWidth: 640, ml: 3 }}>
            <InputBase
              placeholder="Поиск по названию, исполнителю и т.д."
              value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setSearchAnchor(e.currentTarget); }}
              startAdornment={<SearchIcon />}
              sx={{
                flex:1, px: 1.25, py: 0.5, borderRadius: 1.5,
                backgroundColor:'#0f1317', border:'1px solid', borderColor:'divider',
                '& input': { p:0, fontSize:14 }
              }}
            />
            <Popper open={searchResults.length > 0} anchorEl={searchAnchor} placement="bottom-start" sx={{ zIndex: 1300 }}>
              <Paper sx={{ mt: 1, width: 520, p: 0.5 }}>
                {searchResults.map(v => (
                  <Box key={v.id} sx={{ p: 1, borderRadius: 1, cursor:'pointer', '&:hover':{ backgroundColor:'rgba(255,255,255,.04)' } }}
                       onClick={() => setSelectedVinyl(v)}>
                    <Typography variant="subtitle2">{v.title} — {v.artist}</Typography>
                    <Typography variant="caption" sx={{ color:'text.secondary' }}>{v.releaseYear}</Typography>
                  </Box>
                ))}
              </Paper>
            </Popper>

            <Button onClick={handleExploreClick} sx={{ ml: 2 }}>
              Изучить
            </Button>

            <Popover
              open={exploreOpen}
              anchorEl={exploreAnchor}
              onClose={handleExploreClose}
              anchorOrigin={{ vertical:'bottom', horizontal:'left' }}
              transformOrigin={{ vertical:'top', horizontal:'left' }}
              PaperProps={{ sx:{ mt: 1, p: 2, minWidth: 760 } }}
            >
              <Box display="flex" gap={4}>
                {[{
                  title: 'Открыть',
                  links: [{ label:'Каталог всех пластинок', path:'/catalog' }]
                },{
                  title: 'Жанры',
                  links: genreLinks.map(g => ({ label:g.label, path:`/catalog?genreId=${g.id}` }))
                },{
                  title: 'Ещё',
                  links: [{ label:'Подписка', path:'/subscription' }]
                }].map(section => (
                  <Box key={section.title} flex={1}>
                    <Typography sx={{ fontWeight:700, mb:1.5 }}>{section.title}</Typography>
                    {section.links.map(({label, path}) => (
                      <ListItem key={label} disablePadding>
                        <ListItemButton component={Link} to={path} onClick={handleExploreClose}>
                          <ListItemText primary={label} primaryTypographyProps={{ sx:{ fontSize:14 }}} />
                        </ListItemButton>
                      </ListItem>
                    ))}
                  </Box>
                ))}
              </Box>
            </Popover>
          </Box>

          <Box sx={{ ml:'auto' }}>
            {!auth?.user ? (
              <Button component={Link} to="/login">Войти</Button>
            ) : (
              <>
                <IconButton onClick={handleAvatarClick} sx={{ p:0 }}>
                  <Avatar alt={auth.user.username} src={`/api/user/avatar/${auth.user.username}`} />
                </IconButton>
                <Menu anchorEl={anchorEl} open={open} onClose={handleClose}>
                  <MenuItem onClick={() => { navigate('/profile'); handleClose(); }}>Профиль</MenuItem>
                  <MenuItem onClick={() => { navigate('/subscription'); handleClose(); }}>Подписка</MenuItem>
                  {auth.user?.role === 'ADMIN' && (
                    <MenuItem onClick={() => { navigate('/admin/users'); handleClose(); }}>Админка</MenuItem>
                  )}
                  <MenuItem onClick={() => { handleClose(); auth.logout(); }}>Выход</MenuItem>
                </Menu>
              </>
            )}
          </Box>
        </Toolbar>
      </Container>
      <VinylDetailsModal open={!!selectedVinyl} onClose={() => setSelectedVinyl(null)} vinyl={selectedVinyl}/>
    </AppBar>
  );
}
