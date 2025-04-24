import {
  Box, Button, Table, TableBody, TableCell, TableHead, TableRow,
  Typography, Paper, Pagination, Link, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, TableContainer
} from '@mui/material';
import { useEffect, useState } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import VinylUsersModal from '../../components/VinylUsersModal';

interface Vinyl {
  id: number;
  title: string;
  artist: string;
  releaseYear: number;
  genreId: number;
  coverUrl: string;
}

const genreMap: Record<number, string> = {
  1: "Рок",
  2: "Поп",
  3: "Хип-хоп",
  4: "Джаз",
  5: "Электроника",
  6: "Рэп",
};

export default function AdminVinylPage() {
  const [vinyls, setVinyls] = useState<Vinyl[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [editVinyl, setEditVinyl] = useState<Vinyl | null>(null);
  const [selectedVinylId, setSelectedVinylId] = useState<number | null>(null);

  const [filterTitle, setFilterTitle] = useState('');
  const [filterArtist, setFilterArtist] = useState('');
  const [filterYear, setFilterYear] = useState<number | ''>('');
  const [filterGenre, setFilterGenre] = useState('');

  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadVinyls(page);
  }, [page]);

  const loadVinyls = async (pageNumber: number) => {
    try {
      if (filterTitle || filterArtist || filterYear || filterGenre) {
        const res = await axios.get('/api/vinyls/search', {
          params: {
            title: filterTitle || undefined,
            artist: filterArtist || undefined,
            releaseYear: filterYear || undefined,
            genre: filterGenre || undefined
          },
          headers: { Authorization: `Bearer ${auth?.user?.token}` },
        });
        setVinyls(res.data);
        setTotalPages(1);
        setPage(0);
      } else {
        const res = await axios.get('/api/vinyls/page', {
          params: { page: pageNumber, size: 10, sort: 'title,asc' },
          headers: { Authorization: `Bearer ${auth?.user?.token}` },
        });
        setVinyls(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error('Ошибка загрузки винилов:', err);
    }
  };

  const handleSearch = () => loadVinyls(0);

  const handleResetFilters = () => {
    setFilterTitle('');
    setFilterArtist('');
    setFilterYear('');
    setFilterGenre('');
    loadVinyls(0);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Удалить эту пластинку?')) return;
    try {
      await axios.delete(`/api/admin/vinyls/delete/${id}`, {
        headers: { Authorization: `Bearer ${auth?.user?.token}` },
      });
      loadVinyls(page);
    } catch (err) {
      alert('Ошибка при удалении винила');
    }
  };

  const handleSaveEdit = async () => {
    if (!editVinyl) return;
    try {
      await axios.put(`/api/admin/vinyls/update/${editVinyl.id}`, editVinyl, {
        headers: { Authorization: `Bearer ${auth?.user?.token}` },
      });
      alert('✅ Пластинка обновлена!');
      setEditVinyl(null);
      loadVinyls(page);
    } catch (err) {
      alert('❌ Не удалось обновить пластинку.');
      console.error(err);
    }
  };

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom sx={{ color: '#00e5ff', textShadow: '0 0 5px #00e5ff' }}>
        Админ-панель — Винилы
      </Typography>

      <Button
        variant="outlined"
        className="neon-glow"
        onClick={() => navigate('/admin/users')}
        sx={{ mb: 2 }}
      >
        Управление пользователями
      </Button>

      {/* Фильтры */}
      <Paper sx={{ p: 2, mb: 3, backgroundColor: '#1f1f1f', boxShadow: '0 0 10px rgba(0,255,255,0.1)' }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField label="Название" value={filterTitle} onChange={(e) => setFilterTitle(e.target.value)} />
          <TextField label="Артист" value={filterArtist} onChange={(e) => setFilterArtist(e.target.value)} />
          <TextField label="Год" type="number" value={filterYear} onChange={(e) => setFilterYear(e.target.value ? +e.target.value : '')} />
          <Select value={filterGenre} onChange={(e) => setFilterGenre(e.target.value)} displayEmpty className="neon-select">
            <MenuItem value="">Все жанры</MenuItem>
            {Object.entries(genreMap).map(([id, name]) => (
              <MenuItem key={id} value={name}>{name}</MenuItem>
            ))}
          </Select>
          <Button variant="contained" onClick={handleSearch}>Искать</Button>
          <Button onClick={handleResetFilters}>Сбросить</Button>
        </Box>
      </Paper>

      {/* Таблица */}
      <TableContainer component={Paper} sx={{ backgroundColor: '#1f1f1f', boxShadow: '0 0 10px rgba(0,255,255,0.1)' }}>
        <Table size="small">
          <TableHead>
            <TableRow className="neon-header">
              <TableCell>ID</TableCell>
              <TableCell>Название</TableCell>
              <TableCell>Артист</TableCell>
              <TableCell>Год</TableCell>
              <TableCell>Жанр</TableCell>
              <TableCell>Cover URL</TableCell>
              <TableCell>Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vinyls.map((vinyl) => (
              <TableRow key={vinyl.id}>
                <TableCell>{vinyl.id}</TableCell>
                <TableCell>{vinyl.title}</TableCell>
                <TableCell>{vinyl.artist}</TableCell>
                <TableCell>{vinyl.releaseYear}</TableCell>
                <TableCell>{genreMap[vinyl.genreId] || `Жанр #${vinyl.genreId}`}</TableCell>
                <TableCell>
                  <Link
                    href={vinyl.coverUrl}
                    target="_blank"
                    rel="noreferrer"
                    sx={{
                      display: 'inline-block',
                      maxWidth: 150,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                    title={vinyl.coverUrl}
                  >
                    {vinyl.coverUrl}
                  </Link>
                </TableCell>
                <TableCell>
                  <Button size="small" className="action-edit" onClick={() => setEditVinyl(vinyl)}>Изменить</Button>
                  <Button size="small" className="action-delete" onClick={() => handleDelete(vinyl.id)}>Удалить</Button>
                  <Button size="small" className="action-info" onClick={() => setSelectedVinylId(vinyl.id)}>
                    Кто добавил
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
        <Pagination
          count={totalPages}
          page={page + 1}
          onChange={(_, value) => setPage(value - 1)}
          sx={{ button: { color: '#00e5ff' } }}
        />
      </Box>

      {/* Модалки */}
      {selectedVinylId && (
        <VinylUsersModal
          vinylId={selectedVinylId}
          open={!!selectedVinylId}
          onClose={() => setSelectedVinylId(null)}
        />
      )}

      {editVinyl && (
        <Dialog
          open={true}
          onClose={() => setEditVinyl(null)}
          maxWidth="sm"
          fullWidth
          PaperProps={{
            sx: { backgroundColor: '#2a2a2a', boxShadow: '0 0 15px #00e5ff' }
          }}
        >
          <DialogTitle>Редактировать пластинку</DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Название" value={editVinyl.title} onChange={(e) => setEditVinyl({ ...editVinyl, title: e.target.value })} />
            <TextField label="Артист" value={editVinyl.artist} onChange={(e) => setEditVinyl({ ...editVinyl, artist: e.target.value })} />
            <TextField label="Год выпуска" type="number" value={editVinyl.releaseYear} onChange={(e) => setEditVinyl({ ...editVinyl, releaseYear: +e.target.value })} />
            <Select value={editVinyl.genreId} onChange={(e) => setEditVinyl({ ...editVinyl, genreId: +e.target.value })} className="neon-select">
              {Object.entries(genreMap).map(([id, name]) => (
                <MenuItem key={id} value={Number(id)}>{name}</MenuItem>
              ))}
            </Select>
            <TextField label="Cover URL" value={editVinyl.coverUrl} onChange={(e) => setEditVinyl({ ...editVinyl, coverUrl: e.target.value })} />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditVinyl(null)}>Отмена</Button>
            <Button variant="contained" onClick={handleSaveEdit}>Сохранить</Button>
          </DialogActions>
        </Dialog>
      )}
    </Box>
  );
}
