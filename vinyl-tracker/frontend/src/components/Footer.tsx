import { Box, Typography, Link } from '@mui/material';

export default function Footer() {
  return (
    <Box sx={{ mt: 10, py: 4, bgcolor: '#121212', textAlign: 'center', color: '#aaa' }}>
      <Typography variant="body2">© 2025 VinylTracker. Все права защищены.</Typography>
      <Box sx={{ mt: 1 }}>
        <Link href="/privacy" color="inherit" underline="hover" sx={{ mx: 1 }}>
          Политика конфиденциальности
        </Link>
        <Link href="/terms" color="inherit" underline="hover" sx={{ mx: 1 }}>
          Условия использования
        </Link>
        <Link href="/about" color="inherit" underline="hover" sx={{ mx: 1 }}>
          О нас
        </Link>
      </Box>
    </Box>
  );
}
