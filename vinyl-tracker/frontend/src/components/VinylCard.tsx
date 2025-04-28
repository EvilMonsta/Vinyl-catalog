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
        width: 250,
        position: 'relative',
        backgroundColor: '#1e1e1e',
        borderRadius: 3,
        overflow: 'hidden',
        boxShadow: '0 0 10px rgba(0,255,255,0.1)',
        transition: 'transform 0.3s ease, box-shadow 0.3s ease',
        '&:hover': { transform: 'translateY(-5px)', boxShadow: '0 0 14px rgba(0,255,255,0.2)' },
        cursor: 'pointer',
      }}
    >
      <CardMedia
        component="img"
        height="250"
        src={vinyl.coverUrl}
        alt={vinyl.title}
        onError={(e) => {
          const target = e.target as HTMLImageElement;
          target.onerror = null;
          target.src = '/placeholder-vinyl.png';
        }}
        sx={{ objectFit: 'cover' }}
      />

      <Box sx={{ p: 1 }}>
        <Typography variant="subtitle2" noWrap sx={{ color: '#0ff', textShadow: '0 0 4px #0ff' }}>
          {vinyl.title}
        </Typography>
        <Typography variant="caption" noWrap sx={{ color: '#aaa' }}>
          {vinyl.artist}
        </Typography>
        <Typography variant="caption" sx={{ color: '#69d1d1', display: 'block' }}>
          {genreMap[vinyl.genreId] || 'Неизвестный жанр'}
        </Typography>
      </Box>

      {user && (
        <Box
          onClick={(e) => {
            e.stopPropagation();
            if (!isAlreadyAdded) handleAddVinyl();
          }}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            width: 26,
            height: 26,
            borderRadius: '50%',
            backgroundColor: isAlreadyAdded ? '#7cf152' : '#0ff',
            color: '#000',
            fontWeight: 'bold',
            fontSize: '18px',
            cursor: adding || isAlreadyAdded ? 'default' : 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 6px rgba(0,255,255,0.5)',
            transition: 'all 0.3s',
            '&:hover': {
              backgroundColor: isAlreadyAdded ? '#7cf152' : '#7cf152',
              boxShadow: '0 0 8px rgba(124, 241, 82, 0.6)',
            },
          }}
          title={isAlreadyAdded ? 'Уже в коллекции' : 'Добавить в профиль'}
        >
          {isAlreadyAdded ? '✓' : '+'}
        </Box>
      )}
    </Card>
  );
}
