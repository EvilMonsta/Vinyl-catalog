import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, Box } from '@mui/material';

type Vinyl = {
  id: number;
  title: string;
  artist: string;
  releaseYear?: number;
  description: string;
  genreId: number;
  coverUrl?: string;
};

const genreMap: Record<number, string> = {
  1: 'Rock',
  2: 'Pop',
  3: 'Hip-hop',
  4: 'Jazz',
  5: 'Electronic',
  6: 'Rap',
};

export default function ProfileVinylDetailsModal({
                                                   open,
                                                   onClose,
                                                   vinyl,
                                                   onDelete,
                                                   deleting = false,
                                                 }: {
  open: boolean;
  onClose: () => void;
  vinyl: Vinyl | null;
  onDelete: (vinylId: number) => void;
  deleting?: boolean;
}) {
  if (!vinyl) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{vinyl.title} — {vinyl.artist}</DialogTitle>
      <DialogContent dividers>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Box
            component="img"
            src={vinyl.coverUrl || '/placeholder-vinyl.jpg'}
            alt={vinyl.title}
            sx={{ width: 150, height: 150, objectFit: 'cover', borderRadius: 2, boxShadow: '0 0 10px rgba(0,255,255,0.2)' }}
            onError={(e: any) => { e.target.src = '/placeholder-vinyl.jpg'; }}
          />
          <Box>
            <Typography><strong>Год выпуска:</strong> {vinyl.releaseYear ?? '—'}</Typography>
            <Typography><strong>Жанр:</strong> {genreMap[vinyl.genreId] || 'Неизвестно'}</Typography>
          </Box>
        </Box>
        <Typography sx={{ mt: 2 }}>{vinyl.description || 'Описание отсутствует.'}</Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Закрыть</Button>
        <Button
          variant="contained"
          color="error"
          onClick={() => onDelete(vinyl.id)}
          disabled={deleting}
        >
          {deleting ? 'Удаляем…' : 'Удалить из профиля'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
