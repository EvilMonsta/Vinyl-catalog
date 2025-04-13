import {
  Avatar,
  Box,
  Typography,
  Card,
  CardContent,
  CardMedia,
  MenuItem,
  Select,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

interface Vinyl {
  id: number;
  title: string;
  artist: string;
  coverUrl?: string;
  status: {
    id: number;
    name: string;
  };
}

interface User {
  id: number;
  username: string;
  email: string;
}

export default function ProfilePage() {
  const auth = useAuth();
  const [user, setUser] = useState<User | null>(null);
  const [vinyls, setVinyls] = useState<Vinyl[]>([]);
  const [loading, setLoading] = useState(true);
  const [avatarUrl, setAvatarUrl] = useState<string>();
  const [vinylToDelete, setVinylToDelete] = useState<Vinyl | null>(null);

  const token = auth?.user?.token || '';
  const username = auth?.user?.username || '';

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const userRes = await axios.get<User>('/api/user/profile', {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUser(userRes.data);

        const vinylsRes = await axios.get('/api/user/user-vinyls', {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!Array.isArray(vinylsRes.data)) {
          throw new Error('Ожидался массив винилов');
        }

        const parsedVinyls: Vinyl[] = vinylsRes.data.map((item: any) => ({
          id: item.vinyl.id,
          title: item.vinyl.title,
          artist: item.vinyl.artist,
          coverUrl: item.vinyl.coverUrl,
          status: item.status,
        }));

        setVinyls(parsedVinyls);
      } catch (err) {
        console.error('❌ Ошибка при загрузке профиля или винилов:', err);
      } finally {
        setLoading(false);
      }
    };

    void loadProfile();
  }, [token]);

  useEffect(() => {
    const loadAvatar = async () => {
      if (!username) return;
      try {
        const avatarRes = await axios.get(`/api/user/avatar/${username}`, {
          headers: { Authorization: `Bearer ${token}` },
          responseType: 'blob',
        });
        const url = URL.createObjectURL(avatarRes.data);
        setAvatarUrl(url);
      } catch (e) {
        console.warn('⚠️ Не удалось загрузить аватар', e);
      }
    };

    void loadAvatar();
  }, [username, token]);

  const handleStatusChange = async (vinylId: number, newStatusId: number) => {
    try {
      await axios.put('/api/user/user-vinyls/update-status', null, {
        params: { vinylId, newStatusId },
        headers: { Authorization: `Bearer ${token}` },
      });

      setVinyls((prev) =>
        prev.map((v) =>
          v.id === vinylId ? { ...v, status: { ...v.status, id: newStatusId } } : v
        )
      );
    } catch (err) {
      console.error('❌ Ошибка при обновлении статуса:', err);
    }
  };

  const handleDeleteVinyl = async () => {
    if (!vinylToDelete || !user) return;
    try {
      await axios.delete('/api/user/user-vinyls/remove', {
        params: { vinylId: vinylToDelete.id },
        headers: { Authorization: `Bearer ${token}` },
      });
      setVinyls((prev) => prev.filter((v) => v.id !== vinylToDelete.id));
      setVinylToDelete(null);
    } catch (err) {
      console.error('❌ Ошибка при удалении винила:', err);
    }
  };

  if (!auth?.user) return <Typography>Вы не авторизованы</Typography>;
  if (loading) return <Typography>Загрузка профиля...</Typography>;

  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', mt: 4 }}>
      <Box display="flex" alignItems="center" gap={2} mb={4}>
        <Box display="flex" alignItems="center" gap={2} mb={4}>
          <Avatar
            alt={user?.username}
            src={avatarUrl}
            sx={{ width: 80, height: 80, border: '2px solid #0ff', boxShadow: '0 0 10px #0ff' }}
          />
          <Box>
            <Typography variant="h5" sx={{ color: '#0ff', textShadow: '0 0 8px #0ff' }}>{user?.username}</Typography>
            <Typography variant="body1" sx={{ color: '#aaa' }}>{user?.email}</Typography>
          </Box>
        </Box>
      </Box>

      <Typography variant="h5" gutterBottom sx={{ color: '#7cf152', textShadow: '0 0 6px #9f0' }}>
         Моя коллекция винилов
      </Typography>

      <Box
        sx={{
          maxWidth: 1000,
          mx: 'auto',
          mt: 4,
          color: '#eee',
          background: 'linear-gradient(135deg, #1a1a1a 0%, #0d0d0d 100%)',
          borderRadius: 4,
          p: 5,
          boxShadow: '0 0 20px rgba(0,255,255,0.1)',
          backdropFilter: 'blur(8px)',
        }}
      >

        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: {
              xs: '1fr',
              sm: '1fr 1fr',
              md: '1fr 1fr 1fr',
            },
            gap: 2,
          }}
        >
          {vinyls.map((v) => (
            <Card
              key={v.id}
              sx={{
                position: 'relative',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                background: '#222',
                borderRadius: 3,
                overflow: 'hidden',
                boxShadow: '0 0 10px rgba(0,255,255,0.1)',
                transition: 'transform 0.4s ease, box-shadow 0.4s ease',
                '&:hover': {
                  transform: 'translateY(-6px)',
                  boxShadow: '0 0 14px rgba(0,255,255,0.3)',
                },
              }}
            >
              <Box sx={{ position: 'relative', height: 325, backgroundColor: '#111' }}>
                <CardMedia
                  component="img"
                  image={v.coverUrl}
                  alt={v.title}
                  sx={{
                    height: '100%',
                    width: '100%',
                    objectFit: 'contain',
                  }}
                  onError={(e: any) => {
                    e.target.onerror = null;
                    e.target.src = '/placeholder-vinyl.jpg';
                  }}
                />

                <Box
                  onClick={() => setVinylToDelete(v)}
                  className="neon-delete"
                  sx={{
                    position: 'absolute',
                    top: 8,
                    left: 8,
                    width: 26,
                    height: 26,
                    fontSize: '18px',
                    fontWeight: 'bold',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                  }}
                >
                  ×
                </Box>

                <Select
                  size="small"
                  value={v.status.id}
                  onChange={(e) => handleStatusChange(v.id, +e.target.value)}
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
                    MenuListProps: {
                      sx: {
                        outline: 'none',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 0.5,
                        p: 1,
                      },
                    },
                  }}
                  sx={{
                    outline: 'none',
                    position: 'absolute',
                    top: 8,
                    right: 8,
                    minWidth: 40,
                    fontSize: '0.75rem',
                    fontWeight: 'bold',
                    paddingRight: 0,
                    borderRadius: 3,
                    transition: 'background-color 0.25s ease, box-shadow 0.25s ease',
                    backgroundColor:
                      v.status.id === 1
                        ? 'rgba(255, 193, 7, 0.7)'
                        : v.status.id === 2
                          ? 'rgba(76, 175, 80, 0.7)'
                          : 'rgba(33, 150, 243, 0.4)',
                    '&:hover': {
                      boxShadow: '0 0 6px rgba(0,255,255,0.3)',
                      backgroundColor:
                        v.status.id === 1
                          ? 'rgba(255, 193, 7, 0.9)'
                          : v.status.id === 2
                            ? 'rgba(76, 175, 80, 0.9)'
                            : 'rgba(33, 150, 243, 0.3)',
                    },
                    '& .MuiSelect-select': {
                      padding: '4px 6px !important',
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
                        minWidth: 100,
                        mx: 0,
                        my: 0,
                        justifyContent: 'center',
                        animation: `bubbleIn 0.3s ease forwards`,
                        animationDelay: `${idx * 0.05}s`,
                        opacity: 0,
                        transform: 'translateX(-10px) translateY(-10px)',
                        textShadow: '0 1px 3px rgba(0, 0, 0, 0.5)',
                        '&:hover': {
                          bgcolor: 'rgba(0,0,0,0.3)',
                        },
                      }}
                    >
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </Box>

              <CardContent sx={{ py: 1, px: 1.5 }}>
                <Typography variant="subtitle2" noWrap sx={{ color: '#0ff', textShadow: '0 0 4px #0ff' }}>
                  {v.title}
                </Typography>
                <Typography variant="caption" color="text.secondary" noWrap sx={{ color: '#ccc' }}>
                  {v.artist}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>
      </Box>

      <Dialog open={!!vinylToDelete} onClose={() => setVinylToDelete(null)}>
        <DialogTitle>Удалить пластинку?</DialogTitle>
        <DialogContent>
          <Typography>
            Вы действительно хотите удалить{' '}
            <strong>{vinylToDelete?.title}</strong> из вашего профиля?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVinylToDelete(null)}>Отмена</Button>
          <Button color="error" onClick={handleDeleteVinyl}>
            Удалить
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
