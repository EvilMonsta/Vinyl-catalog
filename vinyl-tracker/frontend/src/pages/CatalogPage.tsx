import { useMemo, useState } from 'react';
import { Box, Chip, Container, Grid, Pagination, Typography, Skeleton, Alert, Stack } from '@mui/material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import axios from '../api/axios';
import VinylCard from '../components/VinylCard';
import VinylDetailsModal from '../components/VinylDetailsModal';

type Vinyl = { id:number; title:string; artist:string; description:string; genreId:number; coverUrl?:string; };

const genres = [
  { id: 1, label: 'Rock' }, { id: 2, label: 'Pop' }, { id: 3, label: 'Hip-Hop' },
  { id: 4, label: 'Jazz' }, { id: 5, label: 'Electronic' }, { id: 6, label: 'Rap' },
];

function useQueryParams() {
  const { search } = useLocation();
  return useMemo(() => new URLSearchParams(search), [search]);
}

export default function CatalogPage() {
  const params = useQueryParams();
  const navigate = useNavigate();
  const [selectedVinyl, setSelectedVinyl] = useState<Vinyl | null>(null);

  const page = Number(params.get('page') || 1);
  const genreIdParam = params.get('genreId');
  const genreId = genreIdParam ? Number(genreIdParam) : undefined;
  const genreLabel = genreId ? (genres.find(g => g.id === genreId)?.label ?? '') : '';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['catalog', { page, genreId }],
    queryFn: async () => {
      const res = await axios.get('/api/vinyls/page', {
        params: { page: page - 1, size: 12, ...(genreId ? { genreId } : {}) },
      });
      const payload = res.data;
      return { items: payload.content ?? [], totalPages: payload.totalPages ?? 1 };
    },
    keepPreviousData: true,
  });

  const totalPages = data?.totalPages ?? 1;
  const items = data?.items ?? [];

  const setGenre = (id?: number) => {
    if (id) params.set('genreId', String(id)); else params.delete('genreId');
    params.set('page', '1');
    navigate(`/catalog?${params.toString()}`);
  };

  const handlePageChange = (_: any, p: number) => {
    params.set('page', String(p));
    navigate(`/catalog?${params.toString()}`);
  };

  return (
    <Container maxWidth={false} sx={{ mt: 4, mb: 6, px: 2 }}>
      {/* ЦЕНТРАЛЬНЫЙ WRAPPER — фиксируем полезную ширину и центрируем всё */}
      <Box sx={{ maxWidth: 1200, mx: 'auto' }}>
        <Box sx={{ display:'flex', alignItems:'center', justifyContent:'space-between', mb: 2 }}>
          <Typography variant="h5" sx={{ fontWeight:900 }}>Каталог</Typography>
          {genreLabel && (
            <Chip label={`Жанр: ${genreLabel}`} onDelete={() => setGenre(undefined)} color="primary" variant="outlined" />
          )}
        </Box>

        {/* Лента жанров */}
        <Stack direction="row" spacing={1} sx={{ mb: 2, flexWrap:'wrap', gap:1, justifyContent:'center' }}>
          <Chip
            label="Все"
            onClick={() => setGenre(undefined)}
            variant={!genreId ? 'filled' : 'outlined'}
            color={!genreId ? 'primary' : 'default'}
            sx={{ height:28 }}
          />
          {genres.map(g => (
            <Chip
              key={g.id}
              label={g.label}
              onClick={() => setGenre(g.id)}
              variant={genreId === g.id ? 'filled' : 'outlined'}
              color={genreId === g.id ? 'primary' : 'default'}
              sx={{ height:28 }}
            />
          ))}
        </Stack>

        {isError && <Alert severity="error" sx={{ mb: 2 }}>Не удалось загрузить каталог.</Alert>}

        {/* СЕТКА — центрируем строки, когда карточек мало */}
        <Grid container spacing={2} justifyContent="center">
          {isLoading
            ? Array.from({ length: 12 }).map((_, i) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={i}>
                <Skeleton variant="rectangular" height={300} sx={{ borderRadius: 2 }} />
              </Grid>
            ))
            : items.map((v: Vinyl) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={v.id}>
                <VinylCard vinyl={v} onOpenDetails={() => setSelectedVinyl(v)} />
              </Grid>
            ))}
        </Grid>

        {totalPages > 1 && (
          <Box sx={{ display:'flex', justifyContent:'center', mt: 3 }}>
            <Pagination page={page} count={totalPages} onChange={handlePageChange} color="primary" />
          </Box>
        )}
      </Box>

      <VinylDetailsModal open={!!selectedVinyl} onClose={() => setSelectedVinyl(null)} vinyl={selectedVinyl} />
    </Container>
  );
}
