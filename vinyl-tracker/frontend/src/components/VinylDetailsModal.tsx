import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, Box } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import axios from '../api/axios';
import { useState, useEffect } from 'react';

interface Vinyl {
  id: number;
  title: string;
  artist: string;
  releaseYear?: number;
  description: string;
  genreId: number;
  coverUrl: string;
}

const genreMap: Record<number, string> = {
  1: "Rock",
  2: "Pop",
  3: "Hip-hop",
  4: "Jazz",
  5: "Electronic",
  6: "Rap",
};

interface Props {
  open: boolean;
  onClose: () => void;
  vinyl: Vinyl | null;
}

export default function VinylDetailsModal({ open, onClose, vinyl }: Props) {
  const { user } = useAuth()!;
  const [isAlreadyAdded, setIsAlreadyAdded] = useState(false);

  useEffect(() => {
    const checkVinylExists = async () => {
      if (!user || !vinyl) return;
      try {
        const res = await axios.get('/api/user/user-vinyls/find', { params: { vinylId: vinyl.id } });
        if (res.status === 200) setIsAlreadyAdded(true);
      } catch (err: any) {
        if (err?.response?.status === 404) setIsAlreadyAdded(false);
        else console.error('Ошибка при проверке винила:', err);
      }
    };
    checkVinylExists();
  }, [vinyl, user]);

  const handleAddVinyl = async () => {
    if (!vinyl || isAlreadyAdded) return;
    try {
      await axios.post('/api/user/user-vinyls/add', null, {
        params: { vinylId: vinyl.id, statusId: 2 },
      });
      setIsAlreadyAdded(true);
      alert(`✅ "${vinyl.title}" добавлена в профиль!`);
    } catch (err) {
      console.error('Ошибка при добавлении винила:', err);
      alert('❌ Не удалось добавить пластинку.');
    }
  };

  if (!vinyl) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{vinyl.title} — {vinyl.artist}</DialogTitle>
      <DialogContent dividers>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Box component="img"
               src={vinyl.coverUrl || '/placeholder-vinyl.jpg'}
               alt={vinyl.title}
               sx={{ width: 150, height: 150, objectFit: 'cover', borderRadius: 2, boxShadow: '0 0 10px rgba(0,255,255,0.2)' }}
               onError={(e: any) => { e.target.src = '/placeholder-vinyl.jpg'; }}
          />
          <Box>
            <Typography><strong>Год выпуска:</strong> {vinyl.releaseYear}</Typography>
            <Typography><strong>Жанр:</strong> {genreMap[vinyl.genreId] || 'Неизвестно'}</Typography>
          </Box>
        </Box>
        <Typography sx={{ mt: 2 }}>{vinyl.description || 'Описание отсутствует.'}</Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Закрыть</Button>
        <Button
          onClick={handleAddVinyl}
          variant="contained"
          color="success"
          disabled={isAlreadyAdded}
        >
          {isAlreadyAdded ? 'Уже в профиле' : 'Добавить в профиль'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
