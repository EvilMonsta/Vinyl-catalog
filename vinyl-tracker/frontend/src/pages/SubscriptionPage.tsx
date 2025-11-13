import { Box, Button, Card, CardContent, Container, Grid, Typography, Alert, Chip } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

export default function SubscriptionPage() {
  const auth = useAuth();
  const [message, setMessage] = useState<string | null>(null);

  const handleSubscribe = () => {
    if (auth?.user?.role === 'ADMIN') setMessage('Ты и так админ, зачем тебе подписка?');
    else setMessage('Автор сайта пока не добавил свои реквизиты :P');
  };

  const Item = ({ title, price, features, highlight=false, cta=false }:{
    title:string; price:string; features:string[]; highlight?:boolean; cta?:boolean;
  }) => (
    <Card variant="outlined" sx={{ height:'100%', borderRadius:2, borderColor: highlight ? 'primary.main' : 'divider' }}>
      <CardContent>
        <Box sx={{ display:'flex', alignItems:'center', justifyContent:'space-between', mb:1 }}>
          <Typography variant="h6" sx={{ fontWeight:800 }}>{title}</Typography>
          {highlight && <Chip size="small" color="primary" label="Рекомендовано" />}
        </Box>
        <Typography variant="h4" sx={{ mb: 2, fontWeight:900 }}>{price}</Typography>
        <Box component="ul" sx={{ m:0, p:0, listStyle:'none', display:'grid', gap:1.0 }}>
          {features.map(f => <li key={f}><Typography variant="body2">• {f}</Typography></li>)}
        </Box>
        {cta && (
          <Button fullWidth variant="contained" sx={{ mt: 2 }} onClick={handleSubscribe}>
            Подписаться
          </Button>
        )}
      </CardContent>
    </Card>
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 6, mb: 6 }}>
      <Typography variant="h4" sx={{ mb: 3, fontWeight:900 }}>Подписка</Typography>

      {message && (
        <Alert onClose={() => setMessage(null)} severity="info" sx={{ mb: 3 }}>
          {message}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Item
            title="Обычный"
            price="Бесплатно"
            features={[
              'Просмотр каталога и карточек релизов',
              'Поиск по сайту',
              'Добавление пластинок в профиль',
            ]}
          />
        </Grid>
        <Grid item xs={12} md={6}>
          <Item
            title="VIP"
            price="—"
            features={[
              'Расширенные коллекции и подборки',
              'Персональные рекомендации',
              'Ранний доступ к новым фичам',
            ]}
            highlight
            cta
          />
        </Grid>
      </Grid>

      <Box sx={{ mt: 4, textAlign: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          Как только появятся реквизиты — кнопка оформит подписку. Пока что это демо-экран.
        </Typography>
      </Box>
    </Container>
  );
}
