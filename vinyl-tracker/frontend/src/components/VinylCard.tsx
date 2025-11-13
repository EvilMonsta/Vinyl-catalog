import { Box, Card, CardMedia, Typography } from '@mui/material';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';

interface VinylCardProps {
  vinyl: {
    id: number;
    title: string;
    artist: string;
    genreId: number;
    description: string;
    coverUrl?: string;
  };
  onOpenDetails: () => void;
  onAdded?: (vinylId: number) => void;
}

const genreMap: Record<number, string> = {
  1: "Rock",
  2: "Pop",
  3: "Hip-hop",
  4: "Jazz",
  5: "Electronic",
  6: "Rap",
};

export default function VinylCard({ vinyl, onOpenDetails, onAdded }: VinylCardProps) {
  const { user } = useAuth()!;
  const [adding, setAdding] = useState(false);
  const [isAlreadyAdded, setIsAlreadyAdded] = useState(false);

  useEffect(() => {
    const checkVinylExists = async () => {
      if (!user) return;

      try {
        const res = await axios.get('/api/user/user-vinyls/find', {
          params: { vinylId: vinyl.id },
        });
        if (res.status === 200) {
          setIsAlreadyAdded(true);
        }
      } catch (err: any) {
        if (err?.response?.status === 404) {
          setIsAlreadyAdded(false);
        } else {
          console.error('Ошибка при проверке наличия винила у пользователя:', err);
        }
      }
    };

    checkVinylExists();
  }, [vinyl.id, user]);

  const handleAddVinyl = async () => {
    if (adding || isAlreadyAdded) return;

    try {
      setAdding(true);
      await axios.post('/api/user/user-vinyls/add', null, {
        params: {
          vinylId: vinyl.id,
          statusId: 2,
        },
      });
      setIsAlreadyAdded(true);
      onAdded?.(vinyl.id);
      alert(`✅ Пластинка "${vinyl.title}" добавлена в профиль!`);
    } catch (err) {
      console.error('Ошибка при добавлении винила', err);
      alert('❌ Не удалось добавить пластинку. Попробуйте позже.');
    } finally {
      setAdding(false);
    }
  };

  return (
    <Card
      onClick={onOpenDetails}
      sx={{
        width: 240,
        bgColor: 'background.paper',
        borderRadius: 3,
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,.35)',
        transition: 'transform .18s ease, box-shadow .18s ease',
        cursor: 'pointer',
        '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 6px 18px rgba(0,0,0,.45)' },
      }}
    >
      <Box sx={{ position: 'relative' }}>
        <CardMedia
          component="img"
          alt={vinyl.title}
          src={vinyl.coverUrl}
          onError={(e) => {
            const t = e.target as HTMLImageElement;
            t.onerror = null; t.src = '/placeholder-vinyl.png';
          }}
          sx={{
            width: '100%',
            aspectRatio: '1 / 1',   // КВАДРАТ
            objectFit: 'cover'
          }}
        />

        {/* затемнённый градиент внизу */}
        <Box sx={{
          position: 'absolute', left: 0, right: 0, bottom: 0, height: '42%',
          background: 'linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,.55) 40%, rgba(0,0,0,.7) 100%)'
        }} />

        {/* плюсик компактно */}
        {user && (
          <Box
            onClick={(e) => { e.stopPropagation(); if (!isAlreadyAdded) handleAddVinyl(); }}
            sx={{
              position: 'absolute', top: 8, right: 8, width: 25, height: 25,
              borderRadius: '50%', bgcolor: isAlreadyAdded ? 'success.main' : 'rgba(0,0,0,.55)',
              color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center',
              border: '1px solid rgba(255,255,255,.25)', backdropFilter: 'blur(4px)',
              '&:hover': { bgcolor: isAlreadyAdded ? 'success.main' : 'rgba(0,0,0,.7)' }
            }}
            title={isAlreadyAdded ? 'Уже в коллекции' : 'Добавить в профиль'}
          >
            {isAlreadyAdded ? '✓' : '+'}
          </Box>
        )}
      </Box>

      <Box sx={{ px: 1.25, py: 1 }}>
        <Typography variant="subtitle2" sx={{ color: 'text.primary' }} noWrap>
          {vinyl.title}
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary' }} noWrap>
          {vinyl.artist}
        </Typography>
        <Typography variant="caption" sx={{ color: 'primary.main', display: 'block', mt: .25 }}>
          {genreMap[vinyl.genreId] || 'Genre'}
        </Typography>
      </Box>
    </Card>
  );
}
