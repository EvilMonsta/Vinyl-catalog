import { Box, Card, CardMedia, Typography } from '@mui/material';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';

interface VinylCardProps {
  vinyl: {
    id: number;
    title: string;
    artist: string;
    coverUrl?: string;
  };
}

export default function VinylCard({ vinyl }: VinylCardProps) {
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
          setIsAlreadyAdded(false); // не добавлена
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
          statusId: 2, // статус "Имею"
        },
      });
      setIsAlreadyAdded(true);
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
      sx={{
        width: 250,
        position: 'relative',
        backgroundColor: '#1e1e1e',
        borderRadius: 2,
        overflow: 'hidden',
        boxShadow: '0 0 10px rgba(0,255,255,0.1)',
        transition: 'transform 0.3s ease',
        '&:hover': { transform: 'scale(1.05)' },
      }}
    >
      <CardMedia
        component="img"
        height="250"
        image={vinyl.coverUrl || '/placeholder-vinyl.jpg'}
        alt={vinyl.title}
      />
      <Box sx={{ p: 1 }}>
        <Typography variant="subtitle2" noWrap sx={{ color: '#fff' }}>{vinyl.title}</Typography>
        <Typography variant="caption" noWrap sx={{ color: '#aaa' }}>{vinyl.artist}</Typography>
      </Box>

      {/* Плюсик или галочка */}
      {user && (
        <Box
          onClick={isAlreadyAdded ? undefined : handleAddVinyl}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            width: 24,
            height: 24,
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
