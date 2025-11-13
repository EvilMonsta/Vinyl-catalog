import {
  Avatar, Box, Typography, Card, CardContent, CardMedia, MenuItem, Select,
  Dialog, DialogTitle, DialogContent, DialogActions, Button, Container
} from '@mui/material';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import axios from '../api/axios';
import ProfileVinylDetailsModal from '../components/ProfileVinylDetailsModal';

interface Vinyl {
  id: number;
  title: string;
  artist: string;
  coverUrl?: string;
  genreId: number;
  description: string;
  releaseYear?: number;        // <— добавили
  status: { id: number; name: string };
}

interface User { id:number; username:string; email:string; }

const genreMap: Record<number,string> = { 1:'Rock',2:'Pop',3:'Hip-hop',4:'Jazz',5:'Electronic',6:'Rap' };

export default function ProfilePage() {
  const auth = useAuth();
  const [user, setUser] = useState<User | null>(null);
  const [vinyls, setVinyls] = useState<Vinyl[]>([]);
  const [loading, setLoading] = useState(true);
  const [avatarUrl, setAvatarUrl] = useState<string>();
  const [vinylToDelete, setVinylToDelete] = useState<Vinyl | null>(null);

  const [detailsOpen, setDetailsOpen] = useState(false);
  const [selectedVinyl, setSelectedVinyl] = useState<Vinyl | null>(null);
  const [deletingInModal, setDeletingInModal] = useState(false);

  const token = auth?.user?.token || '';
  const username = auth?.user?.username || '';

  useEffect(() => {
    const load = async () => {
      try {
        const u = await axios.get<User>('/api/user/profile', { headers:{ Authorization:`Bearer ${token}` }});
        setUser(u.data);
        const r = await axios.get('/api/user/user-vinyls', { headers:{ Authorization:`Bearer ${token}` }});
        const parsed: Vinyl[] = (r.data as any[]).map(item => ({
          id: item.vinyl.id,
          title: item.vinyl.title,
          artist: item.vinyl.artist,
          coverUrl: item.vinyl.coverUrl,
          genreId: item.vinyl.genreId,
          description: item.vinyl.description,
          releaseYear: item.vinyl.releaseYear ?? item.vinyl.year ?? item.vinyl.publishedYear, // <—
          status: item.status,
        }));

        setVinyls(parsed);
      } finally { setLoading(false); }
    };
    void load();
  }, [token]);

  useEffect(() => {
    if (!username) return;
    axios.get(`/api/user/avatar/${username}`, { headers:{ Authorization:`Bearer ${token}` }, responseType:'blob' })
      .then(res => setAvatarUrl(URL.createObjectURL(res.data)))
      .catch(() => {});
  }, [username, token]);

  const handleStatusChange = async (vinylId:number, newStatusId:number) => {
    try {
      await axios.put('/api/user/user-vinyls/update-status', null, {
        params:{ vinylId, newStatusId }, headers:{ Authorization:`Bearer ${token}` }
      });
      setVinyls(prev => prev.map(v => v.id === vinylId ? { ...v, status:{ ...v.status, id:newStatusId } } : v));
    } catch {}
  };

  const handleDeleteVinyl = async () => {
    if (!vinylToDelete) return;
    await axios.delete('/api/user/user-vinyls/remove', {
      params:{ vinylId: vinylToDelete.id }, headers:{ Authorization:`Bearer ${token}` }
    });
    setVinyls(prev => prev.filter(v => v.id !== vinylToDelete.id));
    setVinylToDelete(null);
  };

  const handleDeleteFromModal = async (vinylId:number) => {
    try {
      setDeletingInModal(true);
      await axios.delete('/api/user/user-vinyls/remove', {
        params:{ vinylId }, headers:{ Authorization:`Bearer ${token}` }
      });
      setVinyls(prev => prev.filter(v => v.id !== vinylId));
      setDetailsOpen(false); setSelectedVinyl(null);
    } finally { setDeletingInModal(false); }
  };

  if (!auth?.user) return <Typography>Вы не авторизованы</Typography>;
  if (loading) return <Typography>Загрузка профиля…</Typography>;

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 6 }}>
      <Box sx={{
        display:'flex', alignItems:'center', gap:2, p:2.5, border:'1px solid', borderColor:'divider',
        borderRadius:2, mb:3
      }}>
        <Avatar alt={user?.username} src={avatarUrl} sx={{ width:72, height:72 }} />
        <Box>
          <Typography variant="h6" sx={{ fontWeight:800 }}>{user?.username}</Typography>
          <Typography variant="body2" color="text.secondary">{user?.email}</Typography>
        </Box>
      </Box>

      <Typography variant="h5" sx={{ mb: 2, fontWeight:800 }}>Моя коллекция</Typography>

      <Box sx={{
        display:'grid',
        gridTemplateColumns:{ xs:'1fr', sm:'1fr 1fr', md:'1fr 1fr 1fr' },
        gap:2
      }}>
        {vinyls.map(v => (
          <Card key={v.id}
                onClick={() => { setSelectedVinyl(v); setDetailsOpen(true); }}
                sx={{ cursor:'pointer' }}>
            <Box sx={{ position:'relative', height: 300, backgroundColor:'#0f1317' }}>
              <CardMedia
                component="img"
                image={v.coverUrl || '/placeholder-vinyl.jpg'}
                alt={v.title}
                onError={(e:any) => { e.target.src = '/placeholder-vinyl.jpg'; }}
                sx={{ width:'100%', height:'100%', objectFit:'cover' }}
              />
              <Box
                onClick={(e) => { e.stopPropagation(); setVinylToDelete(v); }}
                sx={{
                  position: 'absolute',
                  top: 6,
                  left: 8,
                  width: 28,
                  height: 28,
                  borderRadius: '50%',
                  fontSize: 16,
                  lineHeight: 1,
                  display: 'grid',
                  placeItems: 'center',
                  cursor: 'pointer',
                  color: 'common.white',
                  bgcolor: 'rgba(0,0,0,0.55)',
                  border: '1px solid rgba(255,255,255,0.25)',
                  boxShadow: 'none',
                  '&:hover': { bgcolor: 'rgba(0,0,0,0.7)' },
                }}
              >
                ×
              </Box>


              <Select
                size="small"
                value={v.status.id}
                onChange={(e) => handleStatusChange(v.id, +e.target.value)}
                onClick={(e) => e.stopPropagation()}
                variant="outlined"
                IconComponent={() => null}
                MenuProps={{
                  PaperProps: {
                    sx: {
                      outline: 'none',
                      background: 'rgba(0,0,0,0.6)',
                      borderRadius: '12px',
                      boxShadow: '0 0 12px rgba(0,255,255,0.3)',
                      backdropFilter: 'blur(6px)',
                      px: 1,
                    },
                  },
                  MenuListProps: { sx: { outline: 'none', display: 'flex', flexDirection: 'column', gap: 0.5, p: 1 } },
                }}
                sx={{
                  outline: 'none',
                  position: 'absolute',
                  top: 6,                 // было 8 — визуально лучше
                  right: 8,
                  minWidth: 72,           // чуть шире, чтобы текст не упирался
                  fontSize: '0.75rem',
                  fontWeight: 'bold',
                  borderRadius: 3,
                  textAlign: 'center',    // <-- центрируем текст
                  '& .MuiSelect-select': {
                    padding: '4px 10px !important',
                    display: 'flex',          // центр через flex
                    alignItems: 'center',
                    justifyContent: 'center', // <-- центрируем
                  },
                  transition: 'background-color 0.25s ease, box-shadow 0.25s ease',
                  backgroundColor:
                    v.status.id === 1
                      ? 'rgba(255, 193, 7, 0.5)'
                      : v.status.id === 2
                        ? 'rgba(76, 175, 80, 0.5)'
                        : 'rgba(33, 150, 243, 0.2)',
                  '&:hover': {
                    boxShadow: '0 0 6px rgba(0,255,255,0.3)',
                    backgroundColor:
                      v.status.id === 1
                        ? 'rgba(255, 193, 7, 0.5)'
                        : v.status.id === 2
                          ? 'rgba(76, 175, 80, 0.4)'
                          : 'rgba(33, 150, 243, 0.3)',
                  },
                }}
              >
                {[{ id: 1, label: 'Хочу' }, { id: 2, label: 'Имею' }, { id: 3, label: 'Едет' }].map((option, idx) => (
                  <MenuItem
                    key={option.id}
                    value={option.id}
                    sx={{
                      outline: 'none',
                      bgcolor: 'rgba(0,0,0,0.4)',
                      backdropFilter: 'blur(6px)',
                      color: 'white',
                      borderRadius: '999px',
                      px: 2,
                      py: 1,
                      minWidth: 120,       // чтобы в меню тоже по центру смотрелось
                      justifyContent: 'center',
                      animation: `bubbleIn 0.3s ease forwards`,
                      animationDelay: `${idx * 0.05}s`,
                      opacity: 0,
                      transform: 'translateX(-10px) translateY(-10px)',
                      textShadow: '0 1px 3px rgba(0,0,0,0.5)',
                      '&:hover': { bgcolor: 'rgba(0,0,0,0.3)' },
                    }}
                  >
                    {option.label}
                  </MenuItem>
                ))}
              </Select>

            </Box>

            <CardContent sx={{ py:1 }}>
              <Typography variant="subtitle2" noWrap sx={{ fontWeight:700 }}>{v.title}</Typography>
              <Typography variant="caption" color="text.secondary" noWrap>{v.artist}</Typography>
              <Typography variant="caption" sx={{ display:'block' }}>{genreMap[v.genreId] || 'Genre'}</Typography>
            </CardContent>
          </Card>
        ))}
      </Box>

      <Dialog open={!!vinylToDelete} onClose={() => setVinylToDelete(null)}>
        <DialogTitle>Удалить пластинку?</DialogTitle>
        <DialogContent>
          <Typography>Удалить <strong>{vinylToDelete?.title}</strong> из вашего профиля?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVinylToDelete(null)}>Отмена</Button>
          <Button color="error" onClick={handleDeleteVinyl}>Удалить</Button>
        </DialogActions>
      </Dialog>

      <ProfileVinylDetailsModal
        open={detailsOpen}
        onClose={() => { setDetailsOpen(false); setSelectedVinyl(null); }}
        vinyl={selectedVinyl}
        onDelete={handleDeleteFromModal}
        deleting={deletingInModal}
      />
    </Container>
  );
}
