import { Box, Button, Container, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

export default function WipPage({ title = 'Страница в разработке' }: { title?: string }) {
  return (
    <Container sx={{ mt: 6, textAlign: 'center' }}>
      <Typography variant="h4" sx={{ mb: 2, color: '#7cf152' }}>{title}</Typography>
      <Typography sx={{ opacity: 0.85, mb: 3 }}>
        Автор сайта пока не добавил нужные компоненты. Загляните позже 🙃
      </Typography>
      <Button component={Link} to="/" variant="contained" color="primary">
        На главную
      </Button>
    </Container>
  );
}
